package com.aurora.climatesync.service;

import com.aurora.climatesync.model.EventWeather;
import com.aurora.climatesync.model.HourlyForecast;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public interface WeatherService {
    List<WeatherForecast> getWeeklyForecast(double latitude, double longitude);
    List<WeatherForecast> getWeeklyForecast(Location location);
    List<HourlyForecast> getHourlyForecast(Location location, LocalDate date);
    WeatherForecast getForecastForDate(Location location, LocalDate date);
    EventWeather getForecastForTime(Location location, ZonedDateTime time);
}

