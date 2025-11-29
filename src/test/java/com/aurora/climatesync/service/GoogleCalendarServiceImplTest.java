package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.google.api.client.http.javanet.NetHttpTransport;
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

    @Mock
    private Calendar.Calendars calendars;

    @Mock
    private Calendar.Calendars.Get calendarsGet;

    private TestableGoogleCalendarServiceImpl service;

    // Subclass to override protected methods
    private static class TestableGoogleCalendarServiceImpl extends GoogleCalendarServiceImpl {
        private NetHttpTransport mockTransport;
        private Calendar mockClient;
        private String mockCalendarId;

        public TestableGoogleCalendarServiceImpl(GoogleCredentialManager credentialManager, GoogleEventMapper eventMapper) {
            super(credentialManager, eventMapper);
        }

        public void setMockTransport(NetHttpTransport mockTransport) {
            this.mockTransport = mockTransport;
        }

        public void setMockClient(Calendar mockClient) {
            this.mockClient = mockClient;
        }
        
        public void setMockCalendarId(String id) {
            this.mockCalendarId = id;
        }

        @Override
        protected NetHttpTransport getHttpTransport() {
            return mockTransport;
        }

        @Override
        protected Calendar createCalendarClient(NetHttpTransport transport, com.google.api.client.auth.oauth2.Credential credential) {
            return mockClient;
        }
        
        @Override
        protected String verifyConnection(Calendar client) {
            return mockCalendarId;
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TestableGoogleCalendarServiceImpl(credentialManager, eventMapper);
        service.setCalendarClient(client); // Inject mock client for other tests
    }

    @Test
    void connect_ShouldReturnCalendarId_WhenSuccessful() throws Exception {
        // Arrange
        NetHttpTransport mockTransport = mock(NetHttpTransport.class);
        service.setMockTransport(mockTransport);
        service.setMockClient(client);
        service.setMockCalendarId("primary");
        
        when(credentialManager.getCredentials(mockTransport)).thenReturn(mock(com.google.api.client.auth.oauth2.Credential.class));

        // Act
        String result = service.connect();

        // Assert
        assertEquals("primary", result);
        assertTrue(service.isConnected());
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

    @Test
    void addEvent_ShouldThrowException_WhenClientIsNull() {
        // Arrange
        service.setCalendarClient(null);
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.addEvent(calendarEvent));
    }

    @Test
    void addEvent_ShouldThrowRuntimeException_WhenIOExceptionOccurs() throws IOException {
        // Arrange
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");
        Event googleEvent = new Event();
        
        when(eventMapper.mapToGoogleEvent(calendarEvent)).thenReturn(googleEvent);
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.insert(anyString(), any(Event.class))).thenReturn(insertRequest);
        when(insertRequest.execute()).thenThrow(new IOException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.addEvent(calendarEvent));
    }

    @Test
    void updateEvent_ShouldThrowException_WhenClientIsNull() {
        // Arrange
        service.setCalendarClient(null);
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.updateEvent(calendarEvent));
    }

    @Test
    void updateEvent_ShouldThrowRuntimeException_WhenIOExceptionOccurs() throws IOException {
        // Arrange
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test", "Desc", ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), new Location(), "1");
        
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.get(anyString(), anyString())).thenReturn(getRequest);
        when(getRequest.execute()).thenThrow(new IOException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.updateEvent(calendarEvent));
    }

    @Test
    void deleteEvent_ShouldThrowException_WhenClientIsNull() {
        // Arrange
        service.setCalendarClient(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> service.deleteEvent("123"));
    }

    @Test
    void deleteEvent_ShouldThrowRuntimeException_WhenIOExceptionOccurs() throws IOException {
        // Arrange
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.delete(anyString(), anyString())).thenReturn(deleteRequest);
        when(deleteRequest.execute()).thenThrow(new IOException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> service.deleteEvent("123"));
    }

    @Test
    void isConnected_ShouldReturnTrue_WhenClientIsNotNull() {
        // Act
        boolean result = service.isConnected();

        // Assert
        assertTrue(result);
    }

    @Test
    void isConnected_ShouldReturnFalse_WhenClientIsNull() {
        // Arrange
        service.setCalendarClient(null);

        // Act
        boolean result = service.isConnected();

        // Assert
        assertFalse(result);
    }

    @Test
    void getUpcomingEvents_ShouldReturnEmptyList_WhenIOExceptionOccurs() throws IOException {
        // Arrange
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.list(anyString())).thenReturn(listRequest);
        when(listRequest.setMaxResults(anyInt())).thenReturn(listRequest);
        when(listRequest.setTimeMin(any())).thenReturn(listRequest);
        when(listRequest.setOrderBy(anyString())).thenReturn(listRequest);
        when(listRequest.setSingleEvents(anyBoolean())).thenReturn(listRequest);
        when(listRequest.execute()).thenThrow(new IOException("API Error"));

        // Act
        List<CalendarEvent> result = service.getUpcomingEvents();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getUpcomingEvents_ShouldReturnEmptyList_WhenItemsAreNull() throws IOException {
        // Arrange
        when(client.events()).thenReturn(eventsResource);
        when(eventsResource.list(anyString())).thenReturn(listRequest);
        when(listRequest.setMaxResults(anyInt())).thenReturn(listRequest);
        when(listRequest.setTimeMin(any())).thenReturn(listRequest);
        when(listRequest.setOrderBy(anyString())).thenReturn(listRequest);
        when(listRequest.setSingleEvents(anyBoolean())).thenReturn(listRequest);

        Events events = new Events();
        events.setItems(null);
        when(listRequest.execute()).thenReturn(events);

        // Act
        List<CalendarEvent> result = service.getUpcomingEvents();

        // Assert
        assertTrue(result.isEmpty());
    }
}
