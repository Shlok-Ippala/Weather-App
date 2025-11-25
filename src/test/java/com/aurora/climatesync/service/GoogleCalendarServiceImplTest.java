package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GoogleCalendarServiceImplTest {

    private GoogleCalendarServiceImpl service;

    @Mock
    private Calendar mockCalendarClient;
    @Mock
    private Calendar.Events mockEvents;
    @Mock
    private Calendar.Events.List mockList;
    @Mock
    private Calendar.Events.Insert mockInsert;
    @Mock
    private Calendar.Events.Delete mockDelete;
    @Mock
    private Calendar.Events.Update mockUpdate;
    @Mock
    private Calendar.Events.Get mockGet;
    @Mock
    private GoogleCredentialManager mockCredentialManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GoogleCalendarServiceImpl(mockCredentialManager);
        service.setCalendarClient(mockCalendarClient);
    }

    @Test
    void testConnectThrowsExceptionWhenCredentialManagerFails() throws Exception {
        when(mockCredentialManager.getCredentials(any())).thenThrow(new IOException("Auth failed"));
        
        Exception exception = assertThrows(IOException.class, service::connect);
        assertEquals("Auth failed", exception.getMessage());
    }

    @Test
    void testGetUpcomingEvents() throws IOException {
        when(mockCalendarClient.events()).thenReturn(mockEvents);
        when(mockEvents.list(anyString())).thenReturn(mockList);
        // Chain the fluent API calls
        when(mockList.setMaxResults(anyInt())).thenReturn(mockList);
        when(mockList.setTimeMin(any(DateTime.class))).thenReturn(mockList);
        when(mockList.setOrderBy(anyString())).thenReturn(mockList);
        when(mockList.setSingleEvents(anyBoolean())).thenReturn(mockList);
        when(mockList.execute()).thenReturn(new Events().setItems(Collections.emptyList()));

        List<CalendarEvent> events = service.getUpcomingEvents();
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void testAddEvent() throws IOException {
        when(mockCalendarClient.events()).thenReturn(mockEvents);
        when(mockEvents.insert(anyString(), any(Event.class))).thenReturn(mockInsert);
        when(mockInsert.execute()).thenReturn(new Event());

        CalendarEvent event = new CalendarEvent("1", "Title", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location("City", "Country", 0, 0));
        service.addEvent(event);

        verify(mockEvents).insert(eq("primary"), any(Event.class));
    }
    
    @Test
    void testDeleteEvent() throws IOException {
        when(mockCalendarClient.events()).thenReturn(mockEvents);
        when(mockEvents.delete(anyString(), anyString())).thenReturn(mockDelete);
        // execute() is void, so we don't need to stub return value, just verify it's called
        
        service.deleteEvent("eventId");

        verify(mockEvents).delete("primary", "eventId");
        verify(mockDelete).execute();
    }
    
    @Test
    void testUpdateEvent() throws IOException {
        when(mockCalendarClient.events()).thenReturn(mockEvents);
        
        // Mock get() call
        when(mockEvents.get(anyString(), anyString())).thenReturn(mockGet);
        Event existingEvent = new Event().setId("eventId");
        when(mockGet.execute()).thenReturn(existingEvent);
        
        // Mock update() call
        when(mockEvents.update(anyString(), anyString(), any(Event.class))).thenReturn(mockUpdate);
        when(mockUpdate.execute()).thenReturn(new Event());

        CalendarEvent event = new CalendarEvent("eventId", "Title", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location("City", "Country", 0, 0));
        service.updateEvent(event);

        verify(mockEvents).update(eq("primary"), eq("eventId"), any(Event.class));
    }
}
