package com.aurora.climatesync.presenter;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.DashboardService;
import com.aurora.climatesync.util.WeatherIconMapper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPresenter implements DashboardContract.Presenter {
    private final DashboardContract.View view;
    private final DashboardService dashboardService;
    private final CalendarService calendarService;
    private List<DashboardViewModel> allEvents = new ArrayList<>();

    public DashboardPresenter(DashboardContract.View view, DashboardService dashboardService, CalendarService calendarService) {
        this.view = view;
        this.dashboardService = dashboardService;
        this.calendarService = calendarService;
    }

    @Override
    public void onViewReady() {
        loadEvents();
    }

    @Override
    public void loadEvents() {
        loadEvents(25);
    }

    @Override
    public void loadEvents(int limit) {
        view.showLoading("Loading events...");
        
        SwingWorker<List<DashboardViewModel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DashboardViewModel> doInBackground() {
                List<DashboardEvent> events = dashboardService.getDashboardEvents(limit);
                return events.stream()
                        .map(DashboardPresenter.this::mapToViewModel)
                        .collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<DashboardViewModel> viewModels = get();
                    allEvents = viewModels;
                    view.showEvents(viewModels);
                } catch (Exception e) {
                    view.showError("Failed to load events: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    @Override
    public void onSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            view.showEvents(allEvents);
            return;
        }
        String lowerQuery = query.toLowerCase().trim();
        List<DashboardViewModel> filtered = allEvents.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(lowerQuery) ||
                        (e.getDescription() != null && e.getDescription().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
        view.showEvents(filtered);
    }

    private DashboardViewModel mapToViewModel(DashboardEvent event) {
        String weatherIcon = null;
        String tempDisplay = null;
        String weatherMessage = null;

        if (event.getEventWeather() != null) {
            String condition = event.getEventWeather().getCondition();
            weatherIcon = WeatherIconMapper.getIconForCondition(condition);
            tempDisplay = String.format("%.1f°C", event.getEventWeather().getTemperature());
            weatherMessage = generateWeatherMessage(condition);
        } else if (event.getWeatherForecast() != null) {
            String condition = event.getWeatherForecast().getCondition();
            weatherIcon = WeatherIconMapper.getIconForCondition(condition);
            if (event.getWeatherForecast().getCurrentTemperature() != null) {
                tempDisplay = String.format("%.1f°C", event.getWeatherForecast().getCurrentTemperature());
            } else {
                tempDisplay = String.format("%.0f-%.0f°C", 
                    event.getWeatherForecast().getMinTemperature(), 
                    event.getWeatherForecast().getMaxTemperature());
            }
            weatherMessage = generateWeatherMessage(condition);
        }

        String locationName = null;
        if (event.getCalendarEvent().getEventLocation() != null) {
            locationName = event.getCalendarEvent().getEventLocation().getCityName();
        }

        return new DashboardViewModel(
                event.getCalendarEvent().getEventID(),
                event.getCalendarEvent().getSummary(),
                event.getCalendarEvent().getDescription(),
                locationName,
                event.getCalendarEvent().getStartTime(),
                event.getCalendarEvent().getEndTime(),
                event.getCalendarEvent().getColorId(),
                weatherIcon,
                tempDisplay,
                weatherMessage,
                event.getCalendarEvent()
        );
    }

    private String generateWeatherMessage(String condition) {
        if (condition == null) return null;
        String lowerCondition = condition.toLowerCase();

        if (lowerCondition.contains("rain") || lowerCondition.contains("drizzle") || lowerCondition.contains("shower")) {
            return "It is forecasted to rain. Don't forget your umbrella!";
        } else if (lowerCondition.contains("snow") || lowerCondition.contains("blizzard")) {
            return "Snow is expected. Drive safely and bundle up!";
        } else if (lowerCondition.contains("clear") || lowerCondition.contains("sunny")) {
            return "It's going to be sunny. A great day for this event!";
        } else if (lowerCondition.contains("cloud")) {
            return "It might be cloudy, but still a good day.";
        } else if (lowerCondition.contains("storm") || lowerCondition.contains("thunder")) {
            return "Stormy weather ahead. Please stay safe indoors if possible.";
        }
        return "Weather looks okay for your event.";
    }

    @Override
    public void connectCalendar() {
        try {
            calendarService.connect();
            loadEvents(); // Reload events after connection
        } catch (Exception e) {
            view.showError("Failed to connect to calendar: " + e.getMessage());
        }
    }

    @Override
    public boolean isCalendarConnected() {
        return calendarService.isConnected();
    }

    @Override
    public void addEvent(CalendarEvent event) {
        try {
            calendarService.addEvent(event);
            loadEvents(); // Refresh list
        } catch (Exception e) {
            view.showError("Failed to add event: " + e.getMessage());
        }
    }

    @Override
    public void updateEvent(CalendarEvent event) {
        try {
            calendarService.updateEvent(event);
            loadEvents(); // Refresh list
        } catch (Exception e) {
            view.showError("Failed to update event: " + e.getMessage());
        }
    }

    @Override
    public void deleteEvent(String eventId) {
        try {
            calendarService.deleteEvent(eventId);
            loadEvents(); // Refresh list
        } catch (Exception e) {
            view.showError("Failed to delete event: " + e.getMessage());
        }
    }
}
