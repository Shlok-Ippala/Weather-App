package com.aurora.climatesync.exception;

public class LocationNotFoundException extends WeatherServiceException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}