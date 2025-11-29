package com.aurora.climatesync.presenter;

import java.time.LocalDate;

public class WeatherViewModel {
    private final LocalDate date;
    private final String temperatureDisplay;
    private final String highLowDisplay;
    private final String condition;
    private final String conditionIcon;
    private final String precipitationDisplay;
    private final String windSpeedDisplay;

    public WeatherViewModel(LocalDate date, String temperatureDisplay, String highLowDisplay, 
                          String condition, String conditionIcon, 
                          String precipitationDisplay, String windSpeedDisplay) {
        this.date = date;
        this.temperatureDisplay = temperatureDisplay;
        this.highLowDisplay = highLowDisplay;
        this.condition = condition;
        this.conditionIcon = conditionIcon;
        this.precipitationDisplay = precipitationDisplay;
        this.windSpeedDisplay = windSpeedDisplay;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTemperatureDisplay() {
        return temperatureDisplay;
    }

    public String getHighLowDisplay() {
        return highLowDisplay;
    }

    public String getCondition() {
        return condition;
    }

    public String getConditionIcon() {
        return conditionIcon;
    }

    public String getPrecipitationDisplay() {
        return precipitationDisplay;
    }

    public String getWindSpeedDisplay() {
        return windSpeedDisplay;
    }
}
