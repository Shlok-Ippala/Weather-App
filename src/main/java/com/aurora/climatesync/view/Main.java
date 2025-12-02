package com.aurora.climatesync.view;

import com.aurora.climatesync.ClimatesyncApplication;
import com.aurora.climatesync.repository.LocationRepository;
import com.aurora.climatesync.service.CalendarService;
import com.aurora.climatesync.service.DashboardService;
import com.aurora.climatesync.service.WeatherService;
import com.aurora.climatesync.service.SearchService;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.swing.*;

import com.aurora.climatesync.presenter.WeatherPresenter;

@Component
public class Main {

    private final WeatherService weatherService;
    private final CalendarService calendarService;
    private final DashboardService dashboardService;

    public Main(WeatherService weatherService, CalendarService calendarService, DashboardService dashboardService) {
        this.weatherService = weatherService;
        this.calendarService = calendarService;
        this.dashboardService = dashboardService;
    }

    @Bean
    public SearchService searchService(LocationRepository locationRepository) {
        return new SearchService(locationRepository);
    }

    private void launchUI(ConfigurableApplicationContext context) {
        JFrame frame = new JFrame("ClimateSync");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(800, 600); // Increased size for better calendar view
        frame.setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Home Tab
        DashboardView dashboardView = new DashboardView(calendarService, dashboardService);
        tabbedPane.addTab("Home", dashboardView);

        // Weather Tab
        WeatherView weatherView = new WeatherView();
        SearchService searchService = context.getBean(SearchService.class);
        WeatherPresenter weatherPresenter = new WeatherPresenter(weatherView, weatherService, searchService);
        weatherView.setPresenter(weatherPresenter);
        
        tabbedPane.addTab("Weather", weatherView);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(ClimatesyncApplication.class)
                .headless(false)
                .run(args);

        SwingUtilities.invokeLater(() -> {
            Main app = context.getBean(Main.class);
            app.launchUI(context);
        });
    }
}
