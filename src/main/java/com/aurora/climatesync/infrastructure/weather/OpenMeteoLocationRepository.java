package com.aurora.climatesync.infrastructure.weather;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.repository.LocationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OpenMeteoLocationRepository implements LocationRepository {

    private final RestTemplate restTemplate;
    private final String geocodingApiUrl;
    private final ObjectMapper objectMapper;

    public OpenMeteoLocationRepository(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${weather.api.geocoding-url:https://geocoding-api.open-meteo.com/v1/search}") String geocodingApiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.geocodingApiUrl = geocodingApiUrl;
    }

    @Override
    public List<Location> searchLocations(String query, int maxResults) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        int count = (maxResults > 0 && maxResults <= 100) ? maxResults : 100;

        try {
            String url = UriComponentsBuilder.fromUriString(geocodingApiUrl)
                    .queryParam("name", query.trim())
                    .queryParam("count", count)
                    .queryParam("language", "en")
                    .queryParam("format", "json")
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Location> parseResponse(String json) {
        List<Location> locations = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.has("results")) {
                return locations;
            }

            JsonNode results = root.get("results");
            if (results.isArray()) {
                for (JsonNode obj : results) {
                    String cityName = obj.path("name").asText("Unknown City");
                    String country = obj.path("country").asText("Unknown Country");
                    double latitude = obj.path("latitude").asDouble(0.0);
                    double longitude = obj.path("longitude").asDouble(0.0);

                    if (latitude == 0.0 && longitude == 0.0) {
                        locations.add(Location.unknown());
                    } else {
                        locations.add(new Location(cityName, country, latitude, longitude));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return locations;
    }
}
