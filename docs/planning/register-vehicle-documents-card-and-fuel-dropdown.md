# Register Vehicle — fix Documents card + Fuel Type dropdown (dual layout)

## Requests

1. **Documents tab looks broken.** Each document card's name renders one-character-per-line
   (vertical text) and the "Upload" button overlaps the middle.
2. **Fuel Type should be a dropdown** like Vehicle Type, and the two should sit **side by side
   ("dual part")** in Basic Info.

## Root cause — Documents card (the "weird" layout)

`DocumentUploadCard` lays out a Row: `[icon chip] [name+status Column(weight 1)] [Upload FleetButton]`.
The trailing `FleetButton("Upload", SMALL, modifier = widthIn(min = MinTouchTarget))` only sets a
**min** width. In a Compose `Row`, an *unweighted* child is measured with the **full remaining width**
as its `maxWidth`. `FleetButton` measures its own breakpoint from that slot (`BoxWithConstraints`);
a wide slot on a phone is still **Compact**, and on Compact the inner button does `fillMaxWidth()` →
it greedily fills the whole remaining row → the weighted name `Column` is left ~0 width → its `Text`
wraps one glyph per line. (Same family as the documented FleetButton weight gotcha.)

**Fix = call-site, not the shared component.** Changing FleetButton's Compact-fill would ripple to
every weighted side-by-side button (e.g. bottom action bars rely on it). Instead, replace the inline
"Upload" `FleetButton` with a **fixed-size upload `IconButton`** (44dp), symmetric with the existing
remove `IconButton`, and make the empty card row **clickable** to upload. Fixed size ⇒ no width fight,
locale-proof (no text-width guessing). New drawable `ic_upload.xml` added.

## Fix — Fuel Type dropdown + dual layout

- Replace the chip-row `FuelTypeSelector` and the standalone `VehicleTypeSelector` with a single
  **dual Row**: two `FleetDropdown`s, `Modifier.weight(1f)` each (like the Make/Model row).
  - Left: **Vehicle Type \*** (`FleetDropdown<VehicleType>`, options = enum, localized labels).
  - Right: **Fuel Type** (`FleetDropdown<String>`, options = `state.fuelTypes` — already filtered by
    the selected vehicle type; localized via `fuelLabelFor`). Selecting Truck ⇒ only "Diesel".
- `FleetDropdown` trigger label: cap to **one line** (`maxLines = 1` + ellipsis) so a long label in a
  half-width column can't wrap and double the field height — mirrors the `FleetInputField` fix. Shared
  but low-risk (only caps lines; never grows). Honors `RowScope.weight` (caller modifier already sits
  on its `BoxWithConstraints` root, like FleetButton's fixed version).

## Files

- `ijs-ui-components-lib/.../drawable/ic_upload.xml` — NEW.
- `ijs-ui-components-lib/.../components/FleetDropdown.kt` — label `maxLines = 1` + ellipsis.
- `screen-vehicle/.../presentation/AddVehicleScreen.kt`:
  - `DocumentUploadCard`: trailing action → upload IconButton; row clickable when empty.
  - Basic Info: replace the two selector items with one `VehicleTypeFuelRow` (dual dropdowns); delete
    the old `VehicleTypeSelector` (chip/dropdown) + `FuelTypeSelector` (chips).

## Verify (device, both modes)

- Documents tab: each doc card is a normal one-line row (icon · name · upload icon), no vertical text,
  no overlap; tapping a card opens the upload dialog; uploaded state shows filename + remove.
- Basic Info: Vehicle Type and Fuel Type are two side-by-side dropdowns, regular height. Pick Truck ⇒
  Fuel shows only Diesel; pick Car ⇒ all five. Day + Night.
- No regression to other FleetDropdown/FleetButton usages (adversarial review).
