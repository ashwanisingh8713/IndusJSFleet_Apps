package com.indusjs.fleet.data.database.entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
/**
 * Entity for caching team members in local storage.
 */
@Serializable
data class TeamMemberEntity(
    val id: Int,
    val email: String,
    val mobile: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    @SerialName("owner_id")
    val ownerId: Int,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    val fullName: String get() = "$firstName $lastName"
}
/**
 * Wrapper entity for the team members list cache.
 */
@Serializable
data class TeamMembersListEntity(
    val members: List<TeamMemberEntity>,
    @SerialName("last_updated")
    val lastUpdated: Long = 0L
)
