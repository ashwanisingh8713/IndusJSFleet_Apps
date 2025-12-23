package com.indusjs.fleet.data.mapper.team

import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import com.indusjs.fleet.data.model.team.TeamMemberDto

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

