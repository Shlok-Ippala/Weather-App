package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import java.util.List;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import com.aurora.climatesync.model.EventWeather;

public interface WeatherService {
    List<WeatherForecast> getWeeklyForecast(double latitude, double longitude);
    List<WeatherForecast> getWeeklyForecast(Location location);
    WeatherForecast getForecastForDate(Location location, LocalDate date);
    EventWeather getForecastForTime(Location location, ZonedDateTime time);
}

