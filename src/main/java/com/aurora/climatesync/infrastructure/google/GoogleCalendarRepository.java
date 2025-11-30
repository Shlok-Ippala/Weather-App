package com.aurora.climatesync.infrastructure.google;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.repository.CalendarRepository;
import org.springframework.stereotype.Repository;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Repository
public class GoogleCalendarRepository implements CalendarRepository {
    private static final String APPLICATION_NAME = "ClimateSync";

    Calendar client;
    private final GoogleCredentialManager credentialManager;
    private final GoogleEventMapper eventMapper;

    public GoogleCalendarRepository(GoogleCredentialManager credentialManager, GoogleEventMapper eventMapper) {
        this.credentialManager = credentialManager;
        this.eventMapper = eventMapper;
    }

    @Override
    public String connect() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = getHttpTransport();
        Credential credential = credentialManager.getCredentials(HTTP_TRANSPORT);
        this.client = createCalendarClient(HTTP_TRANSPORT, credential);

        // Test call to verify token
        return verifyConnection(client);
    }

    protected NetHttpTransport getHttpTransport() throws Exception {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    protected Calendar createCalendarClient(NetHttpTransport transport, Credential credential) {
        return new Calendar.Builder(transport, credentialManager.getJsonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    protected String verifyConnection(Calendar client) throws IOException {
        com.google.api.services.calendar.model.Calendar calendar = client.calendars().get("primary").execute();
        return calendar.getId();
    }

    @Override
    public CalendarEvent addEvent(CalendarEvent calendarEvent) {
        if (client == null) {
            throw new IllegalStateException("Calendar client is not connected.");
        }
        try {
            Event event = eventMapper.mapToGoogleEvent(calendarEvent);
            Event createdEvent = client.events().insert("primary", event).execute();
            return eventMapper.mapToCalendarEvent(createdEvent);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add event to Google Calendar", e);
        }
    }

    @Override
    public void updateEvent(CalendarEvent calendarEvent) {
        if (client == null) {
            throw new IllegalStateException("Calendar client is not connected.");
        }
        try {
            Event event = client.events().get("primary", calendarEvent.getEventID()).execute();
            eventMapper.updateGoogleEvent(event, calendarEvent);
            client.events().update("primary", event.getId(), event).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update event in Google Calendar", e);
        }
    }

    @Override
    public void deleteEvent(String eventId) {
        if (client == null) {
            throw new IllegalStateException("Calendar client is not connected.");
        }
        try {
            client.events().delete("primary", eventId).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete event from Google Calendar", e);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null;
    }

    @Override
    public List<CalendarEvent> getUpcomingEvents() {
        return getUpcomingEvents(25);
    }

    @Override
    public List<CalendarEvent> getUpcomingEvents(int maxResults) {
        if (client == null) {
            System.out.println("GoogleCalendarRepository: Client is null (not connected). Returning empty list.");
            return Collections.emptyList();
        }
        try {
            System.out.println("GoogleCalendarRepository: Fetching events...");
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = client.events().list("primary")
                    .setMaxResults(maxResults)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            
            List<CalendarEvent> calendarEvents = new java.util.ArrayList<>();
            List<Event> items = events.getItems();
            if (items != null) {
                for (Event event : items) {
                    calendarEvents.add(eventMapper.mapToCalendarEvent(event));
                }
            }
            return calendarEvents;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // For testing purposes
    void setCalendarClient(Calendar client) {
        this.client = client;
    }
}
