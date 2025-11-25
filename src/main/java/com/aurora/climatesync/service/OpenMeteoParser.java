package com.aurora.climatesync.service;

import com.aurora.climatesync.exception.WeatherServiceException;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class OpenMeteoParser {

    private final ObjectMapper objectMapper;

    public OpenMeteoParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<WeatherForecast> parseForecastResponse(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode daily = root.path("daily");

        if (daily.isMissingNode()) {
            throw new WeatherServiceException("Invalid response: 'daily' node missing");
        }

        JsonNode dates = daily.get("time");
        JsonNode maxTemps = daily.get("temperature_2m_max");
        JsonNode minTemps = daily.get("temperature_2m_min");
        JsonNode precip = daily.get("precipitation_probability_mean");
        JsonNode codes = daily.get("weathercode");

        List<WeatherForecast> forecasts = new ArrayList<>();

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date = LocalDate.parse(dates.get(i).asText());
            double maxT = maxTemps.get(i).asDouble();
            double minT = minTemps.get(i).asDouble();
            double precipitationChance = precip.get(i).asDouble() / 100.0;
            int code = codes.get(i).asInt();

            String condition = mapWeatherCode(code);

            forecasts.add(new WeatherForecast(
                    date,
                    maxT,
                    minT,
                    condition,
                    precipitationChance,
                    0 // windSpeed not in this query
            ));
        }
        return forecasts;
    }

    public void parseGeocodingResponse(String json, Location location) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("results") && root.get("results").isArray() && root.get("results").size() > 0) {
                JsonNode result = root.get("results").get(0);
                location.setLatitude(result.get("latitude").asDouble());
                location.setLongitude(result.get("longitude").asDouble());
            }
        } catch (Exception e) {
            // Log or rethrow? For now, keeping behavior similar to original
            e.printStackTrace();
        }
    }

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
