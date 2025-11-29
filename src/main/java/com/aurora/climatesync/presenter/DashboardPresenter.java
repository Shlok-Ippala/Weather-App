package com.aurora.climatesync.presenter;

import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.service.DashboardService;
import com.aurora.climatesync.util.WeatherIconMapper;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardPresenter implements DashboardContract.Presenter {
    private final DashboardContract.View view;
    private final DashboardService dashboardService;

    public DashboardPresenter(DashboardContract.View view, DashboardService dashboardService) {
        this.view = view;
        this.dashboardService = dashboardService;
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
                    view.showEvents(viewModels);
                } catch (Exception e) {
                    view.showError("Failed to load events: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private DashboardViewModel mapToViewModel(DashboardEvent event) {
        String weatherIcon = null;
        String tempDisplay = null;

        if (event.getEventWeather() != null) {
            weatherIcon = WeatherIconMapper.getIconForCondition(event.getEventWeather().getCondition());
            tempDisplay = String.format("%.1f°C", event.getEventWeather().getTemperature());
        } else if (event.getWeatherForecast() != null) {
            weatherIcon = WeatherIconMapper.getIconForCondition(event.getWeatherForecast().getCondition());
            if (event.getWeatherForecast().getCurrentTemperature() != null) {
                tempDisplay = String.format("%.1f°C", event.getWeatherForecast().getCurrentTemperature());
            } else {
                tempDisplay = String.format("%.0f-%.0f°C", 
                    event.getWeatherForecast().getMinTemperature(), 
                    event.getWeatherForecast().getMaxTemperature());
            }
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
                event.getCalendarEvent()
        );
    }
}
