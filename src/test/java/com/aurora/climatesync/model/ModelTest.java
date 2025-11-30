package com.aurora.climatesync.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testCalendarEvent() {
        ZonedDateTime start = ZonedDateTime.now();
        ZonedDateTime end = start.plusHours(1);
        Location loc = new Location("City", "Country", 10.0, 20.0);
        
        CalendarEvent event = new CalendarEvent("1", "Title", "Desc", start, end, loc, "1");
        
        assertEquals("1", event.getEventID());
        assertEquals("Title", event.getSummary());
        assertEquals("Desc", event.getDescription());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
        assertEquals(loc, event.getEventLocation());
        
        // Test constructor without colorId
        CalendarEvent event2 = new CalendarEvent("2", "Title2", "Desc2", start, end, loc);
        assertNull(event2.getColorId()); // Assuming getter exists or field is null
    }

    @Test
    void testLocation() {
        Location loc = new Location("City", "Country", 10.0, 20.0);
        assertEquals("City", loc.getCityName());
        assertEquals("Country", loc.getCountry());
        assertEquals(10.0, loc.getLatitude());
        assertEquals(20.0, loc.getLongitude());
        
        loc.setCityName("NewCity");
        assertEquals("NewCity", loc.getCityName());
        
        Location emptyLoc = new Location();
        assertNull(emptyLoc.getCityName());
    }

    @Test
    void testWeatherForecast() {
        LocalDate date = LocalDate.now();
        WeatherForecast forecast = new WeatherForecast(date, 20.0, 10.0, "Sunny", 0.0, 5.0);
        
        assertEquals(date, forecast.getDate());
        assertEquals(20.0, forecast.getMaxTemperature());
        assertEquals(10.0, forecast.getMinTemperature());
        assertEquals("Sunny", forecast.getCondition());
        assertEquals(0.0, forecast.getPrecipitationChance());
        assertEquals(5.0, forecast.getWindSpeed());
        
        assertTrue(forecast.toString().contains("Sunny"));
    }
    
    @Test
    void testUser() {
        User user = new User("123", "test@example.com", "Test User");
        assertEquals("123", user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getDisplayName());
        assertFalse(user.isGoogleConnected());
        
        user.setGoogleConnected(true);
        assertTrue(user.isGoogleConnected());
        
        user.clearTokens();
        assertFalse(user.isGoogleConnected());
    }
}
