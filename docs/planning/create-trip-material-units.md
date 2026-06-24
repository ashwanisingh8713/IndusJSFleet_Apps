# Plan — Material type → unit mapping (Create Trip), config-driven via a KMP asset

## Problem / intent
On the Create-Trip screen the cargo **weight + unit** input is weak: the unit dropdown lists ALL units
(`KG, M.Ton, Quintal, Liter, CFT, Bags`) regardless of the chosen material. It should:
1. Show only the units that make sense for the selected **material type** (gitti, balu, … ).
2. Have a cleaner UI, and auto-pick a sensible default unit when a material is chosen.
3. Be **data-driven** from a JSON asset shipped with the app, loaded in the background when the screen opens.
4. Work on **all KMP targets** (Android / iOS / Web) from ONE source — a unified approach.

## Current state (verified)
- `CreateTripContract`: `cargoTypes` (7) + `weightUnits` (6) are hardcoded lists; `State.cargoTypeOptions` /
  `weightUnitOptions` default to them. `UpdateCargoType` just sets the type; `UpdateWeightUnit` sets the unit. No link
  between them.
- UI (`CreateTripScreen` ~772-832): cargo-type `FleetDropdown`, then a Row of weight `OutlinedTextField` +
  unit `FleetDropdown` bound to `state.weightUnitOptions`.
- `screen-trip` has **no** `composeResources` of its own; it consumes `ijs-ui-components-lib`'s generated `Res`
  (strings). No `files/` assets and no `Res.readBytes` usage exist yet — this is the first.
- compose-multiplatform **1.10.0-rc01** → `Res.readBytes(path)` (suspend, all targets) is available.

## Unified-asset decision (the KMP "asset folder")
Do **NOT** use Android `src/androidMain/assets/` (Android-only). The unified KMP mechanism is **Compose Resources**:
place the file under `…/composeResources/files/` and read it with `Res.readBytes("files/<name>.json")`, which resolves
on Android, iOS, Web and Desktop from a single declaration. Location:
`ijs-ui-components-lib/src/commonMain/composeResources/files/cargo_materials.json` — that module already owns the shared
`Res`, and `screen-trip` already imports it, so no new gradle/composeResources wiring is needed.

## JSON schema (`cargo_materials.json`)
```json
{
  "version": 1,
  "materials": [
    { "id": "gitti",     "label": "Gitti (Aggregate)", "units": ["M.Ton","Quintal","CFT","KG"], "defaultUnit": "M.Ton" },
    { "id": "balu",      "label": "Balu (Sand)",       "units": ["CFT","M.Ton","Quintal","KG"], "defaultUnit": "CFT" },
    { "id": "bhakshi",   "label": "Bhakshi",           "units": ["M.Ton","Quintal","KG"],       "defaultUnit": "M.Ton" },
    { "id": "enta",      "label": "Enta (Bricks)",     "units": ["M.Ton","Quintal","Bags","KG"],"defaultUnit": "M.Ton" },
    { "id": "hazardous", "label": "Hazardous",         "units": ["Liter","KG","M.Ton","Quintal"],"defaultUnit": "Liter" },
    { "id": "valuable",  "label": "Valuable",          "units": ["KG","Quintal","Bags"],        "defaultUnit": "KG" },
    { "id": "others",    "label": "Others",            "units": ["KG","M.Ton","Quintal","Liter","CFT","Bags"], "defaultUnit": "KG" }
  ],
  "allUnits": ["KG","M.Ton","Quintal","Liter","CFT","Bags"]
}
```
- `id` matches the existing `cargoTypes` values (sent to the backend, lowercased — unchanged contract).
- Units are a **subset** of the existing 6 (no backend-unknown units → safe with `weight_unit`).
- The mapping is a sensible default; it is **data only** — editable without code. (Domain SME can tweak later.)

## Design (Clean Arch + MVI, all in screen-trip + the one asset)
- **Model** (`screen-trip/domain`): `CargoMaterial(id, label, units, defaultUnit)` + `CargoMaterialConfig(materials, allUnits)`.
  `@Serializable` DTO with `@SerialName` + defaults (DTO discipline); a domain mapper if needed.
- **Loader** (`screen-trip/data`): `CargoConfigDataSource.load()` → `Res.readBytes("files/cargo_materials.json")` on
  `dispatcherProvider.io`, parse with the shared Json. **Fallback** to the hardcoded defaults (current 7 types / 6 units)
  if read/parse fails — the form must never break if the asset is missing/corrupt.
- **Contract**: `State` gains `cargoMaterials: List<CargoMaterial>` (loaded) + a derived `unitOptionsForSelectedCargo`
  (units for the selected `cargoType`, else `allUnits`). `cargoTypeOptions` becomes the loaded materials' ids/labels.
- **ViewModel**: on `init` (screen open) launch a background `loadCargoConfig()` → `updateState`. `UpdateCargoType` now
  also recomputes the unit list and sets `weightUnit` to that material's `defaultUnit` (and clears it if the previously
  chosen unit isn't valid for the new material). Pre-existing validation (`weightUnit` required) unchanged.
- **UI enhancement** (`CreateTripScreen`): the unit `FleetDropdown` is fed `state.unitOptionsForSelectedCargo`; it is
  disabled with a hint until a material is chosen (units depend on material); the default unit shows auto-selected; the
  cargo-type dropdown uses the config `label`. Keep the weight+unit Row but tighten spacing/labels.

## Future-proofing
- New materials / unit sets = edit `cargo_materials.json` only (no recompile of logic).
- Graceful fallback keeps the screen working if the asset is absent (older bundle) or malformed.
- `version` field allows future schema evolution.
- Same `Res.readBytes` pattern can host other config assets later (single, documented approach).

## Verify
`:androidApp:assembleDebug` green; confirm `Res.readBytes("files/cargo_materials.json")` resolves; manual logic check:
select each material → unit list filters + default auto-selects; unknown/blank material → allUnits; asset-missing →
fallback defaults. EN+HI strings for any new labels.
