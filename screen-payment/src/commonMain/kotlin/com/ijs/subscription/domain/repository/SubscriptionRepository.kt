package com.ijs.subscription.domain.repository

import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.OnboardingStatus
import com.ijs.subscription.domain.entity.PaymentOrder
import com.ijs.subscription.domain.entity.PaymentResult
import com.ijs.subscription.domain.entity.Plan

interface SubscriptionRepository {
    suspend fun getOnboardingStatus(): Result<OnboardingStatus>
    suspend fun getPlans(): Result<List<Plan>>
    suspend fun selectPlan(planId: String): Result<Unit>
    suspend fun createPaymentOrder(planId: String, billingInterval: BillingInterval): Result<PaymentOrder>
    suspend fun verifyPayment(orderId: String, providerPaymentId: String, signature: String): Result<PaymentResult>
}
