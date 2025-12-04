package com.example.community.Screens;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.community.ArrayAdapters.EventArrayAdapter;
import com.example.community.DateValidation;
import com.example.community.Event;
import com.example.community.EventService;
import com.example.community.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
/**
 * Fragment for admins to view and manage upcoming events
 * <p<
 *    Display a list of all upcoming events scheduled within a year.
 *    Admins can perform admin actions like deleting events.
 * </p>
 * <p>
 *     Fragment loads events from the current date to one year in the future. Clicking on an event opens
 *     a dialog where the admin can delete the event. Confirmation dialog is shown before event deletion
 *     happens.
 * </p>
 *
 * @see EventService
 * @see EventArrayAdapter
 * @see DateValidation
 */
public class AdminEventFragment extends Fragment implements EventArrayAdapter.OnEventClickListener {

    /** Button to navigate to previous fragment */
    private Button backButton;
    /** RecyclerView displaying the list of upcoming events */
    private RecyclerView adminEventView;

    /** List of upcoming events */
    private ArrayList<Event> eventsArrayList;

    /** Adapter for managing the RecyclerView and event click handling. */
    private EventArrayAdapter eventArrayAdapter;

    /** Service for managing event data. */
    private EventService eventService;

    /**
     * Inflates the dialog's layout view
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.admin_event_page, container, false);
    }

    /**
     * Initializes the fragment's UI and data. Sets up click listeners, sets up RecyclerView
     * adapter, loads upcoming events.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adminEventView = view.findViewById(R.id.adminEventView);
        backButton = view.findViewById(R.id.buttonBack);


        eventService = new EventService();
        eventsArrayList = new ArrayList<>();


        adminEventView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventArrayAdapter = new EventArrayAdapter(eventsArrayList);

        eventArrayAdapter.setOnEventClickListener(this);

        adminEventView.setAdapter(eventArrayAdapter);

        loadEvents();
        setUpClickListener();
    }

    /**
     * Loads upcoming events from today to one year in the future.
     * Uses DateValidation to ensure the date range is valid.
     */
    private void loadEvents() {
        String fromDate = DateValidation.getCurrentDate();

        LocalDate futureDate = LocalDate.now().plusYears(1);
        String toDate = futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if (DateValidation.dateRangeValid(fromDate, toDate)) {
            eventService.listUpcoming(fromDate, toDate, null)
                    .addOnSuccessListener(events -> {
                        eventsArrayList.clear();
                        eventsArrayList.addAll(events);
                        eventArrayAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading events", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    /**
     * Handles the click event for an event in the RecyclerView.
     * When an event is clicked, displays action dialog
     *
     * @param event The Event object that was clicked.
     */
    @Override
    public void onEventClick(Event event) {
        showActionDialog(event);
    }

    /**
     * Displays an action dialog with admin operations for the selected event.
     * Provides the "Delete Event" option. When selected, confirmDelete is called.
     *
     * @param event the event for which the action dialog is being displayed
     */
    private void showActionDialog(Event event) {
        CharSequence[] options = new CharSequence[]{"Delete Event"};

        new AlertDialog.Builder(requireContext())
                .setTitle(event.getTitle())
                .setItems(options, (dialog, which) -> {
                    confirmDelete(event);
                })
                .show();
    }

    /**
     * Displays a confirmation dialog before deleting an event.
     *
     * @param event teh event to be deleted
     */
    private void confirmDelete(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete " + event.getTitle() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }


    /**
     * Deletes the specified event from the backend and updates the RecyclerView.
     * <p>
     *     How it works:
     *     <ul>
     *         <li>Calls the EventService to delete the event and removes it from the list.</li>
     *         <li>Removes event from the local list when successful</li>
     *         <li>Notifies the adapter of the change</li>
     *         <li>Shows a toast message</li>
     *     </ul>
     * </p>
     * @param event the event to be deleted
     */
    private void deleteEvent(Event event) {
        eventService.deleteEvent(event.getEventID())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int position = eventsArrayList.indexOf(event);
                        if (position != -1) {
                            eventsArrayList.remove(position);
                            eventArrayAdapter.notifyItemRemoved(position);
                            eventArrayAdapter.notifyItemRangeChanged(position, eventsArrayList.size() - position);
                        }
                        Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Sets up click listeners for UI components.
     * Currently only the back button.
     */
    private void setUpClickListener() {
        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AdminEventFragment.this)
                    .navigateUp();
        });
    }
}
