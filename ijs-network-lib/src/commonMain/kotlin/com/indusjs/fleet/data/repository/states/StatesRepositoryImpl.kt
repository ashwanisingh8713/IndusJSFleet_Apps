package com.indusjs.fleet.data.repository.states

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.data.datasource.states.StatesLocalDataSource
import com.indusjs.fleet.data.datasource.states.StatesRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.states.StateItemDto
import com.indusjs.fleet.data.model.states.StatesDataDto
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.indusjs.fleet.network.TAG_STATES_REPO
import com.indusjs.error.result.Result
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of StatesRepository with local caching.
 *
 * Follows the same cache-first pattern as CostTypesRepositoryImpl:
 * 1. Check if states are cached in local storage
 * 2. If cached and valid → return cached data
 * 3. If not cached → fetch from API → save to local storage
 * 4. If API fails and no cache → use hardcoded fallback from StatusConstants
 */
@Inject
class StatesRepositoryImpl(
    private val remoteDataSource: StatesRemoteDataSource,
    private val localDataSource: StatesLocalDataSource?,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : StatesRepository {

    override suspend fun initializeStatesIfNeeded(): Result<Unit> {
        // Check if already cached locally
        if (localDataSource?.hasStatesCached() == true) {
            val cached = localDataSource.getStates()
            if (cached != null && cached.vehicleStates.isNotEmpty() && cached.tripStates.isNotEmpty()) {
                logger.d(TAG_STATES_REPO, "States already cached and valid, skipping initialization")
                logger.d(TAG_STATES_REPO, "Cached: vehicles=${cached.vehicleStates.size}, drivers=${cached.driverStates.size}, trips=${cached.tripStates.size}, payments=${cached.paymentStates.size}")
                return Result.Success(Unit)
            } else {
                logger.w(TAG_STATES_REPO, "Cached states are empty or corrupt, clearing and refetching...")
                localDataSource.clearCache()
            }
        }

        // Check if user is logged in before making API calls
        val authToken = userLocalDataSource.getAuthToken()
        if (authToken.isNullOrBlank()) {
            logger.d(TAG_STATES_REPO, "User not logged in, skipping states fetch from API")
            return Result.Success(Unit)
        }

        logger.d(TAG_STATES_REPO, "States not cached, fetching from API...")

        return try {
            val response = remoteDataSource.getStates(authToken)
            if (response.success && response.data != null) {
                localDataSource?.saveStates(response.data)
                logger.d(TAG_STATES_REPO, "States fetched and cached successfully: vehicles=${response.data.vehicleStates.size}, drivers=${response.data.driverStates.size}, trips=${response.data.tripStates.size}, payments=${response.data.paymentStates.size}")
                Result.Success(Unit)
            } else {
                logger.w(TAG_STATES_REPO, "Failed to fetch states: ${response.message}")
                // Don't fail — states will fallback to hardcoded
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            logger.e(TAG_STATES_REPO, "Error fetching states: ${e.message}", e)
            // Don't fail — states will fallback to hardcoded
            Result.Success(Unit)
        }
    }

    override fun getStates(): Flow<Result<StatesDataDto>> = flow {
        emit(Result.Loading)

        // Try local cache first
        val cached = localDataSource?.getStates()
        if (cached != null && cached.vehicleStates.isNotEmpty()) {
            emit(Result.Success(cached))
            return@flow
        }

        // Fetch from API
        val token = userLocalDataSource.getAuthToken()
        if (token.isNullOrBlank()) {
            // Return hardcoded fallback
            emit(Result.Success(getHardcodedStates()))
            return@flow
        }

        val response = remoteDataSource.getStates(token)
        if (response.success && response.data != null) {
            localDataSource?.saveStates(response.data)
            emit(Result.Success(response.data))
        } else {
            // Fallback to hardcoded
            emit(Result.Success(getHardcodedStates()))
        }
    }

    override suspend fun refreshStates(): Result<StatesDataDto> {
        val token = userLocalDataSource.getAuthToken()
        if (token.isNullOrBlank()) {
            return Result.Error(Exception("Not authenticated"), "Please log in to continue")
        }

        val response = remoteDataSource.getStates(token)
        return if (response.success && response.data != null) {
            localDataSource?.clearCache()
            localDataSource?.saveStates(response.data)
            logger.d(TAG_STATES_REPO, "States refreshed successfully")
            Result.Success(response.data)
        } else {
            Result.Error(Exception(response.message), response.message ?: "Failed to refresh states")
        }
    }

    override suspend fun getCachedStates(): StatesDataDto? {
        return localDataSource?.getStates()
    }

    override suspend fun getVehicleStatesFlat(): List<Pair<String, String>> {
        val cached = localDataSource?.getStates()
        return if (cached != null && cached.vehicleStates.isNotEmpty()) {
            cached.vehicleStates.map { it.value to it.label }
        } else {
            StatusConstants.VehicleState.ALL.map { it to StatusConstants.VehicleState.getDisplayLabel(it) }
        }
    }

    override suspend fun getDriverStatesFlat(): List<Pair<String, String>> {
        val cached = localDataSource?.getStates()
        return if (cached != null && cached.driverStates.isNotEmpty()) {
            cached.driverStates.map { it.value to it.label }
        } else {
            StatusConstants.DriverState.ALL.map { it to StatusConstants.DriverState.getDisplayLabel(it) }
        }
    }

    override suspend fun getTripStatesFlat(): List<Pair<String, String>> {
        val cached = localDataSource?.getStates()
        return if (cached != null && cached.tripStates.isNotEmpty()) {
            cached.tripStates.map { it.value to it.label }
        } else {
            StatusConstants.TripState.ALL.map { it to StatusConstants.TripState.getDisplayLabel(it) }
        }
    }

    override suspend fun getPaymentStatesFlat(): List<Pair<String, String>> {
        val cached = localDataSource?.getStates()
        return if (cached != null && cached.paymentStates.isNotEmpty()) {
            cached.paymentStates.map { it.value to it.label }
        } else {
            // Hardcoded payment states fallback (matches server response)
            listOf(
                "received" to "Received",
                "pending" to "Pending",
                "cancelled" to "Cancelled",
                "partial" to "Partial"
            )
        }
    }

    override suspend fun hasStatesCached(): Boolean {
        return localDataSource?.hasStatesCached() == true
    }

    /**
     * Hardcoded fallback states derived from StatusConstants.
     * Used when API fails and no local cache exists.
     */
    private fun getHardcodedStates(): StatesDataDto {
        return StatesDataDto(
            vehicleStates = StatusConstants.VehicleState.ALL.map {
                StateItemDto(value = it, label = StatusConstants.VehicleState.getDisplayLabel(it))
            },
            driverStates = StatusConstants.DriverState.ALL.map {
                StateItemDto(value = it, label = StatusConstants.DriverState.getDisplayLabel(it))
            },
            tripStates = StatusConstants.TripState.ALL.map {
                StateItemDto(value = it, label = StatusConstants.TripState.getDisplayLabel(it))
            },
            paymentStates = listOf(
                StateItemDto(value = "received", label = "Received", color = "#4CAF50"),
                StateItemDto(value = "pending", label = "Pending", color = "#FF9800"),
                StateItemDto(value = "cancelled", label = "Cancelled", color = "#F44336"),
                StateItemDto(value = "partial", label = "Partial", color = "#2196F3")
            )
        )
    }
}
