import { execSync } from 'node:child_process';
import { request } from '@playwright/test';
import { E2E_BASE_URL, LOCAL_COMPOSE_FILE, LOCAL_STACK_NAME } from './compose.local.config';

function sh(cmd: string) {
  execSync(cmd, { stdio: 'inherit' });
}

async function waitForHttp(url: string, timeoutMs: number) {
  const api = await request.newContext();
  const started = Date.now();
  // eslint-disable-next-line no-constant-condition
  while (true) {
    try {
      const res = await api.get(url, { failOnStatusCode: false });
      if (res.status() >= 200 && res.status() < 500) {
        await api.dispose();
        return;
      }
    } catch {
      // ignore until timeout
    }
    if (Date.now() - started > timeoutMs) {
      await api.dispose();
      throw new Error(`Timed out waiting for ${url}`);
    }
    await new Promise((r) => setTimeout(r, 1000));
  }
}

export default async function globalSetup() {
  // Build + start the stack (auth, backend, frontend) from local docker-compose.
  // sh(`docker compose -p "${LOCAL_STACK_NAME}" -f "${LOCAL_COMPOSE_FILE}" up -d --build`);

  // Wait for the frontend to serve something.
  await waitForHttp(E2E_BASE_URL, 180_000);
}

