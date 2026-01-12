package com.indusjs.fleet.core.network

/**
 * API configuration for the Fleet Management API.
 */
object ApiConfig {
    /**
     * Base URL for the Fleet Management API.
     */
    //const val BASE_URL = "http://192.168.1.8:8080/api/v1"
    const val BASE_URL = "https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2"

    /**
     * Google Places API Key for location autocomplete.
     * Get your API key from: https://console.cloud.google.com/apis/credentials
     * Enable "Places API" and "Geocoding API" in your Google Cloud project.
     */
    const val GOOGLE_PLACES_API_KEY = "AIzaSyC53gFf6-HnNkvq2biU2WJLS5bpNkoN0XQ" // It belongs from ashwanisingh8713@gmail.com

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

        // Dashboard
        const val DASHBOARD = "/dashboard"
        const val DASHBOARD_COST_OVERVIEW = "/dashboard/cost-overview"
        const val DASHBOARD_PENDING_PAYMENTS = "/dashboard/pending-payments"
        const val DASHBOARD_ALERTS_STATUS = "/dashboard/alerts-status"
        const val DASHBOARD_VEHICLE_STATUS = "/dashboard/vehicle-status"
        const val DASHBOARD_TRIPS_STATUS = "/dashboard/trips-status"
        const val DASHBOARD_DRIVERS_STATUS = "/dashboard/drivers-status"

        // Cost Types (cached in local DB)
        const val COST_TYPES_TRIP = "/cost-types/trip"
        const val COST_TYPES_MAINTENANCE = "/cost-types/maintenance"
        const val COST_TYPES_DRIVER = "/cost-types/driver"

        // Trip Costs
        const val TRIP_COST_TYPES = "/cost-types/trip"
        const val TRIP_COSTS = "/trip-costs"

        // Maintenance Costs
        const val MAINTENANCE_COST_TYPES = "/cost-types/maintenance"
        const val MAINTENANCE_COSTS = "/maintenance-costs"

        // Driver Cost Types
        const val DRIVER_COST_TYPES = "/cost-types/driver"
    }

    /**
     * Request timeout in milliseconds
     */
    const val TIMEOUT_MS = 30_000L
}
