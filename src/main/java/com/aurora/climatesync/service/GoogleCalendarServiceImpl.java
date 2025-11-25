package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import com.google.api.services.calendar.model.EventDateTime;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarServiceImpl implements CalendarService {
    private static final String APPLICATION_NAME = "ClimateSync";

    private Calendar client;
    private final GoogleCredentialManager credentialManager;

    public GoogleCalendarServiceImpl(GoogleCredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    @Override
    public String connect() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = credentialManager.getCredentials(HTTP_TRANSPORT);
        this.client = new Calendar.Builder(HTTP_TRANSPORT, credentialManager.getJsonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Test call to verify token
        com.google.api.services.calendar.model.Calendar calendar = client.calendars().get("primary").execute();
        return calendar.getId();
    }

    @Override
    public void addEvent(CalendarEvent calendarEvent) {
        if (client == null) {
            throw new IllegalStateException("Calendar client is not connected.");
        }
        try {
            Event event = new Event()
                    .setSummary(calendarEvent.getSummary())
                    .setDescription(calendarEvent.getDescription());

            DateTime startDateTime = new DateTime(calendarEvent.getStartTime().toInstant().toEpochMilli());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(calendarEvent.getStartTime().getZone().toString());
            event.setStart(start);

            DateTime endDateTime = new DateTime(calendarEvent.getEndTime().toInstant().toEpochMilli());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(calendarEvent.getEndTime().getZone().toString());
            event.setEnd(end);

            if (calendarEvent.getEventLocation() != null) {
                event.setLocation(calendarEvent.getEventLocation().toString());
            }
            
            if (calendarEvent.getColorId() != null) {
                event.setColorId(calendarEvent.getColorId());
            }

            client.events().insert("primary", event).execute();
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
            
            event.setSummary(calendarEvent.getSummary());
            event.setDescription(calendarEvent.getDescription());
            
            if (calendarEvent.getColorId() != null) {
                event.setColorId(calendarEvent.getColorId());
            }

            DateTime startDateTime = new DateTime(calendarEvent.getStartTime().toInstant().toEpochMilli());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone(calendarEvent.getStartTime().getZone().toString());
            event.setStart(start);

            DateTime endDateTime = new DateTime(calendarEvent.getEndTime().toInstant().toEpochMilli());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone(calendarEvent.getEndTime().getZone().toString());
            event.setEnd(end);

            if (calendarEvent.getEventLocation() != null) {
                event.setLocation(calendarEvent.getEventLocation().toString());
            }

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
        if (client == null) {
            System.out.println("GoogleCalendarServiceImpl: Client is null (not connected). Returning empty list.");
            return Collections.emptyList();
        }
        try {
            System.out.println("GoogleCalendarServiceImpl: Fetching events...");
            DateTime now = new DateTime(System.currentTimeMillis());
            Events events = client.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            
            List<CalendarEvent> calendarEvents = new java.util.ArrayList<>();
            List<Event> items = events.getItems();
            if (items != null) {
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) start = event.getStart().getDate();
                    DateTime end = event.getEnd().getDateTime();
                    if (end == null) end = event.getEnd().getDate();

                    ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(start.getValue()), ZoneId.systemDefault());
                    ZonedDateTime endTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(end.getValue()), ZoneId.systemDefault());

                    String locationStr = event.getLocation();
                    if (locationStr == null) locationStr = "Unknown";

                    calendarEvents.add(new CalendarEvent(
                        event.getId(),
                        event.getSummary(),
                        event.getDescription(),
                        startTime,
                        endTime,
                        new Location(locationStr, "Unknown", 0, 0), // Coordinates to be resolved by WeatherService
                        event.getColorId()
                    ));
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

