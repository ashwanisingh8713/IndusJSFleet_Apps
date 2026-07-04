# IndusJS Fleet — Designer Visual Spec (Calm Fintech)

> **Author:** DDD (designer session) · **For:** session B (build)
> **Reviews:** `DESIGN_DIRECTION_2026-06_calm-fintech.md` (DECIDED · red-teamed v2) + `DESIGN_BRIEF_2026-06_redesign.md`
> **Status:** ✅ **PO-APPROVED 2026-06-30** — build against direction v2 + this spec. B1, C1, C2, C3, B2, B3 all ratified by PO; B1 is now **locked** (adopt, not shadow-only). The three §11 visual calls are resolved below. Mockups in `docs/design/assets/`.
> **Respects PO-LOCKED:** all contrast floors (§3/§4/§9) and the nav-gating model (§7) — unchanged.

---

## A. Verdict

**Endorse v2 for build.** The direction is coherent, internally consistent, and the red-team pass already fixed the failures I would have flagged first (light semantic chips <4.5:1, dark `outline` 2.83:1, white-on-light-indigo `onPrimary`, the `#161B22` dark collapse, the weight-toggle reflow, Devanagari line-metrics). As a designer I'd ship this. The indigo-single-hue + anti-rainbow discipline is the right call for a 20-row trip list, and the tonal-first depth model is correct for near-black dark mode.

I have **one substantive pushback** (light-mode glare parity, §B1) and **two refinements** (scroll discoverability §B2, premium-feel guardrail §B3). None are blockers.

---

## B. Pushback & refinements (design judgment, with rationale)

### B1. Substantive — light-mode active-pill needs a glare fallback (the symmetric twin of your dark rule)

**Evidence.** v2 hardens *dark* mode against cheap-LCD crushed blacks with a guaranteed 1px top-highlight "so dark mode does not rely on tone alone" (§4, §6). But the **active segmented pill in light mode relies on tone + soft shadow alone**: pill `surface #FFFFFF` on track `surfaceContainerHigh #ECECEA` is only ~4 luminance points of separation, and the 8%-black/16dp soft shadow is exactly what washes out first under bright-Indian-sunlight glare on a cheap in-vehicle LCD — the same panel class the sunlight-first mandate (§1) is written for. So the doc protects the dark selection affordance against field conditions but leaves the **light** selection affordance dependent on the most glare-fragile cue.

**Proposal (mirror the dark rule into light).** Give the **active pill a guaranteed-visible non-tonal affordance** too:
- **Primary:** a **1px hairline stroke in `outlineVariant` (`#D8DAE0`)** around the active pill, clipped to `Radius.Pill` — the light-mode analogue of the dark 1px top-highlight. Survives glare even when the shadow is invisible.
- **And** widen the tonal delta: keep the active pill `#FFFFFF` but **drop the track to `surfaceContainerHighest #E4E4E2`** (≈8 luminance points vs the current ≈4). No new tokens — both already exist in the §3 ladder.

This is **non-blocking** and stays inside the locked floors (it only *adds* contrast). I've drawn it this way in the mockups. **✅ PO-APPROVED — locked. Adopt this, not shadow-only.**

### B2. Refinement — scroll-fallback discoverability (6-tab Hindi Vehicle Detail)

A horizontally-scrollable pill row solves clipping but hides tabs off the right edge — a discoverability cost on the exact screen that triggers it. Spec'd into the mockups:
- **Auto-scroll the active segment into view** on entry and on selection (active never starts off-screen).
- **Right-edge fade mask** (`surface`→transparent, ~16dp) as a "more tabs" affordance whenever content overflows. Pure paint, no layout cost.
- Keep the measured-overflow trigger from §6 verbatim.

### B3. Refinement — Noto Sans is the correct call; protect "premium" elsewhere

Noto Sans + Noto Sans Devanagari is the **only** way to get true EN/HI metric parity with one bundled super-family, so I endorse it over a more characterful Latin face (Inter/Söhne would break parity or force a second Devanagari family with drifting baselines). The cost: Noto Latin reads slightly utilitarian. **Claw "premium" back through discipline, not typeface** — SemiBold large KPI numerics over muted `onSurfaceVariant` labels, the 1.45× line-height air, tabular figures, and tight 8pt spacing. This is the Ramp/Mercury register and it's already in §5; I'm just flagging it as the lever to lean on. No change requested.

---

## C. The three §11 visual calls (RESOLVED)

### C1 — Light semantic swatches (hue/feel; keep ≥4.5:1, target ≥5.5:1)

Kept the red-team's passing values and tuned the **family feel cooler/calmer** so the four read as one disciplined set beside indigo, not a Material rainbow. Foreground = on-container text/glyph; used only in chips, dots, left-accent bars.

| Semantic | fg (light) | container (light) | Target ratio (fg-on-container) | Feel |
|---|---|---|---|---|
| success | `#15683A` | `#DCEDE3` | ≥6.0:1 | deep calm forest, nudged cooler toward indigo |
| warning | `#855900` | `#F5E7CB` | ≥5.5:1 | amber-brown; stays dark — amber must to clear cream |
| info | `#0B6A73` | `#D3EDEF` | ≥6.0:1 | teal == `tertiary`, so info reuses the surviving teal |
| error | `#B0302A` | `#F7DEDC` | ≥5.6:1 | restrained brick red, not fire-engine |

Dark semantic set: keep v2 §3 as-is (already passes). **Authority for exact ratios = the generated WCAG report (§4); these inline targets are CI-gated against it.** If any lands <5.5:1, darken the fg one step before changing hue.

### C2 — Dark 1px top-highlight % per elevation step

White, 1px, top edge only, clipped to the corner radius, via `FleetElevatedSurface`. Lower/darker steps get more (crush is worst there and the highlight is the only affordance); higher steps already separate tonally, so less — avoids a "glowing edge" look.

| Surface step | Dark hex | Top-highlight α |
|---|---|---|
| `surfaceContainerLowest` | `#0B0D11` | **10%** |
| `surfaceContainerLow` | `#13161B` | **10%** |
| `surface` | `#161A20` | **7%** |
| `surfaceContainer` | `#191D24` | **6%** |
| `surfaceContainerHigh` | `#222730` | **5%** |
| `surfaceContainerHighest` | `#2C323C` | **4%** |

Default elsewhere = 6%. Final ±1–2% to be tuned on a real in-vehicle panel (§11 allows). Menus/dialogs route through `FleetElevatedSurface` at the % of whichever step they sit on.

### C3 — Per-role line-heights (trim=None, alignment=Center, all roles)

Paragraph Devanagari gets full air for shirorekha + stacked matras; single-line UI labels get just enough to clear matras while keeping bars compact; large single-line display/numerics tighten (matras have headroom at scale).

| Type role | Size (sp) | Line-height (sp) | Ratio | Use |
|---|---|---|---|---|
| display | 30 | 38 | 1.27 | hero numerics, one line |
| headline | 24 | 32 | 1.33 | screen titles |
| title | 20 | 28 | 1.40 | card/section titles |
| body | 16 | 24 | 1.50 | paragraphs (full Devanagari air) |
| body-small | 14 | 21 | 1.50 | secondary paragraphs |
| label | 12 | 16 | 1.33 | tab / nav / chip / button labels |
| KPI numeric | 28–34 | ×1.20 | 1.20 | SemiBold money/metrics, one line |

`letterSpacing = 0` on **all** Devanagari (positive tracking detaches matras). Money numerics force Latin Noto + `tnum` SpanStyle regardless of locale (§5).

---

## D. Component redlines (build-ready)

### D1 Segmented tab (replaces underline `FleetTabBar`; `FleetTab<T>` API preserved)
- **Track:** `surfaceContainerHighest` (light `#E4E4E2` / dark `#222730`), `Radius.Pill`, padding `Spacing.XXS` (2dp). *(Light track deepened per B1.)*
- **Active pill:** `surface` (light `#FFFFFF` / dark `#2C323C`), `Radius.Pill`, **1px `outlineVariant` hairline (light, B1)** + 1px top-highlight (dark, 4%). Soft shadow light only.
- **Label:** constant `FontWeight.Medium`, `label` role (12sp). Rest `onSurfaceVariant`; active `onSurface`. Icon (optional, 16dp) active-tinted `primary`.
- **Press:** 12% `onSurface` state-layer on touched segment; active pill scales 0.98.
- **Motion:** pill slides x **and** width together, `Motion.FastSpring` ~180ms (cap ≤200ms); labels crossfade same duration. No weight toggle.
- **Badge:** neutral — `surfaceContainerHighest` fill + `onSurfaceVariant` text. Red/primary reserved for true-alert tabs only. Trails label; wraps with 2-line Hindi, never truncates.
- **Scroll:** measured-overflow trigger (§6) on ALL breakpoints; + auto-scroll-active-into-view + right-edge fade (B2). Min-width segments, content-sized, never fixed to EN width.
- States in `assets/seg-tab-spec.svg`.

### D2 Bottom nav (Compact) + rail (Medium/Expanded)
- **Bar surface** `surfaceContainer` (light `#F2F2F0` / dark `#191D24`), hairline `outlineVariant` top divider, min-height ≈80dp + safe-area (2-line Hindi), `wrapContent` height.
- **Item:** 24dp icon above `label` (12sp Medium), **always-visible** both states. Selected = filled icon + active-slot pill `primaryContainer` (light `#E1E4FF` / dark `#2C36A6`) behind icon + `primary` label; **dark selected icon = `onPrimaryContainer #E1E4FF`** (not `primary`). Inactive = outline icon + `onSurfaceVariant`. Touch ≥44dp.
- **IA:** Home · Trips · Live Map · Payments · More(sheet: Vehicles, Drivers, Customers, Team, Vehicle Finance, Reports, Profile).
- **Backfill:** top-4 from the permitted/visible set (interim gating §7); never gaps/shift.
- **Rail:** `FleetNavRail`, breakpoint via **window size class** (`currentWindowAdaptiveInfo`/root `BoxWithConstraints`), never container `rememberFleetBreakpoint`. Medium = compact rail (~80dp, icon+short label stacked); Expanded = full labels beside icons, Hindi-sized. Persistent active highlight. Never a bottom bar on Expanded.
- **Motion:** outline→filled crossfade ~180ms; slot pill `FastSpring`; page `Standard` ~220ms; no bounce.
- Layouts in `assets/bottom-nav-rail.svg`.

### D3 Buttons / cards / chips / top bar — per §8 (no deltas from me)
- Buttons `Shape.Button` 8dp, indigo fills, `StateLayer` press, keep enum API + `RowScope.weight`-on-root fix; 12sp-on-primary labels bump weight; small glyphs `onSurface`-tinted not `primary`.
- Cards consolidate into `FleetSectionCard`; badges → pill radius; `FleetMenu`/`FleetDialog` through `FleetElevatedSurface`.
- Chips → one unified selected token = neutral **`secondaryContainer`** fill + `onSecondaryContainer` (RULING 2026-06-30, correcting an earlier self-contradiction: the segmented tab pill is neutral `surface`, so "primary-tinted matching the tab pill" was impossible). Chip and tab pill are **different idioms** (filter toggle vs single-select on a track) and need not share a token. The one remaining un-migrated style — `CostTypeTwoLevelSelector`'s raw Material `FilterChip` — must move to `FleetFilterChip`.
- Top bar `Height.TopBar` 64dp, `surface`, hairline `outlineVariant` divider, title/SemiBold, no ALL-CAPS.

---

## E. Mockup index (`docs/design/assets/`)
1. `color-tokens.svg` — light+dark core roles, 6-step surface ladder, semantic set (my C1 swatches).
2. `seg-tab-spec.svg` — rest / selected / pressed / disabled / badge / scroll-fallback, light + dark, redlined.
3. `bottom-nav-rail.svg` — bottom bar (EN + Hindi, selected) + compact rail + expanded rail, light + dark.
4. `vehicle-detail-before-after.svg` — before (underline, 6 crowded Hindi tabs) vs after (segmented + scroll).
5. `dashboard-before-after.svg` — before (drawer + Material blue) vs after (bottom nav + indigo).

---

## F. Build readiness
Engineering order in direction §10 stands; my spec changes nothing in sequencing. Net deltas B must apply on top of the direction doc:
1. Segmented tab: **light active-pill = 1px `outlineVariant` hairline + track `surfaceContainerHighest`** (B1).
2. Segmented tab: **auto-scroll-active + right-edge fade** in the scroll fallback (B2).
3. Adopt the **C1/C2/C3 token values** verbatim.

✅ **PO approved 2026-06-30; B released to build.** Redline ambiguities during build → route to DDD (via PO). Blockers/questions → bus.

---

## G. Build redlines — resolved (R1, 2026-06-30)

### R1.1 — Error role vs status-chip error: **keep both, they're different M3 slots (sign off B's split)**
Not a coherence bug. M3 `error` is a **fill** role (white sits on it); the status chip uses `errorContainer` + `onErrorContainer`, and the chip fg `#B0302A` **is** `onErrorContainer`. Same red ramp, two tonal stops the system requires — `error #C2362E` (fill) and `onErrorContainer #B0302A` (text-on-tint) are *supposed* to differ. **Do not unify.** Only gate: verify **white-on-`#C2362E` ≥4.5:1** (it's ~4.8); if it ever fails the CI report, darken the fill to `#BC332B`, not the chip.

### R1.2 — Derived colorScheme hexes (exact, supersede B's interim picks)
Derived from the §3 anchors to stay in-family (slate `secondary`, teal `tertiary`=info, the surface ladder, the indigo). **CI-gated against the generated WCAG report; if any `on*`-on-container pair lands <4.5:1, darken the `on*` one step — don't shift the container.**

| `colorScheme` role | Light | Dark |
|---|---|---|
| `secondaryContainer` | `#DCE2EA` | `#333C49` |
| `onSecondaryContainer` | `#2A323D` | `#DCE2EA` |
| `tertiaryContainer` | `#CDEAEC` | `#10353A` |
| `onTertiaryContainer` | `#044B50` | `#B0EBEF` |
| `inverseSurface` | `#2B3038` | `#E4E4E2` |
| `inverseOnSurface` | `#F2F2F0` | `#1A1E24` |
| `inversePrimary` | `#9AA6FF` | `#3D4EDB` |
| `surfaceBright` | `#FBFBFA` | `#343A44` |
| `surfaceDim` | `#DEDEDC` | `#0E1116` |

Notes: `inversePrimary` = the opposite-mode `primary` (standard M3, so an inverted snackbar shows the right indigo). `tertiaryContainer`/`on*` align to the info-semantic family (§3 info `#D3EDEF`/`#0B6A73`) so teal stays one coherent hue. `surfaceDim`/`surfaceBright` bracket the ladder ends.

---

## K. Soft-indigo surface tint — ✅ OWNER DIRECTION 2026-07-04 (supersedes grey-neutral surfaces)

The human owner gave taste feedback ("no grey anywhere"; "Home boxes don't look good"). **Decision: neutral surfaces carry a soft-indigo (lavender) tint, not grayscale.** Implemented app-wide by re-hueing the `Color.kt` neutral ramp (`surfaceVariant`, `surfaceContainer*`, `secondaryContainer`, `outlineVariant`, `surfaceDim/Bright`, `background`) from grey to a lavender ramp at the **same lightness per step** (so §4/§9 contrast floors hold); `primary`/`primaryContainer` (brand + selection) unchanged. `FleetMetricTile` neutral fill → `surfaceContainerHigh` (now lavender) so stat tiles read as tinted cards, not weak white+hairline boxes.

**DDD verdict: ENDORSE.** Verified on Home/Payments/Reports — it holds every invariant and reads more designed than the grey:
- **§H money-neutral PRESERVED** — numerals stay `onSurface` (dark) on the tinted surfaces; loss-red numeral + ▲/▼ chip unchanged. (Only *surfaces* changed hue, not numbers.)
- **Anti-rainbow (§1) still holds** — a single *brand* hue on surfaces is NOT a rainbow; semantic colour remains confined to chips/dots/accent bars + the loss-red numeral + data-viz (donut/chart). Cards on white stay white (white ≠ grey).
- **Contrast** — same lightness per step → floors preserved (CI luminance report must be re-run green against the retinted tokens).

**DDD refinements (bounded, within the owner direction):**
1. **Intensity — STRONGER recommended (updated 2026-07-04).** Compared the very-soft `#E7E9F8` vs the STRONGER `#DCE0F5` stat-tile fill: the **stronger reads better** — tiles present as clearly-designed lavender cards (directly fixes "boxes don't look good") while staying light + calm, and the white segmented pill pops more against it. Recommend the stronger, **gated on refinement 2**. (Final intensity is the owner's taste; this is the design-informed rec.)
2. **Selection must stay distinct (the one real risk — gates #1).** At `#DCE0F5` the surface is now CLOSE to the selection token `primaryContainer #E1E4FF` (~5–10 pts). The **selected `primaryContainer` states (nav slot pill, selected filter chips, active tab) must read clearly distinct** from the lavender non-selected. Safeguards: keep the **nav-bar / chip-track surface step a notch LIGHTER than the tile step**, and/or **deepen the selected token**. Verify with a selected-filter-chip + selected-nav capture at the stronger tint before locking; if selection collapses, soften back toward `#E7E9F8` or deepen selection. (The white segmented active pill is unaffected — white always pops.)
3. Re-run the CI contrast report over the retinted `Color.kt`; confirm `outline`/`onSurfaceVariant`/text floors still pass with the hue shift.

*(This supersedes "neutral surfaces = grayscale" in §3/§4 — the ladder is now a lavender ramp of the same lightness. §H money-neutral, §1 anti-rainbow, and all contrast floors are unchanged in principle.)*

---

## H. Money treatment for financial cards — ✅ PO-APPROVED 2026-06-30 (for step 4/6)

PO asked me to call how money reads once the Financial Overview rainbow retires (cards de-tint to neutral `surface` in step 4; status colour moves to chips in step 6). Two options were on the table: **(A) fully-neutral tabular figures** vs **(B) restrained ± semantic colour on profit/loss**. **A-plus approved — build against this.**

**Decision: A-plus — neutral numerals by default, sign signalled by a chip/accent, with one sanctioned exception for a true loss.**

Rules:
1. **All money numerals are neutral `onSurface`, SemiBold KPI, tabular figures, Indian grouping `₹2,50,000`.** Labels (`Total Business` / `Expenses` / `Profit`) are muted `onSurfaceVariant`. No card tints — cards are neutral `surface` with the §4 depth model. This is the Mercury/Stripe register and it's the §1 mandate verbatim ("money reads precise and quiet… colour only in chips/dots/accent bars").
2. **Profit/Loss sign is signalled by a semantic element, NOT by colouring the big number:** a small `▲ Profit` (success) / `▼ Loss` (error) **delta chip** beside the label, and/or a left-accent bar on the Profit card. Keeps the numeral sunlight-legible and column-aligned while giving the instant positive/negative read.
3. **One sanctioned exception:** when the value is an actual **loss (negative)**, the Profit *numeral itself* may take `error` colour — a loss is a genuine alert worth one coloured number. **Positive profit stays neutral** (green-on-every-profit is exactly the rainbow we're retiring).

**Why not B (colour the numerals):** the current build already shows the failure mode — green `₹0` / orange `₹0` on tinted cards; coloured money numerals routinely miss the sunlight-first contrast floor (§1) on cheap in-vehicle LCDs, and colouring *positive* profit green reintroduces the always-on accent the anti-rainbow rule kills. A-plus gets the at-a-glance signal from a chip/accent (which §1 explicitly sanctions for semantic colour) without putting fragile colour on the most important number.

Ties to [[financial-terminology]] (Total Business / Expenses / Profit) and [[pnl-driver-costs-excluded]]. **Status: ✅ PO-APPROVED 2026-06-30 — enters the step-4 (card consolidation) + step-6 (status refactor) build spec.**

**§H addendum-3 — P&L card washes + always-on category colour (2026-07-04, DDD RULING on B's flag):** several P&L cards carry an always-on **profit-green / loss-red card-background wash** (`ConsolidatedSummaryCard` ProfitGreen/LossRed @0.1α; smaller 0.06–0.15α container tints in Detail/List/Result/Trip). **RULING: NEUTRALIZE them** — same as the dashboard §H A-plus you already approved (which neutralized exactly this always-on profit/loss accent) and §1 (no full-card semantic tints). P&L cards = **neutral surface**; profit/loss is signalled by the **▲/▼ delta chip + the loss-red numeral exception** (already shipped) — not a card wash. Same family, also neutralize for §1 consistency (my observation, not just B's flag): (i) the **Reports fleet-snapshot count numerals** (2 green trips / 2 purple vehicles / 0% red margin) → neutral `onSurface` (counts aren't profit/loss; the loss already shows in the हानि numeral/chip); (ii) the **Trip-Detail cost-category amount pills** (Toll=blue, Fuel=green) → neutral money, category by label + icon, not a per-category colour pill. Net principle (unchanged across all my rulings): money/counts neutral `onSurface`; semantic colour ONLY in chips/dots/accent bars; profit/loss via ▲/▼ chip + the single loss-red numeral exception.

**§H addendum-2 — trip-payment surfaces (2026-07-04, #52 review):** two more payments surfaces still colour money / type:
- **Trip-selector card rollup (`TripSelectorBottomSheet`):** the प्राप्त (Received) `₹35,000` and लंबित (Pending) `-₹35,000` summary numerals render **green** — §H FAIL. → **neutral `onSurface`** (the row LABELS प्राप्त/लंबित can stay muted; only the numerals go neutral). Same on the payment-detail hero net-amount (indigo) → neutral.
- **Payment-TYPE badge (`getPaymentTypeColor`) — DDD RULING: neutralize.** Payment TYPE (Advance/अग्रिम, Partial/आंशिक, Full, Refund…) is a **category, not a status**, so it must NOT carry a per-type colour rainbow (currently Advance=teal `#D4E5E6`, Partial=amber `#EEE2D0` — borrowing the info/warning hues as arbitrary type identity, which *dilutes* the semantic meaning). → **one neutral chip token** (the D3 unified `secondaryContainer`/`onSecondaryContainer`), type distinguished by **text**. Reserve semantic colour for payment **STATUS** only (`PaymentStatusBadge`: Received=success, Pending=warning — already correct). Same principle as the §I device-alerts per-type ruling. Also: the group count chip ("2 भुगतान") → neutral, not the type hue. Align the trip-detail "Received" chip to the C1 success token (it currently uses indigo/`primaryContainer`, inconsistent with the payments-list green success chip).

**§H addendum — payments (2026-06-30 fidelity FAIL fix):** payment amount numerals + group totals must be **neutral `onSurface`** (SemiBold, tabular). Retire the status-colouring of amounts (`PaymentReceived` green / `PaymentPending` amber) and the **unconditional green group total**. Payment status stays in the `PaymentStatusBadge` chip (already correct). Only a negative/refund figure may take `error`. Summary KPI tiles → neutral (no status-tinted backgrounds/numerals).

---

## I. Alert-card redline (device-alerts) — fidelity FAIL fix (2026-06-30)

The device-alerts feature (tracker firmware) ships **full-card semantic tints** + a rainbow per-type colour map — a hard §1 anti-rainbow violation (both verified in source). Redline for B:

1. **Card = neutral `FleetSectionCard`** (standard surface) — drop `containerColor = alertColor.copy(0.06f)` and the coloured 1.5dp border. The card regains the §4 depth model (soft shadow light / 1px top-highlight dark) automatically.
2. **Severity affordance = a 4dp left-accent bar** (semantic colour) on the card's leading edge + the **existing priority chip** + a **semantic dot** if desired. Colour lives ONLY in the accent bar / chip / dot — never the card fill.
3. **Title text = neutral `onSurface`** (not the semantic hue).
4. **Remap `alertColor` from per-TYPE to SEVERITY → ONE semantic set:** CRITICAL→`error`, WARNING→C1 `warning` (`#855900`/`#E8B24A`), INFO→C1 `info` (`#0B6A73`/`#46C7D0`). Retire the `secondary`(grey)/`primary`(indigo) grab-bag and the raw frozen `FleetStatusColors.FleetMaintenance = Color(0xFFC77A00)` hex (use the themed C1 warning token so it flips for dark).
5. Result: a calm neutral list where severity reads from a disciplined accent-bar + chip, not a three-hue card rainbow. Ties to [[alerts-backend-owned]] (alert data is backend-owned; this is presentation only) and the anti-rainbow §1.
