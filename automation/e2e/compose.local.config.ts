export const LOCAL_COMPOSE_FILE = process.env.E2E_COMPOSE_FILE ?? '../../docker/ecommerce/docker-compose.yml';
export const LOCAL_STACK_NAME = process.env.E2E_COMPOSE_PROJECT ?? 'ecommerce';
export const E2E_BASE_URL = process.env.E2E_BASE_URL ?? 'http://localhost';

