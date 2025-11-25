package com.aurora.climatesync.controller;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.DashboardService;
import com.aurora.climatesync.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public List<DashboardEvent> getDashboardEvents() {
        return dashboardService.getDashboardEvents();
    }
}
