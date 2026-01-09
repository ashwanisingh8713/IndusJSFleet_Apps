# API Integration Template

Integrate an API endpoint following the IndusJS Fleet architecture.

## API Information
- **Endpoint**: [ENDPOINT_PATH]
- **Method**: [GET/POST/PUT/DELETE/PATCH]
- **Description**: [BRIEF_DESCRIPTION]
- **Reference**: See `Fleet_Management_API_v2.postman_collection.json`

## Critical API Rules

### Date/Time Format
```
Display (UI)        API Request (v2)
--------------      ----------------
DD-MM-YYYY    →     DD-MM-YYYY (for date-only fields)
                    OR ISO 8601 (for datetime: 2026-01-04T14:30:00Z)
HH:MM (24hr)  →     HH:MM or combined with date in ISO 8601
```

### Authentication
All protected endpoints require:
```kotlin
header("Authorization", "Bearer $token")
```

### Error Response Handling
```kotlin
// API returns: {"success": false, "message": "Error message"}
if (!response.success) {
    return Result.Error(ApiException(response.message))
}
```

---

## Remote Data Source Template

```kotlin
package com.indusjs.fleet.data.datasource.{feature}

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.{feature}.*
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Remote data source for {Entity} API operations.
 */
@Inject
class {Entity}RemoteDataSource(
    private val httpClient: HttpClient
) : RemoteDataSource {

    private val log = Logger.withTag("{Entity}RemoteDataSource")

    /**
     * Fetch list of {entities} with pagination.
     */
    suspend fun get{Entity}s(
        page: Int = 1,
        perPage: Int = 10,
        status: String? = null
    ): {Entity}ListResponseDto {
        log.d { "Fetching {entities}: page=$page, status=$status" }
        
        val response = httpClient.get("${ApiConfig.BASE_URL}/{entities}") {
            parameter("page", page)
            parameter("per_page", perPage)
            status?.let { parameter("status", it) }
        }
        
        log.d { "Response status: ${response.status}" }
        return response.body()
    }

    /**
     * Fetch single {entity} by ID.
     */
    suspend fun get{Entity}(id: String): {Entity}ResponseDto {
        log.d { "Fetching {entity}: $id" }
        
        val response = httpClient.get("${ApiConfig.BASE_URL}/{entities}/$id")
        
        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            log.e { "Error fetching {entity}: $errorBody" }
        }
        
        return response.body()
    }

    /**
     * Create new {entity}.
     */
    suspend fun create{Entity}(request: Create{Entity}Request): {Entity}ResponseDto {
        log.d { "Creating {entity}: $request" }
        
        val response = httpClient.post("${ApiConfig.BASE_URL}/{entities}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        val responseBody = response.bodyAsText()
        log.d { "Create response: ${response.status} - $responseBody" }
        
        return response.body()
    }

    /**
     * Update existing {entity}.
     */
    suspend fun update{Entity}(id: String, request: Update{Entity}Request): {Entity}ResponseDto {
        log.d { "Updating {entity} $id: $request" }
        
        val response = httpClient.put("${ApiConfig.BASE_URL}/{entities}/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        log.d { "Update response: ${response.status}" }
        return response.body()
    }

    /**
     * Partially update {entity} (PATCH).
     */
    suspend fun patch{Entity}(id: String, request: Patch{Entity}Request): {Entity}ResponseDto {
        log.d { "Patching {entity} $id: $request" }
        
        val response = httpClient.patch("${ApiConfig.BASE_URL}/{entities}/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        return response.body()
    }

    /**
     * Update {entity} status/state.
     */
    suspend fun update{Entity}State(id: String, state: String): {Entity}ResponseDto {
        log.d { "Updating {entity} $id state to: $state" }
        
        val response = httpClient.patch("${ApiConfig.BASE_URL}/{entities}/$id/state") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("state" to state))
        }
        
        return response.body()
    }

    /**
     * Delete {entity}.
     */
    suspend fun delete{Entity}(id: String): {Entity}ResponseDto {
        log.d { "Deleting {entity}: $id" }
        
        val response = httpClient.delete("${ApiConfig.BASE_URL}/{entities}/$id")
        
        log.d { "Delete response: ${response.status}" }
        return response.body()
    }

    // ==================== Nested Resources ====================

    /**
     * Fetch {entity} costs with filters.
     */
    suspend fun get{Entity}Costs(
        {entity}Id: String,
        page: Int = 1,
        perPage: Int = 50,
        costType: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        sortBy: String = "date",
        sortOrder: String = "desc"
    ): {Entity}CostsResponseDto {
        log.d { "Fetching {entity} costs: {entity}Id=${{entity}Id}" }
        
        val response = httpClient.get("${ApiConfig.BASE_URL}/{entities}/${{entity}Id}/costs") {
            parameter("page", page)
            parameter("per_page", perPage)
            costType?.let { parameter("cost_type", it) }
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
            parameter("sort_by", sortBy)
            parameter("sort_order", sortOrder)
        }
        
        return response.body()
    }

    /**
     * Create {entity} cost.
     */
    suspend fun create{Entity}Cost(
        {entity}Id: String,
        request: Create{Entity}CostRequest
    ): {Entity}CostResponseDto {
        log.d { "Creating {entity} cost for ${{entity}Id}" }
        
        val response = httpClient.post("${ApiConfig.BASE_URL}/{entities}/${{entity}Id}/costs") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        
        return response.body()
    }
}
```

---

## Repository Implementation Template

```kotlin
package com.indusjs.fleet.data.repository.{feature}

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.error.ApiException
import com.indusjs.fleet.core.error.NetworkException
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.{feature}.{Entity}RemoteDataSource
import com.indusjs.fleet.data.mapper.{feature}.{Entity}Mapper
import com.indusjs.fleet.data.mapper.mapToDomainList
import com.indusjs.fleet.domain.entity.{feature}.{Entity}
import com.indusjs.fleet.domain.repository.{feature}.{Feature}Repository
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException

/**
 * Repository implementation for {Entity} operations.
 * Coordinates between data source and domain layer.
 */
@Inject
class {Feature}RepositoryImpl(
    private val remoteDataSource: {Entity}RemoteDataSource
) : {Feature}Repository {

    private val log = Logger.withTag("{Feature}RepositoryImpl")

    override suspend fun get{Entity}s(
        page: Int,
        status: String?
    ): Result<List<{Entity}>> = safeApiCall {
        val response = remoteDataSource.get{Entity}s(page = page, status = status)
        
        if (response.success && response.data != null) {
            Result.Success({Entity}Mapper.mapToDomainList(response.data))
        } else {
            Result.Error(ApiException(response.message), response.message)
        }
    }

    override suspend fun get{Entity}(id: String): Result<{Entity}> = safeApiCall {
        val response = remoteDataSource.get{Entity}(id)
        
        if (response.success && response.data != null) {
            Result.Success({Entity}Mapper.mapToDomain(response.data))
        } else {
            Result.Error(ApiException(response.message), response.message)
        }
    }

    override suspend fun create{Entity}(entity: {Entity}): Result<{Entity}> = safeApiCall {
        val request = {Entity}Mapper.toCreateRequest(entity)
        val response = remoteDataSource.create{Entity}(request)
        
        if (response.success && response.data != null) {
            Result.Success({Entity}Mapper.mapToDomain(response.data))
        } else {
            Result.Error(ApiException(response.message), response.message)
        }
    }

    override suspend fun update{Entity}(entity: {Entity}): Result<{Entity}> = safeApiCall {
        val request = {Entity}Mapper.toUpdateRequest(entity)
        val response = remoteDataSource.update{Entity}(entity.id, request)
        
        if (response.success && response.data != null) {
            Result.Success({Entity}Mapper.mapToDomain(response.data))
        } else {
            Result.Error(ApiException(response.message), response.message)
        }
    }

    override suspend fun delete{Entity}(id: String): Result<Unit> = safeApiCall {
        val response = remoteDataSource.delete{Entity}(id)
        
        if (response.success) {
            Result.Success(Unit)
        } else {
            Result.Error(ApiException(response.message), response.message)
        }
    }

    override suspend fun update{Entity}State(id: String, state: String): Result<{Entity}> = safeApiCall {
        val response = remoteDataSource.update{Entity}State(id, state)
        
        if (response.success && response.data != null) {
            Result.Success({Entity}Mapper.mapToDomain(response.data))
        } else {
            Result.Error(ApiException(response.message), response.message)
        }
    }

    /**
     * Wrapper for safe API calls with proper error handling.
     */
    private suspend fun <T> safeApiCall(call: suspend () -> Result<T>): Result<T> {
        return try {
            call()
        } catch (e: ClientRequestException) {
            log.e(e) { "Client error: ${e.response.status}" }
            Result.Error(ApiException("Request failed: ${e.response.status}", e.response.status.value), e.message)
        } catch (e: ServerResponseException) {
            log.e(e) { "Server error: ${e.response.status}" }
            Result.Error(ApiException("Server error: ${e.response.status}", e.response.status.value), e.message)
        } catch (e: IOException) {
            log.e(e) { "Network error" }
            Result.Error(NetworkException("Network unavailable", e), "Network unavailable")
        } catch (e: Exception) {
            log.e(e) { "Unknown error" }
            Result.Error(e, e.message ?: "Unknown error")
        }
    }
}
```

---

## Common API Patterns

### Pagination
```kotlin
// Request
parameter("page", page)
parameter("per_page", 10)

// Response DTO
@SerialName("page") val page: Int?,
@SerialName("per_page") val perPage: Int?,
@SerialName("total") val total: Int?,
@SerialName("total_pages") val totalPages: Int?
```

### Filtering
```kotlin
// Request parameters
parameter("status", status)
parameter("state", state)
parameter("start_date", startDate)  // DD-MM-YYYY
parameter("end_date", endDate)      // DD-MM-YYYY
parameter("cost_type", costType)
parameter("sort_by", "date")
parameter("sort_order", "desc")
```

### File Upload
```kotlin
suspend fun uploadDocument(
    {entity}Id: String,
    fileName: String,
    fileBytes: ByteArray,
    mimeType: String
): DocumentResponseDto {
    val response = httpClient.submitFormWithBinaryData(
        url = "${ApiConfig.BASE_URL}/{entities}/${{entity}Id}/documents",
        formData = formData {
            append("file", fileBytes, Headers.build {
                append(HttpHeaders.ContentType, mimeType)
                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
            })
            append("document_type", "registration_certificate")
        }
    )
    return response.body()
}
```

---

## Validation Checklist

- [ ] Data source uses `@Inject` annotation
- [ ] All API calls wrapped in try-catch or safeApiCall
- [ ] Logging added for debugging (Logger.withTag)
- [ ] Response success checked before returning data
- [ ] Error responses properly converted to Result.Error
- [ ] Pagination parameters included where needed
- [ ] Content-Type set for POST/PUT/PATCH requests
- [ ] Date format conversion done before API call

