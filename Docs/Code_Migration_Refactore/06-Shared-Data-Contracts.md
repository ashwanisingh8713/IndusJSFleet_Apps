# 06 — Shared Data Contracts

## Purpose

Define lightweight cross-feature data models in `ijs-core-lib` for communication between feature modules via `ExternalDeps` interfaces. These replace direct imports of feature-specific domain entities.

---

## Location

```
ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/core/model/shared/
├── SelectableVehicle.kt
├── SelectableDriver.kt
├── SelectableCustomer.kt
├── SelectableTrip.kt
└── CaretakerInfo.kt
```

---

## Data Classes

### SelectableVehicle

Used by: `TripExternalDeps`, `FinanceExternalDeps`, `ReportsExternalDeps`

```kotlin
package com.indusjs.fleet.core.model.shared

/**
 * Lightweight vehicle representation for cross-feature selection/display.
 * Used in feature modules that need vehicle lists without depending on ijs-vehicle-lib.
 */
data class SelectableVehicle(
    val id: String,
    val registrationNumber: String,
    val displayName: String,       // e.g., "MH12AB1234 - Tata Ace"
    val status: String,            // Raw status string (e.g., "active", "on_route")
    val vehicleType: String = ""   // e.g., "truck", "van"
)
```

### SelectableDriver

Used by: `VehicleExternalDeps`, `TripExternalDeps`

```kotlin
package com.indusjs.fleet.core.model.shared

/**
 * Lightweight driver representation for cross-feature selection/display.
 * Used in feature modules that need driver lists without depending on ijs-driver-lib.
 */
data class SelectableDriver(
    val id: String,
    val name: String,              // Full name
    val mobile: String,
    val status: String,            // Raw status string (e.g., "active", "on_route")
    val licenseNumber: String = ""
)
```

### SelectableCustomer

Used by: `TripExternalDeps`, UI components (`CustomerSelectionBottomSheet`)

```kotlin
package com.indusjs.fleet.core.model.shared

/**
 * Lightweight customer representation for cross-feature selection/display.
 * Used in feature modules that need customer lists without depending on ijs-customer-lib.
 */
data class SelectableCustomer(
    val id: String,
    val companyName: String,
    val personName: String,
    val primaryContact: String,
    val email: String = "",
    val gstNumber: String = ""
)
```

### SelectableTrip

Used by: `PaymentExternalDeps`, `ReportsExternalDeps`

```kotlin
package com.indusjs.fleet.core.model.shared

/**
 * Lightweight trip representation for cross-feature selection/display.
 * Used in feature modules that need trip lists without depending on ijs-trip-lib.
 */
data class SelectableTrip(
    val id: String,
    val routeLabel: String,        // e.g., "Mumbai → Pune"
    val vehicleInfo: String,       // e.g., "MH12AB1234"
    val driverName: String = "",
    val status: String,            // Raw status string
    val scheduledDate: String = "",
    val tripPrice: Double = 0.0
)
```

### CaretakerInfo

Used by: `VehicleExternalDeps`, `DriverExternalDeps`, UI components (`CaretakerComponents`)

```kotlin
package com.indusjs.fleet.core.model.shared

/**
 * Lightweight team member representation for caretaker selection.
 * Used in feature modules that need team member lists without depending on ijs-team-lib.
 */
data class CaretakerInfo(
    val id: String,
    val name: String,              // Full name
    val role: String,              // e.g., "manager", "supervisor"
    val mobile: String = "",
    val email: String = ""
)
```

---

## DTOs to Consolidate into ijs-core-lib

These DTOs are currently duplicated or in wrong modules and need consolidation:

### CostBreakdownItemDto

**Current locations:**
- `ijs-network-lib` → `data/model/dashboard/DashboardModels.kt` (simple version: costType, amount, count)
- `ijs-reports-lib` → `data/model/reports/ProfitLossDto.kt` (rich version: costId, costLabel, groupId, percentage)

**Action:** Keep the **rich version** (from reports-lib) in `ijs-core-lib/data/model/costs/`, make fields optional for backward compatibility. Remove duplicates.

```kotlin
// Move to: ijs-core-lib/src/commonMain/.../data/model/costs/CostBreakdownItemDto.kt
@Serializable
data class CostBreakdownItemDto(
    @SerialName("cost_id") val costId: String? = null,
    @SerialName("cost_label") val costLabel: String? = null,
    @SerialName("group_id") val groupId: String? = null,
    @SerialName("cost_type") val costType: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("count") val count: Int = 0,
    @SerialName("percentage") val percentage: Double = 0.0
) : Dto {
    val displayLabel: String
        get() = costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }
}
```

### CostTypeSelection

**Current location:** `sharedUI/core/ui/CostTypeChipSelector.kt`

**Action:** Move to `ijs-core-lib/data/model/costs/` (depends only on group IDs — no UI dependency).

```kotlin
// Move to: ijs-core-lib/src/commonMain/.../data/model/costs/CostTypeSelection.kt
data class CostTypeSelection(
    val costTypeId: String,
    val costTypeLabel: String,
    val groupId: String,
    val groupName: String
) {
    companion object {
        const val FUEL_ENERGY_GROUP_ID = "TC-G-001"
        const val MISCELLANEOUS_GROUP_ID = "TC-G-006"
        const val OTHER_COST_TYPE_ID = "TC-006-004"
    }
    val isFuelCategory: Boolean get() = groupId == FUEL_ENERGY_GROUP_ID
    val isOtherCostType: Boolean get() = costTypeId == OTHER_COST_TYPE_ID
}
```

---

## Already in ijs-core-lib (No Action Needed)

These shared DTOs are already correctly placed:

| DTO | Location in ijs-core-lib |
|-----|-------------------------|
| `TripCostDto` | `data/model/costs/CostEntityModels.kt` |
| `MaintenanceCostDto` | `data/model/costs/CostEntityModels.kt` |
| `HistoryItemDto` | `data/model/history/HistoryDto.kt` |
| `CostTypeGroupDto` | `data/model/costs/CostModels.kt` |
| `CostTypeItemDto` | `data/model/costs/CostModels.kt` |
| `CostTypeCategoryDto` | `data/model/costs/CostModels.kt` |
| `Dto` (marker interface) | `data/model/DataModels.kt` |
| `StatusConstants` | `core/constants/StatusConstants.kt` |
| `ValidationUtils` | `core/util/ValidationUtils.kt` |
| `FormatUtils` | `core/util/FormatUtils.kt` |

---

## Mapping from Feature Entity → Shared Contract

When `sharedUI` creates `ExternalDeps` adapters, it maps feature entities to shared contracts:

```kotlin
// Vehicle entity → SelectableVehicle
fun Vehicle.toSelectable() = SelectableVehicle(
    id = id,
    registrationNumber = registrationNumber,
    displayName = "$registrationNumber - $make $model",
    status = status.name,
    vehicleType = vehicleType.name
)

// Driver entity → SelectableDriver
fun Driver.toSelectable() = SelectableDriver(
    id = id,
    name = "$firstName $lastName",
    mobile = mobile,
    status = status.name,
    licenseNumber = licenseNumber
)

// Customer entity → SelectableCustomer
fun Customer.toSelectable() = SelectableCustomer(
    id = id,
    companyName = companyName,
    personName = personName,
    primaryContact = primaryContact,
    email = email,
    gstNumber = gstNumber
)

// Trip entity → SelectableTrip
fun Trip.toSelectable() = SelectableTrip(
    id = id,
    routeLabel = "$startLocation → $endLocation",
    vehicleInfo = vehicleRegistrationNumber,
    driverName = driverName,
    status = status.name,
    scheduledDate = scheduledDate,
    tripPrice = tripPrice
)

// TeamMemberDto → CaretakerInfo
fun TeamMemberDto.toCaretakerInfo() = CaretakerInfo(
    id = id.toString(),
    name = "$firstName $lastName",
    role = role,
    mobile = mobile ?: "",
    email = email ?: ""
)
```

These mapping extension functions live in `sharedUI/di/adapters/` alongside the `ExternalDeps` adapter implementations.

