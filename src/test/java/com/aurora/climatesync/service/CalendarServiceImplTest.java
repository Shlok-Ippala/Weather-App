package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.repository.CalendarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalendarServiceImplTest {

    private CalendarRepository calendarRepository;
    private CalendarServiceImpl calendarService;

    @BeforeEach
    void setUp() {
        calendarRepository = mock(CalendarRepository.class);
        calendarService = new CalendarServiceImpl(calendarRepository);
    }

    private CalendarEvent createDummyEvent(String id) {
        Location location = new Location("Toronto", "Canada", 0, 0);
        return new CalendarEvent(
                id,
                "Test Event " + id,
                "Description for " + id,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusHours(1),
                location,
                "1"
        );
    }

    @Test
    void testGetUpcomingEvents() {
        List<CalendarEvent> mockEvents = Arrays.asList(createDummyEvent("1"), createDummyEvent("2"));
        when(calendarRepository.getUpcomingEvents()).thenReturn(mockEvents);

        List<CalendarEvent> result = calendarService.getUpcomingEvents();
        assertEquals(mockEvents, result);

        verify(calendarRepository, times(1)).getUpcomingEvents();
    }

    @Test
    void testGetUpcomingEventsWithMaxResults() {
        List<CalendarEvent> mockEvents = Arrays.asList(createDummyEvent("1"), createDummyEvent("2"));
        when(calendarRepository.getUpcomingEvents(2)).thenReturn(mockEvents);

        List<CalendarEvent> result = calendarService.getUpcomingEvents(2);
        assertEquals(mockEvents, result);

        verify(calendarRepository, times(1)).getUpcomingEvents(2);
    }

    @Test
    void testAddEvent() {
        CalendarEvent event = createDummyEvent("3");
        when(calendarRepository.addEvent(event)).thenReturn(event);

        CalendarEvent result = calendarService.addEvent(event);
        assertEquals(event, result);

        verify(calendarRepository, times(1)).addEvent(event);
    }

    @Test
    void testUpdateEvent() {
        CalendarEvent event = createDummyEvent("4");
        doNothing().when(calendarRepository).updateEvent(event);

        calendarService.updateEvent(event);

        verify(calendarRepository, times(1)).updateEvent(event);
    }

    @Test
    void testDeleteEvent() {
        String eventId = "5";
        doNothing().when(calendarRepository).deleteEvent(eventId);

        calendarService.deleteEvent(eventId);

        verify(calendarRepository, times(1)).deleteEvent(eventId);
    }

    @Test
    void testConnect() throws Exception {
        String token = "connected-token";
        when(calendarRepository.connect()).thenReturn(token);

        String result = calendarService.connect();
        assertEquals(token, result);

        verify(calendarRepository, times(1)).connect();
    }

    @Test
    void testIsConnected() {
        when(calendarRepository.isConnected()).thenReturn(true);

        boolean result = calendarService.isConnected();
        assertTrue(result);

        verify(calendarRepository, times(1)).isConnected();
    }
}
