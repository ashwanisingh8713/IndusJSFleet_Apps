# Payments Feature

> Use this prompt when working on trip payment tracking, recording, or editing.

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `PaymentsScreen` | `Payments` | List with filters (status, mode, date range) |
| `PaymentDetailScreen` | `PaymentDetail(paymentId)` | Full payment details |
| `AddPaymentScreen` | `AddPayment(tripId?, vehicleId?)` | Record a payment |
| `EditPaymentScreen` | `EditPayment(paymentId)` | Edit existing payment |

## API Endpoints

```
GET    /trip-payments              → List all payments (filterable)
GET    /trip-payments/{id}         → Payment details
POST   /trip-payments              → Create payment
PUT    /trip-payments/{id}         → Update payment
DELETE /trip-payments/{id}         → Delete payment
GET    /trips/{id}/payments        → Get payments for a specific trip
GET    /trip-payments/summary      → Payment summary statistics
```

## Payment Enums

```kotlin
enum class PaymentType { ADVANCE, PARTIAL, FINAL, REFUND }
enum class PaymentMode { CASH, UPI, BANK_TRANSFER, CARD, CREDIT }
enum class PaymentStatus { RECEIVED, PENDING, CANCELLED }
```

## Domain Entity: `TripPayment`

```kotlin
data class TripPayment(
    val id: String,
    val tripId: String,
    val customerId: String?,
    val customerName: String?,
    val vehicleNumber: String?,
    val amount: Double,
    val paymentType: String,       // advance, partial, final, refund
    val paymentMode: String,       // cash, upi, bank_transfer, card, credit
    val paymentStatus: String,     // received, pending, cancelled
    val referenceNumber: String?,
    val notes: String?,
    val paymentDate: String?,      // DD-MM-YYYY
    val createdAt: String?
)
```

## Add Payment Flow

1. Select Trip (or pre-filled from TripDetail screen via `tripId` parameter)
2. Trip info auto-loads (vehicle number, customer, remaining amount)
3. Enter amount
4. Select type (Advance / Partial / Final / Refund)
5. Select mode (Cash / UPI / Bank Transfer / Card / Credit)
6. Optional: reference number, notes
7. Submit

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/payment/` |
| Repository | `domain/repository/payment/TripPaymentRepository.kt` |
| DataSource | `data/datasource/payment/TripPaymentRemoteDataSource.kt` |
| Repository Impl | `data/repository/payment/TripPaymentRepositoryImpl.kt` |
| Contract/ViewModel/Screen | `presentation/payments/` |

