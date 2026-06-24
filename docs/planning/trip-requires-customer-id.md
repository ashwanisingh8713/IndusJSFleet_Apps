# Plan — Trip requires customer_id (A's CONTRACT_CHANGE)

## 1. Trigger
Backend `CONTRACT_CHANGE` (bus/apps.inbox 1782090365): every trip now REQUIRES `customer_id`.
Backend planning doc: `IndusJSFleet_GoLang_Backend/docs/planning/trip-requires-customer.md`.
- `POST /trips`: `customer_id` (uint) REQUIRED; free-text `customer_name/contact/email/address` REMOVED from request (server snapshots from the customer record). New errors: 404 (unknown/wrong-tenant customer), 409 (inactive customer).
- `PUT /trips/:id`: `customer_id` optional (non-zero ⇒ reassign + re-snapshot; never clears). Same free-text removal + 404/409.
- `TripResponse`: adds `customer_id`; `customer_*` remain but are now the server snapshot (authoritative display).

## 2. Reconciliation with prior B-1 work
- D's earlier B-1 ("make customer optional") is **superseded** — backend now mandates customer. The app already gates the Create-Trip button on a selected customer (`CreateTripContract.isFormValid` → `isCustomerValid = selectedCustomer != null`), so "required" is already enforced.
- The legit remaining B-1 concern (fresh tenant soft-lock) is mitigated by the API auto-load of customers I added earlier (`refreshCustomersFromApi(silent=true)` on open) + the empty-state "Add New Customer". Keep that.
- Net: do NOT pursue "optional"; align everything to "customer required".

## 3. Current state (verified in code)
- `CreateTripRequest`/`UpdateTripRequest`/`TripDto` already have `customer_id` (`customerId: Int?`). Response mapping already reads it.
- `CreateTripViewModel.createTrip` currently sends BOTH `customerId` and free-text `customerName/customerContact` (snapshot of the selected customer). Per the contract, the free-text fields must NOT be sent.

## 4. Approach (chosen)
1. **Create request:** send `customer_id` only. In `createTrip`, set `customerId = selectedCustomer.id`; set `customerName = null`, `customerContact = null` (with `explicitNulls=false` they're omitted from JSON → satisfies "do not send"). Require a selected customer defensively (return a clear error if somehow null).
2. **Validation coherence:** restore "customer required" in `validateForm` (reverse the earlier "optional" edit) so the submit path matches the button gate and the new contract.
3. **Error mapping:** in the create `Result.Error` branch, surface a clear customer-specific message when the failure is about the customer (not found / inactive) — fall back to the backend message otherwise.
4. **Response:** `customer_id` already mapped (`TripDto.customerId`); snapshot `customer_*` kept for display. No change.
5. **Update path:** trip edit (`screen-trip/.../detail`) — if it sends free-text customer fields, null them too; customer reassign via `customer_id` is optional. Verify and adjust only if it currently sends free-text.

Alternative considered: physically delete `customerName/customerContact` from the request DTOs + `CreateTripData` + mapper (per "no backward-compat shims"). Rejected for now as higher-ripple/risk; nulling them fully satisfies the wire contract. Can prune later.

## 5. Affected
- `screen-trip`: `CreateTripViewModel` (request build + validation + error mapping). Possibly trip-detail edit VM for the update path. No response-DTO change (already has `customer_id`).
- No new strings unless adding a customer error message (EN+HI if so).

## 6. Edge cases
- No customers in tenant → button stays disabled (correct); empty state routes to Add Customer; list auto-loads from API so existing customers appear.
- Selected customer became inactive server-side → 409 → inline "Select a valid, active customer".
- Picker selection cleared → button disabled.

## 7. Side benefit (from A)
Trips were never linked to customers, so customer stats/financials returned zeros. With `customer_id` now sent, `GET /customers/:id/statistics|trips|financial-report` populate — the customer screens that looked empty will show real data.

## 8. Verification
- `:androidApp:assembleDebug`; create a trip with a selected customer → 201 with `customer_id` + snapshot; attempt without → button disabled. D re-verifies on-device + checks customer stats now populate.

## 9. Also this turn (D inbox triage)
- D RETEST: B-2/3/4/5/6 PASS. B-1 reframed by this contract change (above).
- D REQUEST (relay 2 fleet bugs to A): both already fixed per A's FYI (`/trips/{id}/costs` 500→200; driver missing-email 500→400) — no relay needed; acknowledge.
- Reports: PARKED per user — do not touch.
