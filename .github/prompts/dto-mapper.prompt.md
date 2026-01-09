# DTO & Mapper Template

Generate DTOs and Mappers for API communication in the IndusJS Fleet app.

## Feature Information
- **Feature Name**: [FEATURE_NAME]
- **Entity Name**: [ENTITY_NAME]
- **API Endpoint**: [API_ENDPOINT]

## Critical Rules

### Date/Time Format Conversion
```
UI Display          → API Request
DD-MM-YYYY          → YYYY-MM-DDTHH:MM:SSZ (ISO 8601)
HH:MM (24hr)        → Combined with date in ISO 8601

Example:
UI: "04-01-2026" + "14:30"
API: "2026-01-04T14:30:00Z"
```

### SerialName Convention
- **ALWAYS** use `@SerialName("snake_case")` for API fields
- Kotlin property names use camelCase
- Default values for nullable fields

---

## DTO Templates

### Main Entity DTO
```kotlin
package com.indusjs.fleet.data.model.{feature}

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for {Entity} API responses.
 * Maps to/from API JSON with snake_case field names.
 */
@Serializable
data class {Entity}Dto(
    @SerialName("id")
    val id: Int,
    
    @SerialName("name")
    val name: String? = null,
    
    @SerialName("description")
    val description: String? = null,
    
    // Foreign keys
    @SerialName("owner_id")
    val ownerId: Int? = null,
    
    @SerialName("created_by_id")
    val createdById: Int? = null,
    
    // Nested objects
    @SerialName("owner")
    val owner: OwnerDto? = null,
    
    // Date/Time fields (ISO 8601 from API)
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    
    @SerialName("start_time")
    val startTime: String? = null,
    
    @SerialName("planned_start")
    val plannedStart: String? = null,
    
    @SerialName("planned_end")
    val plannedEnd: String? = null,
    
    // Timestamps
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("updated_at")
    val updatedAt: String? = null,
    
    // Status/State
    @SerialName("state")
    val state: String? = null,
    
    @SerialName("state_label")
    val stateLabel: String? = null,
    
    @SerialName("is_active")
    val isActive: Boolean? = null,
    
    // Numeric fields
    @SerialName("amount")
    val amount: Double? = null,
    
    @SerialName("total_cost")
    val totalCost: Double? = null,
    
    // Display info (calculated by backend)
    @SerialName("display_info")
    val displayInfo: DisplayInfoDto? = null
)
```

### List Response DTO
```kotlin
/**
 * API response for list of {Entity}s.
 * Includes pagination info.
 */
@Serializable
data class {Entity}ListResponseDto(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("data")
    val data: List<{Entity}Dto>? = null,
    
    // Pagination
    @SerialName("page")
    val page: Int? = null,
    
    @SerialName("per_page")
    val perPage: Int? = null,
    
    @SerialName("total")
    val total: Int? = null,
    
    @SerialName("total_pages")
    val totalPages: Int? = null
)
```

### Single Entity Response DTO
```kotlin
/**
 * API response for single {Entity}.
 */
@Serializable
data class {Entity}ResponseDto(
    @SerialName("success")
    val success: Boolean,
    
    @SerialName("message")
    val message: String,
    
    @SerialName("data")
    val data: {Entity}Dto? = null
)
```

### Create/Update Request DTO
```kotlin
/**
 * Request body for creating/updating {Entity}.
 * 
 * IMPORTANT: Date format must be DD-MM-YYYY for API v2.
 * Time format must be HH:MM (24-hour).
 */
@Serializable
data class Create{Entity}Request(
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    // Foreign key references
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    
    @SerialName("driver_id")
    val driverId: Int? = null,
    
    // Date in DD-MM-YYYY format for API v2
    @SerialName("date")
    val date: String? = null,
    
    // Time in HH:MM format
    @SerialName("time")
    val time: String? = null,
    
    // For trip scheduling - use planned_start/planned_end with ISO format
    @SerialName("planned_start")
    val plannedStart: String? = null,
    
    @SerialName("planned_end")
    val plannedEnd: String? = null,
    
    @SerialName("amount")
    val amount: Double? = null
)
```

### Nested DTOs
```kotlin
/**
 * Nested owner/user info in responses.
 */
@Serializable
data class OwnerDto(
    @SerialName("id")
    val id: Int,
    
    @SerialName("first_name")
    val firstName: String? = null,
    
    @SerialName("last_name")
    val lastName: String? = null,
    
    @SerialName("email")
    val email: String? = null,
    
    @SerialName("mobile")
    val mobile: String? = null,
    
    @SerialName("role")
    val role: String? = null
)

/**
 * Display info calculated by backend.
 */
@Serializable
data class DisplayInfoDto(
    @SerialName("display_value")
    val displayValue: String? = null,
    
    @SerialName("display_label")
    val displayLabel: String? = null,
    
    @SerialName("distance_display")
    val distanceDisplay: String? = null,
    
    @SerialName("duration_display")
    val durationDisplay: String? = null,
    
    @SerialName("cost_display")
    val costDisplay: String? = null
)
```

---

## Mapper Template

```kotlin
package com.indusjs.fleet.data.mapper.{feature}

import com.indusjs.fleet.data.mapper.Mapper
import com.indusjs.fleet.data.mapper.mapToDomainList
import com.indusjs.fleet.data.model.{feature}.{Entity}Dto
import com.indusjs.fleet.data.model.{feature}.Create{Entity}Request
import com.indusjs.fleet.domain.entity.{feature}.{Entity}

/**
 * Mapper for converting between {Entity}Dto and {Entity} domain entity.
 */
object {Entity}Mapper : Mapper<{Entity}Dto, {Entity}> {

    /**
     * Maps API DTO to domain entity.
     * Handles null safety and default values.
     */
    override fun mapToDomain(data: {Entity}Dto): {Entity} = {Entity}(
        id = data.id.toString(),
        name = data.name ?: "",
        description = data.description ?: "",
        
        // Foreign key IDs
        ownerId = data.ownerId?.toString(),
        
        // Nested object mapping
        ownerName = data.owner?.let { "${it.firstName ?: ""} ${it.lastName ?: ""}".trim() },
        
        // Date/Time - keep as string, UI will format
        scheduledDate = data.scheduledDate,
        startTime = data.startTime,
        plannedStart = data.plannedStart,
        plannedEnd = data.plannedEnd,
        
        // Timestamps
        createdAt = data.createdAt ?: "",
        updatedAt = data.updatedAt ?: "",
        
        // Status
        state = data.state ?: "unknown",
        stateLabel = data.stateLabel ?: data.state?.replaceFirstChar { it.uppercase() } ?: "Unknown",
        isActive = data.isActive ?: true,
        
        // Numeric
        amount = data.amount ?: 0.0,
        totalCost = data.totalCost ?: 0.0,
        
        // Display info
        distanceDisplay = data.displayInfo?.distanceDisplay ?: "NA",
        durationDisplay = data.displayInfo?.durationDisplay ?: "NA"
    )

    /**
     * Maps domain entity to API DTO.
     * Used when sending data to API.
     */
    override fun mapToData(entity: {Entity}): {Entity}Dto = {Entity}Dto(
        id = entity.id.toIntOrNull() ?: 0,
        name = entity.name,
        description = entity.description,
        ownerId = entity.ownerId?.toIntOrNull(),
        state = entity.state,
        amount = entity.amount.takeIf { it > 0 },
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )

    /**
     * Creates API request from domain entity.
     * Handles date/time format conversion.
     */
    fun toCreateRequest(entity: {Entity}): Create{Entity}Request = Create{Entity}Request(
        name = entity.name,
        description = entity.description.takeIf { it.isNotBlank() },
        vehicleId = entity.vehicleId?.toIntOrNull(),
        driverId = entity.driverId?.toIntOrNull(),
        
        // Date conversion: domain uses DD-MM-YYYY, API expects DD-MM-YYYY for v2
        date = entity.date,
        time = entity.time,
        
        // For planned times - convert to ISO if needed
        plannedStart = entity.plannedStart,
        plannedEnd = entity.plannedEnd,
        
        amount = entity.amount.takeIf { it > 0 }
    )
}

/**
 * Extension function to map list of DTOs.
 */
fun List<{Entity}Dto>.toDomainList(): List<{Entity}> = 
    {Entity}Mapper.mapToDomainList(this)
```

---

## Date/Time Conversion Utilities

```kotlin
/**
 * Convert DD-MM-YYYY + HH:MM to ISO 8601 for API.
 */
fun toIsoDateTime(date: String, time: String): String {
    // date = "04-01-2026" (DD-MM-YYYY)
    // time = "14:30" (HH:MM)
    val parts = date.split("-")
    if (parts.size != 3) return ""
    val (day, month, year) = parts
    return "${year}-${month}-${day}T${time}:00Z"
}

/**
 * Convert ISO 8601 to DD-MM-YYYY for UI display.
 */
fun fromIsoToDisplayDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    // isoDate = "2026-01-04T14:30:00Z"
    val datePart = isoDate.substringBefore("T")
    val parts = datePart.split("-")
    if (parts.size != 3) return ""
    val (year, month, day) = parts
    return "$day-$month-$year"
}

/**
 * Extract HH:MM from ISO 8601.
 */
fun fromIsoToDisplayTime(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    // isoDate = "2026-01-04T14:30:00Z"
    val timePart = isoDate.substringAfter("T").substringBefore("Z")
    return timePart.take(5)  // "14:30"
}
```

---

## Validation Checklist

- [ ] All fields have `@SerialName("snake_case")`
- [ ] All nullable fields have default `= null`
- [ ] Response DTOs include `success` and `message` fields
- [ ] List responses include pagination fields
- [ ] Nested objects have their own DTO classes
- [ ] Mapper handles null safety with `?: ""` or `?: 0`
- [ ] Date conversion utilities used for API requests
- [ ] Request DTOs only include writable fields

