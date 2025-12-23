package com.indusjs.fleet.feature.team.data.mapper

import com.indusjs.fleet.feature.team.data.model.TeamMemberDto
import com.indusjs.fleet.feature.team.domain.entity.TeamMember
import com.indusjs.fleet.feature.team.domain.entity.TeamMemberRole

/**
 * Mapper for converting between Team DTOs and domain entities.
 */
object TeamMapper {

    fun TeamMemberDto.toDomain(): TeamMember = TeamMember(
        id = id.toString(),
        email = email,
        mobile = mobile,
        firstName = firstName,
        lastName = lastName,
        role = role.toTeamMemberRole(),
        ownerId = ownerId.toString(),
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt ?: createdAt
    )

    fun List<TeamMemberDto>.toDomain(): List<TeamMember> = map { it.toDomain() }

    private fun String.toTeamMemberRole(): TeamMemberRole = when (this.lowercase()) {
        "manager" -> TeamMemberRole.MANAGER
        "supervisor" -> TeamMemberRole.SUPERVISOR
        else -> TeamMemberRole.SUPERVISOR
    }
}

