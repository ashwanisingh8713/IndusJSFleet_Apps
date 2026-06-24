package com.indusjs.fleet.data.database.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Entity for caching customers locally using Settings-based storage.
 */
@Serializable
data class CustomerEntity(
    @SerialName("id")
    val id: Int,
    val companyName: String,
    val personName: String,
    val primaryContact: String,
    val secondaryContact: String? = null,
    val companyAddress: String? = null,
    val email: String? = null,
    val gstNumber: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val ownerId: Int? = null,
    val createdById: Int? = null,
    // UTC epoch-millis (mirrors CustomerDto). 0/null = unset.
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val cachedAt: Long = 0 // Timestamp when cached
)

