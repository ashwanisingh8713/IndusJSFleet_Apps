# Plan — Trip cost toast shows "0 cost(s) saved successfully"

## Problem
After adding a trip cost, the success toast reads "0 cost(s) saved successfully" instead of the actual number.

## Root-cause analysis (confirmed against code + backend contract)
- Toast string: `trip_cost_batch_saved` = "%1$d cost(s) saved successfully"; count comes from `Effect.CostsSaved(count)`.
- `TripCostEntryViewModel` emits `CostsSaved(result.data)` where `result` = `CostsRepository.bulkCreateTripCosts(...)`.
- `CostsRepositoryImpl.bulkCreateTripCosts`: `Result.Success(response.data?.created ?: request.costs.size)`.
- `response.data` is `BulkCostsResultDto` with `@SerialName("created") val created: Int = 0` (shared by trip + maintenance bulk).
- **Backend** (`internal/application/tripcost/dto.go` `BulkCreateResponse`) returns the count as JSON **`created_count`**, not `created`. So the client field never deserializes, stays at its default **0**, and the repo's `?:` fallback (only triggers on null, not 0) does not kick in. ⇒ toast shows 0.
- Cross-check: driver-cost bulk DTO already uses `@SerialName("created_count")` (correct) — trip/maintenance were inconsistent.

## Approach (chosen): align the client DTO with the backend contract
- Change `BulkCostsResultDto.created` `@SerialName` from `"created"` → `"created_count"`. One-line fix that corrects the count for BOTH trip and maintenance bulk creates (shared DTO).
- Alternatives considered: (a) repo `takeIf { it > 0 } ?: request.size` — masks a genuine partial-result 0 and treats the symptom, not the contract mismatch; rejected as the primary fix. The existing `?: request.costs.size` null-guard stays as a safety net.

## Affected
- `ijs-core-lib`: `CostApiResponses.kt` (`BulkCostsResultDto`). No API change; this is a client deserialization fix. No backend change needed (backend is correct).

## Best practices / patterns
- DTO `@SerialName` must mirror the backend JSON exactly (project rule). Single shared DTO = single fix point (reuse).

## Edge cases
- Partial success: backend `created_count` < submitted → toast now shows the true created count.
- `data` null (failure) → `?:` falls back to submitted size (unchanged); only reached on Result.Success anyway.

## Verification
- `:androidApp:assembleDebug`; on adding N costs, toast shows N. (E2E session D to confirm on-device.)

## Future enhancements
- Enrich `BulkCostsResultDto` with `error_count`/`errors` (backend already returns them) to surface partial failures in the UI.
