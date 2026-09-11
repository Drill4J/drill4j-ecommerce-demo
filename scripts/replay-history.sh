#!/usr/bin/env bash
# Replay a git commit range locally against a persistent Drill4J Admin.
# For each commit (oldest → newest):
#   1) gradle unitTest + apiTest (auth/backend if present)
#   2) rebuild using that commit's docker/ecommerce/docker-compose.yml
#      (DRILL_BUILD_VERSION_* from that commit's build.gradle; host .env for secrets)
#   3) Playwright E2E if automation/e2e exists (failures are reported; replay continues)
#
# Reuses long-lived Gradle and Playwright containers across commits (warm daemon /
# npm install once). The host only checks out each SHA into the mounted worktree.
#
# Usage:
#   ./scripts/replay-history.sh <from_ref> <to_ref>
#   ./scripts/replay-history.sh <from_ref> <to_ref> --skip-e2e
#   ./scripts/replay-history.sh <from_ref> <to_ref> --skip-deploy
#   ./scripts/replay-history.sh <from_ref> <to_ref> --unit-api-only
#
# Requires: git, docker, docker compose, network access to Drill Admin.
# Loads DRILL_* from docker/ecommerce/.env when present.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FROM_REF="${1:-}"
TO_REF="${2:-}"
SKIP_E2E=0
SKIP_DEPLOY=0
UNIT_API_ONLY=0

shift $(( $# >= 2 ? 2 : 0 )) || true
for arg in "$@"; do
  case "$arg" in
    --skip-e2e) SKIP_E2E=1 ;;
    --skip-deploy) SKIP_DEPLOY=1 ;;
    --unit-api-only) UNIT_API_ONLY=1; SKIP_E2E=1; SKIP_DEPLOY=1 ;;
    *) echo "Unknown option: $arg" >&2; exit 2 ;;
  esac
done

if [[ -z "$FROM_REF" || -z "$TO_REF" ]]; then
  echo "Usage: $0 <from_ref> <to_ref> [--skip-e2e|--skip-deploy|--unit-api-only]" >&2
  exit 2
fi

# Load docker-compose .env without `source` (unquoted values with spaces are valid there).
load_compose_env() {
  local file="$1" line key val
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    if [[ "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"
      val="${BASH_REMATCH[2]}"
      if [[ "$val" =~ ^\"(.*)\"$ ]]; then
        val="${BASH_REMATCH[1]}"
      elif [[ "$val" =~ ^\'(.*)\'$ ]]; then
        val="${BASH_REMATCH[1]}"
      fi
      export "${key}=${val}"
    fi
  done < "$file"
}

if [[ -f "$ROOT/docker/ecommerce/.env" ]]; then
  load_compose_env "$ROOT/docker/ecommerce/.env"
fi

: "${DRILL_API_URL:=http://localhost:8090/api}"
: "${DRILL_API_KEY:?Set DRILL_API_KEY (e.g. in docker/ecommerce/.env)}"
: "${DRILL_GROUP_ID:=ecommerce}"

WORKTREE="$ROOT/.replay-worktree"
HOST_COMPOSE_ENV="$ROOT/docker/ecommerce/.env"
COMPOSE_PROJECT="${REPLAY_COMPOSE_PROJECT:-ecommerce}"
GRADLE_IMAGE="${REPLAY_GRADLE_IMAGE:-gradle:8.5-jdk17}"
# Keep in sync with automation/e2e package.json @playwright/test version.
PLAYWRIGHT_IMAGE="${REPLAY_PLAYWRIGHT_IMAGE:-mcr.microsoft.com/playwright:v1.54.0-jammy}"
DRILL_NETWORK="${REPLAY_DRILL_NETWORK:-drill4j_default}"
GRADLE_CTR="${REPLAY_GRADLE_CTR:-replay-gradle}"
PLAYWRIGHT_CTR="${REPLAY_PLAYWRIGHT_CTR:-replay-playwright}"

PLAYWRIGHT_READY=0

to_docker_path() {
  local p="$1"
  if command -v cygpath >/dev/null 2>&1; then
    cygpath -m "$p"
  else
    echo "$p"
  fi
}

extract_version() {
  local build_gradle="$1"
  if [[ ! -f "$build_gradle" ]]; then
    echo ""
    return
  fi
  sed -nE "s/^version[[:space:]]*=[[:space:]]*['\"]([^'\"]+)['\"].*/\1/p" "$build_gradle" | head -1
}

resolve_git_mounts() {
  GIT_COMMON="$(git -C "$ROOT" rev-parse --path-format=absolute --git-common-dir)"
  GIT_DIR_HOST="$(git -C "$WORKTREE" rev-parse --path-format=absolute --git-dir)"
  GIT_DIR_REL="${GIT_DIR_HOST#"${GIT_COMMON}/"}"
  if [[ -z "$GIT_DIR_REL" || "$GIT_DIR_REL" == "$GIT_DIR_HOST" ]]; then
    echo "[replay] ERROR: cannot map worktree git-dir into container ($GIT_DIR_HOST)" >&2
    return 1
  fi
  DRILL_API_URL_DOCKER="${DRILL_API_URL/localhost/drill-admin}"
  DRILL_API_URL_DOCKER="${DRILL_API_URL_DOCKER/127.0.0.1/drill-admin}"
  DRILL_API_URL_DOCKER="${DRILL_API_URL_DOCKER/host.docker.internal/drill-admin}"
}

start_gradle_container() {
  resolve_git_mounts
  echo "[replay] starting long-lived Gradle container ($GRADLE_CTR)"
  docker rm -f "$GRADLE_CTR" >/dev/null 2>&1 || true
  MSYS_NO_PATHCONV=1 docker run -d --name "$GRADLE_CTR" \
    --network "$DRILL_NETWORK" \
    -e DRILL_API_URL="$DRILL_API_URL_DOCKER" \
    -e DRILL_API_KEY \
    -e DRILL_GROUP_ID \
    -e DRILL_REPORT_ENABLED=true \
    -e GIT_DIR="/git/${GIT_DIR_REL}" \
    -e GIT_WORK_TREE=/workspace \
    -v "$(to_docker_path "$GIT_COMMON"):/git" \
    -v "$(to_docker_path "$WORKTREE"):/workspace" \
    -w /workspace \
    "$GRADLE_IMAGE" \
    sleep infinity >/dev/null
}

run_module_tests() {
  local module_dir="$1"
  local label="$2"
  local build_gradle="$WORKTREE/$module_dir/build.gradle"
  if [[ ! -f "$build_gradle" ]]; then
    echo "[replay] skip $label tests (no build.gradle)"
    return 0
  fi
  if ! grep -q "unitTest" "$build_gradle" 2>/dev/null; then
    echo "[replay] skip $label unitTest/apiTest (tasks not defined yet)"
    return 0
  fi

  echo "[replay] $label unitTest + apiTest (container $GRADLE_CTR)"
  # Warm daemon inside the long-lived container; bind-mounted worktree already
  # reflects the checked-out commit. Do not pass bare `-e VAR` (would clear
  # container env when the host var is unset).
  MSYS_NO_PATHCONV=1 docker exec \
    -w "/workspace/$module_dir" \
    -e DRILL_REPORT_ENABLED=true \
    "$GRADLE_CTR" \
    gradle unitTest apiTest
}

start_playwright_container() {
  if [[ "$PLAYWRIGHT_READY" -eq 1 ]] && docker inspect -f '{{.State.Running}}' "$PLAYWRIGHT_CTR" 2>/dev/null | grep -q true; then
    return 0
  fi
  if [[ ! -d "$WORKTREE/automation/e2e" ]]; then
    return 0
  fi

  echo "[replay] starting long-lived Playwright container ($PLAYWRIGHT_CTR)"
  docker rm -f "$PLAYWRIGHT_CTR" >/dev/null 2>&1 || true
  MSYS_NO_PATHCONV=1 docker run -d --name "$PLAYWRIGHT_CTR" \
    --network "$DRILL_NETWORK" \
    --add-host=host.docker.internal:host-gateway \
    -e DRILL_API_KEY \
    -e DRILL_GROUP_ID \
    -v "$(to_docker_path "$WORKTREE"):/workspace" \
    -v "$(to_docker_path "$ROOT/docker/ecommerce/.env"):/workspace/docker/ecommerce/.env:ro" \
    -w /workspace/automation/e2e \
    "$PLAYWRIGHT_IMAGE" \
    sleep infinity >/dev/null

  echo "[replay] npm install once in Playwright container"
  MSYS_NO_PATHCONV=1 docker exec -w /workspace/automation/e2e "$PLAYWRIGHT_CTR" \
    bash -lc 'npm install'
  PLAYWRIGHT_READY=1
}

deploy_stack() {
  local auth_ver="$1"
  local backend_ver="$2"
  local backend_url frontend_url compose_file compose_dir
  backend_url="http://127.0.0.1:${BACKEND_HOST_PORT:-8080}/api/v1/products?page=0&size=5"
  # Prefer 127.0.0.1 over localhost — on Windows, localhost can resolve to ::1 while
  # Docker publishes IPv4 only, which makes Playwright's wait hang until timeout.
  frontend_url="http://127.0.0.1:${FRONTEND_HOST_PORT:-80}/"
  compose_file="$WORKTREE/docker/ecommerce/docker-compose.yml"
  compose_dir="$WORKTREE/docker/ecommerce"

  if [[ ! -f "$compose_file" ]]; then
    echo "[replay] skip deploy (no docker/ecommerce/docker-compose.yml in this commit)"
    return 0
  fi

  echo "[replay] compose up --build (auth=$auth_ver backend=$backend_ver) from $compose_file"
  local compose_args=(
    -f "$compose_file"
    --project-name "$COMPOSE_PROJECT"
    --project-directory "$compose_dir"
  )
  if [[ -f "$HOST_COMPOSE_ENV" ]]; then
    compose_args+=(--env-file "$HOST_COMPOSE_ENV")
  fi
  (
    export DRILL_BUILD_VERSION_AUTH="$auth_ver"
    export DRILL_BUILD_VERSION_BACKEND="$backend_ver"
    export DRILL_BUILD_VERSION="$backend_ver"
    docker compose "${compose_args[@]}" up -d --build --force-recreate
  )

  echo "[replay] waiting for backend ($backend_url)..."
  local backend_ok=0
  local i
  for i in $(seq 1 60); do
    if curl -sf "$backend_url" >/dev/null 2>&1; then
      echo "[replay] backend healthy"
      backend_ok=1
      break
    fi
    sleep 2
  done
  if [[ "$backend_ok" -ne 1 ]]; then
    echo "[replay] ERROR: backend health check timed out" >&2
    return 1
  fi

  echo "[replay] waiting for frontend ($frontend_url)..."
  for i in $(seq 1 60); do
    if curl -sf "$frontend_url" >/dev/null 2>&1; then
      echo "[replay] frontend healthy"
      return 0
    fi
    sleep 2
  done
  echo "[replay] ERROR: frontend health check timed out ($frontend_url)" >&2
  return 1
}

run_e2e() {
  if [[ ! -d "$WORKTREE/automation/e2e" ]]; then
    echo "[replay] skip e2e (no automation/e2e)"
    return 0
  fi

  start_playwright_container

  local fe_port="${FRONTEND_HOST_PORT:-80}"
  local base_url="http://host.docker.internal:${fe_port}"
  local drill_url="http://drill-admin:8090/api"

  echo "[replay] Playwright e2e (container $PLAYWRIGHT_CTR → $base_url)"
  set +e
  MSYS_NO_PATHCONV=1 docker exec \
    -e E2E_BASE_URL="$base_url" \
    -e E2E_COMPOSE_FILE=../../docker/ecommerce/docker-compose.yml \
    -e E2E_COMPOSE_PROJECT=ecommerce \
    -e DRILL_API_URL="$drill_url" \
    -e DRILL_API_KEY \
    -e DRILL_GROUP_ID \
    -e DRILL_DASHBOARD_BASE_URL="${DRILL_DASHBOARD_BASE_URL:-http://host.docker.internal:8091}" \
    -w /workspace/automation/e2e \
    "$PLAYWRIGHT_CTR" \
    npx playwright test --project=default
  local rc=$?
  set -e
  if [[ "$rc" -ne 0 ]]; then
    echo "[replay] WARNING: Playwright e2e failed (exit $rc) — recording and continuing" >&2
    E2E_FAILURES=$((E2E_FAILURES + 1))
    E2E_FAILED_SHAS="${E2E_FAILED_SHAS}${E2E_FAILED_SHAS:+ }$sha"
  else
    echo "[replay] Playwright e2e passed"
  fi
  return 0
}

cleanup() {
  rm -f "$ROOT/.replay-compose.override.yml"
  docker rm -f "$GRADLE_CTR" "$PLAYWRIGHT_CTR" >/dev/null 2>&1 || true
  if [[ -d "$WORKTREE" ]]; then
    git -C "$ROOT" worktree remove --force "$WORKTREE" 2>/dev/null || rm -rf "$WORKTREE"
  fi
}
trap cleanup EXIT

cleanup
git -C "$ROOT" worktree add --detach "$WORKTREE" "$TO_REF" >/dev/null

COMMITS=$(git -C "$ROOT" rev-list --reverse "${FROM_REF}..${TO_REF}")
if [[ -z "$COMMITS" ]]; then
  # include TO if FROM..TO empty but FROM^..TO needed — also try inclusive
  COMMITS=$(git -C "$ROOT" rev-list --reverse "${FROM_REF}^..${TO_REF}" 2>/dev/null || true)
fi
if [[ -z "$COMMITS" ]]; then
  echo "[replay] no commits in range ${FROM_REF}..${TO_REF}" >&2
  exit 1
fi

start_gradle_container

echo "[replay] range ${FROM_REF}..${TO_REF}"
E2E_FAILURES=0
E2E_FAILED_SHAS=""
# Avoid `echo | while` so counters/exit status are not lost in a subshell.
while read -r sha; do
  [[ -z "$sha" ]] && continue
  echo "========== replay $sha =========="
  git -C "$WORKTREE" checkout --detach "$sha" >/dev/null

  run_module_tests "example_app/auth" "auth"
  run_module_tests "example_app/backend" "backend"

  AUTH_VER=$(extract_version "$WORKTREE/example_app/auth/build.gradle")
  BACKEND_VER=$(extract_version "$WORKTREE/example_app/backend/build.gradle")
  AUTH_VER="${AUTH_VER:-0.1.0}"
  BACKEND_VER="${BACKEND_VER:-0.1.0}"

  if [[ "$SKIP_DEPLOY" -eq 0 ]]; then
    deploy_stack "$AUTH_VER" "$BACKEND_VER"
  fi
  if [[ "$SKIP_E2E" -eq 0 ]]; then
    run_e2e
  fi
done <<< "$COMMITS"

if [[ "$E2E_FAILURES" -gt 0 ]]; then
  echo "[replay] done with $E2E_FAILURES e2e failure(s) at: $E2E_FAILED_SHAS" >&2
  exit 1
fi
echo "[replay] done"
