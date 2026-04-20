package com.ijs.subscription.presentation.plans

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan

/**
 * Determines the hero copy and CTA tone on the Plans screen.
 *
 * [CHOOSE] — first-time user picking a plan.
 * [COMPLETE_PAYMENT] — user selected a plan but hasn't paid yet.
 */
enum class PlanPageMode { CHOOSE, COMPLETE_PAYMENT }

object PlansContract {

    data class State(
        val isLoading: Boolean = true,
        val plans: List<Plan> = emptyList(),
        val selectedPlan: Plan? = null,
        val billingInterval: BillingInterval = BillingInterval.MONTHLY,
        val preSelectedPlan: Plan? = null,
        val isConfirming: Boolean = false,
        val error: UiText? = null,
        val pageMode: PlanPageMode = PlanPageMode.CHOOSE
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadPlans : Intent
        data class SelectPlan(val plan: Plan) : Intent
        data class ChangeBillingInterval(val interval: BillingInterval) : Intent
        data object ConfirmPlanSelection : Intent
        data object RetryLoad : Intent
        data object Logout : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToPaymentCheckout(
            val plan: Plan,
            val billingInterval: BillingInterval
        ) : Effect
        data object NavigateToDashboard : Effect
        data object NavigateToLogin : Effect
        data class ShowError(val message: UiText) : Effect
    }
}
