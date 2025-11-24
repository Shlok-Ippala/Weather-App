package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import java.util.List;

public interface WeatherService {
    List<WeatherForecast> getWeeklyForecast(double latitude, double longitude);
    List<WeatherForecast> getWeeklyForecast(Location location);
}

