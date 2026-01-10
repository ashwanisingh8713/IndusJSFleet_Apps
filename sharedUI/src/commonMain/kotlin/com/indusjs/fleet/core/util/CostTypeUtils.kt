package com.indusjs.fleet.core.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for cost type display formatting.
 * Used across Trip Costs and Vehicle Maintenance Costs screens.
 */
object CostTypeUtils {

    /**
     * Get human-readable display name for a cost type.
     *
     * @param costType The cost type identifier (e.g., "fuel", "driver_allowance")
     * @return Human-readable display name (e.g., "Fuel", "Driver Allowance")
     */
    fun getDisplayName(costType: String): String {
        return when (costType.lowercase()) {
            "fuel" -> "Fuel"
            "toll" -> "Toll"
            "driver_allowance" -> "Driver Allowance"
            "parking" -> "Parking"
            "loading_charges" -> "Loading Charges"
            "unloading_charges" -> "Unloading Charges"
            "chalan" -> "Chalan"
            "permit" -> "Permit"
            "insurance" -> "Insurance"
            "other" -> "Other"
            // Maintenance cost types
            "tyre" -> "Tyre"
            "battery" -> "Battery"
            "servicing" -> "Servicing"
            "engine_repair" -> "Engine Repair"
            "body_repair" -> "Body Repair"
            "electrical" -> "Electrical"
            "ac_repair" -> "AC Repair"
            else -> costType.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
        }
    }

    /**
     * Get emoji icon for a cost type.
     *
     * @param costType The cost type identifier
     * @return Emoji icon representing the cost type
     */
    fun getIcon(costType: String): String {
        return when (costType.lowercase()) {
            "fuel" -> "⛽"
            "toll" -> "🛣️"
            "driver_allowance" -> "👤"
            "parking" -> "🅿️"
            "loading_charges" -> "📦"
            "unloading_charges" -> "📤"
            "chalan" -> "📄"
            "permit" -> "🎫"
            "insurance" -> "🛡️"
            // Maintenance cost types
            "tyre" -> "🛞"
            "battery" -> "🔋"
            "servicing" -> "🔧"
            "engine_repair" -> "⚙️"
            "body_repair" -> "🚗"
            "electrical" -> "⚡"
            "ac_repair" -> "❄️"
            else -> "💵"
        }
    }

    /**
     * Get predefined color for a cost type for visual differentiation.
     *
     * @param costType The cost type identifier
     * @return Color associated with the cost type
     */
    fun getColor(costType: String): Color {
        return when (costType.lowercase()) {
            "fuel" -> Color(0xFF4CAF50) // Green
            "toll" -> Color(0xFF2196F3) // Blue
            "driver_allowance" -> Color(0xFFFF9800) // Orange
            "parking" -> Color(0xFF9C27B0) // Purple
            "loading_charges" -> Color(0xFF795548) // Brown
            "unloading_charges" -> Color(0xFF607D8B) // Blue Grey
            "chalan" -> Color(0xFFE91E63) // Pink
            "permit" -> Color(0xFF00BCD4) // Cyan
            "insurance" -> Color(0xFF3F51B5) // Indigo
            // Maintenance cost types
            "tyre" -> Color(0xFF424242) // Dark Grey
            "battery" -> Color(0xFFFFEB3B) // Yellow
            "servicing" -> Color(0xFF009688) // Teal
            "engine_repair" -> Color(0xFF8BC34A) // Light Green
            "body_repair" -> Color(0xFFFF5722) // Deep Orange
            "electrical" -> Color(0xFFFFC107) // Amber
            "ac_repair" -> Color(0xFF03A9F4) // Light Blue
            else -> Color(0xFF9E9E9E) // Grey
        }
    }
}

/**
 * Composable extension to get cost type color with fallback to MaterialTheme.
 */
@Composable
fun getCostTypeColorComposable(costType: String): Color {
    val predefinedColor = CostTypeUtils.getColor(costType)
    return if (predefinedColor == Color(0xFF9E9E9E)) {
        MaterialTheme.colorScheme.primary
    } else {
        predefinedColor
    }
}

