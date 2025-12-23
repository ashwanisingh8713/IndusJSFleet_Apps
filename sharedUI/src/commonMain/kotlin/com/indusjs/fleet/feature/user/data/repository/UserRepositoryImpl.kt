package com.indusjs.fleet.feature.user.data.repository

import com.indusjs.fleet.feature.user.data.datasource.UserLocalDataSource
import com.indusjs.fleet.feature.user.data.datasource.UserRemoteDataSource
import com.indusjs.fleet.feature.user.data.mapper.UserMapper.toDomain
import com.indusjs.fleet.feature.user.data.model.ChangePasswordRequest
import com.indusjs.fleet.feature.user.data.model.ForgotPasswordRequest
import com.indusjs.fleet.feature.user.data.model.LoginRequest
import com.indusjs.fleet.feature.user.data.model.ResetPasswordRequest
import com.indusjs.fleet.feature.user.data.model.SignUpRequest
import com.indusjs.fleet.feature.user.data.model.UpdateProfileRequest
import com.indusjs.fleet.feature.user.domain.entity.AuthResult
import com.indusjs.fleet.feature.user.domain.entity.User
import com.indusjs.fleet.feature.user.domain.entity.UserProfile
import com.indusjs.fleet.feature.user.domain.repository.UserRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of UserRepository.
 */
@Inject
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDataSource: UserLocalDataSource
) : UserRepository {

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

        if (response.success && response.data != null) {
            val authResult = response.data.toDomain()
            localDataSource.saveAuthToken(authResult.token)
            authResult
        } else {
            throw Exception(response.message ?: "Sign up failed")
        }
    }

    override suspend fun login(
        identifier: String,
        password: String
    ): Result<AuthResult> = runCatching {
        val response = remoteDataSource.login(
            LoginRequest(identifier = identifier, password = password)
        )

        if (response.success && response.data != null) {
            val authResult = response.data.toDomain()
            localDataSource.saveAuthToken(authResult.token)
            authResult
        } else {
            throw Exception(response.message ?: "Login failed")
        }
    }

    override suspend fun forgotPassword(identifier: String): Result<Unit> = runCatching {
        val response = remoteDataSource.forgotPassword(
            ForgotPasswordRequest(identifier = identifier)
        )

        if (!response.success) {
            throw Exception(response.message ?: "Forgot password request failed")
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
            throw Exception(response.message ?: "Password reset failed")
        }
    }

    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val token = localDataSource.getAuthToken()
            ?: throw Exception("Not authenticated")

        val response = remoteDataSource.getProfile(token)

        if (response.success && response.data != null) {
            response.data.toDomain()
        } else {
            throw Exception(response.message ?: "Failed to get profile")
        }
    }

    override suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        email: String?,
        mobile: String?
    ): Result<User> = runCatching {
        val token = localDataSource.getAuthToken()
            ?: throw Exception("Not authenticated")

        val response = remoteDataSource.updateProfile(
            token = token,
            request = UpdateProfileRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                mobile = mobile
            )
        )

        if (response.success && response.data != null) {
            response.data.toDomain()
        } else {
            throw Exception(response.message ?: "Failed to update profile")
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit> = runCatching {
        val token = localDataSource.getAuthToken()
            ?: throw Exception("Not authenticated")

        val response = remoteDataSource.changePassword(
            token = token,
            request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )
        )

        if (!response.success) {
            throw Exception(response.message ?: "Failed to change password")
        }
    }

    override suspend fun getAuthToken(): String? {
        return localDataSource.getAuthToken()
    }

    override suspend fun saveAuthToken(token: String) {
        localDataSource.saveAuthToken(token)
    }

    override suspend fun logout() {
        localDataSource.clearSession()
    }

    override suspend fun isLoggedIn(): Boolean {
        return localDataSource.isLoggedIn()
    }
}

