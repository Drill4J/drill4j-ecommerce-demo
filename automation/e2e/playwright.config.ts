import { defineConfig, devices } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const e2eDir = path.dirname(fileURLToPath(import.meta.url));
dotenv.config({ path: path.resolve(e2eDir, '.env') });
dotenv.config({ path: path.resolve(e2eDir, '../../docker/ecommerce/.env') });

export default defineConfig({
  testDir: './tests',
  fullyParallel: true,
  retries: process.env.CI ? 2 : 0,
  workers: 2,
  reporter: [['list'], ['html', { open: 'never' }]],
  globalSetup: './playwright.global-setup',
  globalTeardown: './playwright.global-teardown',
  timeout: 60_000,
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'default',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
