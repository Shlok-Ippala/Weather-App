package com.aurora.climatesync.presenter;

import com.aurora.climatesync.model.WeatherForecast;

import java.awt.Color;
import java.time.ZonedDateTime;

public class DashboardViewModel {
    private final String eventId;
    private final String title;
    private final String description;
    private final String location;
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;
    private final String colorId;
    private final String weatherIcon;
    private final String temperatureDisplay;
    private final String weatherMessage;
    private final Object sourceEvent; // The original event object for actions
    private final int weatherCode;
    private final WeatherForecast weatherForecast;

    public DashboardViewModel(String eventId, String title, String description, String location,
                              ZonedDateTime startTime, ZonedDateTime endTime, String colorId,
                              String weatherIcon, String temperatureDisplay, String weatherMessage,
                              Object sourceEvent, int weatherCode, WeatherForecast weatherForecast) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.colorId = colorId;
        this.weatherIcon = weatherIcon;
        this.temperatureDisplay = temperatureDisplay;
        this.weatherMessage = weatherMessage;
        this.sourceEvent = sourceEvent;
        this.weatherCode =  weatherCode;
        this.weatherForecast = weatherForecast;
    }

    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public ZonedDateTime getStartTime() { return startTime; }
    public ZonedDateTime getEndTime() { return endTime; }
    public String getColorId() { return colorId; }
    public String getWeatherIcon() { return weatherIcon; }
    public String getTemperatureDisplay() { return temperatureDisplay; }
    public String getWeatherMessage() { return weatherMessage; }
    public Object getSourceEvent() { return sourceEvent; }
    public int getWeatherCode() { return weatherCode; }
    public WeatherForecast getWeatherForecast() { return weatherForecast; }
}
