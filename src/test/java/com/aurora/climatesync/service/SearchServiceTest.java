package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SearchServiceTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private LocationRepository locationRepository;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        searchService = new SearchService(weatherService, locationRepository);
    }

    @Test
    void testSearchLocations_NullQuery() {
        List<Location> results = searchService.searchLocations(null, 10);
        assertTrue(results.isEmpty());
        verify(locationRepository, never()).searchLocations(anyString(), anyInt());
    }

    @Test
    void testSearchLocations_EmptyQuery() {
        List<Location> results = searchService.searchLocations("   ", 10);
        assertTrue(results.isEmpty());
        verify(locationRepository, never()).searchLocations(anyString(), anyInt());
    }

    @Test
    void testSearchLocations_VirtualQuery() {
        List<Location> results = searchService.searchLocations("virtual meeting", 10);
        assertTrue(results.isEmpty());
        verify(locationRepository, never()).searchLocations(anyString(), anyInt());
    }

    @Test
    void testSearchLocations_OnlineQuery() {
        List<Location> results = searchService.searchLocations("online event", 10);
        assertTrue(results.isEmpty());
        verify(locationRepository, never()).searchLocations(anyString(), anyInt());
    }

    @Test
    void testSearchLocations_ValidQuery() {
        List<Location> mockLocations = new ArrayList<>();
        mockLocations.add(new Location("Toronto", "Canada", 43.7, -79.4));
        when(locationRepository.searchLocations(eq("toronto"), anyInt())).thenReturn(mockLocations);

        List<Location> results = searchService.searchLocations("Toronto", 10);
        assertEquals(1, results.size());
        assertEquals("Toronto", results.get(0).getCityName());
        verify(locationRepository).searchLocations("toronto", 10);
    }

    @Test
    void testGetTopLocation_Found() {
        List<Location> mockLocations = new ArrayList<>();
        mockLocations.add(new Location("Toronto", "Canada", 43.7, -79.4));
        when(locationRepository.searchLocations(eq("toronto"), eq(1))).thenReturn(mockLocations);

        Location result = searchService.getTopLocation("Toronto");
        assertFalse(result.isUnknown());
        assertEquals("Toronto", result.getCityName());
    }

    @Test
    void testGetTopLocation_NotFound() {
        when(locationRepository.searchLocations(anyString(), anyInt())).thenReturn(Collections.emptyList());

        Location result = searchService.getTopLocation("Nowhere");
        assertTrue(result.isUnknown());
    }

    @Test
    void testSearchAndGetWeeklyForecast_Found() {
        List<Location> mockLocations = new ArrayList<>();
        Location loc = new Location("Toronto", "Canada", 43.7, -79.4);
        mockLocations.add(loc);
        when(locationRepository.searchLocations(eq("toronto"), eq(1))).thenReturn(mockLocations);

        List<WeatherForecast> mockForecasts = new ArrayList<>();
        when(weatherService.getWeeklyForecast(loc)).thenReturn(mockForecasts);

        List<WeatherForecast> results = searchService.searchAndGetWeeklyForecast("Toronto");
        assertNotNull(results);
        verify(weatherService).getWeeklyForecast(loc);
    }

    @Test
    void testSearchAndGetWeeklyForecast_NotFound() {
        when(locationRepository.searchLocations(anyString(), anyInt())).thenReturn(Collections.emptyList());

        List<WeatherForecast> results = searchService.searchAndGetWeeklyForecast("Nowhere");
        assertTrue(results.isEmpty());
        verify(weatherService, never()).getWeeklyForecast(any(Location.class));
    }
}
