package com.example.community.Screens.OrganizerScreens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.NotificationService;
import com.example.community.R;
import com.example.community.User;
import com.example.community.UserService;

/**
 * Fragment for creating and sending notifications to event entrants by organizers.
 * <p>
 *     Allows organizes to make and send notifications to specific groups of entrants
 *     for a specific event. The notification includes a title and message, which are
 *     validated before sending
 * </p>
 * <p>
 *     The fragment receives the event ID and entrant type as arguments from the previous
 *     fragment.
 * </p>
 *
 *
 * @see NotificationService
 * @see UserService
 */
public class OrganizerCreateNotificationFragment extends Fragment {

    /**
     * Tag for logging
     */
    private static final String TAG = "CreateNotificationsFragment";

    /**
     * ID of the event for which the notification will be sent
     */
    private String eventID;
    /**
     * The type of entrants to receive the notification.
     */
    private String entrantType;
    /**
     * The current organizer user
     */
    private User currentOrganizer;

    /**
     * UI elements
     */
    private TextView labelNotifyUsers;
    private EditText inputNotificationTitle, inputNotificationMessage;
    private Button buttonCancel, buttonSend;

    /**
     * Service for notification operations
     */
    private NotificationService notificationService;
    /**
     * Service for user operations
     */
    private UserService userService;


    /**
     * Called to have the fragment instantiate its user interface view.
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
        View view = inflater.inflate(R.layout.organizer_create_notification_page, container, false);
        return view;
    }

    /**
     * Initializes the fragment's UI elements and services. Sets up the UI elements
     * and services, and loads the organizer's data.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationService = new NotificationService();
        userService = new UserService();

        labelNotifyUsers = view.findViewById(R.id.labelNotifyUsers);
        inputNotificationTitle = view.findViewById(R.id.inputNotificationTitle);
        inputNotificationMessage = view.findViewById(R.id.inputNotifyMessage);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSend = view.findViewById(R.id.buttonSend);

        if (getArguments() != null) {
            eventID = getArguments().getString("event_id");
            entrantType = getArguments().getString("entrant_type");
        }

        loadOrganizerData();
        updateLabelBasedOnEntrantType();

        buttonCancel.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        buttonSend.setOnClickListener(v -> {
           sendNotifications();
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
     * Updates the label based on the entrant type
     *
     */
    private void updateLabelBasedOnEntrantType() {
        if (entrantType != null) {
            switch (entrantType) {
                case "WAITING":
                    labelNotifyUsers.setText("Notify Waiting List Entrants");
                    break;
                case "INVITED":
                    labelNotifyUsers.setText("Notify Invited Entrants");
                    break;
                case "CANCELLED":
                    labelNotifyUsers.setText("Notify Cancelled Entrants");
                    break;
                default:
                    labelNotifyUsers.setText("Notify Users");
            }
        }
    }

    /**
     * Validates the notification data and sends the notification
     * <p>
     *     Validates:
     *     <ul>
     *         <li>if the title is empty</li>
     *         <li>if the message is empty</li>
     *         <li>if the event ID is empty</li>
     *         <li>if the entrant type is empty</li>
     *     </ul>
     * </p>
     * If validation passes, calls sendNotificationByType
     *
     */
    private void sendNotifications() {
        String title = inputNotificationTitle.getText().toString().trim();
        String message = inputNotificationMessage.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a notification title", Toast.LENGTH_SHORT);
            return;
        }
        if (message.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a notification message", Toast.LENGTH_SHORT);
            return;
        }
        if (eventID == null || eventID.isEmpty()) {
            Toast.makeText(getContext(), "EventID is missing", Toast.LENGTH_SHORT);
            return;
        }
        if (entrantType == null || entrantType.isEmpty()) {
            Toast.makeText(getContext(), "Entrant type is missing", Toast.LENGTH_SHORT);
            return;
        }

        sendNotificationByType(title, message);
    }

    /**
     * Sends the notification to the appropriate entrant group based on the entrant type.
     * <p>
     *      Uses one of three broadcast methods based on the entrant type:
     *      <ul>
     *          <li>{@code "WAITING"} \-\> calls {@code notificationService.broadcastToWaitlist()}</li>
     *          <li>{@code "INVITED"} \-\> calls {@code notificationService.broadcastToInvited()}</li>
     *          <li>{@code "CANCELLED"} \-\> calls {@code notificationService.broadcastToCancelled()}</li>
     *     </ul>
     * </p>
     * On success, displays a confirmation toast and navigates back. On failure, displays an error
     * message with exception details.
     *
     * @param title the notification title
     * @param message the notification message body
     */
    private void sendNotificationByType(String title, String message) {
        Toast.makeText(getContext(), "Sending notification...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "=== sendNotificationByType START ===");
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "EventID: " + eventID);
        Log.d(TAG, "EntrantType: " + entrantType);
        Log.d(TAG, "Organizer ID: " + (currentOrganizer != null ? currentOrganizer.getUserID() : "NULL"));

        // Send notification based on entrant type
        switch (entrantType) {
            case "WAITING":
                // Send to waiting list entrants
                notificationService.broadcastToWaitlist(currentOrganizer.getUserID(), eventID, title, message)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Broadcast successfull");
                            Toast.makeText(getContext(), "Notification sent to waiting list!", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(OrganizerCreateNotificationFragment.this).navigateUp();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "✗ broadcastToWaitlist FAILED", e);
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                break;

            case "INVITED":
                // Send to invited entrants
                notificationService.broadcastToInvited(currentOrganizer.getUserID(), eventID, title, message)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Notification sent to invited entrants!", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(OrganizerCreateNotificationFragment.this).navigateUp();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to send notification", e);
                            Toast.makeText(getContext(), "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                break;

            case "CANCELLED":
                // Send to cancelled entrants
                notificationService.broadcastToCancelled(currentOrganizer.getUserID(), eventID, title, message)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Notification sent to cancelled entrants!", Toast.LENGTH_SHORT).show();
                            NavHostFragment.findNavController(OrganizerCreateNotificationFragment.this).navigateUp();
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to send notification", e);
                            Toast.makeText(getContext(), "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                break;
            default:
                Log.e(TAG, "UNKNOWN ENTRANT TYPE: " + entrantType);
        }
    }
}
