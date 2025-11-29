package com.aurora.climatesync.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeatherIconMapperTest {

    @Test
    void testGetIconForCondition() {
        assertEquals("\u2600", WeatherIconMapper.getIconForCondition("Sunny"));
        assertEquals("\u2600", WeatherIconMapper.getIconForCondition("Clear"));
        assertEquals("\u2601", WeatherIconMapper.getIconForCondition("Cloudy"));
        assertEquals("\uD83C\uDF27", WeatherIconMapper.getIconForCondition("Rain"));
        assertEquals("\u2744", WeatherIconMapper.getIconForCondition("Snow"));
        assertEquals("\u2753", WeatherIconMapper.getIconForCondition(null));
        assertEquals("\uD83C\uDF24", WeatherIconMapper.getIconForCondition("Unknown"));
    }
}
