# Local history replay (Drill4J demo)

Replays a git commit range on a developer machine against a **persistent** local Drill4J Admin so coverage/tracing builds accumulate as a simulated development timeline.

## Prerequisites

1. Drill4J Admin running (e.g. `docker/drill4j`) on `http://localhost:8090`
2. `docker/ecommerce/.env` with a valid `DRILL_API_KEY` (same as Playwright / Gradle)
3. Docker network `drill4j_default` (or set `REPLAY_DRILL_NETWORK`)
4. External volume `drill4j_agent-files` for app agents

## Usage

From repo root:

```bash
chmod +x scripts/replay-history.sh
./scripts/replay-history.sh <from_ref> <to_ref>
```

Examples:

```bash
# After FROM (exclusive) through TO (inclusive), oldest → newest
./scripts/replay-history.sh 06120a1 HEAD

# Gradle tests only (no compose rebuild / e2e)
./scripts/replay-history.sh v0 --unit-api-only HEAD
```

Per commit the script:

1. Checks out the commit in `.replay-worktree` (bind-mounted into long-lived containers)
2. Runs `gradle unitTest` + `apiTest` via **`docker exec`** into one reused Gradle container (warm daemon; no fresh container / `--no-daemon` / `--rerun-tasks` per commit). `.git` is mounted for `commitSha`; `DRILL_REPORT_ENABLED=true`
3. Rebuilds using **that commit’s** `docker/ecommerce/docker-compose.yml` (project name stays `ecommerce`). Secrets/ports still come from the host `docker/ecommerce/.env`. `DRILL_BUILD_VERSION_*` is taken from that commit’s `build.gradle`
4. Runs Playwright via **`docker exec`** into one reused Playwright container (`npm install` once). E2E failures are logged and the replay **continues**; the script exits non-zero at the end if any e2e run failed

## Compose build versions

`docker/ecommerce/docker-compose.yml` passes:

- `DRILL_BUILD_VERSION_AUTH` (fallback `DRILL_BUILD_VERSION`, then `0.1.0`)
- `DRILL_BUILD_VERSION_BACKEND` (same)

Keep these aligned with Gradle `version` / plugin `buildVersion` so app-agent and test-agent builds match in Drill UI.

Compose also sets `DRILL_ENV_ID` (default `local`) for the app agent environment label. The compose agent does not send VCS metadata; commit/branch come from the CI/CD test agent on clean-tree `unitTest` / `apiTest` runs.
