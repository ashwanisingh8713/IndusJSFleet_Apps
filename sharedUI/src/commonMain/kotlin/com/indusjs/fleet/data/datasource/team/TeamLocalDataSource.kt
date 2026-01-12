package com.indusjs.fleet.data.datasource.team

import com.indusjs.fleet.data.database.dao.TeamMembersDao
import com.indusjs.fleet.data.database.entity.TeamMemberEntity
import com.indusjs.fleet.data.datasource.LocalDataSource
import dev.zacsweers.metro.Inject

/**
 * Local data source interface for team members storage.
 */
interface TeamLocalDataSource : LocalDataSource {
    suspend fun getTeamMembers(): List<TeamMemberEntity>
    suspend fun getCaretakers(): List<TeamMemberEntity>
    suspend fun saveTeamMembers(members: List<TeamMemberEntity>)
    suspend fun saveTeamMember(member: TeamMemberEntity)
    suspend fun deleteTeamMember(id: Int)
    suspend fun getTeamMemberById(id: Int): TeamMemberEntity?
    suspend fun hasTeamMembersCached(): Boolean
    suspend fun clearCache()
}

/**
 * Implementation of TeamLocalDataSource using TeamMembersDao.
 */
@Inject
class TeamLocalDataSourceImpl(
    private val teamMembersDao: TeamMembersDao
) : TeamLocalDataSource {

    override suspend fun getTeamMembers(): List<TeamMemberEntity> {
        return teamMembersDao.getTeamMembers()
    }

    override suspend fun getCaretakers(): List<TeamMemberEntity> {
        return teamMembersDao.getCaretakers()
    }

    override suspend fun saveTeamMembers(members: List<TeamMemberEntity>) {
        teamMembersDao.saveTeamMembers(members)
    }

    override suspend fun saveTeamMember(member: TeamMemberEntity) {
        teamMembersDao.saveTeamMember(member)
    }

    override suspend fun deleteTeamMember(id: Int) {
        teamMembersDao.deleteTeamMember(id)
    }

    override suspend fun getTeamMemberById(id: Int): TeamMemberEntity? {
        return teamMembersDao.getTeamMemberById(id)
    }

    override suspend fun hasTeamMembersCached(): Boolean {
        return teamMembersDao.hasTeamMembersCached()
    }

    override suspend fun clearCache() {
        teamMembersDao.clearCache()
    }
}
