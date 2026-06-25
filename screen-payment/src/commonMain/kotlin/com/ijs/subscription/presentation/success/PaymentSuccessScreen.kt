package com.ijs.subscription.presentation.success

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetStepIndicator
import com.indusjs.uicomponents.components.StepInfo
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check_circle
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_chevron_right
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_trophy
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_receipt_amount
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_receipt_confirmed
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_receipt_paid
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_receipt_plan
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_receipt_status
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_receipt_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_success_cd
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_success_next_step
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_success_setup_org
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_success_subtitle
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_success_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_success_welcome
import indusjsfleet.ijs_ui_components_lib.generated.resources.step_dashboard
import indusjsfleet.ijs_ui_components_lib.generated.resources.step_organization
import indusjsfleet.ijs_ui_components_lib.generated.resources.step_payment
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PaymentSuccessScreen(
    planName: String,
    amount: Long,
    currency: String,
    onContinue: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    var showContent by remember { mutableStateOf(false) }
    var showReceipt by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(500, easing = EaseOutBack)
        )
        showContent = true
        delay(200)
        showReceipt = true
        delay(200)
        showButton = true
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scheme.primaryContainer.copy(alpha = 0.15f),
                        scheme.surface
                    ),
                    startY = 0f,
                    endY = 600f
                )
            )
    ) {
        val bp = rememberFleetBreakpoint()
        // Compact: full width. Medium/Expanded: center the content to a comfortable max width.
        val contentWidthModifier = if (bp == FleetBreakpoint.Compact) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.widthIn(max = ContentMaxWidth)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Column(
            modifier = contentWidthModifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = FleetTokens.Spacing.XL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(Modifier.size(FleetTokens.Spacing.XXL))

        FleetStepIndicator(
            steps = listOf(
                StepInfo(stringResource(Res.string.step_payment)),
                StepInfo(stringResource(Res.string.step_organization)),
                StepInfo(stringResource(Res.string.step_dashboard))
            ),
            currentStep = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.size(FleetTokens.Spacing.XXL))

        Box(
            modifier = Modifier.size(SuccessIconOuter).scale(iconScale.value),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = scheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(SuccessIconOuter)
            ) {}
            Surface(
                shape = CircleShape,
                color = scheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(SuccessIconMid)
            ) {}
            Surface(
                shape = CircleShape,
                color = scheme.primary,
                modifier = Modifier.size(SuccessIconInner)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = stringResource(Res.string.payment_success_cd),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(FleetTokens.Spacing.L),
                    tint = scheme.onPrimary
                )
            }
        }

        Spacer(Modifier.size(FleetTokens.Spacing.XL))

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.payment_success_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.size(FleetTokens.Spacing.S))

                Text(
                    text = stringResource(Res.string.payment_success_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.size(FleetTokens.Spacing.XL))

        AnimatedVisibility(
            visible = showReceipt,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
        ) {
            FleetSectionCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = FleetTokens.Elevation.Raised,
                contentPadding = FleetTokens.Spacing.XL
            ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.payment_receipt_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = scheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = FleetTokens.Spacing.S,
                                    vertical = FleetTokens.Spacing.XS
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check_circle),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.XS),
                                    tint = scheme.primary
                                )
                                Text(
                                    text = stringResource(Res.string.payment_receipt_paid),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.primary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.size(FleetTokens.Spacing.L))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.size(FleetTokens.Spacing.L))

                    ReceiptRow(label = stringResource(Res.string.payment_receipt_plan), value = planName)
                    Spacer(Modifier.size(FleetTokens.Spacing.M))

                    val formattedAmount = if (amount > 0) {
                        val whole = amount / 100
                        val frac = amount % 100
                        if (frac == 0L) "$whole" else "$whole.${frac.toString().padStart(2, '0')}"
                    } else "—"

                    val currencySymbol = when (currency.uppercase()) {
                        "INR" -> "\u20B9"
                        "USD" -> "$"
                        "EUR" -> "\u20AC"
                        "GBP" -> "\u00A3"
                        else -> currency
                    }

                    ReceiptRow(label = stringResource(Res.string.payment_receipt_amount), value = "$currencySymbol$formattedAmount")
                    Spacer(Modifier.size(FleetTokens.Spacing.M))
                    ReceiptRow(
                        label = stringResource(Res.string.payment_receipt_status),
                        value = stringResource(Res.string.payment_receipt_confirmed),
                        isHighlight = true
                    )
            }
        }

        Spacer(Modifier.size(FleetTokens.Spacing.XL))

        AnimatedVisibility(
            visible = showReceipt,
            enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { it / 3 }
        ) {
            FleetSectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = scheme.primaryContainer,
                border = null,
                elevation = FleetTokens.Elevation.None
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = scheme.primary,
                        modifier = Modifier.size(WelcomeIconChip)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_trophy),
                            contentDescription = null,
                            modifier = Modifier.padding(FleetTokens.Spacing.S),
                            tint = scheme.onPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.payment_success_welcome),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onPrimaryContainer
                        )
                        Spacer(Modifier.size(FleetTokens.Spacing.XXS))
                        Text(
                            text = stringResource(Res.string.payment_success_next_step),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.size(FleetTokens.Spacing.XXL))

        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
        ) {
            FleetButton(
                text = stringResource(Res.string.payment_success_setup_org),
                onClick = onContinue,
                size = ButtonSize.LARGE,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            )
        }

        Spacer(Modifier.size(FleetTokens.Spacing.XXL))
        }
        }
    }
}

/** Max width for the centered content on Medium / Expanded breakpoints. */
private val ContentMaxWidth = 480.dp

/** Decorative concentric success-icon diameters (no token maps cleanly). */
private val SuccessIconOuter = 110.dp
private val SuccessIconMid = 88.dp
private val SuccessIconInner = 68.dp

/** Trophy chip in the welcome card. */
private val WelcomeIconChip = 40.dp

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        if (isHighlight) {
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.S),
                color = scheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary,
                    modifier = Modifier.padding(
                        horizontal = FleetTokens.Spacing.S,
                        vertical = FleetTokens.Spacing.XXS
                    )
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface
            )
        }
    }
}
