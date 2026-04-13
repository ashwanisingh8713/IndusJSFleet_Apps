package com.ijs.subscription.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionApiResponse<T>(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null
)

@Serializable
data class PlansWrapperDto(
    @SerialName("plans") val plans: List<PlanDto> = emptyList()
)

@Serializable
data class PlanDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("monthly_price") val monthlyPrice: Long = 0L,
    @SerialName("annual_price") val annualPrice: Long = 0L,
    @SerialName("discount_percent") val discountPercent: Double = 0.0,
    @SerialName("effective_monthly_price_annual") val effectiveMonthlyPriceAnnual: Long = 0L,
    @SerialName("annual_savings") val annualSavings: Long = 0L,
    @SerialName("currency") val currency: String = "INR",
    @SerialName("trial_days") val trialDays: Int = 0,
    @SerialName("features") val features: List<String> = emptyList(),
    @SerialName("feature_limits") val featureLimits: List<FeatureLimitDto> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class FeatureLimitDto(
    @SerialName("key") val key: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("value") val value: Long = 0L,
    @SerialName("unlimited") val unlimited: Boolean = false
)

@Serializable
data class OnboardingStatusDto(
    @SerialName("step") val step: String = "complete",
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("mobile_verified") val mobileVerified: Boolean = false,
    @SerialName("plan_selected") val planSelected: Boolean = false,
    @SerialName("payment_done") val paymentDone: Boolean = false,
    @SerialName("selected_plan") val selectedPlan: PlanDto? = null,
    @SerialName("payment_required") val paymentRequired: Boolean = false,
    @SerialName("ready_to_create_tenant") val readyToCreateTenant: Boolean = false
)

@Serializable
data class PaymentOrderDto(
    @SerialName("order_id") val orderId: String = "",
    @SerialName("payment_id") val internalPaymentId: String = "",
    @SerialName("amount") val amount: Long = 0L,
    @SerialName("currency") val currency: String = "INR",
    @SerialName("provider_key") val providerKey: String = "",
    @SerialName("provider_name") val providerName: String = "",
    @SerialName("receipt_id") val receiptId: String = "",
    @SerialName("customer_email") val customerEmail: String = "",
    @SerialName("customer_name") val customerName: String = ""
)

@Serializable
data class PaymentVerifyResponseDto(
    @SerialName("payment_id") val paymentId: String = "",
    @SerialName("provider_id") val providerId: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("method") val method: String = "",
    @SerialName("amount") val amount: Long = 0L,
    @SerialName("currency") val currency: String = "",
    @SerialName("plan_name") val planName: String = "",
    @SerialName("subscription_id") val subscriptionId: String = "",
    @SerialName("message") val message: String = ""
)

@Serializable
data class SelectPlanMessageDto(
    @SerialName("message") val message: String = ""
)
