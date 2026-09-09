# E2E Test Plan (UI Click-Through)

This document defines **end-to-end (E2E) tests** where a user interacts with the **real UI** (clicking, typing, navigating) to validate core functional requirements.

## Scope

- **In scope**: Web UI flows for browsing products, basket management, checkout (COD), order confirmation, authentication, and account/orders.
- **Out of scope**: Mobile UI automation (platform parity is covered by verifying the same business flow on web).
- **Out of scope**: External payment integrations (explicitly not required; COD only).

## Assumptions / Test Preconditions

- The app has at least **4 products** visible in “Featured products” on the home page (FR-1).
- Product list includes product cards with **image, name, price, and link to details** (FR-2).
- Basket and orders require authentication; unauthenticated access redirects to sign-in (FR-7.5).
- JWT session token is stored in a **cookie** and the frontend uses `Authorization: Bearer <token>` for authenticated requests (FR-7.4).

## Recommended E2E Setup

- **Test user**: Create a unique user per test run (e.g., `e2e_<timestamp>@example.com`) to avoid sign-up collisions (FR-7.2).
- **Selectors**: Prefer stable `data-testid` attributes for critical UI elements (nav links, product cards, add-to-basket, basket item controls, checkout step controls).
- **Isolation**:
  - Clear cookies/storage before each test.
  - Keep tests independent (don’t rely on execution order).
- **Network handling**:
  - Prefer running against a real test environment.
  - Optionally allow request waiting/assertions (e.g., verify redirect occurred, or that order placement request returned success).

## Coverage Map (Requirements → E2E Areas)

- **FR-1**: Home page content + featured products + navigation
- **FR-2 / FR-3**: Product list and product details; add-to-basket from both
- **FR-4**: Basket view; quantity changes; remove; totals
- **FR-5**: Checkout multi-step flow; validation; COD; order creation; basket cleared
- **FR-6**: Order confirmation (ID + summary + COD message)
- **FR-7**: Sign up / sign in; session persistence; redirect protection
- **FR-8**: Indirectly covered via UI flows that call these APIs
- **FR-9**: Account page shows user email and list of orders

---

## Test Scenarios

Each scenario is written as a **click-through user journey** with **assertions**.

### E2E-001 Home page renders and navigation works (FR-1)

- **Precondition**: User is not signed in.
- **Steps**:
  - Open the app root (home page).
  - Verify app title and welcome content are visible.
  - Verify “Featured products” section is visible and shows **≥ 4** products.
  - Click navigation link to “Product list”.
  - Navigate back to home.
  - Click navigation link to “Basket”.
- **Expected**:
  - Product list page opens from nav.
  - Clicking “Basket” while signed out redirects to sign-in (FR-7.5).

### E2E-002 Sign up and sign in (FR-7.2, FR-7.3, FR-7.4)

- **Precondition**: Use a new email that is not registered.
- **Steps**:
  - Go to sign-in page (direct URL or via basket redirect).
  - Switch to “Sign up” form (if separate).
  - Fill email + password, submit.
  - Confirm successful registration leads to signed-in state (UI indicates user is signed in).
  - Sign out (if available), then sign in with same credentials.
- **Expected**:
  - Successful sign up logs the user in or enables sign in (implementation-specific, but must allow authentication).
  - Successful sign in results in a stable signed-in UI state (session established via cookie; FR-7.4).

### E2E-003 Sign up rejects duplicate email (FR-7.2)

- **Precondition**: An existing user email is known (can be created in test).
- **Steps**:
  - Attempt to sign up with the same email again.
- **Expected**:
  - UI shows a clear error (e.g., “email already registered”).
  - User remains not signed in (or remains on auth page).

### E2E-004 Product list shows required card fields and can open details (FR-2)

- **Precondition**: User may be signed in or signed out (no auth requirement stated for browsing).
- **Steps**:
  - Open product list page.
  - Inspect first product card.
  - Click “details” / product name / card link to open product details.
- **Expected**:
  - Product card shows image, name, price, and a clear action/link to details.
  - Product details page loads for the selected product.

### E2E-005 Add to basket from list requires auth (FR-2, FR-7.5)

- **Precondition**: User is signed out.
- **Steps**:
  - Open product list.
  - Click “Add to basket” on a product card.
- **Expected**:
  - User is redirected to sign-in form (FR-7.5) and basket is not modified.

### E2E-006 Add to basket from list as signed-in user (FR-2, FR-4, FR-7.5)

- **Precondition**: User is signed in.
- **Steps**:
  - Open product list.
  - Click “Add to basket” for a product.
  - Go to basket.
- **Expected**:
  - Basket shows the added product with quantity \( \ge 1 \).
  - Basket subtotal and total item count are visible and consistent with items shown (FR-4).

### E2E-007 Product details shows full info and add with quantity (FR-3, FR-4)

- **Precondition**: User is signed in.
- **Steps**:
  - Open product details for a product.
  - Verify name, description, price, image, and stock status are present.
  - Set quantity to 2 (or another >1 value) and click “Add to basket”.
  - Open basket.
- **Expected**:
  - Basket contains that product with the selected quantity.
  - Totals reflect the selected quantity.

### E2E-008 Basket item quantity increase/decrease/remove updates totals (FR-4)

- **Precondition**: User is signed in with at least 1 item in basket.
- **Steps**:
  - Open basket.
  - Click “Increase quantity” for an item.
  - Verify item quantity increases by 1 and subtotal/item count update.
  - Click “Decrease quantity”.
  - Verify item quantity decreases by 1 and totals update.
  - Click “Remove item”.
  - Verify the item disappears and totals update accordingly.
- **Expected**:
  - UI remains consistent: quantities, subtotal, and item count update on each action.

### E2E-009 Starting checkout from basket (FR-5)

- **Precondition**: User is signed in with at least 1 item in basket.
- **Steps**:
  - Open basket.
  - Click “Checkout”.
- **Expected**:
  - Checkout flow starts and shows step 1: shipping information (FR-5).

### E2E-010 Shipping form validation blocks progress (FR-5.1)

- **Precondition**: User is signed in with items in basket and is on shipping step.
- **Steps**:
  - Attempt to proceed without filling required fields.
  - Fill fields one by one, attempting to proceed each time.
  - Finally fill all required fields: full name, phone, address line, city, postal code.
- **Expected**:
  - Required field validation messages appear and prevent proceeding until all required fields are valid (FR-5.1).

### E2E-011 Payment step shows only Cash on Delivery (FR-5.2)

- **Precondition**: User completed shipping step.
- **Steps**:
  - Proceed to payment step.
- **Expected**:
  - Only one payment option is presented: **Cash on Delivery**.
  - No external payment form/fields appear (FR-5.2).

### E2E-012 Order review shows shipping, items, total, COD (FR-5.3)

- **Precondition**: User completed shipping + payment steps.
- **Steps**:
  - Proceed to review step.
- **Expected**:
  - Shipping information is displayed.
  - Basket items and quantities are displayed.
  - Total amount is displayed.
  - Payment method displays COD (FR-5.3).

### E2E-013 Place order creates order, clears basket, shows confirmation (FR-5.4, FR-6)

- **Precondition**: User is on final checkout step with valid shipping + COD selected.
- **Steps**:
  - Click “Place order”.
- **Expected**:
  - Confirmation page is displayed (FR-6) including:
    - Order ID
    - Order summary
    - COD confirmation message
  - Basket is cleared after successful order placement (FR-5.4).
  - (If visible in UI) order status corresponds to `PLACED_COD` (FR-5.4).

### E2E-014 Account page shows signed-in email and orders list (FR-9)

- **Precondition**: User is signed in and has at least one placed order (can be created in-test using E2E-013).
- **Steps**:
  - Navigate to account page.
- **Expected**:
  - Signed-in user email is displayed.
  - Orders list contains the recently placed order.

### E2E-015 User-scoped basket and orders (FR-7.5)

- **Precondition**: Two users exist: User A and User B.
- **Steps**:
  - Sign in as User A.
  - Add an item to basket and place an order.
  - Sign out.
  - Sign in as User B.
  - Open basket and account/orders.
- **Expected**:
  - User B’s basket does not show User A’s items.
  - User B’s orders list does not show User A’s orders.

### E2E-016 Protected routes redirect to sign-in when signed out (FR-7.5)

- **Precondition**: User is signed out.
- **Steps**:
  - Attempt to open basket page directly via URL.
  - Attempt to open account/orders page directly via URL.
- **Expected**:
  - Both redirect to sign-in form (FR-7.5).

---

## Minimal “Smoke” Suite (suggested for CI)

- **E2E-001** Home renders + featured products + nav works
- **E2E-002** Sign up/sign in
- **E2E-006** Add to basket from list (signed in)
- **E2E-013** Full checkout to confirmation + basket cleared
- **E2E-014** Account shows order

## Notes / Implementation Tips

- Keep E2E assertions focused on **what the user sees**:
  - Page heading / step title
  - Items in basket and quantities
  - Totals and confirmation messages
- Use unique emails per run to avoid flaky “duplicate signup” failures.

