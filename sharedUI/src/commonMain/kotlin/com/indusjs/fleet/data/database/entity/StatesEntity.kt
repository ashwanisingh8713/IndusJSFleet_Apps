package com.indusjs.fleet.data.database.entity

import com.indusjs.fleet.data.model.DbEntity
import kotlinx.serialization.Serializable

/**
 * Entity for caching Entity States locally.
 * Stores the complete states data fetched from GET /states API.
 *
 * Contains vehicle states, driver states, trip states, and payment states.
 * This is saved once on first app launch and refreshed as needed.
 */
@Serializable
data class StatesEntity(
    val id: Int = 1, // Singleton - only one record
    val vehicleStatesJson: String, // JSON serialized list of StateItemDto
    val driverStatesJson: String,  // JSON serialized list of StateItemDto
    val tripStatesJson: String,    // JSON serialized list of StateItemDto
    val paymentStatesJson: String, // JSON serialized list of StateItemDto
    val savedAt: Long // Timestamp when data was saved
) : DbEntity

