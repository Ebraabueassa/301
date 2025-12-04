package com.example.community;

/**
 * Enum representing the publication status of an event.
 * Tracks event lifecycle from draft to cancelled.
 */
public enum EventStatus {
    DRAFT,
    OPEN,
    CLOSED,
    CANCELLED
}