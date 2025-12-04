package com.example.community.Screens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.ArrayAdapters.NotificationArrayAdapter;
import com.example.community.Notification;
import com.example.community.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fragment for admin to view and manage notifications
 * <p>
 *     Displays a log of all notifications sent to users. Admin can view notification the notification message that was sent and the notification
 *     message.
 * </p>
 * <p>
 *     Fragment loads all notifications from the database and displays them in a RecyclerView.
 * </p>
 *
 * @see NotificationArrayAdapter
 * @see Notification
 */

public class AdminNotificationFragment extends Fragment {

    /**
     * RecyclerView for displaying the list of notifications
     */
    private RecyclerView recyclerView;

    /**
     * Adapter for managing the RecyclerView items and display
     */
    private NotificationArrayAdapter adapter;

    /**
     * List of notifications to be displayed
     */
    private List<Notification> notificationList;

    /**
     * Firebase Firestore instance for querying notification and event data
     */
    private FirebaseFirestore db;

    /**
     * Button to go back to previous fragment
     */
    private Button backButton;

    /**
     * Map of event IDs to event titles for displaying notification details
     */
    private final Map<String, String> eventTitleMap = new HashMap<>();

    /**
     * Inflates the layout for this fragment
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_notification_page, container, false);
    }

    /**
     * Initializes the fragment's UI. Initializes Firestore, sets up adapter, loads notification data,
     * sets up back button listener
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.adminNotificationView);
        backButton = view.findViewById(R.id.buttonBack);
        TextView headerTitle = view.findViewById(R.id.headerTitle);

        if (headerTitle != null) {
            headerTitle.setText("Notification Logs");
        }

        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationArrayAdapter(notificationList, eventTitleMap);
        recyclerView.setAdapter(adapter);

        loadNotifications();

        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminNotificationFragment.this).navigateUp();
        });
    }

    /**
     * Loads all notifications from the database and displays them in the RecyclerView
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Queries the "notifications" collection in Firestore</li>
     *         <li>Clears the notification list and event ID set</li>
     *         <li>Iterates through the query results</li>
     *         <li>Adds each notification to the list</li>
     *         <li>Adds unique event IDs to the set</li>
     *         <li>Sorts the notifications by issue date</li>
     *         <li>Updates the adapter</li>
     *     </ul>
     * </p>
     *
     * Logs errors if notification loading or document conversion fails.
     */
    private void loadNotifications() {
        db.collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null) return;

                    notificationList.clear();
                    Set<String> uniqueEventIds = new HashSet<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Notification notification = document.toObject(Notification.class);
                            notificationList.add(notification);

                            if (notification.getEventID() != null && !notification.getEventID().isEmpty()) {
                                uniqueEventIds.add(notification.getEventID());
                            }
                        } catch (Exception e) {
                            Log.e("AdminNotification", "Error converting document", e);
                        }
                    }

                    if (notificationList.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "No notifications found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    fetchEventTitlesAndSort(uniqueEventIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminNotification", "Error loading notifications", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fetches event titels for all event IDs and sorts notifications
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Creates a list of tasks to fetch event details</li>
     *         <li>Queries the "events" collection for each event ID</li>
     *         <li>Clears the event title map</li>
     *         <li>Iterates through the query results</li>
     *         <li>Adds each event title to the map</li>
     *         <li>Sorts the notifications by issue date</li>
     *         <li>Updates the adapter</li>
     *     </ul>
     * </p>
     *
     *
     *
     * @param eventIds set of event IDs to fetch titles for
     */
    private void fetchEventTitlesAndSort(Set<String> eventIds) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (String eid : eventIds) {
            tasks.add(db.collection("events").document(eid).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(objects -> {
            if (getContext() == null) return;

            eventTitleMap.clear();

            for (Object obj : objects) {
                DocumentSnapshot snapshot = (DocumentSnapshot) obj;
                if (snapshot.exists()) {
                    String title = snapshot.getString("title");
                    eventTitleMap.put(snapshot.getId(), title != null ? title : "Unknown Event");
                }
            }

            sortNotifications();

            adapter.notifyDataSetChanged();

        }).addOnFailureListener(e -> {
            Log.e("AdminNotification", "Error fetching event details", e);
            sortNotifications();
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Sorts notifications by issue date (newest first), then by event title, then by event ID.
     */
    private void sortNotifications() {
        Collections.sort(notificationList, (n1, n2) -> {
            long d1 = n1.getIssueDate();
            long d2 = n2.getIssueDate();
            int dateComp = Long.compare(d2, d1);
            if (dateComp != 0) return dateComp;

            String id1 = n1.getEventID() != null ? n1.getEventID() : "";
            String id2 = n2.getEventID() != null ? n2.getEventID() : "";

            String title1 = eventTitleMap.getOrDefault(id1, "General Notifications");
            String title2 = eventTitleMap.getOrDefault(id2, "General Notifications");

            int titleComp = title1.compareToIgnoreCase(title2);
            if (titleComp != 0) return titleComp;

            return id1.compareTo(id2);
        });
    }
}
