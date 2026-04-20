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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.Plan
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_close
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

    // Resolved accent colours per card index
    val (accentStart, accentEnd) = when (cardIndex % 3) {
        0 -> scheme.secondaryContainer to scheme.secondary.copy(alpha = 0.6f)
        1 -> scheme.primary to scheme.primaryContainer.copy(alpha = 0.8f)
        else -> scheme.tertiary to scheme.tertiaryContainer.copy(alpha = 0.8f)
    }
    val accentContent = when (cardIndex % 3) {
        0 -> scheme.onSecondaryContainer
        1 -> scheme.onPrimary
        else -> scheme.onTertiary
    }

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

    Card(
        onClick = onSelect,
        modifier = modifier.width(PLAN_CARD_WIDTH),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = scheme.surface)
    ) {
        Column {

            // ── Coloured header band ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(accentStart, accentEnd))
                    )
                    .padding(16.dp)
            ) {
                Column {
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
                            color = accentContent
                        )

                        // Selection indicator or Popular badge
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(scheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check),
                                    contentDescription = "Selected",
                                    tint = scheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else if (isPopular) {
                            PopularBadge(accentContent = accentContent)
                        }
                    }

                    if (plan.description.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentContent.copy(alpha = 0.80f),
                            lineHeight = 18.sp
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // ── Price block inside header ─────────────────
                    PriceBlock(
                        plan = plan,
                        billingInterval = billingInterval,
                        textColor = accentContent
                    )

                    if (plan.trialDays > 0) {
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = scheme.surface.copy(alpha = 0.20f)
                        ) {
                            Text(
                                text = "🎁 ${plan.trialDays}-day free trial",
                                style = MaterialTheme.typography.labelSmall,
                                color = accentContent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── Feature list ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = if (isSelected) 0.dp else 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (plan.features.isNotEmpty() || plan.featureLimits.isNotEmpty()) {
                    Text(
                        text = "What's included",
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
                        "Up to 5 vehicles",
                        "Basic trip tracking",
                        "Community support"
                    ).forEach { FeatureRow(it) }
                }

                if (!isSelected) {
                    Spacer(Modifier.height(2.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.4f))
                    Text(
                        text = "Tap to select",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // ── Full-width selected banner at card bottom ─────
            if (isSelected) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = scheme.primary)
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = null,
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Popular badge — top-right corner ribbon
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PopularBadge(accentContent: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = accentContent.copy(alpha = 0.18f)
    ) {
        Text(
            text = "★ Popular",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentContent,
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
    textColor: Color,
    modifier: Modifier = Modifier
) {
    if (plan.isFree) {
        Text(
            text = "Free",
            style = MaterialTheme.typography.displaySmall,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = modifier
        )
        Text(
            text = "forever",
            style = MaterialTheme.typography.bodySmall,
            color = textColor.copy(alpha = 0.70f)
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
                        color = textColor
                    )
                    Text(
                        text = "/mo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.75f),
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
                        color = textColor
                    )
                    Text(
                        text = "/mo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${plan.formattedAnnualPrice()}/yr",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.65f),
                        textDecoration = TextDecoration.None
                    )
                    if (plan.annualSavings > 0) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = textColor.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = plan.formattedAnnualSavings(),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
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
            text = "Free",
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
                        text = "/month",
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
                        text = "/month",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${plan.formattedAnnualPrice()}/year",
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
