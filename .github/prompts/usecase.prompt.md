# Use Case Template

Create use cases for the IndusJS Fleet app following Clean Architecture principles.

## Feature Information
- **Feature Name**: [FEATURE_NAME]
- **Entity Name**: [ENTITY_NAME]
- **Operations**: [LIST_OPERATIONS]

---

## Use Case Patterns

### Base Use Case Interfaces

```kotlin
// For operations returning Flow
interface UseCase<out R> {
    operator fun invoke(): Flow<Result<R>>
}

// For operations with params returning Flow
interface UseCaseWithParams<in P, out R> {
    operator fun invoke(params: P): Flow<Result<R>>
}

// For suspend operations without params
interface SuspendUseCase<out R> {
    suspend operator fun invoke(): Result<R>
}

// For suspend operations with params
interface SuspendUseCaseWithParams<in P, out R> {
    suspend operator fun invoke(params: P): Result<R>
}
```

---

## Standard CRUD Use Cases Template

```kotlin
package com.indusjs.fleet.domain.usecase.{feature}

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.{feature}.{Entity}
import com.indusjs.fleet.domain.repository.{feature}.{Feature}Repository
import com.indusjs.fleet.domain.usecase.SuspendUseCase
import com.indusjs.fleet.domain.usecase.SuspendUseCaseWithParams
import dev.zacsweers.metro.Inject

// ==================== READ OPERATIONS ====================

/**
 * Use case for fetching list of {entities}.
 */
@Inject
class Get{Entity}sUseCase(
    private val repository: {Feature}Repository
) : SuspendUseCase<List<{Entity}>> {
    
    override suspend fun invoke(): Result<List<{Entity}>> {
        return repository.get{Entity}s()
    }
}

/**
 * Use case for fetching list with pagination.
 */
@Inject
class Get{Entity}sPagedUseCase(
    private val repository: {Feature}Repository
) {
    data class Params(
        val page: Int = 1,
        val perPage: Int = 10,
        val status: String? = null
    )
    
    suspend operator fun invoke(params: Params): Result<List<{Entity}>> {
        return repository.get{Entity}s(
            page = params.page,
            perPage = params.perPage,
            status = params.status
        )
    }
}

/**
 * Use case for fetching single {entity} by ID.
 */
@Inject
class Get{Entity}UseCase(
    private val repository: {Feature}Repository
) : SuspendUseCaseWithParams<String, {Entity}> {
    
    override suspend fun invoke(params: String): Result<{Entity}> {
        return repository.get{Entity}(id = params)
    }
}

// ==================== WRITE OPERATIONS ====================

/**
 * Use case for creating a new {entity}.
 */
@Inject
class Create{Entity}UseCase(
    private val repository: {Feature}Repository
) : SuspendUseCaseWithParams<{Entity}, {Entity}> {
    
    override suspend fun invoke(params: {Entity}): Result<{Entity}> {
        // Validate before creating
        val validationError = validate(params)
        if (validationError != null) {
            return Result.Error(IllegalArgumentException(validationError), validationError)
        }
        
        return repository.create{Entity}(params)
    }
    
    private fun validate(entity: {Entity}): String? {
        return when {
            entity.name.isBlank() -> "Name is required"
            // Add more validation rules
            else -> null
        }
    }
}

/**
 * Use case for updating an existing {entity}.
 */
@Inject
class Update{Entity}UseCase(
    private val repository: {Feature}Repository
) : SuspendUseCaseWithParams<{Entity}, {Entity}> {
    
    override suspend fun invoke(params: {Entity}): Result<{Entity}> {
        if (params.id.isBlank()) {
            return Result.Error(IllegalArgumentException("ID is required for update"), "ID is required")
        }
        return repository.update{Entity}(params)
    }
}

/**
 * Use case for deleting a {entity}.
 */
@Inject
class Delete{Entity}UseCase(
    private val repository: {Feature}Repository
) : SuspendUseCaseWithParams<String, Unit> {
    
    override suspend fun invoke(params: String): Result<Unit> {
        return repository.delete{Entity}(id = params)
    }
}

// ==================== STATUS/STATE OPERATIONS ====================

/**
 * Use case for updating {entity} state/status.
 */
@Inject
class Update{Entity}StateUseCase(
    private val repository: {Feature}Repository
) {
    data class Params(
        val id: String,
        val state: String
    )
    
    suspend operator fun invoke(params: Params): Result<{Entity}> {
        // Validate state transition
        val validStates = listOf("active", "inactive", "maintenance", "completed")
        if (params.state !in validStates) {
            return Result.Error(
                IllegalArgumentException("Invalid state: ${params.state}"),
                "Invalid state"
            )
        }
        
        return repository.update{Entity}State(params.id, params.state)
    }
}

/**
 * Use case for toggling {entity} active status.
 */
@Inject
class Toggle{Entity}ActiveUseCase(
    private val repository: {Feature}Repository
) : SuspendUseCaseWithParams<String, {Entity}> {
    
    override suspend fun invoke(params: String): Result<{Entity}> {
        return repository.toggle{Entity}Active(id = params)
    }
}

// ==================== FILTERED/SEARCH OPERATIONS ====================

/**
 * Use case for searching {entities}.
 */
@Inject
class Search{Entity}sUseCase(
    private val repository: {Feature}Repository
) {
    data class Params(
        val query: String,
        val status: String? = null,
        val limit: Int = 20
    )
    
    suspend operator fun invoke(params: Params): Result<List<{Entity}>> {
        if (params.query.length < 2) {
            return Result.Success(emptyList())
        }
        return repository.search{Entity}s(
            query = params.query,
            status = params.status,
            limit = params.limit
        )
    }
}

/**
 * Use case for getting available/unassigned {entities}.
 */
@Inject
class GetAvailable{Entity}sUseCase(
    private val repository: {Feature}Repository
) : SuspendUseCase<List<{Entity}>> {
    
    override suspend fun invoke(): Result<List<{Entity}>> {
        return repository.getAvailable{Entity}s()
    }
}
```

---

## Complex Use Cases

### Use Case with Multiple Repository Dependencies

```kotlin
/**
 * Use case requiring multiple repositories.
 */
@Inject
class AssignDriverToVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val driverRepository: DriverRepository
) {
    data class Params(
        val vehicleId: String,
        val driverId: String
    )
    
    suspend operator fun invoke(params: Params): Result<Vehicle> {
        // Check driver availability first
        when (val driverResult = driverRepository.getDriver(params.driverId)) {
            is Result.Error -> return Result.Error(driverResult.exception, "Driver not found")
            is Result.Success -> {
                if (!driverResult.data.isAvailable) {
                    return Result.Error(
                        IllegalStateException("Driver is not available"),
                        "Driver is already assigned to another vehicle"
                    )
                }
            }
            is Result.Loading -> { }
        }
        
        // Assign driver to vehicle
        return vehicleRepository.assignDriver(params.vehicleId, params.driverId)
    }
}
```

### Use Case with Business Logic

```kotlin
/**
 * Use case with complex business logic.
 */
@Inject
class CalculateTripCostUseCase(
    private val tripRepository: TripRepository,
    private val costRepository: CostRepository
) {
    data class Params(
        val tripId: String
    )
    
    data class TripCostSummary(
        val tripId: String,
        val fuelCost: Double,
        val tollCost: Double,
        val maintenanceCost: Double,
        val otherCosts: Double,
        val totalCost: Double,
        val profitMargin: Double?
    )
    
    suspend operator fun invoke(params: Params): Result<TripCostSummary> {
        // Fetch trip
        val tripResult = tripRepository.getTrip(params.tripId)
        if (tripResult is Result.Error) return Result.Error(tripResult.exception, tripResult.message)
        val trip = (tripResult as Result.Success).data
        
        // Fetch all costs for this trip
        val costsResult = costRepository.getTripCosts(params.tripId)
        if (costsResult is Result.Error) return Result.Error(costsResult.exception, costsResult.message)
        val costs = (costsResult as Result.Success).data
        
        // Calculate by type
        val fuelCost = costs.filter { it.type == "fuel" }.sumOf { it.amount }
        val tollCost = costs.filter { it.type == "toll" }.sumOf { it.amount }
        val maintenanceCost = costs.filter { it.type == "maintenance" }.sumOf { it.amount }
        val otherCosts = costs.filter { it.type !in listOf("fuel", "toll", "maintenance") }.sumOf { it.amount }
        val totalCost = fuelCost + tollCost + maintenanceCost + otherCosts
        
        // Calculate profit margin if selling price is available
        val profitMargin = trip.sellingPrice?.let { 
            if (it > 0) ((it - totalCost) / it) * 100 else null 
        }
        
        return Result.Success(
            TripCostSummary(
                tripId = params.tripId,
                fuelCost = fuelCost,
                tollCost = tollCost,
                maintenanceCost = maintenanceCost,
                otherCosts = otherCosts,
                totalCost = totalCost,
                profitMargin = profitMargin
            )
        )
    }
}
```

### Use Case with Flow for Real-time Updates

```kotlin
/**
 * Use case returning Flow for observing changes.
 */
@Inject
class ObserveVehicleLocationUseCase(
    private val vehicleRepository: VehicleRepository
) {
    operator fun invoke(vehicleId: String): Flow<Result<VehicleLocation>> {
        return vehicleRepository.observeVehicleLocation(vehicleId)
            .map { location ->
                Result.Success(location)
            }
            .catch { e ->
                emit(Result.Error(e, e.message))
            }
    }
}
```

---

## Usage in ViewModel

```kotlin
@Inject
class {Feature}ViewModel(
    private val get{Entity}sUseCase: Get{Entity}sUseCase,
    private val get{Entity}UseCase: Get{Entity}UseCase,
    private val create{Entity}UseCase: Create{Entity}UseCase,
    private val delete{Entity}UseCase: Delete{Entity}UseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Load{Entity}s -> load{Entity}s()
            is Intent.Select{Entity} -> load{Entity}Detail(intent.id)
            is Intent.Create{Entity} -> create{Entity}(intent.{entity})
            is Intent.Delete{Entity} -> delete{Entity}(intent.id)
        }
    }

    private suspend fun load{Entity}s() {
        updateState { copy(isLoading = true, error = null) }
        
        when (val result = get{Entity}sUseCase()) {
            is Result.Success -> updateState { 
                copy(isLoading = false, {entities} = result.data) 
            }
            is Result.Error -> updateState { 
                copy(isLoading = false, error = result.message) 
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun create{Entity}({entity}: {Entity}) {
        updateState { copy(isSubmitting = true) }
        
        when (val result = create{Entity}UseCase({entity})) {
            is Result.Success -> {
                sendEffect(Effect.{Entity}Created)
                sendEffect(Effect.ShowSnackbar("{Entity} created successfully"))
            }
            is Result.Error -> {
                updateState { copy(isSubmitting = false, error = result.message) }
                sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to create {entity}"))
            }
            is Result.Loading -> { }
        }
    }
}
```

---

## Validation Checklist

- [ ] Use case has `@Inject` annotation on class
- [ ] Single responsibility - one operation per use case
- [ ] Returns `Result<T>` for error handling
- [ ] Validation logic in use case, not repository
- [ ] Uses repository interface, not implementation
- [ ] Complex logic broken into private helper functions
- [ ] Params class for multiple parameters
- [ ] Proper error messages for validation failures

