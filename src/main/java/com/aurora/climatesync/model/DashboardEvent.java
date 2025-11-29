package com.aurora.climatesync.model;

public class DashboardEvent {
    private CalendarEvent calendarEvent;
    private WeatherForecast weatherForecast;
    private EventWeather eventWeather;

    public DashboardEvent(CalendarEvent calendarEvent, WeatherForecast weatherForecast) {
        this(calendarEvent, weatherForecast, null);
    }

    public DashboardEvent(CalendarEvent calendarEvent, WeatherForecast weatherForecast, EventWeather eventWeather) {
        this.calendarEvent = calendarEvent;
        this.weatherForecast = weatherForecast;
        this.eventWeather = eventWeather;
    }

    public CalendarEvent getCalendarEvent() {
        return calendarEvent;
    }

    public void setCalendarEvent(CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;
    }

    public WeatherForecast getWeatherForecast() {
        return weatherForecast;
    }

    public void setWeatherForecast(WeatherForecast weatherForecast) {
        this.weatherForecast = weatherForecast;
    }

    public EventWeather getEventWeather() {
        return eventWeather;
    }

    public void setEventWeather(EventWeather eventWeather) {
        this.eventWeather = eventWeather;
    }
}
