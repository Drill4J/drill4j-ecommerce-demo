import { expect } from '@playwright/test';
import { test } from '#drill4j-integration';
import { createUniqueUser, signUp } from './helpers';

test.describe('Wishlist', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

  test('C12 Add to wishlist open list and remove', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);

    await page.goto('/products');
    await page.locator('article.product-card').first().getByRole('link').first().click();
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
    const productName = await page.getByRole('heading', { level: 1 }).innerText();
    await page.getByRole('button', { name: 'Add to wishlist' }).click();
    await expect(page.getByText('Added to wishlist')).toBeVisible();

    await page.getByRole('link', { name: 'Wishlist', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Wishlist' })).toBeVisible();
    await expect(page.getByRole('link', { name: productName })).toBeVisible();

    await page.getByRole('button', { name: 'Remove' }).click();
    await expect(page.getByText('Your wishlist is empty.')).toBeVisible();
  });
});
