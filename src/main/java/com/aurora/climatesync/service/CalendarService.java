package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import java.util.List;

public interface CalendarService {
    List<CalendarEvent> getUpcomingEvents();
    void addEvent(CalendarEvent event);
    String connect() throws Exception;
}
