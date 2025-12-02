package com.aurora.climatesync.presenter;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.model.EventWeather;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.DashboardService;
import com.aurora.climatesync.util.WeatherIconMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DashboardPresenterTest {

    class FakeDashboardView implements DashboardContract.View {
        @Override public void showLoading(String msg) {}
        @Override public void showEvents(List<DashboardViewModel> events) {}
        @Override public void showError(String msg) {}
    }

    class FakeDashboardService implements DashboardService {
        private final List<DashboardEvent> events;

        FakeDashboardService(List<DashboardEvent> events) {
            this.events = events;
        }


        @Override
        public List<DashboardEvent> getDashboardEvents() {
            return List.of();
        }

        @Override
        public List<DashboardEvent> getDashboardEvents(int limit) {
            return events;
        }
    }

    class FakeCalendarService implements CalendarService {
        @Override
        public List<CalendarEvent> getUpcomingEvents() {
            return List.of();
        }

        @Override
        public List<CalendarEvent> getUpcomingEvents(int maxResults) {
            return List.of();
        }

        @Override
        public CalendarEvent addEvent(CalendarEvent event) {
            return null;
        }

        @Override
        public void updateEvent(CalendarEvent event) {

        }

        @Override
        public void deleteEvent(String eventId) {

        }

        @Override
        public String connect() throws Exception {
            return "";
        }

        @Override
        public boolean isConnected() {
            return false;
        }
        // no method needed for this test
    }

    @Test
    void testPresenterMapsWeatherCorrectly() {

        // 1. Fake CalendarEvent
        CalendarEvent calendarEvent = new CalendarEvent(
                "1",                       // eventID
                "Test Event",              // title
                "Test Description",        // description
                ZonedDateTime.now(),       // startTime
                ZonedDateTime.now().plusHours(1),  // endTime
                null,                      // eventLocation
                null                       // colorId
        );

        // 2. Fake WeatherForecast
        WeatherForecast weatherForecast = new WeatherForecast(
                LocalDate.now(),   // date
                20.0,              // maxTemperature
                10.0,              // minTemperature
                "rain",            // condition
                0.5,               // precipitationChance
                5.0,               // windSpeed
                1                  // weathercode
        );
        weatherForecast.setCurrentTemperature(18.5);

        // 3. Fake EventWeather
        EventWeather eventWeather = new EventWeather(
                18.5,        // temperature
                "rain",      // condition
                0.5,         // precipitationChance
                5.0          // windSpeed
        );

        // 4. DashboardEvent
        DashboardEvent event = new DashboardEvent(calendarEvent, weatherForecast, eventWeather);

        // 5. Presenter
        List<DashboardEvent> oneEvent = List.of(event);

        DashboardPresenter presenter = new DashboardPresenter(
                new FakeDashboardView(),
                new FakeDashboardService(oneEvent),
                new FakeCalendarService()
        );

        // 6. calling presenter
        DashboardViewModel vm = presenter.mapToViewModel(event);

        // 7. Assert
        assertEquals("/assets/weather-icons/showers_rain.svg.png", vm.getWeatherIcon());
        assertEquals("It is forecasted to rain. Don't forget your umbrella!", vm.getWeatherMessage());
        assertEquals("18.5°C", vm.getTemperatureDisplay());
    }
}
