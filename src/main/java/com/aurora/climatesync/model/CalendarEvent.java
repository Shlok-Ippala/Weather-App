package com.aurora.climatesync.model;
import java.time.ZonedDateTime;

public class CalendarEvent {
    private String eventID; //event ID from Google API
    private String title; //event title from calendar
    private String description; //event description from calendar
    private ZonedDateTime startTime; // The start date and time, including timezone
    private ZonedDateTime endTime;   // The end date and time, including timezone
    private Location eventLocation;  // The Location object associated with the event
    private String colorId;          // The color ID of the event

    public CalendarEvent(String eventID, String title, String description, ZonedDateTime startTime, ZonedDateTime endTime, Location eventLocation) {
        this(eventID, title, description, startTime, endTime, eventLocation, null);
    }

    public CalendarEvent(String eventID, String title, String description, ZonedDateTime startTime, ZonedDateTime endTime, Location eventLocation, String colorId) {
        this.eventID = eventID;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventLocation = eventLocation;
        this.colorId = colorId;
    }

    public String getEventID() {
        return eventID;
    }

    public String getSummary() {
        return title;
    }

    public String getDescription() {
        return description;
    }

     public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public Location getEventLocation() {
        return eventLocation;
    }

    public String getColorId() {
        return colorId;
    }
}
