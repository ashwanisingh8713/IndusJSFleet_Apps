package com.ijs.subscription.domain.entity

data class OnboardingStatus(
    val step: OnboardingStep,
    val emailVerified: Boolean,
    val mobileVerified: Boolean,
    val planSelected: Boolean,
    val paymentDone: Boolean,
    val selectedPlan: Plan?,
    val paymentRequired: Boolean,
    val readyToCreateTenant: Boolean
) {
    val needsPlanSelection: Boolean
        get() = step == OnboardingStep.PLAN

    val needsPayment: Boolean
        get() = step == OnboardingStep.PAYMENT || (paymentRequired && !paymentDone)

    val isComplete: Boolean
        get() = step == OnboardingStep.COMPLETE || readyToCreateTenant
}

enum class OnboardingStep(val apiValue: String) {
    VERIFY("verify"),
    PLAN("plan"),
    PAYMENT("payment"),
    COMPLETE("complete");

    companion object {
        fun from(value: String): OnboardingStep =
            entries.firstOrNull { it.apiValue == value } ?: COMPLETE
    }
}
