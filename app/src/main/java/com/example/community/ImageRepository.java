package com.example.community;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Repository for managing image uploads and storage.
 * Handles Firebase Storage and Firestore operations for images.
 */
public class ImageRepository {
    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    private final CollectionReference firestoreRef;

    public ImageRepository() {
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
        this.firestoreRef = FirebaseFirestore.getInstance().collection("images");
    }

    /**
     * Generic upload method that handles both Storage and Firestore
     *
     * @param data        Image bytes to upload
     * @param storagePath Path in Firebase Storage
     * @param uploadedBy  User ID who uploaded the image
     * @return Task that resolves to Image object with all metadata
     */
    public Task<Image> upload(byte[] data, String storagePath, String uploadedBy) {
        String imageID = UUID.randomUUID().toString();

        return uploadToStorage(data, storagePath).onSuccessTask(downloadUrl -> {
            Image image = new Image();
            image.setImageID(imageID);
            image.setStoragePath(storagePath);
            image.setImageURL(downloadUrl);
            image.setUploadedBy(uploadedBy);
            image.setUploadedAt(Timestamp.now());

            return firestoreRef.document(imageID).set(image).continueWith(task -> {
                if (!task.isSuccessful()) {
                    // Rollback: delete from Storage if Firestore fails
                    deleteFromStorage(storagePath);
                    throw task.getException();
                }
                return image;
            });
        });
    }

    /**
     * Deletes an image from both Firestore and Storage
     *
     * @param imageID The image document ID
     * @return Task that completes when deletion is done
     */
    public Task<Void> delete(String imageID) {
        return firestoreRef.document(imageID).get().onSuccessTask(snapshot -> {
            if (!snapshot.exists()) {
                return Tasks.forResult(null);
            }

            Image image = snapshot.toObject(Image.class);
            if (image == null) {
                return Tasks.forResult(null);
            }

            return deleteFromStorage(image.getStoragePath()).continueWithTask(task -> {
                return firestoreRef.document(imageID).delete();
            });
        });
    }

    /**
     * Gets an image by its ID
     *
     * @param imageID The image document ID
     * @return Task that resolves to Image object or null if not found
     */
    public Task<Image> getByID(String imageID) {
        return firestoreRef.document(imageID).get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            DocumentSnapshot snapshot = task.getResult();
            return snapshot.exists() ? snapshot.toObject(Image.class) : null;
        });
    }

    /**
     * Gets all images (for admin browsing)
     *
     * @return Task that resolves to list of all images
     */
    public Task<List<Image>> getAll() {
        return firestoreRef.get().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            List<Image> images = new ArrayList<>();
            for (DocumentSnapshot doc : task.getResult()) {
                Image image = doc.toObject(Image.class);
                if (image != null) {
                    images.add(image);
                }
            }
            return images;
        });
    }

    /**
     * Low-level method to upload bytes to Firebase Storage
     *
     * @param data        The image bytes
     * @param storagePath The storage path
     * @return Task that resolves to download URL
     */
    private Task<String> uploadToStorage(byte[] data, String storagePath) {
        StorageReference imageRef = storageRef.child(storagePath);
        UploadTask uploadTask = imageRef.putBytes(data);

        return uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            Uri downloadUri = task.getResult();
            return downloadUri.toString();
        });
    }

    /**
     * Low-level method to delete from Firebase Storage
     *
     * @param storagePath The storage path
     * @return Task that completes when deletion is done
     */
    private Task<Void> deleteFromStorage(String storagePath) {
        StorageReference imageRef = storageRef.child(storagePath);

        return imageRef.delete().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                // ignore file not found errors
                if (e != null && e.getMessage() != null && e.getMessage().contains("does not exist")) {
                    return Tasks.forResult(null);
                }
                throw e;
            }
            return task;
        });
    }

    /**
     * Helper method for UI to get poster URL from Event
     *
     * @param event The event object
     * @return The poster URL or null if none exists
     */
    public String getPosterURLFromEvent(Event event) {
        return event.getPosterImageURL();
    }

    /**
     * Gets download URL for a storage path (if needed for direct access)
     *
     * @param storagePath The path in Firebase Storage
     * @return Task that resolves to the download URL
     */
    public Task<String> getDownloadURL(String storagePath) {
        StorageReference imageRef = storageRef.child(storagePath);
        return imageRef.getDownloadUrl().continueWith(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return task.getResult().toString();
        });
    }
}
