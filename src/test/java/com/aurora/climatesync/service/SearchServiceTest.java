package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SearchServiceTest {

    @Mock
    private WeatherService weatherService;

    private SearchService searchService;

    // Subclass to override makeHttpRequest
    private class TestableSearchService extends SearchService {
        private JSONObject mockResponse;
        private boolean shouldThrowException = false;
        private String lastUrl;

        public void setMockResponse(JSONObject mockResponse) {
            this.mockResponse = mockResponse;
        }

        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }
        
        public String getLastUrl() {
            return lastUrl;
        }

        @Override
        protected JSONObject makeHttpRequest(String urlString) throws Exception {
            this.lastUrl = urlString;
            if (shouldThrowException) {
                throw new Exception("Network error");
            }
            return mockResponse;
        }
    }

    private TestableSearchService testableSearchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testableSearchService = new TestableSearchService();
        // Inject the mock WeatherService
        ReflectionTestUtils.setField(testableSearchService, "weatherService", weatherService);
        searchService = testableSearchService;
    }

    @Test
    void testSearchLocations_NullQuery() {
        List<Location> results = searchService.searchLocations(null, 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLocations_EmptyQuery() {
        List<Location> results = searchService.searchLocations("   ", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLocations_VirtualQuery() {
        List<Location> results = searchService.searchLocations("virtual meeting", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLocations_OnlineQuery() {
        List<Location> results = searchService.searchLocations("online event", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLocations_ApiError() {
        testableSearchService.setShouldThrowException(true);
        List<Location> results = searchService.searchLocations("Toronto", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLocations_NoResultsField() throws Exception {
        JSONObject json = new JSONObject();
        testableSearchService.setMockResponse(json);

        List<Location> results = searchService.searchLocations("Nowhere", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchLocations_Success() throws Exception {
        JSONObject json = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        
        JSONObject loc1 = new JSONObject();
        loc1.put("name", "Toronto");
        loc1.put("country", "Canada");
        loc1.put("latitude", 43.7);
        loc1.put("longitude", -79.4);
        resultsArray.put(loc1);

        JSONObject loc2 = new JSONObject();
        loc2.put("name", "Toronto");
        loc2.put("country", "USA");
        loc2.put("latitude", 40.0);
        loc2.put("longitude", -80.0);
        resultsArray.put(loc2);

        json.put("results", resultsArray);
        testableSearchService.setMockResponse(json);

        List<Location> results = searchService.searchLocations("Toronto", 10);
        assertEquals(2, results.size());
        assertEquals("Toronto", results.get(0).getCityName());
        assertEquals("Canada", results.get(0).getCountry());
        assertEquals(43.7, results.get(0).getLatitude());
    }

    @Test
    void testSearchLocations_ZeroCoordinates() throws Exception {
        JSONObject json = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        
        JSONObject loc1 = new JSONObject();
        loc1.put("name", "Unknown Place");
        loc1.put("country", "Unknown");
        // No lat/lon or 0.0
        loc1.put("latitude", 0.0);
        loc1.put("longitude", 0.0);
        resultsArray.put(loc1);

        json.put("results", resultsArray);
        testableSearchService.setMockResponse(json);

        List<Location> results = searchService.searchLocations("Unknown", 10);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isUnknown());
    }

    @Test
    void testGetTopLocation_Found() throws Exception {
        JSONObject json = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        
        JSONObject loc1 = new JSONObject();
        loc1.put("name", "Paris");
        loc1.put("country", "France");
        loc1.put("latitude", 48.85);
        loc1.put("longitude", 2.35);
        resultsArray.put(loc1);

        json.put("results", resultsArray);
        testableSearchService.setMockResponse(json);

        Location loc = searchService.getTopLocation("Paris");
        assertEquals("Paris", loc.getCityName());
    }

    @Test
    void testGetTopLocation_NotFound() throws Exception {
        JSONObject json = new JSONObject();
        // No results
        testableSearchService.setMockResponse(json);

        Location loc = searchService.getTopLocation("Atlantis");
        assertTrue(loc.isUnknown());
    }

    @Test
    void testSearchAndGetWeeklyForecast_Found() throws Exception {
        // Mock search response
        JSONObject json = new JSONObject();
        JSONArray resultsArray = new JSONArray();
        JSONObject loc1 = new JSONObject();
        loc1.put("name", "London");
        loc1.put("country", "UK");
        loc1.put("latitude", 51.5);
        loc1.put("longitude", -0.1);
        resultsArray.put(loc1);
        json.put("results", resultsArray);
        testableSearchService.setMockResponse(json);

        // Mock weather service response
        List<WeatherForecast> mockForecasts = new ArrayList<>();
        mockForecasts.add(new WeatherForecast(java.time.LocalDate.now(), 20.0, 10.0, "Sunny", 0.0, 5.0));
        when(weatherService.getWeeklyForecast(any(Location.class))).thenReturn(mockForecasts);

        List<WeatherForecast> result = searchService.searchAndGetWeeklyForecast("London");
        assertEquals(1, result.size());
        verify(weatherService).getWeeklyForecast(any(Location.class));
    }

    @Test
    void testSearchAndGetWeeklyForecast_NotFound() throws Exception {
        // Mock search response (empty)
        JSONObject json = new JSONObject();
        testableSearchService.setMockResponse(json);

        List<WeatherForecast> result = searchService.searchAndGetWeeklyForecast("Nowhere");
        assertTrue(result.isEmpty());
        verify(weatherService, never()).getWeeklyForecast(any(Location.class));
    }

    @Test
    void testSearchLocations_UrlFormat() throws Exception {
        JSONObject json = new JSONObject();
        json.put("results", new JSONArray());
        testableSearchService.setMockResponse(json);

        searchService.searchLocations("New York", 10);
        
        String url = testableSearchService.getLastUrl();
        assertNotNull(url);
        assertTrue(url.contains("name=new+york") || url.contains("name=new%20york"), "URL should contain name parameter with encoded space: " + url);
        assertFalse(url.contains("q="), "URL should not contain 'q' parameter");
    }
}
