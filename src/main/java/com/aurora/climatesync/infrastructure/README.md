# Infrastructure Layer (Frameworks & Drivers)

This package contains the **Implementation Details**.

## Responsibilities
- Implements the interfaces defined in the `repository` layer.
- Handles communication with external agencies (Databases, Web APIs, File Systems).

## Rules
- **Dependencies**: Depends on `repository` and `model`.
- **Isolation**: Changes here (e.g., switching from Google Calendar to Outlook) should not affect the core application logic.
