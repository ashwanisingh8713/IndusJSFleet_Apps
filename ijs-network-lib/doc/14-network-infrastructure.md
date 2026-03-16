# 14. Network Infrastructure

> **Package:** `com.indusjs.fleet.core.network`  
> **Last Updated:** 16-Mar-2026

---

## Overview

Core networking infrastructure shared across all API modules. Provides HTTP client creation, endpoint configuration, error handling, and error type definitions.

---

## `ApiConfig`

Centralized configuration for all API endpoints and settings.

### Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `BASE_URL` | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2` | Production API base URL |
| `GOOGLE_PLACES_API_KEY` | `AIzaSy...` | Google Maps API key |
| `TIMEOUT_MS` | `30000` | Request timeout (30s) |

### `ApiConfig.Endpoints`

All API paths organized by domain:

| Domain | Endpoints |
|--------|-----------|
| **Auth** | `SIGNUP`, `LOGIN`, `FORGOT_PASSWORD`, `RESET_PASSWORD` |
| **Profile** | `PROFILE`, `CHANGE_PASSWORD` |
| **Dashboard** | `DASHBOARD`, `DASHBOARD_COST_OVERVIEW`, `DASHBOARD_PENDING_PAYMENTS`, `DASHBOARD_ALERTS_STATUS`, `DASHBOARD_VEHICLE_STATUS`, `DASHBOARD_TRIPS_STATUS`, `DASHBOARD_DRIVERS_STATUS`, `DASHBOARD_FINANCIAL_SUMMARY` |
| **Cost Types** | `COST_TYPES_TRIP`, `COST_TYPES_MAINTENANCE`, `COST_TYPES_DRIVER` |
| **Trip Costs** | `TRIP_COSTS` |
| **Maintenance Costs** | `MAINTENANCE_COSTS` |
| **Vehicles** | `VEHICLES` + `vehicleById()`, `vehicleDetail()`, `vehicleTrips()`, `vehicleRoute()`, `vehicleDocuments()`, `vehicleDocumentsDetail()`, `vehicleState()`, `vehicleStateHistory()`, `vehicleHistory()`, `vehiclePurchase()`, `vehicleLoanSummary()`, `vehicleLoanPayments()` |
| **Drivers** | `DRIVERS` + `driverById()`, `driverToggleActive()`, `driverStatus()`, `driverAvailable()` |
| **Trips** | `TRIPS` + `tripById()`, `tripCancel()`, `tripStatus()`, `tripCosts()`, `tripPayments()`, `tripStops()`, `tripStopById()` |
| **Customers** | `CUSTOMERS` + `customerById()` |
| **Payments** | `TRIP_PAYMENTS` + `tripPaymentById()`, `TRIP_PAYMENTS_SUMMARY`, `TRIP_PAYMENTS_TDS_REPORT` |
| **Team** | `TEAM` + `teamMemberById()` |
| **Documents** | `DOCUMENTS` + `documentDownload()` |
| **Finance** | `VEHICLE_LOAN_PAYMENTS` + `vehicleLoanPaymentById()`, `vehicleLoanPaymentPay()`, `VEHICLE_LOAN_PAYMENTS_UPCOMING`, `VEHICLE_LOAN_PAYMENTS_OVERDUE` |
| **Reports** | `REPORTS_PL`, `REPORTS_PL_VEHICLES`, `REPORTS_PL_TRIPS`, `REPORTS_PL_COST_TYPES`, `REPORTS_PL_CONSOLIDATED`, `REPORTS_PL_SUMMARY` + `tripProfitLoss()`, `vehicleProfitLoss()`, `reportsByCostType()` |
| **Driver Costs** | `DRIVER_COSTS` + `driverCostsForDriver()` |
| **Caretakers** | `CARETAKERS` + `caretakerById()` |

---

## `HttpClientProvider`

Factory for creating configured `HttpClient` and `Json` instances.

### Methods

| Method | Description |
|--------|-------------|
| `createJson()` | Creates `Json` with `ignoreUnknownKeys`, `coerceInputValues`, `encodeDefaults`, `explicitNulls=false` |
| `create()` | Creates `HttpClient` with its own `Json` instance |
| `createHttpClient(json)` | Creates `HttpClient` with shared `Json` (for DI) |

### HttpClient Features

| Feature | Configuration |
|---------|--------------|
| **Content Negotiation** | Kotlinx JSON serialization |
| **Logging** | `LogLevel.BODY` via Kermit logger |
| **Timeout** | 30s for request, connect, socket |
| **401 Interceptor** | Auto-detects authenticated requests (has `Authorization: Bearer` header), emits `SessionExpired` event |
| **Default Request** | `Content-Type: application/json` |

### 401 Interceptor Behavior

```
Response 401 received
  → Check: does request have "Authorization: Bearer" header?
    → YES: Emit SessionExpired (authenticated request expired)
    → NO:  Ignore (unauthenticated request, e.g., login with wrong password)
```

### Platform Engines

| Platform | Engine |
|----------|--------|
| Android | `ktor-client-okhttp` |
| iOS | `ktor-client-darwin` |
| JS | `ktor-client-js` |
| WasmJS | `ktor-client-js` |

---

## `ApiErrorHandler`

Centralized error message extraction for consistent user-facing error messages.

### Methods

| Method | Description |
|--------|-------------|
| `extractErrorMessage(statusCode, responseBody)` | Extract from HTTP response |
| `extractErrorMessage(exception)` | Extract from exception |
| `parseDbConstraintError(error)` | Parse DB constraint violations |
| `getHttpStatusMessage(statusCode)` | Map status code to message |
| `getNetworkErrorMessage(exception, fallback)` | Network-specific extraction |

### Error Extraction Priority

1. Server's `"message"` field from JSON body
2. Server's `"error"` field (parsed for DB constraints)
3. Server's `"detail"` field
4. HTTP status code fallback message

### DB Constraint Parsing

| Pattern | User Message |
|---------|-------------|
| `duplicate key` + `email` | "An account with this email already exists" |
| `duplicate key` + `mobile` | "An account with this mobile number already exists" |
| `duplicate key` + `license` | "A driver with this license number already exists" |
| `duplicate key` + `registration` | "A vehicle with this registration number already exists" |
| `foreign key` | "Cannot complete this operation due to related records" |
| `not null` | "Required field is missing" |

### Network Error Detection

| Pattern | User Message |
|---------|-------------|
| `UnknownHostException` | "No internet connection..." |
| `Connection refused` | "Unable to connect to server..." |
| `timeout` / `SocketTimeoutException` | "Connection timed out..." |
| `SSL` / `certificate` | "Secure connection failed..." |
| IP address patterns | "Unable to connect to server..." |

---

## `NetworkError`

Sealed class for typed network errors:

```kotlin
sealed class NetworkError : Exception() {
    data object NoConnection : NetworkError()
    data object Timeout : NetworkError()
    data class ServerError(val code: Int, override val message: String) : NetworkError()
    data class Unknown(override val message: String, override val cause: Throwable?) : NetworkError()
    data class ParseError(override val message: String, override val cause: Throwable?) : NetworkError()
}
```

---

## Source Files

| File | Path |
|------|------|
| ApiConfig | `core/network/ApiConfig.kt` |
| HttpClientProvider | `core/network/HttpClientProvider.kt` |
| ApiErrorHandler | `core/network/ApiErrorHandler.kt` |
| NetworkError | `core/network/NetworkError.kt` |

