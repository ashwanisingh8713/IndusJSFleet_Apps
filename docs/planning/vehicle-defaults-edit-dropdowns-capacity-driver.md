# Vehicle: Register default, Edit dual dropdowns, remove Capacity, unassign-driver

Four requests (Register + Edit Vehicle):

1. **Register Vehicle default Vehicle Type = Truck** (was Car).
2. **Edit Vehicle: Vehicle Type + Fuel Type** → same config-driven dual dropdowns as Register
   (Fuel filtered by Vehicle Type). Currently FilterChip rows with a static 5-fuel list.
3. **Remove passenger Capacity from ALL UI places** (Register state, Edit field, Overview display).
   Keep the wire/DTO field (contract) — just stop showing/editing it.
4. **Edit Vehicle: selecting "No Driver" doesn't unassign.**

## Task 1 — Register default Truck

`AddVehicleContract.State`: `vehicleType = VehicleType.TRUCK`; default `fuelTypes = listOf("Diesel")`
(Truck's only fuel) so the dropdown shows Diesel before the config asset loads. `fuelType` stays
`"Diesel"`. The init config-seed already snaps correctly for Truck.

## Task 3 — Remove Capacity from UI (keep wire)

Remove (presentation only):
- `AddVehicleContract`: `seatingCapacity` field + `UpdateSeatingCapacity` intent.
- `AddVehicleViewModel`: the intent handler; submit uses `capacity = 4` (Vehicle entity default).
- `VehicleDetailContract`: `capacity` field + `UpdateCapacity` intent.
- `VehicleDetailViewModel`: the handler; drop `capacity` from load/exit/save copy() seeds; in
  `saveChanges` keep `capacity = currentVehicle.capacity` (preserve, no UI change).
- `VehicleDetailEditContent`: delete the Capacity `FleetInputField`.
- `VehicleDetailOverviewContent`: delete the capacity `EnhancedInfoRow` (line ~235).

KEEP (backend contract): `Vehicle.capacity`, `VehicleDto.capacity`, mapper, `CreateVehicleRequest`,
`UpdateVehicleRequest`. Update still sends existing capacity (no behavior change server-side).

## Task 2 — Edit Vehicle dual dropdowns + config fuel filtering

Mirror the Register screen (proven pattern). The config plumbing (`VehicleTypeConfigProvider`,
`VehicleTypeOption`/`FuelTypeLabel`, `vehicle_types.json`) and `FleetDropdown` are already in-module.

- `VehicleDetailContract.State`: add `vehicleTypeOptions: List<VehicleTypeOption>`,
  `fuelTypeLabels: List<FuelTypeLabel>`; add `vehicleTypeLabelFor()` / `fuelLabelFor()` helpers
  (copy from `AddVehicleContract`). Reuse the existing `fuelTypeOptions: List<String>` as the
  **filtered** fuel list.
- `VehicleDetailViewModel`: load config in init (VM already has `DispatcherProvider`); add
  `optionFor()` / `resolveFuel()`; `updateVehicleType` recomputes `fuelTypeOptions` + snaps fuel.
  **On vehicle load / enterEditMode**: seed `fuelTypeOptions` for the saved type, and — important —
  if the vehicle's saved fuel isn't in the config set for its type, **keep it in the options**
  (union) so an already-saved value stays visible rather than snapping/blanking.
- `VehicleDetailEditContent`: replace the Vehicle Type chip Column + Fuel Type chip Column with one
  side-by-side `FleetDropdown` row (like `AddVehicleScreen.VehicleTypeFuelRow`). Place it in the
  Vehicle Information card; fold `Color` in and drop the now-thin "Specifications" card (only Color
  was left after fuel moved + capacity removed).

## Task 4 — "No Driver" doesn't unassign → BACKEND GAP (flag to A)

Root cause is server-side and B cannot edit the backend repo:
- Client already sends `"assigned_driver_id": null` on unassign (Json `explicitNulls=true`).
- Backend `vehicle/usecase.go buildUpdateMap` (~L273): `if input.AssignedDriverID != nil { ... }`.
  A JSON `null` → Go `nil` pointer → the field is **skipped** → driver unchanged. There is no
  unassign endpoint and no `0`-sentinel. So PUT cannot clear an assignment as written.

Action (per "B is client, flag gaps to A, don't drive the contract"): write a precise note to
`backend.inbox` proposing the minimal fix (treat `assigned_driver_id: 0` as unassign → set column
NULL, **or** distinguish explicit-null from absent, **or** add an unassign endpoint). Do **not**
ship a guessed sentinel from the client (sending `0` against today's backend would set an invalid
FK). Once A picks a convention, the client change is ~1 line. Tell the user this is backend-bound.

## Verify

`:screen-vehicle:compileCommonMainKotlinMetadata` → `:androidApp:assembleDebug`; install `-r -d -t`.
Device: Register opens on **Truck** (Fuel=Diesel only). Edit shows two dropdowns, fuel filtered,
saved values preserved; no Capacity field anywhere; Overview has no Capacity row. Day + Night.
