# Trips Feature

> Use this prompt when working on trip management — creation, detail, status transitions, or costs.

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `TripsScreen` | `Trips` | List with status filter chips |
| `TripDetailScreen` | `TripDetail(tripId)` | Route, cargo, schedule, costs, payments |
| `CreateTripScreen` | `CreateTrip` | Multi-step: vehicle → driver → route → schedule → cargo → customer → pricing |
| `TripCostEntryScreen` | `TripCostEntry(tripId?, vehicleId?)` | Record trip expense |

## API Endpoints

```
GET    /trips              → List all trips (supports status filter)
GET    /trips/{id}         → Trip details (includes costs, payments)
POST   /trips              → Create trip (ISO 8601 dates!)
PUT    /trips/{id}         → Update trip
PATCH  /trips/{id}/cancel  → Cancel trip
PATCH  /trips/{id}/status  → Update status (planned → on_route → completed)
POST   /trip-costs         → Add trip cost (DD-MM-YYYY dates!)
GET    /trip-costs?trip_id={id} → Get costs for trip
GET    /trips/{id}/payments → Get payments for trip
```

## Create Trip Flow

```
1. Select Vehicle (from available vehicles — status: "active")
2. Select Driver (from available drivers — status: "active")
3. Set Route:
   - Start Location (Google Places autocomplete)
   - End Location (Google Places autocomplete)
   - Auto-calculate distance (Google Distance Matrix API)
   - Optional intermediate stops
4. Set Schedule:
   - Departure Date + Time (→ ISO 8601 for API)
   - Estimated Arrival Date + Time (→ ISO 8601 for API)
5. Cargo Details:
   - Cargo type (Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others)
   - Weight, description
6. Customer Selection (from customer list)
7. Pricing (Owner/GM only):
   - Trip price
   - Advance payment
```

**CRITICAL: Trip dates must be sent as ISO 8601:**
```kotlin
val isoDate = FleetDateTime.toIso8601(state.departureDate, state.departureTime)
// "15-03-2026" + "14:30" → "2026-03-15T14:30:00Z"
```

## Trip States

See `entity-states.prompt.md` — Trip section.

```
planned → on_route → completed
planned → cancelled
on_route → delayed → completed | failed
on_route → cancelled | failed
```

## Trip Cost Types

See `cost-types.prompt.md` — Trip Costs (TC) section.

**CRITICAL: Cost dates are DD-MM-YYYY, NOT ISO 8601.**

## Domain Entity: `Trip`

```kotlin
data class Trip(
    val id: String,
    val vehicleId: String,
    val driverId: String,
    val vehicleNumber: String?,
    val driverName: String?,
    val status: String,                // TripState constants
    val startLocation: String,
    val endLocation: String,
    val distance: Double?,
    val plannedStart: String?,         // ISO 8601
    val plannedEnd: String?,           // ISO 8601
    val actualStart: String?,          // ISO 8601
    val actualEnd: String?,            // ISO 8601
    val cargoType: String?,
    val cargoWeight: Double?,
    val tripPrice: Double?,            // Owner/GM only
    val customerId: String?,
    val customerName: String?,
    val costs: List<TripCost>?,
    val payments: List<TripPayment>?,
    val stops: List<TripStop>?
)
```

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/trip/Trip.kt` |
| Repository | `domain/repository/trip/TripRepository.kt` |
| Use Cases | `domain/usecase/trip/` (6 use cases) |
| DTO | `data/model/trip/` |
| Mapper | `data/mapper/trip/TripMapper.kt`, `TripStopMapper.kt` |
| DataSource | `data/datasource/trip/TripRemoteDataSourceImpl.kt` |
| Contract | `presentation/trips/TripsContract.kt` |
| Screen | `presentation/trips/TripsScreen.kt` |
| Create | `presentation/trips/create/` |
| Detail | `presentation/trips/detail/` |
| Cost Entry | `presentation/trips/cost/` |

