package com.aurora.climatesync.presenter;

import com.aurora.climatesync.exception.LocationNotFoundException;
import com.aurora.climatesync.model.HourlyForecast;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;
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

    public WeatherPresenter(WeatherContract.View view, WeatherService weatherService) {
        this.view = view;
        this.weatherService = weatherService;
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
        
        String[] parts = query.split(",");
        String city = parts[0].trim();
        String country = parts.length > 1 ? parts[1].trim() : "";
        
        String displayLocation = city + (country.isEmpty() ? "" : ", " + country);

        view.showLoading("Fetching weather for " + displayLocation + "...");

        new SwingWorker<WeatherResult, Void>() {
            private Location resolvedLocation;

            @Override
            protected WeatherResult doInBackground() throws Exception {
                resolvedLocation = new Location(city, country, 0.0, 0.0);
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
                    
                    String displayName = resolvedLocation.getCityName();
                    if (resolvedLocation.getCountry() != null && !resolvedLocation.getCountry().isEmpty()) {
                        displayName += ", " + resolvedLocation.getCountry();
                    }
                    
                    view.showWeather(displayName, result.viewModels);
                    view.updateChart(result.hourlyForecasts);
                } catch (InterruptedException | ExecutionException e) {
                    view.hideLoading();
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    
                    if (cause instanceof LocationNotFoundException) {
                        view.showError("Could not find location: " + displayLocation + ". Please check spelling.");
                    } else {
                        view.showError("Error fetching weather: " + cause.getMessage());
                    }
                    e.printStackTrace();
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
