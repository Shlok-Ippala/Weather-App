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
    private int weathercode;
    private String iconPath;

    public WeatherForecast(LocalDate date,
                           double maxTemperature,
                           double minTemperature,
                           String condition,
                           double precipitationChance,
                           double windSpeed,
                           int weathercode) {
        this(date, maxTemperature, minTemperature, condition, precipitationChance, windSpeed, null, weathercode);
    }

    public WeatherForecast(LocalDate date,
                           double maxTemperature,
                           double minTemperature,
                           String condition,
                           double precipitationChance,
                           double windSpeed,
                           Double currentTemperature,
                           int weathercode) {

        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.condition = condition;
        this.precipitationChance = precipitationChance;
        this.windSpeed = windSpeed;
        this.currentTemperature = currentTemperature;
        this.weathercode = weathercode;
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

    public int getWeathercode() {
        return weathercode;
    }

    public void setWeathercode(int weathercode) {
        this.weathercode = weathercode;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
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