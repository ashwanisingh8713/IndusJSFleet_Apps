package com.indusjs.fleet.feature.sample.data.model

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User Data Transfer Object for API responses.
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String
) : Dto

