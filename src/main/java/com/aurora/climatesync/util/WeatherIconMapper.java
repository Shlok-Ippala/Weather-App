package com.aurora.climatesync.util;

public class WeatherIconMapper {
    public static String getIconForCondition(String condition) {
        if (condition == null) {
            return "/assets/weather-icons/not_available.png"; // ❓
        }

        switch (condition.toLowerCase()) {
            case "rain":
            case "rainy":
            case "drizzle":
            case "showers":
                return "/assets/weather-icons/showers_rain.svg.png"; // 🌧️

            case "sunny":
            case "clear":
                return "/assets/weather-icons/clear_day.png"; // ☀️

            case "cloudy":
            case "partly cloudy":
            case "overcast":
                return "/assets/weather-icons/cloudy.png"; // ☁️

            case "windy":
                return "/assets/weather-icons/blizzard.png"; // 💨

            case "snow":
            case "snowy":
                return "/assets/weather-icons/showers_snow.svg.png"; // ❄️

            case "storm":
            case "thunderstorm":
                return "/assets/weather-icons/isolated_thunderstorms.svg.png"; // ⛈️

            case "fog":
            case "foggy":
                return "/assets/weather-icons/haze_fog_dust_smoke.png"; // 🌫️

            default:
                return "/assets/weather-icons/not_available.png"; // 🌤️
        }
    }
}
