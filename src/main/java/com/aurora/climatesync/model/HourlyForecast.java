package com.aurora.climatesync.model;

import java.time.LocalDateTime;

/**
 * Represents weather data for a specific hour.
 */
public class HourlyForecast {
    private LocalDateTime dateTime;
    private double temperature;
    private double precipitationProbability;
    private String condition;
    private double windSpeed;

    public HourlyForecast(LocalDateTime dateTime, double temperature, 
                          double precipitationProbability, String condition, double windSpeed) {
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.precipitationProbability = precipitationProbability;
        this.condition = condition;
        this.windSpeed = windSpeed;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPrecipitationProbability() {
        return precipitationProbability;
    }

    public String getCondition() {
        return condition;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    @Override
    public String toString() {
        return "HourlyForecast{" +
                "dateTime=" + dateTime +
                ", temperature=" + temperature +
                ", precipitationProbability=" + precipitationProbability +
                ", condition='" + condition + '\'' +
                ", windSpeed=" + windSpeed +
                '}';
    }
}

