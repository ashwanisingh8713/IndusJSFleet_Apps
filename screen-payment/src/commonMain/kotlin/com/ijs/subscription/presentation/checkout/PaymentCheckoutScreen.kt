package com.ijs.subscription.presentation.checkout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.Effect
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.Intent
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
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val breakpoint = rememberFleetBreakpoint()
                val contentWidthModifier = when (breakpoint) {
                    FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
                    else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Hero plan banner ─────────────────────────────
                    PlanHeroBanner(
                        plan = plan,
                        billingInterval = billingInterval,
                        onBack = { viewModel.sendIntent(Intent.ChangePlan) },
                        contentWidthModifier = contentWidthModifier
                    )

                    // ── Body content ─────────────────────────────────
                    Column(
                        modifier = contentWidthModifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = FleetTokens.Spacing.XL,
                                vertical = FleetTokens.Spacing.XL
                            ),
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                    ) {

                        // Order summary
                        OrderSummaryCard(plan = plan, billingInterval = billingInterval)

                        // Error card
                        state.error?.let { error ->
                            FleetInlineErrorBanner(message = error.resolve())
                        }

                        // Legal text
                        Text(
                            text = "By proceeding you agree to our Terms of Service and Privacy Policy. You can cancel your subscription anytime.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bottom spacer so content clears the sticky pay button
                        Spacer(Modifier.height(FleetTokens.Spacing.XXXL))
                    }
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
    onBack: () -> Unit,
    contentWidthModifier: Modifier
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
        Column(modifier = contentWidthModifier.fillMaxWidth()) {

            // Back row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = FleetTokens.Spacing.XS,
                        top = FleetTokens.Spacing.S,
                        end = FleetTokens.Spacing.L
                    ),
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
                    .padding(
                        start = FleetTokens.Spacing.XL,
                        end = FleetTokens.Spacing.XL,
                        bottom = FleetTokens.Spacing.XXL
                    ),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                // Billing interval chip
                HeroBadge(text = if (billingInterval == BillingInterval.ANNUAL) "Annual Plan" else "Monthly Plan")

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
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
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
                        modifier = Modifier.padding(bottom = FleetTokens.Spacing.S)
                    )
                }

                // Trial badge
                if (plan.trialDays > 0) {
                    HeroBadge(text = "🎁 ${plan.trialDays}-day free trial included")
                }

                // Annual savings badge
                if (billingInterval == BillingInterval.ANNUAL && plan.annualSavings > 0) {
                    HeroBadge(
                        text = "You save ${plan.formattedAnnualSavings()} annually",
                        containerColor = scheme.tertiaryContainer,
                        contentColor = scheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

/** Pill badge used on the hero banner (translucent fill over the gradient). */
@Composable
private fun HeroBadge(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Surface(
        shape = RoundedCornerShape(FleetTokens.Radius.Pill),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(
                horizontal = FleetTokens.Spacing.M,
                vertical = FleetTokens.Spacing.XS
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Order summary card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OrderSummaryCard(plan: Plan, billingInterval: BillingInterval) {
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = "Order Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(FleetTokens.Spacing.L))

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

        Spacer(Modifier.height(FleetTokens.Spacing.XS))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(FleetTokens.Spacing.XS))

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

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FleetTokens.Spacing.XS),
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
        shadowElevation = FleetTokens.Elevation.Modal,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FleetTokens.Elevation.Raised
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = FleetTokens.Spacing.XL,
                    vertical = FleetTokens.Spacing.L
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val amount = if (billingInterval == BillingInterval.ANNUAL)
                plan.formattedAnnualPrice() else plan.formattedMonthlyPrice()
            val buttonLabel = when {
                isVerifying -> "Verifying payment…"
                isBusy -> "Preparing order…"
                else -> "Pay $amount Securely"
            }
            FleetButton(
                text = buttonLabel,
                onClick = onPay,
                variant = ButtonVariant.PRIMARY,
                enabled = !isBusy,
                isLoading = isBusy,
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_lock),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = FleetTokens.Width.MaxContent)
            )
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
        FleetSectionCard(
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XL),
            border = null,
            elevation = FleetTokens.Elevation.Dialog,
            contentPadding = FleetTokens.Spacing.XXL
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(FleetTokens.IconSize.XL),
                    strokeWidth = FleetTokens.Height.ProgressStroke,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
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
