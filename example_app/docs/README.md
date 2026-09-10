# Example E-commerce Documentation

This folder contains implementation and usage documentation for the example e-commerce app.

## Documents

- [Setup guide](setup-guide.md)
- [API reference](api-reference.md)
- [User flows](user-flows.md)
- [Incremental changes](incremental-changes.md) (feature + partial-test build order)

## App structure

- `example_app/backend` - Java + Spring Boot API (unit + API tests under `src/test`)
- `example_app/auth` - Java + Spring Boot JWT auth (unit + API tests under `src/test`)
- `example_app/frontend` - React web app
- `example_app/mobile` - React Native (Expo) app for iOS/Android
- `automation/e2e` - Playwright E2E tests
