package com.aurora.climatesync.model;

import java.time.LocalDate;

public class WeatherForecast {

    private LocalDate date;
    private double maxTemperature;
    private double minTemperature;
    private String condition;
    private double precipitationChance;   // 0.0–1.0
    private double windSpeed;
    private Double currentTemperature; // Nullable, only for "Today"

    public WeatherForecast(LocalDate date,
                           double maxTemperature,
                           double minTemperature,
                           String condition,
                           double precipitationChance,
                           double windSpeed) {
        this(date, maxTemperature, minTemperature, condition, precipitationChance, windSpeed, null);
    }

    public WeatherForecast(LocalDate date,
                           double maxTemperature,
                           double minTemperature,
                           String condition,
                           double precipitationChance,
                           double windSpeed,
                           Double currentTemperature) {

        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.condition = condition;
        this.precipitationChance = precipitationChance;
        this.windSpeed = windSpeed;
        this.currentTemperature = currentTemperature;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public String getCondition() {
        return condition;
    }

    public double getPrecipitationChance() {
        return precipitationChance;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public Double getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(Double currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    @Override
    public String toString() {
        return "WeatherForecast{" +
                "date=" + date +
                ", maxTemperature=" + maxTemperature +
                ", minTemperature=" + minTemperature +
                ", condition='" + condition + '\'' +
                ", precipitationChance=" + precipitationChance +
                ", windSpeed=" + windSpeed +
                ", currentTemperature=" + currentTemperature +
                '}';
    }
}