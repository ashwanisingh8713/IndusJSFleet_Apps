# AGENTS.md — screen-trip-payment

## Purpose

Full-stack **Trip Payment Management** feature module. Handles trip payment recording, listing with filters, detail view, and add/edit flows. Payments are always associated with a trip and tracked by mode (cash, UPI, bank transfer, cheque, card) and status (received, pending, cancelled).

**Package:** `com.ijs.trip.payment`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/trip/payment/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   └── TripPaymentRemoteDataSource.kt  # API: list, detail, create, update, delete payments
│   ├── mapper/
│   │   └── TripPaymentMapper.kt            # TripPaymentDto ↔ TripPayment entity
│   ├── model/
│   │   ├── TripPaymentDto.kt               # @Serializable payment DTOs
│   │   └── TripPaymentRequest.kt           # Create/update request DTOs
│   └── repository/
│       └── TripPaymentRepositoryImpl.kt    # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   ├── PaymentEnums.kt                 # PaymentMode, PaymentStatus enums
│   │   └── TripPayment.kt                  # Payment domain entity
│   └── repository/
│       ├── TripPaymentRepository.kt        # Repository interface
│       └── TripProviderForPayment.kt       # Interface for trip data needed by payment screens
└── presentation/
    ├── PaymentFeatureFacade.kt             # DI entry point
    ├── PaymentsContract.kt                 # List screen MVI contract
    ├── PaymentsScreen.kt                   # Payment list with filter chips and grouping
    ├── PaymentsViewModel.kt                # List ViewModel with filtering/sorting
    ├── PaymentFilterBottomSheet.kt         # Advanced filter bottom sheet (date range, mode, status)
    ├── PaymentGroupComponents.kt           # Grouped payment display (by date, by trip)
    ├── PaymentListComponents.kt            # Payment list item cards
    ├── PaymentDetailScreen.kt              # Payment detail with hero card + trip info
    ├── PaymentDetailViewModel.kt           # Detail ViewModel
    ├── PaymentDetailHeroAndTripCards.kt    # Hero card with amount + status badge
    ├── PaymentDetailInfoCards.kt           # Payment method, reference, notes cards
    ├── AddPaymentScreen.kt                 # Add payment form (mode, amount, reference, date)
    └── AddPaymentViewModel.kt              # Add payment ViewModel with validation
```

---

## Payment Status & Modes

**Status:** `received` | `pending` | `cancelled`
**Modes:** `cash` | `upi` | `bank_transfer` | `cheque` | `card`

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:screen-customer` |
| implementation | `:ijs-pdf-report`, `:ijs-datetime-picker` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client |

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| PaymentsScreen | `Payments` | List with filter chips + advanced filter bottom sheet |
| PaymentDetailScreen | `PaymentDetail(id)` | Hero card with amount, trip info, payment method |
| AddPaymentScreen | `AddPayment(tripId?, vehicleId?)` | Record payment with mode selection |

---

## Key Patterns

- **TripProviderForPayment** — Interface abstraction so payment module can fetch trip data without directly depending on `screen-trip` (avoids circular dependency). Implemented in `sharedUI`.
- **Filter bottom sheet** — `PaymentFilterBottomSheet` provides advanced filtering by date range, payment mode, and status. Uses `DateRangePickerDialog` from `ijs-ui-components-lib`.
- **Grouped display** — `PaymentGroupComponents` groups payments by date or by trip for better organization.
- **Hero card** — `PaymentDetailHeroAndTripCards` shows a prominent amount display with status badge, similar to banking apps.
