# Refactoring Log

**Date:** November 24, 2025

Hey team,

I just pushed a few changes to clean up the architecture a bit. The main goal was to decouple our high-level components (like the Controller and the View) from the concrete service implementations. This makes the code easier to test and swap out later if we need to change providers (like switching from Open-Meteo to AccuWeather, or Google Calendar to Outlook).

### What Changed?

1.  **Extracted Interfaces:**
    *   Created `WeatherService` interface.
    *   Created `CalendarService` interface.
    *   Renamed the original implementations to `WeatherServiceImpl` and `GoogleCalendarServiceImpl`.

2.  **Updated Dependencies:**
    *   `CalendarController` now depends on `CalendarService` instead of `GoogleCalendarService`.
    *   `Main` (the Swing UI) now uses the `WeatherService` interface.

### Why?
This aligns us better with Clean Architecture and SOLID principles (specifically Dependency Inversion). Now our business logic depends on abstractions, not details.

Thanks,
William
