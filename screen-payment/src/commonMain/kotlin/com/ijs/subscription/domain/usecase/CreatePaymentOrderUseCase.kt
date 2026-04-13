package com.ijs.subscription.domain.usecase

import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.PaymentOrder
import com.ijs.subscription.domain.repository.SubscriptionRepository

class CreatePaymentOrderUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(planId: String, interval: BillingInterval): Result<PaymentOrder> =
        repository.createPaymentOrder(planId, interval)
}
