package com.aurora.climatesync.view.component;

import com.aurora.climatesync.model.CalendarEvent;
import com.aurora.climatesync.presenter.DashboardViewModel;
import com.aurora.climatesync.util.EventColorUtil;
import com.aurora.climatesync.util.WeatherIconMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

public class TimeBlockPanel extends JPanel {
    private final int HOUR_HEIGHT = 60;
    private final int TIME_COLUMN_WIDTH = 40;
    private final boolean showTimeLabels;
    private final Consumer<CalendarEvent> onEventClick;

    public TimeBlockPanel(List<DashboardViewModel> events, boolean showTimeLabels, Consumer<CalendarEvent> onEventClick) {
        this.showTimeLabels = showTimeLabels;
        this.onEventClick = onEventClick;
        this.setLayout(null);
        this.setBackground(Color.WHITE);
        // Ensure width is sufficient, though layout manager usually handles it. 
        // 24 * HOUR_HEIGHT + padding
        this.setPreferredSize(new Dimension(0, 24 * HOUR_HEIGHT + 20));

        for (DashboardViewModel de : events) {
            addEventCard(de);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int startX = showTimeLabels ? TIME_COLUMN_WIDTH : 0;

        // Draw hour lines and labels
        for (int i = 0; i <= 24; i++) {
            int y = i * HOUR_HEIGHT;
            
            // Draw horizontal line
            g2.setColor(new Color(230, 230, 230)); // Light gray for grid
            g2.drawLine(startX, y, width, y);

            // Draw time label if needed
            if (showTimeLabels && i < 24) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                String label = formatTime(i);
                // Draw label centered vertically around the line? 
                // Or just below/above. Let's put it slightly down to align with the slot start.
                // Actually, standard is label is centered on the tick mark (line).
                FontMetrics fm = g2.getFontMetrics();
                int labelHeight = fm.getAscent();
                g2.drawString(label, 5, y + labelHeight / 2); 
            }
        }
        
        // Vertical line separator
        if (showTimeLabels) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(TIME_COLUMN_WIDTH, 0, TIME_COLUMN_WIDTH, getHeight());
        }
    }

    private String formatTime(int hour) {
        if (hour == 0) return "12 AM";
        if (hour < 12) return hour + " AM";
        if (hour == 12) return "12 PM";
        return (hour - 12) + " PM";
    }

    private void addEventCard(DashboardViewModel de) {
        LocalTime start = de.getStartTime().toLocalTime();
        LocalTime end = de.getEndTime().toLocalTime();

        int startY = start.getHour() * HOUR_HEIGHT + start.getMinute();
        int endY = end.getHour() * HOUR_HEIGHT + end.getMinute();

        // Handle events spanning across days (simplified: clip to end of day)
        if (de.getEndTime().toLocalDate().isAfter(de.getStartTime().toLocalDate())) {
            endY = 24 * HOUR_HEIGHT;
        }

        int height = Math.max(25, endY - startY);

        JPanel card = new JPanel(new BorderLayout());
        Color eventColor = EventColorUtil.getEventColor(de.getColorId());
        card.setBackground(new Color(eventColor.getRed(), eventColor.getGreen(), eventColor.getBlue(), 50));
        card.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, eventColor));

        JPanel content = new JPanel(new GridLayout(0, 1));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));

        JLabel title = new JLabel(de.getTitle());
        title.setFont(new Font("Arial", Font.BOLD, 11));
        content.add(title);

        if (de.getLocation() != null) {
            JLabel loc = new JLabel("\uD83D\uDCCD " + de.getLocation());
            loc.setFont(new Font("Arial", Font.PLAIN, 9));
            loc.setForeground(Color.DARK_GRAY);
            content.add(loc);
        }

        // Add weather info if available
        if (de.getWeatherIcon() != null && de.getTemperatureDisplay() != null) {
            JLabel weatherLabel = new JLabel();
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(de.getWeatherIcon()));
                Image scaled = icon.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH);
                weatherLabel.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                // fallback: show text if icon missing
                weatherLabel.setText(de.getWeatherIcon());
            }
            weatherLabel.setText(" " + de.getTemperatureDisplay());
            weatherLabel.setFont(new Font("Arial", Font.PLAIN, 9));
            weatherLabel.setForeground(new Color(0, 102, 204));

            content.add(weatherLabel);
        }

        // Add weather message if available
        if (de.getWeatherMessage() != null) {
            JLabel msgLabel = new JLabel(de.getWeatherMessage());
            msgLabel.setFont(new Font("Arial", Font.ITALIC, 9));
            msgLabel.setForeground(new Color(0, 102, 204));
            content.add(msgLabel);
            
            // Also add as tooltip in case it's cut off
            card.setToolTipText(de.getWeatherMessage());
        }

        card.add(content, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onEventClick != null && de.getSourceEvent() instanceof CalendarEvent) {
                    onEventClick.accept((CalendarEvent) de.getSourceEvent());
                }
            }
        });

        // Store layout info
        card.putClientProperty("startY", startY);
        card.putClientProperty("height", height);

        this.add(card);
    }

    @Override
    public void doLayout() {
        int startX = showTimeLabels ? TIME_COLUMN_WIDTH : 0;
        int width = getWidth() - startX - 5;
        for (Component comp : getComponents()) {
            if (comp instanceof JComponent) {
                JComponent jc = (JComponent) comp;
                Integer startY = (Integer) jc.getClientProperty("startY");
                Integer height = (Integer) jc.getClientProperty("height");
                if (startY != null && height != null) {
                    jc.setBounds(startX, startY, width, height);
                }
            }
        }
    }
}
