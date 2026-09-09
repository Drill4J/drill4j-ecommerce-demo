# User Flows

## 1) Browse and discover products

1. Open Home page/screen.
2. View featured products.
3. Navigate to Product List.
4. Open Product Details for selected item.

Expected result:

- Product information is visible (name, description, price, category, stock).

## 2) Add to basket and manage quantities

1. Add product from Product List or Product Details.
2. Open Basket.
3. Increase/decrease quantity.
4. Remove items if needed.

Expected result:

- Basket total item count and subtotal are updated after each action.

## 3) Checkout with Cash on Delivery

1. From Basket, start checkout.
2. Fill shipping information.
3. Continue to payment step.
4. Confirm payment method as `Cash on Delivery`.
5. Continue to review step.
6. Place order.

Expected result:

- Order is created with status `PLACED_COD`.
- Payment method is `CASH_ON_DELIVERY`.
- Basket is cleared.

## 4) Order confirmation

1. After order placement, user is redirected to confirmation page/screen.
2. User sees order ID and summary.
3. User can continue shopping.

Expected result:

- Confirmation displays order status, payment method, and total amount.

## Web parity checklist

- Home: implemented.
- Product list: implemented.
- Product details: implemented.
- Basket: implemented.
- Checkout: implemented.
- Order confirmation: implemented.

## Mobile parity checklist

- Home: implemented.
- Product list: implemented.
- Product details: implemented.
- Basket: implemented.
- Checkout: implemented.
- Order confirmation: implemented.
