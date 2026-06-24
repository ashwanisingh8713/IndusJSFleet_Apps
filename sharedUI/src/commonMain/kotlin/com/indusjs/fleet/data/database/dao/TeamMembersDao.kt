package com.indusjs.fleet.data.database.dao

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.TAG_TEAM_MEMBERS_DAO
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.fleet.data.database.entity.TeamMemberEntity
import com.indusjs.fleet.data.database.entity.TeamMembersListEntity
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Data Access Object for Team Members cache operations.
 */
interface TeamMembersDao {
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
 * Settings-based implementation of TeamMembersDao.
 * Uses multiplatform-settings for cross-platform persistence.
 */
class SettingsTeamMembersDao(
    private val settings: Settings,
    private val json: Json,
    private val logger: FleetLogger
) : TeamMembersDao {

    companion object {
        private const val KEY_TEAM_MEMBERS = "team_members_cache"
    }

    override suspend fun getTeamMembers(): List<TeamMemberEntity> {
        return settings.getStringOrNull(KEY_TEAM_MEMBERS)?.let { cached ->
            try {
                json.decodeFromString<TeamMembersListEntity>(cached).members
            } catch (e: Exception) {
                logger.e(TAG_TEAM_MEMBERS_DAO, "Failed to decode team members cache", e)
                emptyList()
            }
        } ?: emptyList()
    }

    override suspend fun getCaretakers(): List<TeamMemberEntity> {
        return getTeamMembers().filter { member ->
            member.isCaretakerEligible
        }
    }

    override suspend fun saveTeamMembers(members: List<TeamMemberEntity>) {
        try {
            val entity = TeamMembersListEntity(
                members = members,
                lastUpdated = currentTimeMillis()
            )
            settings.putString(KEY_TEAM_MEMBERS, json.encodeToString(entity))
            logger.d(TAG_TEAM_MEMBERS_DAO, "Saved ${members.size} team members to cache")
        } catch (e: Exception) {
            logger.e(TAG_TEAM_MEMBERS_DAO, "Failed to save team members: ${e.message}", e)
        }
    }

    override suspend fun saveTeamMember(member: TeamMemberEntity) {
        val current = getTeamMembers().toMutableList()
        val existingIndex = current.indexOfFirst { it.id == member.id }
        if (existingIndex >= 0) {
            current[existingIndex] = member
            logger.d(TAG_TEAM_MEMBERS_DAO, "Updated team member: ${member.fullName}")
        } else {
            current.add(member)
            logger.d(TAG_TEAM_MEMBERS_DAO, "Added new team member: ${member.fullName}")
        }
        saveTeamMembers(current)
    }

    override suspend fun deleteTeamMember(id: Int) {
        val current = getTeamMembers().toMutableList()
        val removed = current.removeAll { it.id == id }
        if (removed) {
            saveTeamMembers(current)
            logger.d(TAG_TEAM_MEMBERS_DAO, "Deleted team member with id: $id")
        }
    }

    override suspend fun getTeamMemberById(id: Int): TeamMemberEntity? {
        return getTeamMembers().find { it.id == id }
    }

    override suspend fun hasTeamMembersCached(): Boolean {
        return settings.getStringOrNull(KEY_TEAM_MEMBERS) != null
    }

    override suspend fun clearCache() {
        settings.remove(KEY_TEAM_MEMBERS)
        logger.d(TAG_TEAM_MEMBERS_DAO, "Cleared team members cache")
    }
}
