package com.ijs.subscription.data.repository

import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.subscription.TAG_SUBSCRIPTION_REPO
import com.ijs.subscription.data.datasource.SubscriptionRemoteDataSource
import com.ijs.subscription.data.mapper.SubscriptionMapper.toDomain
import com.ijs.subscription.data.model.CreatePaymentOrderRequest
import com.ijs.subscription.data.model.SelectPlanRequest
import com.ijs.subscription.data.model.VerifyPaymentRequest
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.OnboardingStatus
import com.ijs.subscription.domain.entity.PaymentOrder
import com.ijs.subscription.domain.entity.PaymentResult
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.domain.repository.SubscriptionRepository

class SubscriptionRepositoryImpl(
    private val remoteDataSource: SubscriptionRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : SubscriptionRepository {

    private suspend fun requireToken(): String =
        userLocalDataSource.getAuthToken()
            ?: throw ApiException("Not authenticated. Please log in again.")

    override suspend fun getOnboardingStatus(): Result<OnboardingStatus> = runCatching {
        val token = requireToken()
        val response = remoteDataSource.getOnboardingStatus(token)
        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to get onboarding status")
    }.also { result ->
        result.onFailure { logger.e(TAG_SUBSCRIPTION_REPO, "getOnboardingStatus failed: ${it.message}", it) }
    }

    override suspend fun getPlans(): Result<List<Plan>> = runCatching {
        val response = remoteDataSource.getPlans()
        response.data?.plans?.map { it.toDomain() }
            ?: throw ApiException(response.message ?: "Failed to load plans")
    }.also { result ->
        result.onFailure { logger.e(TAG_SUBSCRIPTION_REPO, "getPlans failed: ${it.message}", it) }
    }

    override suspend fun selectPlan(planId: String): Result<Unit> = runCatching {
        val token = requireToken()
        remoteDataSource.selectPlan(token, SelectPlanRequest(planId = planId))
        Unit
    }.also { result ->
        result.onFailure { logger.e(TAG_SUBSCRIPTION_REPO, "selectPlan failed: ${it.message}", it) }
    }

    override suspend fun createPaymentOrder(
        planId: String,
        billingInterval: BillingInterval
    ): Result<PaymentOrder> = runCatching {
        val token = requireToken()
        val response = remoteDataSource.createPaymentOrder(
            token,
            CreatePaymentOrderRequest(
                planId = planId,
                billingInterval = billingInterval.apiValue
            )
        )
        val dto = response.data
        if (dto == null) {
            val fromServer = response.message?.takeIf { it.isNotBlank() }
            val logHint =
                "createPaymentOrder: success=${response.success}, data=null. " +
                    "POST ${ApiConfig.BASE_URL}${ApiConfig.Endpoints.PAYMENT_ORDERS} must return " +
                    """{"success":true,"data":{"order_id":"…","amount":…,"currency":"INR","provider_key":"…",…}}."""
            logger.e(TAG_SUBSCRIPTION_REPO, "$logHint ${fromServer?.let { "apiMessage=$it" } ?: ""}")
            throw ApiException(
                fromServer
                    ?: "Failed to create payment order — server returned no order (empty \"data\"). " +
                    "Confirm your backend implements POST ${ApiConfig.Endpoints.PAYMENT_ORDERS} and Razorpay order creation."
            )
        }
        val order = dto.toDomain()
        if (order.orderId.isBlank()) {
            logger.e(TAG_SUBSCRIPTION_REPO, "createPaymentOrder: data present but order_id is blank")
            throw ApiException(
                response.message?.takeIf { it.isNotBlank() }
                    ?: "Invalid payment order: order_id is missing. Backend must return Razorpay order_id in data.order_id."
            )
        }
        order
    }.also { result ->
        result.onFailure { logger.e(TAG_SUBSCRIPTION_REPO, "createPaymentOrder failed: ${it.message}", it) }
    }

    override suspend fun verifyPayment(
        orderId: String,
        providerPaymentId: String,
        signature: String
    ): Result<PaymentResult> = runCatching {
        val token = requireToken()
        val response = remoteDataSource.verifyPayment(
            token,
            VerifyPaymentRequest(
                orderId = orderId,
                providerPaymentId = providerPaymentId,
                signature = signature
            )
        )
        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Payment verification failed")
    }.also { result ->
        result.onFailure { logger.e(TAG_SUBSCRIPTION_REPO, "verifyPayment failed: ${it.message}", it) }
    }
}
