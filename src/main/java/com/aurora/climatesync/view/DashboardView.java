package com.aurora.climatesync.view;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.presenter.DashboardContract;
import com.aurora.climatesync.presenter.DashboardPresenter;
import com.aurora.climatesync.presenter.DashboardViewModel;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.DashboardService;
import com.aurora.climatesync.util.EventColorUtil;
import com.aurora.climatesync.util.WeatherIconLoader;
import com.aurora.climatesync.view.component.DayViewPanel;
import com.aurora.climatesync.view.component.MonthViewPanel;
import com.aurora.climatesync.view.component.WeekViewPanel;
import com.aurora.climatesync.view.dialog.AddEventDialog;
import com.aurora.climatesync.view.dialog.EditEventDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardView extends JPanel implements DashboardContract.View {
    private final DashboardContract.Presenter presenter;

    // UI Components
    private final JLabel statusLabel;

    private final JTextField searchField;
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final JComboBox<String> viewSelector;
    private final JLabel dateRangeLabel;

    // View Containers
    private final JPanel listViewContainer;
    private final DayViewPanel dayViewPanel;
    private final WeekViewPanel weekViewPanel;
    private final MonthViewPanel monthViewPanel;

    // Data State
    private List<DashboardViewModel> displayedEvents = new ArrayList<>();
    private LocalDate currentDate = LocalDate.now();
    
    public DashboardView(CalendarService calendarService, DashboardService dashboardService) {
        this.presenter = new DashboardPresenter(this, dashboardService, calendarService);
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
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                presenter.onSearchQuery(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                presenter.onSearchQuery(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                presenter.onSearchQuery(searchField.getText());
            }
        });
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
        
        dayViewPanel = new DayViewPanel();
        weekViewPanel = new WeekViewPanel();
        monthViewPanel = new MonthViewPanel();

        contentPanel.add(new JScrollPane(listViewContainer), "List All");
        contentPanel.add(dayViewPanel, "Day");
        contentPanel.add(weekViewPanel, "Week");
        contentPanel.add(monthViewPanel, "Month");

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

    @Override
    public void showEvents(List<DashboardViewModel> events) {
        this.displayedEvents = events;
        statusLabel.setText("Loaded " + events.size() + " events.");
        updateView();
    }

    @Override
    public void showLoading(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void showError(String message) {
        statusLabel.setText("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void loadDashboardData() {
        if (!presenter.isCalendarConnected()) {
            showConnectionError();
            return;
        }
        presenter.loadEvents();
    }

    

    private void showConnectionError() {
        listViewContainer.removeAll();
        JLabel notConnectedLabel = new JLabel("Google Calendar is not connected.");
        notConnectedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton connectButton = new JButton("Connect Now");
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(e -> {
            presenter.connectCalendar();
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
        new AddEventDialog((Frame) SwingUtilities.getWindowAncestor(this), newEvent -> {
            presenter.addEvent(newEvent);
        }).setVisible(true);
    }

    private void showEditEventDialog(CalendarEvent event) {
        new EditEventDialog((Frame) SwingUtilities.getWindowAncestor(this), event, 
            updatedEvent -> presenter.updateEvent(updatedEvent),
            eventId -> presenter.deleteEvent(eventId)
        ).setVisible(true);
    }

    private Color getEventColor(String colorId) {
        return EventColorUtil.getEventColor(colorId);
    }

    // --- Renderers ---

    private void renderListView() {
        listViewContainer.removeAll();
        List<DashboardViewModel> eventsToRender = displayedEvents;
        if (eventsToRender.isEmpty()) {
            listViewContainer.add(new JLabel("No events found."));
        } else {
            for (DashboardViewModel de : eventsToRender) {
                listViewContainer.add(createEventCard(de));
                listViewContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        listViewContainer.revalidate();
        listViewContainer.repaint();
    }

    private void renderDayView() {
        dayViewPanel.render(displayedEvents, currentDate, this::showEditEventDialog);
    }

    private void renderWeekView() {
        weekViewPanel.render(displayedEvents, currentDate, this::showEditEventDialog);
    }

    private void renderMonthView() {
        monthViewPanel.render(displayedEvents, currentDate, this::showEditEventDialog);
    }

    private JPanel createEventCard(DashboardViewModel de) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        Color eventColor = getEventColor(de.getColorId());
        
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
        
        JLabel timeLabel = new JLabel(de.getStartTime().format(timeFormatter));
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeLabel.setForeground(new Color(50, 50, 50));
        
        JLabel dateLabel = new JLabel(de.getStartTime().format(dateFormatter));
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);
        
        timePanel.add(timeLabel);
        timePanel.add(dateLabel);
        
        // Center: Title and Location
        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 0, 4)); // 0 rows means any number of rows
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        JLabel titleLabel = new JLabel(de.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        
        String locStr = de.getLocation() != null ? de.getLocation() : "No Location";
        JLabel locLabel = new JLabel(locStr);
        locLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        locLabel.setForeground(Color.DARK_GRAY);
        // Icon removed as per request

        centerPanel.add(titleLabel);
        centerPanel.add(locLabel);
        
        // Add Description if available
        String desc = de.getDescription();
        if (desc != null && !desc.isEmpty()) {
            JLabel descLabel = new JLabel(desc);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            descLabel.setForeground(Color.GRAY);
            centerPanel.add(descLabel);
        }

        // Add Weather Message if available
        if (de.getWeatherMessage() != null) {
            JLabel weatherMsgLabel = new JLabel(de.getWeatherMessage());
            weatherMsgLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            weatherMsgLabel.setForeground(new Color(0, 102, 204)); // A nice blue for info
            centerPanel.add(weatherMsgLabel);
        }

        // Right: Weather & Edit
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        
        JPanel weatherPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        weatherPanel.setOpaque(false);
        
        if (de.getWeatherIcon() != null && de.getTemperatureDisplay() != null) {
            int code = de.getWeatherCode();
            String iconName = WeatherClimateMapper.getIcon(code);
            ImageIcon icon = WeatherIconLoader.load(iconName);

            JLabel iconLabel = new JLabel(icon);
            weatherPanel.add(iconLabel);

            String tempStr = String.format("%d° / %d°",
                    Math.round(de.getWeatherForecast().getMinTemperature()),
                    Math.round(de.getWeatherForecast().getMaxTemperature())
            );

            JLabel tempLabel = new JLabel(tempStr);
            tempLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

            weatherPanel.add(tempLabel);

        } else {
            JLabel noWeather = new JLabel("No Weather");
            noWeather.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noWeather.setForeground(Color.LIGHT_GRAY);
            weatherPanel.add(noWeather);
        }
        
        JButton editButton = new JButton("Edit");
        editButton.setFocusPainted(false);
        editButton.putClientProperty("JButton.buttonType", "roundRect"); // Mac style hint
        editButton.addActionListener(e -> {
            if (de.getSourceEvent() instanceof CalendarEvent) {
                showEditEventDialog((CalendarEvent) de.getSourceEvent());
            }
        });
        
        rightPanel.add(weatherPanel, BorderLayout.CENTER);
        rightPanel.add(editButton, BorderLayout.EAST);

        card.add(timePanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }
    
    // Inner classes removed and extracted to com.aurora.climatesync.view.component
}
