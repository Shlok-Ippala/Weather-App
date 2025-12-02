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

import static org.junit.jupiter.api.Assertions.*;

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
    void testPresenterInitialization() {
        DashboardPresenter presenter = new DashboardPresenter(
                new FakeDashboardView(),
                new FakeDashboardService(List.of()),
                new FakeCalendarService()
        );

        assertNotNull(presenter);
    }

    @Test
    void testPresenterMapsWeatherCorrectly() {
        CalendarEvent calendarEvent = new CalendarEvent("1", "Test Event", "Test Description",
                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1), null, null);

        WeatherForecast weatherForecast = new WeatherForecast(LocalDate.now(), 20.0, 10.0,
                "rain", 0.5, 5.0, 1);
        weatherForecast.setCurrentTemperature(18.5);

        EventWeather eventWeather = new EventWeather(18.5, "rain", 0.5, 5.0);

        DashboardEvent event = new DashboardEvent(calendarEvent, weatherForecast, eventWeather);

        List<DashboardEvent> oneEvent = List.of(event);

        DashboardPresenter presenter = new DashboardPresenter(
                new FakeDashboardView(),
                new FakeDashboardService(oneEvent),
                new FakeCalendarService()
        );

        DashboardViewModel vm = presenter.mapToViewModel(event);

        assertEquals("/assets/weather-icons/showers_rain.svg.png", vm.getWeatherIcon());
        assertEquals("It is forecasted to rain. Don't forget your umbrella!", vm.getWeatherMessage());
        assertEquals("18.5°C", vm.getTemperatureDisplay());
    }

    @Test
    void testPresenterHandlesNullWeatherForecast() {
        CalendarEvent calendarEvent = new CalendarEvent(
                "2", "Event", "desc",
                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
                null, null
        );

        DashboardEvent event = new DashboardEvent(calendarEvent, null, null);

        DashboardPresenter presenter = new DashboardPresenter(
                new FakeDashboardView(),
                new FakeDashboardService(List.of(event)),
                new FakeCalendarService()
        );

        DashboardViewModel vm = presenter.mapToViewModel(event);

        // should not crash
        assertNull(vm.getWeatherIcon());
        assertNull(vm.getWeatherMessage());
        assertNull(vm.getTemperatureDisplay());
    }

    @Test
    void testPresenterHandlesNullEventWeatherButHasForecast() {
        CalendarEvent calendarEvent = new CalendarEvent(
                "3", "Event", "desc",
                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
                null, null
        );

        WeatherForecast forecast = new WeatherForecast(
                LocalDate.now(),
                25, 15,
                "cloudy",
                0.1,
                3.0,
                2
        );
        forecast.setCurrentTemperature(22.0);

        DashboardEvent event = new DashboardEvent(calendarEvent, forecast, null);

        DashboardPresenter presenter = new DashboardPresenter(
                new FakeDashboardView(),
                new FakeDashboardService(List.of(event)),
                new FakeCalendarService()
        );

        DashboardViewModel vm = presenter.mapToViewModel(event);

        assertEquals("22.0°C", vm.getTemperatureDisplay());
        assertNotNull(vm.getWeatherIcon());
    }

    @Test
    void testPresenterHandlesMissingIconMapping() {
        CalendarEvent calendarEvent = new CalendarEvent(
                "5", "Event", "desc",
                ZonedDateTime.now(), ZonedDateTime.now().plusHours(1),
                null, null
        );

        WeatherForecast forecast = new WeatherForecast(LocalDate.now(), 10, 5, "unknown",
                0.2,
                2.0,
                999
        );
        forecast.setCurrentTemperature(8.0);

        DashboardEvent event = new DashboardEvent(calendarEvent, forecast, null);

        DashboardPresenter presenter = new DashboardPresenter(
                new FakeDashboardView(),
                new FakeDashboardService(List.of(event)),
                new FakeCalendarService()
        );

        DashboardViewModel vm = presenter.mapToViewModel(event);

        assertEquals("/assets/weather-icons/not_available.png", vm.getWeatherIcon());
    }
}
