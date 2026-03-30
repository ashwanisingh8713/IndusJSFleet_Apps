# screen-finance — IndusJS Fleet

## Purpose

Vehicle finance: purchase records, loan tracking, EMI payment history.

## Package: `com.ijs.finance`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| VehicleFinanceListScreen | `VehicleFinance` | All vehicles with finance status |
| VehicleFinanceDetailScreen | `VehicleFinanceDetail(vehicleId)` | Purchase info, loan summary |
| AddPurchaseInfoScreen | `AddPurchaseInfo` | Record purchase (cash/loan) |
| EditPurchaseInfoScreen | `EditPurchaseInfo(vehicleId)` | Update purchase info |
| EmiPaymentHistoryScreen | `EmiPaymentHistory(vehicleId)` | EMI payments list |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/FinanceFeatureFacade.kt` | Facade — 5 entry points |
| `presentation/VehicleFinance*.kt` | Shared Contract/VM for finance flow |
| `data/datasource/VehicleFinanceRemoteDataSourceImpl.kt` | API calls |
| `data/repository/VehicleFinanceRepositoryImpl.kt` | Repository impl |
| `data/mapper/VehicleFinanceMapper.kt` | DTO → Entity |
| `domain/entity/VehiclePurchase.kt` | Domain entity |
| `domain/entity/LoanPayment.kt` | EMI entity |
| `domain/repository/VehicleFinanceRepository.kt` | Repository interface |

## Shared ViewModel Pattern

Finance uses `rememberSharedViewModel` because multiple screens (Detail, EMI History,
Edit Purchase) share the same ViewModel state:

```kotlin
val financeVM = rememberSharedViewModel("finance_$vehicleId") {
    vehicleFinanceViewModel()
}
```

Clear when leaving flow: `clearSharedViewModel("finance_$vehicleId")`

## APIs

- `GET /vehicle-finance` — List all
- `GET /vehicle-finance/{id}` — Detail
- `POST /vehicle-finance` — Add purchase
- `PUT /vehicle-finance/{id}` — Update purchase
- `GET /vehicle-finance/{id}/payments` — EMI history

## Module Path

`screen-finance/src/commonMain/kotlin/com/ijs/finance/`

## Depends On: `ijs-network-lib`

