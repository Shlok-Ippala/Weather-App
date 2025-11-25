package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class WeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherServiceImpl(restTemplate, "https://api.open-meteo.com/v1/forecast", "https://geocoding-api.open-meteo.com/v1/search");
    }

    @Test
    void testGetWeeklyForecast_Success() {
        String mockResponse = "{"
                + "\"daily\": {"
                + "\"time\": [\"2023-10-27\"],"
                + "\"temperature_2m_max\": [15.5],"
                + "\"temperature_2m_min\": [5.0],"
                + "\"precipitation_probability_mean\": [20],"
                + "\"weathercode\": [1]"
                + "}"
                + "}";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(43.7, -79.4);

        assertEquals(1, forecasts.size());
        WeatherForecast forecast = forecasts.get(0);
        assertEquals(LocalDate.of(2023, 10, 27), forecast.getDate());
        assertEquals(15.5, forecast.getMaxTemperature());
        assertEquals(5.0, forecast.getMinTemperature());
        assertEquals(0.2, forecast.getPrecipitationChance());
        assertEquals("Cloudy", forecast.getCondition());
    }

    @Test
    void testGetWeeklyForecast_ApiFailure() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Error"));

        List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(43.7, -79.4);

        assertNotNull(forecasts);
        assertTrue(forecasts.isEmpty());
    }

    @Test
    void testGetWeeklyForecast_WithLocation() {
        Location location = new Location("Toronto", "Canada", 43.7, -79.4);
        
        String mockResponse = "{"
                + "\"daily\": {"
                + "\"time\": [\"2023-10-27\"],"
                + "\"temperature_2m_max\": [10.0],"
                + "\"temperature_2m_min\": [2.0],"
                + "\"precipitation_probability_mean\": [0],"
                + "\"weathercode\": [0]"
                + "}"
                + "}";

        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(location);

        assertEquals(1, forecasts.size());
        assertEquals(10.0, forecasts.get(0).getMaxTemperature());
    }
    
    @Test
    void testResolveLocation_Success() {
        String mockGeoResponse = "{"
                + "\"results\": [{"
                + "\"latitude\": 43.7,"
                + "\"longitude\": -79.4,"
                + "\"name\": \"Toronto\","
                + "\"country\": \"Canada\""
                + "}]"
                + "}";
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockGeoResponse);
        
        Location loc = new Location("Toronto", "Canada", 0, 0);
        weatherService.resolveLocation(loc);
        
        assertEquals(43.7, loc.getLatitude());
        assertEquals(-79.4, loc.getLongitude());
    }
}
