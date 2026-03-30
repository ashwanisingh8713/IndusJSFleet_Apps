# screen-payment — IndusJS Fleet

## Purpose

Payment feature: trip payment recording, payment status tracking, payment history.

## Package: `com.ijs.payment`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| PaymentsScreen | `Payments` | All payments with filters |
| PaymentDetailScreen | `PaymentDetail(id)` | Payment details |
| AddPaymentScreen | `AddPayment(tripId?, vehicleId?)` | Record payment |
| EditPaymentScreen | `EditPayment(id)` | Update existing payment |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/PaymentFeatureFacade.kt` | Facade — 4 entry points |
| `presentation/Payments*.kt` | List Contract/VM/Screen |
| `presentation/AddPayment*.kt` | Add Contract/VM/Screen |
| `presentation/PaymentDetail*.kt` | Detail Contract/VM/Screen |
| `data/datasource/TripPaymentRemoteDataSource.kt` | API calls |
| `data/repository/TripPaymentRepositoryImpl.kt` | Repository impl |
| `domain/repository/TripPaymentRepository.kt` | Repository interface |
| `domain/repository/TripProviderForPayment.kt` | Cross-feature adapter interface |
| `presentation/TripSummaryForPayment.kt` | DTO for trip data needed by payment |

## Cross-Feature: Trip Data Access

Payment needs trip data but **cannot depend on screen-trip**. Solution:

1. `TripProviderForPayment` interface in `screen-payment/domain/repository/`
2. `TripProviderAdapter` in `sharedUI/di/adapter/` bridges `TripRepository` → interface
3. Adapter passed to `PaymentsViewModel` / `AddPaymentViewModel` via DI

## Payment Status: `received`, `pending`, `cancelled`
## Payment Modes: `cash`, `upi`, `bank_transfer`, `cheque`, `card`

## APIs

- `GET /payments` — List all
- `GET /payments/{id}` — Detail
- `POST /payments` — Create
- `PUT /payments/{id}` — Update

## Module Path

`screen-payment/src/commonMain/kotlin/com/ijs/payment/`

## Depends On: `ijs-network-lib`

