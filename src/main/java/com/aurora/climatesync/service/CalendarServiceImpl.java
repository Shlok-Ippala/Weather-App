package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.repository.CalendarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarServiceImpl implements CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarServiceImpl(CalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    @Override
    public List<CalendarEvent> getUpcomingEvents() {
        return calendarRepository.getUpcomingEvents();
    }

    @Override
    public List<CalendarEvent> getUpcomingEvents(int maxResults) {
        return calendarRepository.getUpcomingEvents(maxResults);
    }

    @Override
    public CalendarEvent addEvent(CalendarEvent event) {
        return calendarRepository.addEvent(event);
    }

    @Override
    public void updateEvent(CalendarEvent event) {
        calendarRepository.updateEvent(event);
    }

    @Override
    public void deleteEvent(String eventId) {
        calendarRepository.deleteEvent(eventId);
    }

    @Override
    public String connect() throws Exception {
        return calendarRepository.connect();
    }

    @Override
    public boolean isConnected() {
        return calendarRepository.isConnected();
    }
}
