package com.example.community.Screens.OrganizerScreens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
 * Fragment for displaying organizer's events with geolocation support.
 * <p>
 *     Allows organizers to view a list of their events that have geolocation enabled and select
 *     one to view the locations of where entrants joined the waitlist from.
 *     The fragments loads all events of the organizer and filters out the ones that do not have
 *     geolocation enabled.
 *     When an event is selected, fragment navigates to OrganizerGeolocationMapFragment with the event ID.
 * </p>
 *
 * @see OrganizerGeolocationMapFragment
 * @see EventArrayAdapter
 * @see EventService
 * @see UserService
 *
 */
public class OrganizerGeolocationFragment extends Fragment {

    /**
     * Tag for logging
     */
    public static final String TAG = "OrganizerGeolocationFragment";

    private RecyclerView eventListRecyclerView;
    private Button backButton;
    private ArrayList<Event> eventsArrayList;
    private EventArrayAdapter eventArrayAdapter;
    private EventService eventService;
    private UserService userService;
    private String currentOrganizerID;

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
     * @return the view of the fragment's layout
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_geolocation_page, container, false);
    }

    /**
     * Initializes the fragment's UI elements and services. Sets up the RecyclerView
     * adapter with event click listeners, and loads the organizer's geolocation-enabled
     * events
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventService = new EventService();
        userService = new UserService();
        eventsArrayList = new ArrayList<>();

        eventListRecyclerView = view.findViewById(R.id.geolocationEventList);
        backButton = view.findViewById(R.id.geolocationBackButton);

        eventListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventArrayAdapter = new EventArrayAdapter(eventsArrayList);
        eventArrayAdapter.setOnEventClickListener(event -> {
            Log.d(TAG, "Event clicked: " + event.getEventID());
            navigateToMap(event.getEventID());
            });
        eventListRecyclerView.setAdapter(eventArrayAdapter);

        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        loadOrganizerEventsWithGeolocation();
    }

    /**
     * Loads the organizer's data and the events that have geolocation enabled
     */
    private void loadOrganizerEventsWithGeolocation() {
        String deviceToken = userService.getDeviceToken();
        userService.getByDeviceToken(deviceToken)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentOrganizerID = user.getUserID();
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load organizer data", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads all events created by the current organizer and filters for geolocatio-enabled ones.
     * Retrieves 100 events and populates the RecyclerView with only those events
     *
     * Displays error message if the event loading fails.
     */
    private void loadEvents() {
        eventService.listEventsByOrganizer(currentOrganizerID, 100, null)
                .addOnSuccessListener(events -> {
                    if (eventsArrayList != null) {
                        eventsArrayList.clear();
                        // Only add events that have geolocation enabled
                        for (Event event : events) {
                            if (event.getRequiresGeolocation()) {
                                eventsArrayList.add(event);
                            }
                        }
                        eventArrayAdapter.notifyDataSetChanged();

                        if (eventsArrayList.isEmpty()) {
                            Toast.makeText(getContext(), "No events with geolocation enabled", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Navigates to the OrganizerGeolocationMapFragment with the event ID
     *
     * @param eventID the identifier of the event to view on the map
     */
    private void navigateToMap(String eventID) {
        Bundle args = new Bundle();
        args.putString("event_id", eventID);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_GeolocationFragment_to_GeolocationMapFragment, args);
    }
}