# Presenter Layer (Interface Adapters)

This package handles the **Presentation Logic**.

## Responsibilities
- Acts as a bridge between the `view` and the `service` layers.
- Formats data from the `service` layer into a structure suitable for the `view`.
- Handles UI events delegated by the `view`.

## Rules
- **No UI Frameworks**: Ideally, presenters should be testable without the heavy UI framework (though they may reference View interfaces).
- **Dumb Views**: The logic for *what* to display goes here; the View just displays it.
