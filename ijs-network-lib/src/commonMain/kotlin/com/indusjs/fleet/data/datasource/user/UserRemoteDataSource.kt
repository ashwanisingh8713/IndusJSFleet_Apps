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
import com.indusjs.fleet.data.model.user.SignUpApiResponse
import com.indusjs.fleet.data.model.user.SignUpRequest
import com.indusjs.fleet.data.model.user.SimpleApiResponse
import com.indusjs.fleet.data.model.user.SendLoginOtpRequest
import com.indusjs.fleet.data.model.user.UpdateProfileRequest
import com.indusjs.fleet.data.model.user.UserApiResponse
import com.indusjs.fleet.data.model.user.UserDto
import com.indusjs.fleet.data.model.user.VerifyEmailOtpRequest
import com.indusjs.fleet.data.model.user.VerifyLoginOtpRequest
import com.indusjs.fleet.data.model.user.VerifyMobileRequest
import com.indusjs.fleet.data.model.user.VerifyMobileResponseDto
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
import com.indusjs.fleet.core.debug.postDebugLog9fbb5d
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.network.TAG_USER_REMOTE_DS
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.decodeFromString

/**
 * Remote data source for user-related API calls.
 */
interface UserRemoteDataSource : RemoteDataSource {
    suspend fun signUp(request: SignUpRequest): SignUpApiResponse
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponseDto>
    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResponse<Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): ApiResponse<Unit>
    suspend fun getProfile(token: String): ApiResponse<UserProfileDto>
    suspend fun updateProfile(token: String, request: UpdateProfileRequest): ApiResponse<UserDto>
    suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResponse<Unit>
    suspend fun verifyEmailOtp(request: VerifyEmailOtpRequest): ApiResponse<Unit>
    suspend fun verifyMobile(request: VerifyMobileRequest): ApiResponse<VerifyMobileResponseDto>
    suspend fun sendLoginOtp(request: SendLoginOtpRequest): ApiResponse<Unit>
    suspend fun verifyLoginOtp(request: VerifyLoginOtpRequest): ApiResponse<AuthResponseDto>
}

/**
 * Implementation of UserRemoteDataSource using Ktor.
 */
@Inject
class UserRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : UserRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun signUp(request: SignUpRequest): SignUpApiResponse {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Signing up user: ${request.email}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.SIGNUP}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleSignUpResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Sign up failed: ${e.message}", e)
            SignUpApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun login(request: LoginRequest): ApiResponse<AuthResponseDto> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Logging in user: ${request.identifier}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.LOGIN}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val result = handleAuthResponse(response)
            // #region agent log
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"login","location":"UserRemoteDS:login","message":"login_result","data":{"success":${result.success},"message":"${result.message?.take(100)?.replace("\"","'")}","has_data":${result.data != null}},"timestamp":0}""")
            // #endregion
            result
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Login failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResponse<Unit> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Forgot password for: ${request.identifier}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.FORGOT_PASSWORD}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUnitResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Forgot password failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): ApiResponse<Unit> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Reset password for: ${request.identifier}")
            // #region agent log — Hypothesis B: reset_token field missing from request
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"B","location":"UserRemoteDS:resetPassword","message":"reset_password_request_fields","data":{"has_identifier":${request.identifier.isNotBlank()},"has_new_password":${request.newPassword.isNotBlank()}},"timestamp":0}""")
            // #endregion
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.RESET_PASSWORD}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            // #region agent log — Hypothesis B: capture reset_password response status
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"B","location":"UserRemoteDS:resetPassword","message":"reset_password_response_status","data":{"status":${response.status.value}},"timestamp":0}""")
            // #endregion
            handleUnitResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Reset password failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getProfile(token: String): ApiResponse<UserProfileDto> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Getting user profile")
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.PROFILE}") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleProfileResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Get profile failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateProfile(token: String, request: UpdateProfileRequest): ApiResponse<UserDto> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Updating user profile")
            val response: HttpResponse = httpClient.put("$baseUrl${ApiConfig.Endpoints.PROFILE}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUserResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Update profile failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResponse<Unit> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Changing password")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.CHANGE_PASSWORD}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            handleUnitResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Change password failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun verifyEmailOtp(request: VerifyEmailOtpRequest): ApiResponse<Unit> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Verifying email OTP for: ${request.email}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.VERIFY_EMAIL_OTP}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            // #region agent log
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"verify_email","location":"UserRemoteDS:verifyEmailOtp","message":"verify_email_response","data":{"status":${response.status.value}},"timestamp":0}""")
            // #endregion
            handleUnitResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Verify email OTP failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun verifyMobile(request: VerifyMobileRequest): ApiResponse<VerifyMobileResponseDto> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Verifying mobile")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.VERIFY_MOBILE}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val raw = response.bodyAsText()
            // #region agent log
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"verify_mobile","location":"UserRemoteDS:verifyMobile","message":"verify_mobile_response","data":{"status":${response.status.value},"body_preview":"${raw.take(200).replace("\"","'")}"},"timestamp":0}""")
            // #endregion
            if (!response.status.isSuccess()) {
                return ApiResponse(success = false, message = parseErrorMessage(response.status, raw))
            }
            val jsonEl = json.parseToJsonElement(raw) as? JsonObject
                ?: return ApiResponse(success = false, message = "Invalid response")
            val success = try { jsonEl["success"]?.jsonPrimitive?.boolean ?: false } catch (_: Exception) { false }
            if (!success) {
                val msg = jsonEl["message"]?.jsonPrimitive?.contentOrNull
                return ApiResponse(success = false, message = msg ?: "Mobile verification failed")
            }
            val dataObj = jsonEl["data"] as? JsonObject
            val dto = if (dataObj != null) {
                try { json.decodeFromJsonElement<VerifyMobileResponseDto>(dataObj) } catch (_: Exception) { null }
            } else null
            ApiResponse(success = true, message = jsonEl["message"]?.jsonPrimitive?.contentOrNull, data = dto)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Verify mobile failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun sendLoginOtp(request: SendLoginOtpRequest): ApiResponse<Unit> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Sending login OTP to: ${request.mobile}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.LOGIN_OTP_SEND}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            // #region agent log
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"send_otp","location":"UserRemoteDS:sendLoginOtp","message":"send_otp_response","data":{"status":${response.status.value}},"timestamp":0}""")
            // #endregion
            handleUnitResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Send login OTP failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun verifyLoginOtp(request: VerifyLoginOtpRequest): ApiResponse<AuthResponseDto> {
        return try {
            logger.d(TAG_USER_REMOTE_DS, "Verifying login OTP for: ${request.mobile}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.LOGIN_OTP_VERIFY}") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val raw = response.bodyAsText()
            // #region agent log
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"verify_login_otp","location":"UserRemoteDS:verifyLoginOtp","message":"verify_login_otp_response","data":{"status":${response.status.value},"body_preview":"${raw.take(200).replace("\"","'")}"},"timestamp":0}""")
            // #endregion
            if (!response.status.isSuccess()) {
                return ApiResponse(success = false, message = parseErrorMessage(response.status, raw))
            }
            // VerifyLoginOTP returns { data: { access_token, refresh_token, token_type, expires_in } }
            // Convert to AuthResponseDto shape for consistent handling
            val jsonEl = json.parseToJsonElement(raw) as? JsonObject
                ?: return ApiResponse(success = false, message = "Invalid response")
            val success = try { jsonEl["success"]?.jsonPrimitive?.boolean ?: false } catch (_: Exception) { false }
            if (!success) {
                return ApiResponse(success = false, message = jsonEl["message"]?.jsonPrimitive?.contentOrNull ?: "OTP verification failed")
            }
            val dataObj = jsonEl["data"] as? JsonObject
            val accessToken = dataObj?.get("access_token")?.jsonPrimitive?.contentOrNull ?: ""
            // Build AuthResponseDto with a minimal user and the token
            val authDto = AuthResponseDto(
                user = UserDto(email = "", mobile = request.mobile, role = "owner"),
                token = accessToken
            )
            ApiResponse(success = true, message = jsonEl["message"]?.jsonPrimitive?.contentOrNull, data = authDto)
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Verify login OTP failed: ${e.message}", e)
            ApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    /**
     * Handles SignUp response — v1 backend NEVER returns a token (IAM requires verification).
     * Success cases: 201 (new user) or 200 (IsResend: existing unverified user).
     * Both return { success: true, message: "...", data: { user: {...}, token?: "" } }
     */
    private suspend fun handleSignUpResponse(response: HttpResponse): SignUpApiResponse {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Failed to read signup response body", e)
            return SignUpApiResponse(success = false, message = "Failed to read response body")
        }

        // #region agent log
        postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"signup","location":"UserRemoteDS:handleSignUpResponse","message":"signup_raw_response","data":{"status":${response.status.value},"body_preview":"${raw.take(200).replace("\"","'")}"},"timestamp":0}""")
        // #endregion

        if (!response.status.isSuccess()) {
            val errorMsg = parseErrorMessage(response.status, raw)
            logger.e(TAG_USER_REMOTE_DS, "SignUp failed with status ${response.status.value}: $errorMsg")
            return SignUpApiResponse(success = false, message = errorMsg)
        }

        return try {
            val jsonEl = json.parseToJsonElement(raw) as? JsonObject
                ?: return SignUpApiResponse(success = false, message = "Invalid response format")

            val success = try { jsonEl["success"]?.jsonPrimitive?.boolean ?: false } catch (_: Exception) { false }
            val message = jsonEl["message"]?.jsonPrimitive?.contentOrNull

            if (!success) {
                return SignUpApiResponse(success = false, message = message ?: "Sign up failed")
            }

            // Extract user from data.user
            val dataObj = jsonEl["data"] as? JsonObject
            val userEl = dataObj?.get("user")
            val userDto = if (userEl != null) {
                try { json.decodeFromJsonElement<UserDto>(userEl) } catch (_: Exception) { null }
            } else null

            // Check if IsResend case (HTTP 200 vs 201, or token absent)
            val isResend = response.status.value == 200

            logger.d(TAG_USER_REMOTE_DS, "SignUp response: success=true, isResend=$isResend, message=$message")
            SignUpApiResponse(
                success = true,
                message = message,
                user = userDto,
                isResend = isResend
            )
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Failed to parse signup response: $raw", e)
            SignUpApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleAuthResponse(response: HttpResponse): ApiResponse<AuthResponseDto> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Failed to read response body", e)
            return ApiResponse(success = false, message = "Failed to read response body")
        }

        logger.d(TAG_USER_REMOTE_DS, "Auth response status: ${response.status.value}, isSuccess: ${response.status.isSuccess()}, body: $raw")

        if (!response.status.isSuccess()) {
            val errorMsg = parseErrorMessage(response.status, raw)
            logger.e(TAG_USER_REMOTE_DS, "Auth failed with status ${response.status.value}: $errorMsg")
            return ApiResponse(success = false, message = errorMsg)
        }

        return try {
            val apiResp = json.decodeFromString<AuthApiResponse>(raw)
            // #region agent log — Hypothesis C: token may be null in IsResend signup case
            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"C","location":"UserRemoteDS:handleAuthResponse","message":"auth_parse_result","data":{"success":${apiResp.success},"has_data":${apiResp.data != null},"has_token":${apiResp.data?.token?.isNotBlank()}},"timestamp":0}""")
            // #endregion
            when {
                apiResp.success && apiResp.data != null && !apiResp.data.token.isNullOrBlank() -> {
                    // Normal success: new account created or successful login
                    ApiResponse(success = true, message = apiResp.message, data = apiResp.data)
                }
                apiResp.success && apiResp.data != null && apiResp.data.token.isNullOrBlank() -> {
                    // IsResend case: account exists but unverified — server sent verification again
                    // Inform the user with the server message; they must verify before logging in
                    logger.d(TAG_USER_REMOTE_DS, "IsResend case: success=true but token absent. Server: ${apiResp.message}")
                    ApiResponse(success = false, message = apiResp.message ?: "Account already exists. Please check your email or mobile to verify your account.")
                }
                else -> {
                    ApiResponse(success = false, message = apiResp.message ?: "Authentication failed")
                }
            }
        } catch (e: Exception) {
            logger.w(TAG_USER_REMOTE_DS, "Failed to parse as ApiResponse<AuthResponseDto>, trying fallback", e)

            try {
                val jsonEl = json.parseToJsonElement(raw)
                val tokenFound = findInJson(jsonEl, "token")
                val userEl = findElementInJson(jsonEl, "user")

                // #region agent log — Hypothesis C: check if token is missing in fallback path
                postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"C","location":"UserRemoteDS:handleAuthResponse:fallback","message":"fallback_token_check","data":{"token_found":${tokenFound != null},"user_found":${userEl != null},"raw_preview":"${raw.take(120).replace("\"","'")}"},"timestamp":0}""")
                // #endregion

                if (tokenFound != null && userEl != null) {
                    val userDto = json.decodeFromJsonElement<UserDto>(userEl)
                    val auth = AuthResponseDto(user = userDto, token = tokenFound)
                    return ApiResponse(success = true, message = null, data = auth)
                }

                val successField = findInJson(jsonEl, "success")
                val messageField = findInJson(jsonEl, "message")

                if (successField == "false") {
                    return ApiResponse(success = false, message = messageField ?: "Authentication failed")
                }

                // success=true but no token — IsResend fallback
                if (successField == "true" && userEl != null) {
                    return ApiResponse(success = false, message = messageField ?: "Account already exists. Please verify your account.")
                }

                logger.e(TAG_USER_REMOTE_DS, "Could not parse auth response: $raw")
                ApiResponse(success = false, message = "Unable to process authentication response. Please try again.")
            } catch (e2: Exception) {
                logger.e(TAG_USER_REMOTE_DS, "Fallback parsing also failed: $raw", e2)
                ApiResponse(success = false, message = "Failed to parse response: ${e2.message}")
            }
        }
    }

    private suspend fun handleProfileResponse(response: HttpResponse): ApiResponse<UserProfileDto> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Failed to read response body", e)
            return ApiResponse(success = false, message = "Failed to read response body")
        }

        logger.d(TAG_USER_REMOTE_DS, "Profile response status: ${response.status}, body: $raw")

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
            logger.e(TAG_USER_REMOTE_DS, "Failed to parse profile response: $raw", e)
            ApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleUserResponse(response: HttpResponse): ApiResponse<UserDto> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            logger.e(TAG_USER_REMOTE_DS, "Failed to read response body", e)
            return ApiResponse(success = false, message = "Failed to read response body")
        }

        logger.d(TAG_USER_REMOTE_DS, "User response status: ${response.status}, body: $raw")

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
            logger.e(TAG_USER_REMOTE_DS, "Failed to parse user response: $raw", e)
            ApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleUnitResponse(response: HttpResponse): ApiResponse<Unit> {
        val raw = try {
            response.bodyAsText()
        } catch (e: Exception) {
            return ApiResponse(success = true, message = null)
        }

        logger.d(TAG_USER_REMOTE_DS, "Unit response status: ${response.status}, body: $raw")

        if (!response.status.isSuccess()) {
            return ApiResponse(success = false, message = parseErrorMessage(response.status, raw))
        }

        if (raw.isBlank()) return ApiResponse(success = true, message = null)

        return try {
            val apiResp = json.decodeFromString<SimpleApiResponse>(raw)
            ApiResponse(success = apiResp.success, message = apiResp.message)
        } catch (e: Exception) {
            logger.d(TAG_USER_REMOTE_DS, "Non-wrapped unit response body: $raw")
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

