package com.aurora.climatesync.view;

import com.aurora.climatesync.ClimatesyncApplication;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.WeatherService;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class Main {

    private final WeatherService weatherService;
    private final CalendarService calendarService;

    public Main(WeatherService weatherService, CalendarService calendarService) {
        this.weatherService = weatherService;
        this.calendarService = calendarService;
    }

    private void launchUI() {
        JFrame frame = new JFrame("ClimateSync");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Weather Tab
        WeatherView weatherView = new WeatherView(weatherService);
        tabbedPane.addTab("Weather", weatherView);

        // Calendar Tab
        CalendarView calendarView = new CalendarView(calendarService);
        tabbedPane.addTab("Calendar", calendarView);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ClimatesyncApplication.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            Main app = context.getBean(Main.class);
            app.launchUI();
        });
    }
}
