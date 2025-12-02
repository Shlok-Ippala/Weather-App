package com.aurora.climatesync.service;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

class SearchServiceTest {

    @Mock
    private LocationRepository locationRepository;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        searchService = new SearchService(locationRepository);
    }

    @Test
    void searchLocation_nullQuery_returnsUnknown() {
        Location result = searchService.searchLocation(null);
        assertTrue(result.isUnknown());
    }

    @Test
    void searchLocation_emptyQuery_returnsUnknown() {
        Location result = searchService.searchLocation("   ");
        assertTrue(result.isUnknown());
    }

    @Test
    void searchLocation_validQuery_returnsFirstResult() {
        Location toronto = new Location("Toronto", "Canada", 43.7, -79.4);
        when(locationRepository.searchLocations("toronto", 1))
                .thenReturn(List.of(toronto));

        Location result = searchService.searchLocation("Toronto");

        assertEquals("Toronto", result.getCityName());
        assertFalse(result.isUnknown());
        verify(locationRepository).searchLocations("toronto", 1);
    }

    @Test
    void searchLocation_noResults_returnsUnknown() {
        when(locationRepository.searchLocations(anyString(), eq(1)))
                .thenReturn(Collections.emptyList());

        Location result = searchService.searchLocation("xyz123");

        assertTrue(result.isUnknown());
    }

    @Test
    void searchLocation_repositoryNotCalledOnEmptyInput() {
        searchService.searchLocation("");
        verify(locationRepository, never()).searchLocations(anyString(), anyInt());
    }

    @Test
    void searchLocation_queryWithComma_usesOnlyFirstPart() {
        Location toronto = new Location("Toronto", "Canada", 43.7, -79.4);

        when(locationRepository.searchLocations("toronto", 1))
                .thenReturn(List.of(toronto));

        Location result = searchService.searchLocation("Toronto, Canada");

        assertEquals("Toronto", result.getCityName());
        verify(locationRepository).searchLocations("toronto", 1);
    }
}
