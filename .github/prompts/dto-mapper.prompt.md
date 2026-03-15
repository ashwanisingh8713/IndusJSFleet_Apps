# DTO & Mapper Templates

> Use this prompt when creating data transfer objects and mappers for API integration.

## DTO Template

Place at: `data/model/{feature}/`

### List Response DTO

```kotlin
@Serializable
data class My{Feature}ListResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: List<My{Feature}Dto>? = null,
    @SerialName("pagination") val pagination: PaginationDto? = null
)
```

### Single Response DTO

```kotlin
@Serializable
data class My{Feature}Response(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: My{Feature}Dto? = null
)
```

### Entity DTO

```kotlin
@Serializable
data class My{Feature}Dto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("status") val status: String,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("driver_id") val driverId: Int? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("date") val date: String? = null,           // DD-MM-YYYY or ISO
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
```

### Create/Update Request DTO

```kotlin
@Serializable
data class Create{Feature}Request(
    @SerialName("name") val name: String,
    @SerialName("vehicle_id") val vehicleId: Int? = null,
    @SerialName("amount") val amount: Double,
    @SerialName("date") val date: String,    // DD-MM-YYYY for costs, ISO for trips
    @SerialName("notes") val notes: String? = null
)
```

## DTO Rules

1. **Every field** gets `@SerialName("snake_case")` matching the API exactly
2. Kotlin property uses `camelCase`
3. API returns `Int` for IDs — domain entity converts to `String`
4. All nullable fields must have `= null` default
5. Booleans default to sensible value (`= true`, `= false`)
6. Amounts are `Double` (not `BigDecimal` — KMP limitation)
7. Dates remain as `String` — parsed/formatted via `FleetDateTime`

## Mapper Template

Place at: `data/mapper/{feature}/`

```kotlin
class {Feature}Mapper {

    fun toDomain(dto: My{Feature}Dto): {Feature}Entity = {Feature}Entity(
        id = dto.id.toString(),                              // Int → String
        name = dto.name,
        status = dto.status,
        vehicleId = dto.vehicleId?.toString(),
        driverId = dto.driverId?.toString(),
        amount = dto.amount ?: 0.0,
        date = dto.date ?: "",
        notes = dto.notes,
        isActive = dto.isActive,
        createdAt = dto.createdAt
    )

    fun toDomainList(dtos: List<My{Feature}Dto>): List<{Feature}Entity> =
        dtos.map { toDomain(it) }

    // Optional: Domain → DTO for create/update
    fun toRequest(entity: {Feature}Entity): Create{Feature}Request =
        Create{Feature}Request(
            name = entity.name,
            vehicleId = entity.vehicleId?.toIntOrNull(),
            amount = entity.amount,
            date = entity.date,
            notes = entity.notes
        )
}
```

## Mapper Rules

1. Always convert `Int` IDs to `String` in domain entities
2. Handle nulls with defaults (`?: ""`, `?: 0.0`, `?: emptyList()`)
3. Never put date formatting logic in mappers — use `FleetDateTime` in the UI layer
4. Keep mappers pure functions — no side effects, no network calls
5. One mapper per feature (e.g., `VehicleMapper`, `TripMapper`, `DriverMapper`)

## Existing Mappers Reference

| Mapper | Location | Maps |
|--------|----------|------|
| `VehicleMapper` | `data/mapper/vehicle/` | `VehicleDto` → `Vehicle` |
| `DriverMapper` | `data/mapper/driver/` | `DriverDto` → `Driver` |
| `TripMapper` | `data/mapper/trip/` | `TripDto` → `Trip` |
| `TripStopMapper` | `data/mapper/trip/` | `TripStopDto` → `TripStop` |
| `DashboardCacheMapper` | `data/mapper/dashboard/` | Dashboard cache ↔ domain |

