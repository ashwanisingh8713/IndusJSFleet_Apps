package com.indusjs.fleet.domain.entity.customer

import com.indusjs.fleet.domain.entity.Entity
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

// ============= Helper function for formatting =============

/**
 * Multiplatform-compatible number formatting.
 * Formats a double to the specified decimal places.
 */
private fun formatDecimal(value: Double, decimals: Int = 1): String {
    val multiplier = 10.0.pow(decimals)
    val rounded = round(value * multiplier) / multiplier
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1].take(decimals).padEnd(decimals, '0') else "0".repeat(decimals)
    return "$intPart.$decPart"
}

/**
 * Format amount with appropriate suffix (Cr, L, K).
 */
private fun formatAmountWithSuffix(amount: Double): String {
    val absAmount = abs(amount)
    val prefix = if (amount < 0) "-" else ""
    return prefix + when {
        absAmount >= 10000000 -> "${formatDecimal(absAmount / 10000000)}Cr"
        absAmount >= 100000 -> "${formatDecimal(absAmount / 100000)}L"
        absAmount >= 1000 -> "${formatDecimal(absAmount / 1000)}K"
        else -> absAmount.toInt().toString()
    }
}

/**
 * Domain entity representing a Customer.
 * Customers are associated with Trips for business tracking.
 */
data class Customer(
    val id: String,
    val companyName: String,
    val personName: String,
    val primaryContact: String,
    val secondaryContact: String? = null,
    val companyAddress: String? = null,
    val email: String? = null,
    val gstNumber: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val ownerId: String? = null,
    val createdById: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) : Entity

/**
 * Customer statistics for financial overview.
 * Only available to Owner and General Manager.
 */
data class CustomerStatistics(
    val customerId: String,
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val activeTrips: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalPendingPayment: Double = 0.0,
    val totalReceivedPayment: Double = 0.0,
    val averageTripValue: Double = 0.0,
    val lastTripDate: String? = null
) {
    val totalRevenueLabel: String
        get() = formatAmountWithSuffix(totalRevenue)

    val pendingPaymentsLabel: String
        get() = formatAmountWithSuffix(totalPendingPayment)

    val receivedPaymentsLabel: String
        get() = formatAmountWithSuffix(totalReceivedPayment)

    val averageTripValueLabel: String
        get() = formatAmountWithSuffix(averageTripValue)
}

/**
 * Minimal customer info for selection lists (dropdowns, autocomplete).
 */
data class CustomerSummary(
    val id: String,
    val companyName: String,
    val personName: String,
    val primaryContact: String,
    val isActive: Boolean = true
)

// ============= Customer Trip Entity =============

/**
 * Trip associated with a customer.
 */
data class CustomerTrip(
    val id: String,
    val vehicleId: String? = null,
    val vehicleRegistration: String? = null,
    val driverId: String? = null,
    val driverName: String? = null,
    val startLocation: String? = null,
    val endLocation: String? = null,
    val estimatedDistance: Double? = null,
    val scheduledDate: String? = null,
    val tripPrice: Double? = null,
    val paidAmount: Double? = null,
    val pendingAmount: Double? = null,
    val paymentStatus: String? = null,
    val state: String? = null,
    val priority: String? = null,
    val cargoType: String? = null,
    val createdAt: String? = null
) {
    val routeDisplay: String
        get() = buildString {
            append(startLocation?.take(20) ?: "Unknown")
            append(" → ")
            append(endLocation?.take(20) ?: "Unknown")
        }

    val distanceDisplay: String
        get() = estimatedDistance?.let { "${it.toInt()} km" } ?: "N/A"

    val tripPriceDisplay: String
        get() = tripPrice?.let { "₹${formatAmountWithSuffix(it)}" } ?: "N/A"

    val paidAmountDisplay: String
        get() = paidAmount?.let { "₹${formatAmountWithSuffix(it)}" } ?: "₹0"

    val pendingAmountDisplay: String
        get() = pendingAmount?.let { "₹${formatAmountWithSuffix(it)}" } ?: "₹0"

    val stateDisplay: String
        get() = when (state?.lowercase()) {
            "planned" -> "Planned"
            "on_route" -> "On Route"
            "completed" -> "Completed"
            "cancelled" -> "Cancelled"
            "delayed" -> "Delayed"
            "failed" -> "Failed"
            else -> state?.replaceFirstChar { it.uppercase() } ?: "Unknown"
        }

    val stateIcon: String
        get() = when (state?.lowercase()) {
            "planned" -> "📋"
            "on_route" -> "🚛"
            "completed" -> "✅"
            "cancelled" -> "❌"
            "delayed" -> "⏰"
            "failed" -> "⚠️"
            else -> "📦"
        }
}

/**
 * Summary of customer trips.
 */
data class CustomerTripsSummary(
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val activeTrips: Int = 0,
    val plannedTrips: Int = 0,
    val cancelledTrips: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalPending: Double = 0.0
) {
    val totalRevenueDisplay: String
        get() = "₹${formatAmountWithSuffix(totalRevenue)}"

    val totalPendingDisplay: String
        get() = "₹${formatAmountWithSuffix(totalPending)}"
}

// ============= Customer Pending Payment Entity =============

/**
 * Pending payment for a customer trip.
 */
data class CustomerPendingPayment(
    val tripId: String,
    val vehicleRegistration: String? = null,
    val startLocation: String? = null,
    val endLocation: String? = null,
    val tripDate: String? = null,
    val tripPrice: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val daysOverdue: Int = 0,
    val state: String? = null,
    val paymentStatus: String? = null
) {
    val routeDisplay: String
        get() = buildString {
            append(startLocation?.take(15) ?: "Unknown")
            append(" → ")
            append(endLocation?.take(15) ?: "Unknown")
        }

    val tripPriceDisplay: String
        get() = "₹${pendingAmount.toInt()}"

    val paidAmountDisplay: String
        get() = "₹${paidAmount.toInt()}"

    val pendingAmountDisplay: String
        get() = "₹${pendingAmount.toInt()}"

    val isOverdue: Boolean
        get() = daysOverdue > 0

    val isCriticalOverdue: Boolean
        get() = daysOverdue > 7

    val overdueDisplay: String
        get() = when {
            daysOverdue <= 0 -> "Due"
            daysOverdue == 1 -> "1 day overdue"
            else -> "$daysOverdue days overdue"
        }

    val overdueBadge: String
        get() = when {
            daysOverdue <= 0 -> ""
            else -> "$daysOverdue d"
        }
}

// ============= Customer Payment Entity =============

/**
 * Payment mode enum.
 */
enum class PaymentMode(val apiValue: String, val displayName: String, val icon: String) {
    CASH("cash", "Cash", "💵"),
    UPI("upi", "UPI", "📱"),
    BANK_TRANSFER("bank_transfer", "Bank Transfer", "🏦"),
    CARD("card", "Card", "💳");

    companion object {
        fun fromApiValue(value: String?): PaymentMode? {
            return entries.find { it.apiValue.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Payment received from a customer.
 */
data class CustomerPayment(
    val id: String,
    val tripId: String? = null,
    val amount: Double = 0.0,
    val mode: PaymentMode? = null,
    val status: String? = null,
    val date: String? = null,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val createdAt: String? = null
) {
    val amountDisplay: String
        get() = "₹${amount.toInt()}"

    val modeIcon: String
        get() = mode?.icon ?: "💰"

    val modeDisplay: String
        get() = mode?.displayName ?: "Unknown"

    val tripDisplay: String
        get() = tripId?.let { "Trip #$it" } ?: "N/A"
}

// ============= Customer Payment Summary Entity =============

/**
 * Payment breakdown by mode.
 */
data class PaymentByMode(
    val mode: PaymentMode?,
    val amount: Double = 0.0,
    val count: Int = 0,
    val percentage: Double = 0.0
) {
    val amountDisplay: String
        get() = "₹${formatAmountWithSuffix(amount)}"

    val percentageDisplay: String
        get() = "${percentage.toInt()}%"

    val modeDisplay: String
        get() = mode?.displayName ?: "Other"

    val modeIcon: String
        get() = mode?.icon ?: "💰"
}

/**
 * Monthly payment data.
 */
data class MonthlyPayment(
    val month: String,
    val year: Int,
    val amount: Double = 0.0,
    val count: Int = 0
) {
    val amountDisplay: String
        get() = "₹${formatAmountWithSuffix(amount)}"

    val periodDisplay: String
        get() = "$month $year"
}

/**
 * Customer payment summary with breakdowns.
 */
data class CustomerPaymentSummary(
    val byMode: List<PaymentByMode> = emptyList(),
    val byMonth: List<MonthlyPayment> = emptyList(),
    val totalTds: Double = 0.0,
    val totalAmount: Double = 0.0,
    val totalPayments: Int = 0
) {
    val totalAmountDisplay: String
        get() = "₹${formatAmountWithSuffix(totalAmount)}"
}

/**
 * Period breakdown in financial report.
 */
data class PeriodBreakdown(
    val period: String,
    val revenue: Double = 0.0,
    val costs: Double = 0.0,
    val profit: Double = 0.0,
    val trips: Int = 0
) {
    val revenueDisplay: String
        get() = "₹${formatAmountWithSuffix(revenue)}"

    val costsDisplay: String
        get() = "₹${formatAmountWithSuffix(costs)}"

    val profitDisplay: String
        get() = "₹${formatAmountWithSuffix(profit)}"

    val isProfitable: Boolean
        get() = profit >= 0
}

/**
 * Top performing vehicle.
 */
data class TopVehicle(
    val vehicleId: String,
    val vehicleRegistration: String,
    val trips: Int = 0,
    val revenue: Double = 0.0,
    val costs: Double = 0.0,
    val profit: Double = 0.0
) {
    val revenueDisplay: String
        get() = "₹${formatAmountWithSuffix(revenue)}"

    val profitDisplay: String
        get() = "₹${formatAmountWithSuffix(profit)}"
}

/**
 * Trip summary in financial report.
 */
data class FinancialTripSummary(
    val totalTrips: Int = 0,
    val completedTrips: Int = 0,
    val averageTripValue: Double = 0.0,
    val totalDistance: Double = 0.0
) {
    val averageTripValueDisplay: String
        get() = "₹${averageTripValue.toInt()}"

    val totalDistanceDisplay: String
        get() = "${totalDistance.toInt()} km"
}

/**
 * Financial period enum.
 */
enum class FinancialPeriod(val apiValue: String, val displayName: String) {
    MONTHLY("monthly", "Monthly"),
    QUARTERLY("quarterly", "Quarterly"),
    YEARLY("yearly", "Yearly"),
    CUSTOM("custom", "Custom Range");

    companion object {
        fun fromApiValue(value: String?): FinancialPeriod {
            return entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: MONTHLY
        }
    }
}

/**
 * Complete customer financial report.
 */
data class CustomerFinancialReport(
    val customerId: String,
    val customerName: String? = null,
    val period: FinancialPeriod = FinancialPeriod.MONTHLY,
    val startDate: String? = null,
    val endDate: String? = null,
    val totalRevenue: Double = 0.0,
    val totalCosts: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: Double = 0.0,
    val tripSummary: FinancialTripSummary? = null,
    val periodBreakdown: List<PeriodBreakdown> = emptyList(),
    val topVehicles: List<TopVehicle> = emptyList(),
    val paymentReceived: Double = 0.0,
    val paymentPending: Double = 0.0
) {
    val totalRevenueDisplay: String
        get() = "₹${formatAmountWithSuffix(totalRevenue)}"

    val totalCostsDisplay: String
        get() = "₹${formatAmountWithSuffix(totalCosts)}"

    val netProfitDisplay: String
        get() = "₹${formatAmountWithSuffix(netProfit)}"

    val profitMarginDisplay: String
        get() = "${profitMargin.toInt()}%"

    val isProfitable: Boolean
        get() = netProfit >= 0

    val paymentReceivedDisplay: String
        get() = "₹${formatAmountWithSuffix(paymentReceived)}"

    val paymentPendingDisplay: String
        get() = "₹${formatAmountWithSuffix(paymentPending)}"

    val periodDisplay: String
        get() = when {
            startDate != null && endDate != null -> "$startDate to $endDate"
            else -> period.displayName
        }
}
