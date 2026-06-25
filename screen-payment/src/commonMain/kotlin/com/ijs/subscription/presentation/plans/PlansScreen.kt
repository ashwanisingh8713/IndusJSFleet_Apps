package com.ijs.subscription.presentation.plans

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.components.BillingToggle
import com.ijs.subscription.presentation.components.PlanCard
import com.ijs.subscription.presentation.plans.PlansContract.Effect
import com.ijs.subscription.presentation.plans.PlansContract.Intent
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check
import kotlinx.coroutines.flow.collectLatest
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

/** Max width the centered plan-card grid is constrained to on wide screens. */
private val CONTENT_MAX_WIDTH = 720.dp

@Composable
private fun PlansContent(
    state: PlansContract.State,
    onSelectPlan: (Plan) -> Unit,
    onToggleBilling: (BillingInterval) -> Unit,
    onConfirm: () -> Unit,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Scrollable body ──────────────────────────────────────
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val breakpoint = rememberFleetBreakpoint()
            val isWide = breakpoint.isAtLeastMedium

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                // ── Hero header ──────────────────────────────────
                HeroHeader(
                    pageMode = state.pageMode,
                    onLogout = onLogout
                )

                Spacer(Modifier.height(FleetTokens.Spacing.XL))

                // ── Billing toggle ───────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    BillingToggle(
                        selected = state.billingInterval,
                        onToggle = onToggleBilling
                    )
                }

                Spacer(Modifier.height(FleetTokens.Spacing.XL))

                // ── Plan cards ───────────────────────────────────
                // Compact: single-focus horizontal carousel with peek + dots.
                // Medium/Expanded: centered 2-up grid (no carousel).
                if (isWide) {
                    PlanCardGrid(
                        state = state,
                        onSelectPlan = onSelectPlan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = CONTENT_MAX_WIDTH)
                            .align(Alignment.CenterHorizontally)
                            .padding(horizontal = FleetTokens.Spacing.ScreenHorizontal)
                    )
                } else {
                    PlanCardCarousel(
                        state = state,
                        onSelectPlan = onSelectPlan
                    )
                }

                Spacer(Modifier.height(FleetTokens.Spacing.XL))

                // ── Trust badges ─────────────────────────────────
                TrustStrip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = CONTENT_MAX_WIDTH)
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = FleetTokens.Spacing.ScreenHorizontal)
                )

                // Bottom space so content clears the sticky CTA
                Spacer(Modifier.height(FleetTokens.Spacing.XXXL * 2))
            }
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
// Plan cards — compact carousel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PlanCardCarousel(
    state: PlansContract.State,
    onSelectPlan: (Plan) -> Unit
) {
    val listState = rememberLazyListState()

    // Auto-scroll to the initially selected plan so it's visible on load
    val selectedPlanId = state.selectedPlan?.id
    LaunchedEffect(selectedPlanId, state.plans) {
        if (selectedPlanId != null && state.plans.isNotEmpty()) {
            val idx = state.plans.indexOfFirst { it.id == selectedPlanId }
            if (idx > 0) listState.animateScrollToItem(idx)
        }
    }

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

    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = FleetTokens.Spacing.XL),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
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

        if (state.plans.size > 1) {
            Spacer(Modifier.height(FleetTokens.Spacing.L))
            PageDots(
                count = state.plans.size,
                current = centredIndex,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Plan cards — wide 2-up grid
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlanCardGrid(
    state: PlansContract.State,
    onSelectPlan: (Plan) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            FleetTokens.Spacing.L,
            Alignment.CenterHorizontally
        ),
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L),
        maxItemsInEachRow = 2
    ) {
        state.plans.forEachIndexed { index, plan ->
            PlanCard(
                plan = plan,
                isSelected = state.selectedPlan?.id == plan.id,
                billingInterval = state.billingInterval,
                cardIndex = index,
                onSelect = { onSelectPlan(plan) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hero header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(pageMode: PlanPageMode, onLogout: () -> Unit) {
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
                    .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.S),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onLogout) {
                    Text(
                        text = "Log out",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }

            val (headline, subtitle) = when (pageMode) {
                PlanPageMode.CHOOSE -> "Choose Your Plan" to
                        "Powerful fleet management tools.\nCancel anytime. No hidden fees."
                PlanPageMode.COMPLETE_PAYMENT -> "Complete Your Purchase" to
                        "You're almost there!\nComplete payment to start managing your fleet."
            }

            // Headline
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = FleetTokens.Spacing.XL,
                        end = FleetTokens.Spacing.XL,
                        bottom = FleetTokens.Spacing.XXL
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(FleetTokens.Spacing.S))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f)
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
                targetValue = if (isActive) FleetTokens.Spacing.XL else FleetTokens.Spacing.S,
                animationSpec = tween(200),
                label = "dot_width_$idx"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = FleetTokens.Spacing.XXS)
                    .height(FleetTokens.Spacing.S)
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
        shape = RoundedCornerShape(FleetTokens.Radius.L),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = FleetTokens.Elevation.None
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.M),
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
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(FleetTokens.IconSize.XS)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TrustDivider() {
    HorizontalDivider(
        modifier = Modifier
            .height(FleetTokens.Spacing.L)
            .width(FleetTokens.Height.Divider),
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
        shadowElevation = FleetTokens.Elevation.Modal,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FleetTokens.Elevation.Raised
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = CONTENT_MAX_WIDTH)
                .navigationBarsPadding()
                .padding(horizontal = FleetTokens.Spacing.XL, vertical = FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
            horizontalAlignment = Alignment.CenterHorizontally
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

            val ctaLabel = when {
                isConfirming -> "Please wait…"
                selectedPlan == null -> "Select a plan to continue"
                selectedPlan.isFree -> "Activate Free Plan"
                else -> "Continue with ${selectedPlan.name.replaceFirstChar { it.uppercaseChar() }}"
            }

            FleetButton(
                text = ctaLabel,
                onClick = onConfirm,
                size = ButtonSize.LARGE,
                enabled = selectedPlan != null,
                isLoading = isConfirming,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
