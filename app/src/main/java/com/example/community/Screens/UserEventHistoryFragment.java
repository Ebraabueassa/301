package com.example.community.Screens;

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
import com.example.community.EntryStatus;
import com.example.community.Event;
import com.example.community.EventService;
import com.example.community.R;
import com.example.community.UserService;
import com.example.community.WaitingListEntry;
import com.example.community.WaitlistRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment displaying the event history of the current user.
 * Provides a simple back button to navigate to the previous screen.
 */
public class UserEventHistoryFragment extends Fragment {

    private static final String TAG = "UserEventHistory";

    private RecyclerView joinedEventsRecyclerView;
    private RecyclerView notSelectedEventsRecyclerView;
    private Button backButton;

    private ArrayList<Event> joinedEvents = new ArrayList<>();
    private ArrayList<Event> notSelectedEvents = new ArrayList<>();

    private EventArrayAdapter joinedAdapter;
    private EventArrayAdapter notSelectedAdapter;

    private UserService userService;
    private WaitlistRepository waitlistRepository;
    private EventService eventService;

    /**
     * Inflates the layout for the user's event history page.
     *
     * @param inflater           LayoutInflater used to inflate views
     * @param container          Parent container
     * @param savedInstanceState Saved instance state
     * @return The inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_event_page, container, false);

        // Init views
        joinedEventsRecyclerView = view.findViewById(R.id.joinedEventsRecyclerView);
        notSelectedEventsRecyclerView = view.findViewById(R.id.notSelectedEventsRecyclerView);
        backButton = view.findViewById(R.id.back);

        // Init services
        userService = new UserService();
        waitlistRepository = new WaitlistRepository();
        eventService = new EventService();

        // Set up RecyclerViews
        joinedAdapter = new EventArrayAdapter(joinedEvents);
        notSelectedAdapter = new EventArrayAdapter(notSelectedEvents);

        joinedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notSelectedEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        joinedEventsRecyclerView.setAdapter(joinedAdapter);
        notSelectedEventsRecyclerView.setAdapter(notSelectedAdapter);

        // Back button → previous screen
        backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(UserEventHistoryFragment.this).popBackStack()
        );

        // Load the user's history
        loadHistory();

        return view;
    }

    private void loadHistory() {
        // Get device token from FirebaseAuth
        String deviceToken = userService.getDeviceToken();
        Log.d(TAG, "Got deviceToken = " + deviceToken);

        // Convert device token → app userId
        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(userId -> {
                    Log.d(TAG, "Got userId = " + userId);
                    loadHistoryForUser(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get userId from device token", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load event history.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadHistoryForUser(String userId) {
        Log.d(TAG, "Loading history for userId = " + userId);

        waitlistRepository.listByUser(userId)
                .addOnSuccessListener(entries -> {
                    Log.d(TAG, "Found " + entries.size() + " waitlist entries for user");

                    joinedEvents.clear();
                    notSelectedEvents.clear();

                    // If no entries → nothing to load
                    if (entries.isEmpty()) {
                        joinedAdapter.notifyDataSetChanged();
                        notSelectedAdapter.notifyDataSetChanged();
                        return;
                    }

                    List<Task<Event>> fetchTasks = new ArrayList<>();

                    for (WaitingListEntry entry : entries) {
                        final String eventId = entry.getEventID();
                        final EntryStatus status = entry.getStatus();

                        Task<Event> fetchTask = eventService.getEvent(eventId)
                                .addOnSuccessListener(event -> {
                                    if (event == null) return;

                                    // Joined events (ACCEPTED)
                                    if (status == EntryStatus.ACCEPTED) {
                                        joinedEvents.add(event);
                                    }
                                    // Not selected / waiting / cancelled / declined
                                    else if (status == EntryStatus.DECLINED
                                            || status == EntryStatus.WAITING
                                            || status == EntryStatus.CANCELLED) {
                                        notSelectedEvents.add(event);
                                    }
                                });

                        fetchTasks.add(fetchTask);
                    }

                    // Wait for all event fetches to finish
                    Tasks.whenAllComplete(fetchTasks)
                            .addOnSuccessListener(done -> {
                                Log.d(TAG, "Finished fetching events for history. " +
                                        "Joined=" + joinedEvents.size() +
                                        " NotSelected=" + notSelectedEvents.size());
                                joinedAdapter.notifyDataSetChanged();
                                notSelectedAdapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed while fetching events for history", e);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(),
                                            "Failed to load some events.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load waitlist entries for user", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load history.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

