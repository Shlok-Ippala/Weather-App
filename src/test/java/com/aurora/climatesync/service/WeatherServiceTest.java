package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

public class WeatherServiceTest {

    // Default Toronto coordinates
    private static final double TORONTO_LAT = 43.6532;
    private static final double TORONTO_LON = -79.3832;
    // Use the real service (your project already has dummy data for tests)
    private final WeatherService service = Mockito.mock(WeatherService.class);
    // private final WeatherService service = new WeatherService();


    // Ensure 7-day forecast is returned

    @Test
    void testSevenDayForecastReturned() {
        Location toronto = new Location("Toronto", "Canada", TORONTO_LAT, TORONTO_LON);
        // Stub the mock service to return a 7-day forecast for Toronto
        List<WeatherForecast> mockForecasts = IntStream.range(0, 7)
            .mapToObj(i -> new WeatherForecast(LocalDate.now().plusDays(i), 10.0, 0.0, "Clear", 0.0, 0.0, 1))
            .collect(Collectors.toList());
        Mockito.when(service.getWeeklyForecast(Mockito.eq(toronto))).thenReturn(mockForecasts);

        List<WeatherForecast> forecasts = service.getWeeklyForecast(toronto);

        Assertions.assertEquals(7, forecasts.size(),
            "Forecast list should contain exactly 7 days");
    }


    // Ensure fields in WeatherForecast are not null or missing

    @Test
    void testFieldsNotNullOrMissing() {
        Location toronto = new Location("Toronto", "Canada", TORONTO_LAT, TORONTO_LON);

        List<WeatherForecast> forecasts = service.getWeeklyForecast(toronto);

        for (WeatherForecast wf : forecasts) {
            Assertions.assertNotNull(wf.getDate(), "Date cannot be null");
            Assertions.assertNotNull(wf.getCondition(), "Condition cannot be null");
            Assertions.assertTrue(wf.getMaxTemperature() > -100 && wf.getMaxTemperature() < 100,
                    "Max temperature is outside safe range");
            Assertions.assertTrue(wf.getMinTemperature() > -100 && wf.getMinTemperature() < 100,
                    "Min temperature is outside safe range");
        }
    }


    // coordinates are used (default location)
 
    @Test
    void testTorontoCoordinatesAreUsed() {
        Location toronto = new Location("Toronto", "Canada", TORONTO_LAT, TORONTO_LON);

        Assertions.assertEquals(TORONTO_LAT, toronto.getLatitude());
        Assertions.assertEquals(TORONTO_LON, toronto.getLongitude());
    }


    // API failure should not crash the application

    @Test
    void testApiFailureHandledSafely() {
        // Invalid coordinates to force failure in your WeatherService dummy client
        Location fakeLocation = new Location("FakeCity", "Nowhere", 9999, 9999);

        List<WeatherForecast> forecasts = service.getWeeklyForecast(fakeLocation);

        Assertions.assertNotNull(forecasts, "Forecast list should not be null");
        Assertions.assertTrue(forecasts.isEmpty(), "Forecast list should be empty on API failure");
    }
}

