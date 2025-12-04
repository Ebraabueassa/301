package com.example.community.Screens;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.Event;
import com.example.community.EventService;
import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;
import com.example.community.WaitingListEntry;
import com.example.community.WaitingListEntryService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.GeoPoint;

/**
 * Fragment for displaying detailed information about an event to an entrant.
 * Provides options to join or leave the event waitlist.
 */
public class EntrantEventDescriptionFragment extends Fragment {

    public static final String TAG = "EventDescriptionFragment";

    private static final String ARG_EVENT_ID = "event_id";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Event currentEvent;
    private User currentUser;
    private WaitingListEntryService waitingListEntryService;
    private UserService userService;
    private EventService eventService;
    private String currentEntrantId;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView eventTitle, eventDescription, eventLocation, eventDates
            ,registrationDates, capacity, organizerUsername, organizerEmail, organizerPhone,
            waitlistCapacity;
    private Button waitlistButton, backButton;
    private ProgressBar loadingScreen;

    /**
     * Creates a new instance of this fragment with the specified event ID.
     *
     * @param eventId The ID of the event to display
     * @return A new instance of EntrantEventDescriptionFragment
     */
    public static EntrantEventDescriptionFragment newInstance(String eventId) {
        EntrantEventDescriptionFragment fragment = new EntrantEventDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           LayoutInflater object to inflate views
     * @param container          Parent container for the fragment
     * @param savedInstanceState Saved instance state bundle
     * @return The inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View eventDescriptionFragment = inflater.inflate(R.layout.entrant_event_description, container, false);
        return eventDescriptionFragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        waitingListEntryService = new WaitingListEntryService();
        eventService = new EventService();
        userService = new UserService();

        // Get current entrant ID from device token
        String deviceToken = userService.getDeviceToken();
        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(userId -> currentEntrantId = userId);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        eventTitle = view.findViewById(R.id.eventTitle);
        eventDescription = view.findViewById(R.id.eventDescription);
        eventLocation = view.findViewById(R.id.eventLocation);
        eventDates = view.findViewById(R.id.eventDates);
        registrationDates = view.findViewById(R.id.registrationDates);
        capacity = view.findViewById(R.id.capacity);
        organizerUsername = view.findViewById(R.id.eventOrganizerName);
        organizerEmail = view.findViewById(R.id.eventOrganizerEmail);
        organizerPhone = view.findViewById(R.id.eventOrganizerPhone);
        waitlistCapacity = view.findViewById(R.id.waitlistCount);
        waitlistButton = view.findViewById(R.id.waitlistButton);
        backButton = view.findViewById(R.id.backButton);
        loadingScreen = view.findViewById(R.id.loadingScreen);

        showLoadingScreen();
        loadEventDetails();

        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(EntrantEventDescriptionFragment.this)
                    .navigateUp();
        });
    }

    private void loadEventDetails() {
        String eventId = getArguments().getString(ARG_EVENT_ID);
        eventService.getEvent(eventId)
                .addOnSuccessListener(event -> {
                    currentEvent = event;
                    eventTitle.setText(event.getTitle());
                    eventDescription.setText(event.getDescription());
                    eventLocation.setText("Event Location: " + event.getLocation());
                    eventDates.setText(String.format("Event Dates: %s - %s",
                            event.getEventStartDate(), event.getEventEndDate()));
                    registrationDates.setText(String.format("Registration Period: %s - %s",
                            event.getRegistrationStart(), event.getRegistrationEnd()));
                    capacity.setText(String.format("Capacity: %d/%d",
                            event.getCurrentCapacity(), event.getMaxCapacity()));
                    checkWaitlistStatus();
                    loadOrganizerDetails(event.getOrganizerID());
                    hideLoadingScreen();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event details", e);
                    Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT)
                            .show();
                    hideLoadingScreen();
                });
    }

    private void loadOrganizerDetails(String organizerID) {
        userService.getByUserID(organizerID)
                .addOnSuccessListener(organizer -> {
                    if (organizer != null) {
                        organizerUsername.setText("Organizer Username: " + organizer.getUsername());
                        organizerEmail.setText("Organizer Email: " + organizer.getEmail());
                        if (organizer.getPhoneNumber() != null && !organizer.getPhoneNumber().isEmpty()) {
                            organizerPhone.setText("Organizer Phone: " + organizer.getPhoneNumber());
                        } else {
                            organizerPhone.setText("Organizer Phone: No phone number provided");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load organizer details", e);
                    Toast.makeText(getContext(), "Failed to load organizer details", Toast.LENGTH_SHORT)
                            .show();
                });
    }

    /**
     * Checks the waitlist status for the current user and event,
     * updates the waitlist capacity UI and waitlist button state.
     */
    private void checkWaitlistStatus() {
        waitingListEntryService.getWaitlistSize(currentEvent.getEventID())
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    Long size = task.getResult();
                    currentEvent.setCurrentWaitingListSize(size.intValue());
                    Integer maxWaitListSize = currentEvent.getWaitlistCapacity();
                    String waitlistSizeText = (maxWaitListSize == null)
                            ? String.format("Waitlist capacity: %d/no limit", size)
                            : String.format("Waitlist capacity: %d/%d", size, maxWaitListSize);
                    waitlistCapacity.setText(waitlistSizeText);

                    return waitingListEntryService.getWaitlistEntries(currentEvent.getEventID());
                })
                .addOnSuccessListener(entries -> {
                    boolean alreadyJoined = false;
                    for (WaitingListEntry entry : entries) {
                        if (entry.getUserID().equals(currentEntrantId)) {
                            alreadyJoined = true;
                            break;
                        }
                    }
                    updateWaitlistButton(alreadyJoined);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check waitlist status", e);
                    Toast.makeText(getContext(), "Failed to check waitlist status", Toast.LENGTH_SHORT)
                            .show();
        });

    }

    /**
     * Updates the waitlist button text and click listener based on whether the user
     * has already joined the waitlist.
     *
     * @param alreadyJoined True if the user is already on the waitlist
     */
    private void updateWaitlistButton(boolean alreadyJoined) {
        if (alreadyJoined) {
            waitlistButton.setText("Leave waitlist");
            waitlistButton.setOnClickListener(v -> leaveWaitlist());
        } else {
            waitlistButton.setText("Join Waitlist");
            waitlistButton.setOnClickListener(v -> joinWaitlist());
        }
    }

    /**
     * Adds the current user to the event waitlist.
     */
    private void joinWaitlist() {
        if (currentEntrantId == null) {
            Toast.makeText(getActivity(), "User ID not loaded yet.  Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingScreen();

        userService.getByUserID(currentEntrantId)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Validate username and email
                    if (user.getUsername() == null || user.getUsername().isEmpty() ||
                            user.getEmail() == null || user.getEmail().isEmpty()) {
                        Toast.makeText(getContext(), "Please complete your profile first (username and email)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    currentUser = user;

                    if (currentEvent.getRequiresGeolocation()) {
                        // Request location permission if needed
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                // Permission is not granted, request it
                                ActivityCompat.requestPermissions(requireActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSION_REQUEST_CODE);
                                return;
                            }
                        }
                        // Permission is granted, get location and join
                        getLocationAndJoin();
                    } else {
                        // Geolocation not required, join without location
                        joinWaitlistWithoutLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user profile", e);
                    Toast.makeText(getContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void getLocationAndJoin() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setMaxUpdateDelayMillis(10000)
                    .build();

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            joinWaitlistWithLocation(geoPoint);
                        } else {
                            Log.w(TAG, "getCurrentLocation returned null, trying getLastLocation");
                            // Fallback to getLastLocation
                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(lastLocation -> {
                                        if (lastLocation != null) {
                                            Log.d(TAG, "Last location obtained: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
                                            GeoPoint geoPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                                            joinWaitlistWithLocation(geoPoint);
                                        } else {
                                            Log.w(TAG, "Last location also null, joining without location");
                                            Toast.makeText(requireContext(), "Unable to get current location. Joining without location.",
                                                    Toast.LENGTH_SHORT).show();
                                            joinWaitlistWithoutLocation();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to get last location", e);
                                        Toast.makeText(requireContext(), "Failed to get location. Joining without location.",
                                                Toast.LENGTH_SHORT).show();
                                        joinWaitlistWithoutLocation();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get current location", e);
                        Toast.makeText(requireContext(), "Failed to get location. Joining without location.",
                                Toast.LENGTH_SHORT).show();
                        joinWaitlistWithoutLocation();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location", e);
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void joinWaitlistWithLocation(GeoPoint location) {
        if (currentEntrantId == null) {
            Toast.makeText(getActivity(), "User ID not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingScreen();

        waitingListEntryService.joinWithLocation(currentEntrantId, currentEvent.getEventID(), location)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Successfully joined waitlist with location", Toast.LENGTH_SHORT).show();
                    updateWaitlistButton(true);
                    reloadFragment();
                    hideLoadingScreen();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to join waitlist", e);
                    String errorMessage = "Failed to join waitlist";
                    if (e.getMessage() != null && e.getMessage().contains("Waitlist is full")) {
                        errorMessage = "Waitlist is full";
                    }
                    if (e.getMessage() != null && e.getMessage().contains("Already on waitlist")) {
                        errorMessage = "User is already on waitlist";
                    }
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    reloadFragment();
                    hideLoadingScreen();
                });
    }

    private void joinWaitlistWithoutLocation() {
        if (currentEntrantId == null) {
            Toast.makeText(getActivity(), "User ID not loaded yet.Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoadingScreen();

        waitingListEntryService.join(currentEntrantId, currentEvent.getEventID())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Successfully joined waitlist", Toast.LENGTH_SHORT).show();
                    updateWaitlistButton(true);
                    reloadFragment();
                    hideLoadingScreen();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to join waitlist", e);
                    String errorMessage = "Failed to join waitlist";
                    if (e.getMessage() != null && e.getMessage().contains("Waitlist is full")) {
                        errorMessage = "Waitlist is full";
                    }
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                    reloadFragment();
                    hideLoadingScreen();
                });
    }

    /**
     * Removes the current user from the event waitlist.
     */
    private void leaveWaitlist() {
        showLoadingScreen();

        waitingListEntryService.leave(currentEntrantId, currentEvent.getEventID())
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(getActivity(), "Successfully left waitlist", Toast.LENGTH_SHORT).show();
            updateWaitlistButton(false);
            reloadFragment();
            hideLoadingScreen();
        })
        .addOnFailureListener(e -> {
            Log.e(TAG, "Failed to leave waitlist", e);
            Toast.makeText(getActivity(), "Failed to leave waitlist", Toast.LENGTH_SHORT).show();
            reloadFragment();
            hideLoadingScreen();
        });
    }

    private void showLoadingScreen() {
        if (loadingScreen != null) {
            loadingScreen.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingScreen() {
        if (loadingScreen != null) {
            loadingScreen.setVisibility(View.GONE);
        }
    }

    private void reloadFragment() {
        showLoadingScreen();
        loadEventDetails();
    }
}
