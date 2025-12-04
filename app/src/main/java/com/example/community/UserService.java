package com.example.community;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service layer for user operations.
 * Handles authentication, user management, and profile updates.
 */
public class UserService {
    private static final String TAG = "UserService";
    private UserRepository userRepository;
    private EventRepository eventRepository;
    private WaitlistRepository waitlistRepository;
    private NotificationRepository notificationRepository;
    private FirebaseAuth firebaseAuth;

    /**
     * Creates a new UserService instance.
     * Initializes required repositories and Firebase Auth.
     */
    public UserService() {
        userRepository = new UserRepository();
        eventRepository = new EventRepository();
        waitlistRepository = new WaitlistRepository();
        notificationRepository = new NotificationRepository();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Authenticates the current device using Firebase anonymous auth.
     *
     * @return task containing the authenticated Firebase user
     */
    public Task<FirebaseUser> authenticateByDevice() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d(TAG, "User already authenticated with UID: " + currentUser.getUid());
            return Tasks.forResult(currentUser);
        }

        Log.d(TAG, "Starting authentication process");
        return firebaseAuth.signInAnonymously()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG + "(authenticateByDevice)", "Anonymous auth failed", task.getException());
                        throw task.getException();
                    }
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    Log.d(TAG + "(authenticateByDevice)", "User authenticated with UID: " + user.getUid());
                    return user;
                });
    }

    /**
     * Authenticates device and creates user if needed for splash screen.
     *
     * @return task containing the user object
     */
    public Task<User> splashScreenDeviceAuthentication() {
        return authenticateByDevice()
                .continueWithTask(authTask -> {
                    FirebaseUser firebaseUser = authTask.getResult();
                    String deviceUid = firebaseUser.getUid();

                    return userRepository.getByDeviceToken(deviceUid)
                            .continueWithTask(lookupTask -> {
                                User existing = lookupTask.getResult();
                                if (existing != null) {
                                    return Tasks.forResult(existing);
                                }
                                return createUser(firebaseUser);
                            });
                });
    }

    /**
     * Creates a new user from a Firebase authenticated user.
     *
     * @param firebaseUser authenticated Firebase user
     * @return task containing the created user
     */

    public Task<User> createUser(FirebaseUser firebaseUser) {
        String deviceUid = firebaseUser.getUid();

        User u = new User();
        u.setUserID(UUID.randomUUID().toString());
        u.setDeviceToken(deviceUid);
        // role defaults to ENTRANT, optional fields remain null

        return userRepository.create(u)
                .continueWithTask(v -> Tasks.forResult(u));
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userID ID of the user
     * @return task containing the user
     */
    public Task<User> getByUserID(String userID) {
        return userRepository.getByUserID(userID);
    }

    /**
     * Retrieves a user by their device token.
     *
     * @param deviceToken device token
     * @return task containing the user
     */
    public Task<User> getByDeviceToken(String deviceToken) {
        return userRepository.getByDeviceToken(deviceToken);
    }

    /**
     * Gets the current device's authentication token.
     *
     * @return device token string
     */
    public String getDeviceToken() {
        return firebaseAuth.getCurrentUser().getUid();
    }

    /**
     * Gets a user ID from their device token.
     *
     * @param deviceToken device token
     * @return task containing the user ID
     */
    public Task<String> getUserIDByDeviceToken (String deviceToken) {
        return userRepository.getByDeviceToken(deviceToken)
                .continueWithTask(lookupTask -> {
                    User existing = lookupTask.getResult();
                    if (existing != null) {
                        return Tasks.forResult(existing.getUserID());
                    }
                    return Tasks.forException(new IllegalStateException("User not found: " + deviceToken));
                });
    }

    /**
     * Retrieves all users in the system.
     *
     * @return task containing list of all users
     */
    public Task<java.util.List<User>> getAllUsers() {
        return userRepository.getAll();
    }

    /**
     * Updates an existing user's information.
     *
     * @param user user with updated data
     * @return task that completes when update finishes
     */
    public Task<Void> updateUser(User user) {
        return userRepository.update(user);
    }

    /**
     * Deletes a user from the system.
     *
     * @param userID ID of the user to delete
     * @return task that completes when deletion finishes
     */
    public Task<Void> deleteUser(String userID) { return deleteUserCascade(userID);}

    /**
     * Changes a user's role in the system.
     *
     * @param userID ID of the user
     * @param role new role to assign
     * @return task that completes when role is updated
     */
    public Task<Void> setRole(String userID, Role role) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User user = t.getResult();
                    if (user == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    user.setRole(role);
                    return userRepository.update(user);
                });
    }

    /**
     * Updates a user's username.
     *
     * @param userID ID of the user
     * @param username new username
     * @return task that completes when username is updated
     */
    public Task<Void> setUsername(String userID, String username) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User user = t.getResult();
                    if (user == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    user.setUsername(username);
                    return userRepository.update(user);
                });
    }

    /**
     * Updates a user's email address.
     *
     * @param userID ID of the user
     * @param email new email address
     * @return task that completes when email is updated
     */
    public Task<Void> setEmail(String userID, String email) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User user = t.getResult();
                    if (user == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    user.setEmail(email);
                    return userRepository.update(user);
                });
    }

    /**
     * Updates a user's phone number.
     *
     * @param userID ID of the user
     * @param phone new phone number
     * @return task that completes when phone is updated
     */
    public Task<Void> setPhoneNumber(String userID, String phone) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User user = t.getResult();
                    if (user == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    user.setPhoneNumber(phone);
                    return userRepository.update(user);
                });
    }

    /**
     * Turns on notifications for a user.
     *
     * @param userID ID of the user
     * @return task that completes when setting is updated
     */
    public Task<Void> enableNotifications(String userID) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User user = t.getResult();
                    if (user == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    user.enableNotifications();
                    return userRepository.update(user);
                });
    }

    /**
     * Turns off notifications for a user.
     *
     * @param userID ID of the user
     * @return task that completes when setting is updated
     */
    public Task<Void> disableNotifications(String userID) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User user = t.getResult();
                    if (user == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    user.disableNotifications();
                    return userRepository.update(user);
                });
    }

    /**
     * Adds an event to a user's list of created events.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @return task that completes when list is updated
     */
    public Task<Void> addEventCreated(String userID, String eventID) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User u = t.getResult();
                    if (u == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    if (u.hasEventCreated(eventID)) {
                        return Tasks
                                .forException(new IllegalArgumentException("Event already recorded as created"));
                    }
                    u.addEventCreated(eventID);
                    return userRepository.update(u);
                });
    }

    /**
     * Removes an event from a user's list of created events.
     *
     * @param userID ID of the user
     * @param eventID ID of the event
     * @return task that completes when list is updated
     */
    public Task<Void> removeEventCreated(String userID, String eventID) {
        return userRepository.getByUserID(userID)
                .continueWithTask(t -> {
                    User u = t.getResult();
                    if (u == null) return Tasks
                            .forException(new IllegalStateException("User not found: " + userID));
                    if (!u.hasEventCreated(eventID)) {
                        return Tasks
                                .forException(new IllegalArgumentException("Event not in eventsCreatedIDs"));
                    }
                    u.removeEventCreated(eventID);
                    return userRepository.update(u);
                });
    }

    /**
     * Gets all events created by a user.
     *
     * @param userID ID of the user
     * @return task containing list of event IDs
     */
    public Task<List<String>> listEventsCreated(String userID) {
        return userRepository.getByUserID(userID)
                .continueWith(t -> {
                    User u = t.getResult();
                    if (u == null) return new ArrayList<String>();
                    // return live list per your convention; callers should avoid mutating it directly
                    return u.getEventsCreatedIDs();
                });
    }
    
    // Add to existing UserService class
    /**
     * Gets registration history for a user.
     *
     * @param userID ID of the user
     * @return task containing list of event IDs in registration history
     */
    public Task<List<String>> getRegistrationHistory(String userID) {
        return userRepository.getByUserID(userID).continueWith(task -> {
            User user = task.getResult();
            if (user == null) {
                throw new IllegalArgumentException("User not found");
            }
            return new ArrayList<>(user.getRegistrationHistoryIDs());
        });
    }

    /**
     * Completely deletes a user and all related data (cascade deletion).
     *
     * Deletes:
     * - User from all event waitlists
     * - All events created by the user (which cascades to their related data)
     * - All notifications sent to the user
     * - The user document itself
     *
     * @param userID ID of the user to delete
     * @return task that completes when all cascading deletions are done
     */
    public Task<Void> deleteUserCascade(String userID) {
        Log.d(TAG, "Starting cascade deletion for user: " + userID);

        return userRepository.getByUserID(userID).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Failed to get user", task.getException());
                return Tasks.forException(task.getException());
            }

            User user = task.getResult();
            if (user == null) {
                Log.e(TAG, "User not found: " + userID);
                return Tasks.forException(new IllegalArgumentException("User not found"));
            }

            Log.d(TAG, "Starting cleanup");
            List<Task<Void>> allTasks = new ArrayList<>();

            // remove user from all waitlists they're in
            Log.d(TAG, "Cleaning up waitlists");
            Task<Void> waitlistCleanup = waitlistRepository.listByUser(userID)
                    .continueWithTask(wTask -> {
                        if (!wTask.isSuccessful()) {
                            Log.e(TAG, "Failed to list waitlists", wTask.getException());
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
                            // delete waitlist entry
                            Task<Void> deleteEntry = waitlistRepository.delete(entry.getEventID(), userID)
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete waitlist entry", e));
                            tasks.add(deleteEntry);

                            // update event counts if they were accepted
                            if (entry.getStatus() == EntryStatus.ACCEPTED) {
                                Task<Void> updateEvent = eventRepository.getByID(entry.getEventID())
                                        .continueWithTask(eTask -> {
                                            if (!eTask.isSuccessful()) {
                                                Log.e(TAG, "Failed to get event", eTask.getException());
                                                return Tasks.forResult(null);
                                            }

                                            Event event = eTask.getResult();
                                            if (event != null && event.getCurrentCapacity() != null && event.getCurrentCapacity() > 0) {
                                                event.setCurrentCapacity(event.getCurrentCapacity() - 1);
                                                return eventRepository.update(event);
                                            }
                                            return Tasks.forResult(null);
                                        })
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update event capacity", e));
                                tasks.add(updateEvent);
                            }
                        }

                        return Tasks.whenAll(tasks);
                    })
                    .addOnSuccessListener(v -> Log.d(TAG, "Waitlist cleanup completed"))
                    .addOnFailureListener(e -> Log.e(TAG, "Waitlist cleanup failed", e));

            allTasks.add(waitlistCleanup);

            // delete all events created by this user (uses cascade delete)
            Log.d(TAG, "Deleting created events");
            List<String> eventsToDelete = new ArrayList<>(user.getEventsCreatedIDs());
            Log.d(TAG, "Found " + eventsToDelete.size() + " events to delete");

            EventService eventService = new EventService();
            for (String eventID : eventsToDelete) {
                Task<Void> deleteEventTask = eventService.deleteEvent(eventID)
                        .addOnSuccessListener(v -> Log.d(TAG, "Deleted event: " + eventID))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to delete event: " + eventID, e));
                allTasks.add(deleteEventTask);
            }

            // delete all notifications sent to this user
            Log.d(TAG, "Deleting notifications");
            Task<Void> notificationCleanup = notificationRepository.deleteAllForUser(userID)
                    .addOnSuccessListener(v -> Log.d(TAG, "Notifications deleted"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete notifications", e));
            allTasks.add(notificationCleanup);

            // delete the user document itself
            Log.d(TAG, "Deleting user document");
            Task<Void> userDeletion = userRepository.delete(userID)
                    .addOnSuccessListener(v -> Log.d(TAG, "User document deleted"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to delete user document", e));
            allTasks.add(userDeletion);

            // return combined task
            return Tasks.whenAll(allTasks)
                    .addOnSuccessListener(v -> Log.d(TAG, "User cascade deletion completed successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "User cascade deletion failed", e));
        });
    }

}
