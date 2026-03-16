package com.indusjs.fleet.data.datasource.team

import com.indusjs.fleet.data.database.dao.TeamMembersDao
import com.indusjs.fleet.data.database.entity.TeamMemberEntity
import com.indusjs.fleet.data.model.team.TeamMemberDto
import dev.zacsweers.metro.Inject

/**
 * Implementation of TeamLocalDataSource using Room DAOs.
 *
 * The interface [TeamLocalDataSource] is defined in ijs-network-lib.
 * This implementation converts between DTOs and Room entities internally.
 */
@Inject
class TeamLocalDataSourceImpl(
    private val teamMembersDao: TeamMembersDao
) : TeamLocalDataSource {

    override suspend fun getTeamMembers(): List<TeamMemberDto> {
        return teamMembersDao.getTeamMembers().map { it.toDto() }
    }

    override suspend fun getCaretakers(): List<TeamMemberDto> {
        return teamMembersDao.getCaretakers().map { it.toDto() }
    }

    override suspend fun saveTeamMembers(members: List<TeamMemberDto>) {
        teamMembersDao.saveTeamMembers(members.map { it.toEntity() })
    }

    override suspend fun saveTeamMember(member: TeamMemberDto) {
        teamMembersDao.saveTeamMember(member.toEntity())
    }

    override suspend fun deleteTeamMember(id: Int) {
        teamMembersDao.deleteTeamMember(id)
    }

    override suspend fun getTeamMemberById(id: Int): TeamMemberDto? {
        return teamMembersDao.getTeamMemberById(id)?.toDto()
    }

    override suspend fun hasTeamMembersCached(): Boolean {
        return teamMembersDao.hasTeamMembersCached()
    }

    override suspend fun clearCache() {
        teamMembersDao.clearCache()
    }

    private fun TeamMemberEntity.toDto(): TeamMemberDto = TeamMemberDto(
        id = id,
        email = email,
        mobile = mobile,
        firstName = firstName,
        lastName = lastName,
        role = role,
        ownerId = ownerId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TeamMemberDto.toEntity(): TeamMemberEntity = TeamMemberEntity(
        id = id,
        email = email,
        mobile = mobile,
        firstName = firstName,
        lastName = lastName,
        role = role,
        ownerId = ownerId,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
