# Plan — Sweep & fix pre-existing hardcoded user-facing strings (i18n)

CLAUDE.md hard rule: every user-facing string must be in `Res.string.*` with **English + Hindi**. This sweep
finds and fixes the pre-existing hardcoded literals across the `screen-*` modules.

## Scope — what counts
**IN (must localize):** visible UI text in `@Composable` scope — `Text("…")`, `label`/`title`/`placeholder`/
`supportingText`/button labels, dialog titles/messages, snackbar/toast text, tab labels, non-null
`contentDescription` that's read to the user.

**OUT (do NOT touch):**
- Non-user-facing: log messages, `SerialName`/JSON keys, route names, test tags, analytics keys.
- Non-translatable: currency symbol `₹`, bullets `•`, pure emoji, format patterns (`"%.2f"`, `"DD-MM-YYYY"`),
  numbers, single punctuation.
- Strings already from `Res.string.*`.
- Literals in **non-`@Composable`** context (ViewModels, data, mappers) — `stringResource` can't be called
  there; FLAG these separately (they need plumbing, not a one-line swap) — do not hack them.

## Approach (Ultracode, workflow-driven)
1. **Discover** (read-only fan-out, 1 agent/module): list every IN-scope literal with file:line, the text, UI
   context, a proposed key (reuse an existing key if the exact text already exists — agents grep the EN
   strings.xml first), and an EN + **HI** value. Returns structured rows.
2. **Synthesize/dedup**: merge identical literals to one shared key; finalize key names + EN/HI; separate
   "reuse existing key" from "new key"; list the non-composable FLAGGED ones.
3. **Add strings (single writer = me):** append all NEW keys to `values/strings.xml` + `values-hi/strings.xml`
   in one coordinated batch (strings.xml is shared — must NOT be edited by parallel agents).
4. **Apply code edits (fan-out, 1 agent/module):** replace each literal with `stringResource(Res.string.key)`
   (+ imports). Per-module files only → no conflicts. No strings.xml edits by these agents.
5. **Build** `:androidApp:assembleDebug`; fix.
6. **Cross-check everything** (separate adversarial pass): verify the whole session's changes
   (consolidation + i18n) — no regressions, keys exist in BOTH languages, no literal left in scope.

## Risks
- `stringResource` is `@Composable`-only → non-composable literals are flagged, not force-converted.
- Hindi quality: produced by the model; consistent with the app's existing HI set. Acceptable.
- Volume may be large; apply per-module, build incrementally if needed.
- Don't localize the same text under two keys — dedup in step 2.

## Result (executed — all green, androidApp:assembleDebug)
- Discovery (10 read-only agents) found 171 new + 67 reuse composable-layer literals + 74 non-composable.
- After dedup + existence-check: **65 genuinely-new keys added (EN + HI)**; 109 "proposed-new" collided with
  existing keys (safe reuse — values verified to match, mostly ellipsis/case/param-form differences).
- **243 composable-layer literals localized** via per-module fan-out (8 agents); 5 correctly skipped
  (coroutine scope / emoji-only). 1 orphan key removed.
- Cross-check (10 adversarial reviewers): verdict "safe modulo 3 HIGH". Fixed: trip `"Occupied: "` →
  `trip_edit_occupied_window`; vehicle `"Loading drivers…"` → existing `vehicle_edit_loading_drivers`;
  report hero giant `"Report"` word → `📊` emoji; customer-PL margin mislabel (`customer_pl_net` →
  new `customer_pl_margin`); 5 trip-payment `typeDisplay`/`displayName` → `localizedDisplayName()`
  (fixed mixed English/Hindi on the payment screen). Caught + fixed a duplicate-key build error en route.
- Mechanical checks: 0 duplicate keys, full EN/HI parity for added keys, no parameterized key called
  without args.

## Phase 2 — remaining (NOT done; needs the same localized-extension / UiText pattern)
All have the same root cause: user-facing text in NON-composable code, so `stringResource` can't be used
at the literal site. ~80 sites:
- **Customer enum/domain cluster (~6):** `CustomerDetailTab.title`, `TripStateFilter.displayName`,
  and `Customer.kt` getters (`PaymentMode`, `FinancialPeriod`, `stateDisplay`, `routeDisplay`,
  `tripDisplay`, `modeDisplay`). Fix pattern: add `@Composable localizedX()` extensions (mirror
  `screen-trip-payment/PaymentLocalizedLabels.kt`) and call them at the composable render sites.
- **ViewModel-layer strings (~74):** validation / snackbar / fallback-error messages emitted from
  ViewModels. Need a `UiText` (or resource-id-in-Effect) plumbing change so the UI layer resolves
  `stringResource`. Touches MVI Effect/state across ~9 ViewModels — an architectural decision.
- **LOW (pre-existing):** English-only pluralization `"s"` hack (`team_members_count`,
  `vehicle_pl_results_count`/`_fleet_count`); enum `.label` vs `localizedLabel()` in report; fragile
  manual `%1$d` replace in `TripCostEntryScreen`; finance dead "Export PDF" button; vehicle chevron
  rotation; minor label-reuse nits.
