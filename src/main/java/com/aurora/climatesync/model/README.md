# Model Layer (Entities)

This package contains the **Enterprise Business Rules**.

## Responsibilities
- Represents the core business objects (Entities) of the application.
- Encapsulates the most general and high-level rules.

## Rules
- **No Dependencies**: This layer must NOT depend on any other layer (Service, View, Infrastructure, etc.).
- **Plain Java**: Classes should be simple POJOs (Plain Old Java Objects).
- **Stability**: This is the most stable layer; changes in outer layers (UI, Database) should not affect this layer.
