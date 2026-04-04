package com.indusjs.fleet.data.datasource.states

import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.states.StatesDataDto

/**
 * Local data source interface for Entity States caching.
 * Abstracts the underlying storage mechanism.
 *
 * States are fetched on first app launch and can be refreshed manually.
 *
 * NOTE: Interface lives in ijs-network-lib.
 * Implementation (StatesLocalDataSourceImpl) lives in sharedUI (uses Settings DAO).
 */
interface StatesLocalDataSource : LocalDataSource {
    suspend fun getStates(): StatesDataDto?
    suspend fun saveStates(data: StatesDataDto)
    suspend fun hasStatesCached(): Boolean
    suspend fun clearCache()
}

