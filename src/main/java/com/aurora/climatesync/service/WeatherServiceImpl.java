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

import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    /**
     * Main API-powered method that fetches a 7-day forecast
     * using latitude + longitude from Open-Meteo API.
     */
    public List<WeatherForecast> getWeeklyForecast(double latitude, double longitude) {

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Open-Meteo API URL
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://api.open-meteo.com/v1/forecast")
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

