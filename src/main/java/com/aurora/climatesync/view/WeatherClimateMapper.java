package com.aurora.climatesync.view;

import java.util.Map;

public class WeatherClimateMapper {

    private static final String DEFAULT_ICON = "not_available.svg.png";

    private static final Map<Integer, String> CODE_TO_ICON = Map.ofEntries(

            Map.entry(0, "clear_day.png"),
            Map.entry(1, "clear_day.png"),
            Map.entry(2, "partly_cloudy_day.svg.png"),
            Map.entry(3, "cloudy.png"),

            // Fog
            Map.entry(45, "haze_fog_dust_smoke.png"),
            Map.entry(48, "haze_fog_dust_smoke.png"),

            // Drizzle
            Map.entry(51, "drizzle.png"),
            Map.entry(53, "drizzle.png"),
            Map.entry(55, "drizzle.png"),

            // Freezing drizzle / freezing rain
            Map.entry(56, "drizzle.png"),
            Map.entry(57, "drizzle.png"),
            Map.entry(66, "heavy_rain.png"),
            Map.entry(67, "heavy_rain.png"),

            // Rain
            Map.entry(61, "showers_rain.svg.png"),
            Map.entry(63, "heavy_rain.png"),
            Map.entry(65, "heavy_rain.png."),

            // Snow
            Map.entry(71, "snow.svg.png"),
            Map.entry(73, "snow.svg.png"),
            Map.entry(75, "heavy_snow.png"),
            Map.entry(77, "snow.svg.png"),

            // Showers
            Map.entry(80, "showers_rain.svg.png"),
            Map.entry(81, "heavy_rain.png"),
            Map.entry(82, "heavy_rain.png"),

            Map.entry(85, "showers_snow.svg.png"),
            Map.entry(86, "heavy_snow.png"),

            // Thunderstorms
            Map.entry(95, "strong_thunderstorms.svg.png"),
            Map.entry(96, "strong_thunderstorms.svg.png"),
            Map.entry(99, "strong_thunderstorms.svg.png")
    );

    public static String getIcon(int weatherCode) {
        return CODE_TO_ICON.getOrDefault(weatherCode, DEFAULT_ICON);
    }
}
