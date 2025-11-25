package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
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

    @Mock
    private GoogleCredentialManager credentialManager;

    @Mock
    private GoogleEventMapper eventMapper;

    @Mock
    private Calendar client;

    @Mock
    private Calendar.Events eventsResource;

    @Mock
    private Calendar.Events.List listRequest;

    @Mock
    private Calendar.Events.Insert insertRequest;

    @Mock
    private Calendar.Events.Update updateRequest;
    
    @Mock
    private Calendar.Events.Get getRequest;
    
    @Mock
    private Calendar.Events.Delete deleteRequest;

    private GoogleCalendarServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new GoogleCalendarServiceImpl(credentialManager, eventMapper);
        service.client = client; // Inject mock client
    }

    @Test
    void getUpcomingEvents_ShouldReturnEvents_WhenClientIsConnected() throws IOException {
        // Arrange
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.list(anyString())).thenReturn(listRequest);
        when(listRequest.setMaxResults(anyInt())).thenReturn(listRequest);
        when(listRequest.setTimeMin(any())).thenReturn(listRequest);
        when(listRequest.setOrderBy(anyString())).thenReturn(listRequest);
        when(listRequest.setSingleEvents(anyBoolean())).thenReturn(listRequest);

        Events events = new Events();
        Event googleEvent = new Event();
        events.setItems(Collections.singletonList(googleEvent));
        when(listRequest.execute()).thenReturn(events);

        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");
        when(eventMapper.mapToCalendarEvent(googleEvent)).thenReturn(calendarEvent);

        // Act
        List<CalendarEvent> result = service.getUpcomingEvents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(calendarEvent, result.get(0));
        verify(eventMapper).mapToCalendarEvent(googleEvent);
    }

    @Test
    void getUpcomingEvents_ShouldReturnEmptyList_WhenClientIsNull() {
        // Arrange
        service.client = null;

        // Act
        List<CalendarEvent> result = service.getUpcomingEvents();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void addEvent_ShouldCallInsert_WhenClientIsConnected() throws IOException {
        // Arrange
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");
        Event googleEvent = new Event();
        
        when(eventMapper.mapToGoogleEvent(calendarEvent)).thenReturn(googleEvent);
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.insert(anyString(), any(Event.class))).thenReturn(insertRequest);
        when(insertRequest.execute()).thenReturn(googleEvent);

        // Act
        service.addEvent(calendarEvent);

        // Assert
        verify(client.events()).insert("primary", googleEvent);
        verify(insertRequest).execute();
    }

    @Test
    void updateEvent_ShouldCallUpdate_WhenClientIsConnected() throws IOException {
        // Arrange
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");
        Event googleEvent = new Event();
        googleEvent.setId("1");

        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.get(anyString(), anyString())).thenReturn(getRequest);
        when(getRequest.execute()).thenReturn(googleEvent);
        
        when(eventsResource.update(anyString(), anyString(), any(Event.class))).thenReturn(updateRequest);
        when(updateRequest.execute()).thenReturn(googleEvent);

        // Act
        service.updateEvent(calendarEvent);

        // Assert
        verify(eventMapper).updateGoogleEvent(googleEvent, calendarEvent);
        verify(client.events()).update("primary", googleEvent.getId(), googleEvent);
        verify(updateRequest).execute();
    }
    
    @Test
    void deleteEvent_ShouldCallDelete_WhenClientIsConnected() throws IOException {
        // Arrange
        String eventId = "event123";
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.delete(anyString(), anyString())).thenReturn(deleteRequest);
        
        // Act
        service.deleteEvent(eventId);
        
        // Assert
        verify(client.events()).delete("primary", eventId);
        verify(deleteRequest).execute();
    }
}
