package com.aurora.climatesync.service;

import com.aurora.climatesync.model.DashboardEvent;
import java.util.List;

public interface DashboardService {
    /**
     * Retrieves upcoming calendar events and enriches them with weather forecasts.
     * @return List of DashboardEvents containing both calendar and weather data.
     */
    List<DashboardEvent> getDashboardEvents();
    
    /**
     * Retrieves a limited number of upcoming calendar events and enriches them with weather forecasts.
     * @param limit The maximum number of events to retrieve.
     * @return List of DashboardEvents containing both calendar and weather data.
     */
    List<DashboardEvent> getDashboardEvents(int limit);
}
