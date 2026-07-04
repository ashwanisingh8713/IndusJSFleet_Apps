# f2 — Segmented tab-track: geometry / tone / float ruling (DDD)

> **✅ IMPLEMENTED + VERIFIED 2026-07-03** (`f2-tabtrack-BEFORE/AFTER-driver-details-light.png`). Pixel-confirmed: mid-track `#E4E4E2`→`#F2F2F0` (tone), AFTER far-left edge = page-bg `#F6F6F4` (inset), visible float gap under the app bar. The full-width grey band is gone; the track now reads as a contained, inset, whisper-soft floating pill. Owner complaint resolved. Dark unchanged. Active pill still clearly selected (hairline+shadow). B built it in one place (FleetTabBar) → app-wide.

> **Owner complaint:** light-mode segmented-tab TRACK reads as an **ugly full-width grey band** (Driver Details). One fix in `FleetTabBar` = app-wide.
> **PO asked me to rule on:** (a) inset geometry, (b) track tone, (c) float-vs-dock. Ruling below. Stays within PO-locked contrast floors.

## Root cause
The track is `surfaceContainerHighest` **#E4E4E2** rendered **full-bleed edge-to-edge** → a heavy grey slab. Two compounding causes: (1) it spans the full width with no side inset, so it reads as a structural band, not a component; (2) **my own B1 change deepened the track** from `#ECECEA` to `#E4E4E2` to widen the pill/track delta — that made the band heavier. B1 also added the **1px `outlineVariant` pill hairline**, which is the *real* glare affordance (a 1px stroke survives sunlight better than a tonal delta). So the track no longer needs to be dark — the hairline carries the selected-pill separation, and the track can lighten.

## RULING

### (a) Inset geometry — CONTAINED, not full-bleed ✅
- The track is an **inset contained control**, NOT edge-to-edge. Apply **`ScreenHorizontal` side margin (`Spacing.L` = 16dp)** so it sits inboard of the screen edges with breathing room on both sides.
- Keep **`Radius.Pill`** (rounded ends) — already correct.
- Net: a contained rounded pill-shaped control reads as a *component*, which alone kills most of the "band" perception.

### (b) Track tone — LIGHTEN to a whisper ✅
- Light: **`surfaceContainerHighest #E4E4E2` → `surfaceContainer #F2F2F0`** (a barely-there warm gray, ~1 step off the `#F6F6F4` page). This retires the "heavy grey."
- **Selected-pill separation is preserved by the pill's 1px `outlineVariant #D8DAE0` hairline + soft shadow** (B1), which is independent of track tone — so lightening the track does NOT weaken selection or breach the sunlight-first floor. *(This updates my B1 track-deepening: the hairline, not the track darkness, is the glare-robust cue.)*
- **Dark: unchanged** — keep `surfaceContainerHigh #222730`. The complaint is light-mode-specific; dark tracks don't read as slabs.

### (c) Float-vs-dock — FLOAT ✅
- The tab bar **floats** below the top app bar: a **`Spacing.M` (12dp) gap** above the track (separating it from the app-bar divider) and a matching gap below before content. With the ScreenHorizontal side inset, it reads as a floating contained control, **not docked** flush to the app bar (docking is what makes it span the width as a structural band).

## Net FleetTabBar change (one component, app-wide)
```
track:  fill  surfaceContainer #F2F2F0 (light) / surfaceContainerHigh #222730 (dark)   [was surfaceContainerHighest]
        shape Radius.Pill                                                              [unchanged]
        margin ScreenHorizontal = Spacing.L (16dp) each side  + Spacing.M (12dp) top gap (float)   [NEW — was full-bleed]
pill:   surface #FFFFFF (light) / surfaceContainerHighest #2C323C (dark)               [unchanged]
        + 1px outlineVariant #D8DAE0 hairline (light) + soft shadow / 4% top-highlight (dark)  [unchanged — carries separation]
```
Scroll case (§6) is unaffected: the inset + float apply to the track container; the horizontally-scrollable pill row still lives inside it.

## Acceptance
- Light Driver Details: the track is a **contained, inset, whisper-soft** pill control with a clear floating gap under the app bar — no full-width grey band.
- The active pill still reads clearly selected (hairline + shadow), verified in glare — B1 intent intact.
- Dark unchanged. `FleetBreakpoint` behavior unchanged. Before/after light Driver Details shot → `fidelity/`.

## Addendum — stat-tile refinement (PO invited, optional, f7)
PO's hairline-tile call for the grey stat tiles is **consistent with this f2 direction** (retire heavy grey fills → lighter/contained). I **endorse hairline tiles** as the safe, system-coherent choice. If PO wants to go further on the *hero KPI row* specifically (Fleet Overview counts), **boxless** is the more premium option — big `onSurface` numeral + muted `onSurfaceVariant` label separated by generous whitespace and a thin `outlineVariant` vertical divider between the 3 stats, **no box at all** (the Ramp/Mercury register). Boxless removes fills entirely — within floors, nothing to contrast-check. **PO's call**; hairline is fine, boxless is more premium. Not blocking f2.
