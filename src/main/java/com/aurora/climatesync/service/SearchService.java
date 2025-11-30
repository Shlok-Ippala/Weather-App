package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {
    private final WeatherService weatherService;
    private final LocationRepository locationRepository;

    @Autowired
    public SearchService(WeatherService weatherService, LocationRepository locationRepository) {
        this.weatherService = weatherService;
        this.locationRepository = locationRepository;
    }

    /**
     * Searches for locations matching the partial query (e.g., "New Y" for New York suggestions).
     * Ideal for autocomplete dropdowns. Returns up to maxResults matches.
     * @param query The search term (min 2 chars recommended for API).
     * @param maxResults Max number of results (1-100; default 10 if 0).
     * @return List of matching Locations, sorted by relevance (API default).
     */
    public List<Location> searchLocations(String query, int maxResults){
        if(query == null || query.trim().isEmpty()){
            return new ArrayList<>();
        }

        String q = query.trim().toLowerCase();

        if (q.contains("virtual") || q.contains("online")) {
            return new ArrayList<>();
        }

        return locationRepository.searchLocations(q, maxResults);
    }

    /**
     * Convenience: get the top result only (e.g. when user presses Enter)
     */
    public Location getTopLocation(String query){
        List<Location> results = searchLocations(query, 1);
        return results.isEmpty() ? Location.unknown() : results.get(0);
    }

    public List<WeatherForecast> searchAndGetWeeklyForecast(String query){
        Location location = getTopLocation(query);
        if(location.isUnknown()) {
            return new ArrayList<>();
        }

        return weatherService.getWeeklyForecast(location);
    }
}
