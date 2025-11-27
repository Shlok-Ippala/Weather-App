package com.aurora.climatesync.controller;

import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WeatherSearchController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherSearchController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather/search")
    public List<WeatherForecast> searchWeatherByCoordinates(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude) {
        return weatherService.getWeeklyForecast(latitude, longitude);
    }
}
