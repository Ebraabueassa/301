package com.example.community.ArrayAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.Notification;
import com.example.community.NotificationType;
import com.example.community.R;

import java.util.ArrayList;

/**
 * RecyclerView Adapter for displaying a list of {@link Notification} objects.
 * Provides buttons for actions such as Accept, Decline, or View Event based on notification type.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    /** Listener interface for handling notification actions */
    public interface NotificationActionListener {
        /**
         * Called when the user accepts a notification.
         *
         * @param notification The Notification object that was accepted.
         */
        void onAccept(Notification notification);

        /**
         * Called when the user declines a notification.
         *
         * @param notification The Notification object that was declined.
         */
        void onDecline(Notification notification);

        /**
         * Called when the user wants to view the related event.
         *
         * @param notification The Notification object associated with the event.
         */
        void onViewEvent(Notification notification);
    }

    /** List of notifications to display */
    private final ArrayList<Notification> notifications;

    /** Listener for handling notification actions */
    private final NotificationActionListener listener;

    /**
     * Constructor for NotificationAdapter.
     *
     * @param notifications List of notifications to display.
     * @param listener Listener for handling Accept, Decline, or View Event actions.
     */
    public NotificationAdapter(ArrayList<Notification> notifications,
                               NotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder}.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_page_menu, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder for the given position.
     * Sets button visibility and click actions depending on notification type.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position Position of the item in the notifications list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification n = notifications.get(position);

        // Set main notification text
        holder.messageText.setText(n.getMessage());

        // Set the middle button text
        holder.eventButton.setText("View Event");

        // Show Accept/Decline buttons only for WIN-type notifications
        if (n.getType() == NotificationType.WIN) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.declineButton.setVisibility(View.VISIBLE);
        } else {
            holder.acceptButton.setVisibility(View.GONE);
            holder.declineButton.setVisibility(View.GONE);
        }

        // Set click actions for buttons
        holder.eventButton.setOnClickListener(v -> {
            if (listener != null) listener.onViewEvent(n);
            else Toast.makeText(v.getContext(), "View event " + n.getEventID(),
                    Toast.LENGTH_SHORT).show();
        });

        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(n);
            else Toast.makeText(v.getContext(), "Accepted", Toast.LENGTH_SHORT).show();
        });

        holder.declineButton.setOnClickListener(v -> {
            if (listener != null) listener.onDecline(n);
            else Toast.makeText(v.getContext(), "Declined", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Returns the total number of notifications in the adapter.
     *
     * @return Size of the notifications list.
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * ViewHolder class for caching references to views in each notification item.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        /** TextView displaying the notification message */
        TextView messageText;

        /** Button to view related event */
        Button eventButton;

        /** Button to accept the notification */
        Button acceptButton;

        /** Button to decline the notification */
        Button declineButton;

        /**
         * Constructor for ViewHolder.
         *
         * @param itemView Root view of the RecyclerView item.
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText  = itemView.findViewById(R.id.popup_text);
            eventButton  = itemView.findViewById(R.id.studyGroup);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
    public void removeNotification(Notification notification) {
        int index = notifications.indexOf(notification);
        if (index != -1) {
            notifications.remove(index);
            notifyItemRemoved(index);
        }
    }
}
