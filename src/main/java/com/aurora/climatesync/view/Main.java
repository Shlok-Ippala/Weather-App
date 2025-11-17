package com.aurora.climatesync.view;

import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.WeatherService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class Main {
    private final WeatherService weatherService;

    public Main() {
        this.weatherService = new WeatherService();
    }

    private void launchUI() {
        JFrame frame = new JFrame("ClimateSync - Barebones Weather");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(520, 400);
        frame.setLocationRelativeTo(null);

        // Top panel for input
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField cityField = new JTextField("Toronto", 10);
        JTextField countryField = new JTextField("Canada", 10);
        JButton fetchButton = new JButton("Fetch Forecast");
        inputPanel.add(new JLabel("City:"));
        inputPanel.add(cityField);
        inputPanel.add(new JLabel("Country:"));
        inputPanel.add(countryField);
        inputPanel.add(fetchButton);

        // Text area for results
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Status bar
        JLabel statusLabel = new JLabel("Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        // Layout
        frame.setLayout(new BorderLayout());
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.SOUTH);

        // Action
        fetchButton.addActionListener((ActionEvent e) -> {
            String city = cityField.getText().trim();
            String country = countryField.getText().trim();
            if (city.isEmpty() || country.isEmpty()) {
                statusLabel.setText("Please enter both city and country.");
                return;
            }
            statusLabel.setText("Fetching...");
            outputArea.setText("");
            Location location = new Location(city, country, 0,0);
            List<WeatherForecast> forecasts = weatherService.getWeeklyForecast(location);
            StringBuilder sb = new StringBuilder();
            sb.append("=== 7-Day Weather Forecast for ").append(location).append(" ===\n\n");
            for (WeatherForecast wf : forecasts) {
                sb.append(wf).append("\n\n");
            }
            outputArea.setText(sb.toString());
            statusLabel.setText("Done.");
        });

        // Initial populate
        fetchButton.doClick();

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().launchUI());
    }
}