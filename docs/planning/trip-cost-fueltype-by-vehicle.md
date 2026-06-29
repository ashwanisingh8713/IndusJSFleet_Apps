# Add Trip Cost — Fuel cost type follows the vehicle's fuel

## Request + chosen behavior
On Add Trip Cost, under the **Fuel & Energy** category, the **Cost Type** chips should reflect the
trip's **vehicle's exact fuel type**. User chose **"Pre-select + keep AdBlue/EV"**:
- Pre-select the chip matching the vehicle's fuel (e.g. Diesel vehicle → Diesel selected).
- Keep the non-fuel-specific options **EV Charging (TC-001-004)** and **AdBlue/DEF (TC-001-005)** visible.
- Hide the mismatched primary fuels (e.g. hide Petrol/CNG for a Diesel vehicle).

## Facts
- Fuel group `TC-G-001` items: Petrol `TC-001-001`, Diesel `TC-001-002`, CNG/LPG `TC-001-003`,
  EV Charging `TC-001-004`, AdBlue/DEF `TC-001-005` (`CostApiResponses.kt` / `CostTypeSelection`).
- Vehicle fuel values: Petrol/Diesel/CNG/Electric/Hybrid (`Vehicle.fuelType`, e.g. "diesel").
- The cost screen's `selectedTrip` (domain `Trip`) has `vehicleId` but NOT the vehicle's config fuel
  (`trip.fuelType` is the *logged trip fuel*, usually null). So fetch the vehicle.
- `GetVehicleByIdUseCase` is already in DI (`DefaultViewModelProvider:334`); `TripCostEntryViewModel`
  factory at `:530`. screen-trip already depends on screen-vehicle (CreateTrip uses vehicle usecases).

## Mapping (vehicle fuel → fuel cost-type id)
diesel→TC-001-002, petrol→TC-001-001, cng/lpg→TC-001-003, electric→TC-001-004, hybrid→TC-001-001.
**Visible set** = { matchedId, TC-001-004 (EV), TC-001-005 (AdBlue) } (dedup). Unknown fuel → null = show all (graceful).

## Changes
1. **DI** `DefaultViewModelProvider.tripCostEntryViewModel()`: pass `getVehicleByIdUseCase`.
2. **`TripCostEntryViewModel`**: add ctor param `getVehicleByIdUseCase`. In `selectTrip(trip)`, after
   setting `selectedTrip`, launch `getVehicleByIdUseCase(trip.vehicleId)`; on success store
   `vehicleFuelType` and **pre-select** the matched fuel cost type on any entry whose category is the
   fuel group and whose `costType` is blank (don't override a user choice).
3. **`TripCostEntryContract.State`**: add `vehicleFuelType: String? = null`; helpers
   `fuelCostTypeIdFor(fuel)`, `visibleFuelCostTypeIds()`, and `costTypeGroupsForVehicle()` (returns
   `costTypeGroups` with the `TC-G-001` group's items filtered to the visible set when fuel known).
4. **`TripCostEntryScreen`**: pass `state.costTypeGroupsForVehicle()` to the cost-type selector
   instead of `state.costTypeGroups`.

## Safety
- Unknown/failed vehicle fuel → show all 5 fuels, no pre-select (current behavior). Non-fuel
  categories (Maintenance, etc.) untouched. No backend/DTO change. Pre-select never overrides a
  non-blank user selection.

## Verify
`:screen-trip` + `:sharedUI` compile, `:androidApp:assembleDebug`, install `-r -d -t`. Device: open a
Diesel vehicle's trip → Add Trip Cost → Fuel & Energy shows Diesel (selected) + EV Charging + AdBlue;
Petrol/CNG hidden.
