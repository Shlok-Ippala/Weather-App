package com.aurora.climatesync.controller;

import com.aurora.climatesync.service.CalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalendarController.class)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CalendarService calendarService;

    @TestConfiguration
    static class CalendarServiceTestConfig {
        @Bean
        CalendarService calendarService() {
            return Mockito.mock(CalendarService.class);
        }
    }

    @Test
    @WithMockUser
    void testGetEvents() throws Exception {
        when(calendarService.getUpcomingEvents()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());
    }
}
