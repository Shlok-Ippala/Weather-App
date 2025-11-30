package com.aurora.climatesync.repository;

import com.aurora.climatesync.model.EventWeather;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;

import java.time.ZonedDateTime;
import java.util.List;

public interface WeatherRepository {
    List<WeatherForecast> fetchWeeklyForecast(double latitude, double longitude);
    EventWeather fetchForecastForTime(double latitude, double longitude, ZonedDateTime time);
    boolean resolveLocation(Location location);
}
