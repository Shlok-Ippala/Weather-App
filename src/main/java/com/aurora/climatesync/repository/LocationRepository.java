package com.aurora.climatesync.repository;

import com.aurora.climatesync.model.Location;
import java.util.List;

public interface LocationRepository {
    List<Location> searchLocations(String query, int maxResults);
}
