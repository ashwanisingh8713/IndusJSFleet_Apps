# ijs-network-lib — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.fleet.core.network`, `com.indusjs.fleet.core.auth`, `com.indusjs.fleet.data.*.user`, `com.indusjs.fleet.data.datasource.location`

---

## API Documentation Index

| # | File | Domain | Data Source |
|---|------|--------|-------------|
| 01 | [01-authentication-api.md](01-authentication-api.md) | Auth (signup, login, password reset) | `UserRemoteDataSource`, `UserLocalDataSource` |
| 02 | [02-profile-api.md](02-profile-api.md) | User profile (get, update, change password) | `UserRemoteDataSource` |
| 03 | [03-vehicles-api.md](03-vehicles-api.md) | Vehicle CRUD, state, documents, history | `VehicleRemoteDataSource` |
| 04 | [04-drivers-api.md](04-drivers-api.md) | Driver CRUD, status, history | `DriverRemoteDataSource` |
| 05 | [05-trips-api.md](05-trips-api.md) | Trip CRUD, state, progress, stops | `TripRemoteDataSource` |
| 06 | [06-costs-api.md](06-costs-api.md) | Trip / maintenance / driver costs, cost types | `CostsRemoteDataSource` |
| 07 | [07-dashboard-api.md](07-dashboard-api.md) | Dashboard, cost overview, alerts, financials | `DashboardRemoteDataSource` |
| 08 | [08-customers-api.md](08-customers-api.md) | Customer CRUD, trips, payments, analytics | `CustomerRemoteDataSource` |
| 09 | [09-payments-api.md](09-payments-api.md) | Trip payments CRUD, summary, TDS report | `TripPaymentRemoteDataSource` |
| 10 | [10-team-api.md](10-team-api.md) | Team member CRUD, roles, password reset | `TeamRemoteDataSource` |
| 11 | [11-reports-api.md](11-reports-api.md) | P&L reports (trip, vehicle, fleet, consolidated) | `ReportsRemoteDataSource` |
| 12 | [12-vehicle-finance-api.md](12-vehicle-finance-api.md) | Purchase records, loans, EMI payments | `VehicleFinanceRemoteDataSource` |
| 13 | [13-location-api.md](13-location-api.md) | Google Places autocomplete, distance matrix | `GooglePlacesService` |
| 14 | [14-network-infrastructure.md](14-network-infrastructure.md) | HttpClient, ApiConfig, error handling | `HttpClientProvider`, `ApiErrorHandler` |

---

## 1. Purpose

HTTP networking infrastructure, authentication management, user data layer, and external API services for the IndusJS Fleet application. This module is the **foundation for all API communication** — every feature module depends on it.

## 2. Package Structure

```
src/commonMain/kotlin/com/indusjs/fleet/
├── core/
│   ├── auth/
│   │   ├── AuthenticationManager.kt   # Singleton session event bus (401 handling)
│   │   └── AuthTokenHelper.kt        # requireAuthTokenOrRedirect helper
│   └── network/
│       ├── ApiConfig.kt              # BASE_URL + all endpoint constants
│       ├── ApiErrorHandler.kt        # Error extraction from HTTP responses
│       ├── HttpClientProvider.kt     # HttpClient + Json factory methods
│       └── NetworkError.kt           # Sealed error types
├── data/
│   ├── datasource/
│   │   ├── location/
│   │   │   └── GooglePlacesService.kt  # Places autocomplete + Distance Matrix
│   │   └── user/
│   │       ├── UserLocalDataSource.kt  # Settings-based token/role storage
│   │       └── UserRemoteDataSource.kt # Login/signup/profile API calls
│   ├── mapper/user/
│   │   └── UserMapper.kt             # DTO ↔ Domain conversion
│   ├── model/user/
│   │   └── UserDto.kt                # All user request/response DTOs
│   └── repository/user/
│       └── UserRepositoryImpl.kt      # UserRepository implementation
└── domain/
    └── repository/user/
        └── UserRepository.kt          # Repository interface
```

## 3. Public API Surface

### Network Configuration
| Class | Description |
|-------|-------------|
| `HttpClientProvider.create()` | Creates a fully configured `HttpClient` (Json, logging, timeout, 401 interceptor) |
| `HttpClientProvider.createJson()` | Creates a configured `Json` instance for serialization |
| `HttpClientProvider.createHttpClient(json)` | Creates `HttpClient` with a shared `Json` instance |
| `ApiConfig.BASE_URL` | Fleet Management API base URL |
| `ApiConfig.Endpoints.*` | All API endpoint path constants (auth, dashboard, vehicles, drivers, trips, etc.) |
| `ApiConfig.GOOGLE_PLACES_API_KEY` | Google Places API key |

### Authentication
| Class | Description |
|-------|-------------|
| `AuthenticationManager` | Singleton event bus for auth events (401, logout, session expired) |
| `AuthTokenHelper.requireAuthTokenOrRedirect()` | Gets auth token or emits session expired + throws |
| `AuthenticationEvent` | Sealed interface: `Unauthorized`, `LoggedOut`, `SessionExpired` |

### Error Handling
| Class | Description |
|-------|-------------|
| `ApiErrorHandler.extractErrorMessage(statusCode, body)` | Extracts user-friendly message from API response |
| `ApiErrorHandler.extractErrorMessage(exception)` | Extracts message from exception |
| `ApiErrorHandler.getNetworkErrorMessage(exception)` | Network-specific error message |
| `NetworkError` | Sealed error types: `NoConnection`, `Timeout`, `ServerError`, `Unknown`, `ParseError` |

### User Data Layer
| Class | Description |
|-------|-------------|
| `UserLocalDataSource` | Interface for local auth token/role/session storage |
| `UserRemoteDataSource` | Interface for user API calls (login, signup, profile) |
| `UserRepository` | Domain repository interface |
| `UserRepositoryImpl` | Repository implementation |
| `UserMapper` | DTO ↔ Domain entity conversion |
| `ApiResponse<T>` | Generic API response wrapper |

### Location Services
| Class | Description |
|-------|-------------|
| `GooglePlacesService` | Places autocomplete, place details, road distance calculation |
| `PlacePrediction` | Autocomplete result DTO |
| `PlaceDetails` | Place details with coordinates |
| `DistanceResult` | Road distance + duration result |

## 4. Dependencies

| Dependency | Type | Purpose |
|-----------|------|---------|
| `ijs-core-lib` | `api` | Base contracts, user entities, utilities |
| `ktor-client-core` | `implementation` | HTTP client core |
| `ktor-client-content-negotiation` | `implementation` | JSON content negotiation |
| `ktor-client-serialization` | `implementation` | Request/response serialization |
| `ktor-serialization-json` | `implementation` | Kotlinx JSON serializer |
| `ktor-client-logging` | `implementation` | HTTP request/response logging |
| `multiplatform-settings` | `implementation` | Local key-value storage |
| `kermit` | `implementation` | Logging |

### Platform Engines
| Platform | Engine |
|----------|--------|
| Android | `ktor-client-okhttp` |
| iOS | `ktor-client-darwin` |
| JS | `ktor-client-js` |
| WasmJS | `ktor-client-js` |

## 5. Usage Examples

### Creating HttpClient
```kotlin
// Standalone (creates its own Json)
val httpClient = HttpClientProvider.create()

// With shared Json (for DI frameworks)
val json = HttpClientProvider.createJson()
val httpClient = HttpClientProvider.createHttpClient(json)
```

### Using AuthTokenHelper in Repositories
```kotlin
class MyRepositoryImpl(
    private val userLocalDataSource: UserLocalDataSource,
    private val remoteDataSource: MyRemoteDataSource
) {
    suspend fun getData(): Result<Data> = runCatching {
        val token = AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
        remoteDataSource.fetchData(token)
    }
}
```

### Using ApiConfig Endpoints
```kotlin
val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.VEHICLES}") {
    header(HttpHeaders.Authorization, "Bearer $token")
}
// Or with parameterized endpoints:
val response = httpClient.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.vehicleById(vehicleId)}") { ... }
```

### Observing Auth Events
```kotlin
LaunchedEffect(Unit) {
    AuthenticationManager.authEvents.collect { event ->
        when (event) {
            is AuthenticationEvent.SessionExpired -> navigateToLogin()
            is AuthenticationEvent.LoggedOut -> navigateToLogin()
            is AuthenticationEvent.Unauthorized -> navigateToLogin()
        }
    }
}
```

