package com.aurora.climatesync.controller;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final CalendarService calendarService;
    private final WeatherService weatherService;

    public DashboardController(CalendarService calendarService, WeatherService weatherService) {
        this.calendarService = calendarService;
        this.weatherService = weatherService;
    }

    @GetMapping("/dashboard")
    public List<DashboardEvent> getDashboardEvents() {
        List<CalendarEvent> events = calendarService.getUpcomingEvents();
        List<DashboardEvent> dashboardEvents = new ArrayList<>();

        for (CalendarEvent event : events) {
            WeatherForecast forecast = null;
            if (event.getEventLocation() != null) {
                forecast = weatherService.getForecastForDate(event.getEventLocation(), event.getStartTime().toLocalDate());
            }
            dashboardEvents.add(new DashboardEvent(event, forecast));
        }

        return dashboardEvents;
    }
}
