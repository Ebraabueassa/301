package com.example.community.Screens.OrganizerScreens;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.community.DateValidation;
import com.example.community.EventService;
import com.example.community.LotteryService;
import com.example.community.R;
import com.example.community.UserService;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Dialog fragment for confirming and executing a lottery for event registration
 * <p>
 *      Dialog allows event organizers to specify the number of invited entrants that they want to invite
 *      and run a lottery to select participants from waitlisted entrants.
 *      Validates that registration has closed before allowing the lottery to run,
 *      retrieves available slots based on event capacity, and displays a loading state during
 *      the lottery execution
 * </p>
 * <p>
 *      The fragment uses a NumberPicker to allow selection of the sample size and communicates
 *      with EventService, LotteryService, and UserService to execute the lottery
 * </p>
 *
 * @see EventService
 * @see LotteryService
 * @see UserService
 * */
public class LotteryConfirmationDialogFragment extends DialogFragment {
    /** Tag for logging */
    private static final String TAG = "LotteryConfirmationDialogFragment";
    /** Argument key for event ID */
    private static final String ARG_EVENT_ID = "event_id";

    /** The ID of the event to run the lottery for */
    private String eventID;

    /** The number of available slots for the event */

    /** Number of available slots for the lottery */
    private int availableSlots;

    /** Service for retrieving event data */
    private EventService eventService;

    /** Service for running the lottery */
    private LotteryService lotteryService;

    /** Service for retrieving user/organizer info */
    private UserService userService;

    /**
     * UI elements
     */
    private TextView lotteryMessageTextView;

    /** NumberPicker for selecting number of slots to include in the lottery */
    private NumberPicker lotteryNumberPicker;

    /** ProgressBar shown while the lottery is running */
    private ProgressBar lotteryLoadingProgressBar;

    /** Button to confirm and run the lottery */
    private Button lotteryConfirmButton;

    /** Button to cancel the lottery */
    private Button lotteryCancelButton;

    /**
     * Creates a new instance of the fragment with the given event ID
     *
     * @param eventId the ID of the event to run the lottery for
     * @return a new instance of the fragment
     */
    public static LotteryConfirmationDialogFragment newInstance(String eventId) {
        LotteryConfirmationDialogFragment fragment = new LotteryConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment with the event ID and creates instances of the required services
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventService = new EventService();
        lotteryService = new LotteryService();
        userService = new UserService();

        if (getArguments() != null) {
            eventID = getArguments().getString(ARG_EVENT_ID);
        }
    }

    /**
     * Inflates the view for the fragment
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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View lotteryConfirmationDialog = inflater.inflate(R.layout.organizer_lottery_confirmation_dialog, container, false);
        return lotteryConfirmationDialog;
    }

    /**
     * Initializes the views and sets up the number picker and button listeners
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lotteryMessageTextView = view.findViewById(R.id.lotteryMessageTextView);
        lotteryNumberPicker = view.findViewById(R.id.lotteryNumberPicker);
        lotteryLoadingProgressBar = view.findViewById(R.id.lotteryLoadingProgressBar);
        lotteryConfirmButton = view.findViewById(R.id.lotteryConfirmButton);
        lotteryCancelButton = view.findViewById(R.id.lotteryCancelButton);

        // Fetch event to determine available slots
        eventService.getEvent(eventID)
                .addOnSuccessListener(event -> {
                    if (event != null) {
                        availableSlots = event.getMaxCapacity() - event.getCurrentCapacity();
                    } else {
                        availableSlots = 100;
                    }
                    setupNumberPicker(availableSlots);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get event data", e);
                    availableSlots = 100;
                    setupNumberPicker(availableSlots);
                });

        lotteryConfirmButton.setOnClickListener(v -> runLottery());
        lotteryCancelButton.setOnClickListener(v -> dismiss());
    }

    /**
     * Sets up the number picker with the available slots
     *
     * @param availableSlots the number of available slots
     */
    private void setupNumberPicker(int availableSlots) {
        lotteryNumberPicker.setMinValue(1);
        lotteryNumberPicker.setMaxValue(availableSlots);
        lotteryNumberPicker.setValue(1);
        lotteryNumberPicker.setWrapSelectorWheel(false);
    }

    /**
     * Runs the lottery with the selected sample size
     * Validates that registration has ended, retrieves Organizer ID,
     * and calls LotteryService to run the lottery with the selected sample size
     * Displays loading state and appropriate success or failure messages
     */
    private void runLottery() {
        int sampleSize = lotteryNumberPicker.getValue();

        // Get current organizer ID
        String deviceToken = userService.getDeviceToken();
        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(organizerID -> {
                    // Get event to check registration end date
                    eventService.getEvent(eventID)
                            .addOnSuccessListener(event -> {
                                if (event == null) {
                                    Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Check if registration has closed
                                if (!isRegistrationClosed(event.getRegistrationEnd())) {
                                    Toast.makeText(getContext(), "Cannot run lottery: registration has not closed yet", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Show loading bar and start lottery
                                showLoading();
                                Toast.makeText(getContext(), "Running lottery...", Toast.LENGTH_SHORT).show();

                                lotteryService.runLottery(organizerID, eventID, sampleSize)
                                        .addOnSuccessListener(aVoid -> {
                                            hideLoading();
                                            Toast.makeText(getContext(), "Lottery finished successfully!", Toast.LENGTH_SHORT).show();
                                            dismiss();
                                        })
                                        .addOnFailureListener(e -> {
                                            hideLoading();
                                            Log.e(TAG, "Lottery failed", e);
                                            Toast.makeText(getContext(), "Lottery failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to get event", e);
                                Toast.makeText(getContext(), "Failed to get event details", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get organizer ID", e);
                    Toast.makeText(getContext(), "Failed to verify organizer", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Checks if the registration has closed
     *
     * @param registrationEndDate the end date of the registration
     * @return true if the registration has closed, false otherwise
     */
    private boolean isRegistrationClosed(String registrationEndDate) {
        if (!DateValidation.isValidDateFormat(registrationEndDate)) {
            Log.e(TAG, "Invalid date format: " + registrationEndDate);
            return false;
        }
        try {
            LocalDate endDate = LocalDate.parse(registrationEndDate);
            LocalDate currentDate = LocalDate.now();
            return currentDate.isAfter(endDate);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse registration end date", e);
            return false;
        }
    }

    /**
     * Shows the loading state
     */
    private void showLoading() {
        lotteryMessageTextView.setText("Running lottery...");
        lotteryConfirmButton.setEnabled(false);
        lotteryCancelButton.setEnabled(false);
        lotteryLoadingProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the loading state
     */
    private void hideLoading() {
        lotteryLoadingProgressBar.setVisibility(View.GONE);
        lotteryConfirmButton.setEnabled(true);
        lotteryCancelButton.setEnabled(true);
    }

    /**
     * Prevents the dialog from being dismissed when touched outside
     */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
        }
    }
}
