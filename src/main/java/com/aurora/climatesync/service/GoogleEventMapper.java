package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class GoogleEventMapper {

    public CalendarEvent mapToCalendarEvent(Event event) {
        DateTime start = event.getStart().getDateTime();
        if (start == null) start = event.getStart().getDate();
        DateTime end = event.getEnd().getDateTime();
        if (end == null) end = event.getEnd().getDate();

        ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start.getValue()), ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(end.getValue()), ZoneId.systemDefault());

        String locationStr = event.getLocation();
        if (locationStr == null) locationStr = "Unknown";

        return new CalendarEvent(
                event.getId(),
                event.getSummary(),
                event.getDescription(),
                startTime,
                endTime,
                new Location(locationStr, "Unknown", 0, 0),
                event.getColorId()
        );
    }

    public Event mapToGoogleEvent(CalendarEvent calendarEvent) {
        Event event = new Event();
        updateGoogleEvent(event, calendarEvent);
        return event;
    }

    public void updateGoogleEvent(Event event, CalendarEvent calendarEvent) {
        event.setSummary(calendarEvent.getSummary());
        event.setDescription(calendarEvent.getDescription());

        if (calendarEvent.getColorId() != null) {
            event.setColorId(calendarEvent.getColorId());
        }

        DateTime startDateTime = new DateTime(calendarEvent.getStartTime().toInstant().toEpochMilli());
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone(calendarEvent.getStartTime().getZone().toString());
        event.setStart(start);

        DateTime endDateTime = new DateTime(calendarEvent.getEndTime().toInstant().toEpochMilli());
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone(calendarEvent.getEndTime().getZone().toString());
        event.setEnd(end);

        if (calendarEvent.getEventLocation() != null) {
            event.setLocation(calendarEvent.getEventLocation().toString());
        }
    }
}
