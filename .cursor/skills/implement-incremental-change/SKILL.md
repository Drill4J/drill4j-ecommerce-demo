---
name: implement-incremental-change
description: >-
  Implements one drill4j-ecommerce-demo incremental change (C1–C13) from
  example_app/docs/incremental-changes.md: feature + planned unit/API tests,
  verify and commit; then planned Playwright E2E only, verify and commit.
  Use when the user asks to implement Cn, run an incremental change track,
  or continue the partial-coverage demo history.
---

# Implement incremental change (Cn)

Simulates a typical split: **developers** ship feature + unit + API tests; **automation engineers** add E2E later — **two separate commits**.

## Input

1. Require a change id: `C1` … `C13` (normalize `c1` / `C1)` → `C1`).
2. If missing or invalid, ask once; do not invent a track.

## Source of truth (always read first)

Repo root: `drill4j-ecommerce-demo`.

1. Read `example_app/docs/incremental-changes.md` — section for that `Cn` (summary row + detail table: Touches, Unit/API/E2E **Cover** vs **Gap**).
2. Follow the linked **User flow** in `example_app/docs/user-flows.md` and implement behavior that matches it.
3. Match existing code style in touched modules; do not expand scope beyond the track.

### Module map

| Area | Path |
|------|------|
| Ecommerce backend | `example_app/backend` |
| Auth | `example_app/auth` |
| Web UI | `example_app/frontend` |
| Unit tests | Same module `src/test`, `@Tag("unit")` — `gradle unitTest` |
| API tests | Same module `src/test`, `@Tag("api")` — `gradle apiTest` (**not** under `automation/`) |
| E2E tests | `automation/e2e` only |

Touch **only** components listed under **Touches** for that `Cn` (plus tests). Skip mobile unless the track explicitly requires it.

### Hard rule: intentional gaps

- Implement **only** tests listed under each layer’s **Cover**.
- Do **not** implement tests for **Gap** rows (Drill4J demo signal).
- Do not “be helpful” by adding extra negative/edge cases.

### Hard rule: version bump

For each incremental change `Cn`, bump **`version`** in every touched service `build.gradle` (`example_app/backend` and/or `example_app/auth`) by either a **minor** or **patch** increment (starting baseline `0.1.0`). Do not leave version unchanged across a track. Include the bump in the Phase A (feature + unit + API) commit.

---

## Phase A — Feature + unit + API (developer)

### A1. Implement product change

Implement the feature across all affected components from **Touches** (backend and/or auth and/or frontend web UI) so the user flow works end-to-end for the **Cover** scenarios.

Bump `version` (minor or patch) in each touched auth/backend `build.gradle`.

Update docs when the feature lands:

- In `incremental-changes.md`, set that `Cn` **Status** to reflect feature+unit+API done (e.g. `feature+unit+api done`).
- In `user-flows.md` parity checklists, mark the related planned item appropriately if it becomes implemented for web.

### A2. Implement planned unit + API tests only

| Layer | Location | What to add |
|-------|----------|-------------|
| Unit | touched module `src/test` (service/domain) | Only **Unit → Cover** |
| API | same touched module `src/test` (api/WebMvc/integration) | Only **API → Cover** |

Do **not** add E2E in this phase. Do **not** put unit/API tests under `automation/`.

### A3. First verification (must all pass before commit)

**4.1** All affected components must **build and launch** successfully:

```bash
# Backend
cd example_app/backend && gradle assemble

# Auth (if touched or needed to run the stack)
cd example_app/auth && gradle assemble

# Frontend (if touched)
cd example_app/frontend && npm install && npm run build
```

Smoke-launch as needed to confirm startup (e.g. `gradle bootRun` for auth/backend, `npm run dev` for frontend, or `docker compose` under `docker/ecommerce` if that is the project’s usual local stack). Fix failures before continuing.

**4.2** Implementation must match the track description + linked user flow (manual check against Cover scenarios).

**4.3** All **newly implemented** unit and API tests must pass:

```bash
cd example_app/backend && gradle unitTest && gradle apiTest   # if backend touched
cd example_app/auth && gradle unitTest && gradle apiTest      # if auth touched
# or: gradle test  (runs both)
```

**4.4** Only **planned** unit and API tests for this `Cn` were implemented (diff review vs Cover/Gap). Remove any accidental Gap coverage.

If any check fails: fix and re-run; **do not commit**.

### A4. Commit (feature + unit + API)

When 4.1–4.4 pass, create **one** commit (user asked for this workflow — committing is required here).

- Stage only files for this track (feature, unit, API, doc status).
- Message style (HEREDOC):

```text
feat(<scope>): <Cn> <short feature summary>

Add planned unit and API coverage; leave documented gaps for Drill4J.
```

`<scope>` examples: `catalog`, `basket`, `checkout`, `orders`, `auth`, `wishlist`.

Follow the repo git commit protocol (status/diff/log, no force, no secrets).

---

## Phase B — E2E only (automation engineer)

Start only after Phase A commit succeeded.

### B1. Implement planned E2E only

- Add/update Playwright specs under `automation/e2e` for **E2E → Cover** only.
- Reuse existing helpers/patterns in `automation/e2e/tests`.
- Do **not** cover **E2E → Gap**.
- If the track’s E2E cover is empty/`—`, skip implementing new E2E, note that in the summary, and skip B commit (or commit doc-only status if you mark E2E N/A).

### B2. Second verification

**7.1** All **newly implemented** E2E tests must pass (stack up: auth + backend + frontend reachable as existing e2e config expects):

```bash
cd automation/e2e && npm install && npm run test:local
# or project-standard Playwright command if env differs
```

**7.2** Only planned E2E tests for this `Cn` were implemented (no Gap cases).

If fail: fix and re-run; **do not commit**.

### B3. Commit (E2E)

When 7.1–7.2 pass, create a **second** commit:

```text
test(e2e): <Cn> <short E2E summary>

Add planned Playwright coverage only; keep intentional gaps.
```

Update `incremental-changes.md` **Status** to `done` (or `done (no e2e)` if skipped).

---

## Progress checklist

Copy and update while running:

```text
Cn: _
- [ ] Read incremental-changes + user flow
- [ ] Phase A: feature (touched modules only)
- [ ] Phase A: bump version (minor or patch) on touched auth/backend build.gradle
- [ ] Phase A: unit Cover only
- [ ] Phase A: API Cover only
- [ ] 4.1 build/launch
- [ ] 4.2 matches description
- [ ] 4.3 new unit + API pass
- [ ] 4.4 no Gap tests
- [ ] Commit A (feat + unit + API)
- [ ] Phase B: E2E Cover only
- [ ] 7.1 new E2E pass
- [ ] 7.2 no Gap E2E
- [ ] Commit B (e2e)
```

## Done response

Briefly report: change id, what shipped, which gaps remain, both commit hashes/subjects (or why E2E commit skipped).

## Do not

- Implement multiple `Cn` in one run unless the user lists them sequentially.
- Fill Gap tests “for completeness”.
- Mix E2E into the first commit.
- Change unrelated modules or drive-by refactors.
- Push unless the user asks.
