
# User Story Allocations:

This document outlines the primary user stories for the Minimal Viable Product (MVP) of the Climate Sync application. Each story is assigned a lead, with a detailed description, specific acceptance criteria, and a clear Definition of Done to guide development and ensure quality.

---

### 1. Qiuru (Backend/Authentication)

*   **User Story:** As a user, I want to securely connect my Google Account using OAuth 2.0, so the application can gain permission to read my calendar events.
*   **Story Description:** This is a foundational backend task that establishes the secure connection between our application and the user's Google Calendar. It involves handling the OAuth 2.0 flow, managing API credentials securely, and storing authorization tokens for future use. This story provides the authenticated access needed for most other features.
*   **Acceptance Criteria:**
    *   [ ] A "Connect to Google Calendar" button exists in the UI.
    *   [ ] Clicking the button correctly redirects the user to the Google OAuth 2.0 consent screen.
    *   [ ] After the user grants permission, they are redirected back to the application.
    *   [ ] The application successfully receives and securely stores the user's OAuth token.
    *   [ ] The system can make an authenticated test call to the Google Calendar API to verify the token is valid (e.g., fetch the user's primary calendar ID).
    *   [ ] If the user denies permission on the consent screen, the application handles the callback gracefully and displays an appropriate message (e.g., "Calendar access denied.").
*   **Definition of Done:**
    *   [ ] All code is reviewed, approved, and merged into the `main` branch.
    *   [ ] Unit tests are written for the token handling and storage logic, achieving at least 80% code coverage for the new module.
    *   [ ] All secrets (Client ID, Client Secret) are managed via environment variables and are not hardcoded in the source code.
    *   [ ] The authentication flow is documented in the project's technical documentation.

---

### 2. Ritika (Backend/Interactor - Weather Retrieval)

*   **User Story:** As a user, I want the system to be able to take a specific latitude and longitude and retrieve a 7-day weather forecast from the Open-Meteo API, so that weather data is available for any location.
*   **Story Description:** This story involves creating a dedicated, reusable service for fetching weather data. This service will act as a wrapper around the Open-Meteo API, handling the request/response cycle, error handling, and parsing the data into the application's internal `WeatherForecast` model. This decouples the core application from the specific third-party weather provider.
*   **Acceptance Criteria:**
    *   [ ] A backend function/method exists that accepts `latitude` (double) and `longitude` (double) as arguments.
    *   [ ] The function makes a successful API call to the Open-Meteo `/v1/forecast` endpoint.
    *   [ ] The JSON response from the API is correctly parsed into a list of `WeatherForecast` objects as defined in the Project Blueprint.
    *   [ ] The service gracefully handles potential API errors (e.g., network issues, 4xx/5xx status codes) without crashing, returning a clear error state.
    *   [ ] The returned data includes max/min temperature, weather condition, and precipitation probability for the current day and the next six days.
*   **Definition of Done:**
    *   [ ] All code is reviewed, approved, and merged into the `main` branch.
    *   [ ] Unit tests are written for the service, covering both successful data parsing and API failure scenarios.
    *   [ ] The `WeatherForecast` data structure is finalized and committed to the shared data models.

---

### 3. Xinyue (Search & Geocoding)

*   **User Story:** As a user, I want to type a city name into a search bar, and have the application convert that name into a precise latitude and longitude, so that I can look up the weather for any location I choose.
*   **Story Description:** This story covers the end-to-end functionality for location searching. It includes creating the backend logic to interface with a geocoding API (e.g., the Open-Meteo Geocoding API) to translate a human-readable string into geographic coordinates, which are required for weather lookups.
*   **Acceptance Criteria:**
    *   [ ] A backend API endpoint is created that accepts a search query string (e.g., `/api/geocode?q=Toronto`).
    *   [ ] The endpoint successfully calls an external geocoding service with the query.
    *   [ ] The service returns a `Location` object containing the `latitude` and `longitude` for the first valid result.
    *   [ ] The system correctly handles cases where a location cannot be found, returning a clear "not found" response.
    *   [ ] The frontend search bar is connected to this endpoint, and a successful search triggers a new weather forecast lookup.
*   **Definition of Done:**
    *   [ ] All code is reviewed, approved, and merged into the `main` branch.
    *   [ ] Unit tests are written for the geocoding service wrapper.
    *   [ ] An integration test is performed to confirm the frontend search input successfully retrieves and displays data via the new endpoint.

---

### 4. William (Lead - Technical Integration & Caching)

*   **User Story:** As a developer, I need to design and build a robust backend system that automatically fetches and caches weather forecasts for all of a user's upcoming calendar events for the next week, so that the UI can load instantly without waiting for multiple, real-time API calls.
*   **Story Description:** This is a core technical story focused on performance, efficiency, and data orchestration. William is responsible for creating the service that links calendar events (from Qiuru's module) with their corresponding weather forecasts (from Ritika's module). This involves implementing a caching layer to minimize external API calls, reduce latency, and provide a single, fast endpoint for the frontend to consume.
*   **Acceptance Criteria:**
    *   [ ] A background service is implemented that runs periodically (e.g., on user login or every hour).
    *   [ ] The service fetches all calendar events for the next 7 days that have a location.
    *   [ ] For each unique event location, the service fetches the 7-day weather forecast and stores it in a cache (e.g., Redis or an in-memory store).
    *   [ ] A primary API endpoint (e.g., `/api/dashboard`) is created. When called, it retrieves events and matches them with the pre-fetched weather data from the cache.
    *   [ ] The API response time for this endpoint is consistently below 200ms under normal load.
    *   [ ] The cache has a clearly defined expiration strategy to ensure weather data remains reasonably current.
*   **Definition of Done:**
    *   [ ] All code is reviewed, approved, and merged into the `main` branch.
    *   [ ] The caching strategy and the new API endpoint are documented in the technical documentation.
    *   [ ] Integration tests are created to ensure the service correctly combines data from the calendar and weather modules.
    *   [ ] Unit tests are written for the logic that matches events to cached weather data.

---

### 5. Shlok (Frontend/GUI - Weather Chart)

*   **User Story:** As a user, I want to see a visual weather chart displaying hourly temperature trends and precipitation probability throughout the current day, so I can quickly understand how the weather will change hour by hour.
*   **Story Description:** This story focuses on creating a Google-style weather chart component using JFreeChart. The chart displays hourly temperatures as a colored line graph with data points, and precipitation probability as semi-transparent bar overlays on a secondary axis. The component integrates into the existing Weather tab and updates dynamically when a user searches for a new location.
*   **Acceptance Criteria:**
    *   [ ] A `WeatherChartPanel` component is created using JFreeChart library.
    *   [ ] The chart displays hourly temperature as a coral red line with circular data points.
    *   [ ] Precipitation probability is shown as blue semi-transparent bars on a secondary Y-axis (0-100%).
    *   [ ] The X-axis displays hour labels in "ha" format (e.g., "9AM", "2PM") angled at 45° for readability.
    *   [ ] The chart updates automatically when weather data is fetched for a new location.
    *   [ ] The chart handles edge cases gracefully (null data, empty list, single hour, extreme temperatures).
    *   [ ] The chart appears between the current weather display and the 7-day forecast list in the Weather tab.
    *   [ ] An `HourlyForecast` model is created to store hourly weather data.
*   **Definition of Done:**
    *   [ ] All code is reviewed, approved, and merged into the `main` branch.
    *   [ ] JFreeChart dependency (v1.5.4) is added to `pom.xml`.
    *   [ ] Unit tests are written for `WeatherChartPanel` covering initialization, valid data rendering, and edge cases.
    *   [ ] The `WeatherContract.View` interface is updated to include the `updateChart(List<HourlyForecast>)` method.
    *   [ ] The `WeatherService` and `WeatherRepository` are updated to support fetching hourly forecasts.
    *   [ ] The chart is visually inspected to ensure proper rendering, label visibility, and color accuracy.

---

### 6. Franklin (Frontend/GUI - Weather Icons)

*   **User Story:** As a user, I want to see a visual icon (e.g., a sun, cloud, or raindrop) next to each weather forecast that represents the overall weather condition, so I can understand the forecast at a glance.
*   **Story Description:** This is a UI enhancement task designed to improve the readability and user experience of the main dashboard. It involves adding a small but impactful visual element to complement the text-based weather description, making the interface more intuitive.
*   **Acceptance Criteria:**
    *   [ ] A set of weather icons (e.g., SVG or from an icon font library) is added to the project's assets.
    *   [ ] A mapping is implemented in the frontend code that links weather condition strings (e.g., "Sunny", "Rain", "Cloudy") to the corresponding icon.
    *   [ ] In the event list on the dashboard, the correct icon is displayed next to the weather condition text for each event.
    *   [ ] A sensible default icon is displayed if a weather condition string does not match any in the map.
*   **Definition of Done:**
    *   [ ] All code is reviewed, approved, and merged into the `main` branch.
    *   [ ] The implementation is visually inspected to ensure icons render correctly and align properly with the text.
    *   [ ] The task is completed without introducing any negative impact on page load performance.