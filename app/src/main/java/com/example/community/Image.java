package com.example.community;

import com.google.firebase.Timestamp;

/**
 * Represents an uploaded image with metadata.
 * Stores storage path, URL, and upload information.
 */
public class Image {
    private String imageID;
    private String storagePath;
    private String imageURL;
    private String uploadedBy;
    private Timestamp uploadedAt;

    /**
     * Default constructor required for Firebase.
     */
    public Image() {}

    public String getImageID() {
        return imageID;
    }

    public void setImageID(String imageID) {
        this.imageID = imageID;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
