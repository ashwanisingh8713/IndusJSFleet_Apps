# 11. Reports & Profit/Loss API

> **Package:** `com.indusjs.fleet.data.datasource.reports`  
> **Auth Required:** Yes (Bearer token)  
> **Role Restriction:** Owner and General Manager only  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### Single Entity P&L

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/trips/{id}/profit-loss` | Single trip P&L |
| `GET` | `/vehicles/{id}/profit-loss` | Single vehicle P&L |
| `GET` | `/reports/profit-loss` | Fleet-wide P&L |

### Multi-Entity P&L

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/reports/profit-loss/vehicles` | Multi-vehicle P&L |
| `POST` | `/reports/profit-loss/trips` | Multi-trip P&L |

### Cost Analysis

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/reports/profit-loss/cost-type/{type}` | Single cost type analysis |
| `POST` | `/reports/profit-loss/cost-types` | Multi cost type analysis |

### Consolidated & Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/reports/profit-loss/consolidated` | Consolidated P&L statement |
| `GET` | `/reports/profit-loss/summary` | P&L summary with alerts |

---

## Data Source

### `ReportsRemoteDataSource`

> **Note:** Concrete class, not interface + impl.

```kotlin
@Inject
class ReportsRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json
) {
    // Single entity
    suspend fun getTripProfitLoss(token: String, tripId: Int): TripProfitLossDto?
    suspend fun getVehicleProfitLoss(token: String, vehicleId: Int, period: String = "monthly"): VehicleProfitLossDto?
    suspend fun getFleetProfitLoss(token: String, period: String?, startDate: String?, endDate: String?): FleetProfitLossDto?

    // Multi-entity
    suspend fun getMultiVehicleProfitLoss(token: String, request: MultiVehiclePLRequest): List<VehicleProfitLossDto>?
    suspend fun getMultiTripProfitLoss(token: String, request: MultiTripPLRequest): List<TripProfitLossDto>?

    // Cost analysis
    suspend fun getCostTypeAnalysis(token: String, costType: String, startDate: String?, endDate: String?): CostTypeAnalysisDto?
    suspend fun getMultiCostTypeAnalysis(token: String, request: MultiCostTypePLRequest): List<CostTypeAnalysisDto>?

    // Consolidated
    suspend fun getConsolidatedPL(token: String, request: ConsolidatedPLRequest): ConsolidatedPLDto?
    suspend fun getPLSummary(token: String, startDate: String?, endDate: String?): PLSummaryDto?
}
```

---

## Query Parameters

### Vehicle P&L

| Parameter | Type | Description |
|-----------|------|-------------|
| `period` | `String` | `today`, `weekly`, `monthly`, `quarterly`, `yearly` |

### Fleet P&L

| Parameter | Type | Description |
|-----------|------|-------------|
| `period` | `String?` | Named period |
| `start_date` | `String?` | Custom start (DD-MM-YYYY) |
| `end_date` | `String?` | Custom end (DD-MM-YYYY) |

### Cost Type Analysis

| Parameter | Type | Description |
|-----------|------|-------------|
| `start_date` | `String?` | Filter start date |
| `end_date` | `String?` | Filter end date |

### P&L Summary

| Parameter | Type | Description |
|-----------|------|-------------|
| `start_date` | `String?` | Filter start date |
| `end_date` | `String?` | Filter end date |

---

## Request DTOs

### `MultiVehiclePLRequest`

| Field | JSON Key | Type |
|-------|----------|------|
| `vehicleIds` | `vehicle_ids` | `List<Int>` |
| `period` | `period` | `String?` |
| `startDate` | `start_date` | `String?` |
| `endDate` | `end_date` | `String?` |

### `MultiTripPLRequest`

| Field | JSON Key | Type |
|-------|----------|------|
| `tripIds` | `trip_ids` | `List<Int>?` |
| `vehicleId` | `vehicle_id` | `Int?` |
| `startDate` | `start_date` | `String?` |
| `endDate` | `end_date` | `String?` |

### `MultiCostTypePLRequest`

| Field | JSON Key | Type |
|-------|----------|------|
| `costTypes` | `cost_types` | `List<String>` |
| `startDate` | `start_date` | `String?` |
| `endDate` | `end_date` | `String?` |

### `ConsolidatedPLRequest`

| Field | JSON Key | Type |
|-------|----------|------|
| `period` | `period` | `String?` |
| `startDate` | `start_date` | `String?` |
| `endDate` | `end_date` | `String?` |

---

## Response DTOs

### `ProfitLossResponse<T>` (Generic Wrapper)

| Field | Type | Description |
|-------|------|-------------|
| `success` | `Boolean` | Request success |
| `message` | `String?` | Server message |
| `data` | `T?` | P&L data payload |

### `TripProfitLossDto`

Key fields: `tripId`, `vehicleNumber`, `revenue`, `totalExpenses`, `profit`, `profitMargin`, cost breakdowns.

### `VehicleProfitLossDto`

Key fields: `vehicleId`, `registrationNumber`, `totalRevenue`, `totalExpenses`, `netProfit`, `profitMargin`, trip count, cost breakdowns.

### `FleetProfitLossDto`

Fleet-wide aggregate: total revenue, total expenses, net profit, vehicle count, trip count.

### `CostTypeAnalysisDto`

Cost type breakdown: cost type ID/label, total amount, count, average, percentage of total.

### `ConsolidatedPLDto`

Comprehensive statement: revenue, expense categories, gross/net profit, vehicle-wise breakdown.

### `PLSummaryDto`

Dashboard-oriented summary with `overview` (totalRevenue, totalExpenses) and `period` (startDate, endDate).

---

## Report Periods

```
today, weekly, 15days, monthly, quarterly, half_yearly, yearly, custom
```

---

## Source Files

| File | Path |
|------|------|
| ReportsRemoteDataSource | `data/datasource/reports/ReportsRemoteDataSource.kt` |
| Response DTOs | `data/model/reports/ProfitLossDto.kt` |
| Request DTOs | `data/model/reports/ProfitLossRequest.kt` |

