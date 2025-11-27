package com.aurora.climatesync.model;

import java.util.Objects;

public class Location {
    private String cityName;
    private String country;
    private double latitude;
    private double longitude;

    public Location(){
    }

    public Location(String cityName, String country,  double latitude, double longitude) {
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public static Location unknown() {
        return new Location("Unknown City", "Unknown Country", 0.0, 0.0);
    }

    public boolean isUnknown() {
        return latitude == 0.0 && longitude == 0.0;
    }

    @Override
    public String toString() {
        return cityName + ", " + country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location that = (Location) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(cityName, that.cityName) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        String city = cityName == null ? "" : cityName.toLowerCase();
        String ctry = country == null ? "" : country.toLowerCase();
        return Objects.hash(city, ctry, latitude, longitude);
    }
}
