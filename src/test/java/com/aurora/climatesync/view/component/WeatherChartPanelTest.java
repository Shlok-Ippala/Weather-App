package com.aurora.climatesync.view.component;

import com.aurora.climatesync.model.WeatherForecast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
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
    void testUpdateChartWithValidData() {
        List<WeatherForecast> forecasts = createSampleForecasts(7);
        
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
    void testUpdateChartWithSingleDay() {
        List<WeatherForecast> forecasts = createSampleForecasts(1);
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithExtremeTemperatures() {
        List<WeatherForecast> forecasts = Arrays.asList(
            new WeatherForecast(LocalDate.now(), 50.0, -30.0, "Extreme", 0.5, 100.0),
            new WeatherForecast(LocalDate.now().plusDays(1), 45.0, -25.0, "Extreme", 0.8, 80.0)
        );
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithZeroPrecipitation() {
        List<WeatherForecast> forecasts = Arrays.asList(
            new WeatherForecast(LocalDate.now(), 25.0, 15.0, "Sunny", 0.0, 5.0),
            new WeatherForecast(LocalDate.now().plusDays(1), 26.0, 16.0, "Clear", 0.0, 3.0)
        );
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testUpdateChartWithFullPrecipitation() {
        List<WeatherForecast> forecasts = Arrays.asList(
            new WeatherForecast(LocalDate.now(), 20.0, 15.0, "Rain", 1.0, 20.0),
            new WeatherForecast(LocalDate.now().plusDays(1), 18.0, 14.0, "Storm", 1.0, 40.0)
        );
        
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(forecasts);
            });
        });
    }

    @Test
    void testChartPanelPreferredSize() {
        // Chart panel should have a reasonable preferred size
        Dimension prefSize = chartPanel.getPreferredSize();
        assertTrue(prefSize.width > 0, "Preferred width should be positive");
        assertTrue(prefSize.height > 0, "Preferred height should be positive");
    }

    @Test
    void testMultipleChartUpdates() {
        // Test that the chart can be updated multiple times without issues
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                chartPanel.updateChart(createSampleForecasts(3));
                chartPanel.updateChart(createSampleForecasts(7));
                chartPanel.updateChart(null);
                chartPanel.updateChart(createSampleForecasts(5));
            });
        });
    }

    /**
     * Helper method to create sample weather forecasts for testing.
     */
    private List<WeatherForecast> createSampleForecasts(int days) {
        List<WeatherForecast> forecasts = new ArrayList<>();
        LocalDate startDate = LocalDate.now();
        
        for (int i = 0; i < days; i++) {
            double maxTemp = 20.0 + (i * 2) + Math.random() * 5;
            double minTemp = 10.0 + (i * 2) + Math.random() * 3;
            double precip = Math.random();
            double wind = 5.0 + Math.random() * 20;
            
            forecasts.add(new WeatherForecast(
                startDate.plusDays(i),
                maxTemp,
                minTemp,
                i % 2 == 0 ? "Sunny" : "Cloudy",
                precip,
                wind
            ));
        }
        
        return forecasts;
    }
}

