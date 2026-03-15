# ijs-finance-lib — AGENTS.md

## Purpose
Vehicle finance (purchase, loan, EMI) domain and data layer for IndusJS Fleet.

## What's Inside

| Package | Content |
|---------|---------|
| `domain.entity.finance` | `VehiclePurchase`, `LoanPayment`, `LoanSummary`, `EmiAlert`, `PaymentType`, `PaymentMode`, `LoanStatus`, `PaymentStatus`, `VehicleBasicInfo` |
| `domain.repository.finance` | `VehicleFinanceRepository` interface |
| `data.model.finance` | `VehicleFinanceDto.kt` (purchase, loan payment, EMI DTOs) |
| `data.mapper.finance` | `VehicleFinanceMapper.kt` (DTO ↔ Domain) |
| `data.datasource.finance` | `VehicleFinanceRemoteDataSource` (purchase CRUD, loan payments, EMI alerts) |
| `data.repository.finance` | `VehicleFinanceRepositoryImpl` |

## API Endpoints Used
- `POST /vehicle-purchases` — Add purchase info
- `GET /vehicle-purchases/{id}` — Get purchase detail
- `PUT /vehicle-purchases/{id}` — Update purchase info
- `GET /vehicle-purchases` — List all purchases
- `POST /vehicle-purchases/{id}/loan-payments` — Add loan payment
- `GET /vehicle-purchases/{id}/loan-payments` — Payment history
- `PUT /loan-payments/{id}` — Update payment
- `PATCH /loan-payments/{id}/mark-paid` — Mark EMI paid
- `DELETE /loan-payments/{id}` — Delete payment
- `GET /vehicle-purchases/upcoming-emis` — EMI alerts

## Dependencies
- `ijs-network-lib` (api) — HttpClient, ApiConfig, AuthTokenHelper, UserLocalDataSource

