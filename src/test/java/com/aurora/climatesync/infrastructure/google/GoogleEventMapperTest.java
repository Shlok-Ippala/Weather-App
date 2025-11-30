package com.aurora.climatesync.infrastructure.google;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GoogleEventMapperTest {

    private GoogleEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GoogleEventMapper();
    }

    @Test
    void mapToCalendarEvent_ShouldMapCorrectly() {
        // Arrange
        Event googleEvent = new Event();
        googleEvent.setId("event123");
        googleEvent.setSummary("Test Event");
        googleEvent.setDescription("Test Description");
        googleEvent.setLocation("Test Location");
        googleEvent.setColorId("1");

        long now = System.currentTimeMillis();
        EventDateTime start = new EventDateTime().setDateTime(new DateTime(now));
        EventDateTime end = new EventDateTime().setDateTime(new DateTime(now + 3600000)); // +1 hour

        googleEvent.setStart(start);
        googleEvent.setEnd(end);

        // Act
        CalendarEvent result = mapper.mapToCalendarEvent(googleEvent);

        // Assert
        assertEquals("event123", result.getEventID());
        assertEquals("Test Event", result.getSummary());
        assertEquals("Test Description", result.getDescription());
        assertEquals("Test Location", result.getEventLocation().getCityName());
        assertEquals("1", result.getColorId());
        assertNotNull(result.getStartTime());
        assertNotNull(result.getEndTime());
    }

    @Test
    void mapToCalendarEvent_ShouldHandleNullLocation() {
        // Arrange
        Event googleEvent = new Event();
        googleEvent.setId("event123");
        googleEvent.setSummary("Test Event");
        
        long now = System.currentTimeMillis();
        googleEvent.setStart(new EventDateTime().setDateTime(new DateTime(now)));
        googleEvent.setEnd(new EventDateTime().setDateTime(new DateTime(now + 3600000)));

        // Act
        CalendarEvent result = mapper.mapToCalendarEvent(googleEvent);

        // Assert
        assertEquals("Unknown", result.getEventLocation().getCityName());
    }

    @Test
    void mapToGoogleEvent_ShouldMapCorrectly() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        CalendarEvent calendarEvent = new CalendarEvent(
                "event123",
                "Test Event",
                "Test Description",
                now,
                now.plusHours(1),
                new Location("Test Location", "Unknown", 0, 0),
                "1"
        );

        // Act
        Event result = mapper.mapToGoogleEvent(calendarEvent);

        // Assert
        assertEquals("Test Event", result.getSummary());
        assertEquals("Test Description", result.getDescription());
        assertEquals("Test Location, Unknown", result.getLocation());
        assertEquals("1", result.getColorId());
        assertNotNull(result.getStart());
        assertNotNull(result.getEnd());
    }

    @Test
    void updateGoogleEvent_ShouldUpdateCorrectly() {
        // Arrange
        Event googleEvent = new Event();
        googleEvent.setSummary("Old Summary");
        
        ZonedDateTime now = ZonedDateTime.now();
        CalendarEvent calendarEvent = new CalendarEvent(
                "event123",
                "New Summary",
                "New Description",
                now,
                now.plusHours(1),
                new Location("New Location", "Unknown", 0, 0),
                "2"
        );

        // Act
        mapper.updateGoogleEvent(googleEvent, calendarEvent);

        // Assert
        assertEquals("New Summary", googleEvent.getSummary());
        assertEquals("New Description", googleEvent.getDescription());
        assertEquals("New Location, Unknown", googleEvent.getLocation());
        assertEquals("2", googleEvent.getColorId());
    }
}
