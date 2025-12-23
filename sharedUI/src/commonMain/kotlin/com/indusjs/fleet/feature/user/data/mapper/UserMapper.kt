package com.indusjs.fleet.feature.user.data.mapper

import com.indusjs.fleet.feature.user.data.model.AuthResponseDto
import com.indusjs.fleet.feature.user.data.model.OwnerInfoDto
import com.indusjs.fleet.feature.user.data.model.OwnerStatsDto
import com.indusjs.fleet.feature.user.data.model.UserDto
import com.indusjs.fleet.feature.user.data.model.UserProfileDto
import com.indusjs.fleet.feature.user.domain.entity.AuthResult
import com.indusjs.fleet.feature.user.domain.entity.OrganizationStats
import com.indusjs.fleet.feature.user.domain.entity.OwnerInfo
import com.indusjs.fleet.feature.user.domain.entity.User
import com.indusjs.fleet.feature.user.domain.entity.UserProfile
import com.indusjs.fleet.feature.user.domain.entity.UserRole

/**
 * Mapper for converting between User DTOs and domain entities.
 */
object UserMapper {

    fun UserDto.toDomain(): User = User(
        id = id.toString(),
        email = email,
        mobile = mobile,
        firstName = firstName,
        lastName = lastName,
        role = role.toUserRole(),
        ownerId = ownerId?.toString(),
        createdAt = createdAt,
        updatedAt = updatedAt ?: createdAt
    )

    fun AuthResponseDto.toDomain(): AuthResult = AuthResult(
        user = user.toDomain(),
        token = token
    )

    fun OwnerStatsDto.toDomain(): OrganizationStats = OrganizationStats(
        totalManagers = totalManagers,
        totalSupervisors = totalSupervisors,
        totalDrivers = totalTeamMembers, // Map total_team_members to totalDrivers
        totalVehicles = totalVehicles,
        activeTrips = activeTrips
    )

    fun OwnerInfoDto.toDomain(): OwnerInfo = OwnerInfo(
        ownerId = ownerId.toString(),
        ownerName = ownerName,
        ownerEmail = ownerEmail
    )

    /**
     * Convert UserProfileDto to domain UserProfile.
     * The profile endpoint returns user fields directly, not wrapped in a "user" object.
     */
    fun UserProfileDto.toDomain(): UserProfile {
        val user = User(
            id = id.toString(),
            email = email,
            mobile = mobile,
            firstName = firstName,
            lastName = lastName,
            role = role.toUserRole(),
            ownerId = ownerId?.toString(),
            createdAt = createdAt,
            updatedAt = updatedAt ?: createdAt
        )

        return UserProfile(
            user = user,
            organizationStats = ownerStats?.toDomain(),
            ownerInfo = ownerInfo?.toDomain()
        )
    }

    private fun String.toUserRole(): UserRole = when (this.lowercase()) {
        "owner" -> UserRole.OWNER
        "manager" -> UserRole.MANAGER
        "supervisor" -> UserRole.SUPERVISOR
        else -> UserRole.OWNER
    }
}

