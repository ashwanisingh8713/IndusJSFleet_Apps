package com.indusjs.fleet.data.datasource

/**
 * Base marker interface for local data sources.
 * Implementations handle local storage (Room, DataStore, etc.)
 */
interface LocalDataSource

/**
 * Base marker interface for remote data sources.
 * Implementations handle API calls (Ktor, etc.)
 */
interface RemoteDataSource

