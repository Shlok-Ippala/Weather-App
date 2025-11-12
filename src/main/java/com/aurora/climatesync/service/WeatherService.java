package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    public List<WeatherForecast> getWeeklyForecast(Location location) {
        List<WeatherForecast> forecasts = new ArrayList <>();

        forecasts.add(new WeatherForecast(LocalDate.now(), 24.5, 15.2, "Sunny", 0.05, 8.4, location));
        forecasts.add(new WeatherForecast(LocalDate.now().plusDays(1), 22.8, 14.9, "Cloudy", 0.35, 10.2, location));
        forecasts.add(new WeatherForecast(LocalDate.now().plusDays(2), 19.4, 12.8, "Rainy", 0.75, 12.5, location));
        forecasts.add(new WeatherForecast(LocalDate.now().plusDays(3), 20.1, 13.5, "Windy", 0.20, 14.0, location));
        forecasts.add(new WeatherForecast(LocalDate.now().plusDays(4), 25.0, 17.0, "Sunny", 0.10, 9.8, location));
        forecasts.add(new WeatherForecast(LocalDate.now().plusDays(5), 18.9, 13.8, "Rainy", 0.60, 11.7, location));
        forecasts.add(new WeatherForecast(LocalDate.now().plusDays(6), 21.6, 13.8, "Cloudy", 0.40, 10.5, location));

        return forecasts;
    }
}
