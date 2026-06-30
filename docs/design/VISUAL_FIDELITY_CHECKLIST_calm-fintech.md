# Visual-Fidelity Checklist — Calm Fintech (DDD pass)

> **Use:** DDD runs this against B's "design refresh" build. Scope = **fidelity to design** only.
> Behavior / permission-enforcement / i18n-function correctness = **D's** regression pass (overlap noted).
> Authority: `DESIGN_DIRECTION_2026-06_calm-fintech.md` (v2) + `DESIGN_SPEC_2026-06_calm-fintech_DDD.md` (PO-approved) + `assets/*.svg`.
> Run every row in the **4 matrices**: Light · Dark × EN · HI, and at **Compact · Medium · Expanded**.

## 1. Tokens & palette (vs `color-tokens.svg`)
- [ ] No raw hex/dp literal in any component (grep gate). All colour via `colorScheme.*`, dims via `FleetTokens`.
- [ ] `primary` indigo `#3D4EDB` light / `#9AA6FF` dark; old `#1976D2`/teal/orange gone from UI.
- [ ] Dark surface ladder shows **6 visibly distinct** steps (no `#161B22` flat collapse) on a real/emulated panel.
- [ ] Dark `onPrimary` is `#10165A` on `#9AA6FF` — **no white-on-light-indigo** anywhere (buttons, chips, FAB).
- [ ] Semantic colours appear **only** as chips/dots/accent bars — **no full-card tints** (scan a 20-row trip list: stays neutral).
- [ ] Semantic swatches match C1 (success `#15683A`/`#DCEDE3`, warning `#855900`/`#F5E7CB`, info `#0B6A73`/`#D3EDEF`, error `#B0302A`/`#F7DEDC`).

## 2. Depth / elevation (vs §4 + C2)
- [ ] Every elevated dark card/sheet/menu/dialog carries the **1px top highlight** via `FleetElevatedSurface`.
- [ ] Highlight intensity tracks C2 per step (≈10% on `#0B0D11`/`#13161B` wells, tapering to 4% on Highest) — visible but not a glowing edge.
- [ ] Light cards use soft shadow (not hard border); dialogs lean on tone+shadow.
- [ ] Focus indicator is the **2dp ≥3:1 ring**, not the 12% tint overlay.

## 3. Segmented tab (vs `seg-tab-spec.svg`)
- [ ] Underline `TabRow` is **gone**; pill-on-track rendered.
- [ ] **B1 locked:** active pill has 1px `outlineVariant` `#D8DAE0` hairline (light) + track is `surfaceContainerHighest` `#E4E4E2`; dark pill `#2C323C` + 4% top highlight.
- [ ] Constant `FontWeight.Medium` — **no weight-toggle reflow** when switching tabs.
- [ ] Sliding pill animates **x + width together**, ≤200ms; labels crossfade.
- [ ] Pressed = 12% state layer + 0.98 scale on touched segment.
- [ ] Badge is **neutral** (`surfaceContainerHighest`/`onSurfaceVariant`), not red — unless a true-alert tab.
- [ ] **6-tab Hindi Vehicle Detail:** scroll fallback engages at **Compact AND Medium**; no clip; active auto-scrolls into view; right-edge fade present (B2).

## 4. Bottom nav + rail (vs `bottom-nav-rail.svg`)
- [ ] Hamburger drawer **removed** from shell; bottom bar (Compact) present.
- [ ] Labels **always visible** both states; 24dp icon above label; bar grows for 2-line Hindi (no clip), ≈80dp budget.
- [ ] Selected = filled icon + `primary` label + `primaryContainer` slot pill; **dark selected icon = `onPrimaryContainer #E1E4FF`**, not `primary`.
- [ ] Bar↔rail swap reads **window size class** (not container breakpoint): Compact=bar, Medium=compact rail, Expanded=full rail. **Never a bottom bar on Expanded.**
- [ ] Persistent active highlight reflects current screen (no all-`selected=false`).
- [ ] IA = Home · Trips · Live Map · Payments · More; More sheet holds the rest.

## 5. Components (vs §8)
- [ ] Buttons: `Shape.Button` 8dp, indigo fills, `StateLayer` press; side-by-side buttons don't collapse (weight-on-root fix intact).
- [ ] Chips: one primary-tinted selected style (matches active tab pill) — the 4 divergent styles unified.
- [ ] Cards consolidated to `FleetSectionCard`; badges pill-radius; menus/dialogs carry the top highlight.
- [ ] Top bar: `surface`, hairline `outlineVariant` divider, title/SemiBold, **no ALL-CAPS**.

## 6. Typography (vs §5 + C3) — fidelity aspects
- [ ] Poppins gone; Noto Sans + Devanagari rendering (bundled, not system).
- [ ] No ALL-CAPS, no italics; sentence case throughout.
- [ ] EN row + HI row of same role have **equal baseline** side-by-side (snapshot) — Android, iOS, Web.
- [ ] Per-role line-heights match C3; Devanagari shirorekha/matras not clipped.
- [ ] Money numerics: tabular figures align in P&L/costs/Payments/Reports columns; grouped `₹2,50,000` (not abbreviations) in columns; compact badges localize suffix.

## 7. Cross-cutting acceptance (spot-check vs §9)
- [ ] Bright-sunlight / cheap-LCD readability spot-check (primary fills, `onSurfaceVariant`, active pill separation in glare).
- [ ] Crushed-black spot-check: layer affordance survives via top highlight.
- [ ] Permission backfill (manager lacking Payments or Live Map): bar shows 4 valid items + More, no gap/shift, Hindi labels still 2-line-wrap clean. *(behavioral half = D; I check the visual result.)*

---
**Verdict format:** PASS / PASS-WITH-NITS / FAIL per section, with screenshot deltas vs the matching `assets/*.svg`. Route fidelity gaps to B via PO; flag any spec ambiguity I find back to PO.
