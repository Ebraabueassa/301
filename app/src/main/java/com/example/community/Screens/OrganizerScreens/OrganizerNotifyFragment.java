package com.example.community.Screens.OrganizerScreens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.ArrayAdapters.EventArrayAdapter;
import com.example.community.Event;
import com.example.community.EventService;
import com.example.community.R;
import com.example.community.UserService;

import java.util.ArrayList;

/**
 * Fragment for managin event notifications and selecting notification targets/
 * <p>
 *     Fragment displays a list of events created by the current organizer and allows
 *     them to select an event to send notifications to specific user groups.
 *     When an event is selected, a dialog fragment opens where the organizer can choose
 *     which user groups to notify.
 * </p>
 * <p>
 *     Fragment lloads all the events created by the current organizer and displays them in a
 *     RecyclerView. Clicking on an event opens a NotificationTargetDialogFragment where the organizer can select
 *     which user groups to notify.
 * </p>
 *
 * @see EventService
 * @see UserService
 * @see EventArrayAdapter
 * @see NotificationTargetDialogFragment
 */
public class OrganizerNotifyFragment extends Fragment {

    /**
     * Tag for logging
     */
    private static final String TAG = "OrganizerNotifyFragment";

    /**
     * UI elements
     */
    private RecyclerView notifyEventList;
    private Button backButton;

    /**
     * Adapter for managing the RecyclerView and event click handling.
     */
    private EventArrayAdapter eventArrayAdapter;
    /**
     * List of events created by the current organizer
     */
    private ArrayList<Event> eventsArrayList;
    /**
     * The identifier of the current organizer
     */
    private String currentOrganizerID;

    /**
     * Service for event data
     */
    private EventService eventService;
    /**
     * Service for user data
     */
    private UserService userService;

    /**
     * Inflates the dialog's layout view
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_notify_fragment, container, false);
        return view;
    }

    /**
     * Initializes the fragment's UI and data. Sets up click listeners, loads organizer data, and
     * sets up the RecyclerView adapter.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new EventService();
        userService = new UserService();
        eventsArrayList = new ArrayList<>();

        notifyEventList = view.findViewById(R.id.notifyEventRecyclerView);
        backButton = view.findViewById(R.id.buttonBack);

        notifyEventList.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventArrayAdapter = new EventArrayAdapter(eventsArrayList);
        eventArrayAdapter.setOnEventClickListener(event -> {
            NotificationTargetDialogFragment dialogFragment = NotificationTargetDialogFragment.newInstance(event.getEventID());
            dialogFragment.show(getChildFragmentManager(), "notification_target");
        });


        notifyEventList.setAdapter(eventArrayAdapter);

        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        loadOrganizerData();
    }


    /**
     * Loads the current organizer's data using their device token and initiates event loading.
     */
    private void loadOrganizerData() {
        String deviceToken = userService.getDeviceToken();
        userService.getByDeviceToken(deviceToken)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Log.e(TAG, "User not found");
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, "User found: " + user.getUserID());
                    currentOrganizerID = user.getUserID();
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load organizer data", e);
                    Toast.makeText(getContext(), "Failed to load organizer data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads all the events created by the current organizer and fills the RecyclerView
     *
     * Displays error message if the event loading stuff fails
     */
    private void loadEvents() {

        eventService.listEventsByOrganizer(currentOrganizerID, 100, null)
                .addOnSuccessListener(events -> {
                    Log.d(TAG, "Events loaded: " + events.size());
                    if (eventsArrayList != null) {
                        eventsArrayList.clear();
                        eventsArrayList.addAll(events);
                        Log.d(TAG, "Events added to list: " + eventsArrayList.size());
                        eventArrayAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load events", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }
}

