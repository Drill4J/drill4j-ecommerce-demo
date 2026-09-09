import { execSync } from 'node:child_process';
import { LOCAL_COMPOSE_FILE, LOCAL_STACK_NAME } from './compose.local.config';

function sh(cmd: string) {
  execSync(cmd, { stdio: 'inherit' });
}

export default async function globalTeardown() {
  // Stop and remove containers/networks/volumes for a clean slate.
  // Keep it conservative: no volumes removal by default.
  // sh(`docker compose -p "${LOCAL_STACK_NAME}" -f "${LOCAL_COMPOSE_FILE}" down --remove-orphans`);
}

