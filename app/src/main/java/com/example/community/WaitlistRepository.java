package com.example.community;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing waitlist entry data in Firestore.
 * Handles all database operations for event waitlists.
 */
public class WaitlistRepository {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private static final String SUBCOLLECTION_WAITLIST = "waitlist";

    /**
     * Creates a new WaitlistRepository instance.
     * Initializes Firestore connection.
     */
    public WaitlistRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.eventsRef = db.collection("events");
    }

    /**
     * Saves a new waitlist entry to the database.
     *
     * @param entry entry to create
     * @return task that completes when creation finishes
     */
    public Task<Void> create(WaitingListEntry entry) {
        return eventsRef.document(entry.getEventID())
                .collection(SUBCOLLECTION_WAITLIST)
                .document(entry.getUserID()).set(entry);
    }

    /**
     * Retrieves a waitlist entry by event and user ID.
     *
     * @param eventID ID of the event
     * @param userID ID of the user
     * @return task containing the entry or null if not found
     */
    public Task<WaitingListEntry> getByID(String eventID, String userID) {
        return eventsRef.document(eventID).collection(SUBCOLLECTION_WAITLIST)
                .document(userID)
                .get()
                .continueWith(task -> {
            DocumentSnapshot snapshot = task.getResult();
            return snapshot.exists() ? snapshot.toObject(WaitingListEntry.class) : null;
        });
    }


    /**
     * Updates an existing waitlist entry.
     *
     * @param entry entry with updated data
     * @return task that completes when update finishes
     */
    public Task<Void> update(WaitingListEntry entry) {
        return eventsRef.document(entry.getEventID())
                .collection(SUBCOLLECTION_WAITLIST)
                .document(entry.getUserID()).set(entry);
    }

    /**
     * Deletes a waitlist entry.
     *
     * @param eventID ID of the event
     * @param userID ID of the user
     * @return task that completes when deletion finishes
     */
    public Task<Void> delete(String eventID, String userID) {
        return eventsRef.document(eventID)
                .collection(SUBCOLLECTION_WAITLIST)
                .document(userID).delete();
    }

    /**
     * Gets all waitlist entries for an event.
     *
     * @param eventID ID of the event
     * @return task containing list of entries
     */
    public Task<List<WaitingListEntry>> listByEvent(String eventID) {
        return eventsRef.document(eventID)
                .collection(SUBCOLLECTION_WAITLIST)
                .get()
                .continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return task.getResult().toObjects(WaitingListEntry.class);
        });
    }

    /**
     * Gets waitlist entries for an event filtered by status.
     *
     * @param eventID ID of the event
     * @param status status to filter by
     * @return task containing list of matching entries
     */
    public Task<List<WaitingListEntry>> listByEventAndStatus(String eventID, EntryStatus status) {
        return eventsRef.document(eventID)
                .collection(SUBCOLLECTION_WAITLIST)
                .whereEqualTo("status", status)
                .get()
                .continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return task.getResult().toObjects(WaitingListEntry.class);
        });
    }

    /**
     * Counts total waitlist entries for an event.
     *
     * @param eventID ID of the event
     * @return task containing the count
     */
    public Task<Long> countByEvent(String eventID) {
        return eventsRef.document(eventID)
                .collection(SUBCOLLECTION_WAITLIST)
                .get()
                .continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return (long) task.getResult().size();
        });
    }

    /**
     * Counts waitlist entries grouped by status for an event.
     *
     * @param eventID ID of the event
     * @return task containing map of status to count
     */
    public Task<Map<EntryStatus, Long>> countsByEventGrouped(String eventID) {
        return eventsRef.document(eventID)
                .collection(SUBCOLLECTION_WAITLIST)
                .get()
                .continueWith(task -> {
            QuerySnapshot snapshot = task.getResult();
            Map<EntryStatus, Long> counts = new HashMap<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                if (entry != null) {
                    EntryStatus status = entry.getStatus();
                    counts.put(status, counts.getOrDefault(status, 0L) + 1);
                }
            }
            return counts;
        });
    }

    /**
     * Gets all waitlist entries for a user across all events.
     *
     * @param userID ID of the user
     * @return task containing list of entries
     */
    public Task<List<WaitingListEntry>> listByUser(String userID) {
        return db.collectionGroup(SUBCOLLECTION_WAITLIST)
                .whereEqualTo("userID", userID)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return task.getResult().toObjects(WaitingListEntry.class);
                });
    }

    /**
     * Counts waitlist entries for an event filtered by status.
     *
     * @param eventID ID of the event
     * @param status status to filter by
     * @return task containing the count
     */
    public Task<Long> countByEventAndStatus(String eventID, EntryStatus status) {
        return eventsRef. document(eventID)
                .collection(SUBCOLLECTION_WAITLIST)
                .whereEqualTo("status", status)
                .get()
                .continueWith(task -> {
                    if (! task.isSuccessful()) {
                        throw task.getException();
                    }
                    return (long) task.getResult(). size();
                });
    }


}
