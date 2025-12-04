package com.example.community.Screens.OrganizerScreens;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.R;
import com.example.community.Screens.NotificationsFragment;

/**
 * Dialog fragment for selecting the target entrant type for a notification
 * <p>
 *      The dialog allows organizers to select the target entrant type for a notification
 *      and navigate to the notification creation fragment
 * </p>
 *
 * @see NotificationsFragment
 * */
public class NotificationTargetDialogFragment extends DialogFragment {

    /**
     * Tag for logging
     */
    private static final String TAG = "NotificationTargetDialogFragment";

    /**
     * Argument key for event ID
     */
    private static final String ARG_EVENT_ID = "event_id";

    /**
     * The ID of the event to create notifications for
     */
    private String eventID;

    /**
     * UI elements
     */
    private TextView messageTextView;
    private Button toWaitlistButton;
    private Button toInvitedButton;
    private Button toCancelledButton;

    /**
     * Creates a new instance of the fragment with the given event ID
     *
     * @param eventID the ID of the event to create notifications for
     * @return a new instance of the fragment
     */
    public static NotificationTargetDialogFragment newInstance(String eventID) {
        NotificationTargetDialogFragment fragment = new NotificationTargetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventID);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Initializes the fragment with the event ID
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
     * @return the view for the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.organizer_notification_target_dialog, container, false);
        return view;
    }

    /**
     * Creates a dialog for the fragment
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return the dialog for the fragment
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    /**
     * Initializes the views and sets up the button listeners
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messageTextView = view.findViewById(R.id.notificationTargetMessage);
        toWaitlistButton = view.findViewById(R.id.waitlistNotificationButton);
        toInvitedButton = view.findViewById(R.id.invitedNotificationButton);
        toCancelledButton = view.findViewById(R.id.cancelledNotificationButton);

        toWaitlistButton.setOnClickListener(v ->
                navigateToCreateNotification("WAITING"));
        toInvitedButton.setOnClickListener(v ->
                navigateToCreateNotification("INVITED"));
        toCancelledButton.setOnClickListener(v ->
                navigateToCreateNotification("CANCELLED"));
    }

    /**
     * Navigates to the notification creation fragment with the selected entrant type
     *
     * @param entrantType the type of entrant to notify
     */
    private void navigateToCreateNotification(String entrantType) {
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventID);
        args.putString("entrant_type", entrantType);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_OrganizerNotifyFragment_to_OrganizerCreateNotificationFragment, args);
        dismiss();
    }
}
