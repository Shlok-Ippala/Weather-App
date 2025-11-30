package com.aurora.climatesync.presenter;

import java.util.List;

public interface DashboardContract {
    interface View {
        void showEvents(List<DashboardViewModel> events);
        void showLoading(String message);
        void showError(String message);
    }

    interface Presenter {
        void loadEvents();
        void loadEvents(int limit);
        void onViewReady();
        
        // Calendar Operations
        void connectCalendar();
        boolean isCalendarConnected();
        void addEvent(com.aurora.climatesync.model.CalendarEvent event);
        void updateEvent(com.aurora.climatesync.model.CalendarEvent event);
        void deleteEvent(String eventId);
        
        // Search
        void onSearchQuery(String query);
    }
}
