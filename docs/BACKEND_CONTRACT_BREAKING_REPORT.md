# Backend Contract — Breaking Data & Features Report (KMP Apps)

**Status:** Catalog only — **no app code was changed.** Per direction, backend-sync edits are stopped; this is the
single document of what data and features are broken by the current Go backend contract.
**Scope:** `IndusJSFleet_Apps` (KMP) vs `IndusJSFleet_GoLang_Backend` (+ IAM), compared read-only.
**Method:** per-domain diff of backend Go (routes `internal/transport/http/routes/*.go`, handlers, request/response
structs + `domain/*/entity.go` `json:` tags) against the apps' endpoints (`ApiConfig.kt`) and `@Serializable` DTOs.
Backend is canonical (trust code over `API_DOCUMENTATION.md`). JSON config uses `ignoreUnknownKeys` +
`coerceInputValues` + `explicitNulls=false`, so most drift degrades to blank/0 rather than crashing — **except**
required non-null fields with no default and array-vs-object retypes, which throw and empty the whole response.

## TL;DR — why things broke
The backend was heavily refactored (git log): **trip-payments clean-architecture rewrite**, **customer integration**,
**driver create/link**, **team management moved to IAM**, **onboarding APIs from IAM**, a **route param rename**, and a
**password policy**. Field names, shapes, required-ness, and a few endpoints drifted from what the apps still send/expect.

## Severity legend
- 🔴 **HIGH** — parse-breaking response (UI blank or whole-screen empty / crash) **or** backend rejects the request
  (400 → the action simply fails).
- 🟠 **MEDIUM** — wrong/missing data (a value silently shows blank/0), wrong optionality, or data-loss risk.
- 🟢 **LOW** — additive backend field the app safely ignores, dead-but-unused code, or cosmetic.

## Counts
**112 findings — 🔴 46 HIGH · 🟠 30 MEDIUM · 🟢 36 LOW** across 11 areas.

| Area | 🔴 | 🟠 | 🟢 | Headline breakage |
|------|----|----|----|-------------------|
| report | 10 | 2 | 2 | Any report with a cost breakdown throws `MissingFieldException` (`cost_type` required, never sent) → report blank; consolidated/fleet P&L flat-vs-nested → all zeros |
| finance | 13 | 0 | 0 | **Entire feature has NO backend** — all 13 purchase/loan/EMI endpoints 404 (see Whole-Feature Breakage) |
| trip | 7 | 4 | 2 | Create-trip 400s (`customer_id`/dates/coords now required); trip-cost summaries all zero |
| trip-payment | 5 | 4 | 2 | Summary totals always 0 (`total_amount`→`total_received` etc.); breakdown `key` crash; TDS array-vs-object crash; edit PUT 400 (`payment_mode` required) |
| vehicle | 4 | 10 | 5 | Route tab fully blank (flat vs nested); doc download broken (`file_url`→`file_path`); `capacity` Int-vs-float |
| dashboard | 4 | 2 | 3 | Core landing money/count fields show 0 (`total_count`→`count`, team manager/supervisor counts) |
| driver | 2 | 0 | 7 | 2 dead endpoints (404); rest cosmetic |
| customer | 0 | 3 | 1 | `on_route`→`in_progress` — state filter matches zero rows; trip-state counts wrong |
| user (auth/profile) | 1 | 4 | 2 | Token-refresh path dead (see Whole-Feature Breakage); OTP/login token field split |
| team | 0 | 0 | 7 | Team moved to IAM — PUT null-wipe data-loss risk; stale unused endpoints |
| cross-cutting | 0 | 1 | 5 | Error envelope parsing leaks raw/developer text; no shared pagination wrapper |

## Your directives applied to this report
- **Finance:** *Skip for now* — documented as a whole-feature breakage; no app change. (No backend target exists.)
- **Token refresh:** *Needs backend* — documented under Whole-Feature Breakage; no app change.
- **Dropped UI fields:** documented under Product Decisions; no app change.

## Coordination status (with A Backend, via MCP) — resolved after this report
A verified all 112 findings against the live backend. Outcomes:
- **~95 findings = app-side syncs** (backend is canonical). **QUEUED** as one coordinated pass; not yet applied
  (app edits paused by direction). App will land them + reply DONE per module.
- **Vehicle Finance (13 "endpoints 404"):** ✅ **SHIPPED by A & VERIFIED — contract match, ZERO app change.** A built
  all 13 endpoints (`/vehicles/:id/purchase` GET/POST/PUT, `/vehicles/:id/loan-summary`, `/vehicles/:id/loan-payments`,
  `/vehicle-loan-payments` CRUD + `/:id/pay` + `/upcoming` + `/overdue`) to match the app's `VehicleFinanceDto.kt` +
  `ApiConfig` exactly. Verified field-by-field: endpoints, list wrapper, the combined `EmiAlertsData {upcoming, overdue}`
  shape, all DTO fields, and request required-fields all align. Whole-feature breakage **CLOSED** (static contract;
  runtime light-up to confirm on-device/E2E). DB migration 000008 (`ijsfm_vehicle_purchases` + `ijsfm_vehicle_loan_payments`).
- **`/auth/refresh`:** ✅ **SHIPPED by A & verified (additive).** `POST /api/v1/auth/refresh`, body
  `{"refresh_token":"<opaque>"}` (required) → `200 {success, data:{access_token, refresh_token (NEW rotated),
  token_type:"Bearer", expires_in}}`; `401` = invalid/expired/**reused** → re-login. **App action items (NEW, queued):**
  (1) send `refresh_token`, not the old access token; (2) **persist the rotated `refresh_token` from every response,
  discard the old** — replaying an old one 401s AND revokes all the user's refresh tokens (IAM reuse detection);
  (3) any refresh 401 → session-expired → re-login; (4) capture `refresh_token` from **login** (now additively returned
  alongside `token`) + OTP login; (5) use per-response `expires_in` (86400 dev), never a hardcoded TTL.
- **Owner permissions after onboarding (C/IAM fix, relayed by A):** ✅ fixed server-side (stale 5-min authz cache
  invalidated on onboarding commit; owner now gets the full 61-perm set). **App action item (NEW, queued):** re-fetch
  `GET /me/permissions` AFTER onboarding completes (pre-onboarding minimal `["users:read"]` is correct by design) so
  permission gating lights up. Related: [[onboarding-blocked-by-me-permissions-401]].
- **History / state-history (`/…/:id/history` + `/state-history`):** ✅ **SHIPPED by A & VERIFIED — contract match,
  ZERO app change.** `GET /vehicles|drivers|trips/:id/state-history` → `StateHistoryResponseDto {history[], page,
  per_page, total, total_pages}` + `StateHistoryItemDto`; `GET /vehicles|drivers/:id/history` →
  `{history[], page, per_page, total_pages, total_count}` + `HistoryItemDto{…, performed_by{id,first_name,last_name,
  email,role}, performed_by_id}`. App only calls trip state-history (no trip /history) — confirmed sufficient. PATCH
  state/status now persist `reason`/`notes` (app already sends them → resolves the "silently dropped" LOW findings).
  Caveats (handled): data is forward-only → empty list (200) not 404 (DTOs default to empty); `per_page` passed
  explicitly (backend default-10 moot); `performed_by_name`/`fullName` email-fallback already matches. Runtime
  light-up to confirm on-device/E2E.
- **Revenue/COGS unification — v2 model ACKed (SOFT/backward-compatible, BACKEND-FIRST):** Product-owner decision:
  **`selling_value` = the ACTUAL price (canonical REVENUE + payment anchor); `expected_trip_price` = the QUOTE
  (reference only); `purchase_price` = COGS.** Quote and actual MAY differ (supersedes v1's keep-equal). To be **unified
  across ALL API versions** (legacy/v1 + v2 paths, reports, dashboard, trip-payment derivation — one anchor, with
  `expected_trip_price` fallback only when `selling_value` absent/0). **Naming = A's call, LOCKED:** keep wire names
  (`expected_trip_price`/`selling_value`/`purchase_price`); app carries clarity in UI labels ("Quoted Price" / "Actual
  Price (Revenue)" / "Purchase / Cost Price"). B is the client — A owns the contract; we adapt. **Cross-version unification
  CONFIRMED:** single canonical backend path (no /api/v2 data routes), one expression everywhere
  `COALESCE(NULLIF(selling_value,0), expected_trip_price)` → unifies every client.
  **Phase 1 + Phase 2 are DEPLOYED & VERIFIED on :8081** (backward-compatible). Phase 2: `selling_value` is now canonical
  (actual revenue + payment anchor) on every path; 15 report/dashboard revenue sites migrated to the canonical expression;
  E2E proved revenue/pending follow the actual price (6000) not the quote (5000). Numbers stay unchanged until the app
  ships the editable field (selling_value still == quote via current mappers). **Only the app-side flip (3 items below)
  remains — queued.** When B replies DONE, A does the request-DTO cleanup (remove the 3 accepted-ignored create fields).
  **Queued app work** (apply only AFTER A deploys/pings; respects pause):
  1. **NEW UI:** editable "Actual Price (Revenue)" field on create + edit, defaulting to the quote, user-overridable →
     wire into `CreateTripData.sellingValue` + both update builders (today it just mirrors `tripPrice`, no field).
  2. Add `purchase_price` to **both** update builders — `TripRepositoryImpl.updateTrip` (~149) + `TripDetailActionHandler.saveChanges` (~74-75).
  3. Drop `payment_status` + `pending_amount` (+ optional `payment_mode`) from `CreateTripRequest` build at `TripRepositoryImpl.createTripWithData:112-113` + struct fields.
  Response shape unchanged (payment trio server-derived from `ijsfm_payments` — safe; app reads payment state from the
  trip-payment endpoints). Flip order: **A deploys** (anchor=selling_value, reports unify, create-seed
  pending=selling_value, hardening, accept-and-ignore old create fields) → **we flip** → A removes acceptance later.
  No 400s in the interim. ✅ Canonical-revenue-field decision RESOLVED: `selling_value`.
- **Delete guards — new 409 (A P0-9, shipped):** `DELETE /vehicles/:id` + `DELETE /drivers/:id` now return 409 CONFLICT
  with envelope key `vehicle_has_active_trips` / `driver_has_active_trips` when the entity is on a planned/in-progress
  trip (succeeds once the trip is completed/cancelled). No shape change. **App action (NEW, queued):** specialize this
  409 to a friendly localized message ("Can't delete — finish or cancel the active trip first", EN+HI) in
  `ApiErrorHandler` — today a generic 409 shows "This record already exists." (`ApiErrorHandler.kt:179/235`), which is
  misleading for this case. (Double-booking on create/update already enforced — no change.)
- **Multi-tenant uniqueness scoping (A P0-7, shipped):** ✅ no app change — duplicate 409s now per-tenant; app's generic
  duplicate-key handling is unaffected (only benefits: no more cross-tenant false 409s).
- **Live-location WebSocket now FUNCTIONAL (A fix, shipped):** the WS was broken (auth never set owner_id; role cast panic;
  MQTT ingest wrong table) — which is why the live map showed nothing. Now fixed; contract unchanged. **App action (NEW,
  queued — sizable feature):** `screen-map/MapsViewModel.loadMapData` currently renders an EMPTY none-live state
  (TODO at MapsViewModel.kt:50-55). Wire it: (1) drive markers from the tenant's real vehicles via a cross-feature
  adapter; (2) implement a Ktor WebSocket client → `ws://<host>/ws?token=<JWT>` (or `Authorization: Bearer`), parse
  `{type:"location_update", registration_number, vehicle_id, trip_id?, driver_id?, lat, lng, speed, heading?, ts(epoch ms)}`,
  update markers live (tenant-scoped). **Caveat:** A verified WS auth/handshake + ingest paths but NOT a live GPS
  round-trip (no MQTT broker in their local env) — test against a deployment with the broker up (on-device / D's E2E);
  ping A if the feed shows nothing there. Related: [[android-e2e-flows-4-11-status]].
- **Protocol:** one CONTRACT_CHANGE per landed fix; response renames/additions → app syncs after; request-shape /
  new-required / removed-field → A coordinates **before** landing.

---

# Per-domain breaking data & features
_(Ordered as analyzed. Within each area: HIGH → MEDIUM → LOW.)_

### trip-payment  
_5 high · 4 medium · 2 low_

- **🔴 HIGH · response_field_renamed** — GET /trip-payments/summary → PaymentSummaryReportDto
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: SummaryReportResponse emits json:"total_received" (float64) for the headline received total; there is NO json:"total_amount" field (entity.go:149-157, usecase.go:474-482)
  - App now: PaymentSummaryReportDto.totalAmount @SerialName("total_amount") (Double=0.0). App reads response.data.totalAmount into TripPaymentSummary.totalReceived (TripPaymentRepositoryImpl.kt:328)
  - App location: `TripPaymentRequest.kt:127`
  - To sync: In TripPaymentRequest.kt:127 rename @SerialName("total_amount") to @SerialName("total_received") (keep type Double=0.0). Backend never sends total_amount, so the summary total currently always shows 0.0.
- **🔴 HIGH · response_field_renamed** — GET /trip-payments/summary → PaymentSummaryReportDto
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: SummaryReportResponse emits json:"net_payments" (float64); NO json:"total_net_amount" (entity.go:154, usecase.go:479)
  - App now: PaymentSummaryReportDto.totalNetAmount @SerialName("total_net_amount") (Double=0.0); read into summary.totalNetAmount (TripPaymentRepositoryImpl.kt:329)
  - App location: `TripPaymentRequest.kt:129`
  - To sync: TripPaymentRequest.kt:129 rename @SerialName("total_net_amount") to @SerialName("net_payments"). Net amount in summary currently always 0.0.
- **🔴 HIGH · response_field_renamed** — GET /trip-payments/summary → PaymentBreakdownDto (by_mode/by_type items)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: ModeBreakdown emits json:"payment_mode"+"amount"+"count"; TypeBreakdown emits json:"payment_type"+"amount"+"count" (entity.go:160-171). NO json:"key", no "label", no "percentage".
  - App now: PaymentBreakdownDto requires @SerialName("key") val key: String (non-null, no default) plus optional label/percentage. by_mode/by_type items deserialize key from a tag the backend never sends.
  - App location: `TripPaymentRequest.kt:150-156`
  - To sync: TripPaymentRequest.kt:150-156: backend reuses two structs (ModeBreakdown/TypeBreakdown). Split the app DTO into ModeBreakdownDto(@SerialName("payment_mode") key, amount, count) and TypeBreakdownDto(@SerialName("payment_type") key, amount, count), and reference each from by_mode/by_type respectively.
- **🔴 HIGH · request_field_added_required** — PUT /trip-payments/:id → UpdateTripPaymentRequest
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: UpdatePaymentRequest.payment_mode has binding:"required" (usecase_port.go:57); all other update fields optional. Backend ignores amount/payment_type/payment_date/customer_*/trip_id on update (usecase.go:324-350).
  - App now: UpdateTripPaymentRequest.paymentMode @SerialName("payment_mode") val paymentMode: String? = null — nullable; createUpdateRequest passes paymentMode?.apiValue (TripPaymentMapper.kt:299)
  - App location: `TripPaymentRequest.kt:56`
  - To sync: payment_mode is REQUIRED on update; app sends it nullable so when the edit screen doesn't set mode it serializes null/omits it → backend 400. Make paymentMode non-null in UpdateTripPaymentRequest (TripPaymentRequest.kt:56) or enforce non-null before the PUT.
- **🔴 HIGH · response_field_retyped** — GET /trip-payments/tds-report → TdsReportResponse.data
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: GetTdsReport returns []map[string]any (usecase.go:485-487, usecase_port.go:16) — RespondSuccess wraps it as a JSON ARRAY: data: [ {...}, ... ], NOT an object.
  - App now: TdsReportResponse.data is TdsReportDto? — a single OBJECT (financial_year, total_tds, total_amount, by_customer, by_quarter) (TripPaymentRequest.kt:174-214)
  - App location: `TripPaymentRequest.kt:174-178`
  - To sync: Backend sends data as a JSON array; app declares data as a single object → deserialization throws on every TDS report call. Change TdsReportResponse.data (TripPaymentRequest.kt:177) to a List<...> matching the map keys the repo produces. Inspect internal/infrastructure trippayment repo GetTdsReport 
- **🟠 MED · response_field_renamed** — GET /trip-payments/summary → PaymentSummaryReportDto
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: SummaryReportResponse emits json:"receipt_count" (int64); NO json:"payment_count" (entity.go:151, usecase.go:475)
  - App now: PaymentSummaryReportDto.paymentCount @SerialName("payment_count") (Int=0); read into summary.paymentCount (TripPaymentRepositoryImpl.kt:330)
  - App location: `TripPaymentRequest.kt:130`
  - To sync: TripPaymentRequest.kt:130 rename @SerialName("payment_count") to @SerialName("receipt_count"). Count in summary currently always 0.
- **🟠 MED · response_field_removed** — GET /trip-payments (list) → TripPaymentListDataDto
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: PaymentListResponse emits ONLY json:"items","count","page","per_page","total_pages","has_more" (usecase.go:243-250). NO json:"total" and NO json:"summary".
  - App now: TripPaymentListDataDto expects @SerialName("total") (Int=0) and @SerialName("summary") (TripPaymentSummaryDto?=null). Mapper falls back total = data?.total ?: data?.count (TripPaymentMapper.kt:185); summary = data?.summary?.toDomain() → null
  - App location: `TripPaymentDto.kt:194`
  - To sync: Not parse-breaking (defaults present). List `summary` is always null and `total` falls back to count (fine). If the list screen shows an inline summary card it will be empty — it must call GET /trip-payments/summary separately. Optionally remove `summary`/`total` from TripPaymentListDataDto (TripPay
- **🟠 MED · response_field_removed** — GET /trips/{id}/payments → TripPaymentsHistoryDataDto
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: TripPaymentsHistoryResponse emits ONLY json:"payments","count","page","per_page","total_pages","has_more" (usecase.go:440-447). NO trip_id, trip_info, expected_trip_price, paid_trip_price, pending_amount, payment_status, payment_count, total.
  - App now: TripPaymentsHistoryDataDto expects trip_id, trip_info, expected_trip_price, paid_trip_price, pending_amount, payment_status, payment_count, total — all with defaults. Mapper builds summary from paidTripPrice/pendingAmount/paymentCount and pagination from total (TripPaymentMapper.kt:190-201)
  - App location: `TripPaymentDto.kt:235-246`
  - To sync: Not parse-breaking (defaults) but per-trip history summary (paid/pending/count) is always 0 since backend omits them, and mapper uses `total` (→0) instead of backend's `count` for pagination total (TripPaymentMapper.kt:199 should use count). Drop the unsupported fields from TripPaymentsHistoryDataDt
- **🟠 MED · request_field_added_required** — POST /trip-payments → CreateTripPaymentRequest
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: CreatePaymentRequest requires (binding:"required") trip_id, vehicle_id, amount(gt=0), payment_type, payment_mode, payment_date (usecase_port.go:21-36)
  - App now: CreateTripPaymentRequest tripId/vehicleId are Int?=null (nullable); amount/paymentType/paymentMode/paymentDate non-null (TripPaymentRequest.kt:12-26)
  - App location: `TripPaymentRequest.kt:12`
  - To sync: tripId (TripPaymentRequest.kt:12) is nullable with no fallback on the backend → if sent null/omitted backend rejects 400. (vehicle_id is snap-filled from the trip when 0, so less critical.) Make tripId non-null (val tripId: Int) or guarantee non-null at the call site (TripPaymentMapper.createRequest
- **🟢 LOW · response_field_removed** — GET /trip-payments/summary → PaymentSummaryReportDto
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: SummaryReportResponse has NO json:"by_status", NO json:"monthly_trend", NO json:"period" (entity.go:149-157). total_discount IS present and correctly matched.
  - App now: PaymentSummaryReportDto declares period (PaymentPeriodDto?), by_status (List?), monthly_trend (List?) — all nullable with null default
  - App location: `TripPaymentRequest.kt:125`
  - To sync: No crash (all nullable, default null). Optional cleanup: drop period/by_status/monthly_trend from PaymentSummaryReportDto (TripPaymentRequest.kt:125,133,134) since backend never returns them.
- **🟢 LOW · response_field_added** — GET /trip-payments list/detail → TripPaymentDto
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: List PaymentListItem and detail PaymentDetailResponse do NOT emit owner_id, created_by, or nested trip/vehicle/driver/customer/related_payments objects (usecase.go:188-218, 259-304). Detail also exposes created_by_user with only id populated.
  - App now: TripPaymentDto declares owner_id, created_by, nested trip/vehicle/driver/customer, related_payments — all nullable with defaults (TripPaymentDto.kt:59-69)
  - App location: `TripPaymentDto.kt:59`
  - To sync: No action needed — app DTO is a permissive superset (all nullable/default); these app-only fields are simply never populated. Optionally trim TripPaymentDto.kt:59-69 for clarity.
  - _Notes:_ Endpoints all match on path/method: app TRIP_PAYMENTS=\"/trip-payments\", tripPaymentById, TRIP_PAYMENTS_SUMMARY=\"/trip-payments/summary\", TRIP_PAYMENTS_TDS_REPORT=\"/trip-payments/tds-report\", tripPayments(id)=\"/trips/{id}/payments\" (ApiConfig.kt:113,132-135). Dates are snake_case epoch-millis on both sides (payment_date, due_date, invoice_date; start_date/end_date query params) consistently

### customer  
_0 high · 3 medium · 1 low_

- **🟠 MED · response_field_renamed** — GET /customers/:id/statistics → data.trips_by_state (CustomerStatisticsTripsByStateDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: customer_repo.go:351-356 GetCustomerStatistics returns trips_by_state map with keys: "completed", "cancelled", "in_progress", "planned". Canonical trip state is "in_progress" (domain/trip/entity.go:16 StateInProgress="in_progress"; ValidStates=[planned,in_progress,completed,cancelled]). There is NO 
  - App now: CustomerDto.kt:146-148 CustomerStatisticsTripsByStateDto has @SerialName("on_route") val onRoute: Int = 0 — there is no in_progress field; the backend's in_progress count is dropped and onRoute is always 0.
  - App location: `—`
  - To sync: In /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-customer/src/commonMain/kotlin/com/ijs/customer/data/model/CustomerDto.kt:147 rename @SerialName("on_route") to @SerialName("in_progress") and rename the field onRoute→inProgress (default 0). Note: low real-world blast radius because C
- **🟠 MED · other** — GET /customers/:id/trips → ?state= query param (TripStateFilter.ON_ROUTE)
  - Impact: Other
  - Backend: customer_handler.go:180 stateFilter := c.Query("state"); customer_repo.go:216-218 GetTripsByCustomer filters query.Where("state = ?", stateFilter). Valid stored states are planned|in_progress|completed|cancelled (domain/trip/entity.go:22). A value of "on_route" matches zero rows.
  - App now: CustomerDetailContract.kt:28 TripStateFilter.ON_ROUTE("on_route", "On Route"); apiValue "on_route" is sent as parameter("state", it) in CustomerRemoteDataSource.kt:173. The "On Route" filter tab therefore always returns an empty trip list.
  - App location: `—`
  - To sync: In /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailContract.kt:28 change the apiValue from "on_route" to "in_progress" (keep displayName "On Route"). Rename the enum constant to IN_PROGRESS if desired for
- **🟠 MED · response_field_retyped** — client-side trip-state comparisons against trip.state (CustomerTripDto.state)
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: GetTripsByCustomer (customer_repo.go:258) emits "state": t.State where t.State ∈ {planned,in_progress,completed,cancelled}. The literal "on_route" is never produced by the backend.
  - App now: App compares trip state to the literal "on_route" in multiple spots that count/label active trips: TripsTabContent.kt:53 (it.state?.lowercase()=="on_route"), CustomerDetailPdfExporter.kt:210 (activeTrips = trips.count{ it.state=="on_route" }), TripRowComponents.kt:248 (status chip color), Customer.k
  - App location: `—`
  - To sync: Replace the "on_route" string literal with "in_progress" in: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/components/TripsTabContent.kt:53, .../detail/CustomerDetailPdfExporter.kt:210, .../detail/components/TripRowC
- **🟢 LOW · response_field_added** — GET /customers/:id/statistics → data.summary.total_driver_costs (CustomerStatisticsSummaryDto)
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: customer_repo.go:348 summary map includes "total_driver_costs": totalDriverCosts (float64), in addition to total_trip_costs and net_profit (which net_profit subtracts both trip and driver costs: net_profit = total_paid - total_trip_costs - total_driver_costs, line 312).
  - App now: CustomerStatisticsSummaryDto (CustomerDto.kt:113-136) has total_trip_costs and net_profit but no total_driver_costs field; the value is silently ignored by kotlinx.serialization (ignoreUnknownKeys).
  - App location: `—`
  - To sync: Optional: add @SerialName("total_driver_costs") val totalDriverCosts: Double = 0.0 to CustomerStatisticsSummaryDto in /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-customer/src/commonMain/kotlin/com/ijs/customer/data/model/CustomerDto.kt (around line 132) if the UI wants to surface d
  - _Notes:_ Scope: compared backend customer route file (customer_routes.go), handler (customer_handler.go), application DTOs (application/customer/dto.go), use case (usecase.go), domain entity (domain/customer/entity.go), and the GORM repo that actually shapes the map[string]any responses (infrastructure/repository/postgres/customer_repo.go), against the app's screen-customer DTOs (CustomerDto.kt, CustomerPa

### driver  
_2 high · 0 medium · 7 low_

- **🔴 HIGH · path** — GET /drivers/:id/history (driver history)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: NO such route. driver_routes.go registers only: POST/GET /drivers, GET /drivers/available, GET/PUT/DELETE /drivers/:id, PATCH /drivers/:id/status, PATCH /drivers/:id/toggle-active, plus /drivers/:id/costs* and /drivers/:id/financial-summary, /drivers/:id/earnings. No '/drivers/:id/history'.
  - App now: DriverRemoteDataSource.getDriverHistory() calls GET "$baseUrl/$id/history" -> /drivers/{id}/history
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/datasource/DriverRemoteDataSource.kt:317`
  - To sync: Backend has no driver history endpoint. Remove getDriverHistory() and its DriverHistoryApiResponse usage, or hide any UI that calls it. Call returns 404 (DriverHistoryApiResponse(success=false)). Do NOT add a backend route (backend is canonical/read-only).
- **🔴 HIGH · path** — GET /drivers/:id/state-history (driver state history)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: NO such route in driver_routes.go. Only vehicles have state-history elsewhere; drivers do not.
  - App now: DriverRemoteDataSource.getDriverStateHistory() calls GET "$baseUrl/$id/state-history" -> /drivers/{id}/state-history
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/datasource/DriverRemoteDataSource.kt:365`
  - To sync: Backend exposes no driver state-history endpoint. Remove getDriverStateHistory()/StateHistoryResponseDto usage or gate the UI off. The call 404s.
- **🟢 LOW · request_field_removed** — PATCH /drivers/:id/status (UpdateDriverStatus)
  - Impact: App sends a field backend dropped → ignored (usually harmless)
  - Backend: Bound struct driver.UpdateStatusRequest has ONLY: status `json:"status" binding:"required,oneof=active inactive on_trip on_leave suspended"`. No reason/notes fields; extra JSON keys are ignored by Gin binding.
  - App now: updateDriverStatusWithReason() sends StatusUpdateRequestDto{status, reason, notes}. The 'reason' and 'notes' fields are silently dropped by the backend.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/datasource/DriverRemoteDataSource.kt:340`
  - To sync: Backend ignores reason/notes on status change. Either drop updateDriverStatusWithReason() in favor of updateDriverStatus() (which sends UpdateDriverStatusRequest{status} only), or accept that reason/notes are not persisted. No request rejection occurs (status value is the only validated field).
- **🟢 LOW · request_field_added_required** — POST /drivers (CreateDriver) - cost_label / group_id are not here; this is about create-driver Scenario A vs B
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: driver.CreateDriverRequest: first_name, last_name, mobile, license_number, license_expiry are binding:"required". email binding:"omitempty,email". Also supports optional iam_user_id (Scenario A) where password is ignored.
  - App now: CreateDriverRequest sends password (non-null), first_name, last_name, mobile, license_number, license_expiry (Long?=null). App always validates password+licenseExpiry client-side before send, and never sets iam_user_id.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/model/DriverDto.kt:174`
  - To sync: No action required for normal flow: app's CreateDriverViewModel makes password+license_expiry mandatory client-side so required backend fields are always populated. Note: app cannot do Scenario A (link existing IAM user) since it never sends iam_user_id; only flag if that feature is needed.
- **🟢 LOW · response_field_added** — DriverResponse (GET /drivers, GET /drivers/:id) - iam_id field
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: DriverResponse has iam_id `json:"iam_id,omitempty"` (string). Also caretaker_id, created_by_id, updated_by_id, owner_id.
  - App now: DriverDto has no @SerialName("iam_id"). It is silently ignored (ignoreUnknownKeys=true). App also defines @SerialName("owner") and @SerialName("created_by") nested objects that the backend never emits (DriverResponse sends only owner_id/created_by_id ints) — those stay null.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/model/DriverDto.kt:11`
  - To sync: Harmless. iam_id not consumed by app. The owner/created_by nested DTOs (DriverDto.owner / DriverDto.createdBy) will always be null because backend emits only owner_id/created_by_id; safe to delete for clarity. No fix strictly required.
- **🟢 LOW · response_field_removed** — DriverCostsSummaryDto (GET /drivers/:id/costs summary)
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: EarningsSummaryResponse emits ONLY: total_salary, total_incentives, total_deductions, total_other, net_earnings. There is NO cost_count field.
  - App now: DriverCostsSummaryDto declares @SerialName("cost_count") val costCount: Int = 0. Backend never sends it, so it always stays 0.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/driver/DriverCostModels.kt:196`
  - To sync: cost_count is not provided by backend; costCount will always be 0. Remove the field or compute it client-side from costs.size. Cosmetic only (has a default).
- **🟢 LOW · response_field_added** — DriverCostResponse (GET/POST /drivers/:id/costs) - custom_cost_label
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: DriverCostResponse includes custom_cost_label `json:"custom_cost_label,omitempty"`, cost_label, group_id, is_deduction, owner_id, created_by, created_at, updated_at, date(int64 epoch-millis), month(string).
  - App now: DriverCostDto maps all of these correctly (custom_cost_label, cost_label, group_id, is_deduction, owner_id, created_by, date:Long, month:String). Fields align; date typed as Long (epoch-millis) matches backend int64.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/driver/DriverCostModels.kt:62`
  - To sync: No divergence on DriverCostDto; field names/types match backend DriverCostResponse. No action.
- **🟢 LOW · other** — GET /drivers/:id/financial-summary and GET /drivers/:id/earnings
  - Impact: Other
  - Backend: Both routes exist (driver_routes.go:46-47) returning FinancialSummaryResponse (driver, period, trips, revenue_generated, earnings, trip_costs_attributed, efficiency, ranking) and EarningsResponse (driver, period, totals, monthly_breakdown, trip_count).
  - App now: No app code in screen-driver or CostsRemoteDataSource calls these driver-scoped endpoints. (Dashboard's /dashboard/financial-summary is a different endpoint with a different shape.) Backend endpoints are simply unused by the app.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/datasource/costs/CostsRemoteDataSource.kt:70`
  - To sync: No divergence to fix — app does not consume driver financial-summary/earnings. Only relevant if driver-detail finance UI is intended to use them; if so, add typed DTOs matching FinancialSummaryResponse/EarningsResponse.
- **🟢 LOW · request_field_removed** — POST /drivers/:id/costs/bulk (BulkCreate)
  - Impact: App sends a field backend dropped → ignored (usually harmless)
  - Backend: BulkCreateWrapper{costs []BulkCreateItemRequest `json:"costs" binding:"required,min=1"`}. Each item requires cost_id, cost_label, group_id, amount, date (epoch-millis int64). trip_id/vehicle_id/month/description/notes/custom_cost_label/is_deduction optional.
  - App now: BulkCreateDriverCostsRequest{costs: List<BulkDriverCostItem>} with matching @SerialName. ViewModel builds items with costId, costLabel, groupId, amount, date(epoch millis via convertToEpochMillis), month, customCostLabel, notes, isDeduction. Shapes match.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/driver/DriverCostModels.kt:205`
  - To sync: No divergence. Wrapper key 'costs' and required fields all align between BulkDriverCostItem and BulkCreateItemRequest. Note BulkDriverCostsResultDto correctly maps created_count/error_count/costs/errors. No action.
  - _Notes:_ Scope: compared backend driver_routes.go + driver_handler.go + drivercost_handler.go + application/driver/dto.go + application/drivercost/dto.go + domain entities against screen-driver DTOs/datasource, ijs-core-lib DriverCostModels.kt, CostsRemoteDataSource.kt, and ApiConfig.kt.

ENDPOINTS that MATCH exactly (method+path): POST/GET /drivers; GET /drivers/available; GET/PUT/DELETE /drivers/:id; PAT

### trip  
_7 high · 4 medium · 2 low_

- **🔴 HIGH · request_field_added_required** — POST /trips (CreateTripRequest)
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: internal/application/trip/dto.go:64 — `CustomerID uint `json:"customer_id" binding:"required"`` . Free-text customer fields were removed: there is NO `customer_name`/`customer_contact`/`customer` field in the backend bound struct anymore (only `customer_id`).
  - App now: TripDto.kt:436-444 CreateTripRequest: `customerId: Int? = null` (@SerialName customer_id, nullable, no binding) PLUS still sends legacy `customer_name`/`customer_contact`/`customer` (@SerialName customer_name/customer_contact/customer). Repo TripRepositoryImpl.kt:116-118 passes customerId/customerNa
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:436`
  - To sync: Make `customerId` non-null required (`@SerialName("customer_id") val customerId: Int`); REMOVE the `customer`, `customer_name`, `customer_contact` fields from CreateTripRequest. Drop the legacy-customer wiring in TripRepositoryImpl.kt:116-118 and require a selected customer_id before create. Without
- **🔴 HIGH · request_field_added_required** — POST /trips (CreateTripRequest) — scheduled_date / start_time
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: dto.go:18-19 — `ScheduledDate int64 `json:"scheduled_date" binding:"required"``, `StartTime int64 `json:"start_time" binding:"required"``. Both REQUIRED.
  - App now: TripDto.kt:374-377 — `scheduledDate: Long? = null` (@SerialName scheduled_date), `startTime: Long? = null` (@SerialName start_time), both optional/nullable and commented 'Legacy fields (optional)'. CreateTripData (Trip.kt:212-213) also defaults them null.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:374`
  - To sync: Make `scheduledDate` and `startTime` non-null required Long in CreateTripRequest and guarantee they are populated on the create path (derive from plannedStart if needed). Backend binding:required rejects a missing/zero value with 400.
- **🔴 HIGH · request_field_added_required** — POST /trips (CreateTripRequest) — start_lat/start_lng/end_lat/end_lng
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: dto.go:25-29 — `StartLat float64 binding:"required"`, `StartLng float64 binding:"required"`, `EndLat float64 binding:"required"`, `EndLng float64 binding:"required"`. All four coordinates REQUIRED (and non-zero, since float64 binding:required rejects 0).
  - App now: TripDto.kt:384-393 — `startLat: Double? = null`, `startLng: Double? = null`, `endLat: Double? = null`, `endLng: Double? = null` (all nullable, no binding).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:384`
  - To sync: Make the four coordinate fields non-null required Double in CreateTripRequest and require the create form to resolve lat/lng for both start and end before submit. Backend rejects (400) if any is missing/zero.
- **🔴 HIGH · response_field_renamed** — GET /trips/:id/costs (TripCostsListDataDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: tripcost/dto.go:88-93 TripCostsListResponse — `total_cost` (TotalCost), `cost_by_group` (CostByGroup map), `count` (Count). There is NO `total_amount` key.
  - App now: CostApiResponses.kt:44-50 TripCostsListDataDto reads `@SerialName("total_amount") totalAmount: Double = 0.0` only (no total_cost/count/cost_by_group).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/costs/CostApiResponses.kt:48`
  - To sync: Rename to `@SerialName("total_cost") val totalCost` (and optionally add `@SerialName("count") val count: Int = 0` and `@SerialName("cost_by_group") val costByGroup: Map<String,Double> = emptyMap()`). As-is the trip costs list total always shows 0.0 because `total_amount` never appears in the payload
- **🔴 HIGH · response_field_renamed** — GET /trips/:id/costs/summary (TripCostSummaryDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: tripcost/dto.go:128-149 SummaryResponse wraps `summary` { trip_id, total_cost, cost_count, last_updated } and `breakdown` [...]. Headline total tag is `total_cost`; there is NO `total_amount` and NO top-level `by_type`.
  - App now: CostEntityModels.kt:181-191 TripCostSummaryDto reads `@SerialName("total_amount") totalAmount`, `@SerialName("cost_count") costCount`, `@SerialName("by_type") byType` — and is decoded directly from `data` (TripCostSummaryApiResponse.data), not from `data.summary`.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/costs/CostEntityModels.kt:182`
  - To sync: Backend nests the numbers under data.summary and names total `total_cost`. Introduce a wrapper { @SerialName("summary") summary: {...}; @SerialName("breakdown") breakdown: [...] } and read `total_cost` (not total_amount). Current DTO decodes everything to defaults (totalAmount=0, costCount=0) → summ
- **🔴 HIGH · request_field_added_required** — POST /trip-costs (CreateTripCostRequest)
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: tripcost/dto.go:13-26 — required: `trip_id`, `vehicle_id`, `cost_id`, `cost_label`, `group_id`, `amount` (gt=0), `date`. cost_type is NOT a backend field.
  - App now: CostEntityModels.kt:93-124 CreateTripCostRequest: `vehicleId: Int? = null` (nullable!), `costId: String? = null`, `costLabel: String? = null`, `groupId: String? = null`, plus sends `cost_type` (107) which backend ignores. amount/date are non-null (OK).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/costs/CostEntityModels.kt:96`
  - To sync: Backend requires vehicle_id, cost_id, cost_label, group_id (all binding:required). Make `vehicleId: Int`, `costId: String`, `costLabel: String`, `groupId: String` non-null in CreateTripCostRequest and ensure callers populate vehicle_id (the trip's vehicle). A null/missing any of these → 400. (Note: 
- **🔴 HIGH · request_field_retyped** — POST /trips/:id/costs/bulk (BulkTripCostItem.date)
  - Impact: Request type mismatch → 400 or wrong value saved
  - Backend: tripcost/dto.go:43-54 BulkCreateEntryRequest — `Date int64 `json:"date" binding:"required"`` (REQUIRED epoch-millis) and `Amount float64 binding:"required,gt=0"`. There is NO `date_time`, `time`, `fuel_type`, or `cost_type` field in the bulk entry struct.
  - App now: CostEntityModels.kt:130-162 BulkTripCostItem: `date: Long? = null` (nullable — backend requires it), plus sends `time` (147), `date_time` (150), `fuel_type` (154), `cost_type` (138) which backend ignores. costId/costLabel/groupId/amount are non-null (OK).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/costs/CostEntityModels.kt:145`
  - To sync: Make `date: Long` non-null required in BulkTripCostItem and guarantee TripCostEntryViewModel populates it (currently builds the item; verify date is always set). If `date` is omitted the whole bulk entry fails backend validation (binding:required) → that entry is rejected (and if all fail, 400). Ext
- **🟠 MED · request_field_removed** — POST/PUT /trips — customer object & legacy customer fields
  - Impact: App sends a field backend dropped → ignored (usually harmless)
  - Backend: Backend CreateTripRequest/UpdateTripRequest no longer bind `customer` (object), and the UpdateTripRequest binds only `customer_id` (dto.go:136) plus `customer_name`/`customer_contact` are ABSENT from UpdateTripRequest bound struct (only CustomerID exists).
  - App now: CreateTripRequest sends `customer` (TripDto.kt:438, TripCustomerDto), `customer_name` (441), `customer_contact` (443). UpdateTripRequest sends `customer_name`/`customer_contact` (TripDto.kt:515-518) and repo updateTrip() (TripRepositoryImpl.kt:147) sets customerName.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:515`
  - To sync: Remove `customer`, `customer_name`, `customer_contact` from CreateTripRequest and `customer_name`/`customer_contact` from UpdateTripRequest; use `customer_id` only. Extra fields are ignored by Gin binding (not fatal) but are dead data and the snapshot name/contact is overwritten server-side from the
- **🟠 MED · path** — GET /trips/:id/state-history (getTripStateHistory)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: No such route. trip_routes.go has NO state-history endpoint (grep for state-history/state_history in routes + handler returns nothing). State change is PATCH /trips/:id/state only.
  - App now: TripRemoteDataSource.kt:461 — `httpClient.get("$baseUrl/$id/state-history")` i.e. GET /trips/{id}/state-history, decoding into TripApiResponse<StateHistoryResponseDto>.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/datasource/TripRemoteDataSource.kt:461`
  - To sync: This endpoint 404s. Either remove getTripStateHistory()/state-history usage, or repoint to the real endpoint if/when backend adds one. Currently any state-history fetch fails.
- **🟠 MED · response_field_removed** — GET /trips (TripDto.paid_trip_price)
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: Trip response (dto.go:354-362) exposes pricing as `purchase_price`, `selling_value`, `estimated_expense`, `expected_trip_price`, plus payment `payment_status`, `partial_payment_amount`, `pending_amount`, `payment_received_date`, `payment_mode`. There is NO `paid_trip_price` field.
  - App now: TripDto.kt:104 `@SerialName("paid_trip_price") val paidTripPrice: Double? = null`; mapper TripMapper.kt:113 maps it into Trip.paidTripPrice and detail UI shows 'paid' amount.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:104`
  - To sync: `paid_trip_price` is never sent → paidTripPrice is always null (paid amount blank). The backend equivalent for amount-paid is `expected_trip_price - pending_amount`, or use `partial_payment_amount`. Add `@SerialName("partial_payment_amount")` and `@SerialName("payment_received_date")` fields and com
- **🟠 MED · response_field_removed** — GET /trips/:id/costs (TripCostDto fields not in backend response)
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: tripcost/dto.go:66-85 TripCostResponse fields: id, trip_id, vehicle_id, driver_id?, cost_id, cost_label, group_id, amount, date, notes, custom_cost_label?, fuel_quantity?, fuel_rate?, km_per_liter?, owner_id, created_by, created_at, updated_at. There is NO `cost_type`, `time`, `fuel_type`, `created_
  - App now: CostEntityModels.kt TripCostDto reads `@SerialName("cost_type")` (20), `@SerialName("time")` (37), `@SerialName("fuel_type")` (47), `@SerialName("created_by_id")` (49), `@SerialName("created_by_user")` (51). Backend field is `created_by` (uint), not `created_by_id`/`created_by_user`.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/costs/CostEntityModels.kt:48`
  - To sync: `cost_type`, `time`, `fuel_type`, `created_by_id`, `created_by_user` are never populated (all nullable, so no parse crash but always null). The 'created by' chip in the cost list will be blank because backend sends scalar `created_by` (uint), not the `created_by_user` object. Either add `@SerialName
- **🟢 LOW · response_field_added** — GET /trips/:id (TripResponse extra fields app ignores)
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: TripResponse returns several fields the TripDto omits: `current_lat`/`current_lng` (dto.go:335-336 — note app DOES define current_lat/current_lng so OK), `covered_distance` (340), `covered_duration_minutes` (342), `last_location_update` (337), `actual_distance` (app has it), `partial_payment_amount`
  - App now: TripDto.kt has no `covered_distance`, `covered_duration_minutes`, `last_location_update`, `partial_payment_amount`, `payment_received_date`, `special_instructions`, `customer_email`, `customer_address`, `number_of_stops`, `fuel_info`, `remaining_fuel`, owner/created_by. Json parser uses ignoreUnknow
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:127`
  - To sync: Purely additive — safe to ignore. If detail screen wants in-progress progress (covered distance/duration), special instructions, customer email/address, or fuel_info breakdown, add the matching @SerialName fields; otherwise no action needed.
- **🟢 LOW · request_field_removed** — PUT /trips/:id (UpdateTripRequest extra/missing fields)
  - Impact: App sends a field backend dropped → ignored (usually harmless)
  - Backend: UpdateTripRequest (dto.go:87-142) is partial (all pointers). Notable: it accepts `purchase_price`, `estimated_expense`, `payment_status`, `partial_payment_amount`, `pending_amount`, `payment_received_date`, `payment_mode`, `fuel_type`, `filled_fuel_quantity`, `used_fuel_quantity`, `fuel_rate`, `km_p
  - App now: TripDto.kt:461-531 UpdateTripRequest only sends vehicle/driver/schedule/location/cargo/customer + `expected_trip_price`, `selling_value`, priority, notes. Missing the fuel/payment/extra fields above.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-trip/src/commonMain/kotlin/com/ijs/trip/data/model/TripDto.kt:461`
  - To sync: Not breaking — partial update means unsent fields are simply left unchanged. If the edit screen should allow editing fuel/payment fields, add the matching @SerialName fields; otherwise no action.
  - _Notes:_ Scope: trip + tripcost domain. Backend routes (trip_routes.go) and DTOs (application/trip/dto.go, application/tripcost/dto.go) are canonical; apps fixes only.

Endpoint mapping mostly aligns: app TripRemoteDataSource hits POST/GET/PUT /trips, GET /trips/:id, PATCH /trips/:id/state (correct), PATCH /trips/:id/progress, PATCH /trips/:id/location, DELETE /trips/:id (cancelTrip), GET /vehicles/:id/tri

### vehicle (vehicle + vehicledetail + document)  
_4 high · 10 medium · 5 low_

- **🔴 HIGH · path** — GET /vehicles/{id}/state-history
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: NO such route. vehicle_routes.go mounts only: GET /vehicles/:id/state? no — only PATCH /vehicles/:id/state (line 34). No state-history route exists anywhere in vehicle_routes.go / document_routes.go.
  - App now: ApiConfig.kt:93 vehicleStateHistory()='/vehicles/{id}/state-history'; called by VehicleRemoteDataSource.getVehicleStateHistory -> GET "$baseUrl/$id/state-history" (VehicleRemoteDataSource.kt:395). Returns StateHistoryResponseDto.
  - App location: `—`
  - To sync: Backend has no state-history endpoint -> request 404s. Either remove the app call (delete getVehicleStateHistory + StateHistoryResponseDto usage) or, if state history is required, confirm the real backend path. As-is the app must stop calling /vehicles/{id}/state-history. File to fix: VehicleRemoteD
- **🔴 HIGH · path** — GET /vehicles/{id}/history
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: NO such route in vehicle_routes.go (no /history). Available detail routes are /overview, /trips, /route, /documents/detail, /detail, /documents, /location, /location/history.
  - App now: ApiConfig.kt:94 vehicleHistory()='/vehicles/{id}/history'; called by VehicleRemoteDataSource.getVehicleHistory -> GET "$baseUrl/$id/history" (VehicleRemoteDataSource.kt:639). Returns VehicleHistoryApiResponse.
  - App location: `—`
  - To sync: 404. Remove the app call or repoint. If history is needed, use an existing endpoint. File to fix: VehicleRemoteDataSource.kt:639 and ApiConfig.kt:94.
- **🔴 HIGH · response_field_renamed** — POST /vehicles/{id}/documents (UploadDocument) + GET /vehicles/{id}/documents (GetVehicleDocuments) — DocumentResponse
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: document/dto.go DocumentResponse: file path tag is json:"file_path" (line 57). There is NO file_url and NO file_name field. Notes field is json:"remarks" (line 65). Status enum json:"status". Computed: json:"is_expired", json:"days_until_expiry".
  - App now: VehicleDto.kt VehicleDocumentDto: @SerialName("file_url") fileUrl (line 589), @SerialName("file_name") fileName (line 587), @SerialName("notes") notes (line 607). No file_path, no remarks, no is_expired, no days_until_expiry, no tag, no uploader.
  - App location: `—`
  - To sync: Rename app fields to match backend: file_url -> file_path; drop file_name (backend doesn't send it); notes -> remarks. Add @SerialName("tag"), @SerialName("is_expired"), @SerialName("days_until_expiry"), optional @SerialName("uploader"). fileUrl is null today so document view/download-by-url breaks.
- **🔴 HIGH · response_field_renamed** — GET /vehicles/{id}/route — VehicleRouteDto structure (trip/route nesting)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go RouteTabResponse: json:"has_active_trip", json:"trip" (RouteTripResponse: id/state/state_label/customer_name/scheduled_date/start_time/planned_start/planned_end/driver), json:"route" (RouteGeoResponse: origin/destination/current GeoPoint + distance_km), json:"stops" (RouteStopRe
  - App now: VehicleDto.kt VehicleRouteDto is FLAT: trip_id, trip_number, driver_name, origin (string), destination (string), current_position, progress, stops. It expects @SerialName("trip_id")/@SerialName("origin")/@SerialName("destination")/@SerialName("driver_name")/@SerialName("current_position") at top lev
  - App location: `—`
  - To sync: Backend route response is nested (trip{}, route{origin/destination GeoPoint}, stops[]); app VehicleRouteDto is flat. trip_id/origin/destination/driver_name/current_position are all null -> Route tab blanks. Restructure VehicleRouteDto to: has_active_trip, trip: RouteTripDto?, route: RouteGeoDto?(ori
- **🟠 MED · response_field_removed** — GET /vehicles/{id}/documents/detail — VehicleDocumentsDetailDto.alertDocs
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: vehicledetail/dto.go DocumentsDetailResponse has only: json:"summary", json:"document_types", json:"other_documents" (lines 222-226). Alerts live INSIDE summary: DocSummaryResponse.AlertDocs json:"alert_docs" (line 76).
  - App now: VehicleDto.kt VehicleDocumentsDetailDto declares top-level @SerialName("alert_docs") alertDocs (line 518). Backend never emits alert_docs at top level.
  - App location: `—`
  - To sync: Read alerts from summary.alert_docs, not top-level. App top-level alertDocs is always empty. Either drop VehicleDocumentsDetailDto.alertDocs (line 518) and read DocumentsSummaryDto.alerts, OR map summary.alert_docs into it. Note DocumentsSummaryDto.alerts uses @SerialName("alerts") (line 336) but ba
- **🟠 MED · response_field_renamed** — documents summary alerts — DocumentsSummaryDto.alerts
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go DocSummaryResponse: alerts tag is json:"alert_docs" (line 76); expiring tag is json:"expiring_soon" (line 73).
  - App now: VehicleDto.kt DocumentsSummaryDto: @SerialName("alerts") alerts (line 336). expiringSoon already correct (@SerialName("expiring_soon"), line 330).
  - App location: `—`
  - To sync: Rename @SerialName("alerts") -> @SerialName("alert_docs") in DocumentsSummaryDto. As-is alerts list is always empty (silent null). File to fix: VehicleDto.kt:336.
- **🟠 MED · response_field_removed** — GET /vehicles/{id} & GET /vehicles (VehicleDto) — last_location / assigned_driver_name / last_service_date / next_service_date / fuel_level / owner / registered_by
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: vehicle/dto.go VehicleWithAssignmentResponse = VehicleResponse + is_occupied + assigned_driver + trip_assignment. VehicleResponse emits: id, registration_number, make, model, year, vehicle_type, fuel_type, capacity (float64), color, mileage, assigned_driver_id, caretaker_id, state, owner_id, registe
  - App now: VehicleDto.kt declares @SerialName("last_location") (37), @SerialName("fuel_level") (29), @SerialName("assigned_driver_name") (41), @SerialName("last_service_date") (45), @SerialName("next_service_date") (47), @SerialName("owner") (55), @SerialName("registered_by") (59). All stay null/default — back
  - App location: `—`
  - To sync: These app fields are always null/0 (backend omits them). Remove dead fields or stop relying on them in UI (e.g. last_location, fuel_level, last/next_service_date). assigned_driver_name is null -> use assigned_driver.first_name/last_name (assigned_driver IS sent). File: VehicleDto.kt:28-59.
- **🟠 MED · response_field_retyped** — VehicleDto.capacity
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: vehicle/dto.go VehicleResponse.Capacity is json:"capacity" float64 (line 103).
  - App now: VehicleDto.kt @SerialName("capacity") capacity: Int? (line 33). Int decode of a float (e.g. 2.5) will fail/truncate.
  - App location: `—`
  - To sync: Change VehicleDto.capacity to Double? to match backend float64. Same applies to CreateVehicleRequest.capacity (Int=4, line 195) and UpdateVehicleRequest.capacity (Int?, line 218) which serialize as int but backend field is float64 — int is accepted by JSON-number binding, so request side is low risk
- **🟠 MED · response_field_retyped** — GET /vehicles/{id}/trips — VehicleTripItemDto distance & total_pages/has_more
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: vehicledetail/dto.go TripsTabResponse: json:"summary", json:"trips", json:"page", json:"per_page", json:"total", json:"total_pages". NO has_more. TripItemResponse distance tag is json:"distance_km" (line 125), plus json:"cargo_type", json:"priority". No trip_number, no origin/destination (uses start
  - App now: VehicleDto.kt VehicleTripsDto expects @SerialName("has_more") (line 393, default false -> harmless) and total_pages (ok). VehicleTripItemDto expects @SerialName("distance") distance (line 429) — backend sends distance_km, so distance is always null. Also expects driver_name (string) but backend send
  - App location: `—`
  - To sync: In VehicleTripItemDto: rename @SerialName("distance") -> @SerialName("distance_km") (line 429); driver_name (line 426) is never sent (backend sends driver object) -> derive from driver, or add @SerialName("driver") AssignedDriverDto. trip_number/origin/destination/end_time are app-only and stay null
- **🟠 MED · response_field_renamed** — GET /vehicles/{id}/detail — VehicleDetailDto.vehicle wrapper
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go FullDetailResponse: json:"vehicle" (VehicleBasicResponse: id/registration_number/display_name/make/model/year/vehicle_type/fuel_type/capacity/color/state/state_label/is_occupied — NO mileage, fuel_level, assigned_driver_id, owner_id, created_at, updated_at, documents), json:"ass
  - App now: VehicleDto.kt VehicleDetailDto.vehicle is typed as full VehicleDto (line 285). Backend sends VehicleBasicResponse (subset). Fields missing from VehicleBasicResponse will be null in the embedded VehicleDto (fine due to defaults), but state_label/display_name (backend-only) are dropped by VehicleDto. 
  - App location: `—`
  - To sync: VehicleDetailDto.trips is typed TripsSummaryDto (total/planned/in_progress/.../recent_trips) but backend /detail sends trips = {total, recent[RecentTripResponse]} — recent_trips name mismatch (@SerialName("recent_trips") vs backend json:"recent") AND no planned/in_progress/completed/cancelled counts
- **🟠 MED · response_field_renamed** — TripsSummaryDto.recentTrips vs backend recent
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: FullDetailResponse.trips DetailTripsResponse.Recent tag json:"recent" (vehicledetail/dto.go line 259); RecentTripResponse fields: id/state/state_label/scheduled_date/start_location/end_location.
  - App now: VehicleDto.kt TripsSummaryDto.recentTrips @SerialName("recent_trips") (line 372) -> never matches backend json:"recent" in /detail. Always empty.
  - App location: `—`
  - To sync: For the /detail trips block, rename @SerialName("recent_trips") -> @SerialName("recent") (or introduce a dedicated DetailTripsDto). Note: this TripsSummaryDto is reused in overview (DocumentsSummaryDto etc) — verify overview uses recent_trips? Backend OverviewResponse has no recent_trips, so recent_
- **🟠 MED · response_field_renamed** — VehicleStatsDto.totalDistance / tripsThisMonth vs backend
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go StatsResponse: total_trips, completed_trips, cancelled_trips, active_trips, total_distance_km (line 53), this_month_trips (line 54).
  - App now: VehicleDto.kt VehicleStatsDto: @SerialName("total_trips") ok, @SerialName("completed_trips") ok, @SerialName("total_distance") (line 309) vs backend total_distance_km -> null; @SerialName("trips_this_month") (line 311) vs backend this_month_trips -> null; @SerialName("distance_this_month") (line 313
  - App location: `—`
  - To sync: Rename @SerialName("total_distance") -> @SerialName("total_distance_km") (line 309); @SerialName("trips_this_month") -> @SerialName("this_month_trips") (line 311); drop distance_this_month (not sent); add cancelled_trips/active_trips if needed. As-is distance and this-month stats blank. File: Vehicl
- **🟠 MED · response_field_renamed** — DocumentAlertDto fields vs backend DocAlertResponse
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go DocAlertResponse: json:"type", json:"tag", json:"name", json:"expiry_date", json:"days_remaining", json:"status", json:"is_expired", json:"id".
  - App now: VehicleDto.kt DocumentAlertDto: @SerialName("type") ok, @SerialName("type_name") (line 347) NOT sent (backend uses name), @SerialName("alert_type") (line 349) NOT sent, @SerialName("message") (line 351) NOT sent, @SerialName("days_remaining") ok. Missing tag/name/expiry_date/is_expired/status.
  - App location: `—`
  - To sync: DocumentAlertDto fields type_name/alert_type/message are never sent (always blank). Backend sends name/tag/expiry_date/is_expired/status. Replace with @SerialName("name"), @SerialName("tag"), @SerialName("expiry_date"), @SerialName("is_expired"), @SerialName("status"). File: VehicleDto.kt:344-354.
- **🟠 MED · response_field_renamed** — RouteStopDto.sequence vs backend stop_order
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go RouteStopResponse: json:"stop_order" (line 146), json:"location", json:"latitude", json:"longitude", json:"stop_duration_minutes", json:"notes", json:"is_completed", json:"status". No type/address/scheduled_time/actual_time.
  - App now: VehicleDto.kt RouteStopDto: @SerialName("sequence") (line 490), @SerialName("type") (492), @SerialName("address") (496), @SerialName("scheduled_time") (500), @SerialName("actual_time") (502) — none sent by backend. Missing stop_order/latitude/longitude/stop_duration_minutes/is_completed.
  - App location: `—`
  - To sync: Rename @SerialName("sequence") -> @SerialName("stop_order"); drop type/address/scheduled_time/actual_time; add latitude/longitude/stop_duration_minutes/is_completed. sequence/type/address always blank today. File: VehicleDto.kt:486-506.
- **🟢 LOW · response_field_renamed** — GET /vehicles/{id}/documents/detail document item — VehicleDocumentInfoDto.uploadedAt
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: vehicledetail/dto.go DocItemResponse uses json:"file_size", json:"file_size_label", json:"mime_type", json:"uploaded_by" (string), json:"uploaded_at". There is NO file_url field on DocItemResponse.
  - App now: VehicleDto.kt VehicleDocumentInfoDto declares @SerialName("file_url") fileUrl (line 564) which backend never sends; and omits file_size/file_size_label/mime_type/status_label-ok/issue_date. status_label present (line 560 ok), days_remaining present (line 562 ok).
  - App location: `—`
  - To sync: Drop fileUrl from VehicleDocumentInfoDto (backend DocItemResponse has no file_url; download is via /documents/{id}/download). Optionally add file_size, file_size_label, mime_type, issue_date, is_expired, is_expiring_soon, uploaded_by. File: VehicleDto.kt:564.
- **🟢 LOW · request_field_removed** — PATCH /vehicles/{id}/state — StateUpdateRequestDto
  - Impact: App sends a field backend dropped → ignored (usually harmless)
  - Backend: vehicle/dto.go UpdateStateRequest binds ONLY State json:"state" binding:"required" (line 42). reason and notes are NOT bound (ignored).
  - App now: StateHistoryDto.kt StateUpdateRequestDto sends state + @SerialName("reason") + @SerialName("notes") (lines 12-19). VehicleRemoteDataSource.updateVehicleState builds it (line 370).
  - App location: `—`
  - To sync: Harmless (backend ignores extra fields). Optionally drop reason/notes from the vehicle state request to match backend, or leave as-is. Path /vehicles/{id}/state matches. File: StateHistoryDto.kt:12, VehicleRemoteDataSource.kt:370.
- **🟢 LOW · response_field_added** — VehicleDto — backend-only fields caretaker_id, updated_by_id
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: vehicle/dto.go VehicleResponse emits json:"caretaker_id" (omitempty, line 107) and json:"updated_by_id" (omitempty, line 111).
  - App now: VehicleDto.kt has no caretaker_id and no updated_by_id (ignoreUnknownKeys=true so safely dropped).
  - App location: `—`
  - To sync: Additive; app safely ignores (ignoreUnknownKeys=true). Add @SerialName("caretaker_id") and @SerialName("updated_by_id") only if UI needs them. File: VehicleDto.kt VehicleDto.
- **🟢 LOW · request_field_added_required** — POST /vehicles (CreateVehicleRequest) — backend optional caretaker_id/mileage
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: vehicle/dto.go CreateVehicleRequest: required = registration_number, make, model, year (binding:"required"). Optional includes mileage (float64, line 21) and caretaker_id (*uint, line 23).
  - App now: VehicleDto.kt CreateVehicleRequest sends registration_number, make, model, year, vehicle_type, fuel_type, capacity, color, assigned_driver_id. Does NOT send mileage or caretaker_id.
  - App location: `—`
  - To sync: No required field missing (the 4 required are all sent). mileage/caretaker_id are optional — add to CreateVehicleRequest only if needed. File: VehicleDto.kt:181 (no blocker).
- **🟢 LOW · request_field_retyped** — POST /vehicles/with-documents (multipart) — capacity form field
  - Impact: Request type mismatch → 400 or wrong value saved
  - Backend: vehicle_handler.go parseVehicleForm reads form fields registration_number, make, model, year, vehicle_type, fuel_type, capacity (parsed as float64), color (lines 368-417). Required: registration_number, make, model, year.
  - App now: VehicleRemoteDataSource.createVehicleWithDocuments appends registration_number, make, model, year, vehicle_type, fuel_type, capacity (request.capacity.toString() from Int=4), color, plus per-doc binary parts + *_expiry strings (lines 193-251).
  - App location: `—`
  - To sync: Form field names all match backend. capacity sent as int string '4' parses fine into float64. No blocker; only note app capacity is Int (see VehicleDto.kt:33 retype). Document part names (registration_certificate, insurance, puc_certificate, fitness_certificate, road_tax, permit + *_expiry) are cons
  - _Notes:_ Scope: vehicle CRUD + state + vehicledetail tabs + documents. Backend canonical (read-only).

ENDPOINTS: app paths match backend for /vehicles, /vehicles/{id}, /vehicles/{id}/detail, /trips, /route, /documents/detail, /documents, /state, POST /with-documents. TWO app endpoints have NO backend route: GET /vehicles/{id}/state-history (ApiConfig.kt:93) and GET /vehicles/{id}/history (ApiConfig.kt:94)

### finance (vehicle purchase / loan / EMI payments)  
_13 high · 0 medium · 0 low_

- **🔴 HIGH · path** — GET vehicle purchase
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no route. Vehicle sub-routes in registerVehicleRoutes (vehicle_routes.go:28-52) are only: POST/GET/PUT/PATCH/DELETE /vehicles[/:id], /vehicles/:id/overview, /trips, /route, /documents[/detail], /detail, /documents/bulk, /location[/history]. No /vehicles/:id/purchase. No purchase handler/use
  - App now: GET ${BASE_URL}/vehicles/{vehicleId}/purchase — getPurchase. Path also ApiConfig.kt:95 vehiclePurchase().
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:53`
  - To sync: Backend has no purchase API; call 404s. getPurchase swallows 404 → returns success+null, so screen silently shows empty. Either backend adds endpoint or app removes purchase flow. Fix at VehicleFinanceRemoteDataSource.kt:53 and ApiConfig.kt:95.
- **🔴 HIGH · path** — POST create vehicle purchase
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no POST /vehicles/:id/purchase. No CreatePurchaseRequest bind struct in backend.
  - App now: POST ${BASE_URL}/vehicles/{vehicleId}/purchase with CreatePurchaseRequest (VehicleFinanceDto.kt:90; purchase_date, purchase_price, payment_type required).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:73`
  - To sync: No backend create-purchase endpoint or struct; request 404s (failure NOT swallowed). Remove/disable or add backend route+handler+usecase. Fix at VehicleFinanceRemoteDataSource.kt:73; DTO VehicleFinanceDto.kt:90-126.
- **🔴 HIGH · path** — PUT update vehicle purchase
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no PUT /vehicles/:id/purchase. No UpdatePurchaseRequest struct in backend.
  - App now: PUT ${BASE_URL}/vehicles/{vehicleId}/purchase with UpdatePurchaseRequest (VehicleFinanceDto.kt:132).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:90`
  - To sync: No backend endpoint; PUT 404s. Remove/disable or add backend support. Fix at VehicleFinanceRemoteDataSource.kt:90; DTO VehicleFinanceDto.kt:132.
- **🔴 HIGH · path** — GET loan summary
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no GET /vehicles/:id/loan-summary. No LoanSummary entity/response in backend.
  - App now: GET ${BASE_URL}/vehicles/{vehicleId}/loan-summary — response LoanSummaryDto (VehicleFinanceDto.kt:248). Path ApiConfig.kt:96.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:109`
  - To sync: No backend loan-summary endpoint; 404 blanks the loan-summary card. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:109; ApiConfig.kt:96.
- **🔴 HIGH · path** — GET loan payments (vehicle-scoped, paginated)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no GET /vehicles/:id/loan-payments. No LoanPayment entity/repository in backend.
  - App now: GET ${BASE_URL}/vehicles/{vehicleId}/loan-payments?page&per_page&status — response LoanPaymentsListResponse (VehicleFinanceDto.kt:330). Path ApiConfig.kt:97.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:132`
  - To sync: No backend endpoint; 404 → EMI history list empty. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:132.
- **🔴 HIGH · path** — GET all loan payments (global, paginated)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no GET /vehicle-loan-payments. The entire /vehicle-loan-payments resource family does not exist (no route file, no handler).
  - App now: GET ${BASE_URL}/vehicle-loan-payments?page&per_page&vehicle_id&status. Const ApiConfig.kt:146 VEHICLE_LOAN_PAYMENTS.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:156`
  - To sync: No backend resource; 404. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:156; ApiConfig.kt:146.
- **🔴 HIGH · path** — GET loan payment by id
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no GET /vehicle-loan-payments/:id.
  - App now: GET ${BASE_URL}/vehicle-loan-payments/{paymentId}. Const ApiConfig.kt:147 vehicleLoanPaymentById().
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:175`
  - To sync: No backend endpoint; 404. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:175.
- **🔴 HIGH · path** — POST record loan payment
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no POST /vehicle-loan-payments. No RecordPaymentRequest bind struct in backend.
  - App now: POST ${BASE_URL}/vehicle-loan-payments with RecordPaymentRequest (VehicleFinanceDto.kt:198; vehicle_purchase_id, amount, payment_date required).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:190`
  - To sync: No backend endpoint; create 404s. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:190; DTO VehicleFinanceDto.kt:198.
- **🔴 HIGH · path** — PUT update loan payment
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no PUT /vehicle-loan-payments/:id. No UpdatePaymentRequest struct in backend.
  - App now: PUT ${BASE_URL}/vehicle-loan-payments/{paymentId} with UpdatePaymentRequest (VehicleFinanceDto.kt:235).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:207`
  - To sync: No backend endpoint; 404. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:207.
- **🔴 HIGH · path** — POST mark EMI paid
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no POST /vehicle-loan-payments/:id/pay. No MarkEmiPaidRequest struct in backend.
  - App now: POST ${BASE_URL}/vehicle-loan-payments/{paymentId}/pay with MarkEmiPaidRequest (VehicleFinanceDto.kt:221). Const ApiConfig.kt:148 vehicleLoanPaymentPay().
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:224`
  - To sync: No backend endpoint; 404. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:224.
- **🔴 HIGH · path** — DELETE loan payment
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no DELETE /vehicle-loan-payments/:id.
  - App now: DELETE ${BASE_URL}/vehicle-loan-payments/{paymentId} — returns DeleteResponse (VehicleFinanceDto.kt:363).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:241`
  - To sync: No backend endpoint; 404. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:241.
- **🔴 HIGH · path** — GET upcoming EMIs (alerts)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no GET /vehicle-loan-payments/upcoming. No EmiAlert entity in backend.
  - App now: GET ${BASE_URL}/vehicle-loan-payments/upcoming?days — response EmiAlertsResponse (VehicleFinanceDto.kt:350). Const ApiConfig.kt:149.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:258`
  - To sync: No backend endpoint; 404 → upcoming-EMI alerts blank. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:258; ApiConfig.kt:149.
- **🔴 HIGH · path** — GET overdue EMIs (alerts)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: ABSENT — no GET /vehicle-loan-payments/overdue.
  - App now: GET ${BASE_URL}/vehicle-loan-payments/overdue — response EmiAlertsResponse. Const ApiConfig.kt:150.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt:274`
  - To sync: No backend endpoint; 404 → overdue-EMI alerts blank. Remove/disable or implement on backend. Fix at VehicleFinanceRemoteDataSource.kt:274; ApiConfig.kt:150.
  - _Notes:_ SYSTEMIC FINDING: The entire finance domain has NO backend counterpart. The Go backend (canonical) does not implement vehicle-purchase, loan-summary, loan-payment, or EMI-alert functionality anywhere — no domain/, application/, handler, or *_routes.go for it. registerVehicleRoutes (vehicle_routes.go:28-52) defines vehicle sub-routes only for overview/trips/route/documents/detail/location; register

### report  
_10 high · 2 medium · 2 low_

- **🔴 HIGH · response_field_renamed** — GET /reports/profit-loss (Fleet P&L) -> FleetProfitLossDto.summary
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: FleetPLResult.FleetSummary json:"fleet_summary" (type FleetTotal); top-level json:"period" is a STRING, plus separate json:"start_date"/json:"end_date" int64; also json:"vehicle_count"
  - App now: FleetProfitLossDto @SerialName("summary") summary: FleetPLSummaryDto?, @SerialName("period") period: PeriodDto? (object). No field reads fleet_summary; period decoded as object not string.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/VehiclePLDto.kt:121-131 (FleetProfitLossDto): rename @SerialName("summary") -> @SerialName("fleet_summary"); change period to a String? (the backend emits period as the period NAME s
- **🔴 HIGH · response_field_removed** — GET /reports/profit-loss (Fleet P&L) -> FleetPLSummaryDto fields
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: FleetTotal only has: total_trips, total_distance, total_revenue, total_cost, driver_cost, net_profit, active_vehicles. It does NOT emit total_vehicles, completed_trips, total_expenses, total_trip_costs, total_maintenance_costs, gross_profit, total_profit, profit_margin, is_profitable, profitable_veh
  - App now: FleetPLSummaryDto expects total_vehicles, completed_trips, total_expenses, total_trip_costs, total_maintenance_costs, gross_profit, total_profit, profit_margin, is_profitable, profitable_vehicles, loss_making_vehicles — all of which the backend never sends (silently default to 0/false).
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/VehiclePLDto.kt:137-170 (FleetPLSummaryDto): align fields to FleetTotal. Backend has no total_expenses (use total_cost), no gross_profit/net_profit==total_profit (only net_profit), n
- **🔴 HIGH · response_field_retyped** — POST /reports/profit-loss/trips (Multi-trip P&L) response envelope
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: Returns domain.MultiTripProfitLossResponse = object { json:"period":PeriodInfo, json:"trips":[]TripProfitLoss, json:"summary":TripsSummary }.
  - App now: ReportsRemoteDataSource.getMultiTripProfitLoss decodes ProfitLossResponse<List<TripProfitLossDto>> — expects data to be a bare ARRAY of trips, not the {period,trips,summary} wrapper.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/datasource/ReportsRemoteDataSource.kt:200: decode ProfitLossResponse<MultiTripPLResponseDto> (new DTO mirroring MultiVehiclePLResponseDto: period: PeriodDto?, trips: List<TripProfitLossDto
- **🔴 HIGH · response_field_retyped** — GET /reports/profit-loss/cost-type/:type (single) response envelope
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: Returns CostTypePLResult = object { json:"period":PeriodInfo, json:"cost_type_analysis":domain.CostTypeProfitLoss }. CostTypeProfitLoss fields: cost_id, cost_label, total_amount, transaction_count, percentage_of_total, by_vehicle[], by_month[], statistics{}.
  - App now: ReportsRemoteDataSource.getCostTypeAnalysis (line 230) decodes ProfitLossResponse<CostTypeAnalysisDto> directly. CostTypeAnalysisDto expects flat cost_type, total_count, average_per_entry, vehicle_breakdown, monthly_trend — none of which match (wrapper is {period, cost_type_analysis}; inner uses tra
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/ConsolidatedPLDto.kt:16-65 + datasource line 230: introduce wrapper DTO { period: PeriodDto?, cost_type_analysis: CostTypeAnalysisDto } and decode that; rename CostTypeAnalysisDto fi
- **🔴 HIGH · response_field_retyped** — POST /reports/profit-loss/cost-types (multi) response envelope
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: Returns domain.CostTypeProfitLossResponse = object { json:"period":PeriodInfo, json:"cost_types":[]CostTypeProfitLoss, json:"summary":CostTypesSummary }.
  - App now: ReportsRemoteDataSource.getMultiCostTypeAnalysis (line 255) decodes ProfitLossResponse<List<CostTypeAnalysisDto>> — expects a bare array, but backend sends {period, cost_types, summary} object.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/.../ReportsRemoteDataSource.kt:255: decode ProfitLossResponse<MultiCostTypeResponseDto>{ period, cost_types: List<CostTypeAnalysisDto>, summary } and return data?.costTypes. Plus the inner CostTypeAnalysisDto field renames from the previous find
- **🔴 HIGH · response_field_retyped** — POST /reports/profit-loss/consolidated -> ConsolidatedPLDto
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: ConsolidatedProfitLossResponse is DEEPLY NESTED: json:"period":PeriodInfo, json:"filters_applied":{vehicles,trips,cost_ids}, json:"revenue":{total_revenue,by_vehicle[],by_period[]}, json:"expenses":{total_expenses,by_cost_type(map),by_vehicle[],by_period[]}, json:"profit_loss":{gross_profit,net_prof
  - App now: ConsolidatedPLDto is FLAT: start_date,end_date,group_by,total_revenue,total_expenses,net_profit,profit_margin,is_profitable,total_vehicles,total_trips,completed_trips,vehicle_summary[],trip_summary[],cost_breakdown[],period_breakdown[]. NONE of these top-level keys (except none) match backend; total
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/ConsolidatedPLDto.kt:72-103 + mapper toConsolidatedPL (lines 143-159): rebuild ConsolidatedPLDto to the nested shape {period, filters_applied, revenue, expenses, profit_loss, trends}
- **🔴 HIGH · request_field_added_required** — POST /reports/profit-loss/vehicles -> MultiVehiclePLRequest start_date/end_date
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: MultiVehicleProfitLossRequest: VehicleIDs json:"vehicle_ids" binding:"required,min=1"; StartDate json:"start_date" binding:"required" (int64, non-pointer); EndDate json:"end_date" binding:"required".
  - App now: MultiVehiclePLRequest: vehicleIds: List<Int> (ok), startDate: Long? = null, endDate: Long? = null — nullable with null default; if app sends null/omits, gin binding rejects with 400.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/ProfitLossRequest.kt:14-22: make startDate: Long and endDate: Long non-nullable (required) so a value is always serialized; backend binding:"required" rejects missing/zero dates. Cal
- **🔴 HIGH · request_field_added_required** — POST /reports/profit-loss/cost-types -> MultiCostTypePLRequest start_date/end_date
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: CostTypeProfitLossRequest: CostIDs json:"cost_ids" (preferred), StartDate json:"start_date" binding:"required", EndDate json:"end_date" binding:"required", VehicleIDs json:"vehicle_ids,omitempty".
  - App now: MultiCostTypePLRequest sends @SerialName("cost_ids") costTypes: List<String> (correct tag), but startDate/endDate are Long? = null. Null/missing -> 400.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/ProfitLossRequest.kt:45-54: make startDate/endDate non-nullable Long (binding:required). cost_ids tag already matches backend's preferred field (good).
- **🔴 HIGH · request_field_added_required** — POST /reports/profit-loss/consolidated -> ConsolidatedPLRequest start_date/end_date
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: ConsolidatedProfitLossRequest: StartDate json:"start_date" binding:"required", EndDate json:"end_date" binding:"required"; vehicle_ids/trip_ids/cost_ids optional; group_by optional (validated against day|week|month|quarter|year).
  - App now: ConsolidatedPLRequest: startDate/endDate Long? = null; cost_ids tag correct. Null dates -> 400.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/ProfitLossRequest.kt:61-74: make startDate/endDate non-nullable Long. group_by allowed values are day|week|month|quarter|year (backend defaults to month); ensure app only sends those
- **🔴 HIGH · response_field_renamed** — cost_breakdown items -> CostBreakdownItemDto.cost_type
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain.CostTypeBreakdown json tags: cost_id, cost_label, group_id(omitempty), amount, count. There is NO json:"cost_type" and NO json:"percentage" on this entity (used in TripPLResult/VehiclePLResult/CostOverview cost_breakdown).
  - App now: CostBreakdownItemDto requires non-null @SerialName("cost_type") costType: String (no default) plus optional cost_id/cost_label/group_id and percentage. Backend never emits cost_type or percentage in cost_breakdown -> the required non-null cost_type has no value.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/ProfitLossDto.kt:43-67: cost_type has no JSON source and no default -> kotlinx throws MissingFieldException on any cost_breakdown array, failing the whole response decode. Give costT
- **🟠 MED · response_field_renamed** — GET /vehicles/:id/profit-loss & Fleet vehicles[] -> VehicleProfitLossDto.vehicle_number/make/model
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain.VehicleProfitLoss / VehiclePLResult use json:"vehicle_registration" only. No json:"vehicle_number", no json:"make", no json:"model".
  - App now: VehicleProfitLossDto has @SerialName("vehicle_number"), @SerialName("make"), @SerialName("model") (all null) AND @SerialName("vehicle_registration"). Mapper line 53 prefers vehicleNumber ?: vehicleRegistration so it falls back, but make/model always null.
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/VehiclePLDto.kt:54-59: vehicle_number/make/model are never sent by backend (single-vehicle nor fleet list). Either drop them or keep as optional; rely on vehicle_registration. Mapper
- **🟠 MED · response_field_removed** — GET /vehicles/:id/profit-loss -> VehicleProfitLossDto extra fields
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: VehiclePLResult fields: vehicle_id, vehicle_registration, period(PeriodInfo object), total_trips, total_distance, total_revenue, total_cost, fuel_cost, maintenance_cost, driver_cost, other_cost, net_profit, profit_margin, profit_status, avg_profit_per_trip, avg_profit_per_km, cost_breakdown. NO comp
  - App now: VehicleProfitLossDto expects completed_trips, total_trip_costs, total_maintenance_costs, total_expenses, gross_profit, is_profitable, trip_summary — none sent (default 0/false/empty). Mapper uses totalExpenses ?: total_cost and grossProfit ?: net_profit fallbacks (lines 66-69), so totals survive, bu
  - App location: `—`
  - To sync: /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-report/src/commonMain/kotlin/com/ijs/reports/data/model/VehiclePLDto.kt:51-108: backend single-vehicle P&L uses total_cost (not total_expenses) and net_profit (not gross_profit), profit_status (not is_profitable), and has no completed_tri
- **🟢 LOW · response_field_renamed** — POST /reports/profit-loss/trips -> TripProfitLossDto.trip_price/customer fields
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain.TripProfitLoss (used in multi-trip) fields: trip_id, vehicle_id, vehicle_registration, driver_id(uint), driver_name, trip_date, start_location, end_location, distance, purchase_price, selling_value, total_cost, gross_profit, net_profit, profit_margin, payment_status, pending_amount. NO custom
  - App now: TripProfitLossDto expects customer_id, customer_name, trip_price, status, cost_breakdown for ALL trip endpoints; in multi-trip these are absent and default to null/0.
  - App location: `—`
  - To sync: Multi-trip rows lack customer_id/customer_name/trip_price/status/cost_breakdown (only single-trip GET /trips/:id/profit-loss carries them). No rename needed since selling_value present; just be aware multi-trip list rows show blank customer/status. No DTO change strictly required (defaults absorb), 
- **🟢 LOW · request_field_renamed** — POST /reports/profit-loss/trips -> MultiTripPLRequest (no required dates but conditional)
  - Impact: Request field name wrong → backend ignores it (data not saved)
  - Backend: MultiTripProfitLossRequest: TripIDs json:"trip_ids" (optional), StartDate json:"start_date" *int64 optional, EndDate json:"end_date" *int64 optional, VehicleID json:"vehicle_id,omitempty" optional. Handler 400s if trip_ids empty AND (start_date OR end_date) nil.
  - App now: MultiTripPLRequest: tripIds/vehicleId/startDate/endDate all nullable — matches backend (truly optional). OK except the conditional: must send trip_ids OR both dates.
  - App location: `—`
  - To sync: No DTO change needed; tags match (trip_ids, vehicle_id, start_date, end_date). Caller logic must guarantee either non-empty trip_ids or both start_date+end_date, else backend returns 400 'either trip_ids or date range is required'.
  - _Notes:_ Scope: report domain only. Backend handlers return the domain/usecase result structs verbatim via RespondSuccess(data=result), so the json: tags in internal/domain/report/types.go and internal/application/report/usecase_port.go are the wire truth (confirmed in usecase.go return blocks).

ENDPOINT PATHS: all app ApiConfig constants/builders match backend routes exactly (REPORTS_PL=/reports/profit-l

### dashboard  
_4 high · 2 medium · 3 low_

- **🔴 HIGH · response_field_renamed** — GET /dashboard/cost-overview -> data (CostOverviewDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain/report/types.go:41-68 CostOverview tags: period, total_expenses, total_revenue, total_profit, total_loss, net_profit_loss, completed_trips, fuel_expenses, toll_expenses, maintenance_expenses, other_expenses, pending_payments, received_payments, driver_allowance_expenses, parking_expenses, loa
  - App now: DashboardModels.kt:269-313 CostOverviewDto expects: filter, period_label, total_expenses, total_revenue, profit_loss, is_profit, completed_trips, trip_costs, maintenance_costs, fuel_costs, toll_costs, other_costs, plus driver_allowance_expenses/parking_expenses/loading_charges/unloading_charges/chal
  - App location: `—`
  - To sync: Major mismatch — the whole DTO is wrong. Rename/retype in CostOverviewDto: drop filter -> use @SerialName("period"); drop period_label (backend has none); profit_loss -> @SerialName("net_profit_loss"); drop is_profit (backend has none; derive client-side from net_profit_loss>=0); fuel_costs -> @Seri
- **🔴 HIGH · response_field_renamed** — GET /dashboard/cost-overview -> trip_cost_breakdown[] / maintenance_cost_breakdown[] (CostBreakdownItemDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain/report/types.go:71-77 CostTypeBreakdown tags: cost_id (string), cost_label (string), group_id (string,omitempty), amount (float64), count (int)
  - App now: DashboardModels.kt:318-324 CostBreakdownItemDto expects @SerialName("cost_type") val costType, amount, count
  - App location: `—`
  - To sync: cost_type does not exist on backend; the label is cost_label and the id is cost_id. Replace CostBreakdownItemDto fields: @SerialName("cost_id") val costId: String = "", @SerialName("cost_label") val costLabel: String = "", @SerialName("group_id") val groupId: String = "", amount, count. Currently co
- **🔴 HIGH · response_field_renamed** — GET /dashboard/financial-summary -> data (FinancialSummaryDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain/report/types.go:80-99 FinancialSummary tags: period, start_date, end_date, total_revenue, total_expenses, trip_costs, maintenance_costs, driver_costs, net_profit, profit_margin, profit_status, pending_payments, received_payments, completed_trips, total_trips, avg_trip_revenue, avg_trip_cost, 
  - App now: DashboardModels.kt:562-595 FinancialSummaryDto expects: period, period_label, total_revenue, total_expenses, net_profit, profit_margin, profit_status, pending_payments, completed_trips, avg_trip_revenue, avg_trip_profit, fuel_cost, toll_cost, maintenance_cost, other_cost
  - App location: `—`
  - To sync: period/total_revenue/total_expenses/net_profit/profit_margin/profit_status/pending_payments/completed_trips/avg_trip_revenue/avg_trip_profit MATCH (good). Wrong/missing: drop period_label (no backend field). The cost-breakdown fields fuel_cost/toll_cost/maintenance_cost/other_cost DO NOT exist on ba
- **🔴 HIGH · response_field_renamed** — GET /dashboard/pending-payments -> payments[] (PendingPaymentDto)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: domain/report/repository.go:118-130 PendingPayment tags: trip_id (uint), vehicle_id (uint), vehicle_registration, customer_name, customer_contact (string, non-pointer/always present), total_amount (float64), received_amount (float64), pending_amount (float64), trip_date (int64), days_overdue (int)
  - App now: DashboardModels.kt:356-383 PendingPaymentDto expects: trip_id, vehicle_registration, customer_name, customer_contact, selling_value, partial_payment, pending_amount, payment_status, trip_date, start_location, end_location, days_overdue
  - App location: `—`
  - To sync: Renamed amounts: selling_value and partial_payment do NOT exist on backend — backend sends total_amount and received_amount. Rename PendingPaymentDto.sellingValue -> @SerialName("total_amount"), partialPayment -> @SerialName("received_amount"). Remove fields backend never sends: payment_status (line
- **🟠 MED · response_field_removed** — GET /dashboard -> team_stats (TeamStatsDto)
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: domain/dashboard/entity.go:82-84 TeamStats ONLY has total_members (int64). No total_managers / total_supervisors exist.
  - App now: DashboardModels.kt:222-230 TeamStatsDto declares @SerialName("total_managers") and @SerialName("total_supervisors") plus total_members
  - App location: `—`
  - To sync: Remove totalManagers (DashboardModels.kt:224-225) and totalSupervisors (226-227) from TeamStatsDto — backend never sends them, so they silently stay 0 and any UI showing manager/supervisor counts is always wrong. Keep only @SerialName("total_members").
- **🟠 MED · response_field_renamed** — GET /dashboard/pending-payments -> data (PendingPaymentsDataDto wrapper)
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: application/report/usecase_port.go:9-16 PendingPaymentsResult tags: payments, total_pending (float64), count (int64), page, per_page, total_pages
  - App now: DashboardModels.kt:339-351 PendingPaymentsDataDto expects: payments, total_pending, total_count, page, per_page, total_pages
  - App location: `—`
  - To sync: Backend wrapper emits count, NOT total_count. Rename PendingPaymentsDataDto.totalCount @SerialName("total_count") -> @SerialName("count") (DashboardModels.kt:344-345). Today totalCount stays 0 so the 'N pending' header/count is wrong. payments/total_pending/page/per_page/total_pages all match.
- **🟢 LOW · path** — GET /dashboard/vehicle-status, /dashboard/trips-status, /dashboard/drivers-status
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: routes/dashboard_routes.go:38-40 — backend exposes GET /dashboard/vehicle-status (VehicleStatusData), /dashboard/trips-status (TripsStatusData), /dashboard/drivers-status (DriversStatusData)
  - App now: ApiConfig.kt:60-61 explicitly omits these three ('not documented... omitted'); app has unused DTOs VehicleStatusSummaryDto/DriverStatusSummaryDto/TripSummaryDto (DashboardModels.kt:388-427) that don't match backend shapes anyway
  - App location: `—`
  - To sync: No app bug (endpoints simply not consumed). If/when wired, add endpoint consts and DTOs matching domain.VehicleStatusData/TripsStatusData/DriversStatusData (note: backend trips-status uses in_progress/planned/delayed/completed_today/completed_total/recent_trips, NOT the app's TripSummaryDto in_progr
- **🟢 LOW · response_field_added** — GET /dashboard -> today_summary (TodaySummaryDto)
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: domain/dashboard/entity.go:70-80 TodaySummary has total_fuel_filled (float64), total_fuel_used (float64), total_fuel_cost (float64)
  - App now: DashboardModels.kt:94-108 TodaySummaryDto has completed_trips_today/total_distance_today/fuel_consumption/active_vehicles_now/new_trips_today/alerts_count but is MISSING total_fuel_filled, total_fuel_used, total_fuel_cost
  - App location: `—`
  - To sync: Add to TodaySummaryDto: @SerialName("total_fuel_filled") val totalFuelFilled: Double = 0.0, @SerialName("total_fuel_used") val totalFuelUsed: Double = 0.0, @SerialName("total_fuel_cost") val totalFuelCost: Double = 0.0. App safely ignores them today (ignoreUnknownKeys=true).
- **🟢 LOW · request_field_retyped** — GET /dashboard/financial-summary?period= (query param values)
  - Impact: Request type mismatch → 400 or wrong value saved
  - Backend: report_handler.go:118 DefaultQuery("period","monthly"); period passed straight to usecase (accepts today/weekly/monthly/yearly)
  - App now: DashboardModels.kt:600-605 FinancialPeriod enum sends today/weekly/monthly/yearly via parameter("period", period.value)
  - App location: `—`
  - To sync: Param name and values align (today/weekly/monthly/yearly). No change. cost-overview filter (DashboardModels.kt:250-254 today/weekly/monthly) also aligns with report_handler.go:92 DefaultQuery("filter","today"). No change.
  - _Notes:_ Scope: app consumes 5 dashboard endpoints — GET /dashboard (DashboardHandler.GetUnifiedDashboard), /dashboard/cost-overview, /dashboard/financial-summary, /dashboard/pending-payments (all 3 served by ReportHandler), /dashboard/alerts-status (DashboardHandler.GetAlertsStatus). Backend also has /dashboard/owner|manager|supervisor and /dashboard/vehicle-status|trips-status|drivers-status which the ap

### user (auth + profile + permissions)  
_1 high · 4 medium · 2 low_

- **🔴 HIGH · response_field_renamed** — GET /profile (GetProfile) -> ProfileResponse.owner / UserProfileDto.ownerInfo
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: profile/dto.go ProfileResponse.Owner has json tag `owner` (type *OwnerInfoResponse). OwnerInfoResponse fields: `id` (uint), `email`, `mobile`, `first_name`, `last_name`.
  - App now: UserDto.kt UserProfileDto.ownerInfo @SerialName("owner_info") (line 139-140), type OwnerInfoDto whose fields are @SerialName("owner_id"), @SerialName("owner_name"), @SerialName("owner_email") (lines 145-152).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/user/UserDto.kt:139`
  - To sync: Change @SerialName("owner_info") to @SerialName("owner") at UserDto.kt:139, then rewrite OwnerInfoDto (lines 145-152) to backend tags: id @SerialName("id") Int, firstName @SerialName("first_name"), lastName @SerialName("last_name"), email @SerialName("email"), mobile @SerialName("mobile"); drop owne
- **🟠 MED · response_field_removed** — GET /profile -> OwnerStatsResponse / OwnerStatsDto
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: profile/dto.go OwnerStatsResponse + domain OwnerStats emit only: `total_team_members`, `total_vehicles`, `active_vehicles`, `total_trips`, `active_trips`, `completed_trips`. No total_managers / total_supervisors.
  - App now: UserDto.kt OwnerStatsDto declares @SerialName("total_managers") (lines 99-100) and @SerialName("total_supervisors") (lines 101-102) on top of the real fields.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/user/UserDto.kt:99-102`
  - To sync: Remove the two phantom fields totalManagers/totalSupervisors from OwnerStatsDto (UserDto.kt:99-102); they always default to 0. The remaining six fields match the backend. Update any ProfileViewModel UI reading them.
- **🟠 MED · response_field_removed** — GET /profile -> ProfileResponse (identity/verification/IAM fields ignored by app)
  - Impact: App expects a dropped field → BLANK / 0
  - Backend: profile/dto.go ProfileResponse also returns: `tenant_id`, `roles` ([]string), `status`, `email_verified` (bool always present), `mobile_verified` (bool always present), `avatar_url`, `job_title`, `department`, `date_of_birth` (epoch-millis), `gender`, `bio`, `timezone`, `language`, `address_line_1`,
  - App now: UserDto.kt UserProfileDto (lines 119-141) only has id, email, mobile, first_name, last_name, role, owner_id, is_active, created_at, updated_at, owner_stats, owner_info. @JsonIgnoreUnknownKeys silently drops the rest.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/user/UserDto.kt:119`
  - To sync: Add the missing fields to UserProfileDto (UserDto.kt:119): at minimum tenantId @SerialName("tenant_id"), roles @SerialName("roles") List<String>=emptyList(), emailVerified @SerialName("email_verified") Boolean=false, mobileVerified @SerialName("mobile_verified") Boolean=false, permissions @SerialNam
- **🟠 MED · response_field_retyped** — PUT /profile (UpdateProfile) response shape
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: ProfileHandler.UpdateProfile (profile_handler.go:71-94) returns the SAME composite app.ProfileResponse (full profile incl. roles/permissions/owner/email_verified). It does NOT return the slim user.UserResponse.
  - App now: UserRemoteDataSource.updateProfile (UserRemoteDataSource.kt:161-174) decodes UserApiResponse -> UserDto (slim user: id,email,mobile,first_name,last_name,role,owner_id,created_by_id,tenant_id,is_active,created_at,updated_at).
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/datasource/user/UserRemoteDataSource.kt:161`
  - To sync: UpdateProfile returns ProfileResponse, not UserDto. UserDto is a subset so it won't crash, but roles/permissions/verification/owner from the update echo are dropped. Change updateProfile (interface line 62 + impl 161-174 + handleUserResponse) to decode ProfileApiResponse -> UserProfileDto (after Use
- **🟠 MED · response_field_retyped** — POST /auth/login/otp/verify (VerifyLoginOTP) response
  - Impact: Type mismatch (e.g. array-vs-object, int-vs-float) → PARSE CRASH (whole response empty)
  - Backend: user_handler.go VerifyLoginOTP returns data = { access_token, refresh_token, token_type:"Bearer", expires_in } (lines 265-270). No `user` object, no `token` key.
  - App now: verifyLoginOtp (UserRemoteDataSource.kt:248-279) reads data.access_token then synthesizes AuthResponseDto with a fabricated UserDto(email="", mobile=request.mobile, role="owner").
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/datasource/user/UserRemoteDataSource.kt:270`
  - To sync: App extracts access_token correctly but fabricates a fake user with hardcoded role="owner" (UserRemoteDataSource.kt:270-273). After OTP login the app must fetch GET /profile (or /me/permissions) for the real user/role instead of assuming owner. Drop the hardcoded role; treat the synthetic user as pl
- **🟢 LOW · request_field_added_required** — POST /auth/signup (SignUp) request body
  - Impact: Backend now REQUIRES this → request rejected (400), action fails
  - Backend: user/dto.go SignUpRequest: email `omitempty,email`, mobile `omitempty,min=10,max=15`, password `required,password`, first_name `required`, last_name `required`. password must satisfy active password policy.
  - App now: UserDto.kt SignUpRequest (lines 11-20): email/mobile/password/firstName/lastName all non-null String. Tags match (first_name/last_name). App always sends email + mobile.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/user/UserDto.kt:11`
  - To sync: Tags align, no rename. Backend treats email/mobile as optional (omitempty) while app makes them mandatory non-null — acceptable since app sends both. No change required unless signup with only one identifier should be allowed.
- **🟢 LOW · response_field_added** — POST /auth/login & /auth/signup -> UserResponse (data.user) fields app ignores
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: user/dto.go UserResponse (inside data.user) also includes `iam_id`, `roles` ([]string), `is_caretaker_eligible` (bool) beyond id/email/mobile/first_name/last_name/role/owner_id/created_by_id/tenant_id/is_active/created_at/updated_at.
  - App now: UserDto.kt UserDto (lines 64-86) omits iam_id, roles, is_caretaker_eligible; @JsonIgnoreUnknownKeys silently drops them.
  - App location: `/Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/data/model/user/UserDto.kt:86`
  - To sync: Additive — safe to ignore for parsing. If the app needs the full IAM role set or caretaker-eligibility from auth/login, add roles @SerialName("roles") List<String>=emptyList(), isCaretakerEligible @SerialName("is_caretaker_eligible") Boolean=false, optionally iamId @SerialName("iam_id") to UserDto:8
  - _Notes:_ Endpoints all match on method+path (ApiConfig.kt vs auth_routes.go/profile_routes.go). PROFILE=\"/profile\"; backend also exposes GET /me as an alias for GetProfile (same handler) but app only uses /profile — fine. ME_PERMISSIONS=\"/me/permissions\" matches; MyPermissionsResponse correctly reads data.permissions (PermissionDto.kt). TEAM_ROLES=\"/team/roles\" matches TeamRolesResponse.

Highest-imp

### team  
_0 high · 0 medium · 7 low_

- **🟢 LOW · path** — ApiConfig.Endpoints.TEAM / teamMemberById (stale endpoint constants)
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: routes are /team/members (POST,GET), /team/members/:id (GET,PUT,PATCH toggle-active, PATCH change-role, POST reset-password, DELETE) — team_routes.go:45-61. Member collection path is /team/members, single member is /team/members/{id}.
  - App now: ApiConfig.kt:138-139 declares `const val TEAM = "/team"` and `fun teamMemberById(memberId) = "$TEAM/$memberId"` => "/team/{id}". These omit the `/members` segment and would hit the wrong path. NOTE: screen-team's TeamRemoteDataSourceImpl does NOT use these constants — it uses its own local `TEAM_MEM
  - App location: `—`
  - To sync: Fix the shared constants to match backend: in /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/core/network/ApiConfig.kt:138-139 change to `const val TEAM_MEMBERS = "/team/members"` and `fun teamMemberById(memberId: String) = "$TEAM_MEMBE
- **🟢 LOW · response_field_added** — TeamMemberDto (response: create/get/list/update/toggle/etc. `data` object) — backend UserResponse
  - Impact: Backend sends extra data app ignores → no break (additive)
  - Backend: user/dto.go:119-139 UserResponse emits `roles []string json:"roles"` ALWAYS (no omitempty; mapper sets [] when nil, dto.go:184-187). This is the full IAM role set, distinct from the single `role` string. Also emits `iam_id` (omitempty) and `created_by_id` (omitempty).
  - App now: TeamDto.kt:77-98 TeamMemberDto has only the scalar `role: String` (@SerialName not needed, plain `role`). It has NO `roles` field. Class is annotated @JsonIgnoreUnknownKeys so the extra `roles`/`iam_id`/`created_by_id` are silently dropped — no parse failure.
  - App location: `—`
  - To sync: No required change — @JsonIgnoreUnknownKeys safely ignores `roles`, `iam_id`, `created_by_id`. If the UI ever needs the full IAM role set (e.g. multi-role badges), add `val roles: List<String> = emptyList()` to TeamMemberDto at /Users/ashwani/Upgrade/indusjs_fleet/IndusJSFleet_Apps/screen-team/src/c
- **🟢 LOW · request_field_retyped** — PUT /team/members/{id} — UpdateTeamMemberRequest.is_active
  - Impact: Request type mismatch → 400 or wrong value saved
  - Backend: user/dto.go:59 `IsActive *bool json:"is_active"` — pointer (nullable) bool, no binding required. Backend distinguishes "not sent" (nil, no change) from explicit true/false.
  - App now: TeamDto.kt:57-58 `@SerialName("is_active") val isActive: Boolean? = null`. Nullable Boolean with null default. With Json encodeDefaults=true (TeamRemoteDataSource.kt:62) a null value is still serialized as `"is_active": null`, which Go binds to a nil *bool — same as omitted. Type and optionality MAT
  - App location: `—`
  - To sync: No change needed. Kotlin Boolean? <-> Go *bool with json:"is_active" align (renamed tag matches @SerialName). Confirmed compatible; listed for completeness.
- **🟢 LOW · request_field_renamed** — POST /team/members + PUT /team/members/{id} — first_name/last_name fields
  - Impact: Request field name wrong → backend ignores it (data not saved)
  - Backend: CreateTeamMemberRequest (dto.go:42-49) and UpdateTeamMemberRequest (dto.go:52-60) use json:"first_name", json:"last_name", json:"email", json:"mobile", json:"password", json:"role", json:"is_active". Create requires password (binding:"required,password"), first_name, last_name, role (binding:"requir
  - App now: CreateTeamMemberRequest (TeamDto.kt:14-24): @SerialName("first_name") firstName, @SerialName("last_name") lastName, plus plain email/mobile/password/role — all non-null required Strings. UpdateTeamMemberRequest (TeamDto.kt:48-60): same @SerialName mapping, all nullable. Tags MATCH backend json tags 
  - App location: `—`
  - To sync: No change — @SerialName("first_name")/@SerialName("last_name") match backend json:"first_name"/"last_name". Create sends password (backend-required) and role (backend-required). Contract aligned.
- **🟢 LOW · response_field_renamed** — GET /team/members (list) — response envelope data wrapper
  - Impact: App reads a renamed/absent field → shows BLANK / 0
  - Backend: ListTeamMembers handler (user_handler.go:533-536) responds RespondSuccess(... gin.H{"team": result.Team, "count": result.Count}). So `data` = { "team": [...UserResponse], "count": int }.
  - App now: TeamListDataDto (TeamDto.kt:114-119): `val count: Int = 0` and `val team: List<TeamMemberDto>? = null`. Keys `team` and `count` MATCH the gin.H keys. Wrapped in TeamMemberListApiResponse {success,message,data}. Aligned.
  - App location: `—`
  - To sync: No change — list wrapper keys (`team`,`count`) match backend exactly.
- **🟢 LOW · other** — GET /team/roles — ListTeamRoles response
  - Impact: Other
  - Backend: ListTeamRoles handler (user_handler.go:488-494) responds data = gin.H{"roles": []string, "count": int}. roles are raw fleet role-catalog strings (e.g. owner/admin/user or general_manager/manager/supervisor depending on FLEET_ALLOWED_ROLES env).
  - App now: AssignableRolesDataDto (TeamDto.kt:28-31): `val roles: List<String> = emptyList()`, `val count: Int = 0` inside AssignableRolesApiResponse {success,message,data}. Keys MATCH. Endpoint constant TEAM_MEMBER_ROLES_ENDPOINT="/team/roles" (TeamRemoteDataSource.kt:67) and ApiConfig.TEAM_ROLES="/team/roles
  - App location: `—`
  - To sync: No change — /team/roles request and {roles,count} response shape align.
- **🟢 LOW · other** — Backend-only team endpoints the app does not call (change-role, invitations)
  - Impact: Other
  - Backend: team_routes.go exposes PATCH /team/members/:id/change-role (ChangeRoleRequest json:"new_role" required), POST /team/invitations (InviteTeamMemberRequest: email,first_name,last_name,role all required), GET /team/invitations, GET /team/invitations/:id, POST /team/invitations/:id/resend, DELETE /team/i
  - App now: TeamRemoteDataSource interface (TeamRemoteDataSource.kt:36-45) implements only: getAssignableTeamRoles, createTeamMember, getTeamMembers, getTeamMember, updateTeamMember, toggleTeamMemberActive, resetTeamMemberPassword, deleteTeamMember. NO change-role call (role changes go via updateTeamMember PUT 
  - App location: `—`
  - To sync: No divergence that breaks existing calls — these are unused backend features. If invitation-based onboarding is to be supported in the app, add DTOs (e.g. InviteTeamMemberRequest{email,first_name,last_name,role}, InvitationResponse with @SerialName for expires_at/invited_by/created_at) and call site
  - _Notes:_ Backend team routes are served by the FLEET backend (team_routes.go), not via an IAM proxy — the fleet UserHandler handles team CRUD directly against its use case (member create/list/get/update/toggle/reset-pw/delete persist in fleet, while invitations and auth delegate to IAM). The note in the task ("team mgmt moved to IAM") is NOT reflected here: fleet still owns /team/members/* and /team/roles.

### cross-cutting  
_0 high · 1 medium · 5 low_

- **🟠 MED · error_envelope** — Error envelope shape (fields app parses)
  - Impact: Error message parsing drift → wrong/raw error text shown
  - Backend: respond/respond.go:18 + handler/response.go:27 — all errors -> {errorCode("INDUSJS-FLEET-<CODE>"), errorMessage, developerMessage(omitempty), serviceCode("INDUSJS-FLEET"), path, statusCode, timestamp(ms), fields(map,omitempty)}. No top-level message/error/detail key ever sent. For domain errors erro
  - App now: ApiErrorHandler.kt:78 tryExtractJsonMessage probes in order: `message`(never sent), `developerMessage`, `errorMessage` (SKIPPED if it contains '_' — i.e. drops the i18n keys), `error`(never sent), `detail`(never sent). No @Serializable error DTO; loose JsonObject parse + HTTP status fallback. `field
  - App location: `IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/core/network/ApiErrorHandler.kt:78`
  - To sync: Works today because developerMessage is checked before the underscored errorMessage is rejected. But fragile: 3 of 5 probes (message/error/detail) never match this backend; if a handler omits developerMessage and errorMessage is an i18n key, app drops it and shows a generic fallback; per-field valid
- **🟢 LOW · path** — Base path / API version
  - Impact: Wrong URL → 404, feature unreachable
  - Backend: routes/routes.go:28 — all v1 routes under router.Group("/api/v1"); APIVersionMiddleware enforces v1. Dev listen port default 8081 (config/config.go ServerPort = PORT||SERVER_PORT||8081; developer.env=8081, staging.env=8081, production.env=8080, Cloud Run injects PORT).
  - App now: ApiConfig.kt:16 BASE_URL = "http://10.0.2.2:8081/api/v1" (active, local emulator). Commented Cloud Run URL also ends /api/v1.
  - App location: `IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/core/network/ApiConfig.kt:16`
  - To sync: Base path /api/v1 matches; dev port 8081 matches. No code change. Note active BASE_URL is local-only — switch to the Cloud Run line before release.
- **🟢 LOW · auth** — Auth header + token claims (tid)
  - Impact: Auth/token drift
  - Backend: middlewares/auth.go:34 — requires `Authorization: Bearer <token>` (case-insensitive "bearer"); empty/!=2 parts -> 401. ValidateToken decodes JWT; tenant from token (tid -> localUser.TenantID). IAM unavailable/timeout -> 503.
  - App now: Each *RemoteDataSource sets header(HttpHeaders.Authorization, "Bearer $token"). JwtHelper.kt:39 reads `tid` (extractTenantId/hasTenantContext) and global_roles||roles. Header is added per-request, not in HttpClientProvider.defaultRequest.
  - App location: `IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/core/auth/JwtHelper.kt:39`
  - To sync: Header scheme + tid claim match. No change. Auth header attached per-datasource (not centrally) — easy to forget on a new datasource; consider centralizing.
- **🟢 LOW · other** — Success envelope shape
  - Impact: Other
  - Backend: respond/respond.go:49 respond.Success -> {"success":true,"message":<string>,"data":<payload>}.
  - App now: Per-feature wrappers (DriverApiResponse DriverDto.kt:116, TripCost*ApiResponse, CostApiResponses, etc.) decode success:Boolean, message:String?, data:T?. ignoreUnknownKeys=true.
  - App location: `IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/model/DriverDto.kt:116`
  - To sync: Matches. No shared ApiResponse<T> exists — each screen module redefines its own {success,message,data} wrapper. Works but duplicated; a single shared wrapper in ijs-network-lib would reduce drift.
- **🟢 LOW · other** — Pagination/list wrapper
  - Impact: Other
  - Backend: respond/respond.go:58 respond.Paginated -> data:{items:[...], count:<total>, page, per_page, total_pages, has_more, next_page:*int|null}. total is emitted as `count`. Query: page, per_page (default 10, max 100).
  - App now: DriverListDataDto (DriverDto.kt:130) + cost/history/state DTOs decode items, count, page, per_page, total_pages, has_more, next_page (Int? nullable).
  - App location: `IndusJSFleet_Apps/screen-driver/src/commonMain/kotlin/com/ijs/driver/data/model/DriverDto.kt:130`
  - To sync: Pagination fields match exactly (count=total, nullable next_page handled). DriverApiResponse also declares a sibling `pagination` field the backend never sends (paging is nested in data) — dead/always-null but harmless; drivers read paging from the data object.
- **🟢 LOW · auth** — 401 handling / session-neutral pre-onboarding
  - Impact: Auth/token drift
  - Backend: middlewares/auth.go:50 — 401 (respond.Error envelope) for missing/invalid token; 503 for IAM unavailable. RequireAuthenticated (handler/response.go) allows not-yet-onboarded users on authn-only routes (GET /me/permissions); tenant-scoped data routes 401 when token lacks tenant.
  - App now: HttpClientProvider.kt:183 validateResponse: on 401 with Bearer -> emitSessionExpired UNLESS isSessionNeutral401 (path endsWith /me/permissions OR token has no tid). Silent refresh POST /auth/refresh expects {success:true,data:{token}}.
  - App location: `IndusJSFleet_Apps/ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/core/network/HttpClientProvider.kt:183`
  - To sync: Matches contract (401 -> re-login; pre-onboarding no-tid tolerated). 503 (IAM down) falls into generic ApiErrorHandler — acceptable. Confirm /auth/refresh response shape {success,data.token} against the actual refresh handler if refresh is exercised.
  - _Notes:_ Read-only cross-cutting audit; no files changed. The backend↔apps contract is CONSISTENT on all load-bearing pieces: base path /api/v1 matches; success envelope {success,message,data} matches; paginated data {items,count,page,per_page,total_pages,has_more,next_page} matches field-for-field (total is emitted as `count`, next_page nullable — both handled app-side); auth is `Authorization: Bearer <jw
---

# Whole-feature breakage (not field-level)

### 1. `screen-finance` — entire feature has NO backend (13 endpoints → 404)
Vehicle-purchase, loan-summary, loan-payments, EMI alerts/upcoming/overdue, pay-EMI — **none exist** in the fleet
backend. `VehicleFinanceRemoteDataSource.kt` + `VehicleFinanceDto.kt` + `ApiConfig.kt` (`VEHICLE_PURCHASE`,
`vehicleLoanPayments*`, `VEHICLE_LOAN_PAYMENTS*`). `getPurchase` swallows 404 (silent empty); the other 12 surface as
errors. **Cannot be "synced" — there is no target.** Resolution is a product/backend decision: gate the feature off in
the app **or** the backend builds the purchase/loan API. *(Direction: skip for now.)*

### 2. Silent token refresh is dead (forced re-login on expiry)
`HttpClientProvider.kt:135-227` calls a fleet `/auth/refresh` route that **does not exist** (always 404) and sends the
wrong credential (old access token instead of `refresh_token`). Net effect today: when the access token expires the user
is bounced to login. Resolution needs a backend decision (fleet → IAM refresh proxy). *(Direction: needs backend — noted.)*

---

# Product decisions (backend dropped data the UI still shows — confirm intent)
These are **not** rename bugs; the backend genuinely stopped sending them. Each needs a product call (compute client-side,
add a data source, or remove the UI element):

- **Vehicle:** `last_location`, `fuel_level`, `last_service_date`, `next_service_date`, `assigned_driver_name` are never
  sent (`VehicleDto.kt:28-59`). Live location is MQTT→WebSocket (separate from REST); service dates have no backend
  source. → Decide: remove these UI elements or give them a data source. (`assigned_driver_name` IS derivable from
  `assigned_driver.first_name/last_name`.)
- **Fleet P&L KPIs:** `total_expenses`, `gross_profit`, `profit_margin`, `profitable_vehicles`, `loss_making_vehicles`
  (`VehiclePLDto.kt:137-170`) — `FleetTotal` doesn't carry them. → Compute client-side from `total_cost`/`net_profit`,
  drop the cards, or request backend add them.
- **Trip-payment inline summary:** per-trip paid/pending/count and the list's inline summary are no longer sent
  (`TripPaymentDto.kt:194,235-246`). → Keep the card (call `/trip-payments/summary` separately) or drop it.
- **Backend RBAC zeros (intended, NOT a bug):** for manager/supervisor roles the trip-payment backend masks
  amount/TDS/discount/net to 0. The app is unaware and shows 0. → Confirm the app's permission gating reflects this so
  users don't see confusing zeros.
- **Error-message UX:** backend puts i18n **keys** in `errorMessage` for domain errors (human string only for
  auth/param errors); there's no top-level user-facing `message`. `ApiErrorHandler.kt:78-116` can leak `developerMessage`
  / show raw keys. → Decide contract: backend emits localized text, or the app resolves i18n-key→text.
- **Create-driver "link existing IAM user":** `CreateDriverRequest` has no `iam_user_id` field, so that flow is
  impossible in the app. → Decide if the flow is required (add field) or not (no action).

---

# Cross-cutting
- ✅ **Confirmed consistent (no action):** base path/version `/api/v1`, success envelope, `Authorization: Bearer`,
  tenant `tid` claim, 401-vs-403 handling.
- 🟠 **Error envelope:** typed error DTO (`errorCode`/`errorMessage`/`developerMessage`/`fields`) would stop leaking Go
  developer text and let the app resolve i18n keys (`ApiErrorHandler.kt:78-116`).
- 🟢 **Pagination:** introduce one shared `PaginatedData<T>{items,count,page,per_page,total_pages,has_more}` in
  `ijs-network-lib` so future list endpoints don't silently break the ad-hoc per-feature parsers.
- 🟢 **Auth token field split:** OTP-verify returns `data.access_token`; login/signup return `data.token`. App handles
  both (`UserDto.kt:228-236`); latent, not breaking.

---

# Verification gaps (confirm from backend source before any future fix)
- **trip-payment TDS** row DTO keys — read `internal/infrastructure/.../trippayment` `GetTdsReport` for exact map keys
  before defining the `List<...>` row.
- **trip bulk-cost** `date` — confirm `TripCostEntryViewModel` (~397-420) always populates it before making it required.
- **vehicle `/with-documents`** multipart part-names in `helpers.ProcessVehicleDocuments`
  (`registration_certificate`/`insurance`/`puc_certificate`/`fitness_certificate`/`road_tax`/`permit` + `*_expiry`).
- **`GET /vehicles/:id/trips`** — verify the route exists in `vehicle_routes.go` (else `getTripsByVehicleId` 404s).

---

# How to use this report
Fixes are deferred by direction. When resumed, address in this order (most user-visible breakage first):
**report → dashboard → trip-payment → trip → vehicle → team → customer → driver**, then cross-cutting; **finance** and
**token refresh** are blocked on backend/product decisions. Each finding above carries the apps `file:line` and the exact
sync to apply. Re-verify each against current backend source at fix time (backend may move again).

_Generated from a read-only contract diff (11 domain audits + cross-cutting + synthesis). No `IndusJSFleet_Apps` source
or any backend file was modified to produce this report._
