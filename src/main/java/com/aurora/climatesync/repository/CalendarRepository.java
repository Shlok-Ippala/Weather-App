package com.aurora.climatesync.repository;

import com.aurora.climatesync.model.CalendarEvent;
import java.util.List;

public interface CalendarRepository {
    List<CalendarEvent> getUpcomingEvents();
    List<CalendarEvent> getUpcomingEvents(int maxResults);
    CalendarEvent addEvent(CalendarEvent event);
    void updateEvent(CalendarEvent event);
    void deleteEvent(String eventId);
    String connect() throws Exception;
    boolean isConnected();
}
