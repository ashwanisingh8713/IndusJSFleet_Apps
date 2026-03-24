# 07. Dashboard API

> **Package:** `com.indusjs.fleet.data.datasource.dashboard`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

| Method | Endpoint | Description | Role Restriction |
|--------|----------|-------------|------------------|
| `GET` | `/dashboard` | Unified dashboard (auto-detects role) | All |
| `GET` | `/dashboard/cost-overview` | Cost overview with filter | All |
| `GET` | `/dashboard/pending-payments` | Pending payments list | Owner, GM |
| `GET` | `/dashboard/alerts-status` | Alerts with counts by type/priority | All |
| `GET` | `/dashboard/financial-summary` | Financial KPIs | Owner, GM only |

---

## Data Source Interface

### `DashboardRemoteDataSource`

```kotlin
interface DashboardRemoteDataSource : RemoteDataSource {
    suspend fun getDashboard(token: String): DashboardApiResponse
    suspend fun getCostOverview(token: String, filter: CostOverviewFilter): CostOverviewApiResponse
    suspend fun getPendingPayments(token: String, page: Int = 1, perPage: Int = 20): PendingPaymentsApiResponse
    suspend fun getAlertsStatus(token: String): AlertsStatusApiResponse
    suspend fun getFinancialSummary(token: String, period: FinancialPeriod): FinancialSummaryApiResponse
}
```

**Implementation:** `DashboardRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

### `DashboardLocalDataSource`

Caches dashboard data locally for offline access.

---

## Filter Enums

### `CostOverviewFilter`

```kotlin
enum class CostOverviewFilter(val value: String) {
    TODAY("today"),
    WEEKLY("weekly"),
    MONTHLY("monthly")
}
```

### `FinancialPeriod`

```kotlin
enum class FinancialPeriod(val value: String) {
    TODAY("today"),
    WEEKLY("weekly"),
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    YEARLY("yearly")
}
```

---

## Response DTOs

### `DashboardApiResponse`

Top-level response wrapping `DashboardDataDto`.

### `DashboardDataDto`

| Field | JSON Key | Type | Description |
|-------|----------|------|-------------|
| `userInfo` | `user_info` | `UserInfoDto` | Logged-in user info |
| `fleetOverview` | `fleet_overview` | `FleetOverviewDto` | Vehicle/driver/trip counts |
| `todaySummary` | `today_summary` | `TodaySummaryDto` | Today's operational summary |
| `alerts` | `alerts` | `List<AlertDto>` | Active alerts |
| `totalAlerts` | `total_alerts` | `Int` | Total alert count |
| `quickActions` | `quick_actions` | `QuickActionsDto` | Available quick actions |
| `liveStatus` | `live_status` | `LiveStatusDto` | Real-time fleet status |
| `teamStats` | `team_stats` | `TeamStatsDto?` | Team statistics (Owner/GM) |
| `documentStats` | `document_stats` | `DocumentStatsDto?` | Document expiry stats |
| `lastUpdated` | `last_updated` | `String?` | Last data refresh |

### `FleetOverviewDto`

| Field | JSON Key | Type |
|-------|----------|------|
| `totalVehicles` | `total_vehicles` | `Int` |
| `activeVehicles` | `active_vehicles` | `Int` |
| `maintenanceVehicles` | `maintenance_vehicles` | `Int` |
| `inactiveVehicles` | `inactive_vehicles` | `Int` |
| `totalDrivers` | `total_drivers` | `Int` |
| `activeDrivers` | `active_drivers` | `Int` |
| `driversOnTrip` | `drivers_on_trip` | `Int` |
| `driversOnLeave` | `drivers_on_leave` | `Int` |
| `totalTrips` | `total_trips` | `Int` |
| `ongoingTrips` | `ongoing_trips` | `Int` |

### `AlertDto`

| Field | Type | Description |
|-------|------|-------------|
| `type` | `String` | Alert category (e.g., `document_expiry`, `license_expiry`) |
| `priority` | `String` | `high`, `medium`, `low` |
| `message` | `String` | Alert description |
| `entityId` | `Int?` | Related entity ID |
| `entityType` | `String?` | `vehicle`, `driver`, etc. |

---

## Source Files

| File | Path |
|------|------|
| DashboardRemoteDataSource | `data/datasource/dashboard/DashboardRemoteDataSource.kt` |
| DashboardLocalDataSource | `data/datasource/dashboard/DashboardLocalDataSource.kt` |
| DashboardModels | `data/model/dashboard/DashboardModels.kt` |

