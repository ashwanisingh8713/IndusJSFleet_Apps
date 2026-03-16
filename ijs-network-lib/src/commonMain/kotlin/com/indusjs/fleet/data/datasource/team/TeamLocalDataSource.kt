package com.indusjs.fleet.data.datasource.team

import com.indusjs.fleet.data.datasource.LocalDataSource
import com.indusjs.fleet.data.model.team.TeamMemberDto

/**
 * Local data source interface for team members storage.
 *
 * NOTE: Interface lives in ijs-network-lib (uses DTOs, no Room dependency).
 * Implementation (TeamLocalDataSourceImpl) lives in sharedUI (uses Room DAOs).
 *
 * All methods use [TeamMemberDto] instead of Room entities to keep
 * this interface free of Room dependencies.
 */
interface TeamLocalDataSource : LocalDataSource {
    suspend fun getTeamMembers(): List<TeamMemberDto>
    suspend fun getCaretakers(): List<TeamMemberDto>
    suspend fun saveTeamMembers(members: List<TeamMemberDto>)
    suspend fun saveTeamMember(member: TeamMemberDto)
    suspend fun deleteTeamMember(id: Int)
    suspend fun getTeamMemberById(id: Int): TeamMemberDto?
    suspend fun hasTeamMembersCached(): Boolean
    suspend fun clearCache()
}

