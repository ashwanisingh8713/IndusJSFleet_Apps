# c5 — Vehicle P&L label localization + FleetTabBar measured-overflow

Punch-list c5 (folds #31 + #48). Two independent halves.

## Analysis (why / current state)

**c5a — P&L period labels render hardcoded English (break Hindi).**
- `VehiclePLContract.PERIOD_OPTIONS` (VehiclePLContract.kt:145) = `listOf("today" to "Today", "weekly" to "Week", "monthly" to "Month", "yearly" to "Year", "custom" to "Custom")` — English label baked into the contract.
- Rendered at `VehiclePLFleetOverviewContent.kt:406`: `PERIOD_OPTIONS.map { (value, label) -> FleetTab(value, label) }` → the period `FleetTabBar`. In Hindi the tabs still read "Week/Month/Year".
- `VehiclePLResultComponents.kt:77`: `"$vehicleMakeModel • ${period.replaceFirstChar { it.uppercase() }}"` — raw period key, uppercased, never localized.
- `VehiclePLContract.periodDisplayText` (125-130) — hardcoded English `when(period)`, but **DEAD** (no render site found). Delete or leave; not user-visible.
- `currentPeriodLabel` — a DATE-RANGE label computed in the VM via `VehiclePLDateCalculator.getCurrentPeriodLabel(...)` (e.g. "July 2026"). Out of c5 scope (that's month-name/date localization, a bigger FleetDateTime task). c5 = the period SELECTOR names only.

**Strings ALREADY EXIST (EN + HI) — no new string work for the core set:**
- Short forms (for tabs): `reports_period_chip_week`=Week/`_month`=Month/`_year`=Year (strings.xml:1501-1503). VERIFY these have HI (`values-hi`); add if missing (सप्ताह/माह/वर्ष or reuse साप्ताहिक/मासिक/वार्षिक).
- `period_today`=आज, `period_custom`=कस्टम (HI confirmed at values-hi:551/558).

## Plan — c5a (contained, unblocked)

1. Add a small UI-layer resolver so the localization lives at the composable, not the contract:
   ```kotlin
   @Composable fun periodTabLabel(periodKey: String): String = when (periodKey) {
       "today" -> stringResource(Res.string.period_today)
       "weekly" -> stringResource(Res.string.reports_period_chip_week)
       "monthly" -> stringResource(Res.string.reports_period_chip_month)
       "yearly" -> stringResource(Res.string.reports_period_chip_year)
       "custom" -> stringResource(Res.string.period_custom)
       else -> periodKey
   }
   ```
   Place in a reports UI util (or top of VehiclePLFleetOverviewContent). Keep `PERIOD_OPTIONS` as key list; drop the English label half (or keep for the export filename only).
2. VehiclePLFleetOverviewContent:406 → `PERIOD_OPTIONS.map { (value, _) -> FleetTab(value, periodTabLabel(value)) }`.
3. VehiclePLResultComponents:77 → replace `period.replaceFirstChar{...}` with `periodTabLabel(period)` (needs the composable in scope; it is a @Composable).
4. Delete dead `periodDisplayText` (or leave). Keep the export filename using the raw key (filenames stay ASCII).
5. VERIFY values-hi has the 3 short chip strings; add HI if absent.

**Reuse:** existing strings + FleetTab + FleetTabBar. No new components.
**Future-proofing:** one resolver keyed on the period string → any new period option localizes in one place; mirrors how the dashboard already localizes its filter tabs.

## Plan — c5b (FleetTabBar measured-overflow, = #31, harder)

- FleetTabBar.kt:95 TODO: today `scrollable = tabs.size > 4` (count-based). Long-Hindi/large-font at ≤4 tabs can still overflow the fixed `weight(1f)` row → clipped (spec: wrap-never-truncate, clip catches it).
- Add a MEASURED trigger: in the non-scrollable branch, use `SubcomposeLayout` (or a Layout that measures the intrinsic content width of all segments at `weight(1f)` constraints) and, if total measured content width > available width, flip an internal `remember` flag to the scrollable path (reuse the existing `scrollMod`/auto-scroll/edge-fade code — no new UI).
- Keep the explicit `scrollable` param as an override; the measured flag ORs with `tabs.size > 4`.
- Risk: recomposition loop if the flag flips geometry that re-measures. Gate with a stable measurement (measure once at given constraints; only flip false→true, never oscillate). Test EN small-font (no scroll), HI large-font ≤4 tabs (scroll).

## Evidence (per item)
- c5a: HI capture of Vehicle P&L period tabs (माह/वर्ष etc.) — in-app EN→HI switch works within a session ([[i18n-language-switcher]]); AVOID activity recreation mid-capture (bug #42 resets to EN). EN + HI, light.
- c5b: HI + large-font (fontScale ~1.3) capture of a ≤4-tab FleetTabBar that now scrolls instead of clipping.

## Sequencing
c5a first (contained, high value, unblocked). c5b second (layout engineering; can defer if time-boxed — it is the #31 "nit"). Neither conflicts with the §H reports-color sweep (#51) — c5 is localization/layout, orthogonal to numeral color.
