# Example E-commerce Backend

Java/Spring Boot backend for the example e-commerce app.

## Features

- Product list and product details APIs.
- Basket CRUD APIs.
- Checkout preview and order placement APIs.
- Cash on Delivery (COD) payment method only.

## Tech Stack

- Java 17+
- Spring Boot
- Gradle

## Run

From `example_app/backend`:

```bash
gradle bootRun
```

API base URL: `http://localhost:8080/api/v1`

## Documentation

- Setup: `../docs/setup-guide.md`
- API: `../docs/api-reference.md`
- User flows: `../docs/user-flows.md`

## Test

Unit and API tests live under `src/test`, tagged with `@Tag("unit")` / `@Tag("api")`.
Drill4J CI/CD plugin `0.11.0` instruments `unitTest` / `apiTest` (Admin at `http://localhost:8090/api`; set `DRILL_API_KEY` like Playwright / `docker/ecommerce/.env`).

From `example_app/backend`:

```bash
export DRILL_API_KEY=...   # same key as E2E
gradle unitTest   # unit only — session tracing on, per-test tracing off
gradle apiTest    # API only — session + per-test tracing on
gradle test       # all (no Drill agent on this combined task)
```

## Main Endpoints

- `GET /products`
- `GET /products/{productId}`
- `GET /basket`
- `POST /basket/items`
- `PATCH /basket/items/{productId}`
- `DELETE /basket/items/{productId}`
- `POST /checkout/preview`
- `POST /orders`
- `GET /orders/{orderId}`
