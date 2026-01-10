package com.indusjs.fleet.data.repository.user

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSource
import com.indusjs.fleet.data.mapper.user.UserMapper.toDomain
import com.indusjs.fleet.data.model.user.ChangePasswordRequest
import com.indusjs.fleet.data.model.user.ForgotPasswordRequest
import com.indusjs.fleet.data.model.user.LoginRequest
import com.indusjs.fleet.data.model.user.ResetPasswordRequest
import com.indusjs.fleet.data.model.user.SignUpRequest
import com.indusjs.fleet.data.model.user.UpdateProfileRequest
import com.indusjs.fleet.domain.entity.user.AuthResult
import com.indusjs.fleet.domain.entity.user.User
import com.indusjs.fleet.domain.entity.user.UserProfile
import com.indusjs.fleet.domain.entity.user.UserRole
import com.indusjs.fleet.domain.repository.user.UserRepository
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject

/**
 * Implementation of UserRepository.
 * Handles user authentication and profile operations.
 */
@Inject
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {

    private val log = Logger.withTag("UserRepositoryImpl")

    override suspend fun signUp(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthResult> = runCatching {
        val response = remoteDataSource.signUp(
            SignUpRequest(
                email = email,
                mobile = mobile,
                password = password,
                firstName = firstName,
                lastName = lastName
            )
        )

        val authResult = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Sign up failed")

        localDataSource.saveAuthToken(authResult.token)
        localDataSource.saveUserRole(UserRole.toApiString(authResult.user.role))
        authResult
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
        log.d { "Login successful - User: ${authResult.user.email}, Role enum: ${authResult.user.role}, Role to save: '$roleToSave'" }

        localDataSource.saveAuthToken(authResult.token)
        localDataSource.saveUserRole(roleToSave)
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
        newPassword: String
    ): Result<Unit> = runCatching {
        val response = remoteDataSource.resetPassword(
            ResetPasswordRequest(identifier = identifier, newPassword = newPassword)
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

    override suspend fun getAuthToken(): String? = localDataSource.getAuthToken()

    override suspend fun saveAuthToken(token: String) = localDataSource.saveAuthToken(token)

    override suspend fun logout() = localDataSource.clearSession()

    override suspend fun isLoggedIn(): Boolean = localDataSource.isLoggedIn()

    /**
     * Retrieves auth token or throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return localDataSource.getAuthToken()
            ?: throw AuthException.unauthenticated()
    }
}


