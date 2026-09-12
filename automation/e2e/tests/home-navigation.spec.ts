import { expect } from '@playwright/test';
import { test } from '#drill4j-integration';
import { openBasket } from './helpers';

test.describe('Home & navigation', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

  test('E2E-001 Home renders, featured products, navigation, and basket redirects when signed out', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('link', { name: 'Example Store' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Shop everyday essentials' })).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Featured products' })).toBeVisible();

    const featuredCards = page.locator('article.product-card');
    await expect(featuredCards).toHaveCount(8);

    await page.getByRole('link', { name: 'Products', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();

    await page.getByRole('link', { name: 'Home' }).click();
    await expect(page.getByRole('heading', { name: 'Featured products' })).toBeVisible();

    await openBasket(page);
    await expect(page).toHaveURL(/\/signin$/);
    await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
  });

  test('C13 Home featured section loads from featured API', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByRole('heading', { name: 'Featured products' })).toBeVisible();
    const featuredCards = page.locator('article.product-card');
    await expect(featuredCards).toHaveCount(8);
    await expect(featuredCards.first()).toBeVisible();
  });
});

