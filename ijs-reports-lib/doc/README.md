# ijs-reports-lib — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.fleet.domain.entity.reports`, `com.indusjs.fleet.data.*.reports`

---

## 1. Purpose

Profit & Loss reporting domain and data layer. Provides vehicle P&L, trip P&L, cost analysis by type, consolidated reports, and P&L summaries. Owner/GM role only.

## 2. Package Structure

```
src/commonMain/kotlin/com/indusjs/fleet/
├── domain/
│   ├── entity/reports/
│   │   └── ProfitLossEntities.kt  # TripProfitLoss, VehicleProfitLoss, FleetProfitLoss, etc.
│   └── repository/reports/
│       └── ReportsRepository.kt   # 8+ report methods
├── data/
│   ├── datasource/reports/
│   │   └── ReportsRemoteDataSource.kt  # 10 API methods
│   ├── model/reports/
│   │   ├── ProfitLossDto.kt       # All P&L response DTOs
│   │   └── ProfitLossRequest.kt   # Request DTOs with date ranges
│   ├── mapper/reports/
│   │   └── ProfitLossMapper.kt    # DTO → Domain conversion
│   └── repository/reports/
│       └── ReportsRepositoryImpl.kt
```

## 3. API Endpoints Used

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/trips/{id}/profit-loss` | Single trip P&L |
| GET | `/vehicles/{id}/profit-loss` | Single vehicle P&L |
| GET | `/reports/profit-loss` | Fleet-wide P&L |
| POST | `/reports/profit-loss/vehicles` | Multi-vehicle P&L with date range |
| POST | `/reports/profit-loss/trips` | Multi-trip P&L with date range |
| GET | `/reports/profit-loss/cost-type/{type}` | Cost breakdown by type |
| POST | `/reports/profit-loss/cost-types` | Multi-type cost analysis |
| POST | `/reports/profit-loss/consolidated` | Consolidated P&L statement |
| GET | `/reports/profit-loss/summary` | Dashboard P&L summary |

## 4. Dependencies

- `ijs-network-lib` (api) — HttpClient, ApiConfig, AuthTokenHelper, UserLocalDataSource

