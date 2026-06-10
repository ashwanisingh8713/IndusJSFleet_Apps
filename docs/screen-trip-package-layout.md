# screen-trip — package layout (reference)

Module path: [`screen-trip`](../screen-trip/). Android namespace: `com.ijs.trip`. All sources below are under `src/commonMain/kotlin/` unless noted.

## Layer map

| Layer | Package | Responsibility |
|--------|---------|----------------|
| Presentation (root) | `com.ijs.trip.presentation` | Trip list screen, shared contracts, feature facade |
| Presentation — create | `...presentation.create` | Create trip flow |
| Presentation — detail | `...presentation.detail` | Trip detail, stops, costs, payments UI, MVI VM |
| Presentation — cost | `...presentation.cost` | Trip cost entry screen + VM |
| Presentation — cost util | `...presentation.cost.util` | Mapping helpers (e.g. trip cost → driver cost) |
| Domain | `com.ijs.trip.domain.entity` | `Trip`, stops, status enums, create/update DTOs |
| Domain | `com.ijs.trip.domain.repository` | `TripRepository` contract |
| Domain | `com.ijs.trip.domain.usecase` | Metro `@Inject` use cases (`GetTripsUseCase`, stop CRUD, etc.) |
| Data | `com.ijs.trip.data.model` | Ktor JSON DTOs (`TripDto`, stop requests/responses) |
| Data | `com.ijs.trip.data.datasource` | `TripRemoteDataSource` (+ impl): HTTP + serialization |
| Data | `com.ijs.trip.data.mapper` | `TripMapper`, `TripStopMapper` (DTO ↔ domain) |
| Data | `com.ijs.trip.data.repository` | `TripRepositoryImpl` |
| Cross-cutting | `com.ijs.trip` | `LogTags` |

## Files by package

### `com.ijs.trip`

- `LogTags.kt` — logging tags for the module.

### `com.ijs.trip.presentation`

- `TripFeatureFacade.kt` — **integration surface for sharedUI**: `TripsListEntry`, `CreateTripEntry`, `TripDetailEntry`, `TripCostEntryEntry`.
- `TripsScreen.kt`, `TripsViewModel.kt`, `TripsContract.kt` — list + MVI.
- `TripStateOptions.kt` — trip status UI/options helpers.

### `com.ijs.trip.presentation.create`

- `CreateTripScreen.kt`, `CreateTripViewModel.kt`, `CreateTripContract.kt`.

### `com.ijs.trip.presentation.detail`

- `TripDetailScreen.kt`, `TripDetailViewModel.kt`, `TripDetailContract.kt` — primary detail MVI.
- `TripDetailViewContent.kt`, `TripDetailComponents.kt`, `TripDetailInfoSections.kt`, `TripDetailEditContent.kt`, `TripDetailEditForms.kt` — UI decomposition.
- `TripDetailActionHandler.kt`, `TripDetailDataLoader.kt`, `TripDetailStateManager.kt`, `TripDetailLocationHandler.kt` — coordination helpers.
- `TripCostsContent.kt`, `TripPaymentsContent.kt` — embedded sections.

### `com.ijs.trip.presentation.cost`

- `TripCostEntryScreen.kt`, `TripCostEntryViewModel.kt`, `TripCostEntryContract.kt`.

### `com.ijs.trip.presentation.cost.util`

- `TripCostToDriverCostMapper.kt`.

### `com.ijs.trip.domain.entity`

- `Trip.kt` — domain models including `TripStop`, `CreateTripStopData`, `UpdateTripStopData`, trip aggregates.

### `com.ijs.trip.domain.repository`

- `TripRepository.kt` — trips + stops + costs-related repository API consumed by use cases.

### `com.ijs.trip.domain.usecase`

- `TripUseCases.kt` — use cases: list/get/create/update trip, status, cancel, and **trip stops** (`GetTripStopsUseCase`, `CreateTripStopUseCase`, `UpdateTripStopUseCase`, mark complete, delete). Uses `com.indusjs.error.result.Result` and `UseCase` from shared core.

### `com.ijs.trip.data.model`

- `TripDto.kt` — API request/response types (including stop DTOs and wrappers).

### `com.ijs.trip.data.datasource`

- `TripRemoteDataSource.kt` — interface + Ktor-backed implementation for all trip/stop endpoints.

### `com.ijs.trip.data.mapper`

- `TripMapper.kt` — includes **`TripStopMapper`** (same file as trip mapping).

### `com.ijs.trip.data.repository`

- `TripRepositoryImpl.kt` — wires remote datasource, `TripMapper`, `TripStopMapper`, token access.

## Wiring outside this module

- **Navigation**: [`sharedUI`](../sharedUI/src/commonMain/kotlin/com/indusjs/fleet/navigation/FleetNavigation.kt) calls `TripFeatureFacade` entries and supplies ViewModels from [`DefaultViewModelProvider`](../sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/DefaultViewModelProvider.kt).
- **Repository construction**: [`FeatureRepositoryFactory`](../sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/FeatureRepositoryFactory.kt) builds `TripRepositoryImpl` with `TripRemoteDataSourceImpl`, `TripMapper`, `TripStopMapper`.

## Dependency graph (module level)

`screen-trip` → `api(:ijs-network-lib)` (+ Compose conventions → `ijs-ui-components-lib`, etc.). Cross-feature `implementation` projects per [`screen-trip/build.gradle.kts`](../screen-trip/build.gradle.kts): `screen-vehicle`, `screen-driver`, `screen-customer`, `screen-trip-payment`, plus `ijs-pdf-report` and `ijs-datetime-picker`.
