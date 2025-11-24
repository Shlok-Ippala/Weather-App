package com.aurora.climatesync.view;

import com.aurora.climatesync.ClimatesyncApplication;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.WeatherService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

@Component
public class Main {

    private final WeatherService weatherService;
    private final CalendarService calendarService;

    public Main(WeatherService weatherService, CalendarService calendarService) {
        this.weatherService = weatherService;
        this.calendarService = calendarService;
    }

    private void launchUI() {

        JFrame frame = new JFrame("ClimateSync - Barebones Weather");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(520, 400);
        frame.setLocationRelativeTo(null);

        // TOP PANEL
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cityField = new JTextField("Toronto", 10);
        JTextField countryField = new JTextField("Canada", 10);
        JButton fetchButton = new JButton("Fetch Forecast");
        JButton connectCalendarButton = new JButton("Connect to Google Calendar");

        inputPanel.add(new JLabel("City:"));
        inputPanel.add(cityField);
        inputPanel.add(new JLabel("Country:"));
        inputPanel.add(countryField);
        inputPanel.add(fetchButton);
        inputPanel.add(connectCalendarButton);

        // OLD OUTPUT
        /*
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        */

        // REPLACE TEXTAREA WITH A FORECAST PANEL
        JPanel forecastPanel = new JPanel();
        forecastPanel.setLayout(new BoxLayout(forecastPanel, BoxLayout.Y_AXIS));
        forecastPanel.setBackground(new Color(220, 235, 255)); // light blue


        // Status bar
        JLabel statusLabel = new JLabel("Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Layout all parts
        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);

        // OLD → frame.add(scrollPane, BorderLayout.CENTER);
        // frame.add(scrollPane, BorderLayout.CENTER);

        // NEW PANEL
        frame.add(forecastPanel, BorderLayout.CENTER);

        frame.add(statusLabel, BorderLayout.SOUTH);


        // FETCH BUTTON

        fetchButton.addActionListener((ActionEvent e) -> {

            String city = cityField.getText().trim();
            String country = countryField.getText().trim();

            if (city.isEmpty() || country.isEmpty()) {
                statusLabel.setText("Please enter both city and country.");
                return;
            }

            statusLabel.setText("Fetching...");

            Location location = new Location(city, country, 43.6532, -79.3832);

            List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(location);

            /*
            StringBuilder sb = new StringBuilder();
            sb.append("=== 7-Day Weather Forecast for ")
              .append(location).append(" ===\n\n");

            for (WeatherForecast wf : forecasts) {
                sb.append(wf).append("\n\n");
            }

            outputArea.setText(sb.toString());
            */

            renderForecast(forecastPanel, location, forecasts);
            // <<< NEW

            statusLabel.setText("Done.");
        });

        connectCalendarButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    String calendarId = calendarService.connect();
                    JOptionPane.showMessageDialog(frame, "Connected! Primary Calendar ID: " + calendarId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Failed to connect: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        });

        // Auto fetch on startup
        fetchButton.doClick();

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ClimatesyncApplication.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            Main app = context.getBean(Main.class);
            app.launchUI();
        });
    }

    // New Layout
    private void renderForecast(JPanel panel, Location location, List<WeatherForecast> forecasts) {

        panel.removeAll();

        // City
        JLabel cityLabel = new JLabel(location.getCityName());
        cityLabel.setFont(new Font("Arial", Font.BOLD, 28));
        cityLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(cityLabel);

        // Today - temp
        WeatherForecast today = forecasts.get(0);
        JLabel todayTemp = new JLabel((int) today.getMaxTemperature() + "°C");
        todayTemp.setFont(new Font("Arial", Font.BOLD, 48));
        todayTemp.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        panel.add(todayTemp);

        panel.add(Box.createVerticalStrut(20));

        // 7 Day List
        for (WeatherForecast wf : forecasts) {

            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
            row.setOpaque(false);

            JLabel dateLabel = new JLabel(wf.getDate().toString());
            JLabel iconLabel = new JLabel(wf.getConditionIcon());
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
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 10));
        statsPanel.setOpaque(false);

        // Wind Speed Box
        JPanel windBox = new JPanel();
        windBox.setPreferredSize(new Dimension(150, 80));
        windBox.setBackground(new Color(255, 220, 220));
        windBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        windBox.setLayout(new BoxLayout(windBox, BoxLayout.Y_AXIS));

        JLabel windTitle = new JLabel("Wind Speed");
        windTitle.setFont(new Font("Arial", Font.BOLD, 16));
        windTitle.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JLabel windValue = new JLabel(((int) today.getWindSpeed()) + " km/h");
        windValue.setFont(new Font("Arial", Font.BOLD, 22));
        windValue.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        windBox.add(Box.createVerticalStrut(5));
        windBox.add(windTitle);
        windBox.add(windValue);

        // Humidity Box
        JPanel humidityBox = new JPanel();
        humidityBox.setPreferredSize(new Dimension(150, 80));
        humidityBox.setBackground(new Color(255, 220, 220));
        humidityBox.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        humidityBox.setLayout(new BoxLayout(humidityBox, BoxLayout.Y_AXIS));

        JLabel humidityTitle = new JLabel("Humidity");
        humidityTitle.setFont(new Font("Arial", Font.BOLD, 16));
        humidityTitle.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        JLabel humidityValue = new JLabel((int) (today.getPrecipitationChance() * 100) + "%");
        humidityValue.setFont(new Font("Arial", Font.BOLD, 22));
        humidityValue.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);

        humidityBox.add(Box.createVerticalStrut(5));
        humidityBox.add(humidityTitle);
        humidityBox.add(humidityValue);

        statsPanel.add(windBox);
        statsPanel.add(humidityBox);

        panel.add(statsPanel);

        panel.revalidate();
        panel.repaint();
        System.out.println("Wind: " + today.getWindSpeed());
        System.out.println("Humidity: " + today.getPrecipitationChance());
    }
}
