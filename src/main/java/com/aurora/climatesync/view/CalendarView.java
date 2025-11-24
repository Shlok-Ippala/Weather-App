package com.aurora.climatesync.view;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.service.CalendarService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarView extends JPanel {
    private final CalendarService calendarService;
    private final DefaultListModel<String> eventListModel;
    private final JList<String> eventList;
    private final JLabel statusLabel;
    private boolean isConnected = false;

    public CalendarView(CalendarService calendarService) {
        this.calendarService = calendarService;
        this.setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton connectButton = new JButton("Connect Google Calendar");
        JButton refreshButton = new JButton("Refresh");
        JButton createButton = new JButton("Create Event");
        
        toolBar.add(connectButton);
        toolBar.add(refreshButton);
        toolBar.add(createButton);

        // Event List
        eventListModel = new DefaultListModel<>();
        eventList = new JList<>(eventListModel);
        JScrollPane scrollPane = new JScrollPane(eventList);
        
        // Status
        statusLabel = new JLabel("Not connected.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        this.add(toolBar, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(statusLabel, BorderLayout.SOUTH);

        // Actions
        connectButton.addActionListener(e -> connect());
        refreshButton.addActionListener(e -> refreshEvents());
        createButton.addActionListener(e -> showCreateEventDialog());
    }

    private void connect() {
        statusLabel.setText("Connecting...");
        new Thread(() -> {
            try {
                String id = calendarService.connect();
                isConnected = true;
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connected to calendar: " + id);
                    refreshEvents();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Connection failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "Failed to connect: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void refreshEvents() {
        if (!isConnected) {
            statusLabel.setText("Please connect first.");
            return;
        }
        statusLabel.setText("Fetching events...");
        new Thread(() -> {
            try {
                List<CalendarEvent> events = calendarService.getUpcomingEvents();
                SwingUtilities.invokeLater(() -> {
                    eventListModel.clear();
                    if (events.isEmpty()) {
                        eventListModel.addElement("No upcoming events found.");
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
                        for (CalendarEvent event : events) {
                            String timeStr = event.getStartTime().format(formatter) + " - " + event.getEndTime().format(formatter);
                            eventListModel.addElement(String.format("%s: %s (%s)", timeStr, event.getSummary(), event.getDescription()));
                        }
                    }
                    statusLabel.setText("Events updated.");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> statusLabel.setText("Error fetching events: " + e.getMessage()));
            }
        }).start();
    }

    private void showCreateEventDialog() {
        if (!isConnected) {
            JOptionPane.showMessageDialog(this, "Please connect to Google Calendar first.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Event", true);
        dialog.setLayout(new GridLayout(6, 2, 10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField startField = new JTextField(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        JTextField endField = new JTextField(LocalDateTime.now().plusHours(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        JTextField locationField = new JTextField();

        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Description:"));
        dialog.add(descField);
        dialog.add(new JLabel("Start (ISO 8601):"));
        dialog.add(startField);
        dialog.add(new JLabel("End (ISO 8601):"));
        dialog.add(endField);
        dialog.add(new JLabel("Location:"));
        dialog.add(locationField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String title = titleField.getText();
                String desc = descField.getText();
                LocalDateTime startLocal = LocalDateTime.parse(startField.getText());
                LocalDateTime endLocal = LocalDateTime.parse(endField.getText());
                
                ZonedDateTime start = startLocal.atZone(ZoneId.systemDefault());
                ZonedDateTime end = endLocal.atZone(ZoneId.systemDefault());
                
                Location loc = new Location(locationField.getText(), "", 0, 0); // Simplified location

                CalendarEvent newEvent = new CalendarEvent(null, title, desc, start, end, loc);
                
                new Thread(() -> {
                    try {
                        calendarService.addEvent(newEvent);
                        SwingUtilities.invokeLater(() -> {
                            dialog.dispose();
                            refreshEvents();
                            JOptionPane.showMessageDialog(this, "Event created successfully!");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "Failed to create event: " + ex.getMessage()));
                    }
                }).start();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        dialog.add(new JLabel("")); // Spacer
        dialog.add(saveButton);

        dialog.setVisible(true);
    }
}
