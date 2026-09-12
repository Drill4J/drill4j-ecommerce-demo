# Incremental changes (feature + partial test plan)

Build order for evolving the demo so backend/auth changes are only partly covered by tests — leaving gaps for Drill4J to detect.

Each step is roughly 1–2 commits (feature + intentional partial tests).

## Test types

Every track should include (where the change applies) these three layers against **auth** and/or **ecommerce backend**:

| Type | Where | Role |
|------|--------|------|
| **Unit** | `example_app/backend` or `example_app/auth` — `@Tag("unit")`; run `gradle unitTest` | Service/domain logic in isolation |
| **API** | Same modules — `@Tag("api")`; run `gradle apiTest` | HTTP contracts, status codes (`@WebMvcTest` / integration) |
| **E2E** | `automation/e2e` (Playwright) | Full UI path through frontend → auth/backend |

For each track, **Cover** is what that layer should assert; **Gap** is left untested on purpose (Drill4J signal).

---

## Summary

| # | Change | User flow | Touches | Unit cover | API cover | E2E cover | Primary gaps |
|---|--------|-----------|---------|------------|-----------|-----------|--------------|
| [C1](#c1-category-filter-on-catalog) | Category filter on catalog | [5](user-flows.md#5-filter-catalog-by-category) | backend, frontend | Filter + page | `?category=` happy | Select category on list | Unknown category; category+search |
| [C2](#c2-stock-aware-basket-quantity-limits) | Stock-aware basket qty | [6](user-flows.md#6-enforce-stock-aware-quantity-limits) | backend | Add within stock | POST add within limit | Add valid qty | PATCH over stock; merge overflow |
| [C3](#c3-stock-revalidation-on-place-order) | Stock revalidation on place | [6](user-flows.md#6-enforce-stock-aware-quantity-limits) | backend | Place with valid stock | Place success path | Checkout with in-stock basket | Mid-session OOS place failure |
| [C4](#c4-promo-codes-at-checkout) | Promo codes | [7](user-flows.md#7-apply-promo-code-at-checkout) | backend, frontend | One %-off apply | Preview/place with code | Enter valid promo in UI | Expired / min / fixed / bad code |
| [C5](#c5-delivery-fee-by-destination) | Delivery fee by city | [8](user-flows.md#8-delivery-fee-by-destination) | backend, frontend | Known city fee | Preview shows fee | Checkout shows fee | Unknown city; free-threshold |
| [C6](#c6-saved-shipping-addresses) | Saved addresses | [9](user-flows.md#9-save-and-reuse-shipping-addresses) | backend, frontend | Create + list | Create/list/use on place | Save + select in checkout | Delete default; wrong-user |
| [C7](#c7-cancel-placed-cod-order) | Cancel COD order | [10](user-flows.md#10-cancel-a-placed-cod-order) | backend, frontend | Cancel `PLACED_COD` | Cancel endpoint 200 | Cancel from Account | Already cancelled; window; other user |
| [C8](#c8-reorder-from-order-history) | Reorder | [11](user-flows.md#11-reorder-from-order-history) | backend, frontend | Full in-stock reorder | Reorder → basket | Reorder from Account | Partial/all OOS skip paths |
| [C9](#c9-minimum-order-amount) | Min order amount | [12](user-flows.md#12-minimum-order-amount) | backend | Reject below / allow above | Preview/place below → 4xx | — (API-heavy; optional thin E2E) | Exact threshold; promo below min |
| [C10](#c10-change-account-password) | Change password | [13](user-flows.md#13-change-account-password) | auth, frontend | Change success | Auth change-password API | Account UI change + re-login | Wrong current; weak/same-as-old |
| [C11](#c11-stricter-signup-passwordemail-rules) | Stricter signup rules | [13](user-flows.md#13-change-account-password) (related) | auth | Valid signup under rules | Signup still 201 | Signup with valid creds | Weak password branches; email edge |
| [C12](#c12-wishlist) | Wishlist | [14](user-flows.md#14-wishlist-save-for-later) | backend, frontend | Add/list/remove | Wishlist CRUD happy | Add + open wishlist UI | Duplicate; OOS rule; move-to-basket fail |
| [C13](#c13-featured-products-endpoint) | Featured products endpoint | [1](user-flows.md#1-browse-and-discover-products) | backend, frontend | Featured list non-empty | `GET` featured | Home shows featured from API | Empty featured set; flag filtering |

---

## C1) Category filter on catalog

- **User flow:** [5) Filter catalog by category](user-flows.md#5-filter-catalog-by-category)
- **Touches:** backend, frontend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend `ProductService` | Filter by known category + pagination | Unknown category → empty; category + search combo |
| API | `GET /api/v1/products?category=` | 200 + filtered body for known category | Unknown/empty category response shape; combined `search`+`category` |
| E2E | Product list UI | Select a category chip/filter; list updates | Clear filter + search together; empty-category UX |

## C2) Stock-aware basket quantity limits

- **User flow:** [6) Enforce stock-aware quantity limits](user-flows.md#6-enforce-stock-aware-quantity-limits)
- **Touches:** backend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend `BasketService` | Add quantity within stock/max | `updateQuantity` over stock; merge-on-add overflow |
| API | `POST /basket/items` | Add within limit → 200 | `PATCH` over stock; repeated POST merge overflow |
| E2E | Basket / product details | Add a valid quantity successfully | UI attempt to exceed stock (error path) |

## C3) Stock revalidation on place-order

- **User flow:** [6) Enforce stock-aware quantity limits](user-flows.md#6-enforce-stock-aware-quantity-limits)
- **Touches:** backend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend `OrderService` | Place succeeds when all lines in stock | Place when a line went OOS; basket not cleared |
| API | `POST /orders` | Happy place after stocked basket | Forced OOS-before-place scenario |
| E2E | Checkout | Complete COD checkout with in-stock items | No E2E for mid-session stock loss |

## C4) Promo codes at checkout

- **User flow:** [7) Apply promo code at checkout](user-flows.md#7-apply-promo-code-at-checkout)
- **Touches:** backend, frontend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend promo / `OrderService` | Apply one valid %-off code | Expired; min-subtotal; fixed-amount; invalid code |
| API | preview + place with `promoCode` | Valid code reduces total | 4xx branches for bad/expired/min/fixed |
| E2E | Checkout UI | Enter valid promo; see discounted total; place | Invalid promo messaging paths |

## C5) Delivery fee by destination

- **User flow:** [8) Delivery fee by destination](user-flows.md#8-delivery-fee-by-destination)
- **Touches:** backend, frontend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend fee / `OrderService` | Fee for one known city | Unknown city default; free-delivery threshold |
| API | checkout preview | Known city → fee in totals | Unknown city; free-threshold |
| E2E | Checkout | Shipping city shows expected fee on review | Switch to unknown city / free-threshold UI |

## C6) Saved shipping addresses

- **User flow:** [9) Save and reuse shipping addresses](user-flows.md#9-save-and-reuse-shipping-addresses)
- **Touches:** backend, frontend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend address service | Create + list for user | Delete default; update validation |
| API | address CRUD + place with `addressId` | Create, list, place using saved id | Wrong-user 404; delete-default side effects |
| E2E | Account / Checkout | Save address; select it at checkout; place | Edit/delete default flows |

## C7) Cancel placed COD order

- **User flow:** [10) Cancel a placed COD order](user-flows.md#10-cancel-a-placed-cod-order)
- **Touches:** backend, frontend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend `OrderService` | Cancel `PLACED_COD` → `CANCELLED` | Already cancelled; outside window; other user |
| API | `POST /orders/{id}/cancel` | 200 + cancelled status | 4xx for double-cancel / window / foreign order |
| E2E | Account orders | Place then cancel from UI | Second cancel / expired-window UI |

## C8) Reorder from order history

- **User flow:** [11) Reorder from order history](user-flows.md#11-reorder-from-order-history)
- **Touches:** backend, frontend
- **Status:** done

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend reorder / basket | Full in-stock reorder merges into basket | Partial OOS skip; all-OOS no-op |
| API | reorder endpoint | 200 + basket contains lines | Partial/all OOS response payloads |
| E2E | Account | Reorder in-stock order; basket updated | Partial skip messaging |

## C9) Minimum order amount

- **User flow:** [12) Minimum order amount](user-flows.md#12-minimum-order-amount)
- **Touches:** backend
- **Status:** planned

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend `OrderService` | Reject below min; allow at/above | Exact threshold boundary; promo drops below min |
| API | preview / place | Below min → 4xx; above → ok | Exact boundary; promo interaction |
| E2E | Checkout (thin) | Optional: one below-min blocked place | Prefer leaving promo-below-min to gaps |

## C10) Change account password

- **User flow:** [13) Change account password](user-flows.md#13-change-account-password)
- **Touches:** auth, frontend
- **Status:** planned

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | auth `AuthService` | Change password success | Wrong current password; weak / same-as-old |
| API | auth change-password endpoint | 200 + subsequent sign-in with new password | 4xx policy / wrong-current branches |
| E2E | Account UI | Change password; sign out; sign in with new | Error toasts for rejected policy |

## C11) Stricter signup password/email rules

- **User flow:** Related to [13) Change account password](user-flows.md#13-change-account-password) (shared auth policy)
- **Touches:** auth
- **Status:** planned

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | auth `AuthService` / validation | Signup with valid email+password succeeds | Weak password branches; email normalize edges |
| API | `POST .../auth/signup` | Valid payload → 201 | Weak/invalid payload 4xx matrix |
| E2E | Signup page | Successful signup under new rules | Rejected weak-password UI paths |

## C12) Wishlist

- **User flow:** [14) Wishlist (save for later)](user-flows.md#14-wishlist-save-for-later)
- **Touches:** backend, frontend
- **Status:** planned

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend wishlist service | Add, list, remove | Duplicate add; OOS rule; move-to-basket failure |
| API | wishlist endpoints | CRUD happy path | Duplicate / OOS / move-to-basket error contracts |
| E2E | Product + Wishlist UI | Add to wishlist; open list; remove | Move-to-basket failure / OOS save |

## C13) Featured products endpoint

- **User flow:** [1) Browse and discover products](user-flows.md#1-browse-and-discover-products)
- **Touches:** backend, frontend
- **Status:** planned

| Layer | Target | Cover | Gap |
|-------|--------|-------|-----|
| Unit | backend `ProductService` | Featured list returns flagged products | Empty featured set; flag filtering edge |
| API | `GET` featured products | 200 + non-empty featured payload | Empty catalog / no-featured response |
| E2E | Home | Featured section loads from new endpoint | Empty-state home featured |
