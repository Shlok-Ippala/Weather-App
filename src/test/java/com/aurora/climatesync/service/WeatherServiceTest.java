package com.aurora.climatesync.service;

import com.aurora.climatesync.exception.LocationNotFoundException;
import com.aurora.climatesync.model.EventWeather;
import com.aurora.climatesync.model.HourlyForecast;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.repository.WeatherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WeatherServiceTest {

    @Mock
    private WeatherRepository weatherRepository;

    private WeatherServiceImpl weatherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherService = new WeatherServiceImpl(weatherRepository);
    }

    @Test
    void getWeeklyForecast_WithCoordinates_ShouldReturnForecast() {
        // Arrange
        double lat = 10.0;
        double lon = 20.0;
        List<WeatherForecast> expectedForecasts = Arrays.asList(new WeatherForecast(LocalDate.now(), 10.0, 5.0, "Sunny", 0.0, 10.0, 1));
        when(weatherRepository.fetchWeeklyForecast(lat, lon)).thenReturn(expectedForecasts);

        // Act
        List<WeatherForecast> result = weatherService.getWeeklyForecast(lat, lon);

        // Assert
        assertEquals(expectedForecasts, result);
        verify(weatherRepository).fetchWeeklyForecast(lat, lon);
    }

    @Test
    void getWeeklyForecast_WithLocationWithCoordinates_ShouldReturnForecast() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        List<WeatherForecast> expectedForecasts = Arrays.asList(new WeatherForecast(LocalDate.now(), 10.0, 5.0, "Sunny", 0.0, 10.0, 1));
        when(weatherRepository.fetchWeeklyForecast(10.0, 20.0)).thenReturn(expectedForecasts);

        // Act
        List<WeatherForecast> result = weatherService.getWeeklyForecast(location);

        // Assert
        assertEquals(expectedForecasts, result);
        verify(weatherRepository).fetchWeeklyForecast(10.0, 20.0);
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getWeeklyForecast_WithLocationWithoutCoordinates_ShouldResolveAndReturnForecast() {
        // Arrange
        Location location = new Location("City", "Country", 0.0, 0.0);
        List<WeatherForecast> expectedForecasts = Arrays.asList(new WeatherForecast(LocalDate.now(), 10.0, 5.0, "Sunny", 0.0, 10.0, 1));
        
        doAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            loc.setLatitude(10.0);
            loc.setLongitude(20.0);
            return true;
        }).when(weatherRepository).resolveLocation(location);
        
        when(weatherRepository.fetchWeeklyForecast(10.0, 20.0)).thenReturn(expectedForecasts);

        // Act
        List<WeatherForecast> result = weatherService.getWeeklyForecast(location);

        // Assert
        assertEquals(expectedForecasts, result);
        verify(weatherRepository).resolveLocation(location);
        verify(weatherRepository).fetchWeeklyForecast(10.0, 20.0);
    }

    @Test
    void getWeeklyForecast_WithLocationResolutionFailure_ShouldThrowException() {
        // Arrange
        Location location = new Location("UnknownCity", "Country", 0.0, 0.0);
        
        // resolveLocation returns false and does not update coordinates
        when(weatherRepository.resolveLocation(location)).thenReturn(false);

        // Act & Assert
        assertThrows(LocationNotFoundException.class, () -> weatherService.getWeeklyForecast(location));
        verify(weatherRepository).resolveLocation(location);
        verify(weatherRepository, never()).fetchWeeklyForecast(anyDouble(), anyDouble());
    }

    @Test
    void getHourlyForecast_WithLocationWithCoordinates_ShouldReturnForecast() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        LocalDate date = LocalDate.now();
        List<HourlyForecast> expectedForecasts = Collections.emptyList();
        when(weatherRepository.fetchHourlyForecast(10.0, 20.0, date)).thenReturn(expectedForecasts);

        // Act
        List<HourlyForecast> result = weatherService.getHourlyForecast(location, date);

        // Assert
        assertEquals(expectedForecasts, result);
        verify(weatherRepository).fetchHourlyForecast(10.0, 20.0, date);
    }

    @Test
    void getHourlyForecast_WithLocationWithoutCoordinates_ShouldResolveAndReturnForecast() {
        // Arrange
        Location location = new Location("City", "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();
        List<HourlyForecast> expectedForecasts = Collections.emptyList();

        doAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            loc.setLatitude(10.0);
            loc.setLongitude(20.0);
            return true;
        }).when(weatherRepository).resolveLocation(location);

        when(weatherRepository.fetchHourlyForecast(10.0, 20.0, date)).thenReturn(expectedForecasts);

        // Act
        List<HourlyForecast> result = weatherService.getHourlyForecast(location, date);

        // Assert
        assertEquals(expectedForecasts, result);
        verify(weatherRepository).resolveLocation(location);
        verify(weatherRepository).fetchHourlyForecast(10.0, 20.0, date);
    }

    @Test
    void getHourlyForecast_WithLocationResolutionFailure_ShouldThrowException() {
        // Arrange
        Location location = new Location("UnknownCity", "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();

        when(weatherRepository.resolveLocation(location)).thenReturn(false);

        // Act & Assert
        assertThrows(LocationNotFoundException.class, () -> weatherService.getHourlyForecast(location, date));
    }

    @Test
    void getForecastForDate_ShouldReturnMatchingForecast() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        LocalDate today = LocalDate.now();
        WeatherForecast expectedForecast = new WeatherForecast(today, 10.0, 5.0, "Sunny", 0.0, 10.0, 1);
        List<WeatherForecast> weeklyForecast = Arrays.asList(
                expectedForecast,
                new WeatherForecast(today.plusDays(1), 12.0, 6.0, "Cloudy", 0.0, 10.0, 1)
        );

        when(weatherRepository.fetchWeeklyForecast(10.0, 20.0)).thenReturn(weeklyForecast);

        // Act
        WeatherForecast result = weatherService.getForecastForDate(location, today);

        // Assert
        assertEquals(expectedForecast, result);
    }

    @Test
    void getForecastForDate_ShouldReturnNull_WhenDateNotFound() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        LocalDate today = LocalDate.now();
        List<WeatherForecast> weeklyForecast = Arrays.asList(
                new WeatherForecast(today.plusDays(1), 12.0, 6.0, "Cloudy", 0.0, 10.0, 1)
        );

        when(weatherRepository.fetchWeeklyForecast(10.0, 20.0)).thenReturn(weeklyForecast);

        // Act
        WeatherForecast result = weatherService.getForecastForDate(location, today);

        // Assert
        assertNull(result);
    }

    @Test
    void getForecastForDate_WithLocationWithoutCoordinates_ShouldResolve() {
        // Arrange
        Location location = new Location("City", "Country", 0.0, 0.0);
        LocalDate today = LocalDate.now();
        WeatherForecast expectedForecast = new WeatherForecast(today, 10.0, 5.0, "Sunny", 0.0, 10.0, 1);
        
        doAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            loc.setLatitude(10.0);
            loc.setLongitude(20.0);
            return true;
        }).when(weatherRepository).resolveLocation(location);

        when(weatherRepository.fetchWeeklyForecast(10.0, 20.0)).thenReturn(Collections.singletonList(expectedForecast));

        // Act
        WeatherForecast result = weatherService.getForecastForDate(location, today);

        // Assert
        assertEquals(expectedForecast, result);
        verify(weatherRepository).resolveLocation(location);
    }

    @Test
    void getForecastForDate_WithResolutionFailure_ShouldReturnNull() {
        // Arrange
        Location location = new Location("Unknown", "Country", 0.0, 0.0);
        LocalDate today = LocalDate.now();

        // Act
        WeatherForecast result = weatherService.getForecastForDate(location, today);

        // Assert
        assertNull(result);
        verify(weatherRepository, never()).resolveLocation(any()); // "Unknown" check prevents resolution
    }
    
    @Test
    void getForecastForDate_WithResolutionFailure_ShouldReturnNull2() {
        // Arrange
        Location location = new Location("City", "Country", 0.0, 0.0);
        LocalDate today = LocalDate.now();
        
        when(weatherRepository.resolveLocation(location)).thenReturn(false);

        // Act
        WeatherForecast result = weatherService.getForecastForDate(location, today);

        // Assert
        assertNull(result);
    }

    @Test
    void getForecastForTime_ShouldReturnForecast() {
        // Arrange
        Location location = new Location("City", "Country", 10.0, 20.0);
        ZonedDateTime time = ZonedDateTime.now();
        EventWeather expectedWeather = new EventWeather(15.0, "Sunny", 0.0, 5.0);

        when(weatherRepository.fetchForecastForTime(10.0, 20.0, time)).thenReturn(expectedWeather);

        // Act
        EventWeather result = weatherService.getForecastForTime(location, time);

        // Assert
        assertEquals(expectedWeather, result);
    }

    @Test
    void getForecastForTime_WithLocationWithoutCoordinates_ShouldResolve() {
        // Arrange
        Location location = new Location("City", "Country", 0.0, 0.0);
        ZonedDateTime time = ZonedDateTime.now();
        EventWeather expectedWeather = new EventWeather(15.0, "Sunny", 0.0, 5.0);

        doAnswer(invocation -> {
            Location loc = invocation.getArgument(0);
            loc.setLatitude(10.0);
            loc.setLongitude(20.0);
            return true;
        }).when(weatherRepository).resolveLocation(location);

        when(weatherRepository.fetchForecastForTime(10.0, 20.0, time)).thenReturn(expectedWeather);

        // Act
        EventWeather result = weatherService.getForecastForTime(location, time);

        // Assert
        assertEquals(expectedWeather, result);
        verify(weatherRepository).resolveLocation(location);
    }
    
    @Test
    void getForecastForTime_WithResolutionFailure_ShouldReturnNull() {
        // Arrange
        Location location = new Location("Unknown", "Country", 0.0, 0.0);
        ZonedDateTime time = ZonedDateTime.now();

        // Act
        EventWeather result = weatherService.getForecastForTime(location, time);

        // Assert
        assertNull(result);
    }
    
    @Test
    void getForecastForTime_WithResolutionFailure_ShouldReturnNull2() {
        // Arrange
        Location location = new Location("City", "Country", 0.0, 0.0);
        ZonedDateTime time = ZonedDateTime.now();
        
        when(weatherRepository.resolveLocation(location)).thenReturn(false);

        // Act
        EventWeather result = weatherService.getForecastForTime(location, time);

        // Assert
        assertNull(result);
    }

    @Test
    void getWeeklyForecast_WithNullCityName_ShouldThrowException() {
        Location location = new Location(null, "Country", 0.0, 0.0);
        assertThrows(LocationNotFoundException.class, () -> weatherService.getWeeklyForecast(location));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getWeeklyForecast_WithEmptyCityName_ShouldThrowException() {
        Location location = new Location("", "Country", 0.0, 0.0);
        assertThrows(LocationNotFoundException.class, () -> weatherService.getWeeklyForecast(location));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getWeeklyForecast_WithUnknownCityName_ShouldThrowException() {
        Location location = new Location("Unknown", "Country", 0.0, 0.0);
        assertThrows(LocationNotFoundException.class, () -> weatherService.getWeeklyForecast(location));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getHourlyForecast_WithNullCityName_ShouldThrowException() {
        Location location = new Location(null, "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();
        assertThrows(LocationNotFoundException.class, () -> weatherService.getHourlyForecast(location, date));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getHourlyForecast_WithEmptyCityName_ShouldThrowException() {
        Location location = new Location("", "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();
        assertThrows(LocationNotFoundException.class, () -> weatherService.getHourlyForecast(location, date));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getHourlyForecast_WithUnknownCityName_ShouldThrowException() {
        Location location = new Location("Unknown", "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();
        assertThrows(LocationNotFoundException.class, () -> weatherService.getHourlyForecast(location, date));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getForecastForDate_WithNullCityName_ShouldReturnNull() {
        Location location = new Location(null, "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();
        assertNull(weatherService.getForecastForDate(location, date));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getForecastForDate_WithEmptyCityName_ShouldReturnNull() {
        Location location = new Location("", "Country", 0.0, 0.0);
        LocalDate date = LocalDate.now();
        assertNull(weatherService.getForecastForDate(location, date));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getForecastForTime_WithNullCityName_ShouldReturnNull() {
        Location location = new Location(null, "Country", 0.0, 0.0);
        ZonedDateTime time = ZonedDateTime.now();
        assertNull(weatherService.getForecastForTime(location, time));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getForecastForTime_WithEmptyCityName_ShouldReturnNull() {
        Location location = new Location("", "Country", 0.0, 0.0);
        ZonedDateTime time = ZonedDateTime.now();
        assertNull(weatherService.getForecastForTime(location, time));
        verify(weatherRepository, never()).resolveLocation(any());
    }

    @Test
    void getWeeklyForecast_WithPartialCoordinates_ShouldReturnForecast() {
        // lat=0, lon=20
        Location location1 = new Location("City", "Country", 0.0, 20.0);
        List<WeatherForecast> expectedForecasts = Collections.emptyList();
        when(weatherRepository.fetchWeeklyForecast(0.0, 20.0)).thenReturn(expectedForecasts);
        assertEquals(expectedForecasts, weatherService.getWeeklyForecast(location1));

        // lat=10, lon=0
        Location location2 = new Location("City", "Country", 10.0, 0.0);
        when(weatherRepository.fetchWeeklyForecast(10.0, 0.0)).thenReturn(expectedForecasts);
        assertEquals(expectedForecasts, weatherService.getWeeklyForecast(location2));
    }

    @Test
    void getHourlyForecast_WithPartialCoordinates_ShouldReturnForecast() {
        // lat=0, lon=20
        Location location1 = new Location("City", "Country", 0.0, 20.0);
        LocalDate date = LocalDate.now();
        List<HourlyForecast> expectedForecasts = Collections.emptyList();
        when(weatherRepository.fetchHourlyForecast(0.0, 20.0, date)).thenReturn(expectedForecasts);
        assertEquals(expectedForecasts, weatherService.getHourlyForecast(location1, date));

        // lat=10, lon=0
        Location location2 = new Location("City", "Country", 10.0, 0.0);
        when(weatherRepository.fetchHourlyForecast(10.0, 0.0, date)).thenReturn(expectedForecasts);
        assertEquals(expectedForecasts, weatherService.getHourlyForecast(location2, date));
    }

    @Test
    void getForecastForDate_WithPartialCoordinates_ShouldReturnForecast() {
        // lat=0, lon=20
        Location location1 = new Location("City", "Country", 0.0, 20.0);
        LocalDate date = LocalDate.now();
        List<WeatherForecast> weekly = Collections.emptyList();
        when(weatherRepository.fetchWeeklyForecast(0.0, 20.0)).thenReturn(weekly);
        assertNull(weatherService.getForecastForDate(location1, date));

        // lat=10, lon=0
        Location location2 = new Location("City", "Country", 10.0, 0.0);
        when(weatherRepository.fetchWeeklyForecast(10.0, 0.0)).thenReturn(weekly);
        assertNull(weatherService.getForecastForDate(location2, date));
    }

    @Test
    void getForecastForTime_WithPartialCoordinates_ShouldReturnForecast() {
        // lat=0, lon=20
        Location location1 = new Location("City", "Country", 0.0, 20.0);
        ZonedDateTime time = ZonedDateTime.now();
        EventWeather expected = new EventWeather(10.0, "Sunny", 0.0, 0.0);
        when(weatherRepository.fetchForecastForTime(0.0, 20.0, time)).thenReturn(expected);
        assertEquals(expected, weatherService.getForecastForTime(location1, time));

        // lat=10, lon=0
        Location location2 = new Location("City", "Country", 10.0, 0.0);
        when(weatherRepository.fetchForecastForTime(10.0, 0.0, time)).thenReturn(expected);
        assertEquals(expected, weatherService.getForecastForTime(location2, time));
    }
}

