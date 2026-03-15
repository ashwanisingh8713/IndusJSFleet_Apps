# API Integration Guide

> Use this prompt when connecting a feature to the Fleet Management backend API.

## Base URL

```
Production: https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2
```

All endpoints are relative to this base. Auth token required via `Authorization: Bearer <token>`.

## API Response Format

Every API returns this wrapper:

```kotlin
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null
)
```

## DTO Convention

```kotlin
@Serializable
data class MyDto(
    @SerialName("id") val id: Int,                       // API uses int IDs
    @SerialName("vehicle_id") val vehicleId: Int,        // snake_case → camelCase
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)
```

**Rules:**
- Every field gets `@SerialName("snake_case")`
- Kotlin property is `camelCase`
- Nullable + default for optional API fields
- IDs are `Int` from API, converted to `String` in domain entities

## DataSource Pattern

```kotlin
class MyRemoteDataSourceImpl(private val httpClient: HttpClient) {

    suspend fun getAll(token: String): MyListResponse {
        return httpClient.get("${ApiConfig.BASE_URL}/my-items") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
        }.body()
    }

    suspend fun getById(id: String, token: String): MySingleResponse {
        return httpClient.get("${ApiConfig.BASE_URL}/my-items/$id") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
        }.body()
    }

    suspend fun create(token: String, request: CreateMyRequest): MySingleResponse {
        return httpClient.post("${ApiConfig.BASE_URL}/my-items") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun update(id: String, token: String, request: UpdateMyRequest): MySingleResponse {
        return httpClient.put("${ApiConfig.BASE_URL}/my-items/$id") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun delete(id: String, token: String): ApiResponse<Unit> {
        return httpClient.delete("${ApiConfig.BASE_URL}/my-items/$id") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
        }.body()
    }
}
```

## Repository Pattern

```kotlin
class MyRepositoryImpl(
    private val remoteDataSource: MyRemoteDataSourceImpl,
    private val userLocalDataSource: UserLocalDataSourceImpl,
    private val mapper: MyMapper
) : MyRepository {

    override fun getAll(): Flow<Result<List<MyEntity>>> = flow {
        emit(Result.Loading)
        try {
            val token = userLocalDataSource.getAuthToken()
                ?: throw AuthException.unauthenticated()
            val response = remoteDataSource.getAll(token)
            if (response.success && response.data != null) {
                emit(Result.Success(mapper.toDomainList(response.data)))
            } else {
                emit(Result.Error(
                    ApiException(response.message ?: "Failed to load"),
                    response.message
                ))
            }
        } catch (e: Exception) {
            if (e is AuthException) throw e  // Let 401 propagate
            emit(Result.Error(e, e.message))
        }
    }
}
```

## Available Endpoints

| Feature | Method | Endpoint |
|---------|--------|----------|
| **Auth** | POST | `/auth/login`, `/auth/signup`, `/auth/forgot-password` |
| **Profile** | GET/PUT | `/profile`, `/profile/change-password` |
| **Vehicles** | CRUD | `/vehicles`, `/vehicles/{id}` |
| **Drivers** | CRUD | `/drivers`, `/drivers/{id}`, `/drivers/{id}/toggle-active` |
| **Trips** | CRUD | `/trips`, `/trips/{id}`, `/trips/{id}/cancel` |
| **Trip Costs** | CRUD | `/trip-costs`, `/trip-costs/{id}` |
| **Maintenance Costs** | CRUD | `/maintenance-costs`, `/maintenance-costs/{id}` |
| **Driver Costs** | CRUD | `/drivers/{id}/costs` |
| **Cost Types** | GET | `/cost-types/trip`, `/cost-types/maintenance`, `/cost-types/driver` |
| **Dashboard** | GET | `/dashboard`, `/dashboard/cost-overview`, `/dashboard/pending-payments`, `/dashboard/alerts-status` |
| **Payments** | CRUD | `/trip-payments`, `/trip-payments/{id}`, `/trips/{id}/payments` |
| **Customers** | CRUD | `/customers`, `/customers/{id}` |
| **Team** | CRUD | `/team-members`, `/team-members/{id}` |
| **Reports** | GET/POST | `/reports/profit-loss`, `/vehicles/{id}/profit-loss`, `/trips/{id}/profit-loss` |
| **Finance** | CRUD | `/vehicles/{id}/purchase-info`, `/vehicles/{id}/loan-payments` |
| **Documents** | CRUD | `/vehicles/{id}/documents` |
| **Caretaker** | POST | `/vehicles/{id}/assign-caretaker`, `/drivers/{id}/assign-caretaker` |

## 401 Handling

The HttpClient interceptor in `HttpClientProvider.kt` automatically:
1. Detects 401 responses
2. Calls `AuthenticationManager.emitUnauthorized()`
3. Clears session via registered callback
4. App.kt collects the event and navigates to Login

No manual 401 handling needed in DataSources.

