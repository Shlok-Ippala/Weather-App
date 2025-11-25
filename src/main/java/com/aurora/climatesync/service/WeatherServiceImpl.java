package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;
    private final String forecastApiUrl;
    private final String geocodingApiUrl;

    @org.springframework.beans.factory.annotation.Autowired
    public WeatherServiceImpl(
            RestTemplate restTemplate,
            @Value("${weather.api.forecast-url}") String forecastApiUrl,
            @Value("${weather.api.geocoding-url}") String geocodingApiUrl) {
        this.restTemplate = restTemplate;
        this.forecastApiUrl = forecastApiUrl;
        this.geocodingApiUrl = geocodingApiUrl;
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
                    .queryParam("daily",
                            "temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weathercode")
                    .queryParam("timezone", "auto")
                    .toUriString();

            // === Call API ===
            String json = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode daily = root.get("daily");

            JsonNode dates = daily.get("time");
            JsonNode maxTemps = daily.get("temperature_2m_max");
            JsonNode minTemps = daily.get("temperature_2m_min");
            JsonNode precip = daily.get("precipitation_probability_mean");
            JsonNode codes = daily.get("weathercode");

            List<WeatherForecast> forecasts = new ArrayList<>();

            // === Loop and build WeatherForecast objects ===
            for (int i = 0; i < dates.size(); i++) {
                LocalDate date = LocalDate.parse(dates.get(i).asText());
                double maxT = maxTemps.get(i).asDouble();
                double minT = minTemps.get(i).asDouble();
                double precipitationChance = precip.get(i).asDouble() / 100.0; // convert 0–100 → 0–1
                int code = codes.get(i).asInt();

                String condition = mapWeatherCode(code);

                forecasts.add(new WeatherForecast(
                        date,
                        maxT,
                        minT,
                        condition,
                        precipitationChance,
                        0  // add real wind speed later
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

    // Package-private for testing
    void resolveLocation(Location location) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(geocodingApiUrl)
                    .queryParam("name", location.getCityName())
                    .queryParam("count", 1)
                    .queryParam("language", "en")
                    .queryParam("format", "json")
                    .toUriString();

            String json = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            
            if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0) {
                JsonNode result = root.get("results").get(0);
                location.setLatitude(result.get("latitude").asDouble());
                location.setLongitude(result.get("longitude").asDouble());
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

