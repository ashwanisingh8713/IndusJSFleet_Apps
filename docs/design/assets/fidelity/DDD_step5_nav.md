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

### ✅ FINDING 2 — CLOSED 2026-07-03 (`c1-nav-baseuser-backfilled-HI.png`)
B implemented the backfill (punch-list c1). Base-user bar (perms: trips/live-map/vehicles/drivers view, NO payments/customers) reads **होम / ट्रिप / लाइव मैप / वाहन / अधिक** — **Vehicles promoted into Payments' gated slot**, gapless, no shift, 4 primary + More. Exactly the "backfill top-4 from the permitted set" I specced. Owner-verified for the owner case (full 5) + base-user verified here. f5/f7/f8 also hold for the base-user role. Finding-2 resolved.

<details><summary>Original Finding 2 (pre-fix)</summary>

### FINDING 2 (MEDIUM) — backfill is "show fewer," not "backfill top-4"
- Code: Home + `[Trips, LiveMap, Payments].filter(permitted).take(3)` + More. A role missing a middle gets **fewer items**; overflow items (Vehicles…) are NOT promoted into the bar.
- §7/D2: "the bar **backfills its top 4 from the permitted set** so it never shows gaps or shifts (e.g. a manager lacking Payments or Live Map)." Confirm intent: show-fewer may be acceptable, but it deviates from the stated backfill.
</details>

### FINDING 3 (MEDIUM) — bar/rail surface token
- `containerColor = cs.surface`; D2 specifies **`surfaceContainer`** (light #F2F2F0 / dark #191D24). Bar should sit on surfaceContainer, distinct from surface cards. Minor but off-spec.

### ✅ FINDING 4 — CLOSED 2026-07-04 (`c3-nav-rail-expanded-fulllabels-HI.png`)
B built the Expanded-rail variant (punch-list c3). At Expanded/tablet width the rail shows **full labels BESIDE icons** (होम/ट्रिप/लाइव मैप/भुगतान/अधिक, horizontal icon+text), selected on a `primaryContainer` pill, neutral rail surface + trailing divider — exactly §7's "Expanded = full labels beside icons." Content at Expanded width also confirms f2 (inset/floating tab track), f5, f7, f8, §H all render correctly. **All step-5 findings (F1–F5) now resolved.**

<details><summary>Original Finding 4 (pre-fix)</summary>

### FINDING 4 (MEDIUM) — rail has no Expanded variant
- §7: Medium = compact rail (icon+label stacked); Expanded = **full labels beside icons**. Code uses one `NavigationRail` (icon-above-label) for both Medium AND Expanded. The Expanded side-label variant is missing.
</details>

### FINDING 5 — selected colours — ✅ RULED 2026-07-03 (accept onSurface label)
- Selected **label** = `onSurface` (D2 said `primary`); selected **icon** = `onPrimaryContainer`.
- **DDD ruling:** ACCEPT the neutral `onSurface` selected label. The selected slot already carries the indigo via the `primaryContainer` pill + filled indigo icon; adding a primary label = triple-indigo and reads heavier. Neutral label is the calmer Calm-Fintech choice and selection is unambiguous. **D2 wording superseded** — selected nav = primaryContainer pill + filled indigo icon + **neutral `onSurface` label**. No change for B. (Re-confirmed on the f5–f9 matrix; it's consistent across the app.)

### ✅ FINDING 1 — architecture — SUPERSEDED/RESOLVED 2026-07-03 by the f9 rebuild
The M3-`NavigationBar` deviation is gone: **B rebuilt `FleetBottomNavBar` as a custom content-driven `Row`** (f9) — M3 NavigationBar's fixed 80dp couldn't do content-driven height, so it was replaced. The custom bar is single-line ~64dp (EN), `maxLines=2` with content-driven height that grows on HI 2-line wrap (§9.9 no-clip). This is exactly the custom rebuild §10 originally wanted. Finding-1 fully resolved (both the label-fit *and* the architecture).

## Ask
1. Drop a **Hindi** bottom-bar + rail (Medium/Expanded) screenshot to settle Finding 1.
2. PO/B decide: is the M3-NavigationBar base acceptable (and how do we meet 2-line Hindi), or does step 5 need the custom rebuild per §10? Findings 2-4 are spec deltas to confirm or fix.
