import { expect } from '@playwright/test';
import { test } from '#drill4j-integration';
import { createUniqueUser, signIn, signOutViaHeader, signUp } from './helpers';

test.describe('Authentication', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().clearCookies();
  });

  test('E2E-002 Sign up and sign in (web)', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);
    await signOutViaHeader(page);
    await signIn(page, user);
    await expect(page.getByRole('link', { name: new RegExp(user.email) })).toBeVisible();
  });

  test('E2E-003 Duplicate sign up is rejected', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);
    await signOutViaHeader(page);

    await page.goto('/signup');
    await page.getByLabel('Email').fill(user.email);
    await page.getByLabel('Password').fill(user.password);
    await page.getByRole('button', { name: 'Sign up' }).click();

    await expect(page.locator('p.error')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Create account' })).toBeVisible();
  });
});

