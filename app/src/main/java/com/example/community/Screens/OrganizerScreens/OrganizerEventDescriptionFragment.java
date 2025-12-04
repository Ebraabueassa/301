package com.example.community.Screens.OrganizerScreens;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.Event;
import com.example.community.EventService;
import com.example.community.R;
import com.example.community.UserService;
import com.example.community.WaitingListEntryService;
import com.squareup.picasso.Picasso;

/**
 * Fragment for displaying event details and managing event
 *
 * <p>
 *     Fragments shows event details including:
 *     <ul>
 *         <li>Event title</li>
 *         <li>Event description</li>
 *         <li>Event location</li>
 *         <li>Event dates</li>
 *         <li>Registration dates</li>
 *         <li>Capacity</li>
 *         <li>Organizer details</li>
 *         <li>Waitlist count</li>
 *         <li>Attendee count</li>
 *         <li>Invited count</li>
 *     </ul>
 * </p>
 * <p>
 *     Receives event ID through navigation arguments and loads all related data
 * </p>
 *
 * @see UserService
 * @see EventService
 * @see WaitingListEntryService
 *
 */
public class OrganizerEventDescriptionFragment extends Fragment {

    /** Tag for logging */
    public static final String TAG = "OrganizerEventDescriptionFragment";

    /** Navigation argument for event ID */
    private static final String ARG_EVENT_ID = "event_id";

    /** Currently displayed event */
    private Event currentEvent;

    /** Service for managing waiting list entries */
    private WaitingListEntryService waitingListEntryService;

    /** Service for managing user operations */
    private UserService userService;

    /** Service for managing event operations */
    private EventService eventService;

    /**
     * UI elements
     */
    private ImageView posterImageView;
    private TextView eventTitle, eventDescription, eventLocation, eventDates
            , registrationDates, capacity, organizerUsername, organizerEmail, organizerPhone
            , waitlistCount, attendeeCount, invitedCount;
    private Button editButton, uploadPosterButton, viewAttendeesButton,
            viewWaitlistButton, viewInvitedButton, viewDeclinedButton,
            viewCancelledButton, runLotteryButton, exportAttendeesButton, backButton;

    /**
     * Inflate the fragment's layout view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View organizerEventDescriptionFragment = inflater.inflate(R.layout.organizer_event_description_page, container, false);
        return organizerEventDescriptionFragment;
    }

    /**
     * Initialize the fragment's UI elements and set up event listeners
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        waitingListEntryService = new WaitingListEntryService();
        userService = new UserService();
        eventService = new EventService();

        posterImageView = view.findViewById(R.id.posterImageView);
        eventTitle = view.findViewById(R.id.eventTitle);
        eventDescription = view.findViewById(R.id.eventDescription);
        eventLocation = view.findViewById(R.id.eventLocation);
        eventDates = view.findViewById(R.id.eventDates);
        registrationDates = view.findViewById(R.id.registrationDates);
        capacity = view.findViewById(R.id.capacity);
        organizerUsername = view.findViewById(R.id.eventOrganizerName);
        organizerEmail = view.findViewById(R.id.eventOrganizerEmail);
        organizerPhone = view.findViewById(R.id.eventOrganizerPhone);
        waitlistCount = view.findViewById(R.id.organizerWaitlistCount);
        attendeeCount = view.findViewById(R.id.organizerAttendeeCount);
        invitedCount = view.findViewById(R.id.organizerInvitedCount);

        editButton = view.findViewById(R.id.organizerEditEventButton);
        uploadPosterButton = view.findViewById(R.id.uploadPosterButton);
        viewAttendeesButton = view.findViewById(R.id.viewAttendeesButton);
        viewWaitlistButton = view.findViewById(R.id.viewWaitlistButton);
        viewInvitedButton = view.findViewById(R.id.viewInvitedButton);
        viewCancelledButton = view.findViewById(R.id.viewCancelledButton);
        viewDeclinedButton = view.findViewById(R.id.viewDeclinedButton);
        runLotteryButton = view.findViewById(R.id.runLotteryButton);
        exportAttendeesButton = view.findViewById(R.id.exportAttendeesButton);
        backButton = view.findViewById(R.id.organizerEventDescriptionBackButton);


        loadEventDetails();
        setUpClickListeners();
    }

    /**
     * Called when the fragment is resumed. Reloads event if event was loaded
     * previously.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Reload event details when returning from upload fragment
        if (currentEvent != null) {
            loadEventDetails();
        }
    }

    /**
     * Sets up click listeners for all buttons in the fragment.
     */
    private void setUpClickListeners() {
        backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        editButton.setOnClickListener(v -> editEvent());
        uploadPosterButton.setOnClickListener(v -> uploadPoster());
        viewAttendeesButton.setOnClickListener(v -> viewAttendeesList());
        viewWaitlistButton.setOnClickListener(v -> viewWaitlist());
        viewInvitedButton.setOnClickListener(v -> viewInvitedList());
        viewCancelledButton.setOnClickListener(v -> viewCancelledList());
        viewDeclinedButton.setOnClickListener(v -> viewDeclinedList());
        runLotteryButton.setOnClickListener(v -> showLotteryConfirmationDialog());
        exportAttendeesButton.setOnClickListener(v -> exportAttendeesCSV());
    }

    /**
     * Loads event details from the database and displays them in the UI.
     * Fills in all UI elements with the event's details.
     */
    private void loadEventDetails() {
        String eventId = getArguments().getString(ARG_EVENT_ID);
        if (eventId == null) {
            Toast.makeText(getContext(), "Invalid event", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

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
                    capacity.setText(String.format("Amount Attendees: %d/%d",
                            event.getCurrentCapacity(), event.getMaxCapacity()));

                    loadPosterImage();
                    loadWaitlistCount();
                    loadOrganizerDetails(event.getOrganizerID());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event details", e);
                    Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT)
                            .show();
                });
    }

    /**
     * Loads and displays the event's poster image using Picasso.
     * If the event has no poster image, the image view is hidden.
     */
    private void loadPosterImage() {
        if (currentEvent == null) {
            posterImageView.setVisibility(View.GONE);
            return;
        }

        String posterURL = currentEvent.getPosterImageURL();
        if (posterURL != null && !posterURL.isEmpty()) {
            Picasso.get()
                    .load(posterURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.community_logo_full)
                    .placeholder(R.drawable.community_logo_full)
                    .into(posterImageView);
            posterImageView.setVisibility(View.VISIBLE);
        } else {
            posterImageView.setVisibility(View.GONE);
        }
    }

    /**
     * Loads and displays the event's organizer details.
     * If the organizer is not found, a toast message is displayed.
     * Displays a default message if the organizer's phone number is not available.
     *
     * @param organizerID The ID of the organizer to load details for.
     */
    private void loadOrganizerDetails(String organizerID) {
        userService.getByUserID(organizerID)
                .addOnSuccessListener(organizer -> {
                    if (organizer != null) {
                        organizerUsername.setText("Organizer Username: " + organizer.getUsername());
                        organizerEmail.setText("Organizer Email: " +organizer.getEmail());
                        if (organizer.getPhoneNumber() != null  && !organizer.getPhoneNumber().isEmpty()) {
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
     * Loads and displays the event's waitlist count.
     * If the waitlist size is not found, a toast message is displayed.
     * If no maximum waitlist size is set, the waitlist count is displayed as "no limit".
     *
     */
    private void loadWaitlistCount() {
        waitingListEntryService.getWaitlistSize(currentEvent.getEventID())
                .addOnSuccessListener(size -> {
                    currentEvent.setCurrentWaitingListSize(size.intValue());
                    Integer maxWaitListSize = currentEvent.getWaitlistCapacity();
                    String waitlistSizeText = (maxWaitListSize == null)
                            ? String.format("Waitlist: %d/no limit", size)
                            : String.format("Waitlist: %d/%d", size, maxWaitListSize);
                    waitlistCount.setText(waitlistSizeText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load waitlist count", e);
                    Toast.makeText(getContext(), "Failed to load waitlist count", Toast.LENGTH_SHORT)
                            .show();
                });
    }

    /**
     * Navigates to event editing screen, passing all current event details as arguments.
     * Sets edit mode flag to true.
     * Displays error if event is not loaded
     */
    private void editEvent() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("event_id", currentEvent.getEventID());
        args.putString("event_name", currentEvent.getTitle());
        args.putString("event_description", currentEvent.getDescription());
        args.putString("event_location", currentEvent.getLocation());
        args.putString("event_start_date", currentEvent.getEventStartDate());
        args.putString("event_end_date", currentEvent.getEventEndDate());
        args.putString("reg_start", currentEvent.getRegistrationStart());
        args.putString("reg_end", currentEvent.getRegistrationEnd());
        args.putInt("max_participants", currentEvent.getMaxCapacity());
        if (currentEvent.getWaitlistCapacity() != null) {
            args.putInt("waiting_list_size", currentEvent.getWaitlistCapacity());
        }
        args.putBoolean("requires_geolocation", currentEvent.getRequiresGeolocation());

        args.putBoolean("is_edit_mode", true);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_OrganizerEventDescriptionFragment_to_CreateEventFragment, args);
    }

    /**
     * Navigates to poster upload screen, passing event ID as argument.
     * Displays error if event is not loaded
     */
    private void uploadPoster() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("event_id", currentEvent.getEventID());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_OrganizerEventDescriptionFragment_to_OrganizerPosterUploadFragment, args);
    }

    /**
     * Displays dialog fragment showing the list of waitlisted entrants for this event.
     * Displays error if event is not loaded.
     */
    private void viewWaitlist() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        OrganizerEventUserListFragment fragment = OrganizerEventUserListFragment.newInstance(currentEvent.getEventID(), "waitlist");
        fragment.show(getChildFragmentManager(), "waitlist_list");
    }

    /**
     * Displays dialog fragment showing the list of attendees for this event.
     * Displays error if event is not loaded.
     */
    private void viewAttendeesList() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        OrganizerEventUserListFragment fragment = OrganizerEventUserListFragment.newInstance(currentEvent.getEventID(), "attendees");
        fragment.show(getChildFragmentManager(), "attendees_list");
    }

    /**
     * Displays dialog fragment showing the list of invited entrants for this event.
     * Displays error if event is not loaded.
     */
    private void viewInvitedList() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        OrganizerEventUserListFragment fragment = OrganizerEventUserListFragment.newInstance(currentEvent.getEventID(), "invited");
        fragment.show(getChildFragmentManager(), "invited_list");
    }

    /**
     * Displays dialog fragment showing the list of cancelled entrants for this event.
     * Displays error if event is not loaded.
     */
    private void viewCancelledList() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        OrganizerEventUserListFragment fragment = OrganizerEventUserListFragment.newInstance(currentEvent.getEventID(), "cancelled");
        fragment.show(getChildFragmentManager(), "cancelled_list");
    }

    /**
     * Displays dialog fragment showing the list of entrants who declined the invitation for this event.
     * Displays error if event is not loaded.
     */
    private void viewDeclinedList() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        OrganizerEventUserListFragment fragment = OrganizerEventUserListFragment.newInstance(currentEvent.getEventID(), "declined");
        fragment.show(getChildFragmentManager(), "declined_list");
    }

    /**
     * Displays dialog fragment showing the list of entrants who declined the invitation for this event.
     * Displays error if event is not loaded.
     */
    private void showLotteryConfirmationDialog() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }
        LotteryConfirmationDialogFragment fragment = LotteryConfirmationDialogFragment.newInstance(currentEvent.getEventID());
        fragment.show(getChildFragmentManager(), "lottery_confirmation");
    }

    /**
     * Exports the list of attendees for this event to a CSV file.
     * Displays error if event is not loaded.
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Get organizer ID</li>
     *         <li>Calls event service to generate CSV content</li>
     *         <li>Creates CSV file</li>
     *         <li>Writes content to the file in the Downloads folder</li>
     *         <li>Displays success message or error</li>
     *     </ul>
     * </p>
     * Displays error if event is not loaded.
     */
    private void exportAttendeesCSV() {
        if (currentEvent == null) {
            Toast.makeText(getContext(), "Event not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading toast
        Toast.makeText(getContext(), "Generating CSV...", Toast.LENGTH_SHORT).show();

        // Get organizer ID first
        String deviceToken = userService.getDeviceToken();
        userService.getByDeviceToken(deviceToken)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Toast.makeText(getContext(), "Organizer not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String organizerID = user.getUserID();

                    eventService.exportAttendeesCSV(organizerID, currentEvent.getEventID())
                            .addOnSuccessListener(csvContent -> {
                                try {
                                    // Create filename with event title and timestamp
                                    String filename = currentEvent.getTitle().replaceAll("[^a-zA-Z0-9._-]", "_")
                                            + "_attendees_" + System.currentTimeMillis() + ".csv";

                                    // Get Downloads directory
                                    java.io.File downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                                            android.os.Environment.DIRECTORY_DOWNLOADS);


                                    java.io.File csvFile = new java.io.File(downloadsDir, filename);
                                    java.io.FileWriter fileWriter = new java.io.FileWriter(csvFile);
                                    fileWriter.write(csvContent);
                                    fileWriter.close();

                                    Toast.makeText(getContext(), "CSV saved to Downloads: " + filename, Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "CSV file saved at: " + csvFile.getAbsolutePath());

                                } catch (java.io.IOException e) {
                                    Log.e(TAG, "Failed to save CSV file", e);
                                    Toast.makeText(getContext(), "Failed to save CSV file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to export CSV", e);
                                Toast.makeText(getContext(), "Failed to export attendees", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get organizer ID", e);
                    Toast.makeText(getContext(), "Failed to load organizer data", Toast.LENGTH_SHORT).show();
                });
    }

}
