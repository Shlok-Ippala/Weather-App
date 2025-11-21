package com.aurora.climatesync.model;

import java.time.LocalDate;

public class WeatherForecast {

    private LocalDate date;
    private double maxTemperature;
    private double minTemperature;
    private String condition;
    private double precipitationChance;   // 0.0–1.0
    private double windSpeed;
    private String conditionIcon;


    public WeatherForecast(LocalDate date,
                           double maxTemperature,
                           double minTemperature,
                           String condition,
                           double precipitationChance,
                           double windSpeed) {

        this.date = date;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.condition = condition;
        this.precipitationChance = precipitationChance;
        this.windSpeed = windSpeed;

        switch (condition.toLowerCase()) {
            case "rain":
            case "rainy":
                this.conditionIcon = "🌧️";
                break;

            case "sunny":
                this.conditionIcon = "☀️";
                break;

            case "cloudy":
                this.conditionIcon = "☁️";
                break;

            case "windy":
                this.conditionIcon = "💨";
                break;

            case "snow":
                this.conditionIcon = "❄️";
                break;

            case "thunderstorm":
                this.conditionIcon = "⛈️";
                break;

            default:
                this.conditionIcon = "🌤️";   // partly cloudy default
                break;
        }
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

    @Override
    public String toString() {
        return date + " " + condition + " " + conditionIcon +
                " H:" + maxTemperature + "°  L:" + minTemperature + "°";
    }
}