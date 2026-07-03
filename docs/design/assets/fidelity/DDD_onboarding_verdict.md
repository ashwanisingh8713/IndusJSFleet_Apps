# DDD Fidelity — Onboarding flow (org-setup / plans / checkout) · VERDICT

> Batched 10-agent adversarial workflow (review → verify). FAILs pixel-sampled + independently re-verified. Fidelity-only.

## Scoreboard
| Screen | Verdict | Headline |
|---|---|---|
| org-setup (HI) | ✅ PASS | indigo stepper, neutral fields, Devanagari clean, anti-rainbow clean |
| plans (HI) | 🔴 **FAIL** | full-color plan-card tints (indigo Pro + **teal** Enterprise) = anti-rainbow §1/§I |
| checkout (HI) | 🔴 **FAIL** | order-summary **Total = indigo**, trial value **= teal** (must be neutral) |

---

## ✅ FAIL — plans — RESOLVED 2026-07-03 (`onboarding-plans-HI-neutral-light.png`)
Full redline applied: plan cards now **neutral** (Enterprise white surface, teal gone; Pro selected = pale `primaryContainer` tint + indigo border + check badge = sanctioned selection affordance); **prices neutral `onSurface`** (₹999/₹299 dark SemiBold, no white-on-tint); **billing toggle** now the neutral white segmented pill (the D1 medium fixed too). Residual minors only: feature-count "25" in indigo (tolerable — count not money), English feature text (D's i18n), savings-chip matra padding (nit). **Plans PASS now.**

<details><summary>Original plans FAIL (pre-fix)</summary>

### plans: full-color plan-card tints (§1/§I anti-rainbow)
**Verified (both verify agents CONFIRMED the card-tint):** each plan card is a full saturated body wash — Pro = full **indigo** gradient (`#3D4EDB→#8F99EC`), Enterprise = full **teal** fill (`RGB(51,145,154)`). A two-hue full-card rainbow — the exact §1 violation ("semantic/tertiary colour only in chips/dots/accent bars, never a full-card tint"), same pattern §I already redlined for device-alerts.
- **Selection-tint defense fails:** the sanctioned selection token is pale `primaryContainer #E1E4FF`; these are full-saturation washes. And the **unselected** Enterprise card is teal-filled — an unselected card can't be a selection tint, and teal isn't even the selection hue.
- **Prices white-on-tint** (₹999, ₹299) — a symptom of the card tint; neutralizing the card makes the price neutral `onSurface` automatically. *(One verify agent debated whether §H scopes to marketing plan prices — moot: the card-tint FAIL is the root and is unanimous.)*
- MEDIUM: the **billing toggle** (मासिक/वार्षिक) uses an **indigo-filled** active pill — the button/nav idiom, not the D1 segmented pill (neutral `surface` + `outlineVariant` hairline on a `surfaceContainerHighest` track). Use `FleetTabBar` for consistency with the app's other segmented controls.
- NIT: savings chip "वार्षिक…" — the वार्षिक matra sits near-clipped at the chip's top edge; add vertical padding (C3).
- ✅ Correct: bottom CTA white-on-indigo; the selected-Pro **check badge** (legit selection marker); Devanagari clean.

**REDLINE:** plan cards = **neutral `FleetSectionCard` surface**. Differentiate plans via a **chip** ("Recommended"/"Popular" on Pro) and/or a small icon/accent — not a full-card tint. **Selected** plan = pale `primaryContainer #E1E4FF` tint + the existing check badge. **Prices neutral `onSurface`** SemiBold tabular. Billing toggle → the neutral `FleetTabBar` segmented pill.
</details>

## ✅ FAIL — checkout — RESOLVED 2026-07-03 (`onboarding-checkout-HI-neutral.png`)
Both redlines applied: कुल (Total) `₹999/माह` → **neutral `onSurface`** (was indigo); मुफ्त ट्रायल "14 दिन" → **neutral `onSurface`** (was teal). All order-summary rows now neutral; card neutral surface; Pay CTA white-on-indigo; Devanagari clean; hero header (branded display) acceptable. **Checkout PASS now.**

<details><summary>Original checkout FAIL (pre-fix)</summary>

### checkout: order-summary money is status/brand-coloured (§H / §1)
**Both findings verified CONFIRMED on the actual order-summary (a real money screen — no scoping debate):**
- **कुल (Total) value `₹999/माह` = primary indigo `#3D4EDB`** (identical to the CTA fill). §H rule 1: the total is money → **neutral `onSurface #16181D`**, SemiBold tabular. The sibling rows (Pro, मासिक) are already neutral — only the total is mis-coloured.
- **मुफ्त ट्रायल (Free trial) value "14 दिन" = C1 info teal `#0E7C86`** — a bare row value in semantic colour. §1: semantic colour only in chips/dots/accent bars → make it **neutral `onSurface`**, or express "included/free" via a neutral/success **chip**, not a coloured value.
- ✅ Correct: neutral order-summary card (`surfaceBright`), other rows neutral, section title neutral, Pay CTA white-on-indigo, Devanagari clean (₹999 <1 lakh so no grouping needed). Hero header price (white on indigo gradient) is acceptable **display** type, not a summary row — not flagged.

**REDLINE:** total value → neutral `onSurface`; trial value → neutral `onSurface` (or a chip for "included"). Keep everything else.
</details>

## ✅ org-setup — PASS
Indigo stepper (active/completed `#3D4EDB`, upcoming neutral), neutral outlined fields, neutral text hierarchy, Devanagari clean (title + 2-line body no clip), fully anti-rainbow (exhaustive pixel sample found only indigo + neutrals). Nits: CTA was **disabled** (empty field) so enabled white-on-indigo unverified — grab a filled-state capture; input-field radius reads slightly softer than the 8dp button family (cosmetic). **→ enabled-CTA CLOSED 2026-07-03** (`org-setup-HI-enabled-cta.png`): field filled → CTA enabled "बनाएँ और आगे बढ़ें" = **white Devanagari on indigo `#3D4EDB`** (light onPrimary=white confirmed); indigo focus border + floating label; Devanagari clean. Org-setup fully PASS (both states).

## FYI (out of my lane — D/product)
- plans (HI): plan descriptions, feature list, and spec rows are **untranslated English** under Hindi headers — a localization gap, not a design-system fault.

---
**Net:** onboarding **plans + checkout FAIL** the anti-rainbow / money-neutral rules (same class as the device-alerts FAIL, now recurring on marketing/checkout screens). org-setup passes. Redlines above.
