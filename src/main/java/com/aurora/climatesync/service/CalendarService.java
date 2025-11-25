package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import java.util.List;

public interface CalendarService {
    List<CalendarEvent> getUpcomingEvents();
    List<CalendarEvent> getUpcomingEvents(int maxResults);
    CalendarEvent addEvent(CalendarEvent event);
    void updateEvent(CalendarEvent event);
    void deleteEvent(String eventId);
    String connect() throws Exception;
    boolean isConnected();
}
