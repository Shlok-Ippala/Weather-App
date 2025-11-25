package com.aurora.climatesync.model;

public class DashboardEvent {
    private CalendarEvent calendarEvent;
    private WeatherForecast weatherForecast;

    public DashboardEvent(CalendarEvent calendarEvent, WeatherForecast weatherForecast) {
        this.calendarEvent = calendarEvent;
        this.weatherForecast = weatherForecast;
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
}
