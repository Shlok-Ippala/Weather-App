package com.aurora.climatesync.model;

import java.time.ZonedDateTime;

/**
 * Represents a calendar event that has been processed and normalized for the application.
 * This model is decoupled from the raw Google Calendar API data structures.
 */
public class ProcessedEvent {
    /**
     * The unique identifier of the event.
     */
    private String eventID;
    /**
     * The title or summary of the event.
     */
    private String title;
    /**
     * The start time of the event.
     */
    private ZonedDateTime startTime;
    /**
     * The end time of the event.
     */
    private ZonedDateTime endTime;
    /**
     * The location string of the event.
     */
    private String location;

    /**
     * Constructs a new ProcessedEvent.
     *
     * @param eventID   The unique identifier.
     * @param title     The title.
     * @param startTime The start time.
     * @param endTime   The end time.
     * @param location  The location string.
     */
    public ProcessedEvent(String eventID, String title, ZonedDateTime startTime, ZonedDateTime endTime, String location) {
        this.eventID = eventID;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public String getEventID() {
        return eventID;
    }

    public String getTitle() {
        return title;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }
}
