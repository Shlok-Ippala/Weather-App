package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import java.util.List;

import java.time.LocalDate;

public interface WeatherService {
    List<WeatherForecast> getWeeklyForecast(double latitude, double longitude);
    List<WeatherForecast> getWeeklyForecast(Location location);
    WeatherForecast getForecastForDate(Location location, LocalDate date);
}

