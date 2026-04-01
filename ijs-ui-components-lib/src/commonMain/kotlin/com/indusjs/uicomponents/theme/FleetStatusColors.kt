package com.indusjs.uicomponents.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized semantic status colors for the Fleet Management app.
 *
 * Use these constants instead of hardcoding `Color(0xFF...)` values
 * throughout screens. This ensures visual consistency and makes
 * future theme changes a single-file edit.
 *
 * Categories:
 * - **Financial** — profit/loss/expense indicators
 * - **Payment Status** — received/pending/cancelled
 * - **Entity Status** — completed/on-route/planned/failed trip colors
 * - **Semantic** — general success/warning/error/info/neutral
 */
object FleetStatusColors {

    // ==================== Financial KPI Colors ====================

    /** Profit / Revenue green (Tailwind Emerald-500) */
    val ProfitGreen = Color(0xFF10B981)

    /** Darker profit green for gradients (Tailwind Emerald-600) */
    val ProfitGreenDark = Color(0xFF059669)

    /** Loss / Deficit red (Tailwind Red-500) */
    val LossRed = Color(0xFFEF4444)

    /** Darker loss red for emphasis (Tailwind Red-600) */
    val LossRedDark = Color(0xFFDC2626)

    /** Expense / Warning amber (Tailwind Amber-500) */
    val ExpenseAmber = Color(0xFFF59E0B)

    /** Darker expense amber for gradients (Tailwind Amber-600) */
    val ExpenseAmberDark = Color(0xFFD97706)

    /** Info / Neutral blue (Tailwind Blue-500) */
    val InfoBlue = Color(0xFF3B82F6)

    /** Darker info blue (Tailwind Blue-700) */
    val InfoBlueDark = Color(0xFF1D4ED8)

    /** Accent purple (Tailwind Violet-500) */
    val AccentPurple = Color(0xFF8B5CF6)

    /** Success green variant (Tailwind Green-500) */
    val SuccessGreen = Color(0xFF22C55E)

    /** Neutral gray (Tailwind Gray-400) */
    val NeutralGray = Color(0xFF9CA3AF)

    // ==================== Payment Status Colors ====================

    /** Payment received / completed (Material Green-800) */
    val PaymentReceived = Color(0xFF2E7D32)

    /** Payment received background */
    val PaymentReceivedBg = Color(0xFFE8F5E9)

    /** Payment pending / partial (Material Orange-900) */
    val PaymentPending = Color(0xFFE65100)

    /** Payment pending background */
    val PaymentPendingBg = Color(0xFFFFF3E0)

    /** Partial payment amber (Material Amber-700) */
    val PaymentPartial = Color(0xFFFFA000)

    /** Payment cancelled (Material Red-800) */
    val PaymentCancelled = Color(0xFFC62828)

    /** Payment cancelled background */
    val PaymentCancelledBg = Color(0xFFFFEBEE)

    // ==================== Trip Status Colors ====================

    /** On-route blue (Material Blue-700) */
    val TripOnRoute = Color(0xFF1976D2)

    /** Planned purple (Material Purple-700) */
    val TripPlanned = Color(0xFF7B1FA2)

    /** Trip failed / cancelled red (Material Red-700) */
    val TripFailed = Color(0xFFD32F2F)

    // ==================== Vehicle Fleet Status Colors ====================

    /** On-route green for fleet bar chart (Material Green-500) */
    val FleetOnRoute = Color(0xFF4CAF50)

    /** Planned blue (Material Blue-500) */
    val FleetPlanned = Color(0xFF2196F3)

    /** Available teal (Material Teal-500) */
    val FleetAvailable = Color(0xFF009688)

    /** Maintenance orange (Material Orange-500) */
    val FleetMaintenance = Color(0xFFFF9800)

    /** Inactive gray (Material Gray-500) */
    val FleetInactive = Color(0xFF9E9E9E)

    /** Health excellent green — same as FleetOnRoute */
    val HealthGood = FleetOnRoute

    /** Health warning orange — same as FleetMaintenance */
    val HealthWarning = FleetMaintenance

    /** Health critical red (Material Red-400) */
    val HealthCritical = Color(0xFFEF5350)

    /** Advance payment blue (Material Blue-700) */
    val PaymentAdvance = Color(0xFF1976D2)

    /** Refund payment red (Material Red-700) */
    val PaymentRefund = Color(0xFFD32F2F)

    // ==================== Utility ====================

    /**
     * Returns the financial color for a profit/loss value.
     * Positive → [ProfitGreen], negative → [LossRed].
     */
    fun profitLossColor(value: Double): Color =
        if (value >= 0) ProfitGreen else LossRed
}

