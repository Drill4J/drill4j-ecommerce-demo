import { expect, type Locator, type Page, type TestInfo } from '@playwright/test';

export type TestUser = { email: string; password: string };

export const PROCESSING_ORDER_MAX_TIMEOUT = 15000;

export function createUniqueUser(testInfo: TestInfo): TestUser {
  const runId = process.env.CI
    ? process.env.CI_PIPELINE_ID ?? process.env.CI_JOB_ID ?? 'ci'
    : 'local';
  const stamp = Date.now();
  const email = `e2e_${runId}_${stamp}_${testInfo.workerIndex}@example.com`;
  return { email, password: 'P@ssw0rd_123!' };
}

export async function signUp(page: Page, user: TestUser) {
  await page.goto('/signup');
  await expect(page.getByRole('heading', { name: 'Create account' })).toBeVisible();
  await page.getByLabel('Email').fill(user.email);
  await page.getByLabel('Password').fill(user.password);
  await page.getByRole('button', { name: 'Sign up' }).click();
  await expect(page).toHaveURL(/\/products$/);
}

export async function signIn(page: Page, user: TestUser) {
  await page.goto('/signin');
  await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
  await page.getByLabel('Email').fill(user.email);
  await page.getByLabel('Password').fill(user.password);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/\/products$/);
}

export async function signOutViaHeader(page: Page) {
  const signOutButton = page.getByRole('button', { name: 'Sign out' });
  if (await signOutButton.isVisible()) {
    await signOutButton.click();
  }
  await expect(page.getByRole('link', { name: 'Sign in' })).toBeVisible();
}

export async function openProducts(page: Page) {
  await page.getByRole('link', { name: 'Products', exact: true }).click();
  await expect(page.getByRole('heading', { name: 'Products' })).toBeVisible();
}

export async function openBasket(page: Page) {
  await page.getByRole('link', { name: /Basket \(\d+\)/ }).click();
}

export async function firstProductCard(page: Page): Promise<Locator> {
  const card = page.locator('article.product-card').first();
  await expect(card).toBeVisible();
  return card;
}

export async function addFirstProductToBasketFromList(page: Page) {
  await openProducts(page);
  const card = await firstProductCard(page);
  const name = await card.getByRole('heading').first().innerText();
  await card.getByRole('button', { name: 'Add to basket' }).click();
  return { name };
}

