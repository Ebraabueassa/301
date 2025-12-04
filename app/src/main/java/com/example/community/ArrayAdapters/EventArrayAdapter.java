package com.example.community.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.Event;
import com.example.community.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of {@link Event} objects.
 * Each item in the list shows the event's name and description.
 */
public class EventArrayAdapter extends RecyclerView.Adapter<EventArrayAdapter.ViewHolder> {

    /** List of events to display in the RecyclerView */
    private List<Event> events;

    /** Listener for click events on individual Event items */
    private OnEventClickListener listener;

    /**
     * Interface for handling clicks on an Event item.
     */
    public interface OnEventClickListener {
        /**
         * Called when an Event item is clicked.
         *
         * @param event The Event object that was clicked.
         */
        void onEventClick(Event event);
    }

    /**
     * ViewHolder class for caching references to the views in each RecyclerView item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /** TextView displaying the event name */
        public TextView eventName;

        /** TextView displaying the event description */
        public TextView eventDescription;
        public ImageView eventThumbnail;

        /**
         * Constructor for ViewHolder.
         * Sets up click listener for the item view.
         *
         * @param view The root view of the RecyclerView item.
         */
        public ViewHolder(View view) {
            super(view);
            eventName = view.findViewById(R.id.event_name);
            eventDescription = view.findViewById(R.id.event_description);
            eventThumbnail = view.findViewById(R.id.event_thumbnail);

            view.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(events.get(position));
                }
            });

        }
    }

    /**
     * Constructor for EventArrayAdapter.
     *
     * @param events List of Event objects to display.
     */
    public EventArrayAdapter(List<Event> events) {
        this.events = events;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder}.
     *
     * @param viewGroup The parent ViewGroup into which the new view will be added.
     * @param viewType  The view type of the new view.
     * @return A new ViewHolder instance.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.entrant_event_list_content, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at the specified position.
     *
     * @param viewHolder The ViewHolder to bind data to.
     * @param position   Position of the item in the data set.
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Event event = events.get(position);
        viewHolder.eventName.setText(event.getTitle());
        viewHolder.eventDescription.setText(event.getDescription());

        String posterURL = event.getPosterImageURL();
        if (posterURL != null && !posterURL.isEmpty()) {
            Picasso.get()
                    .load(posterURL)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.community_logo_full)
                    .placeholder(R.drawable.community_logo_full)
                    . into(viewHolder.eventThumbnail);
        } else {
            // Use default placeholder if no poster URL
            viewHolder.eventThumbnail.setImageResource(R.drawable.community_logo_full);
        }
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return Number of events in the adapter.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Sets the listener for item click events.
     *
     * @param listener An implementation of {@link OnEventClickListener}.
     */
    public void setOnEventClickListener(OnEventClickListener listener) {
        this.listener = listener;
    }
}

