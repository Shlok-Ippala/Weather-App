package com.aurora.climatesync.model;

public class EventWeather {
    private double temperature;
    private String condition;
    private double precipitationChance;
    private double windSpeed;

    public EventWeather(double temperature, String condition, double precipitationChance, double windSpeed) {
        this.temperature = temperature;
        this.condition = condition;
        this.precipitationChance = precipitationChance;
        this.windSpeed = windSpeed;
    }

    public double getTemperature() {
        return temperature;
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
}