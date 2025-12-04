package com.example.community;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service layer for waitlist entry operations.
 * Handles joining, leaving, and managing invitations for events.
 */
public class WaitingListEntryService {

    private WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    /**
     * Creates a new WaitingListEntryService instance.
     * Initializes required repositories.
     */
    public WaitingListEntryService() {
        waitlistRepository = new WaitlistRepository();
        this.eventRepository = new EventRepository();
        this.userRepository = new UserRepository();
    }

    /**
     * Adds a user to an event's waitlist.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @return task that completes when user is added
     */
    public Task<Void> join(String userID, String eventID) {
        return waitlistRepository.getByID(eventID, userID). continueWithTask(task -> {
            WaitingListEntry existing = task.getResult();
            if (existing != null) {
                return Tasks.forException(new IllegalArgumentException("Already on waitlist"));
            }

            // Check waitlist capacity
            return eventRepository.getByID(eventID). continueWithTask(eventTask -> {
                Event event = eventTask.getResult();
                if (event == null) {
                    return Tasks.forException(new IllegalArgumentException("Event not found"));
                }

                Integer waitlistCapacity = event.getWaitlistCapacity();
                if (waitlistCapacity != null && waitlistCapacity > 0) {
                    return waitlistRepository.countByEventAndStatus(eventID, EntryStatus.WAITING).continueWithTask(countTask -> {
                        Long currentCount = countTask.getResult();
                        if (currentCount >= waitlistCapacity) {
                            return Tasks. forException(new IllegalStateException("Waitlist is full"));
                        }

                        String entryID = UUID.randomUUID().toString();
                        WaitingListEntry entry = new WaitingListEntry(entryID, eventID, userID);
                        entry.markAsJoined();
                        return waitlistRepository.create(entry);
                    });
                }

                String entryID = UUID.randomUUID().toString();
                WaitingListEntry entry = new WaitingListEntry(entryID, eventID, userID);
                entry.markAsJoined();
                return waitlistRepository.create(entry);
            });
        });
    }

    /**
     * Removes a user from an event's waitlist.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @return task that completes when user is removed
     */
    public Task<Void> leave(String userID, String eventID) {
        return waitlistRepository.getByID(eventID, userID).continueWithTask(task -> {
            WaitingListEntry entry = task.getResult();
            if (entry == null) {
                return Tasks.forException(new IllegalArgumentException("Not on waitlist"));
            }
            if (entry.hasStatus(EntryStatus.ACCEPTED)) {
                return Tasks.forException(new IllegalStateException("Cannot leave after accepting"));
            }

            entry.markAsCancelled();

            return waitlistRepository.delete(eventID, userID);
        });
    }

    /**
     * Sends an invitation to a user on the waitlist.
     *
     * @param organizerID ID of the organizer
     * @param eventID ID of the event
     * @param userID ID of the user to invite
     * @return task that completes when invitation is sent
     */
    public Task<Void> invite(String organizerID, String eventID, String userID) {
        return waitlistRepository.getByID(eventID, userID).continueWithTask(task -> {
            WaitingListEntry entry = task.getResult();
            if (entry == null) {
                return Tasks.forException(new IllegalArgumentException("Entry not found"));
            }
            entry.setStatus(EntryStatus.INVITED);
            entry.setInvitedAt(Timestamp.now());
            return waitlistRepository.update(entry);
        });
    }

    /**
     * Records a user accepting an event invitation.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @return task that completes when acceptance is recorded
     */
    public Task<Void> acceptInvite(String userID, String eventID) {
        return waitlistRepository.getByID(eventID, userID).continueWithTask(task -> {
            WaitingListEntry entry = task.getResult();
            if (entry == null) {
                return Tasks.forException(new IllegalArgumentException("Not on waitlist"));
            }
            if (!entry.hasStatus(EntryStatus.INVITED)) {
                return Tasks.forException(new IllegalStateException("Invite not pending"));
            }

            return eventRepository.getByID(eventID).continueWithTask(eventTask -> {
                Event event = eventTask.getResult();
                if (event == null) {
                    return Tasks.forException(new IllegalArgumentException("Event not found"));
                }

                Integer maxCap = event.getMaxCapacity();
                Integer currCap = event.getCurrentCapacity();

                if (maxCap == null) {
                    return Tasks.forException(new IllegalStateException("Event max capacity not set"));
                }
                if (currCap == null) {
                    currCap = 0;
                }
                if (currCap >= maxCap) {
                    return Tasks.forException(new IllegalStateException("Event is full"));
                }

                // update event capacity
                event.setCurrentCapacity(currCap + 1);

                // update entry
                entry.markAsAccepted();

                return Tasks.whenAll(eventRepository.update(event), waitlistRepository.update(entry));
            });
        });
    }

    /**
     * Records a user declining an event invitation.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @return task that completes when decline is recorded
     */
    public Task<Void> declineInvite(String userID, String eventID) {
        return waitlistRepository.getByID(eventID, userID).continueWithTask(task -> {
            WaitingListEntry entry = task.getResult();
            if (entry == null) {
                return Tasks.forException(new IllegalArgumentException("Not on waitlist"));
            }
            if (!entry.hasStatus(EntryStatus.INVITED)) {
                return Tasks.forException(new IllegalStateException("Invite not pending"));
            }

            entry.markAsDeclined();
            return waitlistRepository.update(entry);
        });
    }

//    /**
//     * Gets all waitlist entries for an event.
//     *
//     * @param eventID ID of the event
//     * @return task containing list of waitlist entries
//     */
//    public Task<List<WaitingListEntry>> getWaitlistEntries(String eventID) {
//        return waitlistRepository.listByEvent(eventID);
//    }

    /**
     * Gets all waitlist entries for an event (only WAITING status).
     *
     * @param eventID ID of the event
     * @return task containing list of waitlist entries with WAITING status
     */
    public Task<List<WaitingListEntry>> getWaitlistEntries(String eventID) {
        return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.WAITING);
    }

    /**
     * Gets all invited users for an event.
     *
     * @param eventID ID of the event
     * @return task containing list of invited entries
     */
    public Task<List<WaitingListEntry>> getInvitedList(String eventID) {
        return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.INVITED);
    }

    /**
     * Gets all users who accepted invitations for an event.
     *
     * @param eventID ID of the event
     * @return task containing list of accepted entries
     */
    public Task<List<WaitingListEntry>> getAcceptedList(String eventID) {
        return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.ACCEPTED);
    }

    /**
     * Counts the total number of users WAITING on an event's waitlist. (only WAITING status).
     *
     * @param eventID ID of the event
     * @return task containing the count
     */
    public Task<Long> getWaitlistSize(String eventID) {
        return waitlistRepository.countByEventAndStatus(eventID, EntryStatus.WAITING);
    }

    /**
     * Gets all users who declined invitations for an event.
     *
     * @param eventID ID of the event
     * @return task containing list of declined entries
     */
    public Task<List<WaitingListEntry>> getDeclinedList(String eventID) {
        return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.DECLINED);
    }

    /**
     * Gets all users whose invitations were cancelled for an event.
     *
     * @param eventID ID of the event
     * @return task containing list of cancelled entries
     */
    public Task<List<WaitingListEntry>> getCancelledList(String eventID) {
        return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.CANCELLED);
    }

    /**
     * Counts waitlist entries grouped by status.
     *
     * @param eventID ID of the event
     * @return task containing map of status to count
     */
    public Task<Map<EntryStatus, Long>> getWaitlistCounts(String eventID) {
        return waitlistRepository.countsByEventGrouped(eventID);
    }

    /**
     * Gets all waitlist entries for a specific user across all events.
     *
     * @param userID ID of the user
     * @return task containing list of entries
     */
    public Task<List<WaitingListEntry>> getHistory(String userID) {
        return waitlistRepository.listByUser(userID);
    }

    /**
     * Cancels entrants who did not sign up within the specified time.
     *
     * @param eventID ID of the event
     * @param deadline Timestamp deadline for signup
     * @return task that completes when non-registered entrants are cancelled
     */
    public Task<Void> cancelNonRegistered(String eventID, long deadline) {
        return waitlistRepository.listByEventAndStatus(eventID, EntryStatus.INVITED)
                .continueWithTask(entriesTask -> {
                    if (!entriesTask.isSuccessful()) {
                        throw entriesTask.getException();
                    }

                    List<WaitingListEntry> invitedEntries = entriesTask.getResult();
                    List<Task<Void>> cancelTasks = new ArrayList<>();

                    long currentTime = System.currentTimeMillis();
                    if (currentTime < deadline) {
                        return Tasks.forException(new IllegalArgumentException("Deadline has not passed yet"));
                    }

                    for (WaitingListEntry entry : invitedEntries) {
                        // If they were invited but haven't accepted by deadline, cancel them
                        if (entry.getAcceptedAt() == null) {
                            entry.markAsCancelled();
                            cancelTasks.add(waitlistRepository.update(entry));
                        }
                    }

                    return Tasks.whenAll(cancelTasks);
                });
    }

    /**
     * Adds a user to an event's waitlist with location tracking.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @param location GeoPoint of user's location (can be null)
     * @return task that completes when user is added
     */
    public Task<Void> joinWithLocation(String userID, String eventID, com.google.firebase.firestore.GeoPoint location) {
        return waitlistRepository.getByID(eventID, userID).continueWithTask(task -> {
            WaitingListEntry existing = task.getResult();
            if (existing != null) {
                return Tasks.forException(new IllegalArgumentException("Already on waitlist"));
            }

                    // Check if event's waitlist has reached capacity
                    return eventRepository.getByID(eventID). continueWithTask(eventTask -> {
                        Event event = eventTask.getResult();
                        if (event == null) {
                            return Tasks.forException(new IllegalArgumentException("Event not found"));
                        }

                        Integer waitlistCapacity = event.getWaitlistCapacity();
                        if (waitlistCapacity != null && waitlistCapacity > 0) {
                            return waitlistRepository.countByEventAndStatus(eventID, EntryStatus.WAITING)
                                    .continueWithTask(countTask -> {
                                        Long currentCount = countTask.getResult();
                                        if (currentCount >= waitlistCapacity) {
                                            return Tasks. forException(new IllegalStateException("Waitlist is full"));
                                        }

                                        // Create and add the new entry with location
                                        String entryID = UUID.randomUUID().toString();
                                        WaitingListEntry entry = new WaitingListEntry(entryID, eventID, userID);
                                        entry.markAsJoined(location);
                                        return waitlistRepository.create(entry);
                                    });
                        }

            String entryID = UUID.randomUUID().toString();
            WaitingListEntry entry = new WaitingListEntry(entryID, eventID, userID);
            entry.markAsJoined(location);

            return waitlistRepository.create(entry);
        });
    });
        }

    /**
     * Gets all waitlist entries with location data for an event.
     *
     * @param eventID ID of the event
     * @return task containing list of waitlist entries with location data
     */
    public Task<List<WaitingListEntry>> getWaitlistEntriesWithLocation(String eventID) {
        return waitlistRepository.listByEvent(eventID);
    }

    /**
     * Sets geolocation requirement for an event.
     *
     * @param eventID ID of the event
     * @param required Whether geolocation is required
     * @return task that completes when requirement is set
     */
    public Task<Void> setGeolocationRequirement(String eventID, boolean required) {
        return eventRepository. getByID(eventID).continueWithTask(task -> {
            Event event = task.getResult();
            if (event == null) {
                return Tasks.forException(new IllegalArgumentException("Event not found"));
            }
            event.setRequiresGeolocation(required);
            return eventRepository.update(event);
        });
    }

    public Task<Void> cancelInvite(String userID, String eventID) {
        return waitlistRepository.getByID(eventID, userID). continueWithTask(task -> {
            WaitingListEntry entry = task. getResult();
            if (entry == null) {
                return Tasks.forException(new IllegalArgumentException("Not on waitlist"));
            }
            if (!entry.hasStatus(EntryStatus.INVITED)) {
                return Tasks.forException(new IllegalStateException("User is not invited"));
            }

            entry.markAsCancelled();
            return waitlistRepository.update(entry);
        });
    }

}

