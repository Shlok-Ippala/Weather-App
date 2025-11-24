package com.aurora.climatesync.service;

import com.aurora.climatesync.model.ProcessedEvent;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the EventProcessingService.
 * Responsible for filtering and normalizing raw Google Calendar events.
 */
@Service
public class EventProcessingServiceImpl implements EventProcessingService {

    @Override
    public List<ProcessedEvent> processEvents(List<Event> rawEvents) {
        List<ProcessedEvent> processedEvents = new ArrayList<>();

        if (rawEvents == null) {
            return processedEvents;
        }

        for (Event event : rawEvents) {
            if (isValidEvent(event)) {
                processedEvents.add(convertEvent(event));
            }
        }

        return processedEvents;
    }

    private boolean isValidEvent(Event event) {
        // Filter out events without location
        if (event.getLocation() == null || event.getLocation().trim().isEmpty()) {
            return false;
        }

        // Filter out all-day events
        // All-day events have a date but no dateTime in the start field
        EventDateTime start = event.getStart();
        if (start == null) {
            return false; // Should not happen for valid events, but good to check
        }
        
        // If dateTime is null, it's likely an all-day event (which only has date)
        if (start.getDateTime() == null) {
            return false;
        }

        return true;
    }

    private ProcessedEvent convertEvent(Event event) {
        String eventId = event.getId();
        String title = event.getSummary();
        String location = cleanLocation(event.getLocation());
        
        ZonedDateTime startTime = convertToZonedDateTime(event.getStart());
        ZonedDateTime endTime = convertToZonedDateTime(event.getEnd());

        return new ProcessedEvent(eventId, title, startTime, endTime, location);
    }

    private String cleanLocation(String location) {
        if (location == null) {
            return "";
        }
        // Basic cleaning: trim whitespace
        // Future expansion: Remove room numbers, parse address, etc.
        return location.trim();
    }

    private ZonedDateTime convertToZonedDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return null;
        }

        DateTime dateTime = eventDateTime.getDateTime();
        if (dateTime == null) {
            // Should be caught by isValidEvent, but handle gracefully
            return null; 
        }

        long value = dateTime.getValue();
        // Google DateTime can have a time zone offset or be UTC
        // We can convert to Instant and then ZonedDateTime
        // If eventDateTime has a timeZone field, use it.
        
        ZoneId zoneId = ZoneId.systemDefault();
        if (eventDateTime.getTimeZone() != null) {
            try {
                zoneId = ZoneId.of(eventDateTime.getTimeZone());
            } catch (Exception e) {
                // Fallback to system default or UTC if zone is invalid
                // System.out.println("Invalid timezone: " + eventDateTime.getTimeZone());
            }
        } else {
             // DateTime itself might have an offset
             // But converting from epoch millis is safer if we just want the instant
        }

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value), zoneId);
    }
}
