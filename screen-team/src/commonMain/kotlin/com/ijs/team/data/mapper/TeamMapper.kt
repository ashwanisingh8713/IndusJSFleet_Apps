package com.ijs.team.data.mapper

import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import com.ijs.team.data.model.TeamMemberDto

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
        "general_manager", "generalmanager", "gm" -> TeamMemberRole.GENERAL_MANAGER
        "manager" -> TeamMemberRole.MANAGER
        "supervisor" -> TeamMemberRole.SUPERVISOR
        else -> TeamMemberRole.SUPERVISOR
    }
}

