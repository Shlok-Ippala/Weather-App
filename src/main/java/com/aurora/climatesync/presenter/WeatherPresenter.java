package com.aurora.climatesync.presenter;

import com.aurora.climatesync.exception.LocationNotFoundException;
import com.aurora.climatesync.model.HourlyForecast;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;
import com.aurora.climatesync.service.SearchService;
import com.aurora.climatesync.util.WeatherIconMapper;
import com.aurora.climatesync.view.WeatherClimateMapper;

import javax.swing.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class WeatherPresenter implements WeatherContract.Presenter {
    private final WeatherContract.View view;
    private final WeatherService weatherService;
    private final SearchService searchService;

    public WeatherPresenter(WeatherContract.View view, WeatherService weatherService, SearchService searchService) {
        this.view = view;
        this.weatherService = weatherService;
        this.searchService = searchService;
    }

    @Override
    public void onViewReady() {
        // Load default
        onSearch("Toronto, Canada");
    }

    @Override
    public void onSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            view.showError("Please enter a location.");
            return;
        }

        view.showLoading("Searching for " + query.trim() + "...");

        new SwingWorker<WeatherResult, Void>() {
            private Location resolvedLocation;

            @Override
            protected WeatherResult doInBackground() throws Exception {
                resolvedLocation = searchService.searchLocation(query.trim());  // ← REAL LOCATION FROM API
                if (resolvedLocation.isUnknown()) {
                    return null;
                }
                List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(resolvedLocation);
                List<HourlyForecast> hourlyForecasts = weatherService.getHourlyForecast(resolvedLocation, LocalDate.now());
                List<WeatherViewModel> viewModels = forecasts.stream()
                        .map(WeatherPresenter.this::mapToViewModel)
                        .collect(Collectors.toList());
                return new WeatherResult(viewModels, hourlyForecasts);
            }

            @Override
            protected void done() {
                try {
                    WeatherResult result = get();
                    view.hideLoading();

                    if (result == null || resolvedLocation == null || resolvedLocation.isUnknown()) {
                        view.showError("Location not found: " + query.trim());
                        return;
                    }

                    String displayName = resolvedLocation.getCityName();
                    if (resolvedLocation.getCountry() != null && !resolvedLocation.getCountry().isEmpty()) {
                        displayName += ", " + resolvedLocation.getCountry();
                    }

                    view.showWeather(displayName, result.viewModels);
                    view.updateChart(result.hourlyForecasts);

                } catch (Exception e) {
                    view.hideLoading();
                    view.showError("Location not found: " + query.trim());
                }
            }
        }.execute();
    }

    private WeatherViewModel mapToViewModel(WeatherForecast forecast) {
        // Modify the source of iconFile.
        String iconFile = WeatherClimateMapper.getIcon(forecast.getWeathercode());

        String tempDisplay;
        if (forecast.getCurrentTemperature() != null) {
            tempDisplay = Math.round(forecast.getCurrentTemperature()) + "°C";
        } else {
            tempDisplay = Math.round(forecast.getMaxTemperature()) + "°C";
        }

        String highLow = String.format("H: %.0f°  L: %.0f°", 
                forecast.getMaxTemperature(), forecast.getMinTemperature());
        
        String precip = String.format("%.0f%%", forecast.getPrecipitationChance() * 100);
        String wind = String.format("%.1f km/h", forecast.getWindSpeed());

        return new WeatherViewModel(
                forecast.getDate(),
                tempDisplay,
                highLow,
                forecast.getCondition(),
                iconFile,
                precip,
                wind
        );
    }

    /**
     * Internal class to hold view models and hourly forecasts together.
     */
    private static class WeatherResult {
        final List<WeatherViewModel> viewModels;
        final List<HourlyForecast> hourlyForecasts;

        WeatherResult(List<WeatherViewModel> viewModels, List<HourlyForecast> hourlyForecasts) {
            this.viewModels = viewModels;
            this.hourlyForecasts = hourlyForecasts;
        }
    }
}
