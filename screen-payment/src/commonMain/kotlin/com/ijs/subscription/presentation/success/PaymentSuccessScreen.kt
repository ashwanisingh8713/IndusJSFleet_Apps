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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.FleetStepIndicator
import com.indusjs.uicomponents.components.StepInfo
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

    Column(
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        FleetStepIndicator(
            steps = listOf(
                StepInfo(stringResource(Res.string.step_payment)),
                StepInfo(stringResource(Res.string.step_organization)),
                StepInfo(stringResource(Res.string.step_dashboard))
            ),
            currentStep = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(110.dp).scale(iconScale.value),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = scheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(110.dp)
            ) {}
            Surface(
                shape = CircleShape,
                color = scheme.primary.copy(alpha = 0.2f),
                modifier = Modifier.size(88.dp)
            ) {}
            Surface(
                shape = CircleShape,
                color = scheme.primary,
                modifier = Modifier.size(68.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = stringResource(Res.string.payment_success_cd),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = scheme.onPrimary
                )
            }
        }

        Spacer(Modifier.height(24.dp))

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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.payment_success_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = showReceipt,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                            shape = RoundedCornerShape(8.dp),
                            color = scheme.primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check_circle),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
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

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(16.dp))

                    ReceiptRow(label = stringResource(Res.string.payment_receipt_plan), value = planName)
                    Spacer(Modifier.height(12.dp))

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
                    Spacer(Modifier.height(12.dp))
                    ReceiptRow(
                        label = stringResource(Res.string.payment_receipt_status),
                        value = stringResource(Res.string.payment_receipt_confirmed),
                        isHighlight = true
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = showReceipt,
            enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(tween(400, delayMillis = 100)) { it / 3 }
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = scheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = scheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_trophy),
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp),
                            tint = scheme.primary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.payment_success_welcome),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = scheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(Res.string.payment_success_next_step),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 1.dp
                )
            ) {
                Text(
                    text = stringResource(Res.string.payment_success_setup_org),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

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
                shape = RoundedCornerShape(6.dp),
                color = scheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
