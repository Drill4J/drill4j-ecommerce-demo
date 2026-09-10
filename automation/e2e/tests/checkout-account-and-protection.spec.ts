import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';
import { test } from '#drill4j-integration';
import {
  addFirstProductToBasketFromList,
  createUniqueUser,
  openBasket,
  PROCESSING_ORDER_MAX_TIMEOUT,
  signOutViaHeader,
  signUp
} from './helpers';

async function placeOrderViaUi(page: Page, shipping: {
  fullName: string;
  phoneNumber: string;
  addressLine: string;
  city: string;
  postalCode: string;
}) {
  await addFirstProductToBasketFromList(page);
  await openBasket(page);
  await page.getByRole('button', { name: 'Proceed to checkout' }).click();

  await page.getByLabel('Full name').fill(shipping.fullName);
  await page.getByLabel('Phone number').fill(shipping.phoneNumber);
  await page.getByLabel('Address line').fill(shipping.addressLine);
  await page.getByLabel('City').fill(shipping.city);
  await page.getByLabel('Postal code').fill(shipping.postalCode);
  await page.getByRole('button', { name: 'Continue to payment' }).click();

  await page.getByRole('button', { name: 'Continue to review' }).click();
  const placeOrderButton = page.getByRole('button', { name: 'Place order' });
  await placeOrderButton.click();
  await expect(placeOrderButton).toBeHidden();
  await expect(page.locator('.order-loading-overlay')).toBeHidden({ timeout: PROCESSING_ORDER_MAX_TIMEOUT });
  await expect(page.getByRole('heading', { name: 'Order confirmed' })).toBeVisible();
}

test.describe('Checkout, account, and access rules', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

  test('E2E-009..013 Full checkout flow to confirmation; basket cleared', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);

    await addFirstProductToBasketFromList(page);
    await openBasket(page);
    await page.getByRole('button', { name: 'Proceed to checkout' }).click();

    await expect(page.getByRole('heading', { name: 'Checkout' })).toBeVisible();
    await expect(page.getByText('Step 1 of 3')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Shipping information' })).toBeVisible();

    // E2E-010: validation blocks progress (HTML required)
    await page.getByRole('button', { name: 'Continue to payment' }).click();
    await expect(page.getByRole('heading', { name: 'Shipping information' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Payment method' })).toHaveCount(0);

    await page.getByLabel('Full name').fill('E2E User');
    await page.getByLabel('Phone number').fill('123456789');
    await page.getByLabel('Address line').fill('1 Test Street');
    await page.getByLabel('City').fill('Testville');
    await page.getByLabel('Postal code').fill('00000');
    await page.getByRole('button', { name: 'Continue to payment' }).click();

    // E2E-011 Payment (COD only)
    await expect(page.getByRole('heading', { name: 'Payment method' })).toBeVisible();
    await expect(page.getByText('Cash on Delivery (COD)')).toBeVisible();
    await page.getByRole('button', { name: 'Continue to review' }).click();

    // E2E-012 Review
    await expect(page.getByRole('heading', { name: 'Review your order' })).toBeVisible();
    await expect(page.getByText(/Payment:/)).toBeVisible();
    await expect(page.getByText(/Total:/)).toBeVisible();

    // E2E-013 Place order + confirmation
    const placeOrderButton = page.getByRole('button', { name: 'Place order' });
    await placeOrderButton.click();
    await expect(placeOrderButton).toBeHidden();
    await expect(page.locator('.order-loading-overlay')).toBeHidden({ timeout: PROCESSING_ORDER_MAX_TIMEOUT });
    await expect(page).toHaveURL(/\/checkout\/success\/.+/);
    await expect(page.getByRole('heading', { name: 'Order confirmed' })).toBeVisible();
    await expect(page.getByText(/Order ID:/)).toBeVisible();
    await expect(page.getByText(/Status:/)).toBeVisible();
    await expect(page.getByText(/Payment method:/)).toBeVisible();

    await page.getByRole('link', { name: /Basket \(\d+\)/ }).click();
    await expect(page.getByText('Your basket is empty.')).toBeVisible();
  });

  test('E2E-014 Account shows signed-in email and orders list after placing an order', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);

    await placeOrderViaUi(page, {
      fullName: 'E2E User',
      phoneNumber: '123456789',
      addressLine: '1 Test Street',
      city: 'Testville',
      postalCode: '00000',
    });

    await page.getByRole('link', { name: new RegExp(user.email) }).click();
    await expect(page.getByRole('heading', { name: 'Account' })).toBeVisible();
    await expect(page.getByRole('main').getByText(user.email)).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Your orders' })).toBeVisible();
    await expect(page.locator('.list-row')).toHaveCount(1);
  });

  test('E2E-015 Basket and orders are user-scoped (User A vs User B)', async ({ page }, testInfo) => {
    const userA = createUniqueUser(testInfo);
    await signUp(page, userA);
    await placeOrderViaUi(page, {
      fullName: 'User A',
      phoneNumber: '111111',
      addressLine: 'A Street',
      city: 'A City',
      postalCode: '11111',
    });

    await signOutViaHeader(page);

    const userB = createUniqueUser(testInfo);
    await signUp(page, userB);

    await openBasket(page);
    await expect(page.getByText('Your basket is empty.')).toBeVisible();

    await page.getByRole('link', { name: new RegExp(userB.email) }).click();
    await expect(page.getByRole('heading', { name: 'Account' })).toBeVisible();
    await expect(page.getByText('You have no orders yet.')).toBeVisible();
  });

  test('E2E-016 Protected routes redirect to sign-in when signed out (basket + account)', async ({ page }) => {
    await page.goto('/basket');
    await expect(page).toHaveURL(/\/signin$/);
    await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();

    await page.goto('/account');
    await expect(page).toHaveURL(/\/signin$/);
    await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
  });
});

