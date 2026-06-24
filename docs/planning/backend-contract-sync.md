# Plan — Sync KMP apps to the current Go backend contract

The fleet backend (`IndusJSFleet_GoLang_Backend`) + IAM (`IndusJS-IAM`) had vast refactoring (git log: trip-payments
clean-architecture rewrite, customer integration, driver create/link, team→IAM, onboarding-from-IAM, route param
rename, password policy). This task reconciles the KMP apps' network layer to that current contract.

## Hard scope
- **Edit ONLY files under `IndusJSFleet_Apps/`.** The backend is READ-ONLY reference. Do not touch Go.
- Canonical source = backend **Go code** (`internal/transport/http/routes/*.go`, `handler/*.go`, request/response
  structs + `internal/domain/*/entity.go` json tags). Trust code over `API_DOCUMENTATION.md` (older).

## Method (per-domain read-only diff → fix apps)
For each domain, compare backend ↔ apps:
1. **Endpoints**: backend route method+path (`routes/*.go`) vs apps `ApiConfig.Endpoints`. Flag path/method/param drift.
2. **Request DTOs**: backend bound struct `json:"…"` (+ required/`binding`) vs apps request DTO `@SerialName` (+ type, nullability). Flag renamed / added-required / removed / retyped.
3. **Response DTOs**: backend response struct / entity `json:"…"` vs apps response DTO `@SerialName`. Flag renamed (→ silent blank), added, removed, retyped (e.g. id int vs string, epoch vs ISO).
4. **Cross-cutting**: error envelope shape, auth header/token (`tid`), pagination/list wrapper, base path `/api/v1`.

## Domain → apps module map
trip-payment→screen-trip-payment · customer→screen-customer · driver(+drivercost)→screen-driver ·
trip(+tripcost)→screen-trip · vehicle(+vehicledetail)→screen-vehicle · maintenance/finance(loan,purchase)→screen-finance ·
report→screen-report · dashboard→screen-dashboard · profile/auth/onboarding→screen-user · team→screen-team (NOTE: team
moved to IAM — verify whether fleet still exposes team routes or the app must call IAM/onboarding paths).

## Fix rules (apps side)
- DTO discipline (CLAUDE.md): every field `@SerialName` + a default; JSON uses `coerceInputValues` + `explicitNulls=false`.
- Renamed field → update `@SerialName` (keep Kotlin name or align). Retyped → change type + mapper. Added-required
  request field → add + populate. Don't blindly delete app fields the backend dropped if UI needs them — flag.
- Keep the shared error envelope handling; only adjust if the backend envelope actually changed.
- Per-module `:screen-X:compileCommonMainKotlinMetadata`, then full `:androidApp:assembleDebug`.

## Risk
- Large surface; prioritize HIGH-CHURN (trip-payment, customer, driver, trip, team, onboarding) and PARSE-BREAKING
  drift (renamed/retyped response fields blank the UI silently — highest user impact).
- Some divergences are product decisions (a dropped field the UI still shows) — flag, don't guess.
- Verify against the working E2E smoke flow (memory: e2e-api-smoke-flow) if the backend is runnable.

## Steps
1. Investigate (fan-out, read-only both repos) → per-domain divergence report + cross-cutting.
2. Synthesize → prioritized fix list (parse-breaking first).
3. Fix apps per domain; compile per module.
4. Adversarial cross-check + full build.
