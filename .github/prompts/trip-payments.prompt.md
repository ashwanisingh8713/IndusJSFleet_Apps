# Trip Payment Tracking Implementation

## Overview

This document describes the Trip Payment tracking feature implementation in the IndusJS Fleet Management application.

## Feature Summary

The Trip Payment feature allows users to:
- Record payments received from customers for trips
- Track partial payments, advances, and final payments
- View payment history by trip, customer, or date range
- Filter and search payments
- Generate payment reports

## Architecture

### Domain Layer

#### Entities

**PaymentEnums.kt**
```kotlin
enum class PaymentType { ADVANCE, PARTIAL, FINAL, REFUND }
enum class PaymentMode { CASH, UPI, BANK_TRANSFER, CARD, CREDIT }
enum class PaymentStatus { RECEIVED, PENDING, CANCELLED }
```

**TripPayment.kt**
- `TripPayment` - Main payment entity
- `TripPaymentTripInfo` - Embedded trip info for list display
- `TripPaymentSummary` - Aggregated summary stats
- `TripPaymentListResult` - Paginated list result
- `TripPaymentFilter` - Filter criteria

#### Repository Interface

**TripPaymentRepository.kt**
- `createPayment()` - Create new payment
- `addPaymentToTrip()` - Add payment to specific trip (auto-fills customer info)
- `listPayments()` - List with filters
- `getTripPayments()` - Get payments for specific trip
- `getPayment()` - Get by ID
- `updatePayment()` - Update payment
- `deletePayment()` - Delete payment
- `getPaymentSummary()` - Get summary stats

### Data Layer

#### DTOs

**TripPaymentDto.kt**
- `TripPaymentDto` - API response DTO
- `TripPaymentResponse` - Single payment response
- `TripPaymentListResponse` - List response with pagination
- `TripPaymentsHistoryResponse` - Trip-specific payments

**TripPaymentRequest.kt**
- `CreateTripPaymentRequest` - Create payment request
- `AddPaymentToTripRequest` - Add to specific trip
- `UpdateTripPaymentRequest` - Update payment request

#### Remote Data Source

**TripPaymentRemoteDataSource.kt**
- All API calls for trip payments
- Endpoints:
  - `POST /trip-payments` - Create payment
  - `POST /trips/{id}/payments` - Add to trip
  - `GET /trip-payments` - List payments
  - `GET /trips/{id}/payments` - Get trip payments
  - `GET /trip-payments/{id}` - Get payment
  - `PUT /trip-payments/{id}` - Update payment
  - `DELETE /trip-payments/{id}` - Delete payment
  - `GET /trip-payments/summary` - Summary report

### Presentation Layer

#### MVI Contracts

**PaymentsContract.kt**
- `PaymentsContract` - Payments list screen
- `AddPaymentContract` - Add/Edit payment screen
- `PaymentDetailContract` - Payment detail screen

#### ViewModels

- `PaymentsViewModel` - List with filtering, pagination, delete
- `AddPaymentViewModel` - Create/edit with trip selection
- `PaymentDetailViewModel` - View details with delete

#### Screens

- `PaymentsScreen.kt` - Main list with summary, filters
- `AddPaymentScreen.kt` - Form with trip selector
- `PaymentDetailScreen.kt` - Detail view with actions

## Navigation Routes

```kotlin
// In FleetRoute.kt
@Serializable data object Payments : FleetRoute
@Serializable data class PaymentDetail(val paymentId: String) : FleetRoute
@Serializable data class AddPayment(val tripId: String? = null) : FleetRoute
@Serializable data class EditPayment(val paymentId: String) : FleetRoute
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/trip-payments` | Create payment |
| POST | `/trips/{id}/payments` | Add payment to trip |
| GET | `/trip-payments` | List all payments |
| GET | `/trip-payments/{id}` | Get payment by ID |
| PUT | `/trip-payments/{id}` | Update payment |
| DELETE | `/trip-payments/{id}` | Delete payment |
| GET | `/trips/{id}/payments` | Get trip's payments |
| GET | `/trip-payments/summary` | Summary report |
| GET | `/trip-payments/tds-report` | TDS report |

## Request/Response Examples

### Create Payment Request
```json
POST /trip-payments
{
  "trip_id": 1,
  "amount": 50000,
  "tds_amount": 500,
  "discount_amount": 0,
  "payment_type": "partial",
  "payment_mode": "upi",
  "payment_date": "2026-01-18T10:30:00Z",
  "transaction_id": "UPI123456",
  "notes": "First installment"
}
```

### Add Payment to Trip (simplified)
```json
POST /trips/1/payments
{
  "amount": 50000,
  "payment_type": "advance",
  "payment_mode": "cash",
  "payment_date": "2026-01-18T10:30:00Z"
}
```

### List Payments with Filters
```
GET /trip-payments?page=1&per_page=20&payment_type=advance&start_date=2026-01-01&end_date=2026-01-31
```

## UI Flow

### Payments List Screen
1. Shows summary card with totals (Received, Pending, This Month)
2. Lists all payments with mode icon, amount, status
3. Filter by type, mode, status, date range
4. Click to view details
5. Long-press for quick delete
6. FAB to add new payment

### Add Payment Screen
1. Select trip (with search/filter)
2. Shows trip info and pending amount
3. Quick fill buttons (Full/Half)
4. Enter amount, TDS, discount
5. Select payment type and mode
6. Enter date/time
7. For Bank/UPI: transaction ID, bank name
8. Optional: notes, received by, location

### Payment Detail Screen
1. Hero section with amount and status
2. Payment details card
3. Customer details card
4. Additional info card
5. Edit/Delete from menu

## Validation Rules

1. **Amount**: Required, must be > 0
2. **Trip**: Required - must select a trip
3. **Payment Date**: Required
4. **Transaction ID**: Required for Bank Transfer/UPI
5. **Amount Limit**: Cannot exceed pending amount (soft warning)

## Role-Based Access

| Role | View | Create | Edit | Delete |
|------|------|--------|------|--------|
| Owner | ✅ | ✅ | ✅ | ✅ |
| General Manager | ✅ | ✅ | ✅ | ❌ |
| Manager | ✅ | ✅ | ❌ | ❌ |
| Supervisor | ✅ | ❌ | ❌ | ❌ |
| Driver | ✅ (own trips) | ❌ | ❌ | ❌ |

## Files Created

```
domain/
├── entity/payment/
│   ├── PaymentEnums.kt
│   └── TripPayment.kt
└── repository/payment/
    └── TripPaymentRepository.kt

data/
├── model/payment/
│   ├── TripPaymentDto.kt
│   └── TripPaymentRequest.kt
├── mapper/payment/
│   └── TripPaymentMapper.kt
├── datasource/payment/
│   └── TripPaymentRemoteDataSource.kt
└── repository/payment/
    └── TripPaymentRepositoryImpl.kt

presentation/payments/
├── PaymentsContract.kt
├── PaymentsViewModel.kt
├── PaymentsScreen.kt
├── AddPaymentContract.kt (in PaymentsContract.kt)
├── AddPaymentViewModel.kt
├── AddPaymentScreen.kt
├── PaymentDetailContract.kt (in PaymentsContract.kt)
├── PaymentDetailViewModel.kt
└── PaymentDetailScreen.kt

di/
└── PaymentFeatureGraph.kt

navigation/
└── FleetRoute.kt (updated)
```

## Integration Points

1. **Dashboard**: Add "Payments" in navigation drawer
2. **Trip Detail**: Add "Record Payment" button, show payment history
3. **Customer Detail**: Show payment history in Pending/Payments tab
4. **Fleet P&L**: Include payment data in reports

## Future Enhancements

1. PDF receipt generation
2. Payment reminders/notifications
3. Bulk payment entry
4. Payment reconciliation
5. Integration with accounting software
6. Offline payment recording
