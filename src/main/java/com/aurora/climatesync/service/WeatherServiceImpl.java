package com.aurora.climatesync.service;

import com.aurora.climatesync.exception.LocationNotFoundException;
import com.aurora.climatesync.model.EventWeather;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;

    @Autowired
    public WeatherServiceImpl(WeatherRepository weatherRepository) {
        this.weatherRepository = weatherRepository;
    }

    @Override
    public List<WeatherForecast> getWeeklyForecast(double latitude, double longitude) {
        return weatherRepository.fetchWeeklyForecast(latitude, longitude);
    }

    @Override
    public List<WeatherForecast> getWeeklyForecast(Location location) {
        if ((location.getLatitude() == 0.0 && location.getLongitude() == 0.0)
                && location.getCityName() != null && !location.getCityName().isEmpty()
                && !location.getCityName().equalsIgnoreCase("Unknown")) {
            weatherRepository.resolveLocation(location);
        }

        if (location.getLatitude() == 0.0 && location.getLongitude() == 0.0) {
            throw new LocationNotFoundException("Could not find location: " + location.getCityName());
        }

        return getWeeklyForecast(location.getLatitude(), location.getLongitude());
    }

    @Override
    public WeatherForecast getForecastForDate(Location location, LocalDate date) {
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
             if (location.getCityName() != null && !location.getCityName().equals("Unknown") && !location.getCityName().isEmpty()) {
                 weatherRepository.resolveLocation(location);
             } else {
                 return null;
             }
        }
        
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
            return null;
        }

        List<WeatherForecast> weekly = getWeeklyForecast(location.getLatitude(), location.getLongitude());

        for (WeatherForecast f : weekly) {
            if (f.getDate().equals(date)) {
                return f;
            }
        }
        return null; 
    }

    @Override
    public EventWeather getForecastForTime(Location location, ZonedDateTime time) {
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
             if (location.getCityName() != null && !location.getCityName().equals("Unknown") && !location.getCityName().isEmpty()) {
                 weatherRepository.resolveLocation(location);
             } else {
                 return null;
             }
        }
        
        if (location.getLatitude() == 0 && location.getLongitude() == 0) {
            return null;
        }

        return weatherRepository.fetchForecastForTime(location.getLatitude(), location.getLongitude(), time);
    }
}

