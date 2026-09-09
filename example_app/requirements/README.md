# Example E-commerce App Requirements

## Goal

Build an example e-commerce application with:

- Java-based backend API.
- React-based web frontend.
- Mobile app for iOS and Android.

The app must support product discovery, basket management, and checkout with
Cash on Delivery (COD) only.

## In Scope

- Home page.
- Product list page.
- Product details page.
- Basket page.
- Checkout flow (shipping, payment selection, review, place order).
- Order confirmation page.

## Out of Scope (MVP)

- Online payment gateways and payment integrations.
- User authentication and account management.
- Inventory management dashboard.
- Shipment tracking integrations.
- Promotions and coupons.

## System Components

- `backend` - Java REST API, business logic, persistence.
- `frontend` - React web application.
- `mobile` - React Native application for iOS and Android.

## Core Entities

- Product
- Basket
- Basket item
- Order
- Order item
- Shipping information

## References

- Functional requirements: `functional-requirements.md`
- Non-functional requirements: `non-functional-requirements.md`
