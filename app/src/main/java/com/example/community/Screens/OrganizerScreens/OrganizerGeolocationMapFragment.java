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

import com.example.community.EventService;
import com.example.community.R;
import com.example.community.WaitingListEntry;
import com.example.community.WaitingListEntryService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;

/**
 * Fragment for displaying a Google Map with entrant join locations for a specific event
 * <p>
 *     Displays markers on a Google Map representing the geographic location of where entrants
 *     joined the event's waitlist. Each marker is labeled with the location's latitude and longitude.
 * </p>
 * <p>
 *     Receives the event ID through navigation arguments and uses WaitingListEntryService to
 *     retrieve the join locations of all entrants who joined the event's waitlist. The locations
 *     are then displayed on the map.
 * </p>
 *
 * @see WaitingListEntryService
 * @see GoogleMap
 * @see OnMapReadyCallback
 */
public class OrganizerGeolocationMapFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Tag for logging
     */
    private static final String TAG = "GeolocationMapFragment";
    /**
     * Navigation argument key for event ID
     */
    private static final String ARG_EVENT_ID = "event_id";

    /**
     * Google Map instance for displaying entrant location markers.
     */
    private GoogleMap googleMap;
    /**
     * Back button to navigate back to previous fragment
     */
    private Button backButton;

    /**
     * Service for retrieving wait list entry geolocation
     */
    private WaitingListEntryService waitingListEntryService;
    /**
     * ID of the event to display geolocation for
     */
    private String eventID;

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.organizer_geolocation_map_page, container, false);
    }

    /**
     * Initializes the fragment's UI elements and services.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        waitingListEntryService = new WaitingListEntryService();
        backButton = view.findViewById(R.id.mapBackButton);

        if (getArguments() != null) {
            eventID = getArguments().getString(ARG_EVENT_ID);
            Log.d(TAG, "Event ID: " + eventID);
        }

        if (eventID == null) {
            Log.e(TAG, "Event ID not found");
            Toast.makeText(getContext(), "Event ID not found", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            Log.d(TAG, "Map fragment found");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment not found");
        }
    }

    /**
     * Called when the Google Map is ready for use.
     * Initiates loading of entrant location data
     *
     * @param googleMap the initialized Google Map instance
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        loadEntrantLocations();
    }

    /**
     * Loads waitlist entries with geolocation data for the event and displays them as markers on the map
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Validates that the Google Map is initialized</li>
     *         <li>Retrieves all waitlist entries with geolocation data</li>
     *         <li>Creates a marker for each entrant location</li>
     *         <li>Positions the camera at the first location</li>
     *         <li>Display a toast with the total number of markers added</li>
     *
     *     </ul>
     * </p>
     * If no entrant locations are found, displays a toast message
     * If process fails, displays a failure message
     */
    private void loadEntrantLocations() {
        if (googleMap == null) {
            return;
        }

        waitingListEntryService.getWaitlistEntriesWithLocation(eventID)
                .addOnSuccessListener(entries -> {
                    if (entries == null || entries.isEmpty()) {
                        Log.d(TAG, "No entrants with location data found");
                        Toast.makeText(getContext(), "No entrants with location data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    LatLng firstLocation = null;
                    int markerCount = 0;

                    for (WaitingListEntry entry : entries) {
                        GeoPoint geoPoint = entry.getJoinLocation();
                        if (geoPoint != null) {
                            LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                            // Add marker to map
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(location)
                                    .title("Entrant Location")
                                    .snippet("Joined from: " + geoPoint.getLatitude() + ", " + geoPoint.getLongitude());

                            googleMap.addMarker(markerOptions);

                            if (firstLocation == null) {
                                firstLocation = location;
                            }
                            markerCount++;
                        }
                    }

                    // Move camera to first location and set zoom level
                    if (firstLocation != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10));
                    }

                    Toast.makeText(getContext(), "Loaded " + markerCount + " entrant locations", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load entrant locations", e);
                    Toast.makeText(getContext(), "Failed to load entrant locations", Toast.LENGTH_SHORT).show();
                });
    }
}