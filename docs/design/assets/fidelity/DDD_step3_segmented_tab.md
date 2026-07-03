# DDD Fidelity Pass — Step 3 (Segmented Tab Rebuild) · VERDICT

> Verifies §6 + D1 + B1/B2. Evidence: 6 screenshots (vehicle-detail-tabs en/hi × light/dark, -scrolled, driver-detail-fixed) + SOURCE review of `ijs-ui-components-lib/.../components/FleetTabBar.kt`.

## Screens reviewed
- `vehicle-detail-tabs-en-light` / `-hi-light` / `-en-dark` / `-hi-dark` — 6-tab (Overview/Trips/Costs/Route&Stops/Documents/History) scroll case.
- `vehicle-detail-tabs-en-light-scrolled` — mid-scroll (active=Documents visible, History reachable).
- `driver-detail-tabs-en-light-fixed` — 3-tab fixed/distributed case.

## VERDICT — ✅ PASS (2 non-blocking nits)

### Confirmed (visual + SOURCE — every redline)
- **Rebuild, not TabRow** — custom Box/Row + `Animatable` pill; Material3 `Tab`/`TabRow` dropped. ✅
- **B1 (glare fallback) — DONE:** light track = `surfaceContainerHighest` (#E4E4E2; pixel-measured ~228 lum) AND a 1px `outlineVariant` hairline on the active pill (`BorderStroke(Border.Hairline, outlineVariant)`, applied `.border(...)`). Both parts present. Dark pill = `surfaceContainerHighest` #2C323C on `surfaceContainerHigh` track.
- **Constant weight (no reflow)** — label always `FontWeight.Medium`/`labelMedium`; selection = `onSurface`↔`onSurfaceVariant` colour crossfade, never weight. ✅
- **Sliding pill x+width** — both `animateTo` on a `tween(FastSpringMillis=180, FastOutSlowIn)`, capped < PillCapMillis(200). ✅
- **Press** — 0.98 scale on the active pill + 12% `onSurface` state layer on the touched segment. ✅
- **B2 — DONE:** active segment auto-scrolls centered on the measured viewport; right-edge fade (`Brush.horizontalGradient → trackColor` over `Spacing.L`) gated on `canScrollForward` (subtle — fades to track, so near-invisible in stills but present in code).
- **Dark pill highlight** — routed through `.fleetElevatedSurface(pillShape, Pill)` for the C2 top-highlight. ✅
- **Neutral badge** — `surfaceContainerHighest` fill + `onSurfaceVariant` (not error-red). ✅
- **Scroll vs fixed** — `>4` ⇒ scroll, content-sized `widthIn(min=72dp)`; ≤4 ⇒ `weight(1f)` equal-distribution (confirmed: 6-tab Vehicle Detail scrolls; 3-tab Driver Detail distributes).
- **Bilingual** — Devanagari renders in the active pill (अवलोकन) and all segments, light AND dark, no clip; segments content-sized (not fixed to EN width); 6-Hindi-tab row scrolls. `maxLines=2` + `TextOverflow.Clip` (wrap, snapshot catches overflow).
- **A11y** — `Role.Tab`, `selectable(selected)`, `contentDescription`, `heightIn(min=MinTouchTarget 44dp)`. ✅

### NIT 1 (carry-forward, non-blocking) — MEASURED-overflow trigger deferred
`scrollable = tabs.size > 4` only. §6 requires the fallback be MEASURED on all breakpoints (`>4 segments OR intrinsic seg widths + gaps > available`), treating `>4` as a fast-path heuristic. The code comment flags this as TODO. **Impact:** the §9.8 acceptance case (6-tab Vehicle Detail) is covered, but a **≤4-tab segmented control with long Hindi labels (or large font-scale) could CLIP** instead of scrolling — e.g. the period / group-by / P&L selectors at Hindi length. **Ask:** implement the measured clause, and verify the ≤4-tab segmented selectors at Hindi length + large font don't clip.

### NIT 2 (trivial) — stale comment
Doc-comment (line ~88) calls the dark-pill highlight a "TODO step 4," but line ~208 already applies `fleetElevatedSurface`. Clean up the comment.

**No FAILs.** The segmented tab is faithful to §6/D1 and both my B1/B2 refinements are implemented as specified.
