package com.indusjs.uicomponents.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import kotlin.math.pow

// ==================== Finance Colors ====================

object FinanceColors {
    val LoanBlue = com.indusjs.uicomponents.theme.FleetStatusColors.InfoBlue
    val LoanBlueDark = com.indusjs.uicomponents.theme.FleetStatusColors.InfoBlueDark
    val CashGreen = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
    val CashGreenDark = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreenDark
    val WarningOrange = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber
    val WarningOrangeDark = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmberDark
    val CriticalRed = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
    val CriticalRedDark = com.indusjs.uicomponents.theme.FleetStatusColors.LossRedDark
    val NeutralGray = com.indusjs.uicomponents.theme.FleetStatusColors.NeutralGray
    val SuccessGreen = com.indusjs.uicomponents.theme.FleetStatusColors.SuccessGreen
    val PurpleAccent = com.indusjs.uicomponents.theme.FleetStatusColors.AccentPurple
}

// ==================== Circular Progress Indicator ====================

/**
 * Circular progress indicator for loan completion visualization
 */
@Composable
fun LoanProgressCircle(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    progressColor: Color = FinanceColors.LoanBlue,
    trackColor: Color = FinanceColors.LoanBlue.copy(alpha = 0.2f),
    centerContent: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sweepAngle = animatedProgress * 360f
            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Progress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }

        centerContent()
    }
}

// ==================== Loan Status Badge ====================

enum class LoanStatus {
    ACTIVE, CLOSED, DEFAULTED, NOT_APPLICABLE
}

@Composable
fun LoanStatusBadge(
    status: LoanStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label, icon) = when (status) {
        LoanStatus.ACTIVE -> listOf(
            FinanceColors.CashGreen.copy(alpha = 0.15f),
            FinanceColors.CashGreen,
            "Active",
            "🟢"
        )
        LoanStatus.CLOSED -> listOf(
            FinanceColors.LoanBlue.copy(alpha = 0.15f),
            FinanceColors.LoanBlue,
            "Closed",
            "✅"
        )
        LoanStatus.DEFAULTED -> listOf(
            FinanceColors.CriticalRed.copy(alpha = 0.15f),
            FinanceColors.CriticalRed,
            "Defaulted",
            "🔴"
        )
        LoanStatus.NOT_APPLICABLE -> listOf(
            FinanceColors.NeutralGray.copy(alpha = 0.15f),
            FinanceColors.NeutralGray,
            "Cash",
            "💵"
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor as Color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon as String,
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = label as String,
                style = MaterialTheme.typography.labelMedium,
                color = textColor as Color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==================== Payment Mode Chip ====================

@Composable
fun PaymentModeChip(
    mode: String,
    modifier: Modifier = Modifier
) {
    val (icon, label, color) = when (mode.lowercase()) {
        "netbanking" -> Triple("🏦", "Net Banking", FinanceColors.LoanBlue)
        "upi" -> Triple("📱", "UPI", FinanceColors.PurpleAccent)
        "cash" -> Triple("💵", "Cash", FinanceColors.CashGreen)
        "auto_debit" -> Triple("🔄", "Auto Debit", FinanceColors.WarningOrange)
        "cheque" -> Triple("📝", "Cheque", FinanceColors.NeutralGray)
        else -> Triple("💳", mode.replaceFirstChar { it.uppercase() }, FinanceColors.NeutralGray)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.labelSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================== EMI Countdown Card ====================

@Composable
fun EmiCountdownCard(
    daysUntilDue: Int,
    emiAmount: Double,
    emiNumber: Int,
    onRecordClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (urgencyColor, urgencyText) = when {
        daysUntilDue < 0 -> FinanceColors.CriticalRed to "Overdue by ${-daysUntilDue} days"
        daysUntilDue == 0 -> FinanceColors.CriticalRed to "Due Today!"
        daysUntilDue <= 3 -> FinanceColors.WarningOrange to "Due in $daysUntilDue days"
        daysUntilDue <= 7 -> FinanceColors.WarningOrange to "Due in $daysUntilDue days"
        else -> FinanceColors.CashGreen to "Due in $daysUntilDue days"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = urgencyColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "EMI #$emiNumber",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(emiAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = urgencyColor
                )
                Text(
                    text = urgencyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = urgencyColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onRecordClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = urgencyColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Record Payment")
            }
        }
    }
}

// ==================== Amount Display ====================

@Composable
fun AmountDisplay(
    amount: Double,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    amountStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
    showSign: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (showSign && amount >= 0) "+${formatCurrency(amount)}" else formatCurrency(amount),
            style = amountStyle,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            textAlign = TextAlign.Center
        )
    }
}

// ==================== Copyable Text Row ====================

@Composable
fun CopyableDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onCopied: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        clipboardManager.setText(AnnotatedString(value))
                        onCopied()
                    },
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "📋",
                    modifier = Modifier.padding(6.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// ==================== Finance Stat Card ====================

@Composable
fun FinanceStatCard(
    value: String,
    label: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    subValue: String? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (subValue != null) {
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================== Timeline Connector ====================

@Composable
fun TimelineConnector(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Box(
        modifier = modifier
            .width(2.dp)
            .background(color)
    )
}

// ==================== Payment Timeline Item ====================

@Composable
fun PaymentTimelineItem(
    emiNumber: Int,
    amount: Double,
    date: String,
    isPaid: Boolean,
    isOverdue: Boolean = false,
    paymentMode: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showConnector: Boolean = true
) {
    val statusColor = when {
        isPaid -> FinanceColors.CashGreen
        isOverdue -> FinanceColors.CriticalRed
        else -> FinanceColors.WarningOrange
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            if (showConnector) {
                TimelineConnector(
                    modifier = Modifier.height(50.dp),
                    color = statusColor.copy(alpha = 0.3f)
                )
            }
        }

        // Content
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "EMI #$emiNumber",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (paymentMode != null && isPaid) {
                        PaymentModeChip(mode = paymentMode)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatCurrency(amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = when {
                            isPaid -> "✓ Paid"
                            isOverdue -> "⚠ Overdue"
                            else -> "⏳ Pending"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==================== EMI Calculator Preview ====================

@Composable
fun EmiCalculatorPreview(
    loanAmount: Double,
    interestRate: Double,
    tenureMonths: Int,
    modifier: Modifier = Modifier
) {
    if (loanAmount <= 0 || interestRate <= 0 || tenureMonths <= 0) return

    val monthlyRate = interestRate / 12 / 100
    val emi = if (monthlyRate > 0) {
        loanAmount * monthlyRate * (1 + monthlyRate).pow(tenureMonths.toDouble()) /
                ((1 + monthlyRate).pow(tenureMonths.toDouble()) - 1)
    } else {
        loanAmount / tenureMonths
    }
    val totalPayable = emi * tenureMonths
    val totalInterest = totalPayable - loanAmount

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FinanceColors.LoanBlue.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🧮", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "EMI Calculator Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceColors.LoanBlue
                )
            }

            HorizontalDivider(color = FinanceColors.LoanBlue.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AmountDisplay(
                    amount = emi,
                    label = "Monthly EMI",
                    color = FinanceColors.LoanBlue,
                    amountStyle = MaterialTheme.typography.titleMedium
                )
                AmountDisplay(
                    amount = totalInterest,
                    label = "Total Interest",
                    color = FinanceColors.WarningOrange,
                    amountStyle = MaterialTheme.typography.titleMedium
                )
                AmountDisplay(
                    amount = totalPayable,
                    label = "Total Payable",
                    color = FinanceColors.CashGreen,
                    amountStyle = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ==================== Finance Filter Chip ====================

@Composable
fun FinanceFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label)
                if (count > 0) {
                    Surface(
                        shape = CircleShape,
                        color = if (selected) MaterialTheme.colorScheme.surface else color.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = count.toString(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) color else color
                        )
                    }
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = color.copy(alpha = 0.3f),
            selectedBorderColor = color,
            enabled = true,
            selected = selected
        ),
        modifier = modifier
    )
}
