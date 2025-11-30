package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.model.WeatherForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);
    private final CalendarService calendarService;
    private final WeatherService weatherService;

    public DashboardServiceImpl(CalendarService calendarService, WeatherService weatherService) {
        this.calendarService = calendarService;
        this.weatherService = weatherService;
    }

    @Override
    public List<DashboardEvent> getDashboardEvents() {
        return getDashboardEvents(25);
    }

    @Override
    public List<DashboardEvent> getDashboardEvents(int limit) {
        List<CalendarEvent> events = calendarService.getUpcomingEvents(limit);
        List<DashboardEvent> dashboardEvents = new ArrayList<>();

        for (CalendarEvent event : events) {
            WeatherForecast forecast = null;
            int weathercode = 0;
            if (event.getEventLocation() != null) {
                try {
                    forecast = weatherService.getForecastForDate(
                            event.getEventLocation(),
                            event.getStartTime().toLocalDate()
                    );

                    if (forecast != null) {
                        weathercode = forecast.getWeatherCode();
                    }

                } catch (Exception e) {
                    // Log error but continue processing other events
                    logger.error("Could not fetch weather for event: {}", event.getSummary(), e);
                }
            }
            dashboardEvents.add(new DashboardEvent(event, forecast, weathercode));
        }

        return dashboardEvents;
    }
}
