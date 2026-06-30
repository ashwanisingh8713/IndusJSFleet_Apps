# IndusJS Fleet — Design Direction (Source of Truth)

> **Status:** DECIDED · red-teamed v2 (ship-with-fixes applied 2026-06-30). Hand to designer review, then engineering.
> **Scope:** Full design-system refresh — palette, type, tabs, buttons, cards, nav — shipped as one coherent system. KMP/Compose, one `commonMain` UI across Android + iOS + Web. Colors via `MaterialTheme.colorScheme.*`, dims via `FleetTokens`. No hardcoded hex/dp in components.

---

## 1. Direction name + philosophy

**Calm Fintech.**

A restrained, trustworthy, neutral-heavy interface built on a warm-neutral grayscale canvas, spending colour deliberately: one confident brand indigo for primary actions/active nav/selected pills/key numerics, and a disciplined semantic set (success/warning/error/info) used *only* in chips, dots, and left-accent bars — never tinting a whole card (the Samsara anti-rainbow rule, so a 20-row trip list stays calm). Money data reads precise and quiet (tabular figures, bold KPI numerics, muted labels, generous whitespace despite density — the Ramp/Mercury/Stripe register). Every screen reads identically in bright-sunlight light mode and first-class night dark mode, in EN and Hindi.

**Sunlight-first contrast mandate (design rule, not just "meets AA"):** light-mode contrast is deliberately pushed *past* AA minimums for bright-Indian-sunlight legibility on cheap in-vehicle LCDs. Codified as a contrast floor in `FleetTokens` (see §9). Every primary fill, `surfaceContainer` step, and `onSurfaceVariant` value is verified in glare and low-quality-LCD conditions, not just AA-on-paper.

---

## 2. Resolved decisions

| Open question | Decision | Rationale |
|---|---|---|
| **Brand hue** | **Indigo Trust** — bespoke deep indigo `#3D4EDB` (light), brightening to `#9AA6FF` (dark). Single brand hue. | Fintech-trust signal (Mercury/Stripe), ownable, distinct from generic-Android Blue 700 the audit flagged. Old blue+teal+orange tri-accent retired: teal survives only as semantic **info**, orange only as semantic **warning**. |
| **Segmented-tab treatment** | **Raised pill on a rounded track.** `surfaceContainerHigh` track at pill radius; active segment is a raised `surface` pill with a sliding-pill spring (≤200ms; width animates for Hindi). Replaces the underline `FleetTabBar`. | Gives depth + clear selection without weight-toggle reflow. Preserves `FleetTab<T>` API, badge slot, adaptivity. |
| **Bottom-bar label visibility** | **Labels ALWAYS visible**, both states, every item. 24dp icon above label; bar sized for 2-line Hindi wrap. | Bilingual EN+HI; icon-only is ambiguous for Devanagari (e.g. भुगतान / लाइव मैप). |

---

## 3. Color tokens — final light + dark

All values map to `MaterialTheme.colorScheme.*`. These **replace** the current `Color.kt` (`PrimaryLight = #1976D2` etc.).

### Core roles

| `colorScheme` role | Light | Dark | Notes |
|---|---|---|---|
| `primary` | `#3D4EDB` | `#9AA6FF` | Indigo Trust. |
| `onPrimary` | `#FFFFFF` | `#10165A` | **Dark: never white-on-`#9AA6FF`** (would fail). `#10165A` on `#9AA6FF` = 7.2:1. |
| `primaryContainer` | `#E1E4FF` | `#2C36A6` | Selected nav-slot / tinted chips. |
| `onPrimaryContainer` | `#161C73` | `#E1E4FF` | |
| `secondary` | `#475569` | `#9AA7B8` | Neutral slate; supporting controls. |
| `onSecondary` | `#FFFFFF` | `#10151C` | |
| `tertiary` | `#0E7C86` | `#46C7D0` | Teal = **semantic info only**, not a second brand. |
| `error` | `#C2362E` | `#FF8A7E` | |
| `onError` | `#FFFFFF` | `#5A0A05` | |
| `background` | `#F6F6F4` | `#0E1014` | Warm-neutral page (light) / near-black (dark), **never pure #000**. |
| `onBackground` | `#16181D` | `#EDEFF3` | |
| `surface` | `#FFFFFF` | `#161A20` | Card surface. |
| `onSurface` | `#16181D` | `#EDEFF3` | Primary text. |
| `onSurfaceVariant` | `#535D6B` | `#A6AFBC` | Secondary/label text. **Use this (not primary-tint) for the smallest secondary numerics** (small-glyph legibility in sunlight). |
| `outline` (control/input border) | `#8A909B` | `#707A86` | **Darkened** to clear ≥3:1 on `surface` (the audit's `#A7A9B0`=2.35:1 failed). Dark `#5A626E` ALSO failed at 2.83:1 on `#161A20`; replaced with `#707A86` = 4.01:1. Two-tier split below. |
| `outlineVariant` (divider) | `#D8DAE0` | `#2A303A` | Hairline dividers between rows/sections. |

### Surface container ladder (the dark fix)

The current dark mode collapses `#161B22` across `surface` == `surfaceContainer` (and near-dupes elsewhere) so cards, sheets, menus, dialogs read as one flat layer. **Fixed below** — every step ≥ ~5 luminance points apart, no two tokens equal.

| Role | Light | Dark | Dark step use |
|---|---|---|---|
| `surfaceContainerLowest` | `#FFFFFF` | `#0B0D11` | Recessed wells. |
| `surfaceContainerLow` | `#FBFBFA` | `#13161B` | |
| `surface` | `#FFFFFF` | `#161A20` | Resting card. |
| `surfaceContainer` | `#F2F2F0` | `#191D24` | Nav bar / rail surface. |
| `surfaceContainerHigh` | `#ECECEA` | `#222730` | Segmented-tab track; menus. |
| `surfaceContainerHighest` | `#E4E4E2` | `#2C323C` | Pressed states; dialogs; active dark tab pill. |

**Crushed-black fallback rule (field-engineering hardening):** on cheap in-vehicle panels with crushed blacks the lowest steps (`#0B0D11` / `#13161B` / `#161A20`) may merge. So **dark mode does not rely on tone alone** — every elevated card/sheet/menu also carries a faint **1px top highlight (white ~6%)** as a guaranteed-visible layer affordance even when steps collapse. **Bump the highlight to ~10% on the lowest two dark steps (`#0B0D11` / `#13161B`)** where crush is worst. The exact per-step highlight % is a DDD visual-tuning call (6% default elsewhere, ~10% on the crushed wells).

### Semantic status set (themed, not raw swatches)

Promote the old raw `FleetColors` Material swatches and `FleetStatusColors` into proper themed tokens with `on*`/`container` pairs in both modes. Used **only** in chips, dots, left-accent bars.

| Semantic | Light fg / container | Dark fg / container |
|---|---|---|
| success | `#1A6E35` / `#D5EBDD` | `#5FD08A` / `#16351F` |
| warning | `#855900` / `#F6E6C8` | `#E8B24A` / `#3A2E10` |
| error | `#B12F28` / `#F7DEDC` | `#FF8A7E` / `#3A1512` |
| info | `#0B6A73` / `#D2EEF0` | `#46C7D0` / `#10353A` |

*Note: the LIGHT fg column was darkened — the prior values (warning 3.96, info 4.06, success 4.24, error 4.26) all failed AA on their containers. New values each clear ≥4.5:1 (target ≥5.5:1). DDD may tune the exact swatch hue/feel but **must keep ≥4.5:1 (target ≥5.5:1)**.*

Vehicle/driver/trip state colours map onto this semantic set (e.g. active→success, maintenance→warning, in-progress→info), each as a **pill chip**, never a card tint.

---

## 4. Depth / elevation + new FleetTokens

**Depth order: tonal-first, shadow-second, border-third.**

- **Light:** warm page (`#F6F6F4`) sits below white cards (`#FFFFFF`); resting cards add a soft low-opacity shadow (~16dp blur, ~2dp y, ~8% black) instead of a hard border. Dialogs/modals lean on tone + shadow.
- **Dark:** depth comes from the stepped ladder (higher elevation = lighter) **plus** the faint 1px top highlight; drop shadows minimized (they barely register on near-black). `Elevation.Card` becomes a 1dp tonal-backed surface; dialogs lean on `surfaceContainerHigh`/`Highest` tone, not 8–16dp shadows.

**`FleetTokens` changes:**

- **`Radius`** — consolidate to `M=8`, `L=12`, `XL=16`, `Pill=full`. Retire `ML=10` half-step and `XS=2`/`S=4` for cards/controls.
- **New `Shape`** — `Button=Radius.M (8)`, `Chip/Pill=Radius.Pill`, `Card=Radius.L (12)`, `Sheet=Radius.XL (16)`.
- **New `Motion`** — `FastSpring ≈180ms` (sliding pill), `Standard ≈220ms` (nav/page). Standard easing; cap pill ≤200ms.
- **New `StateLayer` alphas** — hover 8% / pressed 12% / focus 12% / selected 12% / disabled-content 38%.
- **New `Focus` ring token** — a 2dp `onSurface`/`primary` outline ring drawn around the focused element, ≥3:1 against **both** adjacent surfaces. The focus indicator is this ring, **not** the 12% focus state-layer (a tinted overlay can dip below the 3:1 non-text-contrast floor).
- **New `Divider` token** — uses `outlineVariant`; thickness 1dp.
- **New tabular-figures number token** — font-feature `tabular-nums` for all financial numerics.
- **Two-tier border** — `outline` (control/input border, ≥3:1) vs `outlineVariant` (divider). Named pair; validated separately — **both tiers validated in BOTH light and dark.**
- **New `FleetElevatedSurface` modifier** — draws the clipped 1px top-edge highlight via `Modifier.drawWithContent`, respecting the `RoundedCornerShape`. Route **ALL** elevated surfaces — including menus and dialogs (`FleetMenu`/`FleetDialog` wrappers) — through it, because Material3 `Card`/`Surface`/`ModalBottomSheet`/`DropdownMenu`/`Dialog` expose no top-edge stroke.
- **`Elevation`** re-tuned away from the MD2 shadow ramp toward tonal backing.
- **New `Contrast` floor constant** — encodes the sunlight-first mandate: body/label ≥ 4.5:1, large/UI ≥ 3:1, and a *target* push of ≥5.5:1 light / ≥7:1 dark for primary body text. Verified values (a generated WCAG contrast report — one canonical relative-luminance routine over every §3/§4 pair — is the authority; inline numbers are CI-gated against it): indigo fill clears AA, dark body text `#EDEFF3` on `#161A20` = 15.17:1, `onSurfaceVariant` light `#535D6B` on `#FFFFFF` = 6.68:1.

Keep verbatim: the `FleetTokens` single-source architecture, full M3 `colorScheme` plumbing, 8pt `Spacing`, 44dp touch/AA `Height` tokens, `Border` tier, runtime dark toggle, `MaxContent=640`.

---

## 5. Typography

**Retire Poppins.** Adopt one metric-compatible super-family: **Noto Sans (Latin) + Noto Sans Devanagari** — same family/weights so EN and HI rows never misalign. Poppins' wide geometry hurt 12–14sp body and number-dense tables.

- **Ramp (FleetTokens type roles):** display 30 / headline 24 / title 20 / body 16 / body-small 14 / label 12. No poster 52/42/34sp display, no tight negative tracking.
- **Weight hierarchy:** Regular(400) body, Medium(500) labels/titles, SemiBold(600) headlines + KPI numerics. Retire the Bold/ExtraBold near-uniform shouty scale. **Bottom-bar, tab and chip labels use the label (12sp) Medium role — never `labelSmall`/10sp/Bold;** `letterSpacing` 0 on ALL Devanagari text (no positive tracking — it detaches matras from their base glyph). *(Current `Theme.kt` `labelSmall` is Bold/10sp/0.4sp and `labelMedium` Bold/12sp/0.3sp — both retired.)*
- **Devanagari rules (critical):** global `lineHeight ≈ 1.45×` (tall ascenders/descenders + shirorekha clip otherwise); **sentence case everywhere, never ALL-CAPS** (caps don't exist in Devanagari); **no italics** for emphasis — use weight/colour. **Set `LineHeightStyle(alignment=Center, trim=None)` on every type role, and `PlatformTextStyle(includeFontPadding=false)` on Android via `expect`/`actual`;** bundling the Noto font is necessary but **NOT sufficient** for EN/HI metric parity (line-height distribution differs between Android's text path and the Skia path on iOS/Web).
- **Money & metrics:** dedicated tabular/lining-figures token for all financial numerics (P&L, costs, Payments, Reports columns align). KPI numbers SemiBold + large with small `onSurfaceVariant` supporting labels (Motive KPI-card pattern).
  - **(a) Table/column numerics** (P&L, costs, Payments, Reports) use a **full grouped form** with Indian comma grouping (`₹2,50,000`) so `tabular-nums` actually align in a column.
  - **(b) Compact KPI badges** MAY abbreviate (lakh/crore) but **MUST localize the suffix via string resources** (करोड़/लाख/हज़ार), never hardcoded English Cr/L/K.
  - *Note: the live `formatCurrency()` emits hardcoded English `₹X Cr/L/K` today — it must be split into `formatCurrencyFull` (grouped) for columns and a localized-suffix compact form for badges.*
  - **Western Arabic digits in both locales:** all financial numerics render Western Arabic digits (0–9) in BOTH locales; force the numeric run to **Latin Noto Sans + `fontFeatureSettings="tnum"`** via an explicit `SpanStyle` so digits never inherit the Devanagari family (Devanagari digits have different advance widths and would break column alignment). Verify the shipped Latin Noto weights actually carry the `tnum` GSUB feature, and snapshot-test digit-column alignment on Android/iOS/Web.
- **Bilingual test rule:** test every nav/tab/chip/button label at Hindi length; allow 2-line wrap rather than truncate.
- **Bundling (risk):** ship Noto weights in `composeResources/files/` and load composably (same mechanism as current Poppins) so iOS/Web render identical metrics — do not rely on system fonts, or EN/HI baselines drift.

---

## 6. Segmented tab spec

Replaces the underline `FleetTabBar`. **Preserve** the `FleetTab<T>` data API, adaptive behavior, accessibility (contentDescription, badge slot) — only the indicator/container rendering changes. **This is a rebuild, not a re-skin** (see §10 step 3): the component is rebuilt on a custom layout rather than re-skinning Material3 `Tab`/`TabRow` internals.

- **Anatomy:** rounded track in `surfaceContainerHigh` (light `#ECECEA` / dark `#222730`) at `Radius.Pill`, holding equal-width segments separated by `Spacing.XXS` (2dp). Active segment = raised pill in `surface` (light `#FFFFFF` / dark `surfaceContainerHighest #2C323C`) at `Radius.Pill`, soft 1–2dp shadow (light) / 1px top highlight (dark). Each segment min 44dp tall, horizontal padding `Spacing.M` (12dp); optional 16dp icon + label.
- **Rest state:** inactive segments flat/transparent, label `onSurfaceVariant`, **constant `FontWeight.Medium`** (no weight toggle — that caused documented reflow jitter).
- **Selected state:** raised pill, label `onSurface` at constant Medium, icon (if present) tinted `primary`. Selection signalled by pill background + colour, never weight. Pressed: 12% `onSurface` state-layer on the touched segment; active pill scales ~0.98 on press.
- **Motion:** active pill slides between segments — `Motion.FastSpring` ~180ms, animate x-offset **and width together** (Hindi segments differ in width), cap ≤200ms. Label colours crossfade over the same duration.
- **Badge:** **neutral count badge** — `surfaceContainerHighest` fill + `onSurfaceVariant` text. Error red and primary-tint are **reserved strictly for true-alert tabs**; plain counts stay neutral. Badge trails the label; on narrow Hindi labels it wraps with the 2-line label rather than truncating.
- **Scroll:** the fallback is driven by a **MEASURED condition on ALL breakpoints**: `>4 segments OR (subcomposed segment intrinsic widths + gaps > available width)` ⇒ a **horizontally-scrollable pill row** (content-sized segments, min-width); treat `>4` as a fast-path heuristic only. This must drive **Medium/Expanded too — not just Compact** — or the 6-tab Hindi Vehicle Detail (Overview/Trips/Costs/Route/Documents/History) crowds on tablet/web as it does today (previously force-distributed and crowded). **Bilingual rule:** never fix segment width to English text — size to content with a min-width; let the active pill grow. Allow 2-line wrap, never clip.

---

## 7. Bottom nav + rail spec

Kills the hamburger drawer entirely; its destinations move into bar + More sheet (Compact) and the rail (Medium/Expanded).

- **Bottom-bar items (Compact):** Home (Dashboard) · Trips · Live Map · Payments · **More** (sheet: Vehicles, Drivers, Customers, Team, Vehicle Finance, Reports, Profile).
- **Labels:** ALWAYS visible, both states, every item. 24dp icon (`IconSize.Default`) above label; bar sized for 2-line Hindi wrap (Hindi runs ~15–30% taller). **Bar content height = `wrapContent` column with a min-height token; budget ≈80dp for 2-line Hindi plus bottom safe-area inset. 64dp is the EN single-line floor, not the Hindi target.** Let it grow, never clip. Touch target ≥44dp per item.
- **Selected treatment:** filled icon + `primary` colour + soft active-slot pill in `primaryContainer` (light `#E1E4FF` / dark `#2C36A6`) behind the icon. Inactive: outline icon + `onSurfaceVariant`. **Dark selected nav icon is tinted with `onPrimaryContainer` `#E1E4FF` (7.66:1 on `#2C36A6`), NOT `primary` `#9AA6FF`.** Bar/rail surface `surfaceContainer` (light `#F2F2F0` / dark `#191D24`) with a hairline top divider (bar) / trailing divider (rail) + subtle elevation.
- **Motion:** tap crossfades icon outline→filled and colour over ~180ms; active-slot pill animates in with `FastSpring`. No icon bounce. Page transition `Standard` ~220ms.
- **Rail (Medium/Expanded):** new **`FleetNavRail`**, driven by `FleetBreakpoint`, shared across all screens (fixes the drawer-only/no-rail defect). Medium = compact rail (icon + short label stacked, ~80dp wide); Expanded (web/tablet) = expanded rail (full labels beside icons, sized for Hindi, never fixed to English width). Same IA vertically; **never a bottom bar on Expanded.** Persistent active-section highlight for the current screen (fixes the old drawer passing `selected=false` everywhere). Rail surface `surfaceContainer` with hairline trailing divider. **The chrome (bar-vs-rail) swap reads the WINDOW size class (`currentWindowAdaptiveInfo`, or a single root-level `BoxWithConstraints`), NEVER a mid-tree container breakpoint; the container-scoped `rememberFleetBreakpoint` (which reads container `maxWidth` by design) is reserved for in-content adaptivity and would mis-flip the shell.**
- **Permission-gating:** all items permission-driven. Hidden items collapse gracefully; the bar **backfills its top 4 from the permitted set** so it never shows gaps or shifts under the user (e.g. a manager lacking Payments or Live Map). Test these permutations explicitly.
- **Nav-visibility gating model (PO decision):** **INTERIM (ships now, app-side only, no cross-service dependency):** a destination is visible if the user holds ANY permission in its domain; Live Map and Payments are visible to all authenticated users (core daily screens). Nav visibility is UX-only — the backend still enforces every endpoint, so this never weakens security. The bar backfills its top-4 from the visible set. **TARGET (requested from IAM in parallel, not a blocker):** explicit read-scoped nav permissions (`trips:read`, `vehicles:read`, `payments:read`, `customers:read`, `live_map:view`); when IAM issues them, gating migrates to explicit read perms.

---

## 8. Refreshed button / card / top-bar notes

- **Buttons:** explicit `Shape.Button` (8dp), branded indigo fills, pressed-state polish via `StateLayer`. **Keep** the variant/size enum API and the `RowScope.weight`-on-root fix (apply caller modifier to the `BoxWithConstraints` root, not the inner button, or side-by-side buttons collapse). Verify 12sp label-on-primary contrast — bump weight where buttons use 12sp labels. For small glyphs on white, use `onSurface`-tinted icons, not `primary` (thin primary graphics can dip below 3:1).
- **Cards:** consolidate legacy `CardComponents` (`FleetItemCard`/`FleetStatusBadge`/`FleetIconAvatar`, currently raw dp + boxy 4dp badges) into the token-driven `FleetSectionCard` system. Badges → pill radius, unified ~0.12–0.15 alpha. Remove the vestigial no-op `FleetCard` wrapper or give it real chrome. **Consolidation scope also covers menus and dialogs** — route `FleetMenu`/`FleetDialog` wrappers through `FleetElevatedSurface` so they carry the 1px top-edge highlight (§4).
- **Chips:** unify the four divergent selected-chip styles (primaryContainer vs secondaryContainer vs ad-hoc alphas) to ONE primary-tinted selected token matching the segmented tab.
- **Top bar:** `Height.TopBar` (64dp), `surface`, hairline `outlineVariant` bottom divider, title in title/SemiBold. No ALL-CAPS. Actions use `onSurface` glyphs. **Label note:** bottom-bar, tab and chip labels use the label (12sp) Medium role — never `labelSmall`/10sp/Bold; `letterSpacing` 0 on ALL Devanagari text (no positive tracking — it detaches matras from their base glyph).

---

## 9. Acceptance criteria

1. No component contains a raw hex or dp literal; all colours via `colorScheme.*`, all dims via `FleetTokens`.
2. Dark surface ladder has **6 distinct, strictly monotonic rungs** (no two equal, no inversion; the `#161B22` collapse is gone). Adjacent rungs separate by **EITHER ≥5 L\* tonal, OR** — where tonal separation is <5 L\* (the crushed near-black middle) — the upper/elevated surface MUST carry the verified 1px `FleetElevatedSurface` top-highlight (measured edge spike ≥ ~+4 lum). Recessed wells (Lowest/Low) are exempt from the highlight requirement (they read recessed by context). *(Aligns §9.2 with §4 — "dark mode does not rely on tone alone." Verified: middle rungs measure ~1.5–4.9 L\* apart, carried by the measured highlight; strict ≥5-per-rung is physically impossible in near-black without lifting elevated surfaces to gray, against §1.)*
3. Every elevated dark card/sheet/menu/dialog shows the faint 1px top highlight (crushed-black fallback), drawn via `FleetElevatedSurface`; the highlight bumps to ~10% on the lowest two dark steps (`#0B0D11`/`#13161B`).
4. Contrast: body/label ≥ 4.5:1, large/UI ≥ 3:1; primary body text ≥5.5:1 light / ≥7:1 dark (verified, sunlight-first floor). `outline` control border ≥3:1 on `surface`.
   - `cr(outline, surface)` ≥ 3:1 in light AND dark — unit-tested.
   - All semantic fg-on-container pairs ≥4.5:1 in light AND dark.
   - Focus indicator is a ≥3:1 ring, not the 12% state layer.
5. Dark `onPrimary` is `#10165A` on `#9AA6FF` (no white-on-light-indigo anywhere).
6. Every nav/tab/chip/button label renders correctly at Hindi length with 2-line wrap, no clip; line-height ≥1.45×; sentence case; no ALL-CAPS; no italics. **Hindi-length snapshot shows no clip — not a fixed dp.**
7. Financial numerics use the tabular-figures token and align in P&L/costs/Payments/Reports; ₹ uses lakh/crore grouping. **Column numerics use the grouped full form, not abbreviations.**
8. Segmented tab: constant Medium weight (no reflow), sliding-pill ≤200ms animating width, neutral count badge; scroll fallback proven on the 6-tab Hindi Vehicle Detail **at Medium width too, not just Compact**.
9. Bottom bar shows labels on all items both states; rail replaces it on Medium/Expanded with a persistent active highlight; no drawer remains. **Hindi-length snapshot shows no clip — not a fixed dp.**
10. Nav-visibility gating (INTERIM rule): with a permission-limited role, a destination is visible if the user holds ANY permission in its domain, and Live Map + Payments are visible to all authenticated users; the bottom bar backfills its top-4 from the visible set and shows 4 valid items + More, with no gaps and no shift. **At Hindi length, assert the widest/tallest backfillable labels (वाहन वित्त, ग्राहक) still wrap to 2 lines without clip in all slots.**
11. Status colours appear only as chips/dots/accent bars — no full-card tints (20-row trip list stays neutral).
12. EN and HI rows align: render an EN row and an HI row of the **same type role side-by-side; assert equal baseline on Android, iOS and Web (snapshot test)** (Noto super-family bundled, plus `LineHeightStyle(trim=None)` + `includeFontPadding=false`).

---

## 10. Implementation order (engineering)

1. **Tokens foundation** — rewrite `Color.kt` (core roles + 6-step dark ladder + semantic set), add `FleetTokens.Shape`, `Motion`, `StateLayer`, `Focus` ring, `Divider`, tabular-number token, `Contrast` floor; consolidate `Radius`; re-tune `Elevation`. Wire into `Theme.kt`. *(Everything downstream depends on this.)*
   - When retiring `Radius.ML/XS/S`, rewrite `FleetShapes`' five buckets (`extraSmall`/`small`/`medium`/`large`/`extraLarge` in `Theme.kt`) to the surviving `{M,L,XL,Pill}` set and audit every Material component that inherits `MaterialTheme.shapes` for appearance change (dangling refs otherwise fail compile or leave components on stale buckets).
2. **Typography** — bundle Noto Sans + Noto Sans Devanagari in `composeResources/files/`, load composably, apply ramp + weight hierarchy + 1.45× line-height + tabular token. Verify EN/HI metric parity on all three platforms.
3. **Segmented tab** — **REBUILD `FleetTabBar` on a custom `SubcomposeLayout`** (or `Row` of measured children); render the pill as the layout's own background `Box` behind the segments; own the press state-layer + 0.98 scale and the sliding-pill spring; keep the `FleetTab<T>` data API, badge slot and `contentDescription` unchanged. Drop the Material3 `Tab`/`TabRow` dependency for this component (`TabRow`'s indicator slot can't clip the pill shadow/highlight, supplies its own ripple, and `ScrollableTabRow` forces `edgePadding`/min-width/centering). Run the segment intrinsic-width **measurement pass** that drives the scroll fallback (§6). Test 6-tab Hindi Vehicle Detail at Compact AND Medium width.
4. **Buttons / chips / cards** — apply `Shape`/`StateLayer`, unify chips to one selected token, consolidate cards into `FleetSectionCard`, keep button enum API + weight-on-root fix.
5. **Nav migration (largest)** — build `FleetNavRail` (Medium/Expanded) + refactored bottom bar + More sheet (Compact) in `sharedUI`; remove the hamburger drawer from `App.kt` shell; wire persistent active state and permission-driven backfill. Test permission permutations.
   - **Interim gating is app-side; track the IAM read-perm request separately — do not block nav migration on it.**
6. **Status-colour refactor (wide, mechanical)** — migrate `FleetStatusColors` and vehicle/driver/trip state usages to themed semantic chips; grep for stray raw-swatch call sites so no screen keeps the old rainbow.
7. **Audit pass** — run acceptance criteria §9 across light/dark, EN/HI, Compact/Medium/Expanded, including cheap-LCD/sunlight and crushed-black checks. A generated WCAG contrast report (one canonical relative-luminance routine over every §3/§4 pair) is the authority; inline numbers are CI-gated against it.

*Note: §6 (status refactor) is a wide cross-module change — risk of missed raw-swatch usages; treat the grep audit as a gate. The nav step is a large IA migration touching every screen's chrome — land it behind the breakpoint switch and test permission backfill before merge.*

---

## 11. Open visual calls for DDD (within these constraints)

The following remain the designer's judgment, but are **bounded by this contract**:

1. **Exact light-semantic swatch hue/feel** — keep ≥4.5:1 (target ≥5.5:1); the §3 values are passing defaults, tunable in hue/feel only.
2. **Dark 1px top-highlight % per elevation step** — 6% default, ~10% on the crushed `#0B0D11`/`#13161B` wells; tune on real in-vehicle panels.
3. **Per-role line-height** — 1.45× for body/paragraph Devanagari, ~1.2–1.3× for single-line labels/numerics, `trim=None` throughout.

**Not DDD's to change:** the nav-gating model (§7) and all contrast floors (§3/§4/§9) are PO-decided.
