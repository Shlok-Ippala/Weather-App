package com.aurora.climatesync.view.dialog;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.model.Location;
import com.aurora.climatesync.util.EventColorUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Consumer;


public class AddEventDialog extends JDialog {
    private final Consumer<CalendarEvent> onSave;

    public AddEventDialog(Frame owner, Consumer<CalendarEvent> onSave) {
        super(owner, "Add Event", true);
        this.onSave = onSave;
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
        
        DateTimePicker startPicker = new DateTimePicker(LocalDateTime.now().plusHours(1));
        DateTimePicker endPicker = new DateTimePicker(LocalDateTime.now().plusHours(2));
        
        JComboBox<String> colorCombo = new JComboBox<>(EventColorUtil.COLOR_NAMES);
        colorCombo.setRenderer(new ColorListCellRenderer());
        colorCombo.setSelectedIndex(6); // Default to Peacock

        // Helper to add rows
        int row = 0;
        addFormRow(formPanel, gbc, row++, "Title:", titleField);
        addFormRow(formPanel, gbc, row++, "Description:", descField);
        addFormRow(formPanel, gbc, row++, "Location:", locField);
        addFormRow(formPanel, gbc, row++, "Start:", startPicker);
        addFormRow(formPanel, gbc, row++, "End:", endPicker);
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
                
                LocalDateTime startLdt = startPicker.getDateTime();
                LocalDateTime endLdt = endPicker.getDateTime();
                
                ZonedDateTime startZdt = startLdt.atZone(ZoneId.systemDefault());
                ZonedDateTime endZdt = endLdt.atZone(ZoneId.systemDefault());
                
                String selectedColorId = EventColorUtil.COLOR_IDS[colorCombo.getSelectedIndex()];

                CalendarEvent newEvent = new CalendarEvent(
                        null, title, desc, startZdt, endZdt, 
                        new Location(loc, "Unknown", 0, 0),
                        selectedColorId
                );
                
                if (onSave != null) onSave.accept(newEvent);
                dispose();
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
