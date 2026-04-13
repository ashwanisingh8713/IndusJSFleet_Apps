package com.ijs.subscription.presentation

import androidx.compose.runtime.Composable
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.checkout.PaymentCheckoutScreen
import com.ijs.subscription.presentation.checkout.PaymentCheckoutViewModel
import com.ijs.subscription.presentation.plans.PlansScreen
import com.ijs.subscription.presentation.plans.PlansViewModel
import com.ijs.subscription.presentation.platform.RazorpayLauncher
import com.ijs.subscription.presentation.success.PaymentSuccessScreen

object SubscriptionFeatureFacade {

    @Composable
    fun PlansEntry(
        viewModel: PlansViewModel,
        onNavigateToPayment: (plan: Plan, interval: BillingInterval) -> Unit,
        onNavigateToDashboard: () -> Unit,
        onLogout: () -> Unit
    ) {
        PlansScreen(
            viewModel = viewModel,
            onNavigateToPayment = onNavigateToPayment,
            onNavigateToDashboard = onNavigateToDashboard,
            onLogout = onLogout
        )
    }

    @Composable
    fun PaymentCheckoutEntry(
        viewModel: PaymentCheckoutViewModel,
        plan: Plan,
        billingInterval: BillingInterval,
        razorpayLauncher: RazorpayLauncher,
        onPaymentSuccess: (planName: String, amount: Long, currency: String) -> Unit,
        onNavigateBackToPlans: () -> Unit
    ) {
        PaymentCheckoutScreen(
            viewModel = viewModel,
            plan = plan,
            billingInterval = billingInterval,
            razorpayLauncher = razorpayLauncher,
            onPaymentSuccess = onPaymentSuccess,
            onNavigateBackToPlans = onNavigateBackToPlans
        )
    }

    @Composable
    fun PaymentSuccessEntry(
        planName: String,
        amount: Long,
        currency: String,
        onContinue: () -> Unit
    ) {
        PaymentSuccessScreen(
            planName = planName,
            amount = amount,
            currency = currency,
            onContinue = onContinue
        )
    }
}
