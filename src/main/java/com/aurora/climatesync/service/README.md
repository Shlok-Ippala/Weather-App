# Service Layer (Use Cases)

This package contains the **Application Business Rules**.

## Responsibilities
- Orchestrates the flow of data to and from the entities (`model`).
- Implements specific business use cases (e.g., "Get Upcoming Events", "Search Weather").

## Rules
- **Dependencies**: Can depend on the `model` layer.
- **Interfaces**: Should define interfaces for data access (Repositories) but NOT implement them.
- **No UI/Frameworks**: Should not depend on UI components (Swing) or Infrastructure details (Google API, SQL).
