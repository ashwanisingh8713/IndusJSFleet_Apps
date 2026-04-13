package com.ijs.subscription.presentation.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.UiText
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.Effect
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.Intent
import com.ijs.subscription.presentation.components.PriceDisplay
import com.ijs.subscription.presentation.platform.RazorpayLauncher
import com.ijs.subscription.presentation.platform.RazorpayResult
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_arrow_back
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_lock
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

@Composable
fun PaymentCheckoutScreen(
    viewModel: PaymentCheckoutViewModel,
    plan: Plan,
    billingInterval: BillingInterval,
    razorpayLauncher: RazorpayLauncher,
    onPaymentSuccess: (planName: String, amount: Long, currency: String) -> Unit,
    onNavigateBackToPlans: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(plan, billingInterval) {
        viewModel.sendIntent(Intent.Initialize(plan, billingInterval))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Effect.LaunchRazorpayCheckout -> {
                    razorpayLauncher(effect.data) { result ->
                        when (result) {
                            is RazorpayResult.Success ->
                                viewModel.sendIntent(
                                    Intent.RazorpaySuccess(result.orderId, result.paymentId, result.signature)
                                )
                            is RazorpayResult.Failed ->
                                viewModel.sendIntent(
                                    Intent.RazorpayFailed(result.errorCode, result.description)
                                )
                            is RazorpayResult.Cancelled ->
                                viewModel.sendIntent(Intent.RazorpayCancelled)
                        }
                    }
                }
                is Effect.NavigateToSuccess ->
                    onPaymentSuccess(effect.planName, effect.amount, effect.currency)
                is Effect.NavigateBackToPlans -> onNavigateBackToPlans()
                is Effect.ShowError -> pendingSnackbar = effect.message
            }
        }
    }

    pendingSnackbar?.let { msg ->
        val resolved = msg.resolve()
        LaunchedEffect(resolved) {
            snackbarHostState.showSnackbar(resolved)
            pendingSnackbar = null
        }
    }

    val isBusy = state.isCreatingOrder || state.isVerifyingPayment

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Main scrollable content ──────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // ── Hero plan banner ─────────────────────────────
                PlanHeroBanner(
                    plan = plan,
                    billingInterval = billingInterval,
                    onBack = { viewModel.sendIntent(Intent.ChangePlan) }
                )

                // ── Body content ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Order summary
                    OrderSummaryCard(plan = plan, billingInterval = billingInterval)

                    // Error card
                    state.error?.let { error ->
                        ErrorCard(message = error.resolve())
                    }

                    // Legal text
                    Text(
                        text = "By proceeding you agree to our Terms of Service and Privacy Policy. You can cancel your subscription anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 18.sp
                    )

                    // Bottom spacer so content clears the sticky pay button
                    Spacer(Modifier.height(80.dp))
                }
            }

            // ── Sticky pay button ────────────────────────────────
            PayButton(
                plan = plan,
                billingInterval = billingInterval,
                isBusy = isBusy,
                isVerifying = state.isVerifyingPayment,
                onPay = { viewModel.sendIntent(Intent.InitiatePayment) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // ── Full-screen busy overlay ─────────────────────────
            AnimatedVisibility(
                visible = isBusy,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                BusyOverlay(isVerifying = state.isVerifyingPayment)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero plan banner
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanHeroBanner(
    plan: Plan,
    billingInterval: BillingInterval,
    onBack: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(scheme.primary, scheme.primaryContainer)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Back row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = "Back to plans",
                        tint = scheme.onPrimary
                    )
                }
                Text(
                    text = "Change plan",
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onPrimary.copy(alpha = 0.80f)
                )
            }

            // Plan summary
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Billing interval chip
                Surface(
                    shape = RoundedCornerShape(50),
                    color = scheme.onPrimary.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = if (billingInterval == BillingInterval.ANNUAL) "Annual Plan" else "Monthly Plan",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Plan name
                Text(
                    text = plan.name.replaceFirstChar { it.uppercaseChar() },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.onPrimary
                )

                // Price
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val price = if (billingInterval == BillingInterval.ANNUAL)
                        plan.formattedAnnualPrice()
                    else
                        plan.formattedMonthlyPrice()
                    val period = if (billingInterval == BillingInterval.ANNUAL) "/year" else "/month"

                    Text(
                        text = price,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onPrimary
                    )
                    Text(
                        text = period,
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onPrimary.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Trial badge
                if (plan.trialDays > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = scheme.onPrimary.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "🎁 ${plan.trialDays}-day free trial included",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onPrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Annual savings badge
                if (billingInterval == BillingInterval.ANNUAL && plan.annualSavings > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = scheme.tertiary.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "You save ${plan.formattedAnnualSavings()} annually",
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Order summary card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OrderSummaryCard(plan: Plan, billingInterval: BillingInterval) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            SummaryRow(
                label = "Plan",
                value = plan.name.replaceFirstChar { it.uppercaseChar() }
            )
            SummaryRow(
                label = "Billing",
                value = billingInterval.label
            )

            if (plan.trialDays > 0) {
                SummaryRow(
                    label = "Free trial",
                    value = "${plan.trialDays} days",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(4.dp))

            // Total amount — highlighted row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(horizontalAlignment = Alignment.End) {
                    val amount = if (billingInterval == BillingInterval.ANNUAL)
                        plan.formattedAnnualPrice() else plan.formattedMonthlyPrice()
                    val period = if (billingInterval == BillingInterval.ANNUAL) "/year" else "/month"
                    Text(
                        text = "$amount$period",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (billingInterval == BillingInterval.ANNUAL && plan.annualSavings > 0) {
                        Text(
                            text = "Save ${plan.formattedAnnualSavings()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Error card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorCard(message: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "⚠",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                lineHeight = 18.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sticky pay button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PayButton(
    plan: Plan,
    billingInterval: BillingInterval,
    isBusy: Boolean,
    isVerifying: Boolean,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onPay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !isBusy,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (isVerifying) "Verifying payment…" else "Preparing order…",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    val amount = if (billingInterval == BillingInterval.ANNUAL)
                        plan.formattedAnnualPrice() else plan.formattedMonthlyPrice()
                    Icon(
                        painter = painterResource(Res.drawable.ic_lock),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Pay $amount Securely",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Full-screen busy overlay
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BusyOverlay(isVerifying: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(44.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isVerifying) "Verifying Payment" else "Preparing Order",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isVerifying)
                            "Confirming your payment with Razorpay…"
                        else
                            "Setting up your secure checkout…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
