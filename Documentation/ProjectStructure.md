# Project Structure Guide

Quick map of where everything lives in the `src/main/java/com/aurora/climatesync` folder.

## Core Layers

### `model/`
Just the data objects. No logic here.
- **CalendarEvent**: Raw event from Google.
- **ProcessedEvent**: Cleaned-up event ready for our app.
- **WeatherForecast**: Weather data for a specific day.
- **Location**: Stores lat/long and city info.

### `service/`
The heavy lifting. This is where the business logic happens.
- **EventProcessingService**: Takes raw Google events -> cleans them -> outputs ProcessedEvents.
- **WeatherService**: Fetches weather data (currently Open-Meteo).
- **CalendarService**: Handles Google Calendar API interactions.

### `controller/`
Web endpoints (REST API).
- **CalendarController**: Exposes calendar data to the web/frontend.

### `view/`
The desktop UI.
- **Main**: The Swing application window.

### `config/`
Setup stuff.
- **SecurityConfig**: Handles OAuth and app security settings.

## Root
- **ClimatesyncApplication.java**: The main launcher. Run this to start the Spring Boot app.

---
*Last updated: Nov 24, 2025*
