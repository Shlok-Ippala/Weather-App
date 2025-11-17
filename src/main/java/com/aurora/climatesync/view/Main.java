package com.aurora.climatesync.view;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;

import java.util.*;

public class Main {

    // Loading
    public static void showLoading(String message){
        System.out.println(message);

        try{
            for (int i = 0; i < 3; i++){
              Thread.sleep(1000);
              System.out.println(".");
            }
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
        System.out.println("\n");
    }

    public static void main(String[] args) {
        WeatherService service = new WeatherService();
        Location toronto = new Location("Toronto", "Canada");

        showLoading("Fetching Weather Data " + toronto.getCityName() + "...");

        List<WeatherForecast> forecasts = service.getWeeklyForecast(toronto);
        System.out.println("=== 7-Day Weather Forecast for " + toronto + "===\n");

        for (WeatherForecast forecast : forecasts) {
            System.out.println(forecast);
        }
    }
}