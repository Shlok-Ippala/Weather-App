package com.aurora.climatesync.model;

import java.time.LocalDate;

public class WeatherForecast {

    private LocalDate date;
    private double maxTempareture;
    private double minTempareture;
    private String condition;
    private double precipitationChance;
    private double windSpeed;
    private Location location;

    // Constructors
    public WeatherForecast(LocalDate date, double maxTemperature, double minTemperature, String condition, double precipitationChance, double windSpeed) {
        this.date = date;
        this.maxTempareture = maxTemperature;
        this.minTempareture = minTemperature;
        this.condition = condition;
        this.precipitationChance = precipitationChance;
        this.windSpeed = windSpeed;
        // this.location = location;
    }


    // Getters
    public LocalDate getDate() {
        return date;
    }

    public double getMaxTemperature() {
        return maxTempareture;
    }

    public double getMinTemperature() {
        return minTempareture;
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

    public Location getLocation() {
        return location;
    }

    public boolean isRainLikely() {
        return precipitationChance >= 0.5;
    }

    @Override
    public String toString() {
        int rainLikely = (int) (precipitationChance * 100);
        /* String rainStatus = isRainLikely() ? "rain likely 🌧️" : "not rain likely ⛅️";
        return String.format("%s | %s | High: %.1fºC | Low: %.1fºC | Precipitation: %d%% (%s) | Wind: %.1f km/h",
                date, condition, maxTempareture, minTempareture, rainLikely, rainStatus, windSpeed); */
        String icon;
        switch (condition.toLowerCase()) {
            case "rainy":
                icon = "🌧️";
                break;
            case "sunny":
                icon = "☀️";
                break;
            case "cloudy":
                icon = "☁️";
                break;
            case "windy":
                icon = "💨";
                break;
            case "snow":
                icon = "🌨️";
                break;
            default:
                icon = "⛅️";
        }

        String rainStatus = isRainLikely() ? "rain likely 🌧️" : "not rain likely ⛅️";
        return String.format(
                "%s %ns\n" +
                "Condition          : %s %s\n" +
                "High / Low         : %.1fºC / %.1fºC\n" +
                "Precipitation      : %d%% (%s)\n" +
                "Wind Speed         : %.1f km/h",
                date, icon, condition,
                maxTempareture, minTempareture,
                rainLikely, rainStatus, windSpeed

        );
    }
}