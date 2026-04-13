package com.ijs.subscription.data.mapper

import com.ijs.subscription.data.model.FeatureLimitDto
import com.ijs.subscription.data.model.OnboardingStatusDto
import com.ijs.subscription.data.model.PaymentOrderDto
import com.ijs.subscription.data.model.PaymentVerifyResponseDto
import com.ijs.subscription.data.model.PlanDto
import com.ijs.subscription.domain.entity.FeatureLimit
import com.ijs.subscription.domain.entity.OnboardingStatus
import com.ijs.subscription.domain.entity.OnboardingStep
import com.ijs.subscription.domain.entity.PaymentOrder
import com.ijs.subscription.domain.entity.PaymentResult
import com.ijs.subscription.domain.entity.Plan

object SubscriptionMapper {

    fun PlanDto.toDomain(): Plan = Plan(
        id = id,
        name = name,
        description = description,
        monthlyPrice = monthlyPrice,
        annualPrice = annualPrice,
        discountPercent = discountPercent,
        effectiveMonthlyPriceAnnual = effectiveMonthlyPriceAnnual,
        annualSavings = annualSavings,
        currency = currency,
        trialDays = trialDays,
        features = features,
        featureLimits = featureLimits.map { it.toDomain() },
        isActive = isActive
    )

    fun FeatureLimitDto.toDomain(): FeatureLimit = FeatureLimit(
        key = key,
        label = label,
        value = value,
        unlimited = unlimited
    )

    fun OnboardingStatusDto.toDomain(): OnboardingStatus = OnboardingStatus(
        step = OnboardingStep.from(step),
        emailVerified = emailVerified,
        mobileVerified = mobileVerified,
        planSelected = planSelected,
        paymentDone = paymentDone,
        selectedPlan = selectedPlan?.toDomain(),
        paymentRequired = paymentRequired,
        readyToCreateTenant = readyToCreateTenant
    )

    fun PaymentOrderDto.toDomain(): PaymentOrder = PaymentOrder(
        orderId = orderId,
        internalPaymentId = internalPaymentId,
        amount = amount,
        currency = currency,
        providerKey = providerKey,
        providerName = providerName,
        receiptId = receiptId,
        customerEmail = customerEmail,
        customerName = customerName
    )

    fun PaymentVerifyResponseDto.toDomain(): PaymentResult = PaymentResult(
        internalPaymentId = paymentId,
        providerPaymentId = providerId,
        status = status,
        method = method,
        amount = amount,
        currency = currency,
        planName = planName,
        subscriptionId = subscriptionId,
        message = message
    )
}
