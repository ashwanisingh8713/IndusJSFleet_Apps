# ijs-finance-lib — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.fleet.domain.entity.finance`, `com.indusjs.fleet.data.*.finance`

---

## 1. Purpose

Vehicle finance (purchase, loan, EMI) domain and data layer. Tracks vehicle purchase records, loan details, EMI payment history, upcoming/overdue alerts.

## 2. Package Structure

```
src/commonMain/kotlin/com/indusjs/fleet/
├── domain/
│   ├── entity/finance/
│   │   ├── VehiclePurchase.kt   # Purchase info, PaymentType, LoanStatus enums
│   │   └── LoanPayment.kt      # EMI payment, EntryType, PaymentMode, PaymentStatus
│   └── repository/finance/
│       └── VehicleFinanceRepository.kt  # Purchase CRUD, loan payments, alerts
├── data/
│   ├── datasource/finance/
│   │   └── VehicleFinanceRemoteDataSource.kt  # All finance API calls
│   ├── model/finance/
│   │   └── VehicleFinanceDto.kt  # Purchase, loan payment, EMI DTOs
│   ├── mapper/finance/
│   │   └── VehicleFinanceMapper.kt  # DTO ↔ Domain
│   └── repository/finance/
│       └── VehicleFinanceRepositoryImpl.kt
```

## 3. API Endpoints Used

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/vehicles/{id}/purchase` | Get purchase info |
| POST | `/vehicles/{id}/purchase` | Add purchase info |
| PUT | `/vehicles/{id}/purchase` | Update purchase info |
| GET | `/vehicles/{id}/loan-summary` | Get loan summary |
| GET | `/vehicles/{id}/loan-payments` | Get loan payment history |
| GET | `/vehicle-loan-payments` | List all loan payments |
| GET | `/vehicle-loan-payments/{id}` | Get payment detail |
| POST | `/vehicle-loan-payments` | Add loan payment |
| PUT | `/vehicle-loan-payments/{id}` | Update payment |
| POST | `/vehicle-loan-payments/{id}/pay` | Mark EMI paid |
| DELETE | `/vehicle-loan-payments/{id}` | Delete payment |
| GET | `/vehicle-loan-payments/upcoming` | Upcoming EMIs |
| GET | `/vehicle-loan-payments/overdue` | Overdue EMIs |

## 4. Dependencies

- `ijs-network-lib` (api) — HttpClient, ApiConfig, AuthTokenHelper, UserLocalDataSource

