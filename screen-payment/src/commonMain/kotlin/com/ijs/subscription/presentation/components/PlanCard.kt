package com.ijs.subscription.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_close
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_feature_basic_trip_tracking
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_feature_community_support
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_feature_up_to_5_vehicles
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_per_mo
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_per_month
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_per_year
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_per_yr
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_recommended
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_price_forever
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_price_free
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_selected
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_tap_to_select
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_trial_free
import indusjsfleet.ijs_ui_components_lib.generated.resources.plan_whats_included
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

// Card width for horizontal layout — shows peek of adjacent cards
private val PLAN_CARD_WIDTH = 270.dp

@Composable
fun PlanCard(
    plan: Plan,
    isSelected: Boolean,
    billingInterval: BillingInterval,
    cardIndex: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val isPopular = !plan.isFree && cardIndex == 1

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primary else scheme.outlineVariant,
        animationSpec = tween(220),
        label = "card_border_$cardIndex"
    )
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isSelected) 6.dp else 1.dp,
        animationSpec = tween(220),
        label = "card_elevation_$cardIndex"
    )
    // §1 anti-rainbow / §H money-neutral: no colour band. Selection is the ONLY tint — the card fills
    // with primaryContainer (#E1E4FF) plus a check badge; differentiation is a "Recommended" chip, not
    // a card colour. All copy is neutral onSurface/onSurfaceVariant.
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) scheme.primaryContainer else scheme.surface,
        animationSpec = tween(220),
        label = "card_bg_$cardIndex"
    )

    Card(
        onClick = onSelect,
        modifier = modifier.width(PLAN_CARD_WIDTH),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column {

            // ── Header (neutral) ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Plan name
                    Text(
                        text = plan.name.replaceFirstChar { it.uppercaseChar() },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface
                    )

                    // Selection indicator or Recommended chip
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(scheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check),
                                contentDescription = stringResource(Res.string.plan_selected),
                                tint = scheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (isPopular) {
                        RecommendedChip()
                    }
                }

                if (plan.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Price block (neutral) ─────────────────────────
                PriceBlock(
                    plan = plan,
                    billingInterval = billingInterval
                )

                if (plan.trialDays > 0) {
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = scheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = stringResource(Res.string.plan_trial_free, plan.trialDays),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Feature list ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (plan.features.isNotEmpty() || plan.featureLimits.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.plan_whats_included),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurface,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(Modifier.height(2.dp))

                    plan.features.take(6).forEach { feature ->
                        FeatureRow(feature = feature)
                    }
                    plan.featureLimits.take(5).forEach { limit ->
                        FeatureLimitRow(featureLimit = limit)
                    }
                } else if (plan.isFree) {
                    listOf(
                        stringResource(Res.string.plan_feature_up_to_5_vehicles),
                        stringResource(Res.string.plan_feature_basic_trip_tracking),
                        stringResource(Res.string.plan_feature_community_support)
                    ).forEach { FeatureRow(it) }
                }

                if (!isSelected) {
                    Spacer(Modifier.height(2.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.4f))
                    Text(
                        text = stringResource(Res.string.plan_tap_to_select),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Recommended chip — the ONLY differentiator between plans (never a card tint). A single primary
// accent chip that carries meaning; §1-compliant.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecommendedChip() {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = scheme.primaryContainer
    ) {
        Text(
            text = stringResource(Res.string.plan_recommended),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Price block
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PriceBlock(
    plan: Plan,
    billingInterval: BillingInterval,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    if (plan.isFree) {
        Text(
            text = stringResource(Res.string.plan_price_free),
            style = MaterialTheme.typography.displaySmall,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = scheme.onSurface,
            modifier = modifier
        )
        Text(
            text = stringResource(Res.string.plan_price_forever),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant
        )
        return
    }

    Column(modifier = modifier) {
        when (billingInterval) {
            BillingInterval.MONTHLY -> {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = plan.formattedMonthlyPrice(),
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.plan_per_mo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
            BillingInterval.ANNUAL -> {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = plan.formattedEffectiveMonthly(),
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.plan_per_mo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${plan.formattedAnnualPrice()}${stringResource(Res.string.plan_per_yr)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        textDecoration = TextDecoration.None
                    )
                    if (plan.annualSavings > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = scheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = plan.formattedAnnualSavings(),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PriceDisplay — kept for PaymentCheckoutScreen re-use
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PriceDisplay(
    plan: Plan,
    billingInterval: BillingInterval,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    if (plan.isFree) {
        Text(
            text = stringResource(Res.string.plan_price_free),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
            modifier = modifier
        )
        return
    }

    Column(modifier = modifier) {
        when (billingInterval) {
            BillingInterval.MONTHLY -> {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = plan.formattedMonthlyPrice(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.plan_per_month),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            BillingInterval.ANNUAL -> {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = plan.formattedEffectiveMonthly(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.plan_per_month),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${plan.formattedAnnualPrice()}${stringResource(Res.string.plan_per_year)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    if (plan.annualSavings > 0) {
                        Text(
                            text = plan.formattedAnnualSavings(),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
