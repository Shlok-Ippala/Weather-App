package com.aurora.climatesync.service;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
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
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    private Calendar client;

    private final String clientId;
    private final String clientSecret;

    public GoogleCalendarServiceImpl(
            @org.springframework.beans.factory.annotation.Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId,
            @org.springframework.beans.factory.annotation.Value("${spring.security.oauth2.client.registration.google.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        if (clientId == null || clientSecret == null || clientId.isEmpty() || clientSecret.isEmpty()) {
            throw new IOException("Client ID and Client Secret must be configured in application.properties.");
        }

        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details();
        details.setClientId(clientId);
        details.setClientSecret(clientSecret);

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
        clientSecrets.setInstalled(details);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return authorize(flow, receiver);
    }

    protected Credential authorize(GoogleAuthorizationCodeFlow flow, LocalServerReceiver receiver) throws IOException {
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Override
    public String connect() throws Exception {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(HTTP_TRANSPORT);
        this.client = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
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

            client.events().insert("primary", event).execute();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add event to Google Calendar", e);
        }
    }

    @Override
    public List<CalendarEvent> getUpcomingEvents() {
        if (client == null) {
            return Collections.emptyList();
        }
        try {
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

                    calendarEvents.add(new CalendarEvent(
                        event.getId(),
                        event.getSummary(),
                        event.getDescription(),
                        startTime,
                        endTime,
                        new Location("Unknown", "Unknown", 0, 0) // Location parsing is complex, skipping for now
                    ));
                }
            }
            return calendarEvents;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

