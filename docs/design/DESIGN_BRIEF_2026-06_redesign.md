# Design Brief — Fleet App Visual Refresh (2026-06)

**Owner:** Product Owner (Apps)
**For:** Designer session (`DDD IndusJS Fleet app design`)
**Build:** KMP Apps session (`B`) · **QA:** Android E2E session (`D`)
**Status:** Direction APPROVED by product owner. Designer to produce spec.

---

## 1. Why we're doing this

The app currently reads as a **stock Material 3 build**, not a designed product.
Two specific complaints from the product owner:

1. **Tabs look bad everywhere.** `FleetTabBar` is a plain underline-indicator
   `TabRow` (bold text + thin colored underline, no fill, no container). Dated.
2. **Whole theme is not at international standard.** Default `#1976D2` blue /
   teal / orange Material palette, flat surfaces, no depth — looks like a
   template, not a premium global SaaS/fleet product.

Goal: lift the app to a **2025–26 international standard** — clean, premium,
depth-driven, "fintech/SaaS" polish — without breaking the KMP architecture or
the backend contract.

## 2. Approved direction (locked — do not re-litigate)

| Decision | Choice |
|----------|--------|
| **Primary navigation** | Replace the left **hamburger drawer** with a **modern bottom navigation bar** (top destinations) + a **"More" sheet** for overflow. |
| **Tab / control style** | Replace underline tabs with **segmented / pill tabs**: soft filled selected state, rounded container, layered surfaces, subtle shadows ("+ depth"). |
| **Scope** | **Full design-system refresh** — color tokens, typography, tabs, buttons, cards, nav — as one coherent system, rolled across all screens. |

## 3. Proposed information architecture (bottom bar)

Current drawer has 11 destinations: Dashboard, Vehicles, Drivers, Trips,
Customers, Payments, Live Map, Team Members, Vehicle Finance*, Reports*,
Profile. (* = permission-gated.)

**PO proposal** (designer may refine, but keep to 4 primary + More):

| Slot | Destination | Rationale |
|------|-------------|-----------|
| 1 | **Home** (Dashboard) | Daily landing / KPIs |
| 2 | **Trips** | Core operational flow |
| 3 | **Live Map** | Real-time tracking, high-frequency use |
| 4 | **Payments** | Money in/out, high value |
| 5 | **More** | Sheet → Vehicles, Drivers, Customers, Team, Vehicle Finance, Reports, Profile, theme toggle |

Notes:
- Bottom-bar items must **respect permissions** (gate like the drawer does
  today — e.g. Vehicle Finance / Reports). If a primary slot is gated off for a
  role, promote the next item.
- On **web / expanded breakpoint**, the bottom bar should adapt to a
  **navigation rail** (left) — we are KMP (Android + iOS + Web). Don't design
  mobile-only.

## 4. Constraints (hard — the build must honor these)

- **Kotlin Multiplatform + Compose Multiplatform.** Android, iOS, Web all render
  the same `commonMain` UI. No platform-only design.
- **Token-driven.** All colors via `MaterialTheme.colorScheme.*`; all spacing /
  radius / elevation via `FleetTokens`. **No hardcoded hex or raw dp** in
  components. New palette = new values behind the same token names where
  possible.
- **Light AND dark** must both be specified. Dark is a first-class theme.
- **Adaptive.** Respect `FleetBreakpoint` (Compact / Medium / Expanded). Specs
  must say what changes per breakpoint.
- **Accessibility.** Min touch target 44dp (`FleetTokens.Height.MinTouchTarget`);
  WCAG AA contrast for text and the selected-tab fill.
- **i18n.** Labels are EN + Hindi; Hindi strings run longer — segmented tabs must
  not clip. Don't bake text width assumptions into the design.
- **Reuse, don't fork.** Output feeds existing `Fleet*` components in
  `ijs-ui-components-lib`. We restyle the canonical components; we do not create
  parallel ones.

## 5. Current-state references (read these before designing)

- Tabs: `ijs-ui-components-lib/.../components/FleetTabBar.kt`
- Tokens: `ijs-ui-components-lib/.../theme/FleetTokens.kt`
- Theme + type scale: `ijs-ui-components-lib/.../theme/Theme.kt`
- Palette: `ijs-ui-components-lib/.../theme/Color.kt`
  (today: PrimaryLight `#1976D2`, SecondaryLight `#0097A7`, TertiaryLight `#FF5722`)
- Current primary nav (to be replaced): `screen-dashboard/.../components/NavigationDrawerContent.kt`
- App shell / Scaffold (where bottom bar wires in): `sharedUI/.../App.kt`

## 6. Deliverables expected from Designer

1. **Color system** — new light + dark palette mapped to the **existing
   `colorScheme` token names** (primary, secondary, tertiary, surface,
   surfaceContainer*, outline, etc.). Provide hex values + contrast notes.
2. **Type scale** — keep Poppins or propose a swap; specify any weight/size/
   tracking changes vs `Theme.kt`.
3. **Spacing / radius / elevation** — any changes to `FleetTokens` (e.g. new
   shadow/elevation tokens for "depth").
4. **Segmented tab component spec** — anatomy, states (rest/selected/pressed/
   disabled), badge, scroll-vs-fixed behavior, motion, redlines. This replaces
   `FleetTabBar`.
5. **Bottom navigation + rail spec** — anatomy, selected/unselected, badge,
   "More" sheet, breakpoint adaptation, motion.
6. **Refreshed core components** — button, card, top app bar — enough to make
   screens feel coherent with the new tabs/nav.
7. **Before / after** of at least: a detail screen with tabs (e.g. Vehicle
   Detail) and the main shell (Dashboard + nav).
8. Output as a spec doc under `docs/design/` (+ any visual assets) that session
   **B** can implement directly.

## 7. Acceptance criteria

- Tabs no longer underline-only; segmented/pill with depth, looks intentional.
- Bottom bar (mobile) / rail (web) replaces the drawer for primary nav;
  permission-gating preserved.
- One coherent token set drives light + dark; nothing hardcoded.
- Hindi labels don't clip; touch targets ≥ 44dp; AA contrast.
- A neutral reviewer would call it "a designed product," not "stock Material."

## 8. Open questions for the Designer (answer in your spec)

1. Brand color — keep blue family or move to a more distinctive brand hue?
   (PO is open; propose with rationale.)
2. Segmented tabs vs a top app-bar-integrated tab strip for detail screens —
   which reads more premium for our data-dense screens?
3. Bottom bar: labels always visible, or selected-only? (i18n + 5 slots.)

## 9. Sequencing & owners

1. **DESIGN (DDD)** — produce the spec per §6. → drop `DONE` + spec path in
   the bus.
2. **BUILD (B)** — implement tokens first, then `FleetTabBar` → segmented, then
   bottom nav / rail in `App.kt`, then roll components. (PO will brief B with a
   build plan once the spec lands.)
3. **QA (D)** — visual + functional regression across Android E2E flows; verify
   permission-gating of bottom-bar items, light/dark, Hindi, breakpoints.

Don't start BUILD until the spec is approved by the PO. Raise blockers via the bus.
