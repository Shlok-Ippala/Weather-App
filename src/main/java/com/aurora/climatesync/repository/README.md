# Repository Layer (Interface Adapters - Gateways)

This package defines the **Interfaces** for data access and external services.

## Responsibilities
- Defines the contract for how the application accesses data.
- Allows the `service` layer to remain independent of specific data sources.

## Rules
- **Interfaces Only**: This package should primarily contain interfaces.
- **Dependency Inversion**: The `service` layer depends on these interfaces, while the `infrastructure` layer implements them.
