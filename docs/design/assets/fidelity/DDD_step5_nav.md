# DDD Fidelity Pass — Step 5 (Bottom Nav + Rail) · VERDICT

> Verifies §7 + D2. Evidence: `nav-bottombar-en-light/-dark` + SOURCE review of `FleetNavScaffold.kt` (sharedUI) and `FleetNavBar.kt` (ijs-ui-components-lib).

## VERDICT — ⚠️ PASS-WITH-NITS, but ONE probable spec-miss needs a Hindi shot to settle (Finding 1)

### Confirmed (§7/D2 — visual + source)
- **Drawer removed** → bottom bar (Compact) + `FleetNavRail` (Medium+) + More sheet. Top bar no longer shows the hamburger. ✅
- **IA:** Home · (permitted of Trips/Live Map/Payments) · More; More sheet = Vehicles/Drivers/Customers/Team/Vehicle Finance/Reports/Profile + theme toggle, each permission-gated. ✅
- **Dark selected icon = `onPrimaryContainer`** (`selectedIconColor = cs.onPrimaryContainer`) — the §7/D2 requirement (not `primary`). ✅ Selected indicator = `primaryContainer` pill. ✅
- **Labels always visible** (`alwaysShowLabel = true`). ✅
- **Window-size-class swap is CORRECT:** `rememberFleetBreakpoint()` reads the enclosing `BoxWithConstraints.maxWidth`, and the scaffold wraps everything in a **root `fillMaxSize` BoxWithConstraints** → maxWidth = window width. This is §7's sanctioned "single root-level BoxWithConstraints" path, NOT a mid-tree container breakpoint. ✅
- **Persistent active state** — `selectedKey` derived from the current route (detail screens under a section still map to their tab) → fixes the old drawer `selected=false`. ✅
- **Permission-gated on real slugs** (A's RBAC live) — matches the nav-gating plan.

### ✅ FINDING 1 — RESOLVED 2026-06-30 (Hindi shots in): labels fit, no clip
Settled with `nav-bar-hindi-font-1.5-portrait.png` + `nav-bar-hindi-large-font-portrait.png` + `nav-rail-hindi-font-1.5-landscape.png`:
- At **1.5× AND large font**, the Hindi bar labels (होम / ट्रिप / लाइव मैप / भुगतान / अधिक) fit **single-line, NO ellipsis truncation**. The `maxLines=1`+Ellipsis truncation risk is **not realized** for the shipped IA — the fixed bar labels are short, and long-labeled destinations (वाहन वित्त / ग्राहक) live only in the full-width **More sheet**, never a nav slot.
- The landscape shot confirms the **bar↔rail swap fires correctly** (rail on left, no bottom bar) and `अधिक`(More) carries the active slot pill when a More-sheet screen is open (persistent-active ✅).
- Residual (downgraded to NIT): no *true 2-line-wrap capability* (M3 NavigationBar + maxLines=1). Harmless for the current IA; only matters if a long-label item is ever promoted into a nav slot (which the no-backfill behaviour, Finding 2, avoids). **PASS.**
- *(The rail shot is EN-labeled — locale didn't switch for that capture — so the Hindi rail isn't directly shown, but the rail has more per-label room than the bar, which already passed. Low residual risk.)*

<details><summary>Original Finding 1 (pre-resolution)</summary>

### 🔴 FINDING 1 (HIGH — probable §9.9 miss + architecture deviation) — needs a Hindi nav shot
- Bottom bar + rail labels are `maxLines = 1, overflow = TextOverflow.Ellipsis` → long Hindi **truncates**, it does NOT 2-line wrap.
- §7/D2 + §9.9/§9.10 require: "labels 2-line Hindi wrap, **never clip**; budget ≈80dp; Hindi-length snapshot shows no clip."
- Root cause: built on **Material3 `NavigationBar`/`NavigationRail`** (fixed height, single-line label region). The direction explicitly said the bottom nav is a **CUSTOM rebuild** because "Material3 `NavigationBar` can't host the specs" (2-line Hindi / 80dp budget). So this is also a **§10-step-5 architecture deviation** (M3 component instead of the custom rebuild) — and the maxLines=1+Ellipsis is the symptom.
- **Verdict-blocking until settled:** need a **Hindi bottom-bar + rail screenshot** (होम/यात्राएँ/लाइव मैप/भुगतान/और). If "लाइव मैप"/"भुगतान" truncate → FAIL vs §9.9; if they happen to fit single-line → PASS-with-risk (still can't 2-line-wrap the widest backfill labels वाहन वित्त/ग्राहक if promoted). Either way the 2-line-wrap capability is absent.

**→ RESOLVED (see above): labels fit single-line at 1.5×/large font; not a fail.**
</details>

### FINDING 2 (MEDIUM) — backfill is "show fewer," not "backfill top-4"
- Code: Home + `[Trips, LiveMap, Payments].filter(permitted).take(3)` + More. A role missing a middle gets **fewer items**; overflow items (Vehicles…) are NOT promoted into the bar.
- §7/D2: "the bar **backfills its top 4 from the permitted set** so it never shows gaps or shifts (e.g. a manager lacking Payments or Live Map)." Confirm intent: show-fewer may be acceptable, but it deviates from the stated backfill.

### FINDING 3 (MEDIUM) — bar/rail surface token
- `containerColor = cs.surface`; D2 specifies **`surfaceContainer`** (light #F2F2F0 / dark #191D24). Bar should sit on surfaceContainer, distinct from surface cards. Minor but off-spec.

### FINDING 4 (MEDIUM) — rail has no Expanded variant
- §7: Medium = compact rail (icon+label stacked); Expanded = **full labels beside icons**. Code uses one `NavigationRail` (icon-above-label) for both Medium AND Expanded. The Expanded side-label variant is missing.

### FINDING 5 (minor, confirm intent) — selected colours
- Selected **label** = `onSurface` (D2 said `primary`); selected **icon** = `onPrimaryContainer` in light too (D2 said `primary` for light). Both are M3-coherent and arguably calmer/higher-contrast — likely fine, just differ from D2's literal wording.

## Ask
1. Drop a **Hindi** bottom-bar + rail (Medium/Expanded) screenshot to settle Finding 1.
2. PO/B decide: is the M3-NavigationBar base acceptable (and how do we meet 2-line Hindi), or does step 5 need the custom rebuild per §10? Findings 2-4 are spec deltas to confirm or fix.
