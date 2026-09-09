# Test cases from requirements (example_app)

Derived from:

- `example_app/requirements/functional-requirements.md`
- `example_app/requirements/non-functional-requirements.md`
- `example_app/docs/user-flows.md`
- `example_app/docs/api-reference.md`

## Assumptions

- Actors: unauthenticated shopper on web and mobile; backend is the source of truth for catalog, basket, and orders.
- Featured products: at least four distinct products appear on the home/featured area (FR-1).
- Product identifiers: valid IDs come from `GET /products` and `GET /products/{id}` (FR-7, API reference).
- Basket quantity: backend enforces `quantity >= 1` on add and update; removing a line uses remove/delete, not `quantity = 0` (matches `AddBasketItemRequest` / `UpdateBasketItemRequest`).
- Shipping validation: all shipping fields are required non-blank at the API (`ShippingInfoRequest`); UI blocks or shows field errors near relevant fields (FR-5.1, NFR-4).
- Payment: only Cash on Delivery / `CASH_ON_DELIVERY` in checkout and order (FR-5.2, user flow 3).

## Test cases

| ID | Title | Type | Priority | Requirement reference | Preconditions | Steps | Expected result |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TC-01 | Home shows title, welcome, and navigation | UI | Major | FR-1 (title, welcome, nav to list and basket) | App running; catalog available | 1) Open home. 2) Verify title/welcome. 3) Verify product list + basket links. | Title and welcome visible; links present and navigable. |
| TC-02 | Home featured section shows at least four products | UI | Major | FR-1 (>=4 featured products) | Same as TC-01 | 1) Open home. 2) Count featured products. | At least four products shown. |
| TC-03 | Product list shows card fields and opens details | UI | Major | FR-2 (image, name, price, details access) | Products in catalog | 1) Open product list. 2) Inspect a card. 3) Open product details. | Card shows image/name/price; details view opens for selected product. |
| TC-04 | Add to basket from product list | Functional | Critical | FR-2 (add from list); user flow 2 | Product list open | 1) Add product from list. 2) Open basket. | Product line appears; subtotal/count update correctly. |
| TC-05 | Product details show full fields and stock | UI | Major | FR-3 (name, description, price, image, stock) | Valid product ID | 1) Open product details. | Name/description/price/image/stock status visible. |
| TC-06 | Add to basket from details with selected quantity | Functional | Critical | FR-3 (select quantity + add); user flow 2 | Product details open | 1) Set quantity > 1. 2) Add to basket. 3) Open basket. | Basket line quantity and line total match; totals update. |
| TC-07 | View basket with subtotal and item count | UI | Major | FR-4 (basket contents; subtotal and count) | Basket has items | 1) Open basket. | Subtotal and total item count match basket lines. |
| TC-08 | Increase line quantity in basket | Functional | Major | FR-4 (increase quantity) | Item in basket | 1) Increase quantity. 2) Observe totals. | Quantity/line total/subtotal/count refresh correctly. |
| TC-09 | Decrease line quantity in basket | Functional | Major | FR-4 (decrease quantity) | Line qty >= 2 | 1) Decrease quantity. 2) Observe totals. | Totals and count stay consistent. |
| TC-10 | Remove basket line | Functional | Major | FR-4 (remove item) | Item in basket | 1) Remove item. 2) Open basket. | Line removed; subtotal/count update. |
| TC-11 | Start checkout from basket | UI | Critical | FR-5 (start checkout); user flow 3 | Non-empty basket | 1) Start checkout from basket. | Checkout begins at shipping (or first step) without skipping basket context. |
| TC-12 | Shipping step validates required fields | Functional | Critical | FR-5.1 (required fields validated); NFR-4 | Checkout on shipping step | 1) Leave a required field empty/whitespace. 2) Attempt continue/submit. | User cannot proceed; error shown near field or in summary. |
| TC-13 | Complete shipping with valid fields | Functional | Critical | FR-5.1 | Shipping started | 1) Enter full name/phone/address/city/postal code. 2) Proceed. | Advances to payment step; entered data preserved. |
| TC-14 | Payment step offers only Cash on Delivery | UI | Major | FR-5.2 | Payment step | 1) Review payment options. | Only COD option available; no external integrations required. |
| TC-15 | Review shows shipping, items, total, and COD | UI | Critical | FR-5.3 | Review step reachable with filled shipping + basket | 1) Open review. | Review shows shipping, item lines/quantities, total, and COD selection. |
| TC-16 | Place order creates PLACED_COD and clears basket | Functional | Critical | FR-5.4; user flow 3 | Review ready with valid data | 1) Confirm place order. 2) Return to basket/home. | Order created with status `PLACED_COD`; basket cleared. |
| TC-17 | Order confirmation shows ID, summary, and COD message | UI | Critical | FR-6; user flow 4 | Order just placed | 1) Open confirmation screen. | Order ID + summary + COD confirmation message visible. |
| TC-18 | Continue shopping after confirmation | UI | Minor | FR-6; user flow 4 | Confirmation screen | 1) Continue shopping. | User can browse again; no stale cleared basket state inconsistencies. |
| TC-19 | API list products returns 200 and product array | API | Major | FR-7; `GET /products` | Backend up | 1) `GET /api/v1/products`. | `200` with product list fields per API reference. |
| TC-20 | API product by ID: success and not found | API | Major | FR-7; `GET /products/{productId}` | Known + unknown IDs | 1) `GET` known ID. 2) `GET` unknown ID. | Known: `200`. Unknown: error response with human-readable message (e.g. `404`). |
| TC-21 | API add basket item validation | API | Major | FR-7; NFR-3; `POST /basket/items` | Basket identity per app | 1) `POST` with missing/blank `productId`. 2) `POST` with `quantity=0`. | Validation fails (e.g. `400`) with clear message; basket not corrupted. |
| TC-22 | API update basket quantity boundary | API | Major | FR-7; `PATCH /basket/items/{productId}` | Item exists | 1) `PATCH` with `quantity=0` or negative. | Rejected via validation (explicit error). |
| TC-23 | API delete basket item | API | Major | FR-7; `DELETE /basket/items/{productId}` | Item exists | 1) `DELETE` line. 2) `GET /basket`. | `204` on delete; item absent from basket. |
| TC-24 | API checkout preview returns summary and COD | API | Major | FR-7; `POST /checkout/preview` | Basket state defined | 1) `POST` preview with full shipping. | `200` with shipping/items/total and `paymentMethod: CASH_ON_DELIVERY`. |
| TC-25 | API place order from basket creates PLACED_COD and clears basket | API | Critical | FR-7; `POST /orders` | Non-empty basket; valid shipping | 1) `POST /orders`. 2) `GET /basket`. | `201` with status `PLACED_COD` and COD payment; basket cleared. |
| TC-26 | API get order by ID | API | Major | FR-7; `GET /orders/{orderId}` | Existing order ID | 1) `GET /orders/{orderId}`. | `200` with order data matching placed order. |
| TC-27 | API shipping validation for preview/order | API | Critical | FR-5.1; NFR-3 | REST client | 1) `POST` preview or order with blank `postalCode` (or another required field). | `400` with error JSON containing human-readable `message`. |
| TC-28 | Product list performance: < 500ms locally | Performance | Minor | NFR-2 (<500 ms list) | Local dataset | 1) Measure `GET /products` or UI list load timing. | Under 500 ms in the tested local environment. |
| TC-29 | Basket update reflected in UI within 300ms | Performance | Minor | NFR-2 (<300 ms reflect) | UI under normal local conditions | 1) Add/change quantity. 2) Measure time until totals update in UI. | Under 300 ms in normal local conditions (record env). |
| TC-30 | Web/mobile parity for core checkout flow | Functional | Critical | FR-8 | Web + mobile builds | 1) On each platform: browse -> basket -> checkout COD -> confirmation. | Same business outcome: order `PLACED_COD`, basket cleared, confirmation shows ID and COD. |

## Coverage gaps

- Out-of-stock behavior: FR-3 includes stock status, but requirements do not state whether add-to-basket is blocked when `inStock=false`.
- Shipping field formats: requirements say “validated” but do not define phone/postal formats, max lengths, or international rules.
- Featured vs full catalog: FR-1 requires >=4 featured products but does not specify selection rules.
- Concurrency: NFR-3 mentions transactional order creation, but multi-tab/parallel behavior is not specified.
- Authentication/session mechanics: not covered in FR list; affects how basket is identified in API/UI automation.
- Confirmation deep link consistency: whether `GET /orders/{orderId}` must exactly match confirmation screen fields (rounding/taxes/discounts) is not specified.