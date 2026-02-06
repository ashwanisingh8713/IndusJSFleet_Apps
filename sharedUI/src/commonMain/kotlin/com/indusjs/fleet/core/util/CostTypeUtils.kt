package com.indusjs.fleet.core.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for cost type display formatting.
 * Used across Trip Costs and Vehicle Maintenance Costs screens.
 *
 * Supports both:
 * - New structured cost IDs (e.g., "TC-001-002" for Diesel)
 * - Legacy cost types (e.g., "fuel", "toll")
 */
object CostTypeUtils {

    /**
     * Get human-readable display name for a cost type or cost ID.
     *
     * @param costTypeOrId The cost type/ID (e.g., "fuel", "TC-001-002", "driver_allowance")
     * @return Human-readable display name (e.g., "Fuel", "Diesel", "Driver Allowance")
     */
    fun getDisplayName(costTypeOrId: String): String {
        // Handle new structured cost IDs first
        return when (costTypeOrId.uppercase()) {
            // Fuel & Energy (TC-G-001)
            "TC-001-001" -> "Petrol"
            "TC-001-002" -> "Diesel"
            "TC-001-003" -> "CNG / LPG"
            "TC-001-004" -> "EV Charging"

            // Toll & Parking (TC-G-002)
            "TC-002-001" -> "Toll Charges"
            "TC-002-002" -> "Parking Fees"
            "TC-002-003" -> "Entry Charges"

            // Loading & Unloading (TC-G-003)
            "TC-003-001" -> "Loading"
            "TC-003-002" -> "Unloading"
            "TC-003-003" -> "Crane/Forklift"
            "TC-003-004" -> "Labor"

            // Driver Expenses (TC-G-004)
            "TC-004-001" -> "Driver Allowance"
            "TC-004-002" -> "Food"
            "TC-004-003" -> "Accommodation"

            // Permits & Compliance (TC-G-005)
            "TC-005-001" -> "State Permit"
            "TC-005-002" -> "National Permit"
            "TC-005-003" -> "Special Permit"
            "TC-005-004" -> "Chalan/Fine"

            // Miscellaneous (TC-G-006)
            "TC-006-001" -> "Police"
            "TC-006-002" -> "RTO"
            "TC-006-003" -> "Weighbridge"
            "TC-006-004" -> "Commission"
            "TC-006-005" -> "Other"

            // Legacy cost types (fallback)
            else -> when (costTypeOrId.lowercase()) {
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
                else -> costTypeOrId.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
            }
        }
    }

    /**
     * Get emoji icon for a cost type or cost ID.
     *
     * @param costTypeOrId The cost type/ID identifier
     * @return Emoji icon representing the cost type
     */
    fun getIcon(costTypeOrId: String): String {
        // Handle new structured cost IDs first
        return when (costTypeOrId.uppercase()) {
            // Fuel & Energy (TC-G-001)
            "TC-001-001", "TC-001-002", "TC-001-003" -> "⛽"
            "TC-001-004" -> "🔌"

            // Toll & Parking (TC-G-002)
            "TC-002-001" -> "🛣️"
            "TC-002-002" -> "🅿️"
            "TC-002-003" -> "🚧"

            // Loading & Unloading (TC-G-003)
            "TC-003-001" -> "📦"
            "TC-003-002" -> "📤"
            "TC-003-003" -> "🏗️"
            "TC-003-004" -> "👷"

            // Driver Expenses (TC-G-004)
            "TC-004-001" -> "👤"
            "TC-004-002" -> "🍽️"
            "TC-004-003" -> "🏨"

            // Permits & Compliance (TC-G-005)
            "TC-005-001", "TC-005-002", "TC-005-003" -> "🎫"
            "TC-005-004" -> "📄"

            // Miscellaneous (TC-G-006)
            "TC-006-001" -> "👮"
            "TC-006-002" -> "🚗"
            "TC-006-003" -> "⚖️"
            "TC-006-004" -> "💼"
            "TC-006-005" -> "💵"

            // Legacy cost types (fallback)
            else -> when (costTypeOrId.lowercase()) {
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
    }

    /**
     * Get predefined color for a cost type or cost ID for visual differentiation.
     *
     * @param costTypeOrId The cost type/ID identifier
     * @return Color associated with the cost type
     */
    fun getColor(costTypeOrId: String): Color {
        // Handle new structured cost IDs - group by category
        return when {
            // Fuel & Energy (TC-G-001) - Green shades
            costTypeOrId.uppercase().startsWith("TC-001") -> Color(0xFF4CAF50)

            // Toll & Parking (TC-G-002) - Blue/Purple shades
            costTypeOrId.uppercase() == "TC-002-001" -> Color(0xFF2196F3) // Toll - Blue
            costTypeOrId.uppercase() == "TC-002-002" -> Color(0xFF9C27B0) // Parking - Purple
            costTypeOrId.uppercase() == "TC-002-003" -> Color(0xFF673AB7) // Entry - Deep Purple
            costTypeOrId.uppercase().startsWith("TC-002") -> Color(0xFF2196F3)

            // Loading & Unloading (TC-G-003) - Brown shades
            costTypeOrId.uppercase().startsWith("TC-003") -> Color(0xFF795548)

            // Driver Expenses (TC-G-004) - Orange shades
            costTypeOrId.uppercase().startsWith("TC-004") -> Color(0xFFFF9800)

            // Permits & Compliance (TC-G-005) - Cyan/Teal shades
            costTypeOrId.uppercase().startsWith("TC-005") -> Color(0xFF00BCD4)

            // Miscellaneous (TC-G-006) - Grey shades
            costTypeOrId.uppercase().startsWith("TC-006") -> Color(0xFF607D8B)

            // Legacy cost types (fallback)
            else -> when (costTypeOrId.lowercase()) {
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
     * Get icon for a cost based on group ID.
     *
     * @param groupId The group ID (e.g., "TC-G-001")
     * @return Emoji icon for the group
     */
    fun getIconByGroup(groupId: String?): String {
        return when (groupId?.uppercase()) {
            "TC-G-001" -> "⛽" // Fuel & Energy
            "TC-G-002" -> "🛣️" // Toll & Parking
            "TC-G-003" -> "📦" // Loading & Unloading
            "TC-G-004" -> "👤" // Driver Expenses
            "TC-G-005" -> "🎫" // Permits & Compliance
            "TC-G-006" -> "💵" // Miscellaneous
            else -> "💵"
        }
    }

    /**
     * Get color for a cost based on group ID.
     *
     * @param groupId The group ID (e.g., "TC-G-001")
     * @return Color for the group
     */
    fun getColorByGroup(groupId: String?): Color {
        return when (groupId?.uppercase()) {
            "TC-G-001" -> Color(0xFF4CAF50) // Fuel - Green
            "TC-G-002" -> Color(0xFF2196F3) // Toll/Parking - Blue
            "TC-G-003" -> Color(0xFF795548) // Loading/Unloading - Brown
            "TC-G-004" -> Color(0xFFFF9800) // Driver - Orange
            "TC-G-005" -> Color(0xFF00BCD4) // Permits - Cyan
            "TC-G-006" -> Color(0xFF607D8B) // Misc - Blue Grey
            else -> Color(0xFF9E9E9E) // Grey
        }
    }
}

/**
 * Composable extension to get cost type color with fallback to MaterialTheme.
 */
@Composable
fun getCostTypeColorComposable(costTypeOrId: String): Color {
    val predefinedColor = CostTypeUtils.getColor(costTypeOrId)
    return if (predefinedColor == Color(0xFF9E9E9E)) {
        MaterialTheme.colorScheme.primary
    } else {
        predefinedColor
    }
}

/**
 * Composable extension to get cost color by group ID.
 */
@Composable
fun getCostColorByGroupComposable(groupId: String?): Color {
    val predefinedColor = CostTypeUtils.getColorByGroup(groupId)
    return if (predefinedColor == Color(0xFF9E9E9E)) {
        MaterialTheme.colorScheme.primary
    } else {
        predefinedColor
    }
}
