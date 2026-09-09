import { test, expect } from '@playwright/test';
import {
  addFirstProductToBasketFromList,
  createUniqueUser,
  openBasket,
  openProducts,
  signUp,
} from './helpers';

test.describe('Products & basket', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

  test('E2E-004 Product list shows cards and opens details', async ({ page }) => {
    await page.goto('/products');
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();

    const firstCard = page.locator('article.product-card').first();
    await expect(firstCard.locator('img')).toBeVisible();
    await expect(firstCard.getByRole('heading')).toBeVisible();
    await expect(firstCard.locator('.price')).toBeVisible();

    await firstCard.getByRole('link', { name: 'Details' }).click();
    await expect(page).toHaveURL(/\/products\/.+/);
    await expect(page.getByRole('heading').first()).toBeVisible();
    await expect(page.getByText(/In stock|Out of stock/)).toBeVisible();
  });

  test('E2E-005 Add to basket from list requires auth (redirect to sign in)', async ({ page }) => {
    await page.goto('/products');
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
    const firstCard = page.locator('article.product-card').first();
    await firstCard.getByRole('button', { name: 'Add to basket' }).click();
    await expect(page).toHaveURL(/\/signin$/);
  });

  test('E2E-006 Add to basket from list as signed-in user; basket shows item and totals', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);

    await addFirstProductToBasketFromList(page);
    await openBasket(page);

    await expect(page.getByRole('heading', { name: 'Basket' })).toBeVisible();
    await expect(page.locator('.list-row')).toHaveCount(1);
    await expect(page.getByText('Items:')).toBeVisible();
    await expect(page.getByText('Subtotal:')).toBeVisible();
  });

  test('E2E-007 Product details: set quantity and add to basket', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);

    await openProducts(page);
    const firstCard = page.locator('article.product-card').first();
    await firstCard.getByRole('link', { name: 'Details' }).click();

    await expect(page.getByText(/In stock|Out of stock/)).toBeVisible();
    await page.getByLabel('Quantity').fill('2');
    await page.getByRole('button', { name: 'Add to basket' }).click();

    await openBasket(page);
    await expect(page.locator('.list-row')).toHaveCount(1);
    await expect(page.locator('.list-row input[type="number"]')).toHaveValue('2');
  });

  test('E2E-008 Basket update/remove updates UI', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);

    await addFirstProductToBasketFromList(page);
    await openBasket(page);

    const qtyInput = page.locator('.list-row input[type="number"]').first();
    await expect(qtyInput).toHaveValue('1');
    await qtyInput.fill('2');
    await expect(qtyInput).toHaveValue('2');

    await qtyInput.fill('1');
    await expect(qtyInput).toHaveValue('1');

    await page.getByRole('button', { name: 'Remove' }).click();
    await expect(page.getByText('Your basket is empty.')).toBeVisible();
  });
});
