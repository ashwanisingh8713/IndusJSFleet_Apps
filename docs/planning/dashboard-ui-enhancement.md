# Plan — Dashboard UI enhancement ("international level")

## 1. Problem
The Dashboard screen's sections/UI feel underwhelming. Goal: elevate to a polished, modern, "international-level" look while preserving all data, behavior, MVI, and conventions.

## 2. Current-state analysis
Structure (`DashboardScreen` → `DashboardContent` LazyColumn):
- TopBar: time-based greeting + user name + last-updated + notifications/refresh.
- `FleetOverviewHeroCard` → `CostOverviewSection` (financial only, filters + charts) → `TripsStatusSection` → `AlertsSection` → `VehicleStatusSection` → `DriversStatusSection`.

What's good: real data, permission-gated finance, semantics/a11y, refresh animation, offline banner, empty states (`SectionEmptyState`).

Weaknesses (why it looks unpolished):
- **No unified card system.** Each section hand-rolls its own card (shape/elevation/padding/header), so spacing, corner radii, header styles, and emphasis are inconsistent across sections.
- **Weak visual hierarchy.** Section titles, metrics, and actions compete; numbers (the most important content) aren't given hero treatment.
- **Flat, samey cards.** Little use of depth, accent color, iconography rhythm, or "stat tiles" that modern fleet/analytics dashboards use.
- **Token under-use.** A strong design-token system exists (`FleetTokens` Spacing/Radius/Elevation/IconSize, `FleetStatusColors`) but sections use ad-hoc `dp` values.
- **Empty/loading** are functional but plain; no skeletons/shimmer for first load.

## 3. Design direction (best-practice, international)
- **Unified dashboard design system** (new shared composables in `components/`, reused by all sections):
  - `DashboardSectionCard` — consistent surface, radius (`FleetTokens.Radius.XL`), elevation (`Elevation.Card/Raised`), padding (`Spacing.L`), and a standard `SectionHeader` (leading icon chip + title + optional "View all"/trailing action).
  - `MetricTile` / `StatTile` — bold number-forward tile (large value, label, status accent, optional trend/delta + icon) for fleet/vehicle/driver/trip/cost stats.
  - `MiniTrend`/progress visuals where useful (utilization %, on-route ratio).
- **Hierarchy:** large bold metric values (`headlineSmall`/`headlineMedium`), muted labels, accent only for status; generous whitespace via `Spacing`.
- **Hero:** refine `FleetOverviewHeroCard` into a crisp KPI band (vehicles / drivers / active trips) with subtle accent and tap-through.
- **Consistency:** all radii/elevation/spacing/icon sizes from `FleetTokens`; all colours from `MaterialTheme.colorScheme.*` + `FleetStatusColors` (NO hardcoded colours). Light/dark safe.
- **Polish:** consistent section gaps (`Spacing.L`), aligned headers, subtle dividers, tasteful empty states, optional shimmer skeleton on first load.
- **Strings:** any new copy in EN + HI. **MVI:** presentation-only; no VM/contract/data changes.

## 4. Reuse & future-proofing
- New shared components live in `screen-dashboard/.../components/` (or promote to `ijs-ui-components-lib` if reused beyond dashboard later).
- Each section refactors to compose `DashboardSectionCard` + `MetricTile`, removing duplicated card code (net simplification).
- Future: a `FleetMetricTile` in the components lib reused by profile/reports; pull-to-refresh; chart polish.

## 5. Scope options (decision needed)
- **A — Phased high-impact first pass (recommended):** build the shared system (`DashboardSectionCard`, `SectionHeader`, `MetricTile`) + apply to the hero and the top 2–3 sections; iterate the rest next. Fast visible uplift, low risk, reviewable.
- **B — Full redesign in one pass:** refactor all six sections at once. Max impact, larger/riskier diff, longer before reviewable.

## 6. Risks / mitigations
- Large diff across ~7 files → phase it (Option A); build after each section.
- Subjective direction → confirm style + scope before executing (this doc + a scoping question).
- Behavior regressions → presentation-only; keep all click handlers/data wiring intact; `assembleDebug` after each step; D verifies on-device.

## 7. Verification
- `:androidApp:assembleDebug` green after each phase; visual check light/dark, full + sparse + offline data; a11y semantics preserved.
