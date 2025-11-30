# View Layer (Frameworks & Drivers)

This package contains the **User Interface**.

## Responsibilities
- Displays data to the user.
- Captures user input and delegates it to the `presenter` or `controller`.

## Rules
- **Passive/Dumb**: The View should contain no business logic. It simply renders what the Presenter tells it to.
- **Dependencies**: Depends on `presenter` interfaces (Contracts).
- **Framework Specific**: This is where Swing (or any other UI framework) code lives.
