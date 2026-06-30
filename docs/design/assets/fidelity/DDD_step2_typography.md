# DDD Fidelity Pass — Step 2 (Typography) · IN PROGRESS

> Verifies §5 + C3: Noto Sans + Devanagari, type ramp, weight hierarchy, no ALL-CAPS, EN/HI baseline parity, Devanagari line-height/no-clip, tabular ₹ alignment.

## Capture #1 — `dashboard-en-light.png` (EN, light)

### ✅ Looks correct (EN, what this screen exercises)
- **Typeface reads as Noto Sans, not Poppins** — humanist proportions, double-story `a`/`g` (Poppins is geometric/single-story). Pending the actual font-resource confirmation, but the geometric Poppins look is gone.
- **Type ramp present** — muted "Good Evening" → bold "Ashwani Singh" (title) → tiny muted "Updated…"; section titles (~title/SemiBold); large SemiBold KPI numerals over muted `onSurfaceVariant` labels. Matches §5 ramp + the Motive KPI register.
- **Weight hierarchy** — SemiBold titles/numerals, Regular/Medium labels; the old near-uniform Bold/ExtraBold shout is gone (§5).
- **No ALL-CAPS** — "Today / This Week / This Month", "Create Trip", "Add Cost", "View All" all sentence/title case (§5).

### ⏳ NOT verifiable on this screen (need more captures)
- **EN/HI baseline parity** + **Devanagari line-height / no-clip / no shirorekha cut** — needs the **HI counterpart** (`dashboard-hi-light.png`). This is the headline step-2 risk (LineHeightStyle trim=None + includeFontPadding=false).
- **Tabular-figure column alignment** + Indian grouping `₹2,50,000` — this screen has only single-digit `₹0`, so columns can't be checked. Need a **multi-digit number-dense screen**: Reports / Payments / Costs list or a P&L with real ₹ values — ideally **EN + HI** to also confirm Western-Arabic digits render in the HI locale (the `tnum` SpanStyle forcing Latin Noto).

### ⏳ Expected-remaining (NOT step-2 faults)
- Financial ₹ still coloured (indigo/orange/green) on tinted cards — that's the step-4/6 rainbow retire (§H approved), not typography.

## Capture #2 — `profile-hi-light.png` (HI, light)
- ✅ **Devanagari renders clean** — shirorekha intact, matras/conjuncts not clipped (प्रोफाइल, संपर्क जानकारी, अद्यतन's द्य conjunct, यूज़र). Line-height has headroom → the `trim=None` + `includeFontPadding=false` work landed.
- ✅✅ **Western-Arabic digits in HI locale** — `8390098886`, `11`, `25-Jun-2026 06:38 AM` all Latin digits, NOT Devanagari (०–९). The §5 risk (digits inheriting Devanagari advance widths) is handled — the tnum Latin-Noto SpanStyle is doing its job.
- ✅ **Devanagari in chips** (मालिक / सक्रिय) — no clip; semantic chip styling intact (सक्रिय = success-green).
- ✅ **EN/HI parity (visual):** Latin "Ashwani Singh" sits cleanly beside Devanagari labels, baselines aligned. Strong evidence; the formal same-role EN/HI baseline-equal snapshot is B/D's automated gate (§9.12).

## Capture #3 — `dashboard-hi-light.png` (HI) — vs `dashboard-en-light.png` = same-screen EN/HI pair
- ✅ **EN/HI baseline parity CONFIRMED (direct):** identical layout across locales — every row/tile/button in the same position; "Ashwani Singh" pixel-identical; "शुभ संध्या" aligns exactly where "Good Evening" sat. Taller Devanagari fits the SAME containers with no clip, no overflow, no row-shift. The Noto-super-family + line-height metric-parity goal is met.
- ✅ Devanagari in the **segmented pill** (आज / इस सप्ताह / इस महीने) and **buttons** (ट्रिप बनाएं / खर्च जोड़ें) fits without clip.
- ✅ Western-Arabic digits + `30-06-2026` consistent across both locales.

# STEP-2 VERDICT — ✅ PASS
**PASS (verified):** Noto Sans replaces Poppins; §5 ramp + weight hierarchy; no ALL-CAPS; Devanagari clean (no shirorekha/matra clip, adequate line-height); **EN/HI baseline parity confirmed on the same-screen pair**; Western-Arabic digits in HI locale; Devanagari in chips/pills/buttons no-clip.
**Carry-forward — PARTIALLY closed via `reports-en-light.png`:**
- ✅ Comma grouping correct at thousands (`₹50,000`, `₹15,000`, `+₹35,000`); lining/consistent-width digits.
- ⚠️ STILL open (hand to B/D snapshot gates §9.7/§9.12 with seeded ₹1L+ data — not worth more screenshots): (a) Indian **lakh** grouping `₹2,50,000` (no on-screen value ≥1 lakh); (b) a true vertical **₹ column** (screen has side-by-side tiles, not a column); (c) **compact suffix localization** — donut shows `₹15.0K` (English K); confirm HI renders हज़ार/लाख/करोड़, not hardcoded K/L/Cr.
- Note: Profit `+₹35,000` green / Expenses red here is the pre-§H pattern; §H neutralizes positive profit (▲ chip, neutral numeral) in step 4/6.

## Out-of-scope observations (FYI, not type faults — D / product domain)
- Team tiles: role labels **प्रबंधक / पर्यवेक्षक** (manager / supervisor) — possible legacy-terminology leak vs the owner/admin/user target.
- HI header "अपडेट: **Yesterday**, 03:05 PM" — "Yesterday" not localized (i18n-string gap), and relative-time/month-name localization is D's domain.

## Out-of-scope observation (FYI, not a type fault)
- Team tiles show role labels **प्रबंधक / पर्यवेक्षक** (manager / supervisor). Per the project's target model (owner/admin/user; manager/supervisor are legacy) this may be a terminology leak — flagging for whoever owns terminology; not a typography issue and not mine to change.
