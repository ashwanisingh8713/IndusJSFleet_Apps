package com.indusjs.fleet.core.network

/**
 * API configuration for the Fleet Management API.
 */
object ApiConfig {
    /**
     * Base URL for the Fleet Management API.
     */
    const val BASE_URL = "http://192.168.1.4:8080/api/v1"

    /**
     * API endpoints
     */
    object Endpoints {
        // Authentication
        const val SIGNUP = "/auth/signup"
        const val LOGIN = "/auth/login"
        const val FORGOT_PASSWORD = "/auth/forgot-password"
        const val RESET_PASSWORD = "/auth/reset-password"

        // User Profile
        const val PROFILE = "/profile"
        const val CHANGE_PASSWORD = "/profile/change-password"
    }

    /**
     * Request timeout in milliseconds
     */
    const val TIMEOUT_MS = 30_000L
}

