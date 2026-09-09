#!/usr/bin/env bash
# Initialize Playwright E2E dependencies for the Drill4J ecommerce demo.
# Usage:
#   ./init.sh              # npm install + Chromium browsers
#   ./init.sh --insecure   # same, but skip TLS cert verification (corporate proxies)
#   ./init.sh --with-deps  # also install OS dependencies (Linux CI / with-deps)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

INSECURE=0
WITH_DEPS=0
for arg in "$@"; do
  case "$arg" in
    --insecure) INSECURE=1 ;;
    --with-deps) WITH_DEPS=1 ;;
    -h|--help)
      sed -n '2,7p' "$0"
      exit 0
      ;;
    *)
      echo "Unknown option: $arg" >&2
      exit 1
      ;;
  esac
done

if [[ "$INSECURE" -eq 1 ]]; then
  export NODE_TLS_REJECT_UNAUTHORIZED=0
  export npm_config_strict_ssl=false
  echo "Warning: TLS certificate verification is disabled for this run."
fi

need_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

need_cmd node
need_cmd npm

NODE_MAJOR="$(node -p "process.versions.node.split('.')[0]")"
if [[ "$NODE_MAJOR" -lt 18 ]]; then
  echo "Node.js 18+ is required (found $(node -v))." >&2
  exit 1
fi

echo "==> Node $(node -v), npm $(npm -v)"
echo "==> Installing npm packages"
npm install

if [[ ! -f .env && -f .env.example ]]; then
  cp .env.example .env
  echo "==> Created .env from .env.example"
fi

echo "==> Installing Playwright Chromium"
if [[ "$WITH_DEPS" -eq 1 ]]; then
  npx playwright install --with-deps chromium
else
  npx playwright install chromium
fi

echo
echo "Done. With docker/ecommerce running, execute:"
echo "  npm test"
