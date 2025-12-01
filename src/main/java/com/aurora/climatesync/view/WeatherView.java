package com.aurora.climatesync.view;

import com.aurora.climatesync.model.HourlyForecast;
import com.aurora.climatesync.presenter.WeatherContract;
import com.aurora.climatesync.presenter.WeatherViewModel;
import com.aurora.climatesync.view.component.WeatherChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeatherView extends JPanel implements WeatherContract.View {
    private WeatherContract.Presenter presenter;
    private final JTextField searchField;
    private final JLabel statusLabel;
    private final JPanel contentPanel;
    private final WeatherChartPanel chartPanel;

    public WeatherView() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        // --- Header / Search Bar ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        headerPanel.setBackground(new Color(245, 245, 245));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));

        searchField = createStyledTextField("Location (e.g. Toronto, Canada)");
        searchField.setPreferredSize(new Dimension(300, 40));
        
        JButton searchButton = new JButton("Search");
        styleButton(searchButton);
        searchButton.addActionListener(e -> {
            if (presenter != null) {
                presenter.onSearch(searchField.getText());
            }
        });

        headerPanel.add(new JLabel("Location:"));
        headerPanel.add(searchField);
        headerPanel.add(searchButton);

        this.add(headerPanel, BorderLayout.NORTH);

        // --- Main Content Area ---
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        // Initialize the weather chart panel
        chartPanel = new WeatherChartPanel();
        chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chartPanel.setMaximumSize(new Dimension(750, 320));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);

        // --- Footer / Status ---
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        statusLabel.setForeground(Color.GRAY);
        this.add(statusLabel, BorderLayout.SOUTH);
    }

    public void setPresenter(WeatherContract.Presenter presenter) {
        this.presenter = presenter;
        // Notify presenter that view is ready
        presenter.onViewReady();
    }

    @Override
    public void showLoading(String message) {
        statusLabel.setText(message);
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    @Override
    public void hideLoading() {
        statusLabel.setText("Ready");
    }

    @Override
    public void showError(String message) {
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showWeather(String city, List<WeatherViewModel> forecasts) {
        contentPanel.removeAll();
        statusLabel.setText("Weather updated for " + city);

        if (forecasts == null || forecasts.isEmpty()) {
            JLabel errorLabel = new JLabel("No weather data found for " + city);
            errorLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.add(errorLabel);
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        // 1. Current Weather (Today)
        WeatherViewModel today = forecasts.get(0);
        JPanel currentPanel = new JPanel(new GridBagLayout());
        currentPanel.setBackground(Color.WHITE);
        currentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true),
            new EmptyBorder(20, 40, 20, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);

        // City Name
        JLabel cityLabel = new JLabel(city);
        cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        currentPanel.add(cityLabel, gbc);

        // Date
        gbc.gridy++;
        JLabel dateLabel = new JLabel(today.getDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d")));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dateLabel.setForeground(Color.GRAY);
        currentPanel.add(dateLabel, gbc);

        // Icon & Temp
        gbc.gridy++;
        JPanel tempPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        tempPanel.setOpaque(false);
        
        JLabel iconLabel = new JLabel(today.getConditionIcon());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        
        JLabel tempLabel = new JLabel(today.getTemperatureDisplay());
        tempLabel.setFont(new Font("Segoe UI", Font.BOLD, 64));
        
        tempPanel.add(iconLabel);
        tempPanel.add(tempLabel);
        currentPanel.add(tempPanel, gbc);

        // High/Low Label
        gbc.gridy++;
        JLabel hlLabel = new JLabel(today.getHighLowDisplay());
        hlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        hlLabel.setForeground(Color.DARK_GRAY);
        currentPanel.add(hlLabel, gbc);

        // Condition Text
        gbc.gridy++;
        JLabel conditionLabel = new JLabel(today.getCondition());
        conditionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        currentPanel.add(conditionLabel, gbc);

        // Details (Wind, Precip)
        gbc.gridy++;
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.add(createDetailLabel("Wind", today.getWindSpeedDisplay()));
        detailsPanel.add(createDetailLabel("Precip", today.getPrecipitationDisplay()));
        currentPanel.add(detailsPanel, gbc);

        contentPanel.add(currentPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // 2. Today's Hourly Chart (will be populated by updateChart)
        JLabel chartLabel = new JLabel("Today's Temperature");
        chartLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        chartLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(chartLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(chartPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // 3. Weekly Forecast
        JLabel weeklyLabel = new JLabel("7-Day Forecast");
        weeklyLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        weeklyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(weeklyLabel);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel weeklyPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        weeklyPanel.setOpaque(false);
        weeklyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Limit width
        weeklyPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));

        for (WeatherViewModel wf : forecasts) {
            weeklyPanel.add(createForecastRow(wf));
        }

        contentPanel.add(weeklyPanel);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    @Override
    public void updateChart(List<HourlyForecast> hourlyForecasts) {
        chartPanel.updateChart(hourlyForecasts);
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(15);
        field.setToolTipText(placeholder);
        return field;
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(66, 133, 244));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setOpaque(true);
        button.setBorderPainted(false);
    }

    private JLabel createDetailLabel(String title, String value) {
        JLabel label = new JLabel(title + ": " + value);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }

    private JPanel createForecastRow(WeatherViewModel wf) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(new Color(250, 250, 250));
        row.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // Day
        JLabel dayLabel = new JLabel(wf.getDate().format(DateTimeFormatter.ofPattern("EEEE")));
        dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dayLabel.setPreferredSize(new Dimension(100, 20));
        
        // Icon
        JLabel iconLabel = new JLabel(wf.getConditionIcon());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(50, 20));

        // Temps
        JLabel tempLabel = new JLabel(wf.getHighLowDisplay());
        tempLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tempLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(dayLabel, BorderLayout.WEST);
        row.add(iconLabel, BorderLayout.CENTER);
        row.add(tempLabel, BorderLayout.EAST);
        
        return row;
    }
}
