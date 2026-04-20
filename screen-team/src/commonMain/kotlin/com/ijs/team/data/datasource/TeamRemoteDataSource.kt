package com.ijs.team.data.datasource

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.ijs.team.data.model.AssignableRolesApiResponse
import com.ijs.team.data.model.CreateTeamMemberRequest
import com.ijs.team.data.model.ResetPasswordRequest
import com.ijs.team.data.model.TeamMemberApiResponse
import com.ijs.team.data.model.TeamMemberListApiResponse
import com.ijs.team.data.model.TeamSimpleApiResponse
import com.ijs.team.data.model.UpdateTeamMemberRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.team.TAG_TEAM_REMOTE_DS
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json

/**
 * Remote data source for team management API calls.
 */
interface TeamRemoteDataSource {
    suspend fun getAssignableTeamRoles(token: String): AssignableRolesApiResponse
    suspend fun createTeamMember(token: String, request: CreateTeamMemberRequest): TeamMemberApiResponse
    suspend fun getTeamMembers(token: String, role: String? = null): TeamMemberListApiResponse
    suspend fun getTeamMember(token: String, id: String): TeamMemberApiResponse
    suspend fun updateTeamMember(token: String, id: String, request: UpdateTeamMemberRequest): TeamMemberApiResponse
    suspend fun toggleTeamMemberActive(token: String, id: String): TeamMemberApiResponse
    suspend fun resetTeamMemberPassword(token: String, id: String, request: ResetPasswordRequest): TeamSimpleApiResponse
    suspend fun deleteTeamMember(token: String, id: String): TeamSimpleApiResponse
}

/**
 * Implementation of TeamRemoteDataSource using Ktor.
 */
@Inject
class TeamRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : TeamRemoteDataSource {
    private val baseUrl = ApiConfig.BASE_URL

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    companion object {
        private const val TEAM_MEMBERS_ENDPOINT = "/team/members"
        private const val TEAM_MEMBER_ROLES_ENDPOINT = "/team/members/roles"
    }

    override suspend fun getAssignableTeamRoles(token: String): AssignableRolesApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Fetching assignable team member IAM roles")
            val response: HttpResponse = httpClient.get("$baseUrl$TEAM_MEMBER_ROLES_ENDPOINT") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleAssignableRolesResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Get assignable roles failed: ${e.message}", e)
            AssignableRolesApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun createTeamMember(token: String, request: CreateTeamMemberRequest): TeamMemberApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Creating team member: ${request.email} as ${request.role}")
            val response: HttpResponse = httpClient.post("$baseUrl$TEAM_MEMBERS_ENDPOINT") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleTeamMemberResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Create team member failed: ${e.message}", e)
            TeamMemberApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getTeamMembers(token: String, role: String?): TeamMemberListApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Getting team members, role filter: $role")
            val response: HttpResponse = httpClient.get("$baseUrl$TEAM_MEMBERS_ENDPOINT") {
                header(HttpHeaders.Authorization, "Bearer $token")
                if (role != null) {
                    parameter("role", role)
                }
            }
            handleTeamMemberListResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Get team members failed: ${e.message}", e)
            TeamMemberListApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getTeamMember(token: String, id: String): TeamMemberApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Getting team member: $id")
            val response: HttpResponse = httpClient.get("$baseUrl$TEAM_MEMBERS_ENDPOINT/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleTeamMemberResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Get team member failed: ${e.message}", e)
            TeamMemberApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateTeamMember(token: String, id: String, request: UpdateTeamMemberRequest): TeamMemberApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Updating team member: $id with role: ${request.role}")
            logger.d(TAG_TEAM_REMOTE_DS, "Update request: firstName=${request.firstName}, lastName=${request.lastName}, email=${request.email}, role=${request.role}, isActive=${request.isActive}")
            val response: HttpResponse = httpClient.put("$baseUrl$TEAM_MEMBERS_ENDPOINT/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleTeamMemberResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Update team member failed: ${e.message}", e)
            TeamMemberApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun toggleTeamMemberActive(token: String, id: String): TeamMemberApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Toggling team member active status: $id")
            val response: HttpResponse = httpClient.patch("$baseUrl$TEAM_MEMBERS_ENDPOINT/$id/toggle-active") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleTeamMemberResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Toggle team member active failed: ${e.message}", e)
            TeamMemberApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun resetTeamMemberPassword(token: String, id: String, request: ResetPasswordRequest): TeamSimpleApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Resetting team member password: $id")
            val response: HttpResponse = httpClient.post("$baseUrl$TEAM_MEMBERS_ENDPOINT/$id/reset-password") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleSimpleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Reset team member password failed: ${e.message}", e)
            TeamSimpleApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun deleteTeamMember(token: String, id: String): TeamSimpleApiResponse {
        return try {
            logger.d(TAG_TEAM_REMOTE_DS, "Deleting team member: $id")
            val response: HttpResponse = httpClient.delete("$baseUrl$TEAM_MEMBERS_ENDPOINT/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleSimpleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Delete team member failed: ${e.message}", e)
            TeamSimpleApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    private suspend fun handleAssignableRolesResponse(response: HttpResponse): AssignableRolesApiResponse {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Failed to read assignable roles body", e)
            return AssignableRolesApiResponse(success = false, message = "Failed to read response body")
        }
        logger.d(TAG_TEAM_REMOTE_DS, "Assignable roles status: ${response.status}, body: $raw")
        if (!response.status.isSuccess()) {
            return AssignableRolesApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }
        return try {
            json.decodeFromString<AssignableRolesApiResponse>(raw)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Failed to parse assignable roles: $raw", e)
            AssignableRolesApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleTeamMemberResponse(response: HttpResponse): TeamMemberApiResponse {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Failed to read response body", e)
            return TeamMemberApiResponse(success = false, message = "Failed to read response body")
        }
        logger.d(TAG_TEAM_REMOTE_DS, "Team member response status: ${response.status}, body: $raw")

        if (!response.status.isSuccess()) {
            return TeamMemberApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        return try {
            json.decodeFromString<TeamMemberApiResponse>(raw)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Failed to parse team member response: $raw", e)
            TeamMemberApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleTeamMemberListResponse(response: HttpResponse): TeamMemberListApiResponse {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Failed to read response body", e)
            return TeamMemberListApiResponse(success = false, message = "Failed to read response body")
        }
        logger.d(TAG_TEAM_REMOTE_DS, "Team member list response status: ${response.status}, body: $raw")

        if (!response.status.isSuccess()) {
            return TeamMemberListApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        return try {
            json.decodeFromString<TeamMemberListApiResponse>(raw)
        } catch (e: Exception) {
            logger.e(TAG_TEAM_REMOTE_DS, "Failed to parse team member list response: $raw", e)
            TeamMemberListApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleSimpleResponse(response: HttpResponse): TeamSimpleApiResponse {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            return TeamSimpleApiResponse(success = true, message = null)
        }
        logger.d(TAG_TEAM_REMOTE_DS, "Simple response status: ${response.status}, body: $raw")

        if (!response.status.isSuccess()) {
            return TeamSimpleApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        return try {
            json.decodeFromString<TeamSimpleApiResponse>(raw)
        } catch (e: Exception) {
            TeamSimpleApiResponse(success = true, message = null)
        }
    }

    private fun parseErrorMessage(status: HttpStatusCode, raw: String): String {
        return ApiErrorHandler.extractErrorMessage(status, raw)
    }
}
