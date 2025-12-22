package com.indusjs.fleet.feature.sample.domain.entity

import com.indusjs.fleet.domain.entity.Entity

/**
 * Sample User entity in the domain layer.
 * This represents the core business model.
 */
data class User(
    val id: String,
    val name: String,
    val email: String
) : Entity

