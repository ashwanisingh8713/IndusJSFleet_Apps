package com.indusjs.fleet.domain.repository.user

import com.indusjs.fleet.domain.entity.user.AuthResult
import com.indusjs.fleet.domain.entity.user.SignUpResult
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
     * Returns a SignUpResult — no token is issued; verification required before login.
     */
    suspend fun signUp(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String
    ): Result<SignUpResult>

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
     * Reset password using the reset token from the forgot-password email and new password.
     * @param identifier email or mobile used to request the reset
     * @param resetToken one-time token delivered via email/SMS
     * @param newPassword the new password to set
     */
    suspend fun resetPassword(
        identifier: String,
        resetToken: String,
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
     * Verify email using OTP sent during signup.
     */
    suspend fun verifyEmailOtp(email: String, otp: String): Result<Unit>

    /**
     * Verify mobile using the OTP/token sent during signup.
     * Returns an access token on success (user is now fully verified).
     */
    suspend fun verifyMobile(token: String): Result<String?>

    /**
     * Send a login OTP to the given mobile number.
     */
    suspend fun sendLoginOtp(mobile: String): Result<Unit>

    /**
     * Verify login OTP and get auth tokens.
     */
    suspend fun verifyLoginOtp(mobile: String, otp: String): Result<AuthResult>

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

