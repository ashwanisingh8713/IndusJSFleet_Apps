# Register Vehicle — Vehicle Type dropdown + config-driven Fuel Type

## Goal (user request)

On the **Register Vehicle** screen:

1. `Vehicle Type` should be a **dropdown** (today it is a horizontal-scroll row of
   `FilterChip`s).
2. The `Fuel Type` options shown must **depend on the selected Vehicle Type**.
3. The mapping lives in a **bundled JSON asset** (same pattern as `cargo_materials.json`):
   each Vehicle Type lists its allowed Fuel Types + a default.
   - e.g. **Truck** and **Bus** → only **Diesel**.
   - **Car** → Diesel, Petrol, CNG, Electric, Hybrid.

## Existing patterns to reuse (don't reinvent)

- **Config asset**: `ijs-ui-components-lib/src/commonMain/composeResources/files/cargo_materials.json`,
  read via `Res.readBytes("files/...")` on `dispatcher.io`, parsed with lenient `Json`,
  graceful **fallback** on any error. Mirror exactly.
  - Loader: `screen-trip/.../data/CargoConfigProvider.kt`
  - DTO+domain+mappers: `screen-trip/.../domain/entity/CargoMaterial.kt`
  - Localized label helper: `CreateTripContract.cargoLabelFor(id, hindi)` +
    `isHindi = Locale.current.language == "hi"` in the screen.
- **Dropdown**: canonical `FleetDropdown<T>(label, options: List<DropdownOption<T>>, selectedOptionId, onOptionSelected, ...)`
  in `ijs-ui-components-lib`. Single-line, adaptive width, error state — already used for Cargo Type.

## Wire-format facts (client must not change the contract)

- `vehicle_type` is sent **lowercased** (`VehicleMapper.vehicleTypeToApiString = type.name.lowercase()`),
  e.g. `"truck"`. So config `id` = the `VehicleType` enum name lowercased.
- `fuel_type` is sent **lowercased** (`VehicleMapper` does `vehicle.fuelType.lowercase()`).
  The UI keeps display strings ("Diesel"); the mapper lowercases on send — unchanged.
  → We only change *which fuel options are shown*, never the selected value's flow.

## Design

### 1. JSON asset — `composeResources/files/vehicle_types.json` (in ijs-ui-components-lib)

```json
{
  "version": 1,
  "vehicleTypes": [
    { "id": "truck",      "label": "Truck",      "label_hi": "ट्रक",        "fuelTypes": ["Diesel"],                                  "defaultFuel": "Diesel" },
    { "id": "van",        "label": "Van",        "label_hi": "वैन",         "fuelTypes": ["Diesel","Petrol","CNG","Electric"],         "defaultFuel": "Diesel" },
    { "id": "car",        "label": "Car",        "label_hi": "कार",         "fuelTypes": ["Diesel","Petrol","CNG","Electric","Hybrid"], "defaultFuel": "Petrol" },
    { "id": "bus",        "label": "Bus",        "label_hi": "बस",          "fuelTypes": ["Diesel"],                                  "defaultFuel": "Diesel" },
    { "id": "motorcycle", "label": "Motorcycle", "label_hi": "मोटरसाइकिल",  "fuelTypes": ["Petrol","Electric"],                        "defaultFuel": "Petrol" },
    { "id": "trailer",    "label": "Trailer",    "label_hi": "ट्रेलर",       "fuelTypes": ["Diesel"],                                  "defaultFuel": "Diesel" }
  ],
  "allFuelTypes": [
    { "value": "Diesel",   "label": "Diesel",   "label_hi": "डीज़ल" },
    { "value": "Petrol",   "label": "Petrol",   "label_hi": "पेट्रोल" },
    { "value": "CNG",      "label": "CNG",      "label_hi": "सीएनजी" },
    { "value": "Electric", "label": "Electric", "label_hi": "इलेक्ट्रिक" },
    { "value": "Hybrid",   "label": "Hybrid",   "label_hi": "हाइब्रिड" }
  ]
}
```

Rules beyond the user's two examples (sensible commercial-fleet defaults):
Van = multi-fuel commercial; Motorcycle = no diesel; Trailer = towed, diesel placeholder.

### 2. Domain — `screen-vehicle/.../domain/entity/VehicleTypeConfig.kt`

`VehicleTypeOptionDto`/`FuelTypeLabelDto` (+ `@SerialName` + defaults) → domain
`VehicleTypeOption(id,label,labelHi,fuelTypes,defaultFuel)`,
`FuelTypeLabel(value,label,labelHi)`, `VehicleTypeConfig(vehicleTypes, allFuelTypes)`.
Mapper drops malformed entries (blank id or empty fuelTypes).

### 3. Provider — `screen-vehicle/.../data/VehicleTypeConfigProvider.kt`

`object` with `suspend fun load(dispatcher): VehicleTypeConfig`, `ASSET_PATH="files/vehicle_types.json"`,
lenient Json, `fallback()` mirroring the 6 enum entries + the rules above. Never throws.

### 4. Contract (`AddVehicleContract.State`)

- Add `vehicleTypeOptions: List<VehicleTypeOption> = emptyList()`
- Add `fuelTypeLabels: List<FuelTypeLabel> = emptyList()`
- Keep `fuelTypes: List<String>` — now **derived** from the selected type's config.
- Helpers: `vehicleTypeLabelFor(type, hindi)`, `fuelLabelFor(value, hindi)`.

### 5. ViewModel

- `init`: launch `VehicleTypeConfigProvider.load(...)`; set `vehicleTypeOptions`,
  `fuelTypeLabels`, and seed `fuelTypes`/`fuelType` from the currently-selected
  type (default `CAR`)'s config entry.
- `UpdateVehicleType`: set `vehicleType`; recompute `fuelTypes` from config;
  if current `fuelType` ∉ new list → set to that type's `defaultFuel`.
- Enum↔id: `VehicleType.name.lowercase()` ↔ `option.id`. Add private helper
  `optionFor(type)` returning the matching `VehicleTypeOption?`.

### 6. Screen (`AddVehicleScreen`)

- Replace `VehicleTypeSelector` chips with `FleetDropdown<VehicleType>`:
  options from `state.vehicleTypeOptions` (fallback `VehicleType.entries`),
  label localized via `vehicleTypeLabelFor`, `selectedOptionId = state.vehicleType`.
- `FuelTypeSelector`: keep chip row (few options, good UX) but localize labels via
  `fuelLabelFor`. Options already come from `state.fuelTypes` (now filtered).

## Safety / fallbacks

- Asset missing/corrupt → `fallback()` keeps all 6 types working.
- Config empty in UI → dropdown falls back to `VehicleType.entries`, fuel falls back
  to the full 5-fuel list. Form never breaks.
- No backend contract change; no DI wiring change (plain `object` + existing `DispatcherProvider`).

## Verify

- `:screen-vehicle:compileCommonMainKotlinMetadata`, then `:androidApp:assembleDebug`.
- On device: Register Vehicle → Vehicle Type is a dropdown; pick Truck → Fuel shows only
  Diesel (auto-selected); pick Car → all 5; Day + Night; field heights regular.
