package com.aurora.climatesync.controller;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.service.CalendarService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    public List<CalendarEvent> getEvents() {
        // For now, returns a placeholder list or empty list
        return calendarService.getUpcomingEvents();
    }
}
