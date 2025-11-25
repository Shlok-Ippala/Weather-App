package com.aurora.climatesync.view.dialog;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.util.EventColorUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AddEventDialog extends JDialog {
    private final CalendarService calendarService;
    private final Runnable onSuccess;

    public AddEventDialog(Frame owner, CalendarService calendarService, Runnable onSuccess) {
        super(owner, "Add Event", true);
        this.calendarService = calendarService;
        this.onSuccess = onSuccess;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(450, 400);
        setLocationRelativeTo(getOwner());
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField titleField = new JTextField(20);
        JTextField descField = new JTextField(20);
        JTextField locField = new JTextField(20);
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        JTextField startField = new JTextField(LocalDateTime.now().plusHours(1).format(fmt), 20);
        JTextField endField = new JTextField(LocalDateTime.now().plusHours(2).format(fmt), 20);
        
        JComboBox<String> colorCombo = new JComboBox<>(EventColorUtil.COLOR_NAMES);
        colorCombo.setSelectedIndex(6); // Default to Peacock

        // Helper to add rows
        int row = 0;
        addFormRow(formPanel, gbc, row++, "Title:", titleField);
        addFormRow(formPanel, gbc, row++, "Description:", descField);
        addFormRow(formPanel, gbc, row++, "Location:", locField);
        addFormRow(formPanel, gbc, row++, "Start (yyyy-MM-dd HH:mm):", startField);
        addFormRow(formPanel, gbc, row++, "End (yyyy-MM-dd HH:mm):", endField);
        addFormRow(formPanel, gbc, row++, "Color:", colorCombo);

        add(formPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton saveButton = new JButton("Save");
        
        // Style buttons
        saveButton.setBackground(new Color(66, 133, 244)); // Google Blue
        saveButton.setForeground(Color.BLACK);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        
        cancelButton.addActionListener(e -> dispose());
        saveButton.addActionListener(e -> {
            try {
                String title = titleField.getText();
                String desc = descField.getText();
                String loc = locField.getText();
                
                LocalDateTime startLdt = LocalDateTime.parse(startField.getText(), fmt);
                LocalDateTime endLdt = LocalDateTime.parse(endField.getText(), fmt);
                
                ZonedDateTime startZdt = startLdt.atZone(ZoneId.systemDefault());
                ZonedDateTime endZdt = endLdt.atZone(ZoneId.systemDefault());
                
                String selectedColorId = EventColorUtil.COLOR_IDS[colorCombo.getSelectedIndex()];

                CalendarEvent newEvent = new CalendarEvent(
                        null, title, desc, startZdt, endZdt, 
                        new Location(loc, "Unknown", 0, 0),
                        selectedColorId
                );
                
                calendarService.addEvent(newEvent);
                dispose();
                if (onSuccess != null) onSuccess.run();
                JOptionPane.showMessageDialog(this, "Event added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }
}
