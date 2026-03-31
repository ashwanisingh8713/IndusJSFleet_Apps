# ijs-network-lib — Networking & Cross-Cutting Data Layer

**Namespace:** `com.indusjs.fleet.network` (and `com.indusjs.fleet.core.network`, `com.indusjs.fleet.data.*`)
**Depends on:** `ijs-core-lib` (api — re-exports entire foundation chain)

## File Tree (33 files, all commonMain)

```
ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/
├── core/auth/
│   ├── AuthenticationManager.kt       # Session events (SharedFlow), logout, 401 handling
│   └── AuthTokenHelper.kt            # Token validation + redirect on missing
├── core/network/
│   ├── ApiConfig.kt                   # Base URL, all endpoint paths, Google API key
│   ├── ApiErrorHandler.kt            # Parse JSON error bodies, friendly messages
│   ├── HttpClientProvider.kt         # Ktor HttpClient factory (JSON, logging, timeout, 401)
│   └── NetworkError.kt               # Sealed error types (NoConnection, Timeout, etc.)
├── data/datasource/
│   ├── costs/
│   │   ├── CostsLocalDataSource.kt   # Room-based cost types cache
│   │   └── CostsRemoteDataSource.kt  # Trip/maintenance/driver cost API calls
│   ├── dashboard/
│   │   ├── DashboardLocalDataSource.kt  # Room-based dashboard cache
│   │   └── DashboardRemoteDataSource.kt # Dashboard stats, financials API
│   ├── location/
│   │   └── GooglePlacesService.kt    # Places autocomplete, details, distance matrix
│   └── user/
│       ├── UserLocalDataSource.kt    # Auth token/role/id storage (multiplatform-settings)
│       └── UserRemoteDataSource.kt   # Login, signup, profile API calls
├── data/mapper/
│   ├── dashboard/DashboardMapper.kt  # Dashboard DTO → domain
│   └── user/UserMapper.kt           # User DTO → domain
├── data/model/
│   ├── dashboard/DashboardModels.kt  # Dashboard DTOs
│   └── user/UserDto.kt              # User/auth DTOs
├── data/repository/
│   ├── costs/
│   │   ├── CostsRepositoryImpl.kt    # Trip/maintenance/vehicle/driver cost operations
│   │   └── CostTypesRepositoryImpl.kt # Cost type fetching + caching
│   ├── dashboard/DashboardRepositoryImpl.kt  # Offline-first dashboard
│   └── user/UserRepositoryImpl.kt    # Auth + profile operations
├── di/
│   └── NetworkDataGraph.kt           # Manual DI wiring (not Metro-generated)
├── domain/entity/
│   ├── dashboard/DashboardStats.kt   # Dashboard domain entities (20+ data classes)
│   └── maps/MapEntities.kt          # MapVehicle, MapLocation, Geofence
├── domain/repository/
│   ├── costs/CostsRepository.kt, CostTypesRepository.kt
│   ├── dashboard/DashboardRepository.kt
│   └── user/UserRepository.kt
├── domain/usecase/
│   ├── costs/GetCostTypesUseCase.kt, InitializeCostTypesUseCase.kt
│   └── dashboard/DashboardUseCases.kt, GetFinancialSummaryUseCase.kt
└── network/LogTags.kt
```

## HTTP Client Configuration

**File:** `HttpClientProvider.kt`

```
HttpClient {
    ContentNegotiation → kotlinx JSON (ignoreUnknownKeys, isLenient)
    Logging → LogLevel.BODY via Kermit tag "HTTP"
    HttpTimeout → 30s (request, connect, socket)
    HttpResponseValidator → on 401 with Bearer token: emit session expired
    defaultRequest → Content-Type: application/json
}
```

Platform engines (via Gradle, no expect/actual):
- Android: OkHttp
- iOS: Darwin
- JS/WasmJS: Ktor JS engine

## Authentication System

### UserLocalDataSource
Stores in `multiplatform-settings` (KV store):
- `auth_token` — Bearer token
- `user_role` — Owner/GM/Manager/Supervisor
- `user_id` — User identifier

### AuthenticationManager
```kotlin
object AuthenticationManager {
    val authEvents: SharedFlow<AuthEvent>    // replay = 1
    fun registerSessionClearCallback(callback: () -> Unit)
    suspend fun emitSessionExpired(message: String)
    suspend fun emitLoggedOut()
}
```

Events: `Unauthorized`, `SessionExpired(message)`, `LoggedOut`

### 401 Handling Flow
1. HttpClient response validator detects 401 + Bearer token present
2. Calls `AuthenticationManager.emitSessionExpired()`
3. Registered callback clears session in `UserLocalDataSource`
4. `App.kt` collects auth event → navigates to Login screen

### AuthTokenHelper
- `requireAuthTokenOrRedirect(userLocalDataSource)` — returns token or emits session expired + throws `AuthException`

## API Configuration

**Base URL:** `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2`

### Key Endpoints (ApiConfig.Endpoints)

| Category | Path Pattern |
|----------|-------------|
| Auth | `/auth/signup`, `/auth/login`, `/auth/forgot-password`, `/auth/reset-password` |
| Profile | `/profile`, `/profile/change-password` |
| Dashboard | `/dashboard`, `/dashboard/cost-overview`, `/dashboard/pending-payments`, `/dashboard/alerts-status`, `/dashboard/financial-summary` |
| Vehicles | `/vehicles`, `/vehicles/{id}`, `/vehicles/{id}/documents`, `/vehicles/{id}/maintenance-costs` |
| Drivers | `/drivers`, `/drivers/{id}`, `/drivers/{id}/toggle-active`, `/drivers/{id}/costs` |
| Trips | `/trips`, `/trips/{id}`, `/trips/{id}/cancel`, `/trips/{id}/costs`, `/trips/{id}/costs/bulk` |
| Customers | `/customers`, `/customers/{id}` |
| Payments | `/payments`, `/payments/{id}` |
| Team | `/team`, `/team/{id}` |
| Reports | `/reports/vehicle-pl`, `/reports/trip-pl`, `/reports/cost-analysis`, `/reports/consolidated-pl` |
| Finance | `/vehicle-finance`, `/vehicle-finance/{id}`, `/vehicle-finance/{id}/payments` |
| Cost Types | `/cost-types/trip`, `/cost-types/maintenance`, `/cost-types/driver` |
| Google | Places Autocomplete, Details, Distance Matrix (external API) |

## NetworkDataGraph (Manual DI)

```kotlin
class NetworkDataGraph private constructor(...) {
    val userRepository: UserRepository
    val dashboardRepository: DashboardRepository
    val costsRepository: CostsRepository
    val costTypesRepository: CostTypesRepository
    val userLocalDataSource: UserLocalDataSource

    companion object {
        fun create(
            httpClient: HttpClient,
            json: Json,
            settings: Settings,
            dispatcherProvider: DispatcherProvider,
            dashboardLocalDataSource: DashboardLocalDataSource,
            costsLocalDataSource: CostsLocalDataSource,
            fleetLogger: FleetLogger
        ): NetworkDataGraph
    }
}
```

## Google Places Service

```kotlin
class GooglePlacesService(httpClient: HttpClient, apiKey: String) {
    suspend fun searchPlaces(query: String, sessionToken: String?): kotlin.Result<List<PlacePrediction>>
    suspend fun getPlaceDetails(placeId: String, sessionToken: String?): kotlin.Result<PlaceDetails>
    suspend fun getDistance(origin: String, destination: String): kotlin.Result<DistanceInfo>
}
```

- Autocomplete: India bias (`components=country:in`), min 3 chars
- Details: `formatted_address`, `geometry`, `name`
- Distance: driving mode, metric units

## ApiErrorHandler

Parses API error responses:
1. Tries JSON `message` field
2. Tries `error` field
3. Tries `detail` field
4. DB constraint friendly messages (duplicate key, foreign key, etc.)
5. HTTP status fallback
6. Exception message heuristics
