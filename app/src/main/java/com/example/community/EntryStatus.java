package com.example.community;

/**
 * Enum representing the status of a waitlist entry.
 * Tracks user progression from waiting to attending or cancelled.
 */
public enum EntryStatus {
    WAITING, // in waiting list
    INVITED,  // selected by lottery
    ACCEPTED, // accepted invitation
    DECLINED, // declined invitation
    CANCELLED // left waiting list, left attendee list
}
