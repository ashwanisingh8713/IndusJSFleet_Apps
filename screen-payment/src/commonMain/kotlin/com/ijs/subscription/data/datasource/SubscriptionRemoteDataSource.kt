package com.ijs.subscription.data.datasource

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.network.ApiConfig
import com.ijs.subscription.TAG_SUBSCRIPTION_REMOTE_DS
import com.ijs.subscription.data.model.CreatePaymentOrderRequest
import com.ijs.subscription.data.model.OnboardingStatusDto
import com.ijs.subscription.data.model.PaymentOrderDto
import com.ijs.subscription.data.model.PaymentVerifyResponseDto
import com.ijs.subscription.data.model.PlansWrapperDto
import com.ijs.subscription.data.model.SelectPlanMessageDto
import com.ijs.subscription.data.model.SelectPlanRequest
import com.ijs.subscription.data.model.SubscriptionApiResponse
import com.ijs.subscription.data.model.VerifyPaymentRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface SubscriptionRemoteDataSource {
    suspend fun getOnboardingStatus(token: String): SubscriptionApiResponse<OnboardingStatusDto>
    suspend fun getPlans(): SubscriptionApiResponse<PlansWrapperDto>
    suspend fun selectPlan(token: String, request: SelectPlanRequest): SubscriptionApiResponse<SelectPlanMessageDto>
    suspend fun createPaymentOrder(token: String, request: CreatePaymentOrderRequest): SubscriptionApiResponse<PaymentOrderDto>
    suspend fun verifyPayment(token: String, request: VerifyPaymentRequest): SubscriptionApiResponse<PaymentVerifyResponseDto>
}

class SubscriptionRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : SubscriptionRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL

    override suspend fun getOnboardingStatus(token: String): SubscriptionApiResponse<OnboardingStatusDto> {
        logger.d(TAG_SUBSCRIPTION_REMOTE_DS, "GET onboarding status")
        return httpClient.get("$baseUrl${ApiConfig.Endpoints.ONBOARDING_STATUS}") {
            bearerAuth(token)
        }.body()
    }

    override suspend fun getPlans(): SubscriptionApiResponse<PlansWrapperDto> {
        logger.d(TAG_SUBSCRIPTION_REMOTE_DS, "GET plans")
        return httpClient.get("$baseUrl${ApiConfig.Endpoints.SUBSCRIPTION_PLANS}").body()
    }

    override suspend fun selectPlan(
        token: String,
        request: SelectPlanRequest
    ): SubscriptionApiResponse<SelectPlanMessageDto> {
        logger.d(TAG_SUBSCRIPTION_REMOTE_DS, "POST select plan: ${request.planId}")
        return httpClient.post("$baseUrl${ApiConfig.Endpoints.SELECT_PLAN}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun createPaymentOrder(
        token: String,
        request: CreatePaymentOrderRequest
    ): SubscriptionApiResponse<PaymentOrderDto> {
        logger.d(TAG_SUBSCRIPTION_REMOTE_DS, "POST create order for plan: ${request.planId}")
        return httpClient.post("$baseUrl${ApiConfig.Endpoints.PAYMENT_ORDERS}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verifyPayment(
        token: String,
        request: VerifyPaymentRequest
    ): SubscriptionApiResponse<PaymentVerifyResponseDto> {
        logger.d(TAG_SUBSCRIPTION_REMOTE_DS, "POST verify payment order: ${request.orderId}")
        return httpClient.post("$baseUrl${ApiConfig.Endpoints.PAYMENT_VERIFY}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
