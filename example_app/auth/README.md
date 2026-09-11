# Example E-commerce Auth Service

Java/Spring Boot authentication service for the example e-commerce app.

## Features

- User sign up (email + password) with duplicate email prevention.
- User sign in (email + password).
- JWT access token issuance.
- In-memory user store (example-app friendly).

## Tech Stack

- Java 17+
- Spring Boot
- Gradle

## Run

From `example_app/auth` (reuses the backend Gradle wrapper):

```bash
../backend/gradlew -p . bootRun
```

API base URL: `http://localhost:8081/api/v1`

## Configuration

- `SERVER_PORT` (default: `8081`)
- `CORS_ALLOWED_ORIGINS` (default: `http://localhost:5173,http://127.0.0.1:5173`)
- `AUTH_JWT_SECRET` (default: `dev-secret-change-me-please-set-a-longer-secret`)
- `AUTH_JWT_TTL_SECONDS` (default: `86400`)

## Test

Unit and API tests live under `src/test`, tagged with `@Tag("unit")` / `@Tag("api")`.
Drill4J CI/CD plugin `0.11.0` instruments `unitTest` / `apiTest` (Admin at `http://localhost:8090/api`; set `DRILL_API_KEY` like Playwright / `docker/ecommerce/.env`).

From `example_app/auth`:

```bash
export DRILL_API_KEY=...   # same key as E2E
../backend/gradlew -p . unitTest   # unit only — session tracing on, per-test tracing off
../backend/gradlew -p . apiTest    # API only — session + per-test tracing on
../backend/gradlew -p . test       # all (no Drill agent on this combined task)
```

## Main Endpoints

- `POST /auth/signup`

Request payload:

```json
{
  "email": "user@example.com",
  "password": "pass1234"
}
```

Response payload (201):

```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "token": "jwt"
}
```

- `POST /auth/signin`

Request payload:

```json
{
  "email": "user@example.com",
  "password": "pass1234"
}
```

Response payload (200):

```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "token": "jwt"
}
```

