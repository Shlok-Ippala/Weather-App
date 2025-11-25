package com.aurora.climatesync.controller;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    public List<Location> search(@RequestParam("q") String q,
                                 @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return searchService.searchLocations(q, limit);
    }

    @GetMapping("/weather")
    public List<WeatherForecast> weather(@RequestParam("q") String q) {
        return searchService.searchAndGetWeeklyForecast(q);
    }
}