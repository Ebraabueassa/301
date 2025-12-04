package com.example.community.ArrayAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.Notification;
import com.example.community.R;

import java.util.List;
import java.util.Map;

public class NotificationArrayAdapter extends RecyclerView.Adapter<NotificationArrayAdapter.ViewHolder> {
    private final List<Notification> notifications;
    private final Map<String, String> eventTitleMap;

    /**
     * Constructor accepting the list of notifications and the map of Event IDs to Event Titles.
     *
     * @param notifications List of notification objects.
     * @param eventTitleMap Map where Key = EventID and Value = Event Title.
     */
    public NotificationArrayAdapter(List<Notification> notifications, Map<String, String> eventTitleMap) {
        this.notifications = notifications;
        this.eventTitleMap = eventTitleMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifications.get(position);

        holder.message.setText(notif.getMessage() != null ? "-> " + notif.getMessage() : "");

        boolean showHeader = false;
        String currentEventID = notif.getEventID();

        if (position == 0) {
            showHeader = true;
        } else {
            String prevEventID = notifications.get(position - 1).getEventID();

            if (currentEventID != null && !currentEventID.equals(prevEventID)) {
                showHeader = true;
            } else if (currentEventID == null && prevEventID != null) {
                showHeader = true;
            }
        }

        if (showHeader) {
            holder.headerTitle.setVisibility(View.VISIBLE);

            if (currentEventID != null) {
                String title = eventTitleMap != null ? eventTitleMap.get(currentEventID) : null;
                holder.headerTitle.setText(title != null ? title : "Unknown Event");
            } else {
                holder.headerTitle.setText("General Notifications");
            }
        } else {
            holder.headerTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, headerTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.notificationLog);
            headerTitle = itemView.findViewById(R.id.eventTitle);
        }
    }
}
