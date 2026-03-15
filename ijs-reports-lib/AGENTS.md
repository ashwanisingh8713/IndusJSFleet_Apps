# ijs-reports-lib — AGENTS.md

## Purpose
Profit & Loss reporting domain and data layer for IndusJS Fleet. Provides **vehicle P&L, trip P&L, cost analysis, consolidated reports, and P&L summaries**.

## What's Inside

| Package | Content |
|---------|---------|
| `domain.entity.reports` | `TripProfitLoss`, `VehicleProfitLoss`, `FleetProfitLoss`, `CostTypeAnalysis`, `ConsolidatedPL`, `PLSummary`, `VehiclePerformer`, `PLAlert` |
| `domain.repository.reports` | `ReportsRepository` interface |
| `data.model.reports` | `ProfitLossDto.kt` (all P&L DTOs), `ProfitLossRequest.kt` (request DTOs) |
| `data.mapper.reports` | `ProfitLossMapper.kt` (placeholder) |
| `data.datasource.reports` | `ReportsRemoteDataSource` (10 API methods) |
| `data.repository.reports` | `ReportsRepositoryImpl` + DTO→Domain extension mappers |

## API Endpoints Used
- `GET /trips/{id}/profit-loss`
- `GET /vehicles/{id}/profit-loss`
- `GET /reports/profit-loss`
- `POST /reports/profit-loss/vehicles`
- `POST /reports/profit-loss/trips`
- `GET /reports/profit-loss/cost-type/{type}`
- `POST /reports/profit-loss/cost-types`
- `POST /reports/profit-loss/consolidated`
- `GET /reports/profit-loss/summary`

## Dependencies
- `ijs-network-lib` (api) — HttpClient, ApiConfig, AuthTokenHelper, UserLocalDataSource

