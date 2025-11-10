package com.aurora.climatesync.model;

public class User {
    private String userId;          // Google user ID
    private String email;           // User's email from Google
    private String displayName;     // User's name from Google
    private boolean isGoogleConnected; // Track if user connected Google Calendar
    private String accessToken;     // OAuth access token (store securely!)
    private String refreshToken;    // OAuth refresh token

    public User(String userId, String email, String displayName) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.isGoogleConnected = false;
    }

    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public boolean isGoogleConnected() { return isGoogleConnected; }

}
