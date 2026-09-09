# Drill4J E-commerce Demo

A full-stack e-commerce demo application with Drill4J coverage and test-impact integration.

- **Backend** — Java 17 / Spring Boot, Gradle, REST API on `/api/v1`
- **Auth** — Java 17 / Spring Boot, JWT auth API on `/api/v1/auth` (sign up / sign in)
- **Frontend** — React, TypeScript, Vite, served by nginx
- **Docker** — images built locally from `./example_app/*`
- **CI** — GitLab CI builds, tests, and verifies Docker images (no remote registry)

---

## Local development (without Docker)

### Development prerequisites

- JDK 17+ (`java -version`)
- Node.js 20+ (`node -v`)

### Backend

```bash
cd example_app/backend
gradle bootRun
# API available at http://localhost:8080/api/v1
```

Run tests:

```bash
gradle test
```

### Frontend

```bash
cd example_app/frontend
npm install
npm run dev
# UI available at http://localhost:5173
```

By default the frontend points to `http://localhost:8080/api/v1`. Override with:

```bash
API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

### API endpoints

| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/v1/auth/signup` | Create user, return JWT token |
| POST | `/api/v1/auth/signin` | Sign in, return JWT token |
| GET | `/api/v1/products` | List all products |
| GET | `/api/v1/products/{id}` | Product details |
| GET | `/api/v1/basket` | Get basket |
| POST | `/api/v1/basket/items` | Add item to basket |
| PATCH | `/api/v1/basket/items/{id}` | Update basket item |
| DELETE | `/api/v1/basket/items/{id}` | Remove basket item |
| POST | `/api/v1/checkout/preview` | Preview order |
| POST | `/api/v1/orders` | Place order |
| GET | `/api/v1/orders/{id}` | Get order |

---

## Local deployment with Docker Compose

### Prerequisites

- Docker with Compose plugin (`docker compose version`)

### Steps

```bash
cd docker/ecommerce
cp .env.example .env   # if you don't already have .env
docker compose up -d --build
```

Verify:

```bash
curl http://localhost:8080/api/v1/products   # backend
open http://localhost                        # frontend (port 80)
```

Stop:

```bash
docker compose down
```

Images are built from `../../example_app/auth`, `../../example_app/backend`, and `../../example_app/frontend` relative to `docker/ecommerce`.

---

## Building images locally

```bash
docker build -t drill4j-ecommerce-demo/auth:local ./example_app/auth
docker build -t drill4j-ecommerce-demo/backend:local ./example_app/backend
docker build -t drill4j-ecommerce-demo/frontend:local ./example_app/frontend
```

---

## CI/CD pipeline

The pipeline runs on:

- **Push to `develop`** — Gradle build/test and local Docker image builds
- **Git tag creation** — same

```text
build (Gradle assemble + Drill) → test (Gradle check)
                                → docker-build-auth / backend / frontend
```

Images are built in CI only (not pushed to a remote registry). Pipelines are **skipped** for commits that only change documentation (`.md` files).

---

## Configuration reference

### Auth (runtime — no rebuild needed)

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | HTTP port inside the container |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Comma-separated list of allowed browser origins |
| `AUTH_JWT_SECRET` | `dev-secret-change-me-please-set-a-longer-secret` | JWT signing secret |
| `AUTH_JWT_TTL_SECONDS` | `86400` | Token lifetime in seconds |
| `JAVA_TOOL_OPTIONS` | `-Xms256m -Xmx512m` | JVM flags (heap size, GC, etc.) |

### Backend (runtime — no rebuild needed)

| Variable | Default | Description |
| --- | --- | --- |
| `SERVER_PORT` | `8080` | HTTP port inside the container |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Comma-separated list of allowed browser origins |
| `JAVA_TOOL_OPTIONS` | `-Xms256m -Xmx512m` | JVM flags (heap size, GC, etc.) |

#### CORS examples

```bash
# Single production origin
CORS_ALLOWED_ORIGINS=https://shop.example.com

# Multiple origins
CORS_ALLOWED_ORIGINS=https://shop.example.com,https://staging.example.com
```

#### JVM tuning examples

```bash
# Low-memory server
JAVA_TOOL_OPTIONS=-Xms128m -Xmx256m

# High-throughput server with G1GC logging
JAVA_TOOL_OPTIONS=-Xms1g -Xmx2g -XX:+UseG1GC -Xlog:gc
```

### Frontend (build-time — requires image rebuild)

| Variable | Default | Description |
| --- | --- | --- |
| `API_BASE_URL` | `http://localhost:8080/api/v1` | Backend API URL as seen from the user's browser |

### Port mapping

| Service | Container port | Host port env var | Default |
| --- | --- | --- | --- |
| auth | 8081 | `AUTH_HOST_PORT` | `8081` |
| backend | 8080 | `BACKEND_HOST_PORT` | `8080` |
| frontend | 80 | `FRONTEND_HOST_PORT` | `80` |

### Drill4J

Defaults target a local Drill Admin at `http://localhost:8090/api`.

| Variable | Default | Description |
| --- | --- | --- |
| `DRILL_API_URL` | `http://localhost:8090/api` | Drill Admin API base URL (Gradle / host). Docker Compose defaults to `http://host.docker.internal:8090/api` so the backend container can reach Admin on the host. |
| `DRILL_API_KEY` | (see `.env.example`) | Drill Admin API key |
| `DRILL_ENABLED` | unset | Set to `true` to enable Gradle `coverage()` / `testTracing()` |

### E2E tests

Playwright suite under `automation/e2e`. First-time setup:

```bash
cd automation/e2e
./init.sh          # or init.cmd on Windows CMD
# use ./init.sh --insecure if npm/Playwright downloads fail TLS verification
```

See `automation/e2e/README.md`.

| Variable | Description |
| --- | --- |
| `E2E_BASE_URL` | UI base URL (default `http://localhost`, frontend on port 80). |
