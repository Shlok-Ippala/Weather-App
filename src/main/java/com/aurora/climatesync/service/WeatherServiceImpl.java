package com.aurora.climatesync.service;

import com.aurora.climatesync.exception.LocationNotFoundException;
import com.aurora.climatesync.exception.WeatherServiceException;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.model.EventWeather;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;
    private final String forecastApiUrl;
    private final String geocodingApiUrl;
    private final String nominatimApiUrl;

    @org.springframework.beans.factory.annotation.Autowired
    public WeatherServiceImpl(
            RestTemplate restTemplate,
            @Value("${weather.api.forecast-url:https://api.open-meteo.com/v1/forecast}") String forecastApiUrl,
            @Value("${weather.api.geocoding-url:https://geocoding-api.open-meteo.com/v1/search}") String geocodingApiUrl,
            @Value("${weather.api.nominatim-url:https://nominatim.openstreetmap.org/search}") String nominatimApiUrl) {
        this.restTemplate = restTemplate;
        this.forecastApiUrl = forecastApiUrl;
        this.geocodingApiUrl = geocodingApiUrl;
        this.nominatimApiUrl = nominatimApiUrl;
    }



    /**
     * Main API-powered method that fetches a 7-day forecast
     * using latitude + longitude from Open-Meteo API.
     */
    public List<WeatherForecast> getWeeklyForecast(double latitude, double longitude) {

        try {
            // Open-Meteo API URL
            String url = UriComponentsBuilder
                    .fromUriString(forecastApiUrl)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "temperature_2m")
                    .queryParam("daily",
                            "temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weathercode,windspeed_10m_max")
                    .queryParam("timezone", "auto")
                    .toUriString();

            // === Call API ===
            String json = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (root == null || !root.has("daily")) {
                return new ArrayList<>();
            }

            // Parse Current Weather
            Double currentTemp = null;
            if (root.has("current")) {
                JsonNode current = root.get("current");
                if (current.has("temperature_2m")) {
                    currentTemp = current.get("temperature_2m").asDouble();
                }
            }

            JsonNode daily = root.get("daily");

            JsonNode dates = daily.get("time");
            JsonNode maxTemps = daily.get("temperature_2m_max");
            JsonNode minTemps = daily.get("temperature_2m_min");
            JsonNode precip = daily.get("precipitation_probability_mean");
            JsonNode codes = daily.get("weathercode");
            JsonNode winds = daily.get("windspeed_10m_max");

            if (dates == null || !dates.isArray()) {
                return new ArrayList<>();
            }

            int size = dates.size();
            List<WeatherForecast> forecasts = new ArrayList<>();

            int maxIndex = size;
            if (maxTemps != null && maxTemps.isArray()) maxIndex = Math.min(maxIndex, maxTemps.size());
            if (minTemps != null && minTemps.isArray()) maxIndex = Math.min(maxIndex, minTemps.size());
            if (precip != null && precip.isArray()) maxIndex = Math.min(maxIndex, precip.size());
            if (codes != null && codes.isArray()) maxIndex = Math.min(maxIndex, codes.size());
            if (winds != null && winds.isArray()) maxIndex = Math.min(maxIndex, winds.size());
            // === Loop and build WeatherForecast objects ===
            
            for (int i = 0; i < maxIndex; i++) {
                LocalDate date = LocalDate.parse(dates.get(i).asText());
                double maxT = maxTemps != null ? maxTemps.get(i).asDouble() : 0.0;
                double minT = minTemps != null ? minTemps.get(i).asDouble() : 0.0;
                double precipitationChance = precip != null ? (precip.get(i).asDouble() / 100.0) : 0.0;
                int code = codes != null ? codes.get(i).asInt() : -1;
                double windSpeed = winds != null ? winds.get(i).asDouble() : 0.0;

                String condition = mapWeatherCode(code);

                // Only set current temperature for the first day (Today)
                Double todayCurrentTemp = (i == 0) ? currentTemp : null;

                forecasts.add(new WeatherForecast(
                        date,
                        maxT,
                        minT,
                        condition,
                        precipitationChance,
                        windSpeed,
                        todayCurrentTemp
                ));
            }

            return forecasts;
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    /**
     * GUI wrapper: Accepts a Location object,
     * extracts lat/long, and calls the API version.
     */
    public List<WeatherForecast> getWeeklyForecast(Location location) {
        // If coordinates are missing, try to resolve them first
        if ((location.getLatitude() == 0.0 && location.getLongitude() == 0.0)
                && location.getCityName() != null && !location.getCityName().isEmpty()
                && !location.getCityName().equalsIgnoreCase("Unknown")) {
            resolveLocation(location);
        }

        if (location.getLatitude() == 0.0 && location.getLongitude() == 0.0) {
            throw new LocationNotFoundException("Could not find location: " + location.getCityName());
        }

        return getWeeklyForecast(location.getLatitude(), location.getLongitude());
    }

    @Override
    public WeatherForecast getForecastForDate(Location location, LocalDate date) {
        // 1. Resolve location if needed
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
             if (location.getCityName() != null && !location.getCityName().equals("Unknown") && !location.getCityName().isEmpty()) {
                 resolveLocation(location);
             } else {
                 // No valid location info
                 return null;
             }
        }
        
        // If resolution failed or was skipped, and we are still at 0,0 (Null Island), 
        // we assume it's invalid unless the user really meant 0,0. 
        // For this app, we'll treat 0,0 as invalid if it came from default initialization.
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
            return null;
        }

        // 2. Get weekly forecast
        List<WeatherForecast> weekly = getWeeklyForecast(location.getLatitude(), location.getLongitude());

        // 3. Find matching date
        for (WeatherForecast f : weekly) {
            if (f.getDate().equals(date)) {
                return f;
            }
        }
        return null; 
    }

    @Override
    public EventWeather getForecastForTime(Location location, ZonedDateTime time) {
        // 1. Resolve location if needed
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
             if (location.getCityName() != null && !location.getCityName().equals("Unknown") && !location.getCityName().isEmpty()) {
                 resolveLocation(location);
             } else {
                 return null;
             }
        }
        
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
            return null;
        }

        try {
            // Open-Meteo API URL for hourly data
            // We fetch data for the specific day to minimize payload
            String dateStr = time.toLocalDate().toString();
            
            // Request UTC to make time comparison easier
            String url = UriComponentsBuilder
                    .fromUriString(forecastApiUrl)
                    .queryParam("latitude", location.getLatitude())
                    .queryParam("longitude", location.getLongitude())
                    .queryParam("hourly", "temperature_2m,precipitation_probability,weathercode,windspeed_10m")
                    .queryParam("start_date", dateStr)
                    .queryParam("end_date", dateStr)
                    .queryParam("timezone", "UTC") 
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            if (root == null || !root.has("hourly")) {
                return null;
            }

            JsonNode hourly = root.get("hourly");
            JsonNode times = hourly.get("time");
            JsonNode temps = hourly.get("temperature_2m");
            JsonNode precip = hourly.get("precipitation_probability");
            JsonNode codes = hourly.get("weathercode");
            JsonNode winds = hourly.get("windspeed_10m");

            if (times == null || !times.isArray()) {
                return null;
            }

            // Find the closest hour
            int closestIndex = -1;
            long minDiff = Long.MAX_VALUE;
            
            Instant targetInstant = time.toInstant();
            
            for (int i = 0; i < times.size(); i++) {
                // Parse time from API (e.g., "2023-11-27T10:00")
                // Since we requested UTC, we can parse as LocalDateTime and attach UTC zone.
                LocalDateTime localDt = LocalDateTime.parse(times.get(i).asText());
                ZonedDateTime zdt = localDt.atZone(ZoneId.of("UTC"));
                
                long diff = Math.abs(Duration.between(zdt.toInstant(), targetInstant).toMinutes());
                
                if (diff < minDiff) {
                    minDiff = diff;
                    closestIndex = i;
                }
            }

            if (closestIndex != -1) {
                double temp = temps != null ? temps.get(closestIndex).asDouble() : 0.0;
                double precipitation = precip != null ? precip.get(closestIndex).asDouble() : 0.0; // percent
                int code = codes != null ? codes.get(closestIndex).asInt() : 0;
                double wind = winds != null ? winds.get(closestIndex).asDouble() : 0.0;
                
                String condition = mapWeatherCode(code);
                
                // precipitation in API is 0-100, convert to 0-1
                return new EventWeather(temp, condition, precipitation / 100.0, wind);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Package-private for testing
    void resolveLocation(Location location) {
        boolean resolved = false;
        
        // 1. Try Open-Meteo (Best for cities)
        try {
            String url = geocodingApiUrl + "?name=" + 
                    URLEncoder.encode(location.getCityName(), StandardCharsets.UTF_8) +
                    "&count=10&language=en";

            String json = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            
            if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0) {
                JsonNode results = root.get("results");
                JsonNode bestMatch = results.get(0); // Default to first result

                // If country is specified, try to find a match
                if (location.getCountry() != null && !location.getCountry().isEmpty()) {
                    for (JsonNode result : results) {
                        if (result.has("country") && 
                            result.get("country").asText().equalsIgnoreCase(location.getCountry())) {
                            bestMatch = result;
                            break;
                        }
                    }
                }

                if (bestMatch.has("latitude") && bestMatch.has("longitude")) {
                    location.setLatitude(bestMatch.get("latitude").asDouble());
                    location.setLongitude(bestMatch.get("longitude").asDouble());
                    
                    if (bestMatch.has("name")) {
                        location.setCityName(bestMatch.get("name").asText());
                    }
                    if (bestMatch.has("country")) {
                        location.setCountry(bestMatch.get("country").asText());
                    }
                    resolved = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. If not resolved, try Nominatim (Best for addresses)
        if (!resolved) {
            resolveWithNominatim(location);
        }
    }

    private void resolveWithNominatim(Location location) {
        try {
            String query = location.getCityName();
            if (location.getCountry() != null && !location.getCountry().isEmpty()) {
                query += ", " + location.getCountry();
            }

            String url = UriComponentsBuilder
                    .fromUriString(nominatimApiUrl)
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .toUriString();

            // Nominatim requires a User-Agent header
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "ClimateSync/1.0");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                if (root.isArray() && root.size() > 0) {
                    JsonNode result = root.get(0);
                    if (result.has("lat") && result.has("lon")) {
                        location.setLatitude(result.get("lat").asDouble());
                        location.setLongitude(result.get("lon").asDouble());
                        
                        if (result.has("display_name")) {
                            // Nominatim returns full address in display_name
                            // We'll just use the first part as city name for simplicity, or keep the full string
                            String displayName = result.get("display_name").asText();
                            String[] parts = displayName.split(",");
                            location.setCityName(parts[0].trim());
                            if (parts.length > 1) {
                                location.setCountry(parts[parts.length - 1].trim());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert Open-Meteo weather codes → text.
     */
    private String mapWeatherCode(int code) {
        switch (code) {
            case 0: return "Clear";
            case 1:
            case 2:
            case 3: return "Cloudy";
            case 45:
            case 48: return "Fog";
            case 51:
            case 53:
            case 55: return "Drizzle";
            case 61:
            case 63:
            case 65: return "Rainy";
            case 71:
            case 73:
            case 75: return "Snow";
            case 95:
            case 96:
            case 99: return "Thunderstorm";
            default: return "Unknown";
        }
    }
}

