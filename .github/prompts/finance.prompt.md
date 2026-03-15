# Vehicle Finance Feature

> Use this prompt when working on vehicle purchase records, loan tracking, or EMI payments.

## Access Control

**Owner and General Manager only.**

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `VehicleFinanceScreen` | `VehicleFinance` | List all vehicles with finance status |
| `VehicleFinanceDetailScreen` | `VehicleFinanceDetail(vehicleId)` | Purchase info + loan summary |
| `AddPurchaseInfoScreen` | `AddPurchaseInfo` | Record purchase (cash or loan) |
| `EditPurchaseInfoScreen` | `EditPurchaseInfo(vehicleId)` | Edit purchase details |
| `EmiPaymentHistoryScreen` | `EmiPaymentHistory(vehicleId)` | EMI payment records |

## Shared ViewModel Pattern

All finance screens for the same vehicle share a **single ViewModel** instance:

```kotlin
// In FleetNavigation.kt
val viewModel = rememberSharedViewModel("finance_${route.vehicleId}") {
    vehicleFinanceViewModel()
}
```

This means navigating between Detail → Add → Edit → EMI History preserves state.

## API Endpoints

```
GET    /vehicles/{id}/purchase-info    → Purchase details + loan info
POST   /vehicles/{id}/purchase-info    → Create purchase record
PUT    /vehicles/{id}/purchase-info    → Update purchase record
GET    /vehicles/{id}/loan-payments    → EMI payment history
POST   /vehicles/{id}/loan-payments    → Record EMI payment
```

## Purchase Types

- **Cash Purchase:** Total amount paid upfront
- **Loan Purchase:** Down payment + loan amount + interest rate + tenure + monthly EMI

## Domain Entities

```kotlin
data class VehiclePurchase(
    val id: String,
    val vehicleId: String,
    val purchaseType: String,       // "cash" or "loan"
    val totalAmount: Double,
    val purchaseDate: String?,
    val seller: String?,
    // Loan-specific
    val downPayment: Double?,
    val loanAmount: Double?,
    val interestRate: Double?,
    val tenureMonths: Int?,
    val monthlyEmi: Double?,
    val totalPaid: Double?,
    val remainingAmount: Double?,
    val nextDueDate: String?
)

data class EmiPayment(
    val id: String,
    val amount: Double,
    val paymentDate: String?,       // DD-MM-YYYY
    val notes: String?,
    val status: String?
)
```

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/finance/` |
| Repository | `domain/repository/finance/VehicleFinanceRepository.kt` |
| DataSource | `data/datasource/finance/VehicleFinanceRemoteDataSourceImpl.kt` |
| Repository Impl | `data/repository/finance/VehicleFinanceRepositoryImpl.kt` |
| All Screens | `presentation/finance/` |

