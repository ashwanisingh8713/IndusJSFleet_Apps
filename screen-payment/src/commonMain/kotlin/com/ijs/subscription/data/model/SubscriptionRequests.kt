package com.ijs.subscription.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectPlanRequest(
    @SerialName("plan_id") val planId: String
)

@Serializable
data class CreatePaymentOrderRequest(
    @SerialName("plan_id") val planId: String,
    @SerialName("billing_interval") val billingInterval: String
)

@Serializable
data class VerifyPaymentRequest(
    @SerialName("order_id") val orderId: String,
    @SerialName("provider_payment_id") val providerPaymentId: String,
    @SerialName("signature") val signature: String
)
