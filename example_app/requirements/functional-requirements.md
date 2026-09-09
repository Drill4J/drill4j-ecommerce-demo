# Functional Requirements

## FR-1 Home Page

- The system shall display a home page with:
  - App title and short welcome content.
  - A featured products section (at least 4 products).
  - Navigation links to product list and basket.

## FR-2 Product List

- The system shall display a list of available products.
- The default number of products should be 8
- The system shall display pagination controls if total number of products is more than default
- The pagination controls shall include:
  - Next button, which should be disabled on the last page 
  - Previous button, which should be disabled on the first page
  - Label with currently selected page
- Each product card shall include:
  - Product image.
  - Product name.
  - Product price.
  - Quick access to product details.
- The user shall be able to add a product to basket from list view.

## FR-3 Product Details

- The system shall display full product details:
  - Name, description, price, image, stock status.
- The user shall be able to select quantity and add to basket.

## FR-4 Basket

- The user shall be able to view basket contents.
- For each basket item, the user shall be able to:
  - Increase quantity.
  - Decrease quantity.
  - Remove item.
- The system shall show basket subtotal and total item count.

## FR-5 Checkout Flow

- The user shall be able to start checkout from basket.
- Checkout shall include steps:
  1. Shipping information.
  2. Payment method.
  3. Order review.
  4. Place order.

### FR-5.1 Shipping Information

- The user shall provide:
  - Full name.
  - Phone number.
  - Address line.
  - City.
  - Postal code.
- Required fields shall be validated before proceeding.

### FR-5.2 Payment

- The system shall provide only one payment option: Cash on Delivery.
- No external payment integration shall be required.

### FR-5.3 Order Review

- The user shall see:
  - Shipping information.
  - Basket items and quantities.
  - Total amount.
  - Selected payment method (COD).

### FR-5.4 Place Order

- When user confirms checkout, the system shall create a new order.
- The order shall be marked as `PLACED_COD`.
- The basket shall be cleared after successful order placement.

## FR-6 Order Confirmation

- After order placement, the system shall display:
  - Order ID.
  - Order summary.
  - Confirmation message indicating COD.

## FR-7 Authentication

### FR-7.1 Auth Service

- The system shall provide an authentication service called `auth` that is deployed separately from the backend service.
- The `auth` service shall store users in an in-memory data store (e.g., an in-memory map).

### FR-7.2 Sign Up API

- The `auth` service shall provide an API to register a new user.
- Sign up shall require:
  - Email.
  - Password.
- The `auth` service shall reject sign up if a user with the same email is already registered.

### FR-7.3 Sign In API

- The `auth` service shall provide an API to sign in an existing user using:
  - Email.
  - Password.
- On successful sign in, the `auth` service shall issue a JWT access token representing the signed-in user.
- The `auth` service shall provide an API to verify a JWT access token and return the authenticated user identity (for backend-to-auth integration).

### FR-7.4 Session Management (JWT)

- The system shall use JWT (JSON Web Tokens) to represent authenticated user sessions.
- The JWT shall include a stable user identifier (e.g., user ID or email) that can be used for user-scoping basket and orders.
- The frontend app shall store the JWT token in a cookie.
- For authenticated requests, the frontend app shall attach the token using the `Authorization: Bearer <token>` HTTP header.

### FR-7.5 Authorization Rules

- Basket and order functionality shall be available only to signed-in users.
- If a not-signed-in user attempts to access basket or order functionality from the frontend app, the user shall be redirected to a sign-in form.
- Basket contents shall be stored and retrievable only for the respective signed-in user (i.e., basket is user-scoped per session).
- Orders shall be stored and retrievable only for the respective signed-in user (i.e., orders are user-scoped).

## FR-8 API 

- Backend shall provide APIs to:
  - List products.
  - Read product details.
  - Read and update basket.
  - Create order from basket.
  - Read order by ID.
  - List orders for the signed-in user.

## FR-9 Account Page (Web)

- The web frontend shall provide a dedicated account page for signed-in users.
- The account page shall display:
  - Signed-in user's email address.
  - A list of orders for the signed-in user.

## FR-10 Platform Parity

- Web and mobile apps shall support the same core business flow:
  - Browse products -> manage basket -> checkout with COD -> view confirmation.
