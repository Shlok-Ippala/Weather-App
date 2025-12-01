package com.aurora.climatesync.presenter;

import com.aurora.climatesync.model.WeatherForecast;

import java.util.List;

public interface WeatherContract {
    interface View {
        void showLoading(String message);
        void hideLoading();
        void showWeather(String city, List<WeatherViewModel> forecasts);
        void updateChart(List<WeatherForecast> forecasts);
        void showError(String message);
    }

    interface Presenter {
        void onSearch(String query);
        void onViewReady();
    }
}
