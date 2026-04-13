package com.ijs.subscription.presentation.plans

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.components.BillingToggle
import com.ijs.subscription.presentation.components.PlanCard
import com.ijs.subscription.presentation.plans.PlansContract.Effect
import com.ijs.subscription.presentation.plans.PlansContract.Intent
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlansScreen(
    viewModel: PlansViewModel,
    onNavigateToPayment: (Plan, BillingInterval) -> Unit,
    onNavigateToDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is Effect.NavigateToPaymentCheckout ->
                    onNavigateToPayment(effect.plan, effect.billingInterval)
                is Effect.NavigateToDashboard -> onNavigateToDashboard()
                is Effect.NavigateToLogin -> onLogout()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> LoadingContent(message = "Loading plans…")

                state.error != null && state.plans.isEmpty() -> ErrorContent(
                    error = state.error!!.resolve(),
                    onRetry = { viewModel.sendIntent(Intent.RetryLoad) }
                )

                else -> PlansContent(
                    state = state,
                    onSelectPlan = { viewModel.sendIntent(Intent.SelectPlan(it)) },
                    onToggleBilling = { viewModel.sendIntent(Intent.ChangeBillingInterval(it)) },
                    onConfirm = { viewModel.sendIntent(Intent.ConfirmPlanSelection) },
                    onLogout = { viewModel.sendIntent(Intent.Logout) }
                )
            }
        }
    }
}

@Composable
private fun PlansContent(
    state: PlansContract.State,
    onSelectPlan: (Plan) -> Unit,
    onToggleBilling: (BillingInterval) -> Unit,
    onConfirm: () -> Unit,
    onLogout: () -> Unit
) {
    val listState = rememberLazyListState()

    // Track which card is most visible (centred)
    val centredIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf 0
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }?.index ?: 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Scrollable body ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // ── Hero header ──────────────────────────────────────
            HeroHeader(
                isRenewal = state.isRenewal,
                onLogout = onLogout
            )

            Spacer(Modifier.height(24.dp))

            // ── Billing toggle ───────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                BillingToggle(
                    selected = state.billingInterval,
                    onToggle = onToggleBilling
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Horizontal plan cards ────────────────────────────
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(state.plans, key = { _, plan -> plan.id }) { index, plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = state.selectedPlan?.id == plan.id,
                        billingInterval = state.billingInterval,
                        cardIndex = index,
                        onSelect = { onSelectPlan(plan) }
                    )
                }
            }

            // ── Page dots ────────────────────────────────────────
            if (state.plans.size > 1) {
                Spacer(Modifier.height(16.dp))
                PageDots(
                    count = state.plans.size,
                    current = centredIndex,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Trust badges ─────────────────────────────────────
            TrustStrip(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            // Bottom space so content clears the sticky CTA
            Spacer(Modifier.height(100.dp))
        }

        // ── Sticky bottom CTA ────────────────────────────────────
        BottomCtaBar(
            selectedPlan = state.selectedPlan,
            billingInterval = state.billingInterval,
            isConfirming = state.isConfirming,
            onConfirm = onConfirm,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(isRenewal: Boolean, onLogout: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryVariant = MaterialTheme.colorScheme.primaryContainer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(primary, primaryVariant)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Logout action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onLogout) {
                    Text(
                        text = "Log out",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }

            // Headline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = if (isRenewal) "Renew Your Plan" else "Choose Your Plan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (isRenewal)
                        "Your subscription has expired.\nPick a plan to continue managing your fleet."
                    else
                        "Powerful fleet management tools.\nCancel anytime. No hidden fees.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Page dots
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PageDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { idx ->
            val isActive = idx == current
            val dotColor by animateColorAsState(
                targetValue = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                animationSpec = tween(200),
                label = "dot_color_$idx"
            )
            val dotWidth by androidx.compose.animation.core.animateDpAsState(
                targetValue = if (isActive) 20.dp else 6.dp,
                animationSpec = tween(200),
                label = "dot_width_$idx"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(dotWidth)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trust strip
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrustStrip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrustItem(label = "256-bit SSL")
            TrustDivider()
            TrustItem(label = "UPI & Cards")
            TrustDivider()
            TrustItem(label = "Cancel anytime")
        }
    }
}

@Composable
private fun TrustItem(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrustDivider() {
    HorizontalDivider(
        modifier = Modifier
            .height(14.dp)
            .width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Sticky bottom CTA
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BottomCtaBar(
    selectedPlan: Plan?,
    billingInterval: BillingInterval,
    isConfirming: Boolean,
    onConfirm: () -> Unit,
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
            if (selectedPlan != null) {
                val price = when {
                    selectedPlan.isFree -> "Free"
                    billingInterval == BillingInterval.ANNUAL ->
                        "${selectedPlan.formattedAnnualPrice()}/year"
                    else ->
                        "${selectedPlan.formattedMonthlyPrice()}/month"
                }
                Text(
                    text = "${selectedPlan.name.replaceFirstChar { it.uppercaseChar() }} · $price",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = selectedPlan != null && !isConfirming,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                if (isConfirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Please wait…", style = MaterialTheme.typography.labelLarge)
                } else if (selectedPlan == null) {
                    Text(
                        "Select a plan to continue",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else if (selectedPlan.isFree) {
                    Text(
                        "Activate Free Plan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        "Continue with ${selectedPlan.name.replaceFirstChar { it.uppercaseChar() }}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
