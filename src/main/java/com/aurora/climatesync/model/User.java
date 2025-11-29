package com.aurora.climatesync.model;

public class User {
    private String userId;          // Google user ID
    private String email;
    private String displayName;
    private boolean isGoogleConnected; // Track if connected Google Calendar
    
    public User(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.isGoogleConnected = false;
    }


    public void setGoogleConnected(boolean isGoogleConnected) {
        this.isGoogleConnected = isGoogleConnected;
    }


    public void clearTokens() {
        this.isGoogleConnected = false;
    }


    public String getUserId() {
        return userId;
    }
    public String getEmail() {
        return email;
    }
    public String getDisplayName() {
        return displayName;
    }
    public boolean isGoogleConnected() {
        return isGoogleConnected;
    }
}
