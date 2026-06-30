# DDD Fidelity Pass — Step 1 (Tokens) · PRELIMINARY

> **Build confirmed:** `com.indusjs.fleet.androidApp`, lastUpdateTime **2026-06-30 17:25** (B's fresh token build) on emulator-5554.
> **Captured:** LIGHT dashboard only. **DARK not captured** — emulator dropped off adb during the dark toggle (shared-device/adb flapping). Dark ladder (the key step-1 deliverable) is therefore **UNVERIFIED**.
> **Scope:** step-1 tokens only. Rainbow/card issues below are later-step (4/6) work, flagged not failed.

## Verdict (light only): PASS-so-far, dark pending

### ✅ Step-1 tokens that look correctly applied (light)
- **Primary indigo** live: hamburger / bell / refresh icons, the "Today" pill, "1 available" + "Completed Trips 0" numerals read indigo `#3D4EDB` family — **not** the old `#1976D2` Material sky-blue.
- **Warm-neutral canvas + white cards with soft shadow** (tonal-first depth) — applied.
- **onSurface** dark headings, clean hierarchy.

### ▲ Real step-1 nit (verify)
- **Small secondary labels in primary-tint.** "1 available" under Vehicles renders in primary indigo. §3 says the *smallest secondary numerics/labels* should use `onSurfaceVariant` (small-glyph sunlight legibility), **not** primary-tint. Low-sev, but it's a §3 rule — B should confirm intent.

### ⏳ Expected-remaining (NOT step-1 faults — later steps; flagged so nobody ships them)
- **Financial Overview rainbow:** Total Business (lavender), Expenses (cream + **orange ₹**), Profit (green tint + **green ₹**) are **full-card tints with coloured numerals** — exactly what §1 retires (status colour only as chips/dots/accent, never card tint; money terminology stays neutral). This is step-6 status-colour refactor + step-4 card consolidation. **Confirm it's on the plan.**
- **"Today/Week/Month"** is the *existing* filled-primary pill with a **white** label. My segmented-tab spec (step 3) is a raised **surface/white** pill with **`onSurface` (dark)** label — not white-on-indigo. Changes when B builds step 3. Noting so the current pill isn't mistaken for the spec'd tab.
- Fleet-Overview mini-cards + green progress bar still use old tints — same later-step cleanup.

## Dark capture #1 — `login-dark.png` (B drop, 17:51)
Dark LOGIN screen. Verifies several dark-token fundamentals, but **not** the ladder/highlight (login is near-flat, no stacked cards/sheets/menus).
- ✅ **Dark primary `#9AA6FF`** — logo, Sign In button, Forgot/Sign Up links.
- ✅ **Dark `onPrimary` (the high-risk fix):** "Sign In" label is **dark navy on `#9AA6FF`, not white** — the exact red-team failure, correctly avoided.
- ✅ **Dark `outline` fix:** Email/Password field borders perceptible on near-black (`#707A86` ≥3:1).
- ✅ Near-black background (not pure `#000`); muted `onSurfaceVariant` subtitle; white `onSurface` heading.
- (proxy) Email/Mobile segmented toggle reads well in dark (lighter active pill on darker track) — encouraging for the step-3 tab, though it's the login's own control.

## Dark capture #2/#3 — `dashboard-dark.png` + `drawer-dark.png` (full-res 1080×2400) — objective pixel analysis
PIL luminance sampling (not eyeballing) of the two flagged items:
- ✅ **1px top-highlight CONFIRMED.** Dashboard card top edges carry a consistent **+8.9 lum spike** over the card body (y=272/879/1742); the drawer/sheet carries **+4.7…+15.5**. `FleetElevatedSurface` is wired on cards AND menus/sheets (§4/§8 routing done).
- ✅ **Ladder collapse fixed.** background ≈15.9 vs card surface ≈36.4 = ~20-lum step; the `#161B22` flat-collapse is gone (§9.2 macro check met).
- ⓘ Full **6-step ≥5-lum-each** certification belongs to the generated WCAG/luminance report over the §3 hexes (CI-gated, §4), not screenshot pixels — one nested patch read near-equal but that's rough sample coords, inconclusive. Recommend confirming that report is green.

---

# FINAL VERDICT — Step 1 (Tokens): ✅ PASS-WITH-NITS

**PASS (verified, light + dark):**
- Light: indigo `primary` on warm canvas, white cards + soft shadow, clean onSurface hierarchy.
- Dark: `primary #9AA6FF`; the high-risk **`onPrimary` = dark-navy-not-white** (red-team fix landed); `outline` fix visible (#707A86 ≥3:1); near-black bg, muted `onSurfaceVariant`.
- Dark depth: ladder collapse gone (objective ~20-lum step); **1px top-highlight present on cards + sheets** (objective spike).

**NITS (non-blocking):**
1. Light: smallest secondary labels ("1 available") in primary-tint → should be `onSurfaceVariant` (§3 small-glyph rule). *(already routed to B)*
2. ~~Verification-method: certify the full 6-step ≥5-lum ladder via the CI report.~~ **RESOLVED:** PO's luminance math shows strict "≥5 L* per rung" fails in the bunched near-black middle (Low→surface +2.0, surface→Container +1.6 L*). This is physics — 6 near-black rungs can't all be ≥5 L* apart without graying the top rungs. **Design call (DDD): keep near-black, certify via tone OR the measured 1px highlight** (see §9.2 rewording below). Highlight is the intended adjacent-rung mechanism in the crushed range (§4). Reject lightening rungs.

**EXPECTED-REMAINING (NOT step-1 faults — step 4/6, §H approved):** Financial Overview rainbow + coloured ₹ persists in light AND dark (orange Expenses, green Profit); retires with card consolidation (step 4) + status refactor (step 6). The "Today/Week/Month" indigo-fill pill is the existing control; the neutral segmented pill lands in step 3.

**No FAILs.** Step-1 tokens are faithful to `color-tokens.svg` + the R1 hexes.

## Device side-effect to restore
- I set system `uimode night yes` to capture dark; emulator then dropped off adb before screenshot/restore. **If it returns it may be in dark — restore `night no`** (or whoever owns the device re-toggles). Apologies for the residue.
