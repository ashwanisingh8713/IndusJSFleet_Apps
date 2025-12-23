package com.indusjs.fleet.data.mapper.user

import com.indusjs.fleet.data.model.user.AuthResponseDto
import com.indusjs.fleet.data.model.user.OwnerInfoDto
import com.indusjs.fleet.data.model.user.OwnerStatsDto
import com.indusjs.fleet.data.model.user.UserDto
import com.indusjs.fleet.data.model.user.UserProfileDto
import com.indusjs.fleet.domain.entity.user.AuthResult
import com.indusjs.fleet.domain.entity.user.OrganizationStats
import com.indusjs.fleet.domain.entity.user.OwnerInfo
import com.indusjs.fleet.domain.entity.user.User
import com.indusjs.fleet.domain.entity.user.UserProfile
import com.indusjs.fleet.domain.entity.user.UserRole

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
        totalDrivers = totalTeamMembers,
        totalVehicles = totalVehicles,
        activeTrips = activeTrips
    )

    fun OwnerInfoDto.toDomain(): OwnerInfo = OwnerInfo(
        ownerId = ownerId.toString(),
        ownerName = ownerName,
        ownerEmail = ownerEmail
    )

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

