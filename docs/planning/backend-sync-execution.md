# Plan — Execute the app-side backend sync (resume)

Direction: apply the queued app-side changes to match the **current** Go backend. The backend moved a lot since the
original diff ([BACKEND_CONTRACT_BREAKING_REPORT.md]), so **re-ground against current backend code, don't trust the stale
report blindly.** Client posture ([[session-b-is-client-accept-backend]]): A owns the API; we adapt. If a field can't be
consumed / data mismatches / a feature needs an API → **inform A** (don't invent backend behavior).

## Two work streams

### A. Per-domain DTO syncs (re-verify current backend → apply) — fan-out, edit-only, I build
For each of report · dashboard · trip-payment · trip · vehicle · customer · driver:
- RE-READ the current backend contract (routes/handlers/structs + `domain/*/entity.go` json tags) for the endpoints in
  scope — the report's findings are a *guide*, but confirm each still holds (A may have changed/resolved/added).
- Apply app-side: response field renames (→ correct `@SerialName`), retypes (object↔array, Int→Double), required-field
  fixes, nested restructures + mapper updates, and **A's additive fields** (`driver_cost`/`driver_costs`/
  `total_driver_costs` from P0 batch 2; selling_value canonical already equals quote — no math change yet).
- DTO discipline: every field `@SerialName` + default; JSON `coerceInputValues`/`explicitNulls=false` already set.
- Flag (don't guess): any field with no backend source, any data mismatch, anything needing a new/changed API → report
  for me to relay to A.

### B. Cross-cutting / feature items (I do carefully, not blind fan-out)
1. **`/auth/refresh` wiring** (`HttpClientProvider`): send `{refresh_token}`, persist the ROTATED token each response,
   capture refresh_token from login + OTP, use per-response `expires_in`, any refresh 401 → session-expired→re-login.
2. **Post-onboarding `/me/permissions` re-fetch**: after onboarding completes, refetch permissions so owner gating lights up.
3. **Revenue/COGS flip** (backend Phase 1+2 live): add editable "Actual Price (Revenue)" field (default=quote) on
   create+edit → `CreateTripData.sellingValue` + both update builders; add `purchase_price` to both update builders;
   drop `payment_status`/`pending_amount`(/`payment_mode`) from create. Then notify A → DONE (they cleanup request DTOs).
4. **Delete-guard 409**: friendly EN+HI message for `vehicle_has_active_trips`/`driver_has_active_trips` in `ApiErrorHandler`.
5. **Live-map WS** (now functional): Ktor WS client → `ws://<host>/ws?token=<JWT>`, parse `location_update`, drive markers
   from real vehicles. Sizable; verify on a deployment with the broker (on-device/D), ping A if no feed.

## Order (most user-visible parse-breaking first)
report → dashboard → trip-payment → trip → vehicle → customer → driver  (stream A) ; then B1–B5.

## Verify
Per-module `:screen-X:compileCommonMainKotlinMetadata` then full `:androidApp:assembleDebug`; adversarial cross-check of
the applied syncs (args/keys/behavior/no-blank); relay flagged items to A via MCP. Build green is the gate each step.

## RESULT — Stream A DONE & green (androidApp:assembleDebug)
- **Per-module sync** (8 domains) + **shared-lib sync** (ijs-network-lib dashboard models/entity/mapper/repo; ijs-core-lib
  cost DTOs; driver-status value-set; team endpoint consts; vehicle /detail flat-route) applied. report did the biggest
  rebuild (cost_type crash fix, nested consolidated/cost-type/fleet, multi-trip/cost-type decoded as objects, additive
  driver_cost). Both waves compiled green.
- **Adversarial cross-check** (8 domains) → fixed all HIGH+MEDIUM app-side: report `cost_ids`→`cost_types` (Cost Analysis
  was 400ing) + consolidated `completed_trips`; dashboard `UserInfoDto` defaults (omitempty email crash); vehicle
  `hasMore` derived from pagination + doc-alert "not uploaded" vs "Expired"; driver bulk **HTTP 206** partial-success now
  honored; customer payment-summary `netAmount`. Green.
- **Flagged to A (11 data gaps, A owns):** multi-trip customer/status, vehicle make/model, consolidated counts, fleet
  gross/margin, summary pending/cancelled, per-trip payment summary, customer driver-cost inconsistency, doc download
  mechanism, payment-DETAIL trip card, period-breakdown profit, /detail mileage. A acked + is adding/harmonizing
  (additive). The history-route "404" agent flag was a FALSE alarm (routes exist in activitylog_routes.go).

## RESULT — A's 8 additive fields WIRED & green
A resolved all flagged gaps (additive). Consumed app-side (fan-out + my fixes), build green:
- R1 customer_name + status (multi-trip badge off status: profit/loss/break_even). R2 vehicle_make/vehicle_model
  (repointed @SerialName). R3 completed_trips (already done in cross-check). R4 profit_margin + completed_trips on
  fleet_summary; **grossProfit=0 on fleet** (no fake gross — fleet is opex; consolidated is the gross source).
- P5 total_pending/total_cancelled (was already wired). P6 per-trip summary block {paid,pending,receipt_count,status}.
- C7 net_profit/total_driver_costs/net_margin → net_profit is the consistent profit. V8 download_url via new
  `ApiConfig.BASE_ORIGIN` (BASE_URL minus /api/v1) wired into doc preview/download.

## RESULT — Stream B DONE & green (androidApp:assembleDebug, APK 05:21)
- **B1 /auth/refresh** — ALREADY implemented (pre-existing). Verified: HttpClientProvider has the full silent-refresh
  plugin (POST /auth/refresh with {refresh_token}, rotated-pair persistence, per-response expires_in, older-session→
  relogin, refresh-401→session-expired); login/verifyLoginOtp/signup all `persistRefreshSession(...)`. No change needed.
- **B2 perms refetch** — `markTeamSetupCompleted()` now re-fetches /me/permissions after onboarding (both completion
  exits), and the CreateOrganization skip path awaits it before Dashboard nav (sharedUI: DefaultViewModelProvider +
  FleetNavigation). So freshly-onboarded owner gating lights up.
- **B3 revenue/COGS flip** — editable "Actual Price (Revenue)" on create + edit (defaults to quote) → selling_value;
  purchase_price sent on update; dropped payment_status/pending_amount/payment_mode from the create request. Relayed
  DONE to A (they can retire those create-request fields). FOLLOW-UP (minor): no purchase_price input on the CREATE
  screen yet (edit has it; backend defaults COGS).
- **B4 delete-guard 409** — friendly EN+HI message added to ApiErrorHandler. **Cross-check caught the agent's own flag was
  real**: vehicle delete returned a hardcoded "Failed to delete vehicle" and driver delete decoded the wrong envelope
  field (`message` vs backend `errorMessage`/`developerMessage`). FIXED both: delete paths now route the 409 body through
  `ApiErrorHandler.extractErrorMessage(status, body)` so the guard message actually surfaces.
- **B5 live-map WS** — Ktor WS client (`KtorLiveLocationSocket`: ws(s)://origin/ws?token=, frame parse, own client),
  domain interfaces (LiveLocationSocket/LocationUpdate/AuthTokenProvider/MapVehicleProvider), `MapVehicleProviderAdapter`
  (VehicleRepository→map seeds), and MapsViewModel rewrite (seed real vehicles + live updates, job in viewModelScope,
  closed in onCleared). The 2 stalled-agent gaps I finished: fixed the socket's method-ref compile bug, and wired the
  `mapsViewModel()` DI factory (adapter + socket + token lambda + logger). VERIFY on a deployment with the MQTT broker
  (on-device/D) for a real feed; ping A if no frames arrive.

Strings added (EN+HI): trip_actual_price_revenue_label/_hint, error_delete_blocked_active_trip.

## RESULT — A's Round-2 fields (9/10/11) WIRED & green (APK 06:10), each verify-checked
- #9 payment-detail trip context: DTO+mapper already carried all 8 trip_* fields (single TripPaymentDto for list+detail);
  the gap was UI — added trip state + start/delivery date-time rows to the payment-detail TripInfoCard (was blank).
  Reused existing localized labels (Status/Start Date/End Date); 3 more precise labels available if wanted (optional).
- #10 customer period_breakdown: added @SerialName costs/profit to FinancialPeriodBreakdownDto; mapper now profit =
  dto.profit (revenue−costs), no longer the `collected` stopgap. Verified vs customer_repo.go:556-563.
- #11 vehicle /detail mileage: already mapped app-side (json "mileage"); now populated by backend → shows through. No
  change. fuel_type already mapped.

NET: Stream A + A's Round-1 (8) + Round-2 (3) fields + Stream B (B1-B5) ALL green. Every flagged gap cleared.
Remaining = on-device/E2E only: live-location WS feed (B5) against a broker deployment; minor create-screen COGS input.
