package dev.devrunner.outbox.model;

/**
 * Processing status of outbox events
 */
public enum EventStatus {
    WAIT,
    PROCESSING,
    COMPLETED,
    FAILED
}
