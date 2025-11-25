package com.aurora.climatesync.view;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.DashboardEvent;
import com.aurora.climatesync.model.WeatherForecast;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.WeatherService;
import com.aurora.climatesync.util.EventColorUtil;
import com.aurora.climatesync.view.dialog.AddEventDialog;
import com.aurora.climatesync.view.dialog.EditEventDialog;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DashboardView extends JPanel {
    private final CalendarService calendarService;
    private final WeatherService weatherService;

    // UI Components
    private final JLabel statusLabel;
    private final JTextField searchField;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JComboBox<String> viewSelector;
    private final JLabel dateRangeLabel;

    // View Containers
    private final JPanel listViewContainer;
    private final JPanel dayViewContainer;
    private final JPanel weekViewContainer;
    private final JPanel monthViewContainer;

    // Data State
    private List<DashboardEvent> allEvents = new ArrayList<>();
    private LocalDate currentDate = LocalDate.now();
    
    public DashboardView(CalendarService calendarService, WeatherService weatherService) {
        this.calendarService = calendarService;
        this.weatherService = weatherService;
        this.setLayout(new BorderLayout());

        // --- Top Bar ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Left: Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search events...");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);

        // Center: Navigation & View Selection
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setOpaque(false);
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        
        // Style nav buttons
        prevButton.setFocusPainted(false);
        nextButton.setFocusPainted(false);

        String[] views = {"List All", "Day", "Week", "Month"};
        viewSelector = new JComboBox<>(views);
        viewSelector.setFocusable(false);

        dateRangeLabel = new JLabel(getDateLabel());
        dateRangeLabel.setPreferredSize(new Dimension(200, 25));
        dateRangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dateRangeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        navPanel.add(prevButton);
        navPanel.add(dateRangeLabel);
        navPanel.add(nextButton);
        navPanel.add(Box.createHorizontalStrut(15));
        navPanel.add(viewSelector);

        // Right: Refresh & Add Event
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton addEventButton = new JButton("+ Add Event");
        
        addEventButton.setBackground(new Color(66, 133, 244));
        addEventButton.setForeground(Color.BLACK);
        addEventButton.setOpaque(true);
        addEventButton.setBorderPainted(false);
        addEventButton.setFocusPainted(false);
        
        rightPanel.add(refreshButton);
        rightPanel.add(addEventButton);

        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(navPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        this.add(topPanel, BorderLayout.NORTH);

        // --- Center Content (CardLayout) ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Initialize View Containers
        listViewContainer = new JPanel();
        listViewContainer.setLayout(new BoxLayout(listViewContainer, BoxLayout.Y_AXIS));
        
        dayViewContainer = new JPanel();
        dayViewContainer.setLayout(new BoxLayout(dayViewContainer, BoxLayout.Y_AXIS));

        weekViewContainer = new JPanel(new GridLayout(1, 7));
        
        monthViewContainer = new JPanel(new BorderLayout());

        contentPanel.add(new JScrollPane(listViewContainer), "List All");
        contentPanel.add(new JScrollPane(dayViewContainer), "Day");
        contentPanel.add(new JScrollPane(weekViewContainer), "Week");
        contentPanel.add(new JScrollPane(monthViewContainer), "Month");

        this.add(contentPanel, BorderLayout.CENTER);

        // --- Bottom Status ---
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(statusLabel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        refreshButton.addActionListener(e -> loadDashboardData());
        addEventButton.addActionListener(e -> showAddEventDialog());

        viewSelector.addActionListener(e -> {
            String view = (String) viewSelector.getSelectedItem();
            cardLayout.show(contentPanel, view);
            updateView();
        });

        prevButton.addActionListener(e -> navigate(-1));
        nextButton.addActionListener(e -> navigate(1));

        // Initial Load
        loadDashboardData();
    }

    private void navigate(int direction) {
        String currentView = (String) viewSelector.getSelectedItem();
        if ("Day".equals(currentView)) {
            currentDate = currentDate.plusDays(direction);
        } else if ("Week".equals(currentView)) {
            currentDate = currentDate.plusWeeks(direction);
        } else if ("Month".equals(currentView)) {
            currentDate = currentDate.plusMonths(direction);
        }
        // List view doesn't really navigate by date, but we could scroll? 
        // For now, List view ignores navigation or maybe jumps to date.
        
        dateRangeLabel.setText(getDateLabel());
        updateView();
    }

    private String getDateLabel() {
        String currentView = (String) viewSelector.getSelectedItem();
        DateTimeFormatter dayFmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy");
        
        if ("Day".equals(currentView)) {
            return currentDate.format(dayFmt);
        } else if ("Week".equals(currentView)) {
            LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(6);
            return startOfWeek.format(DateTimeFormatter.ofPattern("MMM dd")) + " - " + endOfWeek.format(dayFmt);
        } else if ("Month".equals(currentView)) {
            return currentDate.format(monthFmt);
        }
        return "Upcoming Events";
    }

    private void loadDashboardData() {
        statusLabel.setText("Loading dashboard data...");
        
        if (!calendarService.isConnected()) {
            showConnectionError();
            return;
        }

        new SwingWorker<List<DashboardEvent>, Void>() {
            @Override
            protected List<DashboardEvent> doInBackground() throws Exception {
                List<CalendarEvent> events = calendarService.getUpcomingEvents();
                List<DashboardEvent> dashboardEvents = new ArrayList<>();

                for (CalendarEvent event : events) {
                    WeatherForecast forecast = null;
                    if (event.getEventLocation() != null) {
                        try {
                            forecast = weatherService.getForecastForDate(event.getEventLocation(), event.getStartTime().toLocalDate());
                        } catch (Exception e) {
                            System.err.println("Could not fetch weather for event: " + event.getSummary());
                        }
                    }
                    dashboardEvents.add(new DashboardEvent(event, forecast));
                }
                return dashboardEvents;
            }

            @Override
            protected void done() {
                try {
                    allEvents = get();
                    updateView();
                    statusLabel.setText("Dashboard updated. " + allEvents.size() + " events found.");
                } catch (InterruptedException | ExecutionException e) {
                    statusLabel.setText("Error loading data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void showConnectionError() {
        listViewContainer.removeAll();
        JLabel notConnectedLabel = new JLabel("Google Calendar is not connected.");
        notConnectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton connectButton = new JButton("Connect Now");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(e -> {
            try {
                calendarService.connect();
                loadDashboardData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });

        listViewContainer.add(Box.createVerticalGlue());
        listViewContainer.add(notConnectedLabel);
        listViewContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        listViewContainer.add(connectButton);
        listViewContainer.add(Box.createVerticalGlue());
        
        statusLabel.setText("Please connect to Google Calendar.");
        listViewContainer.revalidate();
        listViewContainer.repaint();
    }

    private void updateView() {
        String currentView = (String) viewSelector.getSelectedItem();
        dateRangeLabel.setText(getDateLabel());

        if ("List All".equals(currentView)) {
            renderListView();
        } else if ("Day".equals(currentView)) {
            renderDayView();
        } else if ("Week".equals(currentView)) {
            renderWeekView();
        } else if ("Month".equals(currentView)) {
            renderMonthView();
        }
    }

    private void showAddEventDialog() {
        new AddEventDialog((Frame) SwingUtilities.getWindowAncestor(this), calendarService, this::loadDashboardData).setVisible(true);
    }

    private void showEditEventDialog(CalendarEvent event) {
        new EditEventDialog((Frame) SwingUtilities.getWindowAncestor(this), event, calendarService, this::loadDashboardData).setVisible(true);
    }

    private Color getEventColor(String colorId) {
        return EventColorUtil.getEventColor(colorId);
    }

    // --- Renderers ---

    private void renderListView() {
        listViewContainer.removeAll();
        if (allEvents.isEmpty()) {
            listViewContainer.add(new JLabel("No events found."));
        } else {
            for (DashboardEvent de : allEvents) {
                listViewContainer.add(createEventCard(de));
                listViewContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        listViewContainer.revalidate();
        listViewContainer.repaint();
    }

    private void renderDayView() {
        dayViewContainer.removeAll();
        List<DashboardEvent> dayEvents = allEvents.stream()
                .filter(e -> e.getCalendarEvent().getStartTime().toLocalDate().equals(currentDate))
                .collect(Collectors.toList());

        if (dayEvents.isEmpty()) {
            JLabel empty = new JLabel("No events for " + currentDate);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            dayViewContainer.add(Box.createVerticalGlue());
            dayViewContainer.add(empty);
            dayViewContainer.add(Box.createVerticalGlue());
        } else {
            for (DashboardEvent de : dayEvents) {
                dayViewContainer.add(createEventCard(de));
                dayViewContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        dayViewContainer.revalidate();
        dayViewContainer.repaint();
    }

    private void renderWeekView() {
        weekViewContainer.removeAll();
        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            JPanel dayCol = new JPanel();
            dayCol.setLayout(new BoxLayout(dayCol, BoxLayout.Y_AXIS));
            dayCol.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
            
            // Header
            JLabel header = new JLabel(day.format(DateTimeFormatter.ofPattern("EEE dd")));
            header.setAlignmentX(Component.CENTER_ALIGNMENT);
            header.setFont(new Font("Arial", Font.BOLD, 12));
            dayCol.add(header);
            dayCol.add(Box.createRigidArea(new Dimension(0, 5)));
            
            // Events
            List<DashboardEvent> dayEvents = allEvents.stream()
                    .filter(e -> e.getCalendarEvent().getStartTime().toLocalDate().equals(day))
                    .collect(Collectors.toList());
            
            for (DashboardEvent de : dayEvents) {
                JPanel miniCard = new JPanel(new BorderLayout());
                Color eventColor = getEventColor(de.getCalendarEvent().getColorId());
                // Use a lighter version of the color for background
                Color bgColor = new Color(eventColor.getRed(), eventColor.getGreen(), eventColor.getBlue(), 50);
                miniCard.setBackground(bgColor);
                miniCard.setBorder(BorderFactory.createMatteBorder(0, 3, 0, 0, eventColor));
                miniCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
                
                JLabel time = new JLabel(de.getCalendarEvent().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                time.setFont(new Font("Arial", Font.PLAIN, 10));
                JLabel title = new JLabel(de.getCalendarEvent().getSummary());
                title.setFont(new Font("Arial", Font.BOLD, 11));
                
                miniCard.add(time, BorderLayout.NORTH);
                miniCard.add(title, BorderLayout.CENTER);
                
                if (de.getWeatherForecast() != null) {
                    JLabel w = new JLabel(de.getWeatherForecast().getConditionIcon());
                    miniCard.add(w, BorderLayout.EAST);
                }
                
                dayCol.add(miniCard);
                dayCol.add(Box.createRigidArea(new Dimension(0, 2)));
            }
            
            weekViewContainer.add(dayCol);
        }
        weekViewContainer.revalidate();
        weekViewContainer.repaint();
    }

    private void renderMonthView() {
        monthViewContainer.removeAll();
        
        // Header Row (Mon, Tue, ...)
        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            headerPanel.add(l);
        }
        monthViewContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Grid
        JPanel gridPanel = new JPanel(new GridLayout(0, 7));
        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        
        // Empty cells before 1st
        for (int i = 1; i < dayOfWeek; i++) {
            gridPanel.add(new JLabel(""));
        }
        
        // Days
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = yearMonth.atDay(day);
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            cell.setBackground(Color.WHITE);
            
            // Date Number
            JLabel num = new JLabel(" " + day);
            cell.add(num, BorderLayout.NORTH);
            
            // Events Container
            JPanel eventsList = new JPanel();
            eventsList.setLayout(new BoxLayout(eventsList, BoxLayout.Y_AXIS));
            eventsList.setOpaque(false);
            
            List<DashboardEvent> dayEvents = allEvents.stream()
                    .filter(e -> e.getCalendarEvent().getStartTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            
            // Show max 3 events to fit
            for (int k = 0; k < Math.min(dayEvents.size(), 3); k++) {
                DashboardEvent de = dayEvents.get(k);
                JLabel evLabel = new JLabel("• " + de.getCalendarEvent().getSummary());
                evLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                evLabel.setForeground(getEventColor(de.getCalendarEvent().getColorId()));
                eventsList.add(evLabel);
            }
            if (dayEvents.size() > 3) {
                eventsList.add(new JLabel(" + " + (dayEvents.size() - 3) + " more"));
            }
            
            cell.add(eventsList, BorderLayout.CENTER);
            
            // Weather (Bottom Right)
            // We can try to find weather for this date from our events or fetch it?
            // Currently we only have weather attached to events. 
            // If there is an event with weather, show it.
            DashboardEvent weatherEvent = dayEvents.stream().filter(e -> e.getWeatherForecast() != null).findFirst().orElse(null);
            if (weatherEvent != null) {
                JLabel w = new JLabel(weatherEvent.getWeatherForecast().getConditionIcon() + " ");
                w.setHorizontalAlignment(SwingConstants.RIGHT);
                cell.add(w, BorderLayout.SOUTH);
            }
            
            gridPanel.add(cell);
        }
        
        monthViewContainer.add(gridPanel, BorderLayout.CENTER);
        monthViewContainer.revalidate();
        monthViewContainer.repaint();
    }

    private JPanel createEventCard(DashboardEvent de) {
        JPanel card = new JPanel(new BorderLayout());
        Color eventColor = getEventColor(de.getCalendarEvent().getColorId());
        
        // Modern Card Style
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, eventColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(12, 15, 12, 15)
                )
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Left: Time and Date
        JPanel timePanel = new JPanel(new GridLayout(2, 1));
        timePanel.setOpaque(false);
        timePanel.setPreferredSize(new Dimension(100, 0));
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
        
        JLabel timeLabel = new JLabel(de.getCalendarEvent().getStartTime().format(timeFormatter));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeLabel.setForeground(new Color(50, 50, 50));
        
        JLabel dateLabel = new JLabel(de.getCalendarEvent().getStartTime().format(dateFormatter));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);
        
        timePanel.add(timeLabel);
        timePanel.add(dateLabel);
        
        // Center: Title and Location
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JLabel titleLabel = new JLabel(de.getCalendarEvent().getSummary());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        String locStr = de.getCalendarEvent().getEventLocation() != null ? de.getCalendarEvent().getEventLocation().toString() : "No Location";
        JLabel locLabel = new JLabel(locStr);
        locLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locLabel.setForeground(Color.DARK_GRAY);
        locLabel.setIcon(UIManager.getIcon("FileView.directoryIcon")); // Placeholder icon if available, or remove
        
        centerPanel.add(titleLabel);
        centerPanel.add(locLabel);

        // Right: Weather & Edit
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        JPanel weatherPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        weatherPanel.setOpaque(false);
        
        if (de.getWeatherForecast() != null) {
            JLabel iconLabel = new JLabel(de.getWeatherForecast().getConditionIcon()); 
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            
            String tempStr = String.format("%d° / %d°", 
                    Math.round(de.getWeatherForecast().getMinTemperature()), 
                    Math.round(de.getWeatherForecast().getMaxTemperature()));
            JLabel tempLabel = new JLabel(tempStr);
            tempLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            weatherPanel.add(tempLabel);
            weatherPanel.add(iconLabel);
        } else {
            JLabel noWeather = new JLabel("No Weather");
            noWeather.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noWeather.setForeground(Color.LIGHT_GRAY);
            weatherPanel.add(noWeather);
        }
        
        JButton editButton = new JButton("Edit");
        editButton.setFocusPainted(false);
        editButton.putClientProperty("JButton.buttonType", "roundRect"); // Mac style hint
        editButton.addActionListener(e -> showEditEventDialog(de.getCalendarEvent()));
        
        rightPanel.add(weatherPanel, BorderLayout.CENTER);
        rightPanel.add(editButton, BorderLayout.EAST);

        card.add(timePanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }
}
