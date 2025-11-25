package com.aurora.climatesync.service;

import com.aurora.climatesync.exception.WeatherServiceException;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private final RestTemplate restTemplate;
    private final String forecastApiUrl;
    private final String geocodingApiUrl;
    private final OpenMeteoParser parser;

    @org.springframework.beans.factory.annotation.Autowired
    public WeatherServiceImpl(
            RestTemplate restTemplate,
            @Value("${weather.api.forecast-url}") String forecastApiUrl,
            @Value("${weather.api.geocoding-url}") String geocodingApiUrl,
            OpenMeteoParser parser) {
        this.restTemplate = restTemplate;
        this.forecastApiUrl = forecastApiUrl;
        this.geocodingApiUrl = geocodingApiUrl;
        this.parser = parser;
    }



    /**
     * Main API-powered method that fetches a 7-day forecast
     * using latitude + longitude from Open-Meteo API.
     */
    public List<WeatherForecast> getWeeklyForecast(double latitude, double longitude) {
        logger.info("Fetching weekly forecast for lat: {}, lon: {}", latitude, longitude);
        try {
            String url = buildForecastUrl(latitude, longitude);
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new WeatherServiceException("Empty response from Weather API");
            }

            return parser.parseForecastResponse(jsonResponse);
        } catch (Exception e) {
            logger.error("Failed to fetch weather forecast", e);
            throw new WeatherServiceException("Failed to fetch weather forecast", e);
        }
    }

    private String buildForecastUrl(double latitude, double longitude) {
        return UriComponentsBuilder
                .fromUriString(forecastApiUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,precipitation_probability_mean,weathercode")
                .queryParam("timezone", "auto")
                .toUriString();
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
            parser.parseGeocodingResponse(json, location);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

