# Plan — Complete component consolidation (#13/#14) + exhaustive sweep

Follows [component-duplication-audit.md]. Foundation already shipped & green: `FleetSectionCard`,
`FleetTitledSectionCard`, `FleetSectionHeader`, `FleetMetricTile`, `FleetAccentIconChip`, `FleetAvatar`,
`initialsOf` (all in `ijs-ui-components-lib`); validation→`ValidationUtils` and formatting→`TimeUtils`
done; dashboard + profile migrated.

## Goal
Finish #13 (section cards) + #14 (metric tiles + a shared empty state) across the remaining modules, and
make sure NOTHING is missed (exhaustive, Ultracode).

## Approach
1. **Discover exhaustively (Workflow, read-only fan-out):** one agent per module (driver, team, trip,
   trip-payment, vehicle, customer, finance, report, user, dashboard) reports EVERY local building block
   that duplicates a shared `Fleet*` component — section cards, metric/stat tiles, empty/error states,
   section headers, avatars/initials, plus any lingering local validators/formatters. Returns file:line,
   signature, the exact delegation, and a visual-equivalence risk note. A synthesis agent merges + dedups
   into a master migration list; a completeness critic flags anything not yet covered.
2. **Fix sequentially (me):** apply each delegation in the main tree (NOT in workflow worktrees — edits
   must land here), grouped by module; `:androidApp:assembleDebug` after each module group.
3. **Adversarially verify:** re-scan to confirm no local duplicate remains and the shared component is the
   single implementation; final green build.

## Delegation rules (preserve behavior)
- Section card `(title, icon, content)` → `FleetTitledSectionCard(title, emoji=icon, content)`.
- Section card with `subtitle` (team-create) → `FleetSectionCard { FleetSectionHeader(title); subtitle Text; content }` OR extend `FleetTitledSectionCard` with an optional `subtitle` (preferred — one place).
- Container-only cards → `FleetSectionCard`.
- Stat/metric tiles → `FleetMetricTile` (preserve value color via `valueColor`, accent, emoji/icon).
- Empty states → new `FleetEmptyState(icon/emoji, title, message?, actionLabel?, onAction?)`; converge bespoke ones; keep genuinely unique layouts.
- Keep local composable NAMES as thin delegates where call sites are many (low-risk, no mass call-site edits). Strings stay EN+HI; colours from theme; dims from `FleetTokens`.

## Risks / mitigations
- Visual normalization (slight radius/padding/header-style shifts) is expected and desirable (consistency);
  flag any material change. Behaviour unchanged. Build per module; D verifies on-device.
- Don't edit inside workflow worktrees (won't propagate) — workflow is for discovery/verification only.

## Verification
- Per-module `assembleDebug`; final exhaustive grep shows each `Fleet*` component is the sole implementation
  and per-module copies are gone or thin delegates.

## Result (executed — all green, androidApp:assembleDebug)
- **Sweep** (9 read-only agents) found ~160 duplications across 9 modules — far beyond the eyeball audit.
- **Foundation gaps closed:** `FleetSectionCard`/`FleetTitledSectionCard` gained `containerColor`/`border`/
  `elevation`/`contentPadding`; `FleetAvatar` gained `border`; `FleetMetricTile` gained `showBackground`/
  `centered`; `EmptyContent`/`ErrorContent` gained `fillMaxSize` toggle + `ErrorContent` secondary action;
  new `FleetInlineErrorBanner`.
- **Card-system reconciliation:** discovered I'd shadowed pre-existing `CardComponents.kt`
  `FleetSectionCard(title,…)`/`FleetSectionHeader(title,action)`/`FleetEnhancedSectionCard` with overloads.
  Removed the two dead ones, renamed the live plain-column variant → `FleetFormSection` (+12 finance/vehicle
  call sites). `FleetSectionCard`/`FleetSectionHeader` now unambiguously = the modern components. Also
  deleted a duplicate `FleetEmptyState` I'd added (`EmptyContent` already existed).
- **Migration:** safe wave (~80 none+minor) + material wave (~80, foundation params used to preserve look),
  each a parallel per-module agent fan-out (edit-only; I compiled). Genuinely-unpreservable items (solid-fill
  hero tiles, opaque role chips) were skipped by the agents with reasons.
- **Adversarial verification** (9 reviewers + synthesis): no behavioral/logic regressions. Fixed 3 i18n/UX
  regressions (trip-cost empty strings → new EN+HI keys; payment "Customer"/"Received By" → resources;
  customer search-empty dup title==message), honored emphasis flags via `showBackground` (customer
  `FinancialMetric.isLarge`, report `KPIItem.isHighlighted`), vehicle stat tiles → transparent/centered,
  finance in-list empty → `fillMaxSize=false`. Verifier's "lost tint" flags (ProfileHeader, SignUpSuccess)
  were false positives (those cards were already `surface`) — not changed.
- **Dead code removed:** `DriverCostsSummaryCard`, `DriverCostItem` (composable), `TripQuickStat`,
  `EditSectionHeader` (trip), `StickyVehicleHeader`, `PaymentItem`.

## Follow-up (completed — user-requested, build green)
- Team/Profile created-on kept human-readable `DD-MMM-YYYY` (the #12 standard); team got a `—` blank fallback
  (matches profile) for unset `createdAt`.
- Finance **detail** sections (`VehicleFinanceDetailScreen`, 6 sections incl. the payment-history one whose
  `trailingAction` mapped to `actionLabel`/`onActionClick`) → `FleetTitledSectionCard` (cards). Actual forms
  (`AddPurchaseInfoScreen`, `MaintenanceCostEntryScreen`) keep `FleetFormSection`.
- Removed unused team state/entity fields: `TeamMember.initials`, `State.displayName`, `State.initials`,
  `adminsCount`/`usersCount`/`activeCount`.
- Localized the 5 trip-edit section titles (Vehicle & Driver / Route / Schedule / Cargo & Customer / Notes)
  via new plain-text `trip_edit_section_*` keys (EN + HI; emoji stays the chip param).

## Still deferred (pre-existing, out of scope)
- Other pre-existing hardcoded strings flagged LOW (finance form placeholders, etc.) — separate i18n pass.
