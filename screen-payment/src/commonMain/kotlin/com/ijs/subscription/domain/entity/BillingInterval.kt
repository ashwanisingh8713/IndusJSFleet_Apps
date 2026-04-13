package com.ijs.subscription.domain.entity

enum class BillingInterval(val apiValue: String, val label: String) {
    MONTHLY("monthly", "Monthly"),
    ANNUAL("annual", "Annual");

    companion object {
        fun from(value: String): BillingInterval =
            entries.firstOrNull { it.apiValue == value } ?: MONTHLY
    }
}
