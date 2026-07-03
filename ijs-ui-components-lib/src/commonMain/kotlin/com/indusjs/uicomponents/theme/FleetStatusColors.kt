package com.indusjs.uicomponents.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Centralized semantic status colors for the Fleet Management app (Calm Fintech, step-6 retune).
 *
 * These map status meaning to the Calm Fintech semantic palette (DDD C1) — replacing the old off-palette
 * Tailwind/Material swatches that clashed with the indigo refresh. Values are balanced mid-tones chosen to
 * clear the §4 LARGE-UI / chip contrast floor (≥3:1) in BOTH light and dark, since these colours appear on
 * large KPI numerals, status chips and dots — never on body text (financial *labels* stay neutral
 * `onSurfaceVariant`; see the financial-terminology rule). The `*Dark` / container (`*Bg`) variants use the
 * C1 emphasis / container hexes for light-mode fills.
 *
 * Use these instead of hardcoding `Color(0xFF...)`. Full per-mode (light/dark) theme-awareness — resolving
 * each role from the colorScheme via a CompositionLocal — is a tracked follow-up; the current values are a
 * single mid-tone per role.
 */
object FleetStatusColors {

    // ==================== Financial KPI Colors ====================

    /** Profit / positive — balanced emerald (success), legible on light + dark large text. */
    val ProfitGreen = Color(0xFF1E9E57)

    /** Deep success green (C1) — light-mode emphasis / fills. */
    val ProfitGreenDark = Color(0xFF15683A)

    /** Loss / negative — balanced red (error). */
    val LossRed = Color(0xFFD64541)

    /** Deep error red (C1). */
    val LossRedDark = Color(0xFFB0302A)

    /** Expense / warning — deep amber (light-readable, unlike the old bright amber). */
    val ExpenseAmber = Color(0xFFC77A00)

    /** Deep warning amber (C1) — light-mode emphasis / accents. */
    val ExpenseAmberDark = Color(0xFF855900)

    /** C1 warning amber tuned for DARK surfaces — brighter than [ExpenseAmberDark] so a warning
     *  accent / dot clears the §4 contrast floor on a dark card. Pair via [fleetWarningAccent]. */
    val ExpenseAmberOnDark = Color(0xFFE8B24A)

    /** Info — teal (C1), on-palette (replaces the old Material sky-blue). */
    val InfoBlue = Color(0xFF1C8A93)

    /** Deep info teal (C1) — light-mode emphasis / accents. */
    val InfoBlueDark = Color(0xFF0B6A73)

    /** C1 info teal tuned for DARK surfaces — brighter than [InfoBlue] so an info accent / dot
     *  clears the §4 contrast floor on a dark card. Pair via [fleetInfoAccent]. */
    val InfoBlueOnDark = Color(0xFF46C7D0)

    /** Accent — indigo family (replaces the old violet). */
    val AccentPurple = Color(0xFF6E72E8)

    /** Success green (alias of profit emerald). */
    val SuccessGreen = Color(0xFF1E9E57)

    /** Neutral slate (better contrast than the old gray-400). */
    val NeutralGray = Color(0xFF6B7280)

    // ==================== Payment Status Colors ====================

    /** Payment received / completed — success. */
    val PaymentReceived = Color(0xFF15683A)

    /** Payment received background (C1 success container). */
    val PaymentReceivedBg = Color(0xFFDCEDE3)

    /** Payment pending — warning. */
    val PaymentPending = Color(0xFF855900)

    /** Payment pending background (C1 warning container). */
    val PaymentPendingBg = Color(0xFFF5E7CB)

    /** Partial payment — amber. */
    val PaymentPartial = Color(0xFFC77A00)

    /** Payment cancelled — error. */
    val PaymentCancelled = Color(0xFFB0302A)

    /** Payment cancelled background (C1 error container). */
    val PaymentCancelledBg = Color(0xFFF7DEDC)

    // ==================== Trip Status Colors ====================

    /** On-route — info teal (was Material sky-blue). */
    val TripOnRoute = Color(0xFF1C8A93)

    /** Planned — indigo accent (was Material purple). */
    val TripPlanned = Color(0xFF6E72E8)

    /** Trip failed / cancelled — error. */
    val TripFailed = Color(0xFFB0302A)

    // ==================== Vehicle Fleet Status Colors ====================

    /** On-route — success green. */
    val FleetOnRoute = Color(0xFF1E9E57)

    /** Planned — indigo (distinct chart hue; was Material sky-blue). */
    val FleetPlanned = Color(0xFF4F57D4)

    /** Available — positive green (healthy state). */
    val FleetAvailable = Color(0xFF1E9E57)

    /** Maintenance — warning amber. */
    val FleetMaintenance = Color(0xFFC77A00)

    /** Inactive — neutral slate. */
    val FleetInactive = Color(0xFF6B7280)

    /** Health excellent — same as [FleetOnRoute]. */
    val HealthGood = FleetOnRoute

    /** Health warning — same as [FleetMaintenance]. */
    val HealthWarning = FleetMaintenance

    /** Health critical — error. */
    val HealthCritical = Color(0xFFB0302A)

    /** Advance payment — info teal. */
    val PaymentAdvance = Color(0xFF1C8A93)

    /** Refund payment — error. */
    val PaymentRefund = Color(0xFFB0302A)

    // ==================== Utility ====================

    /**
     * Returns the financial color for a profit/loss value.
     * Positive → [ProfitGreen], negative → [LossRed].
     */
    fun profitLossColor(value: Double): Color =
        if (value >= 0) ProfitGreen else LossRed
}

/**
 * Theme-aware C1 WARNING accent: deep [FleetStatusColors.ExpenseAmberDark] on light,
 * brighter [FleetStatusColors.ExpenseAmberOnDark] on dark. Use for warning accent bars / dots /
 * expiry badges so the amber stays legible on a dark card (CRITICAL/INFO already read from the
 * theme colorScheme / mid-tone teal).
 */
@Composable
fun fleetWarningAccent(): Color =
    if (isAppInDarkTheme()) FleetStatusColors.ExpenseAmberOnDark else FleetStatusColors.ExpenseAmberDark

/**
 * Theme-aware C1 INFO accent: [FleetStatusColors.InfoBlue] teal on light, brighter
 * [FleetStatusColors.InfoBlueOnDark] on dark. Companion to [fleetWarningAccent] for info-severity
 * accent bars / dots / expiry badges.
 */
@Composable
fun fleetInfoAccent(): Color =
    if (isAppInDarkTheme()) FleetStatusColors.InfoBlueOnDark else FleetStatusColors.InfoBlue
