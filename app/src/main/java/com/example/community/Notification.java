package com.example.community;

import com.google.firebase.firestore.DocumentId;

import java.time.LocalDateTime;

/**
 * Represents a notification sent to a user.
 * Contains message, type, and related event information.
 */
public class Notification {

    @DocumentId
    private String notificationID;
    private String recipientID;
    private String eventID;
    private long issueDate;
    private String title;
    private String message;
    private NotificationType type;
    private boolean dismissed;

    public String eventTitle;

    /**
     * Default constructor required for Firebase.
     */
    public Notification() {
    }

    public String getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(String notificationID) {
        this.notificationID = notificationID;
    }

    public String getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public long getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(long issueDate) {
        this.issueDate = issueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public void setDismissed(boolean dismissed) {
        this.dismissed = dismissed;
    }
    public boolean isDismissed() {
        return dismissed;
    }

    public void markAsDismissed() {
        this.dismissed = true;
    }
}
