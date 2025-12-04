package com.example.community;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

/**
 * Represents a single entry on an event's waiting list.
 *
 * <p>The WaitingListEntry records which user joined which event's waiting list,
 * its current status (waiting, invited, accepted, declined, cancelled), and
 * timestamps for when state transitions happened.
 */
public class WaitingListEntry {
    private String entryID;
    private String eventID;
    private String userID;
    private  EntryStatus status = EntryStatus.WAITING;

    private Timestamp joinedAt;
    private Timestamp invitedAt;
    private Timestamp acceptedAt;
    private Timestamp declinedAt;
    private Timestamp cancelledAt;

    private GeoPoint joinLocation;

    /**
     * Default no-arg constructor required for some Firebase.
     */
    public WaitingListEntry() { }

    /**
     * Constructor for a new waiting list entry.
     *
     * @param entryID unique id for this waiting list entry
     * @param eventID id of the event for which the entry was created
     * @param userID id of the user who joined the waiting list
     */
    public WaitingListEntry(String entryID, String eventID, String userID) {
        this.entryID = entryID;
        this.eventID = eventID;
        this.userID = userID;
    }

    /**
     * Returns the unique id for this waiting list entry.
     *
     * @return the entry id
     */
    public String getEntryID() {
        return entryID;
    }
    /**
     * Sets the unique id for this waiting list entry.
     *
     * @param entryID the entry id to set
     */
    public void setEntryID(String entryID) {
        this.entryID = entryID;
    }

    /**
     * Returns the event id this entry belongs to.
     *
     * @return the event id
     */
    public String getEventID() {
        return eventID;
    }
    /**
     * Sets the event id this entry belongs to.
     *
     * @param eventID the event id to set
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * Returns the user id who created the entry.
     *
     * @return the user id
     */
    public String getUserID() {
        return userID;
    }
    /**
     * Sets the user id who created the entry.
     *
     * @param userID the user id to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Returns the current status for this entry.
     *
     * @return the entry status
     */
    public EntryStatus getStatus() {
        return status;
    }
    /**
     * Sets the current status for this entry.
     *
     * @param status the entry status to set
     */
    public void setStatus(EntryStatus status) {
        this.status = status;
    }

    /**
     * Returns the timestamp when the user joined the waiting list.
     *
     * @return timestamp of join, or null if unset
     */
    public Timestamp getJoinedAt() {
        return joinedAt;
    }
    /**
     * Sets the timestamp when the user joined the waiting list.
     *
     * @param joinedAt the join timestamp to set
     */
    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Returns the timestamp when the user was invited (if applicable).
     *
     * @return invite timestamp or null
     */
    public Timestamp getInvitedAt() {
        return invitedAt;
    }
    /**
     * Sets the timestamp when the user was invited.
     *
     * @param invitedAt the invite timestamp to set
     */
    public void setInvitedAt(Timestamp invitedAt) {
        this.invitedAt = invitedAt;
    }

    /**
     * Returns the timestamp when the user accepted an invitation.
     *
     * @return acceptance timestamp or null
     */
    public Timestamp getAcceptedAt() {
        return acceptedAt;
    }
    /**
     * Sets the timestamp when the user accepted an invitation.
     *
     * @param acceptedAt the acceptance timestamp to set
     */
    public void setAcceptedAt(Timestamp acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    /**
     * Returns the timestamp when the user cancelled or left.
     *
     * @return cancellation timestamp or null
     */
    public Timestamp getCancelledAt() {
        return cancelledAt;
    }
    /**
     * Sets the timestamp when the user cancelled or left.
     *
     * @param cancelledAt the cancellation timestamp to set
     */
    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    /**
     * Returns the timestamp when the user declined an invitation.
     *
     * @return decline timestamp or null
     */
    public Timestamp getDeclinedAt() {
        return declinedAt;
    }
    /**
     * Sets the timestamp when the user declined an invitation.
     *
     * @param declinedAt the decline timestamp to set
     */
    public void setDeclinedAt(Timestamp declinedAt) {
        this.declinedAt = declinedAt;
    }

    public GeoPoint getJoinLocation() {
        return joinLocation;
    }

    public void setJoinLocation(GeoPoint joinLocation) {
        this.joinLocation = joinLocation;
    }

    /**
     * Returns whether the entry currently has the provided status.
     *
     * @param status status to compare
     * @return true if the current status equals the provided status
     */
    public boolean hasStatus(EntryStatus status) {
        return this.status == status;
    }

    /**
     * Mark the entry as joined and record the join timestamp.
     */
    public void markAsJoined() {
        this.status = EntryStatus.WAITING;
        this.joinedAt = Timestamp.now();
    }

    /**
     * Mark the entry as joined and record the join timestamp and location.
     */
    public void markAsJoined(GeoPoint location) {
        this.status = EntryStatus.WAITING;
        this.joinedAt = Timestamp.now();
        this.joinLocation = location;
    }

    /**
     * Mark the entry as accepted and record the acceptance timestamp.
     */
    public void markAsAccepted() {
        this.status = EntryStatus.ACCEPTED;
        this.acceptedAt = Timestamp.now();
    }

    /**
     * Mark the entry as declined and record the decline timestamp.
     */
    public void markAsDeclined() {
        this.status = EntryStatus.DECLINED;
        this.declinedAt = Timestamp.now();
    }

    /**
     * Mark the entry as cancelled and record the cancellation timestamp.
     */
    public void markAsCancelled() {
        this.status = EntryStatus.CANCELLED;
        this.cancelledAt = Timestamp.now();
    }
    public void markAsInvited() {
        this.status = EntryStatus.INVITED;
        this.invitedAt = Timestamp.now();
    }

}
