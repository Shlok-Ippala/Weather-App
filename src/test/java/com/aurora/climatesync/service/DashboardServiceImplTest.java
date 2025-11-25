package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class DashboardServiceImplTest {

    @Mock
    private CalendarService calendarService;

    @Mock
    private WeatherService weatherService;

    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dashboardService = new DashboardServiceImpl(calendarService, weatherService);
    }

    @Test
    void getDashboardEvents_ShouldReturnEventsWithWeather_WhenLocationExists() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        ZonedDateTime now = ZonedDateTime.now();
        CalendarEvent event = new CalendarEvent("1", "Summary", "Desc", now, now.plusHours(1), location, "1");
        WeatherForecast forecast = new WeatherForecast(now.toLocalDate(), 10.0, 20.0, "Sunny", 0.0, 10.0);

        when(calendarService.getUpcomingEvents(anyInt())).thenReturn(Collections.singletonList(event));
        when(weatherService.getForecastForDate(any(Location.class), any(LocalDate.class))).thenReturn(forecast);

        // Act
        List<DashboardEvent> result = dashboardService.getDashboardEvents();

        // Assert
        assertEquals(1, result.size());
        assertEquals(event, result.get(0).getCalendarEvent());
        assertEquals(forecast, result.get(0).getWeatherForecast());
        verify(weatherService).getForecastForDate(location, now.toLocalDate());
    }

    @Test
    void getDashboardEvents_ShouldReturnEventsWithoutWeather_WhenLocationIsNull() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        CalendarEvent event = new CalendarEvent("1", "Summary", "Desc", now, now.plusHours(1), null, "1");

        when(calendarService.getUpcomingEvents(anyInt())).thenReturn(Collections.singletonList(event));

        // Act
        List<DashboardEvent> result = dashboardService.getDashboardEvents();

        // Assert
        assertEquals(1, result.size());
        assertEquals(event, result.get(0).getCalendarEvent());
        assertNull(result.get(0).getWeatherForecast());
        verify(weatherService, never()).getForecastForDate(any(), any());
    }

    @Test
    void getDashboardEvents_ShouldReturnEventsWithoutWeather_WhenWeatherServiceFails() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        ZonedDateTime now = ZonedDateTime.now();
        CalendarEvent event = new CalendarEvent("1", "Summary", "Desc", now, now.plusHours(1), location, "1");

        when(calendarService.getUpcomingEvents(anyInt())).thenReturn(Collections.singletonList(event));
        when(weatherService.getForecastForDate(any(Location.class), any(LocalDate.class))).thenThrow(new RuntimeException("API Error"));

        // Act
        List<DashboardEvent> result = dashboardService.getDashboardEvents();

        // Assert
        assertEquals(1, result.size());
        assertEquals(event, result.get(0).getCalendarEvent());
        assertNull(result.get(0).getWeatherForecast());
    }

    @Test
    void getDashboardEvents_ShouldReturnEmptyList_WhenNoEvents() {
        // Arrange
        when(calendarService.getUpcomingEvents(anyInt())).thenReturn(Collections.emptyList());

        // Act
        List<DashboardEvent> result = dashboardService.getDashboardEvents();

        // Assert
        assertTrue(result.isEmpty());
    }
}
