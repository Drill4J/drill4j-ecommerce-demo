# API Reference

Base URL: `http://localhost:8080/api/v1`

## Products

### `GET /products`

Returns all products.

Response `200`:

```json
[
  {
    "id": "p-1001",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse",
    "price": 24.99,
    "imageUrl": "https://picsum.photos/seed/mouse/400/300",
    "category": "Accessories",
    "inStock": true
  }
]
```

### `GET /products/{productId}`

Returns product details by ID.

## Basket

### `GET /basket`

Returns current basket.

Response `200`:

```json
{
  "items": [
    {
      "productId": "p-1001",
      "productName": "Wireless Mouse",
      "unitPrice": 24.99,
      "quantity": 2,
      "lineTotal": 49.98
    }
  ],
  "totalItems": 2,
  "subtotal": 49.98
}
```

### `POST /basket/items`

Adds item to basket.

Request:

```json
{
  "productId": "p-1001",
  "quantity": 2
}
```

Response `201`: updated basket object.

### `PATCH /basket/items/{productId}`

Updates quantity for existing basket item.

Request:

```json
{
  "quantity": 3
}
```

Response `200`: updated basket object.

### `DELETE /basket/items/{productId}`

Removes basket item.

Response `204`: no content.

## Checkout and Orders

### `POST /checkout/preview`

Returns checkout summary and payment method.

Request:

```json
{
  "shippingInfo": {
    "fullName": "Jane Doe",
    "phoneNumber": "+1-555-100-100",
    "addressLine": "Main street 10",
    "city": "Austin",
    "postalCode": "73301"
  }
}
```

Response `200`:

```json
{
  "shippingInfo": {
    "fullName": "Jane Doe",
    "phoneNumber": "+1-555-100-100",
    "addressLine": "Main street 10",
    "city": "Austin",
    "postalCode": "73301"
  },
  "items": [],
  "totalAmount": 0,
  "paymentMethod": "CASH_ON_DELIVERY"
}
```

### `POST /orders`

Creates an order from current basket and shipping info.

Request:

```json
{
  "shippingInfo": {
    "fullName": "Jane Doe",
    "phoneNumber": "+1-555-100-100",
    "addressLine": "Main street 10",
    "city": "Austin",
    "postalCode": "73301"
  }
}
```

Response `201`:

```json
{
  "orderId": "uuid-value",
  "status": "PLACED_COD",
  "shippingInfo": {
    "fullName": "Jane Doe",
    "phoneNumber": "+1-555-100-100",
    "addressLine": "Main street 10",
    "city": "Austin",
    "postalCode": "73301"
  },
  "items": [],
  "totalAmount": 0,
  "paymentMethod": "CASH_ON_DELIVERY"
}
```

### `GET /orders/{orderId}`

Returns previously created order.

## Error format

For `400`/`404` errors the API returns:

```json
{
  "message": "Human-readable error message"
}
```
