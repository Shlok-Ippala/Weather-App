package com.aurora.climatesync.service;

import com.aurora.climatesync.exception.WeatherServiceException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class WeatherServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private OpenMeteoParser parser;

    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherServiceImpl(restTemplate, "https://api.open-meteo.com/v1/forecast", "https://geocoding-api.open-meteo.com/v1/search", parser);
    }

    @Test
    void testGetWeeklyForecast_Success() throws Exception {
        String mockResponse = "{}"; // Response content doesn't matter as we mock parser
        
        WeatherForecast mockForecast = new WeatherForecast(LocalDate.of(2023, 10, 27), 15.5, 5.0, "Cloudy", 0.2, 0);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(parser.parseForecastResponse(mockResponse)).thenReturn(List.of(mockForecast));

        List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(43.7, -79.4);

        assertEquals(1, forecasts.size());
        WeatherForecast forecast = forecasts.get(0);
        assertEquals(LocalDate.of(2023, 10, 27), forecast.getDate());
        assertEquals(15.5, forecast.getMaxTemperature());
    }

    @Test
    void testGetWeeklyForecast_ApiFailure() {
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RuntimeException("API Error"));

        assertThrows(WeatherServiceException.class, () -> {
            weatherService.getWeeklyForecast(43.7, -79.4);
        });
    }

    @Test
    void testGetWeeklyForecast_WithLocation() throws Exception {
        Location location = new Location("Toronto", "Canada", 43.7, -79.4);
        String mockResponse = "{}";
        
        WeatherForecast mockForecast = new WeatherForecast(LocalDate.of(2023, 10, 27), 10.0, 2.0, "Clear", 0.0, 0);
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);
        when(parser.parseForecastResponse(mockResponse)).thenReturn(List.of(mockForecast));

        List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(location);

        assertEquals(1, forecasts.size());
        assertEquals(10.0, forecasts.get(0).getMaxTemperature());
    }
    
    @Test
    void testResolveLocation_Success() {
        String mockGeoResponse = "{}";
        
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockGeoResponse);
        
        Location loc = new Location("Toronto", "Canada", 0, 0);
        
        // Simulate parser updating the location
        doAnswer(invocation -> {
            Location l = invocation.getArgument(1);
            l.setLatitude(43.7);
            l.setLongitude(-79.4);
            return null;
        }).when(parser).parseGeocodingResponse(anyString(), any(Location.class));

        weatherService.resolveLocation(loc);
        
        assertEquals(43.7, loc.getLatitude());
        assertEquals(-79.4, loc.getLongitude());
    }
}
