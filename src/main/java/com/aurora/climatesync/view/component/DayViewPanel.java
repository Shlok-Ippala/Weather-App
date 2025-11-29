package com.aurora.climatesync.view.component;

import com.aurora.climatesync.presenter.DashboardViewModel;
import com.aurora.climatesync.model.CalendarEvent;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DayViewPanel extends JPanel {
    private final JPanel contentPanel;

    public DayViewPanel() {
        setLayout(new BorderLayout());
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    public void render(List<DashboardViewModel> events, LocalDate date, Consumer<CalendarEvent> onEventClick) {
        contentPanel.removeAll();
        List<DashboardViewModel> dayEvents = events.stream()
                .filter(e -> e.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());

        if (dayEvents.isEmpty()) {
            JLabel empty = new JLabel("No events for " + date);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(Box.createVerticalGlue());
            contentPanel.add(empty);
            contentPanel.add(Box.createVerticalGlue());
        } else {
            TimeBlockPanel timePanel = new TimeBlockPanel(dayEvents, true, onEventClick);
            contentPanel.add(timePanel);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
