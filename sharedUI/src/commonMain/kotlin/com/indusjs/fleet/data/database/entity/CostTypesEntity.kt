package com.indusjs.fleet.data.database.entity

import com.indusjs.fleet.data.model.DbEntity
import kotlinx.serialization.Serializable

/**
 * Entity for caching Trip Cost Types locally.
 * Stores the complete cost types data fetched from API.
 *
 * This is saved once on first app launch and never updated again.
 */
@Serializable
data class TripCostTypesEntity(
    val id: Int = 1, // Singleton - only one record
    val categoryId: String,
    val categoryName: String,
    val groupsJson: String, // JSON serialized list of CostTypeGroupDto
    val savedAt: Long // Timestamp when data was saved
) : DbEntity

/**
 * Entity for caching Maintenance Cost Types locally.
 * Stores the complete cost types data fetched from API.
 *
 * This is saved once on first app launch and never updated again.
 */
@Serializable
data class MaintenanceCostTypesEntity(
    val id: Int = 1, // Singleton - only one record
    val categoryId: String,
    val categoryName: String,
    val groupsJson: String, // JSON serialized list of CostTypeGroupDto
    val savedAt: Long // Timestamp when data was saved
) : DbEntity

/**
 * Entity for caching Driver Cost Types locally.
 * Stores the complete cost types data fetched from API.
 *
 * This is saved once on first app launch and never updated again.
 */
@Serializable
data class DriverCostTypesEntity(
    val id: Int = 1, // Singleton - only one record
    val categoryId: String,
    val categoryName: String,
    val groupsJson: String, // JSON serialized list of CostTypeGroupDto
    val savedAt: Long // Timestamp when data was saved
) : DbEntity
