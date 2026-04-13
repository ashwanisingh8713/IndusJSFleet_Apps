package com.ijs.subscription.domain.usecase

import com.ijs.subscription.domain.entity.PaymentResult
import com.ijs.subscription.domain.repository.SubscriptionRepository

class VerifyPaymentUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(
        orderId: String,
        providerPaymentId: String,
        signature: String
    ): Result<PaymentResult> =
        repository.verifyPayment(orderId, providerPaymentId, signature)
}
