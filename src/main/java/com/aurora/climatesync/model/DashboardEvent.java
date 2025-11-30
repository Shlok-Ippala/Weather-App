package com.aurora.climatesync.model;

public class DashboardEvent {
    private CalendarEvent calendarEvent;
    private WeatherForecast weatherForecast;
    private int weatherCode;

    public DashboardEvent(CalendarEvent calendarEvent, WeatherForecast weatherForecast, int weatherCode) {
        this.calendarEvent = calendarEvent;
        this.weatherForecast = weatherForecast;
        this.weatherCode = weatherCode;
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

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }
}
