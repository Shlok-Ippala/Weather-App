package com.aurora.climatesync.service;

import com.aurora.climatesync.model.ProcessedEvent;
import com.google.api.services.calendar.model.Event;

import java.util.List;

public interface EventProcessingService {
    /**
     * Processes a list of raw Google Calendar events.
     * Filters out events without location or all-day events.
     * Normalizes the data into ProcessedEvent objects.
     *
     * @param rawEvents List of raw Google Calendar events.
     * @return List of processed events.
     */
    List<ProcessedEvent> processEvents(List<Event> rawEvents);
}
