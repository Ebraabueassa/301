package com.example.community.Screens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.community.R;
import com.example.community.UserService;

/**
 * Fragment that allows the user to view and adjust notification settings.
 * Provides confirm and cancel buttons to save or discard changes.
 */
public class NotificationSettingsFragment extends Fragment {

    private static final String TAG = "NotificationSettings";

    private UserService userService;
    private RadioGroup notificationRadioGroup;
    private RadioButton yesResultsRadio;
    private RadioButton noResultsRadio;
    private String userId;

    /**
     * Inflates the notification settings layout.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          Parent view container
     * @param savedInstanceState Saved state bundle
     * @return Inflated fragment view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_settings, container, false);
    }

    /**
     * Called after the fragment's view is created.
     * Sets up click listeners for confirm and cancel buttons.
     *
     * @param view               The fragment's view
     * @param savedInstanceState Saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService = new UserService();

        Button confirmButton = view.findViewById(R.id.confirm_button);
        Button cancelButton  = view.findViewById(R.id.cancel_popup);
        notificationRadioGroup = view.findViewById(R.id.notification_radio_group);
        yesResultsRadio      = view.findViewById(R.id.yes_results);
        noResultsRadio       = view.findViewById(R.id.no_results);

        // load current notification settings
        loadCurrentSettings();

        // confirm: save settings, then go back
        confirmButton.setOnClickListener(v -> saveSettingsAndGoBack());

        // cancel: just go back to previous page
        cancelButton.setOnClickListener(v ->
                NavHostFragment.findNavController(NotificationSettingsFragment.this)
                        .popBackStack()
        );
    }

    /**
     * Load the user's current notification preference and set the radio buttons.
     */
    private void loadCurrentSettings() {
        String deviceToken = userService.getDeviceToken();
        if (deviceToken == null || deviceToken.isEmpty()) {
            Log.e(TAG, "Unable to find device token");
            Toast.makeText(getContext(),
                    "Unable to load settings",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(uid -> {
                    userId = uid;
                    // load user to get current notification preference
                    userService.getByUserID(userId)
                            .addOnSuccessListener(user -> {
                                if (user != null) {
                                    Boolean receiveNotifications = user.getReceiveNotifications();
                                    if (receiveNotifications != null && receiveNotifications) {
                                        yesResultsRadio.setChecked(true);
                                    } else {
                                        noResultsRadio.setChecked(true);
                                    }
                                    Log.d(TAG, "Loaded notification setting: " + receiveNotifications);
                                } else {
                                    // default to yes if user not found
                                    yesResultsRadio.setChecked(true);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to load user settings", e);
                                // default to yes on error
                                yesResultsRadio.setChecked(true);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user ID", e);
                    Toast.makeText(getContext(),
                            "Failed to load settings",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveSettingsAndGoBack() {
        // check if any radio button is selected
        int selectedId = notificationRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(getContext(),
                    "Please select an option",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // figure out which option is selected
        final boolean wantsNotifications = yesResultsRadio.isChecked();

        // if we already have userId from loading, use it
        if (userId != null) {
            updateNotificationSetting(userId, wantsNotifications);
            return;
        }

        // otherwise, get it again
        String deviceToken = userService.getDeviceToken();
        if (deviceToken == null || deviceToken.isEmpty()) {
            Toast.makeText(getContext(),
                    "Unable to find current user device token",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(uid -> updateNotificationSetting(uid, wantsNotifications))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get user ID", e);
                    Toast.makeText(getContext(),
                            "Failed to load user for notification settings",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Update the notification setting for the user.
     */
    private void updateNotificationSetting(String userId, boolean wantsNotifications) {
        if (wantsNotifications) {
            userService.enableNotifications(userId)
                    .addOnSuccessListener(v -> {
                        Log.d(TAG, "Notifications enabled");
                        Toast.makeText(getContext(),
                                "Notifications enabled",
                                Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(NotificationSettingsFragment.this)
                                .popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to enable notifications", e);
                        Toast.makeText(getContext(),
                                "Failed to enable notifications",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            userService.disableNotifications(userId)
                    .addOnSuccessListener(v -> {
                        Log.d(TAG, "Notifications disabled");
                        Toast.makeText(getContext(),
                                "Notifications disabled",
                                Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(NotificationSettingsFragment.this)
                                .popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to disable notifications", e);
                        Toast.makeText(getContext(),
                                "Failed to disable notifications",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}