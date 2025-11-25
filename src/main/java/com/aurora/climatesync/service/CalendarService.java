package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import java.util.List;

public interface CalendarService {
    List<CalendarEvent> getUpcomingEvents();
    void addEvent(CalendarEvent event);
    void updateEvent(CalendarEvent event);
    void deleteEvent(String eventId);
    String connect() throws Exception;
    boolean isConnected();
}
