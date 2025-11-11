package com.aurora.climatesync.model;

import java.util.Objects;

public class Location {
    private String cityName;
    private String country;

    public Location(){
    }

    public Location(String cityName, String country) {
        this.cityName = cityName;
        this.country = country;
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

    @Override
    public String toString() {
        return cityName + ", " + country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location that = (Location) o;
        return cityName.equalsIgnoreCase(that.cityName) && country.equalsIgnoreCase(that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityName.toLowerCase(), country.toLowerCase());
    }
}
