# API Conventions — IndusJS Fleet

## Base URL

```
Production: https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2
```

## Date & Time Formats (CRITICAL)

| Context | Date Format | Time Format | Example |
|---------|-------------|-------------|---------|
| Trip scheduling | ISO 8601 | ISO 8601 | `2026-01-04T14:30:00Z` |
| Cost entries | `DD-MM-YYYY` | `HH:MM` | `"date": "20-12-2025"` |
| Document expiry | `DD-MM-YYYY` | N/A | `"expiry_date": "31-12-2026"` |
| UI display | `DD-MM-YYYY` | `HH:MM` (24hr) | `04-01-2026`, `14:30` |

**Conversion:** Use `FleetDateTime.toIso8601(date, time)` from `ijs-datetime-utils`.

## DTO Rules

```kotlin
@Serializable
data class VehicleDto(
    @SerialName("id") val id: Int,
    @SerialName("vehicle_number") val vehicleNumber: String,  // snake_case API → camelCase Kotlin
    @SerialName("is_active") val isActive: Boolean = false,
)
```

- Every DTO field must have `@SerialName` with snake_case
- All DTOs in `data/model/{feature}/`
- DTOs are **separate** from domain entities — use Mappers

## API Response Wrapper

```kotlin
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T? = null
)
```

## Repository Pattern

```kotlin
override fun getVehicles(): Flow<Result<List<Vehicle>>> = flow {
    emit(Result.Loading)
    val token = userLocalDataSource.getAuthToken()
    val response = remoteDataSource.getVehicles(token)
    if (response.success && response.data != null) {
        emit(Result.Success(response.data.map { mapper.toDomain(it) }))
    } else {
        emit(Result.Error(ApiException(response.message), response.message))
    }
}.catch { e -> emit(Result.Error(e, e.message)) }
```

## Auth Token

- Stored via `UserLocalDataSource.getAuthToken()`
- Passed as `Authorization: Bearer $token` header
- 401 responses trigger `AuthenticationManager.notifyAuthExpired()`

## Key Files

- `ijs-network-lib/.../core/network/ApiConfig.kt` — Base URL, endpoints
- `ijs-network-lib/.../core/network/ApiErrorHandler.kt` — Error extraction
- `ijs-datetime-utils/.../FleetDateTime.kt` — Date conversion

