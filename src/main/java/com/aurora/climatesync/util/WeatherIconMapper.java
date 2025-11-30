package com.aurora.climatesync.util;

public class WeatherIconMapper {
    public static String getIconForCondition(String condition) {
        if (condition == null) {
            return "\u2753"; // ❓
        }

        switch (condition.toLowerCase()) {
            case "rain":
            case "rainy":
            case "drizzle":
            case "showers":
                return "\uD83C\uDF27"; // 🌧️

            case "sunny":
            case "clear":
                return "\u2600"; // ☀️

            case "cloudy":
            case "partly cloudy":
            case "overcast":
                return "\u2601"; // ☁️

            case "windy":
                return "\uD83D\uDCA8"; // 💨

            case "snow":
            case "snowy":
                return "\u2744"; // ❄️

            case "storm":
            case "thunderstorm":
                return "\u26C8"; // ⛈️

            case "fog":
            case "foggy":
                return "\uD83C\uDF2B"; // 🌫️

            default:
                return "\uD83C\uDF24"; // 🌤️
        }
    }
}