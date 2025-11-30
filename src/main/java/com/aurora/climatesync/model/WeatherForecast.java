package com.aurora.climatesync.model;

import com.aurora.climatesync.view.WeatherClimateMapper;

import java.time.LocalDate;

public class WeatherForecast {

    private LocalDate date;
    private double maxTemperature;
    private double minTemperature;
    private String condition;
    private double precipitationChance;   // 0.0–1.0
    private double windSpeed;
    private String conditionIcon;
    private int weatherCode;
    private String iconPath;


    public WeatherForecast(LocalDate date,
                           double maxTemperature,
                           double minTemperature,
                           String condition,
                           double precipitationChance,
                           double windSpeed,
                           int weatherCode
    ) {

        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.condition = condition;
        this.precipitationChance = precipitationChance;
        this.windSpeed = windSpeed;
        this.weatherCode = weatherCode;

        this.iconPath = WeatherClimateMapper.getIcon(weatherCode);
    }

    public String getIconPath() {
        return this.iconPath;
    }

    // GETTERS

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

    public String getConditionIcon() {
        return conditionIcon;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    @Override
    public String toString() {
        return date + " " + condition + " " + conditionIcon +
                " H:" + maxTemperature + "°  L:" + minTemperature + "°";
    }
}