package com.example.community.Screens.OrganizerScreens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
 * Fragment displaying the organizer's home screen with event management and navigation options
 *<p>
 *     Fragment is the main hub for organizers. Shows a list of events they have created.
 *     Provides access to creating new events, viewing notifications, managing their profile,
 *     sending notifcations, and viewing event geolocation data.
 *</p>
 * <p>
 *     Loads all the events created by the current organizer and displays them in a RecyclerView.
 *     Clicking on an event navigates to the OrganzierEventDescriptionFragment for event management.
 * </p>
 *
 * @see EventArrayAdapter
 * @see EventService
 * @see UserService
 */
public class OrganizerHomeFragment extends Fragment {
    /** Tag for logging */
    private static final String TAG = "OrganizerHomeFragment";
    /**
     * UI elements
     */
    private ImageButton notificationsButton, cameraButton;
    private Button guideButton, filterButton, createButton, notifyButton;
    private Button geolocationButton, myProfileButton;
    private RecyclerView hostEventList;

    /** List of events created by the current organizer. */
    private ArrayList<Event> eventsArrayList;

    /** Identifier of the current organizer */
    private String currentOrganizerID;

    /**
     * Adapter for managing the RecyclerView and event click handling
     */
    private EventArrayAdapter eventArrayAdapter;

    /** Service for event data */
    private EventService eventService;

    /** Service for user data */
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View organizerHomeFragment = inflater.inflate(R.layout.organizer_main_page, container, false);
        return organizerHomeFragment;
    }

    /**
     * Initializes the fragment's UI and data. Sets up click listeners, loads organizer data, and
     * sets up the event list.
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

        // Initialize UI components
        notificationsButton = view.findViewById(R.id.organizerNotifications);
        cameraButton = view.findViewById(R.id.buttonCamera);
        guideButton = view.findViewById(R.id.buttonGuide);
        filterButton = view.findViewById(R.id.buttonFilter);
        createButton = view.findViewById(R.id.buttonCreate);
        notifyButton = view.findViewById(R.id.buttonNotify);
        geolocationButton = view.findViewById(R.id.buttonGeolocation);
        myProfileButton = view.findViewById(R.id.buttonMyProfile);
        hostEventList = view.findViewById(R.id.HostEventView);

        // Set up RecyclerView
        hostEventList.setLayoutManager(new LinearLayoutManager(getContext()));
        eventArrayAdapter = new EventArrayAdapter(eventsArrayList);
        eventArrayAdapter.setOnEventClickListener(event -> {
            Bundle args = new Bundle();
            args.putString("event_id", event.getEventID());
            NavHostFragment.findNavController(OrganizerHomeFragment.this)
                    .navigate(R.id.action_OrganizerHomeFragment_to_OrganizerEventDescriptionFragment, args);
        });
        hostEventList.setAdapter(eventArrayAdapter);

        loadOrganizerData();
        setUpClickListeners();
    }

    /**
     * Loads the current organizer's data using their device token and initiates event loading.
     */
    private void loadOrganizerData() {
        String deviceToken = userService.getDeviceToken();
        userService.getByDeviceToken(deviceToken)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Log.e(TAG, "user does not exist");
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.d(TAG, "user found: " + user.getUserID());
                    currentOrganizerID = user.getUserID();
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "failed to load user data", e);
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
                    Log.d(TAG, "events loaded: " + events.size());
                    if (eventsArrayList != null) {
                        eventsArrayList.clear();
                        eventsArrayList.addAll(events);
                        eventArrayAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "failed to load events", e);
                    Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Sets up the click listeners for all navigation and action buttons in the fragment.
     */
    private void setUpClickListeners() {
        notificationsButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(OrganizerHomeFragment.this)
                    .navigate(R.id.action_OrganizerHomeFragment_to_NotificationsFragment);
        });

        myProfileButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(OrganizerHomeFragment.this)
                    .navigate(R.id.action_OrganizerHomeFragment_to_OrganizerProfileFragment);
        });

        createButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(OrganizerHomeFragment.this)
                    .navigate(R.id.action_OrganizerHomeFragment_to_CreateEventFragment);
        });

        // Temporary toast messages for unimplemented features
        cameraButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Camera feature not implemented yet", Toast.LENGTH_SHORT).show());

        guideButton.setOnClickListener(v ->
                Toast.makeText(getActivity(), "Guide feature not implemented yet", Toast.LENGTH_SHORT).show());

        notifyButton.setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerHomeFragment.this)
                        .navigate(R.id.action_OrganizerHomeFragment_to_HostNotifyFragment)
        );

        geolocationButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(OrganizerHomeFragment.this)
                    .navigate(R.id.action_OrganizerHomeFragment_to_GeolocationFragment);
        });
    }
}