package com.example.community;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing user data in Firestore.
 * Handles all database operations for users.
 */
public class UserRepository {

    private final String TAG = "UserRepository";

    private FirebaseFirestore db;
    private CollectionReference usersRef;

    /**
     * Creates a new UserRepository instance.
     * Initializes Firestore connection.
     */
    public UserRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.usersRef = db.collection("users");
    }

    /**
     * Saves a new user to the database.
     *
     * @param user user to create
     * @return task that completes when creation finishes
     */
    public Task<Void> create(User user) {
        return usersRef.document(user.getUserID()).set(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userID ID of the user
     * @return task containing the user or null if not found
     */
    public Task<User> getByUserID(String userID) {
        return usersRef.document(userID).get().continueWith(task -> {
            DocumentSnapshot snapshot = task.getResult();
            return snapshot.exists() ? snapshot.toObject(User.class) : null;
        });
    }

    /**
     * Updates an existing user in the database.
     *
     * @param user user with updated data
     * @return task that completes when update finishes
     */
    public Task<Void> update(User user) {
        return usersRef.document(user.getUserID()).set(user);
    }

    /**
     * Deletes a user from the database.
     *
     * @param userID ID of the user to delete
     * @return task that completes when deletion finishes
     */
    public Task<Void> delete(String userID) {
        return usersRef.document(userID).delete();
    }

    /**
     * Retrieves all users from the database.
     *
     * @return task containing list of all users
     */
    public Task<List<User>> getAll() {
        return usersRef.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<User> users = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    users.add(user);
                }
            }
            return users;
        });
    }

    /**
     * Finds a user by their device token.
     *
     * @param deviceToken device token to search for
     * @return task containing the user or null if not found
     */
    public Task<User> getByDeviceToken(String deviceToken) {
        return usersRef.whereEqualTo("deviceToken", deviceToken)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    QuerySnapshot qs = task.getResult();
                    if (qs == null || qs.isEmpty()) {
                        return null;
                    }
                    DocumentSnapshot doc = qs.getDocuments().get(0);
                    return doc.toObject(User.class);
                });
    }
}
