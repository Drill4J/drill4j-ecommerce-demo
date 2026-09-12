# User Flows

## 1) Browse and discover products

1. Open Home page/screen.
2. View featured products.
3. Navigate to Product List.
4. Open Product Details for selected item.

Expected result:

- Product information is visible (name, description, price, category, stock).

Implementation track: [C13 Featured products endpoint](incremental-changes.md#c13-featured-products-endpoint)

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

## 5) Filter catalog by category

Implementation track: [C1 Category filter on catalog](incremental-changes.md#c1-category-filter-on-catalog)

1. Open Product List.
2. Select a category (e.g. Electronics, Home).
3. Optionally combine with search text.
4. Clear the category filter to see the full catalog again.

Expected result:

- Only products matching the selected category are listed.
- Pagination and search still work within the filtered set.
- Unknown/empty category returns an empty list (not an error).

## 6) Enforce stock-aware quantity limits

Implementation track: [C2 Stock-aware basket quantity limits](incremental-changes.md#c2-stock-aware-basket-quantity-limits), [C3 Stock revalidation on place-order](incremental-changes.md#c3-stock-revalidation-on-place-order)

1. Add a product to basket.
2. Increase quantity toward available stock / per-line max.
3. Attempt to exceed available stock (add or update).
4. Proceed to checkout with a valid basket.

Expected result:

- Basket rejects quantities above available stock (or configured max).
- Subtotal reflects only accepted quantities.
- Checkout re-validates stock; if a line became unavailable, place-order fails with a clear error and basket is not cleared.

## 7) Apply promo code at checkout

Implementation track: [C4 Promo codes at checkout](incremental-changes.md#c4-promo-codes-at-checkout)

1. Start checkout with items in basket.
2. Enter a promo code on review (or shipping) step.
3. Preview order totals with discount applied.
4. Place order with the same promo code.

Expected result:

- Valid promo reduces `totalAmount` (percent or fixed, per code rules).
- Order stores the applied promo code and discount amount.
- Invalid / expired / below-minimum-subtotal codes are rejected without placing the order.

## 8) Delivery fee by destination

Implementation track: [C5 Delivery fee by destination](incremental-changes.md#c5-delivery-fee-by-destination)

1. Fill shipping information with a known city.
2. Preview checkout totals.
3. Change city to another region (or an unknown city).
4. Place order.

Expected result:

- Preview and placed order include a delivery fee based on city/region rules.
- Free-delivery threshold (if configured) can zero the fee.
- Unknown city uses a default fee (not a hard failure).

## 9) Save and reuse shipping addresses

Implementation track: [C6 Saved shipping addresses](incremental-changes.md#c6-saved-shipping-addresses)

1. From Account or Checkout, save a shipping address.
2. Mark one address as default.
3. Start checkout and select a saved address instead of typing.
4. Edit or delete a saved address.

Expected result:

- Addresses are user-scoped and listed on Account / Checkout.
- Selecting a saved address pre-fills shipping fields for preview/place.
- Deleting the default clears default; another address can be set as default.

## 10) Cancel a placed COD order

Implementation track: [C7 Cancel placed COD order](incremental-changes.md#c7-cancel-placed-cod-order)

1. Place a COD order.
2. Open Account → order details (or order list).
3. Cancel the order while it is still cancellable.
4. Attempt cancel again / after the allowed window.

Expected result:

- Cancellable `PLACED_COD` order becomes `CANCELLED`.
- Second cancel or cancel outside the policy window is rejected.
- Users cannot cancel another user's order.

## 11) Reorder from order history

Implementation track: [C8 Reorder from order history](incremental-changes.md#c8-reorder-from-order-history)

1. Open Account → past orders.
2. Choose Reorder on a previous order.
3. Review basket (some lines may be skipped if now out of stock).
4. Checkout again if desired.

Expected result:

- In-stock lines from the source order are added to the basket (merged with existing qty rules).
- Out-of-stock lines are skipped and reported; basket is not left half-applied silently.
- Empty result (all lines unavailable) leaves basket unchanged and returns a clear message.

## 12) Minimum order amount

Implementation track: [C9 Minimum order amount](incremental-changes.md#c9-minimum-order-amount)

1. Add a low-priced item so basket subtotal is below the configured minimum.
2. Attempt checkout preview / place order.
3. Add more items (or apply a promo that changes totals) and retry.

Expected result:

- Preview/place rejected when payable total is below minimum.
- Orders at or above the minimum succeed.
- Promo that drops total below minimum is rejected at place (or preview).

## 13) Change account password

Implementation track: [C10 Change account password](incremental-changes.md#c10-change-account-password), [C11 Stricter signup password/email rules](incremental-changes.md#c11-stricter-signup-passwordemail-rules)

1. Sign in.
2. Open Account → Change password.
3. Submit current password + new password (meeting policy).
4. Sign in again with the new password.

Expected result:

- Password updated; old password no longer works.
- Wrong current password is rejected.
- Weak / identical-to-current new password is rejected per policy.

## 14) Wishlist (save for later)

Implementation track: [C12 Wishlist](incremental-changes.md#c12-wishlist)

1. From Product Details (or List), add item to wishlist (auth required).
2. Open Wishlist page.
3. Move a wishlist item to basket, or remove it.
4. Attempt to wishlist an out-of-stock product.

Expected result:

- Wishlist is user-scoped; duplicates are no-ops or rejected clearly.
- Move-to-basket uses existing basket/stock rules.
- OOS handling follows explicit product rule (allow save vs reject).

## Web parity checklist

- Home: implemented.
- Product list: implemented.
- Product details: implemented.
- Basket: implemented.
- Checkout: implemented.
- Order confirmation: implemented.
- Category filter: implemented ([C1](incremental-changes.md#c1-category-filter-on-catalog)).
- Stock-aware quantity limits: implemented ([C2](incremental-changes.md#c2-stock-aware-basket-quantity-limits), [C3](incremental-changes.md#c3-stock-revalidation-on-place-order)).
- Promo codes: implemented ([C4](incremental-changes.md#c4-promo-codes-at-checkout)).
- Delivery fee by destination: implemented ([C5](incremental-changes.md#c5-delivery-fee-by-destination)).
- Saved shipping addresses: implemented ([C6](incremental-changes.md#c6-saved-shipping-addresses)).
- Order cancellation: implemented ([C7](incremental-changes.md#c7-cancel-placed-cod-order)).
- Reorder: implemented ([C8](incremental-changes.md#c8-reorder-from-order-history)).
- Minimum order amount: implemented ([C9](incremental-changes.md#c9-minimum-order-amount)).
- Change password: implemented ([C10](incremental-changes.md#c10-change-account-password)); stricter signup rules planned ([C11](incremental-changes.md#c11-stricter-signup-passwordemail-rules)).
- Wishlist: planned ([C12](incremental-changes.md#c12-wishlist)).

## Mobile parity checklist

- Home: implemented.
- Product list: implemented.
- Product details: implemented.
- Basket: implemented.
- Checkout: implemented.
- Order confirmation: implemented.
- Category filter: implemented ([C1](incremental-changes.md#c1-category-filter-on-catalog)).
- Stock-aware quantity limits: implemented ([C2](incremental-changes.md#c2-stock-aware-basket-quantity-limits), [C3](incremental-changes.md#c3-stock-revalidation-on-place-order)).
- Promo codes: implemented ([C4](incremental-changes.md#c4-promo-codes-at-checkout)).
- Delivery fee by destination: implemented ([C5](incremental-changes.md#c5-delivery-fee-by-destination)).
- Saved shipping addresses: implemented ([C6](incremental-changes.md#c6-saved-shipping-addresses)).
- Order cancellation: implemented ([C7](incremental-changes.md#c7-cancel-placed-cod-order)).
- Reorder: planned ([C8](incremental-changes.md#c8-reorder-from-order-history)).
- Minimum order amount: implemented ([C9](incremental-changes.md#c9-minimum-order-amount)).
- Change password: planned ([C10](incremental-changes.md#c10-change-account-password), [C11](incremental-changes.md#c11-stricter-signup-passwordemail-rules)).
- Wishlist: planned ([C12](incremental-changes.md#c12-wishlist)).
