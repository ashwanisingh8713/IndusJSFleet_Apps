# Program plan — app-wide UI enhancement (design-system compliance sweep)

## Key finding: the design system already EXISTS
The app is NOT a greenfield. It already has a real design system; the work is to bring **every screen into
compliance** with it, fill a few small gaps, and polish — screen by screen. (Create Trip was the proof-of-concept.)

Already in place (verified):
- **Theme + dark mode** — `Theme.kt` has `LightColorScheme` + `DarkColorScheme`, switched by `isSystemInDarkTheme()`;
  `App.kt` is themed. **Day/Night already works** for anything using `MaterialTheme.colorScheme.*`.
- **Type scale** — `fleetTypography()` (full M3 roles) in `Theme.kt` → `MaterialTheme.typography.*`.
- **Tokens** — `FleetTokens` (Spacing XS–XXXL, Radius XS–XXL, Elevation, IconSize).
- **Responsive** — `FleetBreakpoints` (Compact <600 / Medium 600–840 / Expanded >840) + `rememberFleetBreakpoint()`.
- **~33 unified `Fleet*` components** — InputField, Dropdown, Button, SectionCard, TopAppBar, Dialogs, PasswordField,
  SearchField, Avatar, TabBar, EmptyState/Shimmer, StatusColors, etc.
- **Validation** — `ValidationUtils` (`isValidEmail`, `isValidIndianMobile`, date/time/amount, required selection) +
  `FleetPasswordField` (password policy is backend-only — [[password-ui-and-validation]]).

So "dark mode / resolutions / unified components" are mostly **already supported**; the gaps are **adoption +
consistency + a few missing pieces**.

## Web research applied
- **Design tokens are the single source of truth**; semantic color tokens map to light/dark values — use
  `colorScheme.*`/`FleetStatusColors`, never hardcoded `Color(0x…)`. ([Muzli dark-mode guide](https://muz.li/blog/dark-mode-design-systems-a-complete-guide-to-patterns-tokens-and-hierarchy/), [M3 design tokens](https://m3.material.io/foundations/design-tokens), [tokens & theming 2025](https://materialui.co/blog/design-tokens-and-theming-scalable-ui-2025))
- **Adaptive UI via WindowSizeClass** — make composables size-aware (Compact/Medium/Expanded); 2-col on wider widths.
  ([KMP adaptive layouts](https://kotlinlang.org/docs/multiplatform/compose-adaptive-layouts.html), [Android adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive))
- **Typography + contrast** — M3 type roles; body ≥14sp (never <12sp), line-height ~1.5; **WCAG AA** 4.5:1 normal /
  3:1 large text; touch targets ≥48dp. ([M3 typography](https://m3.material.io/styles/typography/applying-type), [WCAG 2.1](https://www.w3.org/TR/WCAG21/), [mobile a11y](https://fontfyi.com/blog/mobile-typography-accessibility/))

## Foundation gaps to close FIRST (small, shared — unblocks every screen)
1. **`validateName`** in `ValidationUtils` (non-blank, min length, allowed charset) — used by Add/Edit Driver, Customer,
   Team member, profile.
2. **Eliminate hardcoded colors** — only ~6 across all screens (`Color.White/Black`, 4 hex). Map to
   `colorScheme`/`FleetStatusColors` so Night mode is correct everywhere.
3. **"Crunchy" baseline checklist** (below) documented as the per-screen contract.
4. (Confirm) one source of truth for field rounding = `FleetInputField` (Radius.L) + the date picker fix already done.

## Per-screen compliance checklist (apply to each screen, one by one)
- **Components**: raw `OutlinedTextField`/`Card`/`Button`/`TextField` → `FleetInputField`/`FleetSectionCard`/`FleetButton`
  /`FleetDropdown`/`FleetSearchField`. No bespoke field shapes.
- **Tokens**: spacing/radius/elevation from `FleetTokens` — no ad-hoc dp literals.
- **Color / Night**: only `colorScheme.*` + `FleetStatusColors` — zero hardcoded colors; verify in dark mode.
- **Typography**: `MaterialTheme.typography` roles; body ≥14sp; sensible hierarchy; contrast ≥ WCAG AA.
- **Responsive**: `rememberFleetBreakpoint()` — single-column on Compact, 2-col / wider gutters on Medium/Expanded;
  full-width fields on Compact; nothing clipped at small widths; targets ≥48dp.
- **Validation**: Name → `validateName`; Email → `isValidEmail`; Mobile → `isValidIndianMobile`; Password →
  `FleetPasswordField` (+ confirm match); required fields gate submit with inline errors.
- **States**: loading (shimmer) / error (retry) / empty (`FleetEmptyState`) on every data screen.
- **i18n**: all strings EN + HI.
- **Verify**: `:androidApp:assembleDebug` green per screen; quick light+dark eyeball.

## Execution order (one by one)
0. **Foundation** — validateName + hardcoded-color cleanup + checklist.
1. **Auth / User** (login, signup, OTP, forgot/reset) — first impression + validation-heavy.
2. **Onboarding** (org create, team member, plan).
3. **Dashboard** (home; metrics/alerts).
4. **Vehicle** (list + add/edit + detail).
5. **Driver** (list + add/edit + detail; name/mobile/email validation).
6. **Customer** (list + add/edit + detail).
7. **Trip** ✅ (create done) — sweep list + detail/edit.
8. **Trip-payment** · 9. **Report** · 10. **Finance** · 11. **Team** · 12. **Alerts** · 13. **Map** · 14. **Payment**.

## Phase 2 — Dialogs (after screens)
Standardize the **27 raw `AlertDialog`/`Dialog`** usages onto `FleetDialogs` (consistent shape/typography/buttons/
dark-mode); confirm/destructive/input dialog variants.

## Cadence
One screen (or tight screen group) per iteration: audit → apply checklist → build green → report. Each screen ends green.
