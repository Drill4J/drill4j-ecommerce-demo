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

  test('C10 Change password sign out and sign in with new password', async ({ page }, testInfo) => {
    const user = createUniqueUser(testInfo);
    await signUp(page, user);
    const newPassword = 'N3w_P@ssw0rd_456!';

    await page.getByRole('link', { name: new RegExp(user.email) }).click();
    await expect(page.getByRole('heading', { name: 'Change password' })).toBeVisible();
    await page.getByLabel('Current password').fill(user.password);
    await page.getByLabel('New password').fill(newPassword);
    await page.getByRole('button', { name: 'Change password' }).click();

    await expect(page).toHaveURL(/\/signin$/);
    await page.getByLabel('Email').fill(user.email);
    await page.getByLabel('Password', { exact: true }).fill(newPassword);
    await page.getByRole('button', { name: 'Sign in' }).click();
    await expect(page.getByRole('link', { name: new RegExp(user.email) })).toBeVisible();
  });
});

