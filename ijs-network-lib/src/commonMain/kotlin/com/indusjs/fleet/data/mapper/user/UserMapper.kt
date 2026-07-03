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
import com.indusjs.fleet.core.util.formatDateTimeForDisplay

/**
 * Mapper for converting between User DTOs and domain entities.
 *
 * Wire timestamps are UTC epoch-millis (Long). The [User] domain entity keeps
 * createdAt/updatedAt as display Strings, so we format here.
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
        isActive = isActive,
        createdAt = formatDateTimeForDisplay(createdAt),
        updatedAt = formatDateTimeForDisplay(updatedAt ?: createdAt),
        businessName = businessName
    )

    fun AuthResponseDto.toDomain(): AuthResult = AuthResult(
        user = user.toDomain(),
        // Backend dropped the legacy `token` key — read `access_token` (fall back to
        // the old `token` only for safety on a stale server).
        token = accessToken ?: token ?: ""
    )

    fun OwnerStatsDto.toDomain(): OrganizationStats = OrganizationStats(
        totalTeamMembers = totalTeamMembers,
        admins = admins,
        users = users,
        totalVehicles = totalVehicles,
        activeVehicles = activeVehicles,
        totalTrips = totalTrips,
        activeTrips = activeTrips,
        completedTrips = completedTrips
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
            isActive = isActive,
            createdAt = formatDateTimeForDisplay(createdAt),
            updatedAt = formatDateTimeForDisplay(updatedAt ?: createdAt),
            businessName = businessName
        )

        return UserProfile(
            user = user,
            organizationStats = ownerStats?.toDomain(),
            ownerInfo = ownerInfo?.toDomain()
        )
    }

    private fun String.toUserRole(): UserRole = when (this.lowercase().replace("_", "")) {
        "owner" -> UserRole.OWNER
        "admin" -> UserRole.ADMIN
        "user" -> UserRole.USER
        else -> UserRole.USER // Default to lowest privilege for safety
    }
}

