package com.indusjs.fleet.domain.repository.user

import com.indusjs.fleet.domain.entity.user.AuthResult
import com.indusjs.fleet.domain.entity.user.User
import com.indusjs.fleet.domain.entity.user.UserProfile
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for user-related operations.
 * Follows the Repository pattern with domain entities.
 */
interface UserRepository : Repository {

    /**
     * Sign up a new owner account.
     */
    suspend fun signUp(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<AuthResult>

    /**
     * Login with email or mobile and password.
     */
    suspend fun login(
        identifier: String,
        password: String
    ): Result<AuthResult>

    /**
     * Request password reset for an account.
     */
    suspend fun forgotPassword(identifier: String): Result<Unit>

    /**
     * Reset password with new password.
     */
    suspend fun resetPassword(
        identifier: String,
        newPassword: String
    ): Result<Unit>

    /**
     * Get the current user's profile.
     */
    suspend fun getProfile(): Result<UserProfile>

    /**
     * Update the current user's profile.
     */
    suspend fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        mobile: String? = null
    ): Result<User>

    /**
     * Change the current user's password.
     */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Unit>

    /**
     * Get the current authentication token.
     */
    suspend fun getAuthToken(): String?

    /**
     * Save the authentication token.
     */
    suspend fun saveAuthToken(token: String)

    /**
     * Clear the authentication session.
     */
    suspend fun logout()

    /**
     * Check if user is logged in.
     */
    suspend fun isLoggedIn(): Boolean
}

