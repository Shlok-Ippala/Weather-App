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


public class EditEventDialog extends JDialog {
    private final CalendarEvent event;
    private final Consumer<CalendarEvent> onUpdate;
    private final Consumer<String> onDelete;

    public EditEventDialog(Frame owner, CalendarEvent event, Consumer<CalendarEvent> onUpdate, Consumer<String> onDelete) {
        super(owner, "Edit Event", true);
        this.event = event;
        this.onUpdate = onUpdate;
        this.onDelete = onDelete;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(450, 450);
        setLocationRelativeTo(getOwner());
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(event.getSummary(), 20);
        JTextField descField = new JTextField(event.getDescription(), 20);
        String locStr = event.getEventLocation() != null ? event.getEventLocation().getCityName() : "";
        if (locStr == null) locStr = "";
        JTextField locField = new JTextField(locStr, 20);
        
        DateTimePicker startPicker = new DateTimePicker(event.getStartTime().toLocalDateTime());
        DateTimePicker endPicker = new DateTimePicker(event.getEndTime().toLocalDateTime());
        
        JComboBox<String> colorCombo = new JComboBox<>(EventColorUtil.COLOR_NAMES);
        colorCombo.setRenderer(new ColorListCellRenderer());
        colorCombo.setSelectedIndex(EventColorUtil.getColorIndex(event.getColorId()));

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
        JButton deleteButton = new JButton("Delete");
        JButton updateButton = new JButton("Update");
        
        // Style buttons
        updateButton.setBackground(new Color(66, 133, 244));
        updateButton.setForeground(Color.BLACK);
        updateButton.setOpaque(true);
        updateButton.setBorderPainted(false);
        
        deleteButton.setForeground(Color.RED);
        
        cancelButton.addActionListener(e -> dispose());
        
        updateButton.addActionListener(e -> {
            try {
                String title = titleField.getText();
                String desc = descField.getText();
                String loc = locField.getText();
                LocalDateTime startLdt = startPicker.getDateTime();
                LocalDateTime endLdt = endPicker.getDateTime();
                
                ZonedDateTime startZdt = startLdt.atZone(ZoneId.systemDefault());
                ZonedDateTime endZdt = endLdt.atZone(ZoneId.systemDefault());
                
                String selectedColorId = EventColorUtil.COLOR_IDS[colorCombo.getSelectedIndex()];

                CalendarEvent updatedEvent = new CalendarEvent(
                        event.getEventID(), title, desc, startZdt, endZdt, 
                        new Location(loc, "Unknown", 0, 0),
                        selectedColorId
                );
                
                if (onUpdate != null) onUpdate.accept(updatedEvent);
                dispose();
                JOptionPane.showMessageDialog(this, "Event updated successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating event: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this event?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (onDelete != null) onDelete.accept(event.getEventID());
                    dispose();
                    JOptionPane.showMessageDialog(this, "Event deleted successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(updateButton);
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
