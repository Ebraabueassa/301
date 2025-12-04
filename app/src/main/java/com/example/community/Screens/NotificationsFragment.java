package com.example.community.Screens;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.ArrayAdapters.NotificationAdapter;
import com.example.community.Notification;
import com.example.community.NotificationService;
import com.example.community.R;
import com.example.community.UserService;

import java.util.ArrayList;

/**
 * Fragment that displays notifications for the current user.
 * Users can view, accept, or decline invitations and navigate to notification settings.
 */
public class NotificationsFragment extends Fragment {

    ImageButton notificationSettingsButton;
    Button backButton;
    RecyclerView notificationList;

    private ArrayList<Notification> notifications;
    private NotificationAdapter notificationAdapter;

    private NotificationService notificationService;
    private UserService userService;

    // store the current userId here once we resolve it
    private String currentUserId;

    /**
     * Inflates the notifications layout.
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
        return inflater.inflate(R.layout.notification_page, container, false);
    }

    /**
     * Called after the fragment's view is created.
     * Initializes UI components, sets up RecyclerView, and loads notifications.
     *
     * @param view               The fragment's view
     * @param savedInstanceState Saved state bundle
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationSettingsButton = view.findViewById(R.id.notificationSettings);
        backButton = view.findViewById(R.id.backToEntrantHome);
        notificationList = view.findViewById(R.id.notificationList);

        notifications = new ArrayList<>();
        notificationService = new NotificationService();
        userService = new UserService();

        // Initialize RecyclerView adapter
        notificationAdapter = new NotificationAdapter(
                notifications,
                new NotificationAdapter.NotificationActionListener() {
                    @Override
                    public void onAccept(Notification notification) {
                        // Guard: make sure we actually have a userId
                        if (currentUserId == null || currentUserId.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "User not loaded yet. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        notificationService.respondToInvitation(
                                        notification.getEventID(),
                                        currentUserId,
                                        true
                                )
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(getContext(),
                                            "Invitation accepted",
                                            Toast.LENGTH_SHORT).show();
                                    dismissNotification(notification);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Failed to accept invitation",
                                                Toast.LENGTH_SHORT).show()
                                );

                    }

                    @Override
                    public void onDecline(Notification notification) {
                        // Same guard here
                        if (currentUserId == null || currentUserId.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "User not loaded yet. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        notificationService.respondToInvitation(
                                        notification.getEventID(),
                                        currentUserId,
                                        false
                                )
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(getContext(),
                                            "Invitation declined",
                                            Toast.LENGTH_SHORT).show();
                                    dismissNotification(notification);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Failed to decline invitation",
                                                Toast.LENGTH_SHORT).show()
                                );
                    }

                    @Override
                    public void onViewEvent(Notification notification) {
                        if (notification.getEventID() == null || notification.getEventID().isEmpty()) {
                            Toast.makeText(getContext(),
                                    "Event ID not found",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString("event_id", notification.getEventID());
                        NavHostFragment.findNavController(NotificationsFragment.this)
                                .navigate(R.id.action_NotificationsFragment_to_EventDetailsFragment, bundle);
                    }
                });

        notificationList.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList.setAdapter(notificationAdapter);

        // Load notifications
        loadNotificationsForCurrentUser();

        notificationSettingsButton.setOnClickListener(v ->
                NavHostFragment.findNavController(NotificationsFragment.this)
                        .navigate(R.id.action_EntrantNotificationsFragment_to_NotificationSettingsFragment)
        );

        backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(NotificationsFragment.this)
                        .popBackStack()
        );
    }

    /**
     * Loads the list of notifications for the current user based on device token.
     * Updates the RecyclerView adapter with the fetched notifications.
     */
    private void loadNotificationsForCurrentUser() {
        String deviceToken = userService.getDeviceToken();
        if (deviceToken == null || deviceToken.isEmpty()) {
            Toast.makeText(getContext(),
                    "Device token not found – cannot load notifications",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        userService.getUserIDByDeviceToken(deviceToken)
                .addOnSuccessListener(userId -> {

                    // Save for accept/decline actions
                    currentUserId = userId;

                    notificationService.listUserNotification(userId, 50, null)
                            .addOnSuccessListener(fetchedNotifications -> {
                                notifications.clear();
                                for (Notification n : fetchedNotifications) {
                                    if (!n.isDismissed()) {
                                    notifications.add(n);
                                    }
                                }
                                notificationAdapter.notifyDataSetChanged();

                            })
                            .addOnFailureListener(e -> {
                                e.printStackTrace();
                                Toast.makeText(getContext(),
                                        "Failed to load notifications: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(getContext(),
                            "Failed to resolve user from device token: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void dismissNotification(Notification notification) {
        notificationService.dismissNotification(notification.getNotificationID())
                .addOnSuccessListener(v -> {
                    notificationAdapter.removeNotification(notification);
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationsFragment", "Failed to dismiss notification", e);
                    Toast.makeText(getContext(), "Failed to dismiss notification", Toast.LENGTH_SHORT).show();
                });
    }

}
