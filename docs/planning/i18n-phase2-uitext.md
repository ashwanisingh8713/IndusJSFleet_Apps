# Plan — i18n Phase 2: localize non-composable user-facing strings

Follows [i18n-hardcoded-string-sweep.md]. Phase 1 localized 243 composable-layer literals. Phase 2 targets
the ~80 user-facing strings that live in NON-composable code (`stringResource` can't be called there).

## Two workstreams

### A. Customer enum/domain cluster (~6) — achievable now, low risk
`CustomerDetailTab.title`, `TripStateFilter.displayName`, and `Customer.kt` getters (`PaymentMode`,
`FinancialPeriod`, `stateDisplay`, `routeDisplay`, `tripDisplay`, `modeDisplay`) are *rendered in
composables*. Fix: add `@Composable localizedX()` extensions (mirror
`screen-trip-payment/PaymentLocalizedLabels.kt`) and swap at the render sites. Reuse existing keys
(`filter_all`, `trip_state_*`, payment-mode keys) where present; add only what's missing (EN+HI).

### B. ViewModel-layer strings (~74) — architectural (`UiText`)
ViewModels emit user-facing text as raw `String` in Effects (e.g. `Effect.ShowSnackbar(message)`) and in
`state.error`/validation fields. `stringResource` is `@Composable`-only, so the resolution must move to the
UI layer.

**Approach — `UiText` abstraction (industry-standard for this exact problem):**
```
sealed interface UiText {
    data class Dynamic(val value: String) : UiText           // backend/echo strings, pass-through
    data class Resource(val id: StringResource, val args: List<Any> = emptyList()) : UiText
    @Composable fun asString(): String                        // resolves Resource via stringResource
    // + a non-composable resolver if needed for places without composition
}
```
- ViewModels build `UiText.Resource(Res.string.x, args)` for app messages, `UiText.Dynamic(backendMsg)` for
  pass-through. No `@Composable` needed at construction (StringResource is a plain data handle).
- Screens resolve with `effect.message.asString()` / `state.error?.asString()`.
- Keeps ViewModels free of `@Composable`; only the resolver is `@Composable`.

**Open questions to resolve via investigation BEFORE coding:**
1. Where can `UiText` live so both ViewModels (data) and screens (resolver) see it, WITHOUT making
   ViewModels depend on a UI lib? Candidates: `ijs-core-lib` (if it can see
   `org.jetbrains.compose.resources.StringResource`) for the data type + the `@Composable` resolver in
   `ijs-ui-components-lib`; or both in `ijs-ui-components-lib`. Need each module's gradle deps + whether
   core has compose-resources on its classpath.
2. Exact per-module Effect/State shapes that carry user-facing strings (field names, how the screen shows
   them — snackbar host vs ErrorContent vs inline). Need the real list per ViewModel.
3. Which strings are genuinely client app-messages (→ Resource) vs backend echoes (→ Dynamic / leave).
4. Interaction with existing `ErrorContent(error: String, screenContext)` + `toErrorInfo` — error flow may
   already map raw → friendly; confirm we don't double-handle.

## Execution order
1. Investigate (read-only fan-out): MVI base + module deps + per-ViewModel string inventory + consumption.
2. Finalize `UiText` location/shape from findings.
3. Implement `UiText` + resolver; migrate per module (Effect/State + emission sites + screen resolution).
   Add missing keys (EN+HI). Build per module.
4. Part A customer cluster.
5. Cross-check (adversarial) + final green build.

## Risk
- Touches MVI Effect/State contracts across ~9 modules — do per-module, build incrementally.
- Don't change navigation-via-callback or non-message Effects. Only message-carrying fields.
- Backend error messages stay pass-through (`Dynamic`) — don't force-localize server text.

## Result (executed — all green, androidApp:assembleDebug)
- **Key correction:** `UiText` already existed (`ijs-ui-components-lib/.../components/UiText.kt`: `StringRes`/
  `Raw`/`@Composable resolve()`), adopted by team + trip-payment. Extended it — did NOT fork a new type.
- **~292 strings localized** to `UiText` across 7 modules (230 in the migration fan-out + 62 fallback
  literals wired to `StringRes` once keys were added). State `error: String? → UiText?`, Effect
  `message: String → UiText` (driver/customer ripple landed clean), validation/success → `StringRes`,
  backend `?:` fallbacks → `StringRes`, ~25 pure server echoes kept as `Raw`.
- **Enum/domain labels → `@Composable` resolvers:** `FinanceLocalizedLabels`, `VehicleLocalizedLabels`,
  `CustomerLocalizedLabels` (+ existing Team/Payment). Customer Financials tab + EMI fallback wired.
- **106 new keys** added (EN+HI, existence-checked); fixed the driver status-token bug (pass display
  label, not API token); deleted dead getters/scope vars.
- **Adversarial cross-check:** 0 HIGH. Fixed 3 report MEDIUMs (2× un-remembered `SnackbarHostState`,
  1 double-snackbar overwrite on export). 3 orphan keys resolved (1 wired, 2 non-composable removed,
  1 export key removed with its dup effect). Final: 0 orphans, full EN/HI parity for added keys.
- **Pre-existing, untouched:** the EN/HI string file has a ~40-key HI gap for OLDER keys (not from this
  work; missing HI falls back to EN). The single-slot snackbar pattern suppresses identical back-to-back
  messages (codebase-wide convention). Customer PDF-export literals + report period-label strings live in
  non-composable helpers (date calculator / exporter) — a future `UiText`-in-helper pass if needed.
