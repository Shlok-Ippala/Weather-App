package com.aurora.climatesync.view.component;

import com.aurora.climatesync.presenter.DashboardViewModel;
import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.util.EventColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MonthViewPanel extends JPanel {
    private final JPanel contentPanel;

    public MonthViewPanel() {
        setLayout(new BorderLayout());
        contentPanel = new JPanel(new BorderLayout());
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }

    public void render(List<DashboardViewModel> events, LocalDate currentDate, Consumer<CalendarEvent> onEventClick) {
        contentPanel.removeAll();
        
        // Header Row (Mon, Tue, ...)
        JPanel headerPanel = new JPanel(new GridLayout(1, 7));
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Arial", Font.BOLD, 12));
            headerPanel.add(l);
        }
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
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
            
            List<DashboardViewModel> dayEvents = events.stream()
                    .filter(e -> e.getStartTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            
            // Show max 3 events to fit
            for (int k = 0; k < Math.min(dayEvents.size(), 3); k++) {
                DashboardViewModel de = dayEvents.get(k);
                String timeStr = de.getStartTime().format(DateTimeFormatter.ofPattern("ha")).toLowerCase();
                
                String weatherIcon = "";
                if (de.getWeatherIcon() != null) {
                    weatherIcon = " " + de.getWeatherIcon();
                }

                JLabel evLabel = new JLabel("• " + timeStr + " " + de.getTitle() + weatherIcon);
                evLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                evLabel.setForeground(EventColorUtil.getEventColor(de.getColorId()));
                
                evLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                evLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (de.getSourceEvent() instanceof CalendarEvent) {
                            onEventClick.accept((CalendarEvent) de.getSourceEvent());
                        }
                    }
                });
                
                eventsList.add(evLabel);
            }
            if (dayEvents.size() > 3) {
                eventsList.add(new JLabel(" + " + (dayEvents.size() - 3) + " more"));
            }
            
            cell.add(eventsList, BorderLayout.CENTER);
            
            // Weather (Bottom Right)
            DashboardViewModel weatherEvent = dayEvents.stream()
                    .filter(e -> e.getWeatherIcon() != null && !e.getWeatherIcon().isEmpty())
                    .findFirst()
                    .orElse(null);
            
            if (weatherEvent != null) {
                JLabel w = new JLabel(weatherEvent.getWeatherIcon() + " ");
                w.setHorizontalAlignment(SwingConstants.RIGHT);
                cell.add(w, BorderLayout.SOUTH);
            }
            
            gridPanel.add(cell);
        }
        
        contentPanel.add(gridPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
