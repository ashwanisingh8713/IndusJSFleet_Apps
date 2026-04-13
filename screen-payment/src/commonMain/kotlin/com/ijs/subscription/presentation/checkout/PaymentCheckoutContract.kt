package com.ijs.subscription.presentation.checkout

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.PaymentOrder
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.platform.RazorpayCheckoutData

object PaymentCheckoutContract {

    data class State(
        val plan: Plan? = null,
        val billingInterval: BillingInterval = BillingInterval.MONTHLY,
        val isCreatingOrder: Boolean = false,
        val isVerifyingPayment: Boolean = false,
        val currentOrder: PaymentOrder? = null,
        val error: UiText? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Initialize(val plan: Plan, val interval: BillingInterval) : Intent
        data object InitiatePayment : Intent
        data class RazorpaySuccess(
            val orderId: String,
            val providerPaymentId: String,
            val signature: String
        ) : Intent
        data class RazorpayFailed(val errorCode: Int, val errorDescription: String) : Intent
        data object RazorpayCancelled : Intent
        data object ChangePlan : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data class LaunchRazorpayCheckout(val data: RazorpayCheckoutData) : Effect
        data class NavigateToSuccess(
            val planName: String,
            val amount: Long,
            val currency: String
        ) : Effect
        data object NavigateBackToPlans : Effect
        data class ShowError(val message: UiText) : Effect
    }
}
