package com.aurora.climatesync.view;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;
import com.aurora.climatesync.util.WeatherIconLoader;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class WeatherView extends JPanel {
    private final WeatherService weatherService;
    private final JTextField cityField;
    private final JTextField countryField;
    private final JLabel statusLabel;
    private final JPanel forecastPanel;

    public WeatherView(WeatherService weatherService) {
        this.weatherService = weatherService;
        this.setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        inputPanel.setBackground(new Color(245, 245, 245));
        inputPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        
        cityField = new JTextField("Toronto", 15);
        countryField = new JTextField("Canada", 15);
        JButton fetchButton = new JButton("Fetch Forecast");
        
        fetchButton.setBackground(new Color(66, 133, 244));
        fetchButton.setForeground(Color.BLACK);
        fetchButton.setOpaque(true);
        fetchButton.setBorderPainted(false);

        inputPanel.add(new JLabel("City:"));
        inputPanel.add(cityField);
        inputPanel.add(new JLabel("Country:"));
        inputPanel.add(countryField);
        inputPanel.add(fetchButton);

        // Forecast Panel
        forecastPanel = new JPanel();
        forecastPanel.setLayout(new BoxLayout(forecastPanel, BoxLayout.Y_AXIS));
        forecastPanel.setBackground(Color.WHITE);
        forecastPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Status Label
        statusLabel = new JLabel("Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        this.add(inputPanel, BorderLayout.NORTH);
        this.add(new JScrollPane(forecastPanel), BorderLayout.CENTER);
        this.add(statusLabel, BorderLayout.SOUTH);

        fetchButton.addActionListener(e -> fetchWeather());
        
        // Auto fetch on startup
        fetchWeather();
    }

    private void fetchWeather() {
        String city = cityField.getText().trim();
        String country = countryField.getText().trim();

        if (city.isEmpty() || country.isEmpty()) {
            statusLabel.setText("Please enter both city and country.");
            return;
        }

        statusLabel.setText("Fetching...");

        // Run in background to avoid freezing UI
        new Thread(() -> {
            try {
                Location location = new Location(city, country, 43.6532, -79.3832);
                List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(location);
                
                SwingUtilities.invokeLater(() -> {
                    renderForecast(forecastPanel, location, forecasts);
                    statusLabel.setText("Done.");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void renderForecast(JPanel panel, Location location, List<WeatherForecast> forecasts) {
        panel.removeAll();

        // City
        JLabel cityLabel = new JLabel(location.getCityName());
        cityLabel.setFont(new Font("Arial", Font.BOLD, 28));
        cityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(cityLabel);

        if (forecasts.isEmpty()) return;

        // Today - temp
        WeatherForecast today = forecasts.get(0);
        JLabel todayTemp = new JLabel((int) today.getMaxTemperature() + "°C");
        todayTemp.setFont(new Font("Arial", Font.BOLD, 48));
        todayTemp.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(todayTemp);

        panel.add(Box.createVerticalStrut(20));

        // 7 Day List
        for (WeatherForecast wf : forecasts) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setOpaque(false);

            JLabel dateLabel = new JLabel(wf.getDate().toString());
            String iconPath = WeatherClimateMapper.getIcon(wf.getWeatherCode());
            ImageIcon icon = WeatherIconLoader.load(iconPath);
            JLabel iconLabel = new JLabel(icon);
            JLabel hlLabel = new JLabel(
                    "H: " + (int) wf.getMaxTemperature() + "°   L: " + (int) wf.getMinTemperature() + "°"
            );

            row.add(dateLabel);
            row.add(Box.createHorizontalStrut(10));
            row.add(iconLabel);
            row.add(Box.createHorizontalStrut(10));
            row.add(hlLabel);

            panel.add(row);
        }
        
        panel.add(Box.createVerticalStrut(25));
        
        // Stats Panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));
        statsPanel.setOpaque(false);

        // Wind Speed Box
        JPanel windBox = createStatBox("Wind Speed", ((int) today.getWindSpeed()) + " km/h");
        statsPanel.add(windBox);

        // Humidity Box
        JPanel humidityBox = createStatBox("Humidity", (int) (today.getPrecipitationChance() * 100) + "%");
        statsPanel.add(humidityBox);

        panel.add(statsPanel);
        
        panel.revalidate();
        panel.repaint();
    }

    private JPanel createStatBox(String title, String value) {
        JPanel box = new JPanel();
        box.setPreferredSize(new Dimension(150, 80));
        box.setBackground(new Color(255, 220, 220));
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(Box.createVerticalStrut(5));
        box.add(titleLabel);
        box.add(valueLabel);
        return box;
    }
}
