package com.indusjs.fleet.core.error

import com.indusjs.error.handler.ErrorContext

/**
 * Fleet-specific error context for screen-specific error messages.
 * Implements the ErrorContext interface from ijs-error-lib.
 */
enum class FleetErrorContext(
    override val contextName: String
) : ErrorContext {
    DASHBOARD("dashboard") {
        override fun getNetworkErrorPrefix() = "Unable to load your dashboard"
        override fun getTimeoutErrorPrefix() = "Dashboard is taking too long to load"
    },
    VEHICLES("vehicles") {
        override fun getNetworkErrorPrefix() = "Unable to load vehicles"
        override fun getTimeoutErrorPrefix() = "Loading vehicles is taking too long"
        override fun getNotFoundMessage() = "Vehicle not found"
        override fun getUnauthorizedMessage() = "access this vehicle"
    },
    VEHICLE_DETAIL("vehicle details") {
        override fun getNetworkErrorPrefix() = "Unable to load vehicle details"
        override fun getTimeoutErrorPrefix() = "Loading vehicle details is taking too long"
        override fun getNotFoundMessage() = "Vehicle not found"
    },
    DRIVERS("drivers") {
        override fun getNetworkErrorPrefix() = "Unable to load drivers"
        override fun getTimeoutErrorPrefix() = "Loading drivers is taking too long"
        override fun getNotFoundMessage() = "Driver not found"
        override fun getUnauthorizedMessage() = "access this driver"
    },
    DRIVER_DETAIL("driver details") {
        override fun getNetworkErrorPrefix() = "Unable to load driver details"
        override fun getTimeoutErrorPrefix() = "Loading driver details is taking too long"
        override fun getNotFoundMessage() = "Driver not found"
    },
    TRIPS("trips") {
        override fun getNetworkErrorPrefix() = "Unable to load trips"
        override fun getTimeoutErrorPrefix() = "Loading trips is taking too long"
        override fun getNotFoundMessage() = "Trip not found"
        override fun getUnauthorizedMessage() = "access this trip"
    },
    TRIP_DETAIL("trip details") {
        override fun getNetworkErrorPrefix() = "Unable to load trip details"
        override fun getTimeoutErrorPrefix() = "Loading trip details is taking too long"
        override fun getNotFoundMessage() = "Trip not found"
    },
    TEAM("team") {
        override fun getNetworkErrorPrefix() = "Unable to load team members"
        override fun getTimeoutErrorPrefix() = "Loading team members is taking too long"
        override fun getNotFoundMessage() = "Team member not found"
        override fun getUnauthorizedMessage() = "manage team members"
    },
    PROFILE("profile") {
        override fun getNetworkErrorPrefix() = "Unable to load your profile"
        override fun getTimeoutErrorPrefix() = "Loading your profile is taking too long"
        override fun getNotFoundMessage() = "Profile not found"
    },
    LOGIN("login") {
        override fun getNetworkErrorPrefix() = "Unable to sign in"
        override fun getTimeoutErrorPrefix() = "Sign in is taking too long"
    },
    SIGNUP("signup") {
        override fun getNetworkErrorPrefix() = "Unable to create account"
        override fun getTimeoutErrorPrefix() = "Account creation is taking too long"
    },
    MAPS("map") {
        override fun getNetworkErrorPrefix() = "Unable to load map data"
        override fun getTimeoutErrorPrefix() = "Loading map is taking too long"
    },
    DOCUMENTS("documents") {
        override fun getNetworkErrorPrefix() = "Unable to load documents"
        override fun getTimeoutErrorPrefix() = "Loading documents is taking too long"
        override fun getNotFoundMessage() = "Document not found"
        override fun getUnauthorizedMessage() = "access these documents"
    },
    COSTS("costs") {
        override fun getNetworkErrorPrefix() = "Unable to load costs"
        override fun getTimeoutErrorPrefix() = "Loading costs is taking too long"
        override fun getNotFoundMessage() = "Cost entry not found"
    },
    GENERIC("data") {
        override fun getNetworkErrorPrefix() = "Unable to connect"
        override fun getTimeoutErrorPrefix() = "Request is taking too long"
    }
}
