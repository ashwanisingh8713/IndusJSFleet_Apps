# 05. Trips API

> **Package:** `com.indusjs.fleet.data.datasource.trip`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/trips` | List all trips |
| `GET` | `/trips/{id}` | Get trip by ID |
| `POST` | `/trips` | Create a new trip |
| `PUT` | `/trips/{id}` | Update trip |
| `PATCH` | `/trips/{id}/cancel` | Cancel a trip |

### State & Progress

| Method | Endpoint | Description |
|--------|----------|-------------|
| `PATCH` | `/trips/{id}/status` | Update trip state |
| `GET` | `/trips/{id}/state-history` | Get state change history |
| `PATCH` | `/trips/{id}/progress` | Update trip progress (distance, duration, location) |
| `PATCH` | `/trips/{id}/location` | Update current location |

### Related

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles/{id}/trips` | Get trips by vehicle ID |
| `GET` | `/trips/{id}/costs` | Get trip costs |
| `GET` | `/trips/{id}/payments` | Get trip payments |

### Trip Stops

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/trips/{id}/stops` | List trip stops |
| `POST` | `/trips/{id}/stops` | Add a trip stop |
| `PUT` | `/trips/{id}/stops/{stopId}` | Update a trip stop |
| `PATCH` | `/trips/{id}/stops/{stopId}/complete` | Mark stop completed |
| `DELETE` | `/trips/{id}/stops/{stopId}` | Delete a trip stop |

---

## Data Source Interface

### `TripRemoteDataSource`

```kotlin
interface TripRemoteDataSource : RemoteDataSource {
    // CRUD
    suspend fun getTrips(token: String): TripApiResponse<List<TripDto>>
    suspend fun getTripById(token: String, id: String): TripApiResponse<TripDto>
    suspend fun createTrip(token: String, request: CreateTripRequest): TripApiResponse<TripDto>
    suspend fun updateTrip(token: String, id: String, request: UpdateTripRequest): TripApiResponse<TripDto>
    suspend fun cancelTrip(token: String, id: String): TripApiResponse<Unit>

    // Status
    suspend fun updateTripStatus(token: String, id: String, status: String): TripApiResponse<TripDto>
    suspend fun updateTripState(token: String, id: String, state: String, reason: String?, notes: String?): TripApiResponse<TripDto>
    suspend fun getTripStateHistory(token: String, id: String, page: Int, perPage: Int): TripApiResponse<StateHistoryResponseDto>

    // Progress
    suspend fun updateTripProgress(token: String, tripId: String, coveredDistance: Double?, coveredDurationMinutes: Long?, currentLat: Double?, currentLng: Double?): TripApiResponse<TripDto>
    suspend fun updateTripLocation(token: String, tripId: String, lat: Double, lng: Double): TripApiResponse<TripDto>

    // Related
    suspend fun getTripsByVehicleId(token: String, vehicleId: String): TripApiResponse<List<TripDto>>

    // Trip Stops
    suspend fun getTripStops(token: String, tripId: String): TripStopsApiResponse
    suspend fun createTripStop(token: String, tripId: String, request: CreateTripStopRequest): TripStopApiResponse
    suspend fun updateTripStop(token: String, tripId: String, stopId: String, request: UpdateTripStopRequest): TripStopApiResponse
    suspend fun markStopCompleted(token: String, tripId: String, stopId: String): TripStopApiResponse
    suspend fun deleteTripStop(token: String, tripId: String, stopId: String): TripApiResponse<Unit>
}
```

**Implementation:** `TripRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

---

## Trip States

```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

---

## Key DTOs

### `CreateTripRequest`

| Field | JSON Key | Type | Required | Notes |
|-------|----------|------|----------|-------|
| `vehicleId` | `vehicle_id` | `Int` | ✅ | |
| `driverId` | `driver_id` | `Int` | ✅ | |
| `startLocation` | `start_location` | `LocationDto` | ✅ | `{ address, lat, lng }` |
| `endLocation` | `end_location` | `LocationDto` | ✅ | |
| `scheduledDate` | `scheduled_date` | `String` | ✅ | ISO 8601: `2026-01-04T14:30:00Z` |
| `plannedStart` | `planned_start` | `String?` | ❌ | ISO 8601 |
| `plannedEnd` | `planned_end` | `String?` | ❌ | ISO 8601 |
| `distance` | `distance` | `Double?` | ❌ | km |
| `cargoType` | `cargo_type` | `String?` | ❌ | See Cargo Types |
| `cargoWeight` | `cargo_weight` | `Double?` | ❌ | tons |
| `tripPrice` | `trip_price` | `Double?` | ❌ | Owner/GM only |
| `customerId` | `customer_id` | `Int?` | ❌ | |
| `notes` | `notes` | `String?` | ❌ | |

### `TripDto`

Core trip response fields:

| Field | JSON Key | Type |
|-------|----------|------|
| `id` | `id` | `Int` |
| `vehicleId` | `vehicle_id` | `Int` |
| `driverId` | `driver_id` | `Int` |
| `state` | `state` | `String` |
| `startLocation` | `start_location` | `LocationDto?` |
| `endLocation` | `end_location` | `LocationDto?` |
| `scheduledDate` | `scheduled_date` | `String?` |
| `plannedStart` | `planned_start` | `String?` |
| `plannedEnd` | `planned_end` | `String?` |
| `distance` | `distance` | `Double?` |
| `cargoType` | `cargo_type` | `String?` |
| `tripPrice` | `trip_price` | `Double?` |
| `vehicleNumber` | `vehicle_number` | `String?` |
| `driverName` | `driver_name` | `String?` |
| `customerName` | `customer_name` | `String?` |

### Trip Stop DTOs

**`CreateTripStopRequest`:**

| Field | JSON Key | Type |
|-------|----------|------|
| `location` | `location` | `LocationDto` |
| `stopOrder` | `stop_order` | `Int` |
| `purpose` | `purpose` | `String?` |
| `estimatedArrival` | `estimated_arrival` | `String?` |

---

## Date Format

> **CRITICAL:** Trip scheduling dates use **ISO 8601** format (`2026-01-04T14:30:00Z`).
> Convert from UI format `DD-MM-YYYY` + `HH:MM` before sending.

---

## Cargo Types

```
Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others
```

---

## Source Files

| File | Path |
|------|------|
| TripRemoteDataSource | `data/datasource/trip/TripRemoteDataSource.kt` |
| TripDto & related | `data/model/trip/TripDto.kt` |
| StateUpdateRequestDto | `data/model/state/` |

