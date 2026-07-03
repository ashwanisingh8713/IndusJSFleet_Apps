# DDD Fidelity — Step 4 / Step 6 / Device-Alerts · VERDICT

> Produced from a 7-agent adversarial workflow (review → verify). Every FAIL below was independently re-verified in source by a second agent. Fidelity-only (behavior/permissions = D).

## Scoreboard
| Group | Verdict | Headline |
|---|---|---|
| Step 4 — cards + elevated surface | **PASS-WITH-NITS** | consolidation good; elevated-surface not routed to menus/dialogs/sheets |
| Step 4 — chips + segmented rollout | **PASS-WITH-NITS** | segmented reuse good; a 4th chip style survives; my D3 wording was self-contradictory |
| Step 6 — status colours + §H money | **🔴 FAIL** | payment money numerals status-coloured (incl. unconditional green group total) |
| Device-alerts | **🔴 FAIL** | full-card semantic tints + rainbow per-type colour map |

---

## ✅ FAIL 1 — RESOLVED (payments) 2026-06-30
B reshipped: `payments-list-neutral-numerals-EN-light.png` shows summary tiles **neutral** (Received/Pending/This-Month, gray tiles, neutral numerals) and group totals **₹50,000.00 / ₹10,000.00 neutral dark** — the unconditional-green group total is gone; the MEDIUM summary-tile tint is also fixed. §H addendum satisfied on the light capture.
- **~~Still open~~ → CLOSED 2026-06-30:** `payments-list-expanded-group-EN-dark.png` confirms in dark — summary tiles, group totals (`₹50,000.00`/`₹10,000.00`), AND per-payment amounts (`₹20000`/`₹30000`) all **neutral `onSurface`**, status carried by chips (Partial=amber / Received=green). Per-payment + compact-item amounts neutral. FAIL 1 fully closed, light + dark.
- **New NIT (minor, §5):** individual payment amounts lack Indian comma grouping (`₹20000` should be `₹20,000`) and drop the `.00` decimals the summary tiles use (`₹60,000.00`) — a formatting inconsistency; normalize to the grouped full form. **→ ✅ RESOLVED** (`payments-expanded-grouped-amounts-EN-light.png`): individual amounts now `₹20,000` / `₹30,000` (grouped). (Trivial residual: line items omit `.00` decimals while summary/group tiles show them — negligible.) **→ ✅ RESOLVED** (`payments-totals-no-decimals-EN-light.png`): summary + group totals now drop `.00` too — all amounts consistent grouped no-decimal (`₹60,000`/`₹50,000`/`₹10,000`). Payments 100% clean.
- **Separate, still pending (expected-remaining):** the **dashboard Financial Overview** §H de-tint — the mislabeled dark shot shows Expenses orange ₹0 / Profit green ₹0 still tinted+coloured. That's the dashboard §H fix (step-4/6), distinct from the payments fix just landed. **→ ✅ RESOLVED 2026-06-30** (`dashboard-financial-neutral-EN-light.png`): money numerals neutral (`₹1.3 L`/`₹27.5K`/`₹1.02 L`), tiles de-tinted to neutral, and **Profit sign via a ▲ Profit green delta chip with a NEUTRAL numeral** — a textbook §H A-plus. Residual NIT: Fleet Overview + Trips **count tiles** still carry faint ~0.08α semantic tints (lower-priority anti-rainbow cleanup; money cards were the priority and are fixed). **→ ✅ RESOLVED** (`dashboard-count-tiles-neutral-EN-dark.png`): count tiles now uniformly neutral; dashboard reads fully calm (colour only in the ▲ Profit chip + the sanctioned availability accent bar). HI capture still needed to confirm the compact suffix localizes (लाख/हज़ार) vs the EN `L/K` shown.

<details><summary>Original FAIL 1 (pre-fix)</summary>

### Step 6: payment money numerals are status-coloured (§H rule 1 / §1)
**Verified in source.** Money numerals painted by status: green `PaymentReceived` / amber `PaymentPending`, and the **group total is hardcoded green unconditionally** even on a plain positive sum.
- `PaymentListComponents.kt:305-312` (card amount), `PaymentGroupComponents.kt:215` (unconditional green group total), `:319-326` (compact item).
- §H rule 1: **all money numerals neutral `onSurface`** (SemiBold, tabular). §H rule 3: positive stays neutral; only a true **loss** goes `error`-red. Green-on-positive is the exact rainbow §H retires.
- **FIX (redline):** amount numerals → `onSurface`; group total → `onSurface`; keep the payment **status** in the existing `PaymentStatusBadge` chip (that part is correct). No colour on the numeral. (Only a negative/refund figure may take `error`.)
- *Note:* the `status-colors-payments-en-light.png` capture is actually the **Home dashboard**, not payments — so this rests on source truth. Need a real payments-list capture (EN light+dark, grouped+flat) to close visually.
- Also MEDIUM: payment **summary KPI tiles** have status-tinted backgrounds + coloured numerals (`SummaryItemCard`) — a lingering rainbow-tile pattern; de-tint to neutral tiles.
- ✅ Correct already: `PaymentStatusBadge` semantic pill chips (C1 pairs), neutral list-card surfaces, no stray raw swatches.
</details>

## ✅ FAIL 2 — RESOLVED (device-alerts) 2026-06-30 — re-verified light + dark + source
B reshipped per the §I redline. `alerts-severity-accent-EN-light.png` + `-dark.png` + source (`screen-alerts/.../AlertsListScreen.kt:281-295`) all confirm:
- ✅ **Neutral cards** (`containerColor = colorScheme.surface`) — full-card tints + coloured borders gone.
- ✅ **4dp severity left-accent bar** ("§I redline" cited in code L281,295).
- ✅ **Severity → ONE C1 semantic set** (L284-286): CRITICAL→`error`, WARNING→C1 amber, INFO→C1 teal — the per-type `secondary`/`primary` grab-bag retired; WARNING now renders amber (not grey).
- ✅ **Neutral `onSurface` title text**; priority chip retained; filter chips unified.
- ~~**Residual NIT:** warning/info accents use fixed constants…~~ **→ WARNING RESOLVED** (`alerts-warning-accent-theme-aware-EN-dark.png` + source): WARNING now uses `fleetWarningAccent()` — theme-aware (`isAppInDarkTheme() ? ExpenseAmberOnDark #E8B24A : ExpenseAmberDark #855900`), brighter amber in dark. CRITICAL themes via `colorScheme.error`. **Tiny follow-up:** INFO still uses fixed `FleetStatusColors.InfoBlue #1C8A93` (not theme-flipped) — reads acceptably (mid-tone teal) but should get the same `fleetInfoAccent()` treatment (dark → C1 `#46C7D0`) for consistency, and align `InfoBlue` to the C1 info values. Low priority. **→ ✅ RESOLVED** (source + `alerts-severity-accent-theme-aware-EN-dark.png`): INFO now `fleetInfoAccent()` = `isAppInDarkTheme() ? InfoBlueOnDark #46C7D0 : InfoBlue #1C8A93`; applied consistently incl. the days-left badge (L364-365). All three severities now theme-aware. Trivial residual: light info accent `#1C8A93` vs C1 info-light `#0B6A73` — acceptable for a non-text accent bar.

<details><summary>Original FAIL 2 (pre-fix)</summary>

### Device-alerts: full-card tints + rainbow colour map (§1 anti-rainbow)
**Both findings verified in source.** This is the new tracker-firmware alerts feature and it did **not** go through the anti-rainbow rule.
- **Full-card tint:** `AlertsListScreen.kt:309-314` — every card `containerColor = alertColor.copy(0.06f)` + a 1.5dp `alertColor.copy(0.35f)` border. §1 hard acceptance (line 178): "status colours appear only as chips/dots/accent bars — no full-card tints." Screenshots show pink/grey/lavender full-card tints across all tabs.
- **Rainbow map:** `AlertsListScreen.kt:282-299` — `alertColor` is a per-**type** grab-bag onto core roles: WARNING-type alerts → `secondary` (**grey**), INFO → `primary` (**indigo**), no `warning`-amber and no `success`. So a WARNING card renders grey while its own WARNING badge is amber (internal contradiction).
- MEDIUM: alert **title text** painted in the semantic hue (should be neutral `onSurface`); WARNING badge uses raw `FleetStatusColors.FleetMaintenance = Color(0xFFC77A00)` (frozen hex, won't flip for dark) instead of a themed C1 warning token.
- ✅ Correct already: the filter bar uses the unified `FleetFilterChip`; the priority chip itself is the sanctioned chip use of colour.
- **FIX (redline — §I in DESIGN_SPEC):** neutral `FleetSectionCard` surface (regains the §4 depth model + 1px dark highlight automatically) **+ a 4dp semantic LEFT-ACCENT BAR** keyed to severity **+ the existing priority chip** + **neutral `onSurface` title**. Remap `alertColor` from per-type to **severity → ONE semantic set**: CRITICAL→`error`, WARNING→C1 `warning`, INFO→C1 `info` (retire the `secondary`/`primary` grab-bag and the raw `FleetMaintenance` hex).
</details>

---

## Step 4 — cards + elevated surface (PASS-WITH-NITS)
- ✅ `FleetSectionCard` consolidated, token-driven; light soft-shadow / dark 1px-highlight / tinted-flat branch all correct. `FleetElevatedSurface` C2 alphas (10/10/7/6/5/4%) correct on cards + tab pill. Buttons `Shape.Button` 8dp + indigo + weight-fix. ✅
- 🟡 **MEDIUM — elevated-surface not routed to menus/dialogs/sheets.** `fleetElevatedSurface` is applied ONLY on the card + tab pill. `FleetDialogs.kt` (raw `AlertDialog`), `FleetDropdown.kt`/`CostTypeChipSelector.kt` (raw `DropdownMenu`), `CustomerSelectionBottomSheet.kt` (raw `ModalBottomSheet`) carry NO top-highlight → violates §9.3 ("every elevated dark card/**sheet/menu/dialog**"). Item 4 half-delivered. **Fix:** wrap these through `fleetElevatedSurface` (or `FleetMenu`/`FleetDialog` wrappers).
- 🟡 MEDIUM — `FleetStatusBadge` still boxy `RoundedCornerShape(4.dp)` + Bold/0.3sp (`CardComponents.kt:115-128`); §8 said retire to `Radius.Pill`. Migrate. **→ ✅ RESOLVED** (`status-badge-pill-EN-light.png` + source): now `RoundedCornerShape(FleetTokens.Radius.Pill)` @0.12α — the "Completed" trip badge renders as a pill.
- NIT — vestigial no-op `FleetCard` still a bare Column (§8 said remove/give-chrome).
- **DDD ruling — card radius:** `FleetSectionCard` uses `Radius.XL(16)` vs §4/§8 `Shape.Card=Radius.L(12)`. **Accept 16 for section cards** (reads premium; no rework); controls/buttons keep the smaller radii. Spec note updated.

## Step 4 — chips + segmented rollout (PASS-WITH-NITS)
- ✅ Segmented control reused across all 4 selectors (Vehicle/Customer P&L period, dashboard period, group-by) — real `FleetTabBar`, no bespoke/Material `SegmentedButton`. Vehicles-list filter chips on the unified `FleetFilterChip`. ✅
- **DDD ruling — resolve my D3 contradiction:** D3 said "one **primary-tinted** selected token **matching the tab pill**" — self-contradictory (the tab pill is neutral `surface`). **Ruling: ACCEPT the build's neutral `secondaryContainer` selected-chip token** (calm, anti-rainbow-consistent, clear enough, avoids ~30-site churn). Chips and the segmented pill are deliberately **different idioms** (chip = filter toggle; pill = single-select on a track) and need not share a token. Correcting D3 wording in the spec.
- 🟡 MEDIUM — a **4th divergent chip style survives**: `CostTypeTwoLevelSelector` still uses a raw Material `FilterChip` (`primaryContainer` fill, rounded-rect 12dp, check icon) in 3 cost-entry screens (`CostTypeChipSelector.kt:233-269`). **Migrate to `FleetFilterChip`** to actually finish "four unified to one." **→ ✅ RESOLVED** (`cost-type-fleetfilterchip-EN-light.png` + source): now `FleetFilterChip` (`CostTypeChipSelector.kt:232`), no raw `FilterChip(` remains — all four chip styles truly unified. *(Note: the file still uses a raw `ExposedDropdownMenu` at :146 — that belongs to the separate §9.3 menus-not-routed-through-FleetElevatedSurface finding, still open.)*
- 🟡 MEDIUM — **latent Hindi overflow:** Vehicle P&L `PERIOD_OPTIONS` and Consolidated Group-By carry **hardcoded English** labels, masking the clip risk. Once localized, Vehicle P&L runs `scrollable=false` (`weight(1f)`, 5 Hindi labels at 1/5 width, `maxLines=2` + `Clip`) → could wrap-then-clip. Ties to the step-3 measured-overflow carry-forward. **Fix:** localize + set Vehicle P&L to scrollable/measured-overflow before Hindi ships.
- NIT — the two P&L period selectors are wired inconsistently (`scrollable` default vs `false`); normalize.
- NIT — `vehicle-pl-segmented-en-light.png` is a **stale** capture (rounded-rect chips, pre-migration); grade against `vehicle-pl-chiptoken`.

---

## What I need from B/PO
1. **Fix the 2 FAILs** per the redlines above (payments money-neutral; alerts neutral-card + accent-bar + severity→semantic-set). These block those screens' fidelity sign-off.
2. Captures to close visually: a real **payments-list** (EN light+dark), and **device-alerts dark** + loading/empty.
3. Confirm the step-4 mediums (elevated-surface routing to menus/dialogs/sheets; StatusBadge pill; CostType chip migration) are in scope now vs a cleanup pass.
4. Noted my rulings: accept neutral chip token (correct D3), accept 16dp section-card radius, alert-card redline = §I.
