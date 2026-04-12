package com.indusjs.fleet.core.network

/**
 * API configuration for the Fleet Management API.
 *
 * All API endpoint paths are centralized here to ensure consistency
 * across all data sources and modules.
 */
object ApiConfig {

    /** `IndusJSFleet_GoLang_Backend` branch `Google_Cloud_SQL` (Cloud Run). */
//    const val BASE_URL = "https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2"

    /** `IndusJSFleet_GoLang_Backend` branch `clean-architecture-code-refactoring` (Cloud Run). */
    const val BASE_URL = "https://indusjsfleet-api-clean-architecture-refactor-960880113496.asia-south1.run.app/api/v1"

    /**
     * Google Places API Key for location autocomplete.
     * Get your API key from: https://console.cloud.google.com/apis/credentials
     * Enable "Places API" and "Geocoding API" in your Google Cloud project.
     */
    const val GOOGLE_PLACES_API_KEY = "AIzaSyC53gFf6-HnNkvq2biU2WJLS5bpNkoN0XQ" // It belongs from ashwanisingh8713@gmail.com

    /**
     * API endpoints organized by feature domain.
     */
    object Endpoints {

        // ── Authentication ──────────────────────────────────────────────
        const val SIGNUP = "/auth/signup"
        const val LOGIN = "/auth/login"
        const val FORGOT_PASSWORD = "/auth/forgot-password"
        const val RESET_PASSWORD = "/auth/reset-password"

        // ── User Profile ────────────────────────────────────────────────
        const val PROFILE = "/profile"
        const val CHANGE_PASSWORD = "/profile/change-password"

        // ── Dashboard ───────────────────────────────────────────────────
        const val DASHBOARD = "/dashboard"
        const val DASHBOARD_COST_OVERVIEW = "/dashboard/cost-overview"
        const val DASHBOARD_PENDING_PAYMENTS = "/dashboard/pending-payments"
        const val DASHBOARD_ALERTS_STATUS = "/dashboard/alerts-status"
        const val DASHBOARD_FINANCIAL_SUMMARY = "/dashboard/financial-summary"
        // NOTE: /dashboard/vehicle-status, /dashboard/trips-status, /dashboard/drivers-status
        // are not documented in the API spec and have no backend confirmation — omitted.

        // ── Cost Types (cached in local DB) ─────────────────────────────
        const val COST_TYPES_TRIP = "/cost-types/trip"
        const val COST_TYPES_MAINTENANCE = "/cost-types/maintenance"
        const val COST_TYPES_DRIVER = "/cost-types/driver"

        // ── Trip Costs ──────────────────────────────────────────────────
        @Deprecated("Use COST_TYPES_TRIP", ReplaceWith("COST_TYPES_TRIP"))
        const val TRIP_COST_TYPES = COST_TYPES_TRIP
        const val TRIP_COSTS = "/trip-costs"

        // ── Maintenance Costs ───────────────────────────────────────────
        @Deprecated("Use COST_TYPES_MAINTENANCE", ReplaceWith("COST_TYPES_MAINTENANCE"))
        const val MAINTENANCE_COST_TYPES = COST_TYPES_MAINTENANCE
        const val MAINTENANCE_COSTS = "/maintenance-costs"

        // ── Driver Cost Types ───────────────────────────────────────────
        @Deprecated("Use COST_TYPES_DRIVER", ReplaceWith("COST_TYPES_DRIVER"))
        const val DRIVER_COST_TYPES = COST_TYPES_DRIVER

        // ── Vehicles ────────────────────────────────────────────────────
        const val VEHICLES = "/vehicles"
        /** GET /{id}, PUT /{id}, DELETE /{id} — append vehicle ID */
        fun vehicleById(vehicleId: String) = "$VEHICLES/$vehicleId"
        fun vehicleWithDocuments() = "$VEHICLES/with-documents"
        fun vehicleDetail(vehicleId: String) = "$VEHICLES/$vehicleId/detail"
        fun vehicleTrips(vehicleId: String) = "$VEHICLES/$vehicleId/trips"
        fun vehicleRoute(vehicleId: String) = "$VEHICLES/$vehicleId/route"
        fun vehicleDocuments(vehicleId: String) = "$VEHICLES/$vehicleId/documents"
        fun vehicleDocumentsDetail(vehicleId: String) = "$VEHICLES/$vehicleId/documents/detail"
        fun vehicleState(vehicleId: String) = "$VEHICLES/$vehicleId/state"
        fun vehicleStateHistory(vehicleId: String) = "$VEHICLES/$vehicleId/state-history"
        fun vehicleHistory(vehicleId: String) = "$VEHICLES/$vehicleId/history"
        fun vehiclePurchase(vehicleId: String) = "$VEHICLES/$vehicleId/purchase"
        fun vehicleLoanSummary(vehicleId: String) = "$VEHICLES/$vehicleId/loan-summary"
        fun vehicleLoanPayments(vehicleId: String) = "$VEHICLES/$vehicleId/loan-payments"

        // ── Drivers ─────────────────────────────────────────────────────
        const val DRIVERS = "/drivers"
        fun driverById(driverId: String) = "$DRIVERS/$driverId"
        fun driverToggleActive(driverId: String) = "$DRIVERS/$driverId/toggle-active"
        fun driverStatus(driverId: String) = "$DRIVERS/$driverId/status"
        fun driverAvailable() = "$DRIVERS/available"

        // ── Trips ───────────────────────────────────────────────────────
        const val TRIPS = "/trips"
        fun tripById(tripId: String) = "$TRIPS/$tripId"
        fun tripCancel(tripId: String) = "$TRIPS/$tripId/cancel"
        fun tripStatus(tripId: String) = "$TRIPS/$tripId/status"
        fun tripCosts(tripId: String) = "$TRIPS/$tripId/costs"
        fun tripCostsBulk(tripId: String) = "$TRIPS/$tripId/costs/bulk"
        fun tripPayments(tripId: String) = "$TRIPS/$tripId/payments"
        fun vehicleTripsById(vehicleId: String) = "$VEHICLES/$vehicleId/trips"

        // ── Trip Stops ──────────────────────────────────────────────────
        fun tripStops(tripId: String) = "$TRIPS/$tripId/stops"
        fun tripStopById(tripId: String, stopId: String) = "$TRIPS/$tripId/stops/$stopId"

        // ── Customers ───────────────────────────────────────────────────
        const val CUSTOMERS = "/customers"
        fun customerById(customerId: String) = "$CUSTOMERS/$customerId"
        fun customerToggleActive(customerId: String) = "$CUSTOMERS/$customerId/toggle-active"
        fun customerTrips(customerId: String) = "$CUSTOMERS/$customerId/trips"
        fun customerStatistics(customerId: String) = "$CUSTOMERS/$customerId/statistics"
        fun customerPendingPayments(customerId: String) = "$CUSTOMERS/$customerId/pending-payments"
        fun customerPayments(customerId: String) = "$CUSTOMERS/$customerId/payments"
        fun customerPaymentSummary(customerId: String) = "$CUSTOMERS/$customerId/payment-summary"
        fun customerFinancialReport(customerId: String) = "$CUSTOMERS/$customerId/financial-report"

        // ── Trip Payments ───────────────────────────────────────────────
        const val TRIP_PAYMENTS = "/trip-payments"
        fun tripPaymentById(paymentId: String) = "$TRIP_PAYMENTS/$paymentId"
        const val TRIP_PAYMENTS_SUMMARY = "/trip-payments/summary"
        const val TRIP_PAYMENTS_TDS_REPORT = "/trip-payments/tds-report"

        // ── Team Management ─────────────────────────────────────────────
        const val TEAM = "/team"
        fun teamMemberById(memberId: String) = "$TEAM/$memberId"

        // ── Documents ───────────────────────────────────────────────────
        const val DOCUMENTS = "/documents"
        fun documentDownload(documentId: String) = "$DOCUMENTS/$documentId/download"

        // ── Vehicle Finance (Loan Payments) ─────────────────────────────
        const val VEHICLE_LOAN_PAYMENTS = "/vehicle-loan-payments"
        fun vehicleLoanPaymentById(paymentId: String) = "$VEHICLE_LOAN_PAYMENTS/$paymentId"
        fun vehicleLoanPaymentPay(paymentId: String) = "$VEHICLE_LOAN_PAYMENTS/$paymentId/pay"
        const val VEHICLE_LOAN_PAYMENTS_UPCOMING = "/vehicle-loan-payments/upcoming"
        const val VEHICLE_LOAN_PAYMENTS_OVERDUE = "/vehicle-loan-payments/overdue"

        // ── Reports (P&L) ───────────────────────────────────────────────
        const val REPORTS_PL = "/reports/profit-loss"
        const val REPORTS_PL_VEHICLES = "/reports/profit-loss/vehicles"
        const val REPORTS_PL_TRIPS = "/reports/profit-loss/trips"
        const val REPORTS_PL_COST_TYPES = "/reports/profit-loss/cost-types"
        const val REPORTS_PL_CONSOLIDATED = "/reports/profit-loss/consolidated"
        const val REPORTS_PL_SUMMARY = "/reports/profit-loss/summary"
        fun tripProfitLoss(tripId: String) = "$TRIPS/$tripId/profit-loss"
        fun vehicleProfitLoss(vehicleId: String) = "$VEHICLES/$vehicleId/profit-loss"
        fun reportsByCostType(costType: String) = "/reports/profit-loss/cost-type/$costType"

        // ── Driver Costs ────────────────────────────────────────────────
        const val DRIVER_COSTS = "/driver-costs"
        fun driverCostsForDriver(driverId: String) = "$DRIVERS/$driverId/costs"

        // ── Caretaker ───────────────────────────────────────────────────
        const val CARETAKERS = "/caretakers"
        fun caretakerById(caretakerId: String) = "$CARETAKERS/$caretakerId"

        // ── Audit Logs (Owner/GM only) ──────────────────────────────────
        const val AUDIT_LOGS = "/audit-logs"

        // ── States (reference data) ─────────────────────────────────────
        const val STATES = "/states"

        // ── Location Tracking ───────────────────────────────────────────
        const val LOCATIONS_TRACK = "/locations/track"
        const val LOCATIONS_CURRENT = "/locations/current"

        // ── Health / Status (unauthenticated) ───────────────────────────
        const val HEALTH = "/health"
        const val STATUS = "/status"
    }

    /**
     * Request timeout in milliseconds
     */
    const val TIMEOUT_MS = 30_000L
}
