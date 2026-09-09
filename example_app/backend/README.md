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

From `example_app/backend`:

```bash
gradle test
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
