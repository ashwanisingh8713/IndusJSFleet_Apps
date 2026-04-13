package com.ijs.subscription.domain.entity

data class PaymentResult(
    val internalPaymentId: String,
    val providerPaymentId: String,
    val status: String,
    val method: String,
    val amount: Long,
    val currency: String,
    val planName: String,
    val subscriptionId: String,
    val message: String
)
