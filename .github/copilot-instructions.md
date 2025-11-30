<!-- Copilot instructions for working with the ClimateSync (Weather-App) repository -->
# Quick Orientation

This is a small Spring Boot desktop+web hybrid that syncs Google Calendar events and shows weather forecasts.
Key facts you must know to be productive:

- **App entrypoints**: `src/main/java/com/aurora/climatesync/ClimatesyncApplication.java` (Spring Boot) and
  `src/main/java/com/aurora/climatesync/view/Main.java` (launches a Swing UI via Spring, uses `.headless(false)`).
- **Service boundaries**: There are clear service interfaces in `service/` (e.g., `CalendarService`, `WeatherService`, `EventProcessingService`) with concrete `*Impl` classes annotated `@Service`.
- **Models**: POJOs in `model/` (e.g., `CalendarEvent`, `Location`, `ProcessedEvent`, `WeatherForecast`) are simple data holders used across services and view.

# Architecture & Data Flow (short)

- The Swing UI (`WeatherView`, `CalendarView`) calls service interfaces provided by Spring. Example: `WeatherView` uses `WeatherService#getWeeklyForecast(Location)`.
- `GoogleCalendarServiceImpl` handles Google OAuth and remote calls, returning `CalendarEvent` objects used by controllers/views.
- `EventProcessingServiceImpl` normalizes Google `Event` objects into `ProcessedEvent` and filters out all-day or location-less events.

# Important integration details (do not miss)

- Google OAuth: `GoogleCalendarServiceImpl` uses the Google API Java client with a local OAuth flow. It stores tokens in the working-directory folder `tokens/` and starts a `LocalServerReceiver` on **port 8888**.
- Required properties (set these before running features that touch Google Calendar):
  - `spring.security.oauth2.client.registration.google.client-id`
  - `spring.security.oauth2.client.registration.google.client-secret`
  See `src/main/resources/application.properties.example` for context.
- Calendar endpoint: `GET /events` is exposed by `CalendarController` but guarded by `SecurityConfig` — `/events` requires authentication (OAuth login), other endpoints are `permitAll()`.

# Running, building, and tests

- Build: `mvn -DskipTests=false clean package` (project uses Java 17 and Spring Boot 3.x parent in `pom.xml`).
- Run from CLI: `mvn spring-boot:run` will start the Spring app; the Swing UI launches when the `Main` class runs because it sets `headless(false)`.
- Run the UI from IDE: run the `main` method in `src/main/java/com/aurora/climatesync/view/Main.java` to start the Swing desktop UI.
- Tests: `mvn test`. Unit tests reference `src/test/resources/mock-events.json` for calendar payloads.

# Project-specific conventions & patterns

- Interface-first services: new features should add an interface under `service/` and a corresponding `*Impl` annotated with `@Service`.
- Simple error handling: many service implementations catch exceptions, `printStackTrace()`, and return safe defaults (e.g., empty lists). Follow that existing pattern unless fixing the root cause.
- Time handling: code often uses `ZonedDateTime` and `ZoneId.systemDefault()`; when adding features preserve timezone-aware types.
- Location parsing: `GoogleCalendarServiceImpl` currently sets `Location("Unknown", "Unknown", 0, 0)` — location parsing is intentionally minimal; avoid assuming rich location data from events.

# Files you will reference frequently

- `src/main/java/com/aurora/climatesync/service/GoogleCalendarServiceImpl.java` — Google OAuth, token storage, calendar API usage, `connect()` and `getUpcomingEvents()` examples.
- `src/main/java/com/aurora/climatesync/service/EventProcessingServiceImpl.java` — filtering and date-time conversion for events.
- `src/main/java/com/aurora/climatesync/service/WeatherServiceImpl.java` — REST call to Open-Meteo and mapping of weather codes.
- `src/main/java/com/aurora/climatesync/view/WeatherView.java` — UI interactions and background threading for network calls.

# Small, actionable guidance for code generation assistants

- When adding endpoints, mirror the existing security pattern in `SecurityConfig.java` — do not expose calendar-sensitive routes without `authenticated()`.
- For mockable units, prefer extracting network calls into small methods so unit tests can stub them (see how `WeatherServiceImpl#getWeeklyForecast` builds the URL and parses JSON).
- Preserve the `tokens/` storage convention for OAuth tests; test suites may assume the token-IO path.

# Common gotchas

- Running the app without `client-id`/`client-secret` will cause `GoogleCalendarServiceImpl#getCredentials` to throw an `IOException` — set the properties or avoid calling calendar features.
- The local OAuth flow uses port `8888` — ensure the port is available in development environments.

# If you need more

If anything above is unclear or you want sample tasks (add a new endpoint, mock the calendar client, or improve timezone handling), tell me which area to expand and I will update this file.
