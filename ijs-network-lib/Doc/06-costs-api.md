# 06. Costs API

> **Package:** `com.indusjs.fleet.data.datasource.costs`  
> **Auth Required:** Mixed (cost types = No, cost CRUD = Yes)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### Cost Types (Cached Locally)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/cost-types/trip` | Get trip cost type definitions | ❌ |
| `GET` | `/cost-types/maintenance` | Get maintenance cost type definitions | ❌ |
| `GET` | `/cost-types/driver` | Get driver cost type definitions | ❌ |

### Trip Costs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/trip-costs` | Create single trip cost |
| `POST` | `/trips/{id}/costs/bulk` | Bulk create trip costs |
| `GET` | `/trips/{id}/costs` | List trip costs |
| `GET` | `/trips/{id}/costs/summary` | Get trip cost summary |
| `DELETE` | `/trip-costs/{id}` | Delete a trip cost |

### Maintenance Costs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/maintenance-costs` | Create single maintenance cost |
| `POST` | `/vehicles/{id}/maintenance-costs/bulk` | Bulk create maintenance costs |
| `GET` | `/vehicles/{id}/maintenance-costs` | List vehicle maintenance costs |
| `DELETE` | `/maintenance-costs/{id}` | Delete a maintenance cost |

### Vehicle Cost Queries (Paginated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles/{id}/trip-costs` | Vehicle trip costs (paginated, filtered) |
| `GET` | `/vehicles/{id}/maintenance-costs` | Vehicle maintenance costs (paginated, filtered) |

### Driver Costs

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/drivers/{id}/costs` | Create single driver cost |
| `POST` | `/drivers/{id}/costs/bulk` | Bulk create driver costs |
| `GET` | `/drivers/{id}/costs` | List driver costs (paginated, filtered) |

---

## Data Source Interface

### `CostsRemoteDataSource`

```kotlin
interface CostsRemoteDataSource : RemoteDataSource {
    // Cost Types (no auth required)
    suspend fun getTripCostTypes(): CostTypesApiResponse
    suspend fun getMaintenanceCostTypes(): CostTypesApiResponse
    suspend fun getDriverCostTypes(): CostTypesApiResponse

    // Trip Costs
    suspend fun createTripCost(token: String, request: CreateTripCostRequest): TripCostApiResponse
    suspend fun bulkCreateTripCosts(token: String, tripId: String, request: BulkCreateTripCostsRequest): BulkTripCostsApiResponse
    suspend fun getTripCosts(token: String, tripId: String): TripCostsListApiResponse
    suspend fun getTripCostSummary(token: String, tripId: String): TripCostSummaryApiResponse
    suspend fun deleteTripCost(token: String, costId: String): DeleteCostApiResponse

    // Maintenance Costs
    suspend fun createMaintenanceCost(token: String, request: CreateMaintenanceCostRequest): MaintenanceCostApiResponse
    suspend fun bulkCreateMaintenanceCosts(token: String, vehicleId: String, request: BulkCreateMaintenanceCostsRequest): BulkMaintenanceCostsApiResponse
    suspend fun getMaintenanceCosts(token: String, vehicleId: String): MaintenanceCostsListApiResponse
    suspend fun deleteMaintenanceCost(token: String, costId: String): DeleteCostApiResponse

    // Vehicle Cost Queries
    suspend fun getVehicleTripCosts(token: String, vehicleId: String, page: Int, perPage: Int, costType: String?, startDate: String?, endDate: String?, sortBy: String, sortOrder: String): VehicleTripCostsApiResponse
    suspend fun getVehicleMaintenanceCosts(token: String, vehicleId: String, page: Int, perPage: Int, costType: String?, startDate: String?, endDate: String?, sortBy: String, sortOrder: String): VehicleMaintenanceCostsApiResponse

    // Driver Costs
    suspend fun getDriverCosts(token: String, driverId: String, page: Int, perPage: Int, groupId: String?, month: String?, startDate: String?, endDate: String?): DriverCostsListApiResponse
    suspend fun createDriverCost(token: String, driverId: String, request: CreateDriverCostRequest): DriverCostApiResponse
    suspend fun bulkCreateDriverCosts(token: String, driverId: String, request: BulkCreateDriverCostsRequest): BulkDriverCostsApiResponse
}
```

**Implementation:** `CostsRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

---

## Local Data Source

### `CostsLocalDataSource`

Caches cost type definitions locally (populated on app launch).

---

## Cost Type Structure

All cost types follow a hierarchical structure:

### `CostTypeGroupDto`

| Field | JSON Key | Type |
|-------|----------|------|
| `groupId` | `group_id` | `String` |
| `groupName` | `group_name` | `String` |
| `items` | `items` | `List<CostTypeItemDto>` |

### `CostTypeItemDto`

| Field | JSON Key | Type |
|-------|----------|------|
| `id` | `id` | `String` |
| `value` | `value` | `String` |
| `label` | `label` | `String` |

### Trip Cost Groups (Fallback)

| Group ID | Group Name | Example Items |
|----------|------------|---------------|
| `TC-G-001` | Fuel & Energy | Petrol, Diesel, CNG/LPG, EV Charging |
| `TC-G-002` | Toll & Parking | Toll Charges, Parking Fees, Entry Charges |
| `TC-G-003` | Loading & Unloading | Loading, Unloading, Crane/Forklift, Labor |
| `TC-G-004` | Driver Expenses | Allowance, Food, Accommodation |
| `TC-G-005` | Permits & Compliance | State Permit, National Permit, Chalan/Fine |
| `TC-G-006` | Miscellaneous | Police/RTO, Weighbridge, Commission, Other |

### Maintenance Cost Groups (Fallback)

| Group ID | Group Name |
|----------|------------|
| `MC-G-001` | Engine & Mechanical |
| `MC-G-002` | Body & Exterior |
| `MC-G-003` | Tyres & Wheels |
| `MC-G-004` | Electrical & Electronics |
| `MC-G-005` | Fuel & Fluids |
| `MC-G-006` | Routine Service |

### Driver Cost Groups (Fallback)

| Group ID | Group Name |
|----------|------------|
| `DC-G-001` | Salary & Wages |
| `DC-G-002` | Incentives & Bonuses |
| `DC-G-003` | Deductions |
| `DC-G-004` | Other |

---

## Key Request DTOs

### `CreateTripCostRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `tripId` | `trip_id` | `Int` | ✅ |
| `costType` | `cost_type` | `String` | ✅ |
| `amount` | `amount` | `Double` | ✅ |
| `date` | `date` | `String` | ✅ |
| `time` | `time` | `String?` | ❌ |
| `notes` | `notes` | `String?` | ❌ |

### `BulkCreateTripCostsRequest`

| Field | JSON Key | Type |
|-------|----------|------|
| `costs` | `costs` | `List<BulkCostItem>` |

### `BulkCostItem`

| Field | JSON Key | Type |
|-------|----------|------|
| `costId` | `cost_id` | `String` |
| `costLabel` | `cost_label` | `String` |
| `groupId` | `group_id` | `String` |
| `amount` | `amount` | `Double` |
| `dateTime` | `date_time` | `String` |
| `notes` | `notes` | `String?` |

---

## Date Format

> **Individual costs:** `DD-MM-YYYY` date + `HH:MM` time (sent as-is)  
> **Bulk costs:** ISO 8601 `date_time` field (`2026-12-20T10:30:00Z`)

---

## Vehicle Cost Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | `Int` | Page number |
| `per_page` | `Int` | Items per page |
| `cost_type` | `String?` | Filter by cost type ID |
| `start_date` | `String?` | Filter start date |
| `end_date` | `String?` | Filter end date |
| `sort_by` | `String` | Sort field (default: `date`) |
| `sort_order` | `String` | `asc` or `desc` |

---

## Source Files

| File | Path |
|------|------|
| CostsRemoteDataSource | `data/datasource/costs/CostsRemoteDataSource.kt` |
| CostsLocalDataSource | `data/datasource/costs/CostsLocalDataSource.kt` |
| CostModels | `data/model/costs/CostModels.kt` |
| DriverCostModels | `data/model/driver/DriverCostModels.kt` |

