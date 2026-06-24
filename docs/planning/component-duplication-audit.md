# Audit — Component & logic duplication across modules (IndusJSFleet_Apps)

Deep inspection of cross-module duplication, with the canonical/shared target for each and a phased
consolidation plan. Includes the "Add Member password" finding that prompted this.

## 0. Add Member "Password must be at least 8 characters"
- Client side is already correct: `CreateTeamMemberViewModel` no longer validates password length (removed
  with the single-policy work); the screen uses `FleetPasswordField` (no rules). The error UI shows
  `error.message` from the **backend** (`UiText.Raw`).
- The string `error_password_min_chars` = "Password must be at least 8 characters" now has **zero code
  usages** (only stale imports, now removed). So the message the user sees is either:
  1. a **stale APK** still running the old client check (most likely — "8" was exactly the old client
     value), fixed by reinstalling the latest build; or
  2. the **backend** team-member-create endpoint enforcing min-8, which would contradict A's single
     `PASSWORD_POLICY` (simple = min 6) → a Backend/IAM fix to relay to A.
- Action: removed dead imports; rebuild; reinstall and re-check. If it persists post-reinstall, it's
  backend (relay to A).
- Cleanup candidates (now unused client strings, inconsistent): `error_password_min_chars` ("8"),
  `change_password_min_chars` ("6").

## 1. Validation logic — shared utils exist but are bypassed (HIGH)
Canonical: `ijs-core-lib/.../core/util/ValidationUtils` already provides `isValidEmail`, `getEmailError`,
`isValidMobile`, `getMobileError`, plus date/time/amount/cost validators.
- **`isValidEmail` reimplemented locally in 5 screens** (slightly different regexes!):
  `screen-driver` CreateDriverViewModel + DriverDetailViewModel; `screen-team` CreateTeamMemberViewModel +
  TeamMemberDetailViewModel; `screen-user` SignUpViewModel.
- **Mobile/10-digit validation reimplemented** in `screen-driver` (×2), `screen-trip` CreateTripViewModel,
  `screen-user` SignUpViewModel, `screen-customer` (×2).
- Risk: divergent rules (e.g. `+_` allowed in some regexes, not others) → inconsistent UX.
- **Fix:** route all to `ValidationUtils.isValidEmail/getEmailError` and `isValidMobile/getMobileError`;
  delete the local copies.

## 2. Date / mobile formatting (HIGH)
Canonical: `ijs-datetime-utils/FleetDateTime` + `ijs-core-lib/.../util/TimeUtils`
(`formatDateToHumanReadable`, `formatDateTimeForDisplay`).
- **`formatDate`/date display reimplemented** in ~7 places: `screen-driver` DriverDetailOverviewContent;
  `screen-vehicle` VehicleDetailEditContent + VehicleDetailViewModel(`formatDateForPdf`); `screen-user`
  ProfileScreen; `screen-report` ReportsScreen(`formatDateDisplay`); `screen-finance`
  EmiPaymentHistoryScreen(`formatDateDisplay`); `screen-trip-payment` AddTripPaymentViewModel(`formatDateForInput`).
- **`formatMobile`** local in ProfileScreen (and inline elsewhere).
- **Fix:** standardize on `FleetDateTime`/`TimeUtils`; if a needed variant is missing, add it there once.

## 3. "Section card with icon header" UI (HIGH — most pervasive)
~10 near-identical "rounded card + leading icon/emoji chip + title header + content" composables, one per
module:
- `screen-driver` SectionCard + SectionHeader; `screen-team` SectionCard; `screen-trip`
  EnhancedSectionCard + SectionCard; `screen-vehicle` EnhancedSectionCard + SectionHeader; `screen-report`
  ReportCard; `screen-dashboard` DashboardSectionCard + DashboardSectionHeader; `screen-user`
  EnhancedProfileCard.
- **Fix:** promote ONE `FleetSectionCard` + `FleetSectionHeader` into `ijs-ui-components-lib` (generalize
  the dashboard `DashboardDesignSystem` versions); migrate all modules; delete the per-module copies.
  Biggest consistency + LOC win.

## 4. Stat / metric tile (MEDIUM)
Multiple "big number + label (+ accent/icon)" tiles: dashboard `MetricTile`; cost-overview
`FinancialStatCard`; profile `EnhancedStatItem`/`EnhancedStatItemWithIcon`; customer-PL `Kpi`/`MiniStat`;
various report stat rows.
- **Fix:** promote `MetricTile` (+ a compact `StatItem`) to `ijs-ui-components-lib` as `FleetMetricTile`;
  reuse across dashboard/profile/reports/customer-PL.

## 5. Empty / error states (MEDIUM)
Canonical: `ijs-ui-components-lib/CommonComponents` `ErrorContent` + `EmptyContent`. Bespoke re-rolls:
dashboard `SectionEmptyState`; trip `TripCostsEmptyContent`/`PaymentsEmptyContent`; trip-payment
`TripSelectorEmptyContent`; customer-PL `EmptyState`; customer `EmptyCustomerState`.
- **Fix:** converge on a small parameterized `FleetEmptyState` (icon, title, message, optional action) and
  reuse; keep genuinely bespoke ones only where layout differs materially.

## 6. Avatar / initials (LOW)
`computeInitials` (dashboard drawer), inline initials (ProfileScreen), `take(1)` (customer bottom sheet).
- **Fix:** one `FleetAvatar(name)` / `initialsOf(name)` util in `ijs-ui-components-lib`.

## 7. Password (mostly DONE — reference for the pattern)
`FleetPasswordField` already consolidated all password inputs; password policy is backend-only. This is the
model to replicate for #1–#6: one shared component/util, delete per-module copies. Residual: remove the now
unused `error_password_min_chars` / `change_password_min_chars` strings.

## Phased consolidation plan (recommended order)
1. **P1 Validation + formatting (#1, #2):** lowest-risk, pure logic — point everything at `ValidationUtils`
   / `FleetDateTime`-`TimeUtils`; delete local copies. (No UI risk.)
2. **P1 `FleetSectionCard` + `FleetSectionHeader` (#3):** promote to ui-components, migrate module-by-module
   (build per module).
3. **P2 `FleetMetricTile` (#4) + `FleetEmptyState` (#5):** promote + migrate.
4. **P3 `FleetAvatar`/initials (#6) + string cleanup (#0/#7).**

Each step: build (`:androidApp:assembleDebug`) after each module migration; presentation/logic-equivalent,
no behavior change; D verifies visually. Honors Clean+MVI, `Fleet*` reuse, no hardcoded colours, EN+HI.

## Verification of this turn
- Removed dead password-min imports from `CreateTeamMemberViewModel`; `:androidApp:assembleDebug` green.
- This doc is the inspection deliverable; consolidation to follow on approval (pick a phase).
