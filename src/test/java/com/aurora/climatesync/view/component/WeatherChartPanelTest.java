package com.aurora.climatesync.view.component;

import com.aurora.climatesync.model.HourlyForecast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WeatherChartPanelTest {

    private WeatherChartPanel chartPanel;

    @BeforeEach
    void setUp() {
        // Run on EDT to avoid Swing threading issues
        try {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel = new WeatherChartPanel();
            });
        } catch (Exception e) {
            fail("Failed to initialize chart panel: " + e.getMessage());
        }
    }

    @Test
    void testChartPanelInitialization() {
        assertNotNull(chartPanel);
        assertEquals(BorderLayout.class, chartPanel.getLayout().getClass());
        assertTrue(chartPanel.getComponentCount() > 0, "Chart panel should have components");
    }

    @Test
    void testUpdateChartWithValidHourlyData() {
        List<HourlyForecast> forecasts = createSampleHourlyForecasts(24);
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithNullData() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(null);
            });
        });
    }

    @Test
    void testUpdateChartWithEmptyList() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(Collections.emptyList());
            });
        });
    }

    @Test
    void testUpdateChartWithSingleHour() {
        List<HourlyForecast> forecasts = createSampleHourlyForecasts(1);
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithExtremeTemperatures() {
        LocalDateTime now = LocalDateTime.now();
        List<HourlyForecast> forecasts = Arrays.asList(
            new HourlyForecast(now, 45.0, 0.5, "Hot", 5.0),
            new HourlyForecast(now.plusHours(1), -20.0, 0.8, "Cold", 10.0)
        );
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithZeroPrecipitation() {
        LocalDateTime now = LocalDateTime.now();
        List<HourlyForecast> forecasts = Arrays.asList(
            new HourlyForecast(now, 25.0, 0.0, "Sunny", 5.0),
            new HourlyForecast(now.plusHours(1), 26.0, 0.0, "Clear", 3.0)
        );
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithFullPrecipitation() {
        LocalDateTime now = LocalDateTime.now();
        List<HourlyForecast> forecasts = Arrays.asList(
            new HourlyForecast(now, 20.0, 1.0, "Rain", 20.0),
            new HourlyForecast(now.plusHours(1), 18.0, 1.0, "Storm", 40.0)
        );
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testChartPanelPreferredSize() {
        Dimension prefSize = chartPanel.getPreferredSize();
        assertTrue(prefSize.width > 0, "Preferred width should be positive");
        assertTrue(prefSize.height > 0, "Preferred height should be positive");
    }

    @Test
    void testMultipleChartUpdates() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(createSampleHourlyForecasts(6));
                chartPanel.updateChart(createSampleHourlyForecasts(24));
                chartPanel.updateChart(null);
                chartPanel.updateChart(createSampleHourlyForecasts(12));
            });
        });
    }

    /**
     * Helper method to create sample hourly forecasts for testing.
     */
    private List<HourlyForecast> createSampleHourlyForecasts(int hours) {
        List<HourlyForecast> forecasts = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0);
        
        for (int i = 0; i < hours; i++) {
            double temp = 15.0 + 10 * Math.sin(Math.PI * i / 12); // Simulates daily temp curve
            double precip = Math.random() * 0.5;
            double wind = 5.0 + Math.random() * 15;
            
            forecasts.add(new HourlyForecast(
                startTime.plusHours(i),
                temp,
                precip,
                i < 6 || i > 18 ? "Clear" : "Sunny",
                wind
            ));
        }
        
        return forecasts;
    }
}
