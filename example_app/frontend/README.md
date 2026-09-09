# Example E-commerce Frontend

React web frontend for the example e-commerce app.

## Features

- Home page with featured products.
- Product list and product details pages.
- Basket page with quantity updates and remove actions.
- Checkout flow:
  - Shipping information
  - Payment method (Cash on Delivery)
  - Order review
  - Order confirmation

## Run

From `example_app/frontend`:

```bash
npm install
npm run dev
```

## Backend API URL

By default, the app uses `http://localhost:8080/api/v1`.

You can override with:

```bash
API_BASE_URL=http://localhost:8080/api/v1
```

## Documentation

- Setup: `../docs/setup-guide.md`
- API: `../docs/api-reference.md`
- User flows: `../docs/user-flows.md`
