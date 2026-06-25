# Plan — Visual polish pass (crunchy redesign), starting with the Verify screen

User feedback: the earlier sweep made screens *compliant* (tokens, dark-mode-safe,
responsive, validation) but several input-heavy screens read as "tidy", not
"crunchy". User approved the Verify-screen redesign mockup and chose a **broader
key-screen pass**.

## The visual bar (apply consistently)
- **Reusable components first.** New `FleetOtpInput` (segmented digit boxes). Reuse
  existing `FleetSectionCard` (clean surface + subtle border + elevation — do NOT
  override to muddy `surfaceVariant.copy(0.3)`), `FleetAccentIconChip` (icon anchor),
  `FleetButton` (pill), `FleetInputField`, `FleetStatusColors`.
- **Icon anchor** per section/card (tinted chip) for visual hierarchy.
- **Contrast & depth**: surface cards on a distinct page bg; no grey-on-grey.
- **Status feedback**: success pill / collapsed confirmed state.
- **Crunchy buttons**: filled primary + outlined secondary, clear disabled state,
  side-by-side via weight (now works post FleetButton fix).
- **Typography**: title 15–16/Medium, subtitle 13 muted; consistent spacing tokens.
- **Day/Night** via color roles; **all resolutions** via FleetBreakpoints.
- **Validation** preserved/strengthened (name/email/mobile/amount).

## Foundation deliverables
1. `FleetTokens.Border` { Hairline 1, Default 1.5, Emphasis 2 } — lib forbids raw dp.
2. `FleetOtpInput(value, onValueChange, length, enabled, isError)` — single
   BasicTextField (NumberPassword) rendered as `length` segmented boxes: filled digit,
   focused/next box = primary 2dp border, error = error border, resting = outlineVariant.

## Screen 1 — Verify Your Account (approved mockup)
- Clean `FleetSectionCard` (default styling) per section.
- Header: `FleetAccentIconChip(ic_email / ic_phone)` + title + address; verified → green
  check pill + confirmation line, input hidden.
- `FleetOtpInput` (6 email / 4 mobile) + inline error.
- Row[ Resend SECONDARY weight(1f), Verify PRIMARY weight(1f) ].
- Verify on device: light + dark, enabled/disabled, error state.

## Subsequent screens (broader pass — execute one by one, build+verify each)
Auth flow first (most-seen): Login, Sign Up, Forgot Password (icon anchors, crunchy
inputs, consistent spacing/hierarchy). Then key input/visual-heavy screens:
Create Trip, Dashboard, and primary detail screens. Each: apply the bar, build the
module, install `-r -d -t`, eyeball light/dark on device.

## Verify
Per-module `compileCommonMainKotlinMetadata`, then `:androidApp:assembleDebug` (clean
if incremental dex acts up), `adb install -r -d -t`, on-device light/dark check.
