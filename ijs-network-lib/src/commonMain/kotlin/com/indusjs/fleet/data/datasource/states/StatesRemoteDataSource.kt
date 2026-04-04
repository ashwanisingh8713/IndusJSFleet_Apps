package com.indusjs.fleet.data.datasource.states

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.states.StatesApiResponse
import com.indusjs.fleet.network.TAG_STATES_REMOTE_DS
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

interface StatesRemoteDataSource : RemoteDataSource {
    suspend fun getStates(token: String): StatesApiResponse
}

@Inject
class StatesRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : StatesRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getStates(token: String): StatesApiResponse {
        return try {
            logger.d(TAG_STATES_REMOTE_DS, "Fetching entity states")
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.STATES}") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                val body = response.bodyAsText()
                json.decodeFromString<StatesApiResponse>(body)
            } else {
                logger.w(TAG_STATES_REMOTE_DS, "States API returned ${response.status}")
                StatesApiResponse(success = false, message = "HTTP ${response.status.value}")
            }
        } catch (e: Exception) {
            logger.e(TAG_STATES_REMOTE_DS, "Failed to fetch states: ${e.message}", e)
            StatesApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }
}
