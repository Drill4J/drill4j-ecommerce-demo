# Playwright E2E (Web UI)

## Setup

From `automation/e2e`, run the init script once:

```bash
# Git Bash / macOS / Linux
./init.sh

# Windows CMD
init.cmd
```

This installs npm packages, Playwright Chromium, and creates `.env` from
`.env.example` when missing.

Options:

| Flag | Meaning |
| --- | --- |
| `--insecure` | Disable TLS cert verification (corporate proxies / MITM) |
| `--with-deps` | Also install OS deps (`playwright install --with-deps`) |

Example:

```bash
./init.sh --insecure
```

Manual equivalent:

```bash
npm install
npx playwright install chromium
```

## Run tests

Expect the ecommerce stack from `docker/ecommerce` to be running
(`FRONTEND_HOST_PORT=80` by default):

```bash
npm test
```

### Headed / UI mode

```bash
npm run test:headed
npm run test:ui
```

### Against a local Docker Compose stack

```bash
npm run test:local
```
