package com.indusjs.fleet.core.network

/**
 * API configuration for the Fleet Management API.
 *
 * All API endpoint paths are centralized here to ensure consistency
 * across all data sources and modules.
 */
object ApiConfig {


    // ── Dev base URLs (the active one is chosen at runtime per platform/device) ──────
    // A REAL device cannot reach 10.0.2.2 — that's only the Android *emulator's* alias for the host
    // machine's localhost. So the DEFAULT is the dev machine's LAN IP, reachable from a physical
    // phone on the SAME Wi-Fi (the backend binds to *:8081), the iOS simulator, and web. The Android
    // app shell (FleetApplication) overrides it to the emulator loopback when it detects an emulator,
    // so ONE build runs on both the emulator AND a real device.
    // Update LOCAL_LAN_BASE_URL when your machine's network changes (`ipconfig getifaddr en0`) and
    // keep androidApp's network_security_config.xml in sync.
    const val LOCAL_LAN_BASE_URL = "http://192.168.1.8:8081/api/v1"
    const val ANDROID_EMULATOR_BASE_URL = "http://10.0.2.2:8081/api/v1"
    // const val CLOUD_RUN_BASE_URL = "https://indusjsfleet-api-clean-architecture-refactor-960880113496.asia-south1.run.app/api/v1"

    /**
     * Active API base URL. Defaults to the LAN IP; set ONCE at startup before any network call
     * (see FleetApplication for the Android emulator override). It is read per-request, so updating
     * it before the first request is sufficient.
     */
    var BASE_URL: String = LOCAL_LAN_BASE_URL

    /**
     * Server origin without the "/api/v1" suffix. Computed (get()) so it always reflects the current
     * [BASE_URL]. Backend-issued relative URLs (e.g. document download_url) already include "/api/v1",
     * so prefix them with BASE_ORIGIN, NOT BASE_URL, to avoid double "/api/v1".
     */
    val BASE_ORIGIN: String get() = BASE_URL.removeSuffix("/api/v1")

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
        /**
         * Exchanges a (rotating) refresh token for a NEW access+refresh pair.
         * Body: {"refresh_token":"<opaque>"} → 200 {data:{access_token, refresh_token,
         * token_type, expires_in}}. A 401 means the refresh token is
         * invalid/expired/REUSED → the session is dead → re-login.
         */
        const val REFRESH = "/auth/refresh"
        const val LOGOUT = "/auth/logout"
        const val FORGOT_PASSWORD = "/auth/forgot-password"
        const val RESET_PASSWORD = "/auth/reset-password"

        // ── Email / Mobile Verification (required after signup) ─────────
        const val VERIFY_EMAIL = "/auth/verify-email"
        const val VERIFY_EMAIL_OTP = "/auth/verify-email/otp"
        const val VERIFY_MOBILE = "/auth/verify-mobile"

        // ── OTP Passwordless Login ───────────────────────────────────────
        const val LOGIN_OTP_SEND = "/auth/login/otp/send"
        const val LOGIN_OTP_VERIFY = "/auth/login/otp/verify"

        // ── User Profile ────────────────────────────────────────────────
        const val PROFILE = "/profile"
        const val CHANGE_PASSWORD = "/profile/change-password"

        // ── Permissions / Roles (current user) ──────────────────────────
        const val ME_PERMISSIONS = "/me/permissions"
        const val TEAM_ROLES = "/team/roles"

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
        // Backend (team_routes.go): collection at /team/members,
        // single member at /team/members/:id.
        const val TEAM_MEMBERS = "/team/members"
        fun teamMemberById(memberId: String) = "$TEAM_MEMBERS/$memberId"

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
        const val REPORTS_PL_CUSTOMERS = "/reports/profit-loss/customers"
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

        // ── Subscription / Billing (IAM-proxied) ────────────────────────
        const val SUBSCRIPTION_PLANS = "/plans"
        const val ONBOARDING_STATUS = "/onboarding/status"
        const val SELECT_PLAN = "/onboarding/plan"
        const val PAYMENT_ORDERS = "/payments/orders"
        const val PAYMENT_VERIFY = "/payments/verify"
        const val PAYMENTS_LIST = "/payments"
        fun paymentById(paymentId: String) = "/payments/$paymentId"
        const val CREATE_TENANT = "/tenants"
    }

    /**
     * Request timeout in milliseconds
     */
    const val TIMEOUT_MS = 30_000L
}