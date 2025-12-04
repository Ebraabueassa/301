package com.example.community.Screens.OrganizerScreens;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.DateValidation;
import com.example.community.EventService;
import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;

import java.time.LocalDate;

/**
 * Fragment for creating and editing events by organizers
 * <p>
 *     This fragment gives organizer a form to create new events or edit existing events.
 *     It contains fields for event name, description, location, start and end dates,
 *     registration start and end dates, maximum participants, waiting list size,
 *     and geolocation requirements. The fragment also manages date picker dialogs for selecting
 *     event and registration dates
 * </p>
 * <p>
 *     When creating an event, the fragment generates a QR code.
 *     When editing, it loads existing event data and updates it accordingly
 * </p>
 *
 * @see EventService
 * @see UserService
 *
 */
public class OrganizerCreateEventFragment extends Fragment {

    /** Tag used for logging */
    private final String TAG = "CreateEventFragment";


    /** Input field for the event name */
    private EditText eventNameInput;

    /** Input field for the event description */
    private EditText eventDescriptionInput;

    /** Input field for the event location */
    private EditText eventLocationInput;

    /** Input field for the maximum number of participants */
    private EditText eventMaxParticipantsInput;

    /** Input field for the waiting list size (optional) */
    private EditText waitingListSizeInput;

    /** Input field for the event start date */
    private EditText eventStartDateInput;

    /** Input field for the event end date */
    private EditText eventEndDateInput;

    /** Input field for registration start date */
    private EditText inputRegStart;

    /** Input field for registration end date */
    private EditText inputRegEnd;

    /** Checkbox for the geolocation tracking */
    private CheckBox geolocationRequiredCheckbox;

    /** Button to cancel event creation/editing */
    private Button cancelButton;

    /** Button to submit the new or edited event */
    private Button submitButton;

    /** Service for event-related database operations */
    private EventService eventService;

    /** Service for getting user data from Firebase Firestore */
    private UserService userService;

    /** Currently logged-in organizer */
    private User currentOrganizer;

    /** ID of the event being edited (null if creating a new event) */
    private String editingEventId;

    /** Flag indicating if the fragment is in editing mode */
    private boolean isEditing;

    /**
     * Inflates the fragment's UI
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View createEventFragment = inflater.inflate(R.layout.organizer_create_event_page, container, false);
        return createEventFragment;
    }

    /**
     * Initializes the fragment's UI and services
     * Loads organizer data, sets up date pickers, and configures button click listeners
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

        eventNameInput = view.findViewById(R.id.inputEventName);
        eventDescriptionInput = view.findViewById(R.id.inputDescription);
        eventLocationInput = view.findViewById(R.id.inputEventLocation);
        eventMaxParticipantsInput = view.findViewById(R.id.inputMaxParticipants);
        waitingListSizeInput = view.findViewById(R.id.inputWaitingListSize);
        eventStartDateInput = view.findViewById(R.id.inputEventStart);
        eventEndDateInput = view.findViewById(R.id.inputEventEnd);
        inputRegStart = view.findViewById(R.id.inputRegistrationStart);
        inputRegEnd = view.findViewById(R.id.inputRegistrationEnd);
        geolocationRequiredCheckbox = view.findViewById(R.id.checkboxGeolocationRequired);

        cancelButton = view.findViewById(R.id.buttonCancel);
        submitButton = view.findViewById(R.id.buttonSubmit);

        eventStartDateInput.setFocusable(false);
        eventEndDateInput.setFocusable(false);
        inputRegStart.setFocusable(false);
        inputRegEnd.setFocusable(false);

        // Check if the fragment is in edit mode
        Bundle args = getArguments();
        if (args != null) {
            isEditing = args.getBoolean("is_edit_mode", false);
            if (isEditing) {
                editingEventId = args.getString("event_id");
                loadEventDataForEditing(args);
                submitButton.setText("Update Event");
            }
        }

        loadOrganizerData();
        setupDatePickers();

        cancelButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        submitButton.setOnClickListener(v -> {
            // Create or update event based on edit mode
            if (isEditing) {
                updateEvent();
            } else {
                createEvent();
            }
        });

    }

    /**
     * Loads the current organizer's data from the Firestore database
     * Displays a toast message if the user is not found or if the user's profile is incomplete
     */
    private void loadOrganizerData() {
        String deviceToken = userService.getDeviceToken();

        userService.getByDeviceToken(deviceToken)
                .addOnSuccessListener(user -> {
                    if (user == null) {
                        Log.e(TAG, "User not found: " + deviceToken);
                        throw new IllegalArgumentException("User not found: " + deviceToken);
                    }
                    if (user.getUsername() == null || user.getUsername().isEmpty() ||
                            user.getEmail() == null || user.getEmail().isEmpty()) {
                        Toast.makeText(getContext(), "Please complete your profile first (username and email)", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).navigateUp();
                        return;
                    }
                    currentOrganizer = user;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load Organizer data", e);
                    Toast.makeText(getContext(), "Failed to get user data", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                });
    }

    /**
     * Loads the event data for editing from the arguments bundle for editing mode.
     * Fills all the input fields with the event's current values.
     *
     * @param args The arguments bundle containing the event data
     */
    private void loadEventDataForEditing(Bundle args) {
        String eventName = args.getString("event_name", "");
        String eventDescription = args.getString("event_description", "");
        String eventLocation = args.getString("event_location", "");
        String eventStartDate = args.getString("event_start_date", "");
        String eventEndDate = args.getString("event_end_date", "");
        String regStart = args.getString("reg_start", "");
        String regEnd = args.getString("reg_end", "");
        int maxParticipants = args.getInt("max_participants", 0);
        int waitingListSize = args.getInt("waiting_list_size", 0);
        boolean geolocationRequired = args.getBoolean("requires_geolocation", false);

        eventNameInput.setText(eventName);
        eventDescriptionInput.setText(eventDescription);
        eventLocationInput.setText(eventLocation);
        eventStartDateInput.setText(eventStartDate);
        eventEndDateInput.setText(eventEndDate);
        inputRegStart.setText(regStart);
        inputRegEnd.setText(regEnd);
        eventMaxParticipantsInput.setText(String.valueOf(maxParticipants));
        if (waitingListSize > 0) {
            waitingListSizeInput.setText(String.valueOf(waitingListSize));
        }
        geolocationRequiredCheckbox.setChecked(geolocationRequired);
    }

    /**
     * Sets up the date pickers for the event start and end dates and registration start and end dates.
     * */
    private void setupDatePickers() {
        eventStartDateInput.setOnClickListener(v -> showDatePicker(eventStartDateInput));
        eventEndDateInput.setOnClickListener(v -> showDatePicker(eventEndDateInput));
        inputRegStart.setOnClickListener(v -> showDatePicker(inputRegStart));
        inputRegEnd.setOnClickListener(v -> showDatePicker(inputRegEnd));
    }

    /**
     * Creates a new event with the provided details. Validates all inputs, checks date ranges,
     * and generates a QR code for the event after successful creation.
     * <p>
     *      Validation includes:
     *          <ul>
     *              <li>All required fields are filled</li>
     *              <li>Max participants is a positive integer</li>
     *              <li>Waiting list size (if provided) is a positive integer</li>
     *              <li>Registration dates are valid</li>
     *              <li>Event dates are valid</li>
     *              <li>Registration ends before the event starts</li>
     *         </ul>
     * </p>
     */
    private void createEvent() {
        if (currentOrganizer == null) {
            Log.d(TAG, "createEvent: Organizer not found");
            Toast.makeText(getContext(), "Organizer not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventName = eventNameInput.getText().toString();
        String eventDescription = eventDescriptionInput.getText().toString();
        String eventLocation = eventLocationInput.getText().toString();
        String eventStartDate = eventStartDateInput.getText().toString();
        String eventEndDate = eventEndDateInput.getText().toString();
        String registrationStart = inputRegStart.getText().toString();
        String registrationEnd = inputRegEnd.getText().toString();
        boolean geolocationRequired = geolocationRequiredCheckbox.isChecked();

        if (eventName.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() ||
                eventStartDate.toString().isEmpty() || eventEndDate.isEmpty() ||
                registrationStart.isEmpty() || registrationEnd.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int eventMaxParticipants;
        Integer waitingListSize = null;

        // Validate max participants
        try {
            eventMaxParticipants = Integer.parseInt(eventMaxParticipantsInput.getText().toString().trim());
            if (eventMaxParticipants < 1) {
                Toast.makeText(getContext(), "Number of participants must be positive and greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input for number of participants", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate waiting list size
        String waitingListSizeStr = waitingListSizeInput.getText().toString().trim();
        if (!waitingListSizeStr.isEmpty()) {
            try {
                waitingListSize = Integer.parseInt(waitingListSizeStr);
                if (waitingListSize < 1) {
                    Toast.makeText(getContext(), "Waiting list size must be positive and greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid input for waiting list size", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validate registration period
        if (!DateValidation.dateRangeValid(registrationStart, registrationEnd)) {
            Toast.makeText(getContext(), "Invalid registration period", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate event period
        if (!DateValidation.dateRangeValid(eventStartDate, eventEndDate)) {
            Toast.makeText(getContext(), "Invalid event period", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate registration end date
        if (!DateValidation.dateRangeValid(registrationEnd, eventStartDate)) {
            Toast.makeText(getContext(), "Registration must end before the event starts", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create event
        eventService.createEvent(currentOrganizer.getUserID(), eventName, eventDescription, eventLocation,
                        eventMaxParticipants, eventStartDate, eventEndDate, waitingListSize, registrationStart, registrationEnd)
                .addOnSuccessListener(eventId -> {
                    // Set geolocation requirement
                    eventService.getEvent(eventId)
                            .addOnSuccessListener(event -> {
                                event.setRequiresGeolocation(geolocationRequired);
                                eventService.updateEvent(currentOrganizer.getUserID(), event)
                                        .addOnSuccessListener(aVoid -> {
                                            eventService.refreshEventQR(currentOrganizer.getUserID(), eventId)
                                                    .addOnSuccessListener(imageUrl -> {
                                                        Toast.makeText(getContext(), "Event created successfully with QR code", Toast.LENGTH_SHORT)
                                                                .show();
                                                        NavHostFragment.findNavController(this).navigateUp();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to generate QR code", e);
                                                        Toast.makeText(getContext(), "Event created but QR code generation failed", Toast.LENGTH_SHORT)
                                                                .show();
                                                    });
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to set geolocation requirement", e);
                                            Toast.makeText(getContext(), "Event created but failed to set geolocation", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to fetch event for geolocation update", e);
                                Toast.makeText(getContext(), "Event created but failed to update settings", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create event", e);
                    Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT)
                            .show();
                });
    }

    /**
     * Updates an existing event with new details. Validates all inputs similar to createEvent()
     * and fetches the event before applying edits.
     *
     * @see #createEvent()
     */
    private void updateEvent() {
        if (currentOrganizer == null || editingEventId == null) {
            Toast.makeText(getContext(), "Error: Cannot update event", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventName = eventNameInput.getText().toString();
        String eventDescription = eventDescriptionInput.getText().toString();
        String eventLocation = eventLocationInput.getText().toString();
        String eventStartDate = eventStartDateInput.getText().toString();
        String eventEndDate = eventEndDateInput.getText().toString();
        String registrationStart = inputRegStart.getText().toString();
        String registrationEnd = inputRegEnd.getText().toString();
        boolean geolocationRequired = geolocationRequiredCheckbox.isChecked();

        if (eventName.isEmpty() || eventDescription.isEmpty() ||
                eventStartDate.isEmpty() || eventEndDate.isEmpty() ||
                registrationStart.isEmpty() || registrationEnd.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int eventMaxParticipants;

        try {
            eventMaxParticipants = Integer.parseInt(eventMaxParticipantsInput.getText().toString().trim());
            if (eventMaxParticipants <= 0) {
                Toast.makeText(getContext(), "Number of participants must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number of participants", Toast.LENGTH_SHORT).show();
            return;
        }

        String waitingListSizeStr = waitingListSizeInput.getText().toString().trim();
        Integer parsedWaitingListSize = null;
        if (!waitingListSizeStr.isEmpty()) {
            try {
                parsedWaitingListSize = Integer.parseInt(waitingListSizeStr);
                if (parsedWaitingListSize <= 0) {
                    Toast.makeText(getContext(), "Waiting list size must be positive", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid waiting list size", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final Integer waitingListSize = parsedWaitingListSize;

        if (!DateValidation.dateRangeValid(registrationStart, registrationEnd)) {
            Toast.makeText(getContext(), "Invalid registration period", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DateValidation.dateRangeValid(eventStartDate, eventEndDate)) {
            Toast.makeText(getContext(), "Invalid event period", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DateValidation.dateRangeValid(registrationEnd, eventStartDate)) {
            Toast.makeText(getContext(), "Registration must end before the event starts", Toast.LENGTH_SHORT).show();
            return;
        }


        // Create updated event object
        eventService.getEvent(editingEventId)
                .addOnSuccessListener(event -> {
                    event.setTitle(eventName);
                    event.setDescription(eventDescription);
                    event.setLocation(eventLocation);
                    event.setEventStartDate(eventStartDate);
                    event.setEventEndDate(eventEndDate);
                    event.setRegistrationStart(registrationStart);
                    event.setRegistrationEnd(registrationEnd);
                    event.setMaxCapacity(eventMaxParticipants);
                    event.setRequiresGeolocation(geolocationRequired);
                    if (waitingListSize != null) {
                        event.setWaitlistCapacity(waitingListSize);
                    }

                    eventService.updateEvent(currentOrganizer.getUserID(), event)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Event updated successfully", Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(this).navigateUp();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update event", e);
                                Toast.makeText(getContext(), "Failed to update event", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch event for update", e);
                    Toast.makeText(getContext(), "Failed to fetch event", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Helper method to show a date picker dialog for a given edit text field.
     *
     * @param editText The edit text field to show the date picker for.
     */
    private void showDatePicker(final EditText editText) {
        LocalDate minDate = LocalDate.now();
        LocalDate initialDate = LocalDate.now();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    LocalDate selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    editText.setText(selectedDate.format(DateValidation.DATE_FORMAT));

                },
                initialDate.getYear(),
                initialDate.getMonthValue() - 1,
                initialDate.getDayOfMonth()
        );
        datePickerDialog.getDatePicker().setMinDate(minDate.toEpochDay());
        datePickerDialog.show();
    }
}