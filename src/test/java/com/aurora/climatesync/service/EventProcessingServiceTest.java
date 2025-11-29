package com.aurora.climatesync.service;

import com.aurora.climatesync.model.ProcessedEvent;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventProcessingServiceTest {

    private EventProcessingService eventProcessingService;

    @BeforeEach
    void setUp() {
        eventProcessingService = new EventProcessingServiceImpl();
    }

    @Test
    void testProcessEvents_ValidEvent() {
        Event event = new Event();
        event.setId("1");
        event.setSummary("Meeting");
        event.setLocation("Toronto, ON");
        
        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000-04:00"));
        event.setStart(start);
        
        EventDateTime end = new EventDateTime();
        end.setDateTime(new DateTime("2023-10-27T11:00:00.000-04:00"));
        event.setEnd(end);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);

        assertEquals(1, result.size());
        ProcessedEvent processed = result.get(0);
        assertEquals("1", processed.getEventID());
        assertEquals("Meeting", processed.getTitle());
        assertEquals("Toronto, ON", processed.getLocation());
        assertNotNull(processed.getStartTime());
        assertNotNull(processed.getEndTime());
    }

    @Test
    void testProcessEvents_NoLocation() {
        Event event = new Event();
        event.setId("2");
        event.setSummary("No Location Event");
        // Location is null

        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000-04:00"));
        event.setStart(start);
        event.setEnd(start);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);

        assertEquals(0, result.size());
    }

    @Test
    void testProcessEvents_EmptyLocation() {
        Event event = new Event();
        event.setId("3");
        event.setSummary("Empty Location Event");
        event.setLocation("   ");

        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000-04:00"));
        event.setStart(start);
        event.setEnd(start);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);

        assertEquals(0, result.size());
    }

    @Test
    void testProcessEvents_AllDayEvent() {
        Event event = new Event();
        event.setId("4");
        event.setSummary("All Day Event");
        event.setLocation("Toronto");

        EventDateTime start = new EventDateTime();
        start.setDate(new DateTime("2023-10-27")); // Date only, no time
        event.setStart(start);
        
        EventDateTime end = new EventDateTime();
        end.setDate(new DateTime("2023-10-28"));
        event.setEnd(end);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);

        assertEquals(0, result.size());
    }

    @Test
    void testProcessEvents_MessyLocation() {
        Event event = new Event();
        event.setId("5");
        event.setSummary("Messy Location");
        event.setLocation("  New York, NY  ");

        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000-04:00"));
        event.setStart(start);
        event.setEnd(start);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);

        assertEquals(1, result.size());
        assertEquals("New York, NY", result.get(0).getLocation());
    }
    
    @Test
    void testProcessEvents_NullList() {
        List<ProcessedEvent> result = eventProcessingService.processEvents(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testProcessEvents_FromMockJson() throws IOException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        InputStream in = getClass().getResourceAsStream("/mock-events.json");
        assertNotNull(in, "Mock JSON file not found");

        // Parse JSON array into List<Event>
        // Since Google's parser might expect a wrapper or specific token stream, 
        // and Event[] might not be directly supported by parse, let's try parsing as array.
        Event[] eventsArray = jsonFactory.fromInputStream(in, Event[].class);
        List<Event> rawEvents = Arrays.asList(eventsArray);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);

        // Should contain only the event with location (json-1)
        assertEquals(1, result.size());
        assertEquals("json-1", result.get(0).getEventID());
        assertEquals("Montreal, QC", result.get(0).getLocation());
    }

    @Test
    void testProcessEvents_NullStart() {
        Event event = new Event();
        event.setId("6");
        event.setSummary("Null Start");
        event.setLocation("Toronto");
        event.setStart(null);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);
        assertEquals(0, result.size());
    }

    @Test
    void testProcessEvents_NullEnd() {
        Event event = new Event();
        event.setId("7");
        event.setSummary("Null End");
        event.setLocation("Toronto");
        
        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000-04:00"));
        event.setStart(start);
        event.setEnd(null);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);
        assertEquals(1, result.size());
        assertNull(result.get(0).getEndTime());
    }

    @Test
    void testProcessEvents_NullEndDateTime() {
        Event event = new Event();
        event.setId("8");
        event.setSummary("Null End DateTime");
        event.setLocation("Toronto");
        
        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000-04:00"));
        event.setStart(start);
        
        EventDateTime end = new EventDateTime();
        // DateTime is null
        event.setEnd(end);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);
        assertEquals(1, result.size());
        assertNull(result.get(0).getEndTime());
    }

    @Test
    void testProcessEvents_WithTimeZone() {
        Event event = new Event();
        event.setId("9");
        event.setSummary("Timezone Event");
        event.setLocation("Toronto");
        
        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000Z"));
        start.setTimeZone("America/Toronto");
        event.setStart(start);
        event.setEnd(start);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);
        assertEquals(1, result.size());
        assertEquals("America/Toronto", result.get(0).getStartTime().getZone().toString());
    }

    @Test
    void testProcessEvents_InvalidTimeZone() {
        Event event = new Event();
        event.setId("10");
        event.setSummary("Invalid Timezone");
        event.setLocation("Toronto");
        
        EventDateTime start = new EventDateTime();
        start.setDateTime(new DateTime("2023-10-27T10:00:00.000Z"));
        start.setTimeZone("Invalid/Zone");
        event.setStart(start);
        event.setEnd(start);

        List<Event> rawEvents = new ArrayList<>();
        rawEvents.add(event);

        List<ProcessedEvent> result = eventProcessingService.processEvents(rawEvents);
        assertEquals(1, result.size());
        // Should fallback to system default or UTC, but definitely not crash
        assertNotNull(result.get(0).getStartTime());
    }
}
