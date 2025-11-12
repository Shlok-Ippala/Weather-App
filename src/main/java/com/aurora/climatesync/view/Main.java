package com.aurora.climatesync.view;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        WeatherService service = new WeatherService();
        Location toronto = new Location("Toronto", "Canada");

        List<WeatherForecast> forecasts = service.getWeeklyForecast(toronto);
        System.out.println("=== 7-Day Weather Forecast for " + toronto + "===\n");

        for (WeatherForecast forecast : forecasts) {
            System.out.println(forecast);
        }
    }
}