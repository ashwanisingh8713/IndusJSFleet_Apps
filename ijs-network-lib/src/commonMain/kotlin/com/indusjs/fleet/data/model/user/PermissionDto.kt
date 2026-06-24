@file:OptIn(ExperimentalSerializationApi::class)

package com.indusjs.fleet.data.model.user

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * Response for `GET /api/v1/me/permissions`.
 *
 * Shape: `{ "data": { "permissions": ["financials:read", ...] } }`
 */
@JsonIgnoreUnknownKeys
@Serializable
data class MyPermissionsResponse(
    @SerialName("data")
    val data: PermissionsData? = null
)

@JsonIgnoreUnknownKeys
@Serializable
data class PermissionsData(
    @SerialName("permissions")
    val permissions: List<String> = emptyList()
)

/**
 * Response for `GET /api/v1/team/roles`.
 *
 * Shape: `{ "data": { "roles": ["owner", ...], "count": N } }`
 */
@JsonIgnoreUnknownKeys
@Serializable
data class TeamRolesResponse(
    @SerialName("data")
    val data: TeamRolesData? = null
)

@JsonIgnoreUnknownKeys
@Serializable
data class TeamRolesData(
    @SerialName("roles")
    val roles: List<String> = emptyList()
)
