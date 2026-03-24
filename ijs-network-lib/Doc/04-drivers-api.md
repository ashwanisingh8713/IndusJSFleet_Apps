# 04. Drivers API

> **Package:** `com.indusjs.fleet.data.datasource.driver`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/drivers` | List drivers (with optional filters) |
| `GET` | `/drivers/available` | List available drivers |
| `GET` | `/drivers/{id}` | Get driver by ID |
| `POST` | `/drivers` | Create a new driver |
| `PUT` | `/drivers/{id}` | Update driver |
| `DELETE` | `/drivers/{id}` | Delete driver |

### Status Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `PATCH` | `/drivers/{id}/toggle-active` | Toggle driver active status |
| `PATCH` | `/drivers/{id}/status` | Update driver status with reason |
| `GET` | `/drivers/{id}/state-history` | Get state change history |

### History

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/drivers/{id}/history` | Get driver change history (paginated) |

---

## Data Source Interface

### `DriverRemoteDataSource`

```kotlin
interface DriverRemoteDataSource : RemoteDataSource {
    suspend fun getDrivers(
        token: String,
        status: String? = null,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 50
    ): DriverApiResponse<List<DriverDto>>

    suspend fun getAvailableDrivers(token: String): DriverApiResponse<List<DriverDto>>
    suspend fun getDriverById(token: String, id: String): DriverApiResponse<DriverDto>
    suspend fun createDriver(token: String, request: CreateDriverRequest): DriverApiResponse<DriverDto>
    suspend fun updateDriver(token: String, id: String, request: UpdateDriverRequest): DriverApiResponse<DriverDto>
    suspend fun updateDriverStatus(token: String, id: String, status: String): DriverApiResponse<DriverDto>
    suspend fun toggleDriverActive(token: String, id: String): DriverApiResponse<DriverDto>
    suspend fun deleteDriver(token: String, id: String): DriverApiResponse<Unit>

    suspend fun updateDriverStatusWithReason(
        token: String, id: String,
        status: String, reason: String?, notes: String?
    ): DriverApiResponse<DriverDto>

    suspend fun getDriverHistory(token: String, id: String, page: Int, perPage: Int): DriverHistoryApiResponse
    suspend fun getDriverStateHistory(token: String, id: String, page: Int, perPage: Int): DriverApiResponse<StateHistoryResponseDto>
}
```

**Implementation:** `DriverRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

---

## Query Parameters (List Drivers)

| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | `String?` | Filter by state: `active`, `inactive`, `on_route`, `on_leave`, `suspended`, `terminated` |
| `search` | `String?` | Search by name or mobile |
| `page` | `Int` | Page number (default: 1) |
| `per_page` | `Int` | Items per page (default: 50) |

---

## Key DTOs

### `CreateDriverRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `firstName` | `first_name` | `String` | ✅ |
| `lastName` | `last_name` | `String` | ✅ |
| `mobile` | `mobile` | `String` | ✅ |
| `email` | `email` | `String?` | ❌ |
| `licenseNumber` | `license_number` | `String` | ✅ |
| `licenseExpiry` | `license_expiry` | `String` | ✅ |
| `licenseType` | `license_type` | `String?` | ❌ |
| `address` | `address` | `String?` | ❌ |
| `dateOfBirth` | `date_of_birth` | `String?` | ❌ |
| `emergencyContact` | `emergency_contact` | `String?` | ❌ |

### `DriverDto`

| Field | JSON Key | Type |
|-------|----------|------|
| `id` | `id` | `Int` |
| `firstName` | `first_name` | `String` |
| `lastName` | `last_name` | `String` |
| `mobile` | `mobile` | `String` |
| `email` | `email` | `String?` |
| `licenseNumber` | `license_number` | `String` |
| `licenseExpiry` | `license_expiry` | `String?` |
| `state` | `state` | `String` |
| `isActive` | `is_active` | `Boolean` |
| `assignedVehicleId` | `assigned_vehicle_id` | `Int?` |
| `assignedVehicle` | `assigned_vehicle` | Nested vehicle info |

### Driver States

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```

### `StatusUpdateRequestDto`

Used for updating driver status with reason:

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `status` | `status` | `String` | ✅ |
| `reason` | `reason` | `String?` | ❌ |
| `notes` | `notes` | `String?` | ❌ |

---

## Source Files

| File | Path |
|------|------|
| DriverRemoteDataSource | `data/datasource/driver/DriverRemoteDataSource.kt` |
| DriverDto & related | `data/model/driver/` (in DriverCostModels.kt and related) |
| StateHistoryResponseDto | `data/model/state/` |
| DriverHistoryApiResponse | `data/model/history/` |

