package com.indusjs.fleet.data.datasource.user

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.user.ApiResponse
import com.indusjs.fleet.data.model.user.AuthApiResponse
import com.indusjs.fleet.data.model.user.AuthResponseDto
import com.indusjs.fleet.data.model.user.ChangePasswordRequest
import com.indusjs.fleet.data.model.user.ForgotPasswordRequest
import com.indusjs.fleet.data.model.user.LoginRequest
import com.indusjs.fleet.data.model.user.ProfileApiResponse
import com.indusjs.fleet.data.model.user.ResetPasswordRequest
import com.indusjs.fleet.data.model.user.SignUpRequest
import com.indusjs.fleet.data.model.user.SimpleApiResponse
import com.indusjs.fleet.data.model.user.UpdateProfileRequest
import com.indusjs.fleet.data.model.user.UserApiResponse
import com.indusjs.fleet.data.model.user.UserDto
import com.indusjs.fleet.data.model.user.UserProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
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
import dev.zacsweers.metro.Inject
import co.touchlab.kermit.Logger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.decodeFromString

/**
 * Remote data source for user-related API calls.
 */
interface UserRemoteDataSource : RemoteDataSource {
    suspend fun signUp(request: SignUpRequest): ApiResponse<AuthResponseDto>
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponseDto>
    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResponse<Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): ApiResponse<Unit>
    suspend fun getProfile(token: String): ApiResponse<UserProfileDto>
    suspend fun updateProfile(token: String, request: UpdateProfileRequest): ApiResponse<UserDto>
    suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResponse<Unit>
}

/**
 * Implementation of UserRemoteDataSource using Ktor.
 */
@Inject
class UserRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : UserRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL
    private val log = Logger.withTag("UserRemoteDataSource")

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun signUp(request: SignUpRequest): ApiResponse<AuthResponseDto> {
        return try {
            log.d { "Signing up user: ${request.email}" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.SIGNUP}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleAuthResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Sign up failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun login(request: LoginRequest): ApiResponse<AuthResponseDto> {
        return try {
            log.d { "Logging in user: ${request.identifier}" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.LOGIN}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleAuthResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Login failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResponse<Unit> {
        return try {
            log.d { "Forgot password for: ${request.identifier}" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.FORGOT_PASSWORD}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUnitResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Forgot password failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): ApiResponse<Unit> {
        return try {
            log.d { "Reset password for: ${request.identifier}" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.RESET_PASSWORD}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUnitResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Reset password failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getProfile(token: String): ApiResponse<UserProfileDto> {
        return try {
            log.d { "Getting user profile" }
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.PROFILE}") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleProfileResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Get profile failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateProfile(token: String, request: UpdateProfileRequest): ApiResponse<UserDto> {
        return try {
            log.d { "Updating user profile" }
            val response: HttpResponse = httpClient.put("$baseUrl${ApiConfig.Endpoints.PROFILE}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUserResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Update profile failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResponse<Unit> {
        return try {
            log.d { "Changing password" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.CHANGE_PASSWORD}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUnitResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Change password failed: ${e.message}" }
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    private suspend fun handleAuthResponse(response: HttpResponse): ApiResponse<AuthResponseDto> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            log.e(e) { "Failed to read response body" }
            return ApiResponse(success = false, message = "Failed to read response body")
        }

        log.d { "Auth response status: ${response.status.value}, isSuccess: ${response.status.isSuccess()}, body: $raw" }

        if (!response.status.isSuccess()) {
            val errorMsg = parseErrorMessage(response.status, raw)
            log.e { "Auth failed with status ${response.status.value}: $errorMsg" }
            return ApiResponse(success = false, message = errorMsg)
        }

        return try {
            val apiResp = json.decodeFromString<AuthApiResponse>(raw)
            if (apiResp.success && apiResp.data != null) {
                ApiResponse(success = true, message = apiResp.message, data = apiResp.data)
            } else {
                ApiResponse(success = false, message = apiResp.message ?: "Login failed")
            }
        } catch (e: Exception) {
            log.w(e) { "Failed to parse as ApiResponse<AuthResponseDto>, trying fallback" }

            try {
                val jsonEl = json.parseToJsonElement(raw)
                val tokenFound = findInJson(jsonEl, "token")
                val userEl = findElementInJson(jsonEl, "user")

                if (tokenFound != null && userEl != null) {
                    val userDto = json.decodeFromJsonElement<UserDto>(userEl)
                    val auth = AuthResponseDto(user = userDto, token = tokenFound)
                    return ApiResponse(success = true, message = null, data = auth)
                }

                val successField = findInJson(jsonEl, "success")
                val messageField = findInJson(jsonEl, "message")

                if (successField == "false") {
                    return ApiResponse(success = false, message = messageField ?: "Login failed")
                }

                log.e { "Could not find token/user in response: $raw" }
                ApiResponse(success = false, message = "Failed to parse login response")
            } catch (e2: Exception) {
                log.e(e2) { "Fallback parsing also failed: $raw" }
                ApiResponse(success = false, message = "Failed to parse response: ${e2.message}")
            }
        }
    }

    private suspend fun handleProfileResponse(response: HttpResponse): ApiResponse<UserProfileDto> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            log.e(e) { "Failed to read response body" }
            return ApiResponse(success = false, message = "Failed to read response body")
        }

        log.d { "Profile response status: ${response.status}, body: $raw" }

        if (!response.status.isSuccess()) {
            return ApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        return try {
            val apiResp = json.decodeFromString<ProfileApiResponse>(raw)
            if (apiResp.success && apiResp.data != null) {
                ApiResponse(success = true, message = apiResp.message, data = apiResp.data)
            } else {
                ApiResponse(success = false, message = apiResp.message ?: "Failed to get profile")
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse profile response: $raw" }
            ApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleUserResponse(response: HttpResponse): ApiResponse<UserDto> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            log.e(e) { "Failed to read response body" }
            return ApiResponse(success = false, message = "Failed to read response body")
        }

        log.d { "User response status: ${response.status}, body: $raw" }

        if (!response.status.isSuccess()) {
            return ApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        return try {
            val apiResp = json.decodeFromString<UserApiResponse>(raw)
            if (apiResp.success && apiResp.data != null) {
                ApiResponse(success = true, message = apiResp.message, data = apiResp.data)
            } else {
                ApiResponse(success = false, message = apiResp.message ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse user response: $raw" }
            ApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleUnitResponse(response: HttpResponse): ApiResponse<Unit> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            return ApiResponse(success = true, message = null)
        }

        log.d { "Unit response status: ${response.status}, body: $raw" }

        if (!response.status.isSuccess()) {
            return ApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        if (raw.isBlank()) return ApiResponse(success = true, message = null)

        return try {
            val apiResp = json.decodeFromString<SimpleApiResponse>(raw)
            ApiResponse(success = apiResp.success, message = apiResp.message)
        } catch (e: Exception) {
            log.d { "Non-wrapped unit response body: $raw" }
            ApiResponse(success = true, message = null)
        }
    }

    private fun parseErrorMessage(status: HttpStatusCode, raw: String): String {
        return ApiErrorHandler.extractErrorMessage(status, raw)
    }

    private fun findInJson(el: JsonElement, key: String): String? {
        if (el is JsonObject) {
            el[key]?.let { v ->
                v.jsonPrimitive.contentOrNull?.let { return it }
            }
            for ((_, child) in el) {
                val found = findInJson(child, key)
                if (found != null) return found
            }
        }
        return null
    }

    private fun findElementInJson(el: JsonElement, key: String): JsonElement? {
        if (el is JsonObject) {
            el[key]?.let { return it }
            for ((_, child) in el) {
                val found = findElementInJson(child, key)
                if (found != null) return found
            }
        }
        return null
    }
}

