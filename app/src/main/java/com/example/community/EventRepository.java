package com.example.community;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing event data in Firestore.
 * Handles all database operations for events.
 */
public class EventRepository {

    private final String TAG = "EventRepository";

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    /**
     * Creates a new EventRepository instance.
     * Initializes Firestore connection.
     */
    public EventRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("events");
    }

    /**
     * Saves a new event to the database.
     *
     * @param event event to create
     * @return task that completes when creation finishes
     */
    public Task<Void> create(Event event) {
        return eventsRef.document(event.getEventID()).set(event);
    }

    /**
     * Retrieves an event by its ID.
     *
     * @param eventID ID of the event
     * @return task containing the event or null if not found
     */
    public Task<Event> getByID(String eventID) {
        return eventsRef.document(eventID).get().continueWith(task -> {
            DocumentSnapshot snapshot = task.getResult();
            return snapshot.exists() ? snapshot.toObject(Event.class) : null;
        });
    }

    /**
     * Updates an existing event in the database.
     *
     * @param event event with updated data
     * @return task that completes when update finishes
     */
    public Task<Void> update(Event event) {
        return eventsRef.document(event.getEventID()).set(event);
    }

    /**
     * Deletes an event from the database.
     *  US 03.01.01
     *
     * @param eventID ID of the event to delete
     * @return task that completes when deletion finishes
     */
    public Task<Void> delete(String eventID) {
        return eventsRef.document(eventID).delete();
    }

    /**
     * Retrieves all events from the database.
     *
     * @return task containing list of all events
     */
    public Task<List<Event>> getAll() {
        return eventsRef.get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<Event> events = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            events.add(event);
                        }
                    }
                    return events;
                });
    }

    /**
     * Lists events created by a specific organizer.
     *
     * @param organizerID  ID of the organizer
     * @param limit        maximum number of events to return
     * @param startAfterID ID to start pagination after
     * @return task containing list of events
     */
    public Task<List<Event>> listEventsByOrganizer(String organizerID, int limit,
                                                   String startAfterID) {
        com.google.firebase.firestore.Query query =
                eventsRef.whereEqualTo("organizerID", organizerID).limit(limit);

        if (startAfterID != null) {
            query = query.startAfter(startAfterID);
        }

        return query.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<Event> events = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                    events.add(event);
                }
            }
            return events;
        });
    }

    /**
     * Lists upcoming open events within a date range and optional tag filter.
     *
     * @param fromDate     earliest event start date
     * @param toDate       latest event start date
     * @param tags         optional list of tags to filter by
     * @param limit        maximum number of events to return
     * @param startAfterID ID to start pagination after
     * @return task containing list of matching events
     */
    public Task<List<Event>> listUpcoming(String fromDate, String toDate, List<String> tags,
                                          int limit, String startAfterID) {

        Query query = eventsRef
                .whereEqualTo("status", EventStatus.OPEN.name());  // only open events

        if (fromDate != null) {
            query = query.whereGreaterThanOrEqualTo("eventStartDate", fromDate);
        }
        if (toDate != null) {
            query = query.whereLessThanOrEqualTo("eventStartDate", toDate);
        }
        if (tags != null && !tags.isEmpty()) {
            query = query.whereArrayContains("tags", tags.get(0));
        }

        query = query.limit(limit);
        if (startAfterID != null) {
            query = query.startAfter(startAfterID);
        }

        return query.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            java.util.List<Event> events = new java.util.ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                Event e = doc.toObject(Event.class);
                if (e != null) {
                    events.add(e);
                    Log.d("EventRepository", "Found event: " +e.getTitle());
                }
            }
            Log.d("EventRepository", "Found " + events.size() + " events");
            return events;
        });
    }

    /**
     * Lists events matching tags with pagination.
     *
     * @param tags         list of tags to filter by
     * @param limit        maximum number of events to return
     * @param startAfterID ID to start pagination after
     * @return task containing list of matching events
     */
    public Task<List<Event>> listByTags(List<String> tags, int limit, String startAfterID) {
        if (tags == null || tags.isEmpty()) {
            return getAll();
        }

        com.google.firebase.firestore.Query query = eventsRef
                .whereEqualTo("status", EventStatus.OPEN.name());

        // For simplicity, we'll match any of the tags (OR logic)
        // For more complex matching, consider using array-contains-any if under 10 tags
        query = query.whereArrayContains("tags", tags.get(0));

        query = query.limit(limit);
        if (startAfterID != null) {
            query = query.startAfter(startAfterID);
        }

        return query.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            java.util.List<Event> events = new java.util.ArrayList<>();
            for (com.google.firebase.firestore.DocumentSnapshot doc : task.getResult()) {
                Event e = doc.toObject(Event.class);
                if (e != null) {
                    events.add(e);
                }
            }
            return events;
        });
    }

}
