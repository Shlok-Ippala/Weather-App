package com.aurora.climatesync.view.component;

import com.aurora.climatesync.presenter.DashboardViewModel;
import com.aurora.climatesync.model.CalendarEvent;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class WeekViewPanel extends JPanel {
    private final JPanel contentPanel;

    public WeekViewPanel() {
        setLayout(new BorderLayout());
        contentPanel = new ScrollablePanel(new GridLayout(1, 7));
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    public void render(List<DashboardViewModel> events, LocalDate currentDate, Consumer<CalendarEvent> onEventClick) {
        contentPanel.removeAll();
        LocalDate startOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            JPanel dayCol = new JPanel(new BorderLayout());
            dayCol.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
            
            // Header
            JLabel header = new JLabel(day.format(DateTimeFormatter.ofPattern("EEE dd")), SwingConstants.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 12));
            header.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            dayCol.add(header, BorderLayout.NORTH);
            
            // Events
            List<DashboardViewModel> dayEvents = events.stream()
                    .filter(e -> e.getStartTime().toLocalDate().equals(day))
                    .collect(Collectors.toList());
            
            TimeBlockPanel timePanel = new TimeBlockPanel(dayEvents, i == 0, onEventClick);
            
            dayCol.add(timePanel, BorderLayout.CENTER);
            contentPanel.add(dayCol);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
