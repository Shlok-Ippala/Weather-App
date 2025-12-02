package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final LocationRepository locationRepository;

    @Autowired
    public SearchService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location searchLocation(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Location.unknown();
        }

        String cleaned = query.trim();
        if (cleaned.contains(",")) {
            cleaned = cleaned.split(",")[0].trim(); // takes only "Toronto" from "Toronto, Canada"
        }

        List<Location> results = locationRepository.searchLocations(cleaned.toLowerCase(), 1);
        return results.isEmpty() ? Location.unknown() : results.get(0);
    }
}
