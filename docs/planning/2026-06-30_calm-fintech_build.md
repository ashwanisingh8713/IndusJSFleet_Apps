# Calm Fintech build — engineering plan (session B)

> PO go-ahead received 2026-06-30. Build against **direction v2**
> (`docs/design/DESIGN_DIRECTION_2026-06_calm-fintech.md`) + **DDD spec**
> (`docs/design/DESIGN_SPEC_2026-06_calm-fintech_DDD.md`) + 5 mockups in `docs/design/assets/`.
> PO-LOCKED (do not change): all contrast floors (§3/§4/§9) and the nav-gating model (§7).

## Build order (direction §10 — my sizing)
1. **Tokens foundation** (this doc's focus) — colorScheme indigo + 6-step dark ladder + new FleetTokens.
2. Typography — Noto Sans + Noto Sans Devanagari; ramp + 1.45× line-height + tnum.
3. Segmented tab — REBUILD on custom SubcomposeLayout (not a re-skin).
4. Buttons / chips / cards — Shape/StateLayer; one selected-chip token; consolidate into FleetSectionCard.
5. Nav migration — FleetNavRail + bottom bar + More sheet; remove drawer; interim perm-gating + top-4 backfill.
   - Final slugs LOCKED by C (IAM) 2026-06-30: trips/vehicles/drivers/`live-map:view` (HYPHEN) = all;
     `customers:view`/`payments:view` = admin/owner; Home = `dashboard:supervisor_view`. Keep interim
     heuristic until PO sends slug-migration go + A confirms server-side enforcement live. ACK in iam.inbox.
6. Status-colour refactor — FleetStatusColors → themed semantic chips; grep audit for stray raw swatches.
7. Acceptance pass — §9 across light/dark · EN/HI · Compact/Medium/Expanded · sunlight/crushed-black.

## Net deltas applied (DDD §F)
- Seg-tab light active-pill = 1px `outlineVariant` hairline + track `surfaceContainerHighest` (B1, locked).
- Seg-tab scroll fallback = auto-scroll-active-into-view + right-edge fade (B2).
- Adopt C1 (light semantics), C2 (dark top-highlight % per step), C3 (per-role line-heights) verbatim.

## Step 1 sub-phasing (compile-safety)
Retiring `Radius.ML/XS/S` ripples to **~64 call sites across ~40 files** + `FleetShapes`. To keep a
green build and deliver the visible re-theme first, step 1 splits:

- **1a (foundation, compile-safe, additive/value-only):**
  - `Color.kt`: new indigo core roles + 6-step dark surface ladder (val NAMES unchanged → `Theme.kt`
    untouched). Light/dark per direction §3.
  - `FleetTokens.kt`: ADD `Shape` (Button=M/Chip=Pill/Card=L/Sheet=XL), `Motion` (FastSpring 180 /
    Standard 220 / pill cap 200), `StateLayer` (hover .08 / pressed .12 / focus .12 / selected .12 /
    disabled .38), `Focus` (2dp ring), `Divider` (1dp), `Number` (tnum feature), `Contrast` floors.
    `Radius.Pill` already exists.
  - Keep `FleetColors`/`FleetStatusColors` and `Radius.ML/XS/S` UNTOUCHED (no breakage). Poppins stays
    (type is step 2).
- **1b (the sweep, next):** retire `Radius.ML/XS/S`, rewrite `FleetShapes` 5 buckets → {M,L,XL,Pill},
  migrate the ~64 call sites, add `FleetElevatedSurface` modifier (1px top-edge highlight, C2 per-step
  %), re-tune `Elevation` toward tonal. Land as one mechanical commit so there is no broken interim.

## Exact token values (source of truth for 1a)
Core light/dark, ladder, outline two-tier, semantic set: see direction §3 + DDD C1 (light semantics)
+ C2 (dark highlight %). Key numbers verified by the WCAG report are authoritative; inline values are
CI-gated against it (acceptance §4).

## Risks / notes
- FleetStatusColors (ProfitGreen/FleetMaintenance/LossRed) stay OLD until step 6 → transitional colour
  mix is expected and acceptable between steps.
- Noto bundling (step 2) is necessary-but-not-sufficient for EN/HI parity — also needs
  `LineHeightStyle(trim=None)` + `includeFontPadding=false` (expect/actual on Android).
- Nav uses INTERIM gating on EXISTING perms now; explicit-slug migration is a later separate follow-up
  (not a blocker).

## Tracked follow-ups (DDD fidelity findings; non-blocking)
- **Step 1 ✅ certified** (light+dark). **Step 2 ✅ certified** (EN/HI parity, Noto, tnum). Grouped ₹
  (`formatCurrencyFull`) + app-wide `tnum` applied to Reports — verified. Donut-center label still
  abbreviates (`₹15.0K`) — tidy when touching that chart.
- **Legacy terminology leak** (DDD): Profile "Organization Overview" shows Managers/Supervisors from
  `teamStats.totalManagers/totalSupervisors`; app `UserRole` enum still has GENERAL_MANAGER/MANAGER/
  SUPERVISOR. Align to owner/admin/user. **Blocked on A** — coordinated (backend.inbox): need new
  teamStats role-count field names once A lands FLEET_ALLOWED_ROLES→admin/user; then retire legacy enum
  values + re-point tiles + drive role dropdown off /team/roles. See [[ask-owning-session-not-legacy-code]].
- **"Yesterday" not localized** (i18n): relative-date strings ("Today/Yesterday/N days ago") are hardcoded
  English in pure utils `ijs-core-lib/TimeUtils` + `ijs-datetime-utils/FleetDateTime`. Localize via
  injected strings or a Composable date-label layer (composeResources). Client-side, no backend dep.

### Currency/i18n backlog (DDD step-2 close; fold into step-6 currency work — not urgent)
- **Localized compact ₹ suffix:** `formatCurrency` emits English K/L/Cr (donut center "₹15.0K"). HI must
  render हज़ार/लाख/करोड़ — the localized-suffix half of the §5 split (compact form localizes suffix via
  string resources; columns use grouped `formatCurrencyFull`, done).
- **Test: lakh grouping** (₹2,50,000) untested — seeded data all <₹1L. Add a ₹1L+ value to the §9.7 snapshot.
- **Test: true vertical ₹ column** alignment not exercised (Reports = side-by-side tiles) — assert column
  alignment in the §9.12 snapshot.
- **§H re-shoot:** Reports is a clean §H example (Profit green / Expenses red) — re-capture for the §H
  fidelity check after step 4/6.

## Verification per step
Compile `:ijs-ui-components-lib` then `:androidApp:assembleDebug`; install `adb install -r -d -t`;
on-device screenshot light + dark; report progress to PO after each step. Redline ambiguities → PO→DDD.
