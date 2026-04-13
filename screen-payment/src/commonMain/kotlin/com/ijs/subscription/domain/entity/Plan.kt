package com.ijs.subscription.domain.entity

data class Plan(
    val id: String,
    val name: String,
    val description: String,
    val monthlyPrice: Long,
    val annualPrice: Long,
    val discountPercent: Double,
    val effectiveMonthlyPriceAnnual: Long,
    val annualSavings: Long,
    val currency: String,
    val trialDays: Int,
    val features: List<String>,
    val featureLimits: List<FeatureLimit>,
    val isActive: Boolean
) {
    val isFree: Boolean get() = monthlyPrice == 0L && annualPrice == 0L

    fun priceFor(interval: BillingInterval): Long = when (interval) {
        BillingInterval.MONTHLY -> monthlyPrice
        BillingInterval.ANNUAL -> annualPrice
    }

    fun formattedMonthlyPrice(): String =
        if (isFree) "Free" else formatAmount(monthlyPrice, currency)

    fun formattedAnnualPrice(): String =
        if (isFree) "Free" else formatAmount(annualPrice, currency)

    fun formattedEffectiveMonthly(): String =
        if (isFree) "Free" else formatAmount(effectiveMonthlyPriceAnnual, currency)

    fun formattedAnnualSavings(): String =
        if (annualSavings <= 0) "" else "Save ${formatAmount(annualSavings, currency)}"

    private fun formatAmount(paise: Long, @Suppress("UNUSED_PARAMETER") currency: String): String {
        val whole = paise / 100
        val frac = paise % 100
        return if (frac == 0L) "₹$whole" else "₹$whole.${frac.toString().padStart(2, '0')}"
    }
}
