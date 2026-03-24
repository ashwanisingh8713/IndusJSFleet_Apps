# 03. Vehicles API

> **Package:** `com.indusjs.fleet.data.datasource.vehicle`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles` | List all vehicles |
| `GET` | `/vehicles/available` | List available (unoccupied) vehicles |
| `GET` | `/vehicles/{id}` | Get vehicle by ID |
| `POST` | `/vehicles` | Create a new vehicle |
| `POST` | `/vehicles/with-documents` | Create vehicle with documents |
| `PUT` | `/vehicles/{id}` | Update vehicle |
| `DELETE` | `/vehicles/{id}` | Delete vehicle |

### Detail Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles/{id}/detail` | Get vehicle detail (overview) |
| `GET` | `/vehicles/{id}/trips` | Get vehicle's trips (paginated) |
| `GET` | `/vehicles/{id}/route` | Get vehicle route info |
| `GET` | `/vehicles/{id}/documents/detail` | Get documents with detail |
| `GET` | `/vehicles/{id}/documents` | Get vehicle documents (legacy) |

### State Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `PATCH` | `/vehicles/{id}/state` | Update vehicle state |
| `GET` | `/vehicles/{id}/state-history` | Get state change history |

### Documents & History

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/vehicles/{id}/documents` | Upload document (multipart) |
| `GET` | `/documents/{id}/download` | Download document |
| `GET` | `/vehicles/{id}/history` | Get vehicle change history |

---

## Data Source Interface

### `VehicleRemoteDataSource`

```kotlin
interface VehicleRemoteDataSource : RemoteDataSource {
    // CRUD
    suspend fun getVehicles(token: String): VehicleApiResponse<List<VehicleDto>>
    suspend fun getAvailableVehicles(token: String): VehicleApiResponse<List<VehicleDto>>
    suspend fun getVehicleById(token: String, id: String): VehicleApiResponse<VehicleDto>
    suspend fun createVehicle(token: String, request: CreateVehicleRequest): VehicleApiResponse<VehicleDto>
    suspend fun createVehicleWithDocuments(token: String, request: CreateVehicleWithDocumentsRequest): VehicleApiResponse<VehicleDto>
    suspend fun updateVehicle(token: String, id: String, request: UpdateVehicleRequest): VehicleApiResponse<VehicleDto>
    suspend fun deleteVehicle(token: String, id: String): VehicleApiResponse<Unit>

    // State Management
    suspend fun updateVehicleState(token: String, id: String, state: String, reason: String?, notes: String?): VehicleApiResponse<VehicleDto>
    suspend fun getVehicleStateHistory(token: String, id: String, page: Int, perPage: Int): VehicleApiResponse<StateHistoryResponseDto>

    // Detail
    suspend fun getVehicleDetail(token: String, id: String): VehicleApiResponse<VehicleDetailDto>
    suspend fun getVehicleTrips(token: String, id: String, page: Int, perPage: Int, state: String?): VehicleApiResponse<VehicleTripsDto>
    suspend fun getVehicleRoute(token: String, id: String): VehicleApiResponse<VehicleRouteDto>
    suspend fun getVehicleDocumentsDetail(token: String, id: String): VehicleApiResponse<VehicleDocumentsDetailDto>

    // Legacy
    suspend fun getVehicleDocuments(token: String, id: String): VehicleApiResponse<List<VehicleDocumentDto>>
    suspend fun getTripsByVehicleId(token: String, vehicleId: String, page: Int, perPage: Int, state: String?): VehicleApiResponse<List<VehicleTripDto>>

    // Documents
    suspend fun uploadDocument(token: String, vehicleId: String, documentType: String, documentName: String, fileBytes: ByteArray, fileName: String, mimeType: String, documentNumber: String?, expiryDate: String?): VehicleApiResponse<VehicleDocumentDto>
    suspend fun downloadDocument(token: String, documentId: String): Result<ByteArray>

    // History
    suspend fun getVehicleHistory(token: String, id: String, page: Int, perPage: Int): VehicleHistoryApiResponse
}
```

**Implementation:** `VehicleRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

---

## Key DTOs

### `VehicleDto`

| Field | JSON Key | Type | Notes |
|-------|----------|------|-------|
| `id` | `id` | `Int` | |
| `registrationNumber` | `registration_number` | `String` | |
| `make` | `make` | `String` | |
| `model` | `model` | `String` | |
| `year` | `year` | `Int` | |
| `type` | `vehicle_type` | `String` | |
| `status` | `state` | `String` | See Vehicle States |
| `fuelType` | `fuel_type` | `String?` | |
| `fuelLevel` | `fuel_level` | `Int` | |
| `mileage` | `mileage` | `Double` | |
| `capacity` | `capacity` | `Int?` | |
| `color` | `color` | `String?` | |
| `lastLocation` | `last_location` | `LocationDto?` | GPS coordinates |
| `assignedDriverId` | `assigned_driver_id` | `Int?` | |
| `assignedDriverName` | `assigned_driver_name` | `String?` | |
| `assignedDriver` | `assigned_driver` | `AssignedDriverDto?` | Nested driver info |
| `isOccupied` | `is_occupied` | `Boolean` | Currently on trip |
| `tripAssignment` | `trip_assignment` | `VehicleTripAssignmentDto?` | Current trip info |
| `createdAt` | `created_at` | `String?` | |

### Vehicle States

```
inactive ←→ active → on_route → active
                  → maintenance ←→ damaged → decommissioned
```

### `VehicleApiResponse<T>`

Generic response wrapper for vehicle APIs (same pattern as `ApiResponse<T>`).

---

## Document Upload (Multipart)

Upload uses `submitFormWithBinaryData` with the following form fields:

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `document_type` | `String` | ✅ | e.g., `rc`, `insurance`, `permit` |
| `document_name` | `String` | ✅ | User-friendly name |
| `document_number` | `String` | ❌ | Document reference number |
| `expiry_date` | `String` | ❌ | Format: `DD-MM-YYYY` |
| `file` | `ByteArray` | ✅ | Binary file content |

---

## Source Files

| File | Path |
|------|------|
| VehicleRemoteDataSource | `data/datasource/vehicle/VehicleRemoteDataSource.kt` |
| VehicleDto & related | `data/model/vehicle/VehicleDto.kt` |
| StateHistoryResponseDto | `data/model/state/` |
| VehicleHistoryApiResponse | `data/model/history/` |

