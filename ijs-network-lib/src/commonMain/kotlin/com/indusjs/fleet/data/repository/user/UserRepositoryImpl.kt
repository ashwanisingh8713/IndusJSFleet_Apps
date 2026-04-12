package com.indusjs.fleet.data.repository.user

import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.auth.AuthTokenHelper
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
import com.indusjs.fleet.network.TAG_USER_REPO
import dev.zacsweers.metro.Inject

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

        val authResult = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Login failed")

        val roleToSave = UserRole.toApiString(authResult.user.role)
        logger.d(TAG_USER_REPO, "Login successful - User: ${authResult.user.email}, Role enum: ${authResult.user.role}, Role to save: '$roleToSave'")

        localDataSource.saveAuthToken(authResult.token)
        localDataSource.saveUserRole(roleToSave)
        localDataSource.saveUserId(authResult.user.id)
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
        identifier: String,
        resetToken: String,
        newPassword: String
    ): Result<Unit> = runCatching {
        val response = remoteDataSource.resetPassword(
            ResetPasswordRequest(
                identifier = identifier,
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

        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to get profile")
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

        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to update profile")
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
        val accessToken = response.data?.token
        if (!accessToken.isNullOrBlank()) {
            localDataSource.saveAuthToken(accessToken)
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
        val authResult = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "OTP verification failed")
        localDataSource.saveAuthToken(authResult.token)
        localDataSource.saveUserRole(UserRole.toApiString(authResult.user.role))
        localDataSource.saveUserId(authResult.user.id)
        authResult
    }

    override suspend fun getAuthToken(): String? = localDataSource.getAuthToken()

    override suspend fun saveAuthToken(token: String) = localDataSource.saveAuthToken(token)

    override suspend fun logout() = localDataSource.clearSession()

    override suspend fun isLoggedIn(): Boolean = localDataSource.isLoggedIn()

    /**
     * Retrieves auth token or emits session expired event and throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            localDataSource.getAuthToken()
        }
    }
}
