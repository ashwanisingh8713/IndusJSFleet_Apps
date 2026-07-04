package com.indusjs.fleet.data.repository.user

import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.auth.JwtHelper
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSource
import com.indusjs.fleet.data.mapper.user.UserMapper.toDomain
import com.indusjs.fleet.data.model.user.ChangePasswordRequest
import com.indusjs.fleet.data.model.user.ForgotPasswordRequest
import com.indusjs.fleet.data.model.user.LoginRequest
import com.indusjs.fleet.data.model.user.ResetPasswordRequest
import com.indusjs.fleet.data.model.user.SendLoginOtpRequest
import com.indusjs.fleet.data.model.user.SignUpRequest
import com.indusjs.fleet.data.model.user.UpdateProfileRequest
import com.indusjs.fleet.data.model.user.VerifyEmailOtpRequest
import com.indusjs.fleet.data.model.user.VerifyLoginOtpRequest
import com.indusjs.fleet.data.model.user.VerifyMobileRequest
import com.indusjs.fleet.domain.entity.user.AuthResult
import com.indusjs.fleet.domain.entity.user.SignUpResult
import com.indusjs.fleet.domain.entity.user.User
import com.indusjs.fleet.domain.entity.user.UserProfile
import com.indusjs.fleet.domain.entity.user.UserRole
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.permission.PermissionStore
import com.indusjs.fleet.core.permission.Permissions
import com.indusjs.fleet.network.TAG_USER_REPO
import com.indusjs.datetimeutils.FleetEpoch
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

/**
 * Implementation of UserRepository.
 * Handles user authentication and profile operations.
 */
@Inject
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : UserRepository {

    override suspend fun signUp(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<SignUpResult> = runCatching {
        val response = remoteDataSource.signUp(
            SignUpRequest(
                email = email,
                mobile = mobile,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
        )

        if (!response.success) {
            throw ApiException(response.message ?: "Sign up failed")
        }

        // v1 backend does NOT issue tokens at signup — verification is required first.
        // Do NOT save any auth token or navigate to dashboard.
        SignUpResult(
            message = response.message ?: "Account registered successfully",
            isResend = response.isResend
        )
    }

    override suspend fun login(
        identifier: String,
        password: String
    ): Result<AuthResult> = runCatching {
        val response = remoteDataSource.login(
            LoginRequest(identifier = identifier, password = password)
        )

        val authData = response.data
            ?: throw ApiException(response.message ?: "Login failed")
        val authResult = authData.toDomain()

        val roleToSave = resolveLoginRole(authData.user.role, authResult.user.role, authResult.token)
        logger.d(TAG_USER_REPO, "Login successful - User: ${authResult.user.email}, Role enum: ${authResult.user.role}, Role to save: '$roleToSave', tenantId: '${authData.user.tenantId}'")

        localDataSource.saveAuthToken(authResult.token)
        // Persist the rotated refresh pair + expiry so the app can silently refresh.
        persistRefreshSession(authData.refreshToken, authData.expiresIn)
        localDataSource.saveUserRole(roleToSave)
        localDataSource.saveUserId(authResult.user.id)
        localDataSource.saveTenantId(authData.user.tenantId)
        val fullName = "${authResult.user.firstName} ${authResult.user.lastName}".trim()
        localDataSource.saveUserName(fullName)
        // Load the user's actual permission set (UI gating). Non-fatal on failure.
        refreshPermissions()
        // f5: warm the tenant/business-name cache right after login (the login payload has no
        // business_name — it's a /profile enrichment) so the Home header shows it on first render.
        // Non-fatal: getProfile() returns a Result and never throws; it caches businessName internally.
        getProfile()
        authResult
    }

    override suspend fun forgotPassword(identifier: String): Result<Unit> = runCatching {
        val response = remoteDataSource.forgotPassword(
            ForgotPasswordRequest(identifier = identifier)
        )

        if (!response.success) {
            throw ApiException(response.message ?: "Forgot password request failed")
        }
    }

    override suspend fun resetPassword(
        resetToken: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val response = remoteDataSource.resetPassword(
            ResetPasswordRequest(
                resetToken = resetToken,
                newPassword = newPassword
            )
        )

        if (!response.success) {
            throw ApiException(response.message ?: "Password reset failed")
        }
    }

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getProfile(token)

        val profile = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to get profile")
        val fullName = "${profile.user.firstName} ${profile.user.lastName}".trim()
        localDataSource.saveUserName(fullName)
        // f5: cache the tenant/business name for the Home header (survives restarts; null clears it).
        localDataSource.saveBusinessName(profile.user.businessName)
        profile
    }

    override suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        email: String?,
        mobile: String?
    ): Result<User> = runCatching {
        val token = requireAuthToken()

        val response = remoteDataSource.updateProfile(
            token = token,
            request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                mobile = mobile
            )
        )

        val user = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to update profile")
        val fullName = "${user.firstName} ${user.lastName}".trim()
        localDataSource.saveUserName(fullName)
        user
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> = runCatching {
        val token = requireAuthToken()

        val response = remoteDataSource.changePassword(
            token = token,
            request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
        )

        if (!response.success) {
            throw ApiException(response.message ?: "Failed to change password")
        }
    }

    override suspend fun verifyEmailOtp(email: String, otp: String): Result<Unit> = runCatching {
        val response = remoteDataSource.verifyEmailOtp(VerifyEmailOtpRequest(email = email, otp = otp))
        if (!response.success) {
            throw ApiException(response.message ?: "Email verification failed")
        }
    }

    override suspend fun verifyMobile(token: String): Result<String?> = runCatching {
        val response = remoteDataSource.verifyMobile(VerifyMobileRequest(token = token))
        if (!response.success) {
            throw ApiException(response.message ?: "Mobile verification failed")
        }
        val accessToken = response.data?.accessToken ?: response.data?.token
        if (!accessToken.isNullOrBlank()) {
            localDataSource.saveAuthToken(accessToken)
            // Persist the rotated refresh pair + expiry so the app can silently refresh.
            persistRefreshSession(response.data?.refreshToken, response.data?.expiresIn?.toLong() ?: 0L)
        }
        accessToken
    }

    override suspend fun sendLoginOtp(mobile: String): Result<Unit> = runCatching {
        val response = remoteDataSource.sendLoginOtp(SendLoginOtpRequest(mobile = mobile))
        if (!response.success) {
            throw ApiException(response.message ?: "Failed to send OTP")
        }
    }

    override suspend fun verifyLoginOtp(mobile: String, otp: String): Result<AuthResult> = runCatching {
        val response = remoteDataSource.verifyLoginOtp(VerifyLoginOtpRequest(mobile = mobile, otp = otp))
        val authData = response.data
            ?: throw ApiException(response.message ?: "OTP verification failed")
        val authResult = authData.toDomain()
        localDataSource.saveAuthToken(authResult.token)
        // Persist the rotated refresh pair + expiry so the app can silently refresh.
        persistRefreshSession(authData.refreshToken, authData.expiresIn)
        localDataSource.saveUserRole(UserRole.toApiString(authResult.user.role))
        localDataSource.saveUserId(authResult.user.id)
        // Load the user's actual permission set (UI gating). Non-fatal on failure.
        refreshPermissions()
        authResult
    }

    override suspend fun getAuthToken(): String? = localDataSource.getAuthToken()

    override suspend fun saveAuthToken(token: String) = localDataSource.saveAuthToken(token)

    override suspend fun logout() {
        localDataSource.clearSession()
        PermissionStore.clear()
    }

    override suspend fun isLoggedIn(): Boolean = localDataSource.isLoggedIn()

    override suspend fun refreshPermissions() {
        val response = remoteDataSource.fetchMyPermissions()
        val fetched = response.data?.data?.permissions
        val base: Set<String> = if (response.success && fetched != null) {
            val perms = fetched.toSet()
            localDataSource.saveUserPermissions(perms)
            perms
        } else {
            // Fall back to the last cached set so the UI still gates sensibly.
            logger.w(TAG_USER_REPO, "Permissions fetch failed (${response.message}); using cached set")
            localDataSource.getUserPermissions()
        }
        // Expand to full owner access when the signed-in user is an owner. IAM grants
        // the owner role but not the full permission set, so without this a freshly
        // onboarded owner would be locked out of every fleet screen. Owner detection
        // lives only here (see Permissions.effectivePermissions) — call sites stay
        // permission-based.
        val effective = Permissions.effectivePermissions(base, currentUserRoles())
        PermissionStore.update(effective)
        logger.d(
            TAG_USER_REPO,
            "Permissions updated: ${base.size} from backend, ${effective.size} effective" +
                (if (effective.size != base.size) " (owner-expanded)" else "")
        )
    }

    /** Roles for the signed-in user, from the JWT (preferred) plus the saved role. */
    private suspend fun currentUserRoles(): Set<String> {
        val fromToken = localDataSource.getAuthToken()?.let { JwtHelper.extractRoles(it) }.orEmpty()
        val saved = localDataSource.getUserRole()
        return (fromToken + listOfNotNull(saved)).toSet()
    }

    /**
     * Persists the (rotating) refresh token and the absolute access-token expiry
     * derived from the per-response [expiresInSeconds] (no hardcoded TTL). Called on
     * every login / OTP / verify-mobile success. Backward-compatible: a null/blank
     * refresh token (older backend) leaves the stored value untouched-as-absent, so
     * a later silent refresh treats the session as needs-relogin.
     */
    private suspend fun persistRefreshSession(refreshToken: String?, expiresInSeconds: Long) {
        if (!refreshToken.isNullOrBlank()) {
            localDataSource.saveRefreshToken(refreshToken)
        } else {
            logger.w(TAG_USER_REPO, "Auth response carried no refresh_token; session cannot be silently refreshed")
        }
        if (expiresInSeconds > 0) {
            localDataSource.saveTokenExpiresAt(FleetEpoch.now() + expiresInSeconds * 1000L)
        }
    }

    /**
     * Retrieves auth token or emits session expired event and throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            localDataSource.getAuthToken()
        }
    }

    private fun resolveLoginRole(
        apiRole: String,
        fallbackRole: UserRole,
        token: String
    ): String {
        if (apiRole.isNotBlank()) return apiRole

        val globalRoles = JwtHelper.decodeClaims(token)
            ?.get("global_roles")
            ?.jsonArray
            ?.mapNotNull { it.jsonPrimitive.content.takeIf(String::isNotBlank) }
            .orEmpty()

        return when {
            globalRoles.any { it.equals("owner", ignoreCase = true) } -> "owner"
            globalRoles.any { it.equals("admin", ignoreCase = true) } -> "admin"
            globalRoles.any { it.equals("user", ignoreCase = true) } -> "user"
            else -> UserRole.toApiString(fallbackRole)
        }
    }
}
