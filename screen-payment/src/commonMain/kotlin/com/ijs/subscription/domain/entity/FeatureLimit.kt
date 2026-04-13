package com.ijs.subscription.domain.entity

data class FeatureLimit(
    val key: String,
    val label: String,
    val value: Long,
    val unlimited: Boolean
) {
    val displayValue: String
        get() = when {
            unlimited -> "Unlimited"
            value == -1L -> "✓"
            value == 0L -> "✗"
            else -> value.toString()
        }

    val isEnabled: Boolean get() = value != 0L || unlimited
}
