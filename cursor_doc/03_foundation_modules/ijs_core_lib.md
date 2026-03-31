# ijs-core-lib — Shared Foundation

**Namespace:** `com.indusjs.fleet.core`
**Depends on:** `ijs-error-lib` (api), `ijs-dispatcher-lib` (api), `ijs-datetime-utils` (api)

All three dependencies are re-exported via `api()`, so any module depending on `ijs-core-lib` gets them transitively.

## File Tree

```
ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/
├── core/
│   ├── constants/StatusConstants.kt       # Vehicle/Driver/Trip states, transitions
│   ├── error/FleetErrorContext.kt         # ErrorContext implementations for fleet screens
│   ├── logger/FleetLogger.kt             # Logger interface (impl in ijs-logger-lib)
│   ├── model/shared/
│   │   ├── CaretakerInfo.kt              # Caretaker assignment data
│   │   ├── SelectableCustomer.kt         # Customer picker item
│   │   ├── SelectableDriver.kt           # Driver picker item
│   │   ├── SelectableTrip.kt             # Trip picker item
│   │   └── SelectableVehicle.kt          # Vehicle picker item
│   ├── mvi/
│   │   ├── MviContract.kt               # UiState, UiIntent, UiEffect markers
│   │   ├── MviExtensions.kt             # HandleEffects, collectState, intentHandler
│   │   └── MviViewModel.kt              # Base MVI ViewModel
│   └── util/
│       ├── CostTypeUtils.kt             # Cost type display names, icons, colors
│       ├── FormatUtils.kt               # Currency (INR), percentage, distance formatting
│       ├── PermissionUtils.kt           # Role-based permission checks
│       ├── PhoneCallUtil.kt             # expect/actual phone dialer
│       ├── TimeUtils.kt                 # Relative time, ISO conversion, display helpers
│       └── ValidationUtils.kt           # Date, time, email, mobile, amount validation
├── data/
│   ├── datasource/DataSource.kt          # LocalDataSource, RemoteDataSource interfaces
│   ├── mapper/Mapper.kt                  # Mapper<D,E> interface with list extensions
│   └── model/
│       ├── DataModels.kt                 # Dto, DbEntity marker interfaces
│       ├── costs/
│       │   ├── CostApiResponses.kt       # API response wrappers for costs
│       │   ├── CostBreakdownItemDto.kt   # Cost breakdown for reports
│       │   ├── CostEntityModels.kt       # Trip/maintenance/vehicle cost DTOs
│       │   ├── CostModels.kt            # Cost type category/group/item DTOs
│       │   └── CostTypeSelection.kt     # Selected cost type model
│       ├── driver/DriverCostModels.kt    # Driver cost DTOs and request models
│       ├── history/HistoryDto.kt         # Activity history items
│       └── state/StateHistoryDto.kt      # State transition history
└── domain/
    ├── entity/Entity.kt                  # Entity marker interface
    ├── entity/user/User.kt               # User, UserRole, UserProfile, AuthResult
    ├── repository/Repository.kt          # Repository marker interface
    └── usecase/UseCase.kt                # UseCase, UseCaseWithParams, SuspendUseCase variants
```

**Platform source sets:** `androidMain`, `iosMain`, `jsMain`, `wasmJsMain` — each provides `actual` for:
- `PhoneCallUtil` (phone dialer composable)
- `TimeUtils.currentTimeMillis()` (platform time)

## StatusConstants

### Vehicle States
`INACTIVE`, `ACTIVE`, `ON_ROUTE`, `MAINTENANCE`, `DAMAGED`, `DECOMMISSIONED`

Grouped: `ALL`, `CORE`, `AVAILABLE_FOR_ASSIGNMENT`, `UNAVAILABLE`

Helpers: `getDisplayLabel()`, `getIcon()`, `getColorScheme()`, `isValid()`, `isAvailableForAssignment()`

### Driver States
`INACTIVE`, `ACTIVE`, `ON_ROUTE`, `ON_LEAVE`, `SUSPENDED`, `TERMINATED`

### Trip States
`PLANNED`, `ON_ROUTE`, `COMPLETED`, `CANCELLED`, `FAILED`, `DELAYED`

Grouped: `ACTIVE`, `FINISHED`, `EDITABLE`, `CANCELLABLE`

### State Transitions
`VehicleTransitions`, `DriverTransitions`, `TripTransitions` — each with:
- `canTransition(from, to): Boolean`
- `getValidTransitions(from): List<String>`
- `validateTransition(from, to): TransitionResult` (Valid or Invalid with message)

### StateColorScheme
Enum: `SUCCESS`, `WARNING`, `ERROR`, `INFO`, `NEUTRAL`

## Utility Functions

### ValidationUtils
- `validateDate(date)`, `isValidDate()`, `getDateError()` — DD-MM-YYYY with leap year support
- `validateTime(time, required)`, `isValidTime()`, `getTimeError()`
- `validateAmount(amount)`, `getAmountError()`
- `isValidEmail(email)`, `getEmailError()`
- `isValidMobile(mobile)` (≥10 digits), `getMobileError()`
- `validateCostEntry()` — validates entire cost entry form

### FormatUtils (top-level functions)
- `formatCurrency(amount)` — INR grouping (e.g., "₹1,23,456.00")
- `formatCurrencyFull(amount)` — with "INR" prefix
- `formatPercentage(value)`, `formatDistance(km)`, `formatWeight(kg)`

### CostTypeUtils
- `getDisplayName(costTypeId)`, `getIcon(costTypeId)`, `getColor(costTypeId)`
- Composable color helpers for Material theme integration

### PermissionUtils
Role-based checks: `canViewFinancials(role)`, `canManageTeam(role)`, `canEditTrips(role)`, `canDeleteCosts(role)`, etc.

### TimeUtils
- `currentTimeMillis()` — expect/actual per platform
- `formatRelativeTime(isoDate)` — "2 hours ago", "Yesterday"
- `convertFormattedToIsoDateTime(date, time)` — DD-MM-YYYY + HH:MM → ISO 8601
- Display helpers delegating to `FleetDateTime`

## Base Interfaces

| Interface | Package | Purpose |
|-----------|---------|---------|
| `Entity` | `domain.entity` | Marker for domain entities |
| `Repository` | `domain.repository` | Marker for repository interfaces |
| `UseCase<T>` | `domain.usecase` | `operator fun invoke(): Flow<Result<T>>` |
| `UseCaseWithParams<P, T>` | `domain.usecase` | `operator fun invoke(params: P): Flow<Result<T>>` |
| `SuspendUseCase<T>` | `domain.usecase` | `suspend operator fun invoke(): Result<T>` |
| `SuspendUseCaseWithParams<P, T>` | `domain.usecase` | `suspend operator fun invoke(params: P): Result<T>` |
| `LocalDataSource` | `data.datasource` | Marker for local data sources |
| `RemoteDataSource` | `data.datasource` | Marker for remote data sources |
| `Mapper<D, E>` | `data.mapper` | `mapToDomain(D): E`, `mapToData(E): D`, list extensions |
| `Dto` | `data.model` | Marker for DTOs |
| `DbEntity` | `data.model` | Marker for Room entities |

## Shared DTOs (Cross-Feature)

These live in `ijs-core-lib` to avoid circular dependencies between feature modules:

### Cost Types (`CostModels.kt`)
- `CostTypeCategoryDto` → `CostTypeGroupDto` → `CostTypeItemDto` (3-level hierarchy)

### Cost Entries (`CostEntityModels.kt`)
- `TripCostDto`, `CreateTripCostRequest`, `BulkCreateTripCostsRequest`
- `MaintenanceCostDto`, `CreateMaintenanceCostRequest`, `BulkCreateMaintenanceCostsRequest`
- `TripCostSummaryDto`, `VehicleTripCostsDataDto`, `VehicleMaintenanceCostsDataDto`

### Driver Costs (`DriverCostModels.kt`)
- `DriverCostDto`, `CreateDriverCostRequest`, `BulkCreateDriverCostsRequest`
- `DriverCostsSummaryDto` (totalEarnings, totalDeductions, netAmount)

### History (`HistoryDto.kt`)
- `HistoryItemDto` — entity type, action, field changes, performer info, timestamps

### State History (`StateHistoryDto.kt`)
- `StateUpdateRequestDto`, `StateHistoryItemDto` — state transitions with reason/notes

### Selection Models
- `SelectableVehicle`, `SelectableDriver`, `SelectableTrip`, `SelectableCustomer`
- `CaretakerInfo` — for caretaker assignment dropdowns

### User Domain (`User.kt`)
- `UserRole` enum (OWNER, GENERAL_MANAGER, MANAGER, SUPERVISOR, DRIVER)
- `User`, `UserProfile`, `OrganizationStats`, `OwnerInfo`, `AuthResult`
