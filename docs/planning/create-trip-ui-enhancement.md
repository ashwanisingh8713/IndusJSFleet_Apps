# Plan — Create Trip screen UI/UX enhancement

## Problem (user report)
- Input fields are **visually inconsistent**: some rectangular (default `OutlinedTextField` shape), some rounded
  (`RoundedCornerShape(12.dp)`). Verified: 13 raw `OutlinedTextField`s with mixed/ad-hoc shapes (12.dp / default / 4.dp).
- **Priority** is a row of `FilterChip`s — should be a **dropdown** (consistent with cargo type / unit).
- **Too much vertical space wasted** — 9 separate section cards, a whole card just for Priority, stacked single fields,
  an 80.dp bottom spacer.
- Overall: make it **good looking** and consistent.

## Web research — Material 3 form best practices (applied)
- **One field variant per region, used consistently** — don't intermix styles. → use ONE shared field everywhere.
  ([M3 text-field guidelines](https://m3.material.io/components/text-fields/guidelines))
- **Consistent vertical rhythm**, ~16–24px between elements; group related fields. ([M3 spacing](https://m3.material.io/foundations/layout/understanding-layout/spacing))
- **Density is good for scanning** but keep touch targets ≥ **48dp**; don't over-compress. ([M2 density](https://m2.material.io/design/layout/applying-density.html))
- **Compact width → full-width fields**; group short fields side-by-side to cut height. ([M3 layout](https://m3.material.io/foundations/layout/understanding-layout/spacing), [mobile form best practices](https://digitalthriveai.com/en-us/resources/web-design/best-practices-for-mobile-form-design/))

## Reuse (this repo already has the pieces — the screen just bypasses them)
- **`FleetInputField`** — shared text field, fixed shape `RoundedCornerShape(FleetTokens.Radius.L)` (12dp), built-in
  label/placeholder/leading+trailing icon/error/multiline (`fieldType` ADDRESS/NOTES) + keyboard types. The screen uses
  raw `OutlinedTextField` instead → the source of the inconsistency. **Migrate all text inputs to `FleetInputField`.**
- **`FleetDropdown`** — used for cargo type/unit; use it for **Priority** too.
- **`FleetTokens`** — `Spacing` (XS4/S8/M12/L16/XL24), `Radius` (L12/XL16), `Elevation.Card`. Replace ad-hoc dp literals.
- **`FleetSectionCard`** (Radius.XL) — the section container; keep, but fewer/denser sections.

## Design changes
1. **Field consistency:** every text input → `FleetInputField` (uniform 12dp radius, label/placeholder/error/icons).
   Location-autocomplete fields keep their custom dropdown but adopt the same `Radius.L` shape + a leading 📍 icon.
2. **Priority → `FleetDropdown`** (low/normal/high/urgent) with a leading flag icon; remove the chips.
3. **Cut wasted height (denser, fewer cards):**
   - **Remove the standalone Priority card** — move Priority into the **Schedule** (or a compact "Trip details") section,
     paired in a **row with cargo/priority**, so a whole card disappears.
   - **Side-by-side rows** for short fields: Departure **date + time**; Delivery **person + contact**; Cargo **weight +
     unit** (already a row). Two-column rows roughly halve those blocks' height.
   - Tokenize spacing: within-section `Spacing.M` (12), card inner padding `Spacing.L` (16), list `contentPadding`
     `Spacing.L` horizontal; drop the 80dp tail spacer to ~`Spacing.XXL`.
   - Add a small **leading icon per section header** (🗓 schedule, 🚚 vehicle, 📍 route, 📦 cargo, 👤 customer, 🚚
     delivery, 💰 pricing) for scannability — purely in the existing `SectionCard` title.
4. **Polish / hierarchy:** consistent required `*` markers; consistent supportingText for errors; sticky bottom bar with
   the completion bar + Create button (already present — tighten); keep all tap targets ≥48dp.
5. **Order (group logically):** Schedule + Priority → Vehicle/Driver → Route → Cargo → Customer → Delivery → Pricing →
   Notes. (Was: Schedule, Vehicle/Driver, Route, Cargo, **Priority**, Customer, Delivery, Pricing, Notes.)

## Out of scope / keep
- No contract/field changes (this is presentation only); all the recent fields (delivery, COGS, material units) stay.
- Trip-detail **Edit** screen unchanged (separate screen).

## Verify
`:androidApp:assembleDebug` green; visual check on device/D; touch targets ≥48dp; EN+HI for any new section/label strings.
Mockup shown to the user for alignment before the full migration.
