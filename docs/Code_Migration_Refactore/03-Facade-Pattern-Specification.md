# 03 — Facade Pattern Specification

## Overview

Each feature module exposes a **single Facade class** as its public API surface. Internal ViewModels, Contracts, and Screens are implementation details — never referenced outside the module.

---

## Facade Design Principles

1. **Single entry point per module** — `{Feature}FeatureFacade`
2. **Composable screen functions** — each screen is a `@Composable` function on the Facade
3. **Navigation via lambdas only** — no `FleetRoute`, no `NavBackStack` references
4. **Cross-feature data via `ExternalDeps`** — injected at construction time
5. **Internal ViewModel lifecycle** — Facade manages ViewModel creation internally using `rememberViewModel` or similar Compose-scoped patterns

---

## Facade Template

```kotlin
package com.indusjs.{feature}.facade

import androidx.compose.runtime.Composable
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.{feature}.domain.repository.{feature}.{Feature}Repository

/**
 * Public API for the {Feature} module.
 *
 * sharedUI constructs this Facade and calls its @Composable screen functions
 * from FleetNavigation.kt, passing lambda callbacks for navigation.
 */
class {Feature}FeatureFacade(
    private val repository: {Feature}Repository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: {Feature}ExternalDeps? = null  // Only if cross-feature deps exist
) {

    @Composable
    fun {Feature}ListScreen(
        onNavigateToDetail: (id: String) -> Unit,
        onNavigateToCreate: () -> Unit,
        onNavigateBack: () -> Unit
    ) {
        // Create ViewModel internally — NOT exposed
        val viewModel = remember { {Feature}ListViewModel(repository, dispatcherProvider) }
        // Render the internal Screen composable
        Internal{Feature}ListScreen(
            viewModel = viewModel,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToCreate = onNavigateToCreate,
            onNavigateBack = onNavigateBack
        )
    }

    @Composable
    fun {Feature}DetailScreen(
        id: String,
        onNavigateBack: () -> Unit,
        // Feature-specific navigation callbacks
        onNavigateToRelated: ((relatedId: String) -> Unit)? = null
    ) {
        val viewModel = remember { {Feature}DetailViewModel(repository, dispatcherProvider, externalDeps) }
        Internal{Feature}DetailScreen(
            viewModel = viewModel,
            id = id,
            onNavigateBack = onNavigateBack,
            onNavigateToRelated = onNavigateToRelated
        )
    }
}
```

---

## ExternalDeps Interface Template

Only modules with cross-feature dependencies define this. Uses **shared contracts from `ijs-core-lib`** — never feature-specific domain entities.

```kotlin
package com.indusjs.{feature}.facade

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.model.shared.*

/**
 * Cross-feature data provider for {Feature} module.
 * Implemented by sharedUI's adapter layer.
 */
interface {Feature}ExternalDeps {
    suspend fun getSomeCrossFeatureData(): Result<List<SharedContract>>
}
```

---

## Per-Module Facade Specifications

### CustomerFeatureFacade (no ExternalDeps)

```kotlin
class CustomerFeatureFacade(
    private val customerRepository: CustomerRepository,
    private val refreshCustomersUseCase: RefreshCustomersUseCase,
    private val getCustomersUseCase: GetCustomersUseCase,
    private val getLocalCustomersUseCase: GetLocalCustomersUseCase,
    private val createCustomerUseCase: CreateCustomerUseCase
) {
    @Composable fun CustomerListScreen(onNavigateToDetail: (String) -> Unit, onNavigateToCreate: () -> Unit, onBack: () -> Unit)
    @Composable fun CustomerDetailScreen(customerId: String, onBack: () -> Unit)
    @Composable fun CreateCustomerScreen(onBack: () -> Unit, onCreated: (String) -> Unit)
}
```

### TeamFeatureFacade (no ExternalDeps)

```kotlin
class TeamFeatureFacade(
    private val teamRepository: TeamRepository,
    private val userLocalDataSource: UserLocalDataSource,
    private val dispatcherProvider: DispatcherProvider
) {
    @Composable fun TeamListScreen(onNavigateToCreate: () -> Unit, onNavigateToDetail: (String) -> Unit, onBack: () -> Unit)
    @Composable fun CreateTeamMemberScreen(excludeGeneralManager: Boolean, onBack: () -> Unit)
    @Composable fun TeamMemberDetailScreen(memberId: String, onBack: () -> Unit)
}
```

### VehicleFeatureFacade

```kotlin
interface VehicleExternalDeps {
    suspend fun getDrivers(): Result<List<SelectableDriver>>
    suspend fun getCaretakers(): Result<List<CaretakerInfo>>
}

class VehicleFeatureFacade(
    private val vehicleRepository: VehicleRepository,
    private val costsRepository: CostsRepository,
    private val costTypesRepository: CostTypesRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: VehicleExternalDeps
) {
    @Composable fun VehicleListScreen(onNavigateToDetail: (String) -> Unit, onNavigateToAdd: () -> Unit, onBack: () -> Unit)
    @Composable fun VehicleDetailScreen(vehicleId: String, onBack: () -> Unit, onNavigateToMaintenanceCost: (String) -> Unit, onRequestFilePicker: ..., onOpenDocumentPreview: ..., onDownloadDocument: ..., onSaveDocument: ...)
    @Composable fun AddVehicleScreen(onBack: () -> Unit, onVehicleRegistered: (String) -> Unit, onNavigateToCreateTeamMember: () -> Unit, onRequestFilePicker: ...)
    @Composable fun MaintenanceCostEntryScreen(initialVehicleId: String?, onBack: () -> Unit)
}
```

### DriverFeatureFacade

```kotlin
interface DriverExternalDeps {
    suspend fun getCaretakers(): Result<List<CaretakerInfo>>
}

class DriverFeatureFacade(
    private val driverRepository: DriverRepository,
    private val costsRepository: CostsRepository,
    private val costTypesRepository: CostTypesRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: DriverExternalDeps
) {
    @Composable fun DriverListScreen(onNavigateToDetail: (String) -> Unit, onNavigateToAdd: () -> Unit, onBack: () -> Unit)
    @Composable fun DriverDetailScreen(driverId: String, onBack: () -> Unit, onNavigateToDriverCost: (String) -> Unit)
    @Composable fun CreateDriverScreen(onBack: () -> Unit, onDriverCreated: (String) -> Unit)
    @Composable fun DriverCostEntryScreen(initialDriverId: String?, onBack: () -> Unit)
}
```

### TripFeatureFacade

```kotlin
interface TripExternalDeps {
    suspend fun getAvailableVehicles(): Result<List<SelectableVehicle>>
    suspend fun getAvailableDrivers(): Result<List<SelectableDriver>>
    suspend fun getCustomers(): Result<List<SelectableCustomer>>
    suspend fun refreshCustomers(): Result<Unit>
    suspend fun searchPlaces(query: String): List<PlacePrediction>
    suspend fun getPlaceDetails(placeId: String): PlaceDetails?
    suspend fun getUserRole(): String
}

class TripFeatureFacade(
    private val tripRepository: TripRepository,
    private val costsRepository: CostsRepository,
    private val costTypesRepository: CostTypesRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: TripExternalDeps
) {
    @Composable fun TripListScreen(onNavigateToDetail: (String) -> Unit, onNavigateToCreate: () -> Unit, onBack: () -> Unit)
    @Composable fun TripDetailScreen(tripId: String, onBack: () -> Unit, onNavigateToAddTripCost: (tripId: String, vehicleId: String) -> Unit, onNavigateToAddPayment: (tripId: String, vehicleId: String) -> Unit, onNavigateToAddCustomer: () -> Unit)
    @Composable fun CreateTripScreen(onBack: () -> Unit, onTripCreated: (String) -> Unit, onNavigateToAddCustomer: () -> Unit)
    @Composable fun TripCostEntryScreen(initialTripId: String?, onBack: () -> Unit)
}
```

### PaymentFeatureFacade

```kotlin
interface PaymentExternalDeps {
    suspend fun getTrips(): Result<List<SelectableTrip>>
    suspend fun getUserRole(): String
}

class PaymentFeatureFacade(
    private val paymentRepository: TripPaymentRepository,
    private val userRepository: UserRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: PaymentExternalDeps
) {
    @Composable fun PaymentListScreen(onNavigateToDetail: (String) -> Unit, onNavigateToAdd: (String?) -> Unit, onBack: () -> Unit)
    @Composable fun PaymentDetailScreen(paymentId: String, onBack: () -> Unit, onNavigateToEdit: (String) -> Unit)
    @Composable fun AddPaymentScreen(tripId: String?, paymentId: String?, onBack: () -> Unit)
}
```

### FinanceFeatureFacade

```kotlin
interface FinanceExternalDeps {
    suspend fun getVehicles(): Result<List<SelectableVehicle>>
}

class FinanceFeatureFacade(
    private val financeRepository: VehicleFinanceRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: FinanceExternalDeps
) {
    @Composable fun VehicleFinanceListScreen(onNavigateToDetail: (String) -> Unit, onNavigateToAddPurchase: () -> Unit, onBack: () -> Unit)
    @Composable fun VehicleFinanceDetailScreen(vehicleId: String, onBack: () -> Unit, onNavigateToEdit: (String) -> Unit, onNavigateToHistory: (String) -> Unit)
    @Composable fun AddPurchaseInfoScreen(onBack: () -> Unit)
    @Composable fun EmiPaymentHistoryScreen(vehicleId: String, onBack: () -> Unit)
}
```

### ReportsFeatureFacade

```kotlin
interface ReportsExternalDeps {
    suspend fun getVehicles(): Result<List<SelectableVehicle>>
    suspend fun getTrips(startDate: String?, endDate: String?): Result<List<SelectableTrip>>
}

class ReportsFeatureFacade(
    private val reportsRepository: ReportsRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val externalDeps: ReportsExternalDeps
) {
    @Composable fun ReportsHubScreen(onBack: () -> Unit, onNavigateToVehiclePL: () -> Unit, onNavigateToTripPL: () -> Unit, onNavigateToCostAnalysis: () -> Unit, onNavigateToConsolidatedPL: () -> Unit)
    @Composable fun VehicleProfitLossScreen(onBack: () -> Unit)
    @Composable fun TripProfitLossScreen(onBack: () -> Unit)
    @Composable fun CostAnalysisScreen(onBack: () -> Unit)
    @Composable fun ConsolidatedPLScreen(onBack: () -> Unit)
}
```

---

## sharedUI Wiring Example

```kotlin
// In FleetAppOrchestrator / DefaultViewModelProvider
private val customerFacade by lazy {
    CustomerFeatureFacade(
        customerRepository = featureRepos.customerRepository,
        refreshCustomersUseCase = refreshCustomersUseCase,
        getCustomersUseCase = getCustomersUseCase,
        getLocalCustomersUseCase = getLocalCustomersUseCase,
        createCustomerUseCase = createCustomerUseCase
    )
}

private val vehicleExternalDeps by lazy {
    object : VehicleExternalDeps {
        override suspend fun getDrivers() = driverRepository.getDrivers().map { drivers ->
            drivers.map { SelectableDriver(it.id, it.fullName, it.mobile, it.status.name) }
        }
        override suspend fun getCaretakers() = teamRepository.getTeamMembers().map { members ->
            members.map { CaretakerInfo(it.id, it.fullName, it.role, it.mobile) }
        }
    }
}
```

