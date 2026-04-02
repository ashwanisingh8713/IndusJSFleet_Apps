# AGENTS.md — screen-finance

## Purpose

Full-stack **Vehicle Finance** feature module. Tracks vehicle purchases (cash or loan), loan details, and EMI payment history. Allows fleet owners to record purchase information and manage loan repayment schedules.

**Package:** `com.ijs.finance`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/finance/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   └── VehicleFinanceRemoteDataSource.kt  # API: purchase info, loan details, EMI payments
│   ├── mapper/
│   │   └── VehicleFinanceMapper.kt            # DTO ↔ domain entity mapping
│   ├── model/
│   │   └── VehicleFinanceDto.kt               # @Serializable DTOs (purchase, loan, EMI)
│   └── repository/
│       └── VehicleFinanceRepositoryImpl.kt    # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   ├── VehiclePurchase.kt                 # Purchase domain entity (price, mode, dealer, date)
│   │   └── LoanPayment.kt                    # Loan + EMI payment domain entity
│   └── repository/
│       └── VehicleFinanceRepository.kt        # Repository interface
└── presentation/
    ├── FinanceFeatureFacade.kt                # DI entry point
    ├── VehicleFinanceContract.kt              # Shared MVI contract for all finance screens
    ├── VehicleFinanceScreen.kt                # Finance list: all vehicles with finance status
    ├── VehicleFinanceDetailScreen.kt          # Purchase info + loan summary for a vehicle
    ├── VehicleFinanceViewModel.kt             # Shared ViewModel for list + detail + add + EMI
    ├── AddPurchaseInfoScreen.kt               # Record purchase (cash/loan, amount, dealer, date)
    └── EmiPaymentHistoryScreen.kt             # EMI payment list with add payment
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:screen-vehicle` |
| implementation | `:ijs-pdf-report`, `:ijs-datetime-picker` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client, kotlinx-datetime |

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| VehicleFinanceScreen | `VehicleFinance` | All vehicles with finance status |
| VehicleFinanceDetailScreen | `VehicleFinanceDetail(id)` | Purchase info + loan summary |
| AddPurchaseInfoScreen | `AddPurchaseInfo` | Record purchase (cash/loan) |
| EmiPaymentHistoryScreen | `EmiPaymentHistory(id)` | EMI payment list + add payment |

---

## Key Patterns

- **Single ViewModel** — `VehicleFinanceViewModel` handles all four screens (list, detail, add, EMI) since the data is closely related and the total complexity is moderate.
- **Purchase modes** — Cash purchase records price only; loan purchase additionally records loan amount, interest rate, tenure, and EMI schedule.
- **EMI tracking** — `EmiPaymentHistoryScreen` shows paid/pending EMIs with running balance. New EMI payments can be recorded with date and amount.
- **Vehicle dependency** — Depends on `screen-vehicle` for vehicle selection when adding purchase info.
