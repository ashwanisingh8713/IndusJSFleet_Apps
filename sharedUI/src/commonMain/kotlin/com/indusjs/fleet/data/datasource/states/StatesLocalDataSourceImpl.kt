package com.indusjs.fleet.data.datasource.states

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.TAG_STATES_LOCAL_DS
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.dao.StatesDao
import com.indusjs.fleet.data.database.entity.StatesEntity
import com.indusjs.fleet.data.model.states.StateItemDto
import com.indusjs.fleet.data.model.states.StatesDataDto
import dev.zacsweers.metro.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of StatesLocalDataSource using Settings DAO.
 *
 * The interface [StatesLocalDataSource] is defined in ijs-network-lib.
 */
@Inject
class StatesLocalDataSourceImpl(
    private val statesDao: StatesDao,
    private val json: Json,
    private val logger: FleetLogger
) : StatesLocalDataSource {

    override suspend fun getStates(): StatesDataDto? {
        return try {
            statesDao.getStates()?.let { entity ->
                val vehicleStates = json.decodeFromString<List<StateItemDto>>(entity.vehicleStatesJson)
                val driverStates = json.decodeFromString<List<StateItemDto>>(entity.driverStatesJson)
                val tripStates = json.decodeFromString<List<StateItemDto>>(entity.tripStatesJson)
                val paymentStates = json.decodeFromString<List<StateItemDto>>(entity.paymentStatesJson)
                StatesDataDto(
                    vehicleStates = vehicleStates,
                    driverStates = driverStates,
                    tripStates = tripStates,
                    paymentStates = paymentStates
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_STATES_LOCAL_DS, "Failed to get states from cache: ${e.message}", e)
            null
        }
    }

    override suspend fun saveStates(data: StatesDataDto) {
        try {
            val entity = StatesEntity(
                id = 1,
                vehicleStatesJson = json.encodeToString(data.vehicleStates),
                driverStatesJson = json.encodeToString(data.driverStates),
                tripStatesJson = json.encodeToString(data.tripStates),
                paymentStatesJson = json.encodeToString(data.paymentStates),
                savedAt = currentTimeMillis()
            )
            statesDao.saveStates(entity)
            logger.d(TAG_STATES_LOCAL_DS, "Saved states to cache: vehicles=${data.vehicleStates.size}, drivers=${data.driverStates.size}, trips=${data.tripStates.size}, payments=${data.paymentStates.size}")
        } catch (e: Exception) {
            logger.e(TAG_STATES_LOCAL_DS, "Failed to save states: ${e.message}", e)
        }
    }

    override suspend fun hasStatesCached(): Boolean {
        return statesDao.hasStates()
    }

    override suspend fun clearCache() {
        statesDao.clearStates()
        logger.d(TAG_STATES_LOCAL_DS, "Cleared states cache")
    }
}

