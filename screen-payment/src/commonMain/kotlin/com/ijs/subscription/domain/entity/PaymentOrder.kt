package com.ijs.subscription.domain.entity

data class PaymentOrder(
    val orderId: String,
    val internalPaymentId: String,
    val amount: Long,
    val currency: String,
    val providerKey: String,
    val providerName: String,
    val receiptId: String,
    val customerEmail: String,
    val customerName: String
)
