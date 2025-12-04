package com.example.community;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for event operations.
 * Handles business logic for creating, updating, and managing events.
 */
public class EventService {
    private static final String TAG = "EventService";
    private final EventRepository eventRepository;
    private final WaitlistRepository waitlistRepository;
    private final QRCodeService qrCodeService;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Creates a new EventService instance.
     * Initializes all required repositories and services.
     */
    public EventService() {
        this.eventRepository = new EventRepository();
        this.waitlistRepository = new WaitlistRepository();
        this.qrCodeService = new QRCodeService();
        this.imageService = new ImageService();
        this.userRepository = new UserRepository();
        this.notificationRepository = new NotificationRepository();
    }

    /**
     * Creates a new event and adds it to the organizer's created events list.
     *
     * @param organizerID ID of the event organizer
     * @param title       event title
     * @param description event description
     * @param maxCapacity maximum number of attendees
     * @param startDate   event start date
     * @param endDate     event end date
     * @return task containing the new event ID
     */
    public Task<String> createEvent(String organizerID, String title, String description, String location,
                                    Integer maxCapacity, String startDate, String endDate, Integer maxWaitingListSize,
                                    String regStart, String regEnd)
    {
        Event e = new Event();
        e.setEventID(java.util.UUID.randomUUID().toString());
        e.setOrganizerID(organizerID);
        e.setTitle(title);
        e.setDescription(description);
        e.setLocation(location);
        e.setMaxCapacity(maxCapacity);
        e.setCurrentCapacity(0);
        e.setEventStartDate(startDate);
        e.setEventEndDate(endDate);
        e.setStatus(EventStatus.OPEN);
        e.setRegistrationStart(regStart);
        e.setRegistrationEnd(regEnd);
        if (maxWaitingListSize != null ) {
            e.setWaitlistCapacity(maxWaitingListSize);
        }

        return eventRepository.create(e)
                .continueWithTask(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    final String eventID = e.getEventID();

                    // append to organizer.eventsCreatedIDs
                    return userRepository
                            .getByUserID(organizerID)
                            .continueWithTask(ut -> {
                                User u = ut.getResult();
                                if (u == null) {
                                    return Tasks
                                            .forException(new IllegalArgumentException("Organizer not found"));
                                }
                                if (!u.hasEventCreated(eventID)) {
                                    u.addEventCreated(eventID);
                                    return userRepository
                                            .update(u)
                                            .continueWith(tt -> eventID);
                                }
                                // already recorded; just return id
                                return Tasks.forResult(eventID);
                            });
                });
    }

    /**
     * Updates an existing event with new information.
     *
     * @param organizerID ID of the organizer making the update
     * @param patch       event object with updated fields
     * @return task that completes when update finishes
     */
    public Task<Void> updateEvent(String organizerID, Event patch) {
        return eventRepository.update(patch);
    }

    /**
     * Changes an event status to open/published.
     *
     * @param organizerID ID of the organizer
     * @param eventID     ID of the event to publish
     * @return task that completes when status is updated
     */
    public Task<Void> publishEvent(String organizerID, String eventID) {
        return eventRepository.getByID(eventID).continueWithTask(task -> {
            Event event = task.getResult();
            if (event == null) {
                return Tasks.forException(new IllegalArgumentException("Event not found"));
            }
            if (!event.getOrganizerID().equals(organizerID)) {
                return Tasks.forException(new SecurityException("Not authorized"));
            }
            event.setStatus(EventStatus.OPEN);
            return eventRepository.update(event);
        });
    }

    /**
     * Changes an event status to cancelled.
     *
     * @param organizerID ID of the organizer
     * @param eventID     ID of the event to cancel
     * @return task that completes when status is updated
     */
    public Task<Void> cancelEvent(String organizerID, String eventID) {
        return eventRepository.getByID(eventID).continueWithTask(task -> {
            Event event = task.getResult();
            if (event == null) {
                return Tasks.forException(new IllegalArgumentException("Event not found"));
            }
            if (!event.getOrganizerID().equals(organizerID)) {
                return Tasks.forException(new SecurityException("Not authorized"));
            }
            event.setStatus(EventStatus.CANCELLED);
            return eventRepository.update(event);
        });
    }

    /**
     * Lists all events created by an organizer with pagination.
     *
     * @param organizerID  ID of the organizer
     * @param limit        maximum number of events to return
     * @param startAfterID ID to start pagination after
     * @return task containing list of events
     */
    public Task<List<Event>> listEventsByOrganizer(String organizerID, int limit, String startAfterID) {
        return eventRepository.listEventsByOrganizer(organizerID, limit, startAfterID);
    }

    /**
     * Uploads and sets a poster image for an event.
     *
     * @param organizerID ID of the organizer
     * @param eventID     ID of the event
     * @param imageData   raw image bytes
     * @param uploadedBy  ID of user uploading the image
     * @return task containing the image URL
     */
    public Task<String> setPoster(String organizerID, String eventID, byte[] imageData, String uploadedBy) {
        return imageService.uploadEventPoster(eventID, imageData, uploadedBy, true).continueWith(t -> t.getResult().getImageURL());
    }

    /**
     * Generates a new QR code for an event.
     *
     * @param organizerID ID of the organizer
     * @param eventID     ID of the event
     * @return task containing the QR code image URL
     */
    public Task<String> refreshEventQR(String organizerID, String eventID) {
        return qrCodeService.generateAndUploadQRCode(eventID, organizerID).continueWith(t -> t.getResult().getImageURL());
    }

    /**
     * Lists all upcoming open events within a date range.
     *
     * @param fromDate earliest event start date
     * @param toDate   latest event start date
     * @param tags     optional list of tags to filter by
     * @return task containing list of open events
     */
    public Task<List<Event>> listUpcoming(String fromDate, String toDate, List<String> tags) {
        return eventRepository.listUpcoming(fromDate, toDate, tags, 50, null).continueWith(task -> {
            List<Event> all = task.getResult();
            List<Event> openOnly = new java.util.ArrayList<>();
            for (Event e : all) {
                if (e != null && e.getStatus() == EventStatus.OPEN) {
                    openOnly.add(e);
                }
            }
            return openOnly;
        });
    }

    /**
     * Lists events a user can join (not already on waitlist).
     *
     * @param userID   ID of the user
     * @param fromDate earliest event start date
     * @param toDate   latest event start date
     * @param tags     optional list of tags to filter by
     * @return task containing list of joinable events
     */
    public Task<List<Event>> listJoinable(String userID, String fromDate, String toDate, List<String> tags) {
        return listUpcoming(fromDate, toDate, tags)   // already filtered to OPEN
                .onSuccessTask(events -> {
                    List<Task<Boolean>> checks = new java.util.ArrayList<>();
                    for (Event e : events) {
                        checks.add(waitlistRepository.getByID(e.getEventID(), userID).continueWith(t -> t.getResult() == null));
                    }
                    return Tasks.whenAllSuccess(checks).continueWith(t -> {
                        List<?> results = t.getResult();
                        List<Event> joinable = new java.util.ArrayList<>();
                        for (int i = 0; i < events.size(); i++) {
                            boolean canJoin = (Boolean) results.get(i);
                            if (canJoin) {
                                joinable.add(events.get(i));
                            }
                        }
                        return joinable;
                    });
                });
    }

    /**
     * Lists joinable events filtered by user interests.
     *
     * @param userID   ID of the user
     * @param fromDate earliest event start date
     * @param toDate   latest event start date
     * @param tags     optional list of tags to filter by
     * @return task containing list of matching events
     */
    public Task<List<Event>> listJoinableByInterests(String userID, String fromDate, String toDate, List<String> tags) {
        return listJoinable(userID, fromDate, toDate, tags);
    }

    /**
     * Retrieves a single event by ID.
     *
     * @param eventID ID of the event
     * @return task containing the event
     */
    public Task<Event> getEvent(String eventID) {
        return eventRepository.getByID(eventID);
    }

    /**
     * Gets the QR code URL for an event.
     *
     * @param organizerID ID of the organizer
     * @param eventID     ID of the event
     * @return task containing the QR code URL
     */
    public Task<String> getEventQRCode(String organizerID, String eventID) {
        return eventRepository.getByID(eventID).continueWith(task -> {
            Event event = task.getResult();
            if (event == null) {
                throw new IllegalArgumentException("Event not found");
            }
            return event.getQRCodeImageURL();
        });
    }

    /**
     * Gets all users who have accepted invitations to an event.
     *
     * @param eventID ID of the event
     * @return task containing list of attendee users
     */
    public Task<List<User>> getAttendees(String eventID) {
        return waitlistRepository
                .listByEventAndStatus(eventID, EntryStatus.ACCEPTED)
                .onSuccessTask(entries -> {
                    java.util.List<Task<User>> reads = new java.util.ArrayList<>();
                    for (WaitingListEntry e : entries) {
                        reads.add(userRepository.getByUserID(e.getUserID()));
                    }
                    return com.google.android.gms.tasks.Tasks.whenAllSuccess(reads)
                            .continueWith(t -> {
                                java.util.List<?> results = t.getResult();
                                java.util.List<User> users = new java.util.ArrayList<>();
                                for (Object o : results) {
                                    User u = (User) o;
                                    if (u != null) users.add(u);
                                }
                                return users;
                            });
                });
    }

    /**
     * Gets all users who cancelled their participation in an event.
     *
     * @param eventID ID of the event
     * @return task containing list of cancelled users
     */
    public Task<List<User>> getCancelledUsers(String eventID) {
        return waitlistRepository
                .listByEventAndStatus(eventID, EntryStatus.CANCELLED)
                .onSuccessTask(entries -> {
                    java.util.List<Task<User>> reads = new java.util.ArrayList<>();
                    for (WaitingListEntry e : entries) {
                        reads.add(userRepository.getByUserID(e.getUserID()));
                    }
                    return com.google.android.gms.tasks.Tasks.whenAllSuccess(reads)
                            .continueWith(t -> {
                                java.util.List<?> results = t.getResult();
                                java.util.List<User> users = new java.util.ArrayList<>();
                                for (Object o : results) {
                                    User u = (User) o;
                                    if (u != null) users.add(u);
                                }
                                return users;
                            });
                });
    }

    /**
     * Gets all users who declined invitations to an event.
     *
     * @param eventID ID of the event
     * @return task containing list of declined users
     */
    public Task<List<User>> getDeclinedUsers(String eventID) {
        return waitlistRepository
                .listByEventAndStatus(eventID, EntryStatus.DECLINED)
                .onSuccessTask(entries -> {
                    java.util.List<Task<User>> reads = new java.util.ArrayList<>();
                    for (WaitingListEntry e : entries) {
                        reads.add(userRepository.getByUserID(e.getUserID()));
                    }
                    return com.google.android.gms.tasks.Tasks.whenAllSuccess(reads)
                            .continueWith(t -> {
                                java.util.List<?> results = t.getResult();
                                java.util.List<User> users = new java.util.ArrayList<>();
                                for (Object o : results) {
                                    User u = (User) o;
                                    if (u != null) users.add(u);
                                }
                                return users;
                            });
                });
    }

    /**
     * Exports final list of entrants in CSV format.
     *
     * @param eventID ID of the event
     * @return task containing CSV string of enrolled entrants
     */
    public Task<String> exportAttendeesCSV(String organizerID, String eventID) {
        return getAttendees(eventID).continueWith(task -> {
            List<User> attendees = task.getResult();
            StringBuilder csvBuilder = new StringBuilder();

            // CSV header
            csvBuilder.append("Name,Email,Phone Number\n");

            // CSV rows
            for (User user : attendees) {
                csvBuilder.append(String.format("%s,%s,%s\n",
                        escapeCSVField(user.getUsername()),
                        escapeCSVField(user.getEmail()),
                        escapeCSVField(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")));
            }

            return csvBuilder.toString();
        });
    }

    /**
     * Helper method to escape CSV fields.
     */
    private String escapeCSVField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Gets the organizer ID for an event.
     *
     * @param eventID ID of the event
     * @return task containing the organizer ID
     */
    public Task<String> getOrganizerID(String eventID) {
        return eventRepository.getByID(eventID).continueWith(task -> {
            Event event = task.getResult();
            if (event == null) {
                throw new IllegalArgumentException("Event not found");
            }
            return event.getOrganizerID();
        });
    }

    /**
     * Completely deletes an event and all related data (cascade deletion).
     *
     * Deletes:
     * - All waitlist entries
     * - Event from all users' lists (waitlist, attending, registration history)
     * - Event from organizer's eventsCreatedIDs
     * - Event poster image
     * - Event QR code image
     * - All notifications related to the event
     * - The event document itself
     *
     * @param eventID     ID of the event to delete
     * @return task that completes when all cascading deletions are done
     */
    public Task<Void> deleteEvent(String eventID) {
        Log.d(TAG, "Starting cascade deletion for event: " + eventID);

        return eventRepository.getByID(eventID).continueWithTask(eventTask -> {
            if (!eventTask.isSuccessful()) {
                Log.e(TAG, "Failed to get event", eventTask.getException());
                return Tasks.forException(eventTask.getException());
            }

            Event event = eventTask.getResult();
            if (event == null) {
                Log.e(TAG, "Event not found: " + eventID);
                return Tasks.forException(new IllegalArgumentException("Event not found"));
            }

            Log.d(TAG, "tarting cleanup");

            // delete images first (before event document is deleted)
            Log.d(TAG, "Deleting images");
            List<Task<Void>> imageTasks = new ArrayList<>();

            if (event.getPosterImageID() != null) {
                Task<Void> deletePoster = imageService.deleteEventPoster(eventID)
                        .addOnSuccessListener(v -> Log.d(TAG, "Deleted poster image"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to delete poster", e));
                imageTasks.add(deletePoster);
            }
            if (event.getQRCodeImageID() != null) {
                Task<Void> deleteQR = qrCodeService.deleteEventQRCode(eventID)
                        .addOnSuccessListener(v -> Log.d(TAG, "Deleted QR code"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to delete QR code", e));
                imageTasks.add(deleteQR);
            }

            // wait for images to be deleted first
            return Tasks.whenAll(imageTasks).continueWithTask(imageResult -> {
                        List<Task<Void>> allTasks = new ArrayList<>();

                        // clean up waitlists
                        Log.d(TAG, "Cleaning up waitlists");
                        Task<Void> waitlistCleanup = waitlistRepository.listByEvent(eventID)
                                .continueWithTask(wTask -> {
                                    if (!wTask.isSuccessful()) {
                                        Log.e(TAG, "Failed to list waitlist entries", wTask.getException());
                                        return Tasks.forResult(null);
                                    }

                                    List<WaitingListEntry> entries = wTask.getResult();
                                    if (entries == null || entries.isEmpty()) {
                                        Log.d(TAG, "No waitlist entries to clean up");
                                        return Tasks.forResult(null);
                                    }

                                    Log.d(TAG, "Found " + entries.size() + " waitlist entries to clean");
                                    List<Task<Void>> tasks = new ArrayList<>();

                                    for (WaitingListEntry entry : entries) {
                                        // remove event from each user's lists
                                        Task<Void> userCleanup = userRepository.getByUserID(entry.getUserID())
                                                .continueWithTask(uTask -> {
                                                    if (!uTask.isSuccessful()) {
                                                        Log.e(TAG, "Failed to get user", uTask.getException());
                                                        return Tasks.forResult(null);
                                                    }

                                                    User user = uTask.getResult();
                                                    if (user == null) return Tasks.forResult(null);

                                                    if (user.hasEventInWaitlist(eventID)) {
                                                        user.getWaitingListsJoinedIDs().remove(eventID);
                                                    }
                                                    if (user.hasEventInAttendingList(eventID)) {
                                                        user.getAttendingListsIDs().remove(eventID);
                                                    }
                                                    if (user.hasEventInRegistrationHistory(eventID)) {
                                                        user.getRegistrationHistoryIDs().remove(eventID);
                                                    }

                                                    return userRepository.update(user);
                                                })
                                                .addOnFailureListener(e -> Log.e(TAG, "Failed to update user", e));
                                        tasks.add(userCleanup);

                                        // delete the waitlist entry
                                        Task<Void> deleteEntry = waitlistRepository.delete(eventID, entry.getUserID())
                                                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete waitlist entry", e));
                                        tasks.add(deleteEntry);
                                    }

                                    return Tasks.whenAll(tasks);
                                })
                                .addOnSuccessListener(v -> Log.d(TAG, "Waitlist cleanup completed"))
                                .addOnFailureListener(e -> Log.e(TAG, "Waitlist cleanup failed", e));
                        allTasks.add(waitlistCleanup);

                        // remove event from organizer's eventsCreatedIDs
                        Log.d(TAG, "Cleaning up organizer");
                        Task<Void> organizerCleanup = userRepository
                                .getByUserID(event.getOrganizerID())
                                .continueWithTask(oTask -> {
                                    if (!oTask.isSuccessful()) {
                                        Log.e(TAG, "Failed to get organizer", oTask.getException());
                                        return Tasks.forResult(null);
                                    }

                                    User organizer = oTask.getResult();
                                    if (organizer != null && organizer.hasEventCreated(eventID)) {
                                        organizer.getEventsCreatedIDs().remove(eventID);
                                        return userRepository.update(organizer);
                                    }
                                    return Tasks.forResult(null);
                                })
                                .addOnSuccessListener(v -> Log.d(TAG, "Organizer cleanup completed"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to cleanup organizer", e));
                        allTasks.add(organizerCleanup);

                        // delete all notifications
                        Log.d(TAG, "Deleting notifications");
                        Task<Void> notificationCleanup = notificationRepository.deleteAllForEvent(eventID)
                                .addOnSuccessListener(v -> Log.d(TAG, "Notifications deleted"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete notifications", e));
                        allTasks.add(notificationCleanup);

                        // delete the event document last
                        Log.d(TAG, "Deleting event document");
                        return Tasks.whenAll(allTasks).continueWithTask(cleanupResult -> {
                            return eventRepository.delete(eventID)
                                    .addOnSuccessListener(v -> Log.d(TAG, "Event document deleted"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete event document", e));
                        });
                    }).addOnSuccessListener(v -> Log.d(TAG, "Event cascade deletion completed successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Event cascade deletion failed", e));
        });
    }
}
