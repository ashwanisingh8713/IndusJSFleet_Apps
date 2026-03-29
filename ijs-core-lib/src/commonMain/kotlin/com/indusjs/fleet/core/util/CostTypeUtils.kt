package com.indusjs.fleet.core.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for cost type display formatting.
 * Used across Trip Costs, Maintenance Costs, and Driver Costs screens.
 *
 * Supports all three cost categories:
 * - Trip Cost structured IDs (TC-*): e.g., "TC-001-002" for Diesel
 * - Maintenance Cost structured IDs (MC-*): e.g., "MC-003-001" for Tyre Replacement
 * - Driver Cost structured IDs (DC-*): e.g., "DC-001-001" for Monthly Salary
 * - Legacy cost types: e.g., "fuel", "toll", "tyre"
 */
object CostTypeUtils {

    /**
     * Get human-readable display name for a cost type or cost ID.
     *
     * @param costTypeOrId The cost type/ID (e.g., "fuel", "TC-001-002", "MC-003-001", "DC-001-001")
     * @return Human-readable display name (e.g., "Fuel", "Diesel", "Tyre Replacement", "Monthly Salary")
     */
    fun getDisplayName(costTypeOrId: String): String {
        // Handle new structured cost IDs first
        return when (costTypeOrId.uppercase()) {
            // ==================== Trip Costs (TC-*) ====================

            // Fuel & Energy (TC-G-001)
            "TC-001-001" -> "Petrol"
            "TC-001-002" -> "Diesel"
            "TC-001-003" -> "CNG/LPG"
            "TC-001-004" -> "EV Charging"
            "TC-001-005" -> "AdBlue/DEF"

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
            "TC-005-003" -> "Chalan/Fine"
            "TC-005-004" -> "Weighbridge"

            // Miscellaneous (TC-G-006)
            "TC-006-001" -> "Police/RTO"
            "TC-006-002" -> "Commission"
            "TC-006-003" -> "Other"

            // ==================== Maintenance Costs (MC-*) ====================

            // Engine & Mechanical (MC-G-001)
            "MC-001-001" -> "Engine Repair"
            "MC-001-002" -> "Transmission"
            "MC-001-003" -> "Clutch/Brake"
            "MC-001-004" -> "Suspension"

            // Body & Exterior (MC-G-002)
            "MC-002-001" -> "Body Repair"
            "MC-002-002" -> "Paint Job"
            "MC-002-003" -> "Glass/Mirror"

            // Tyres & Wheels (MC-G-003)
            "MC-003-001" -> "Tyre Replacement"
            "MC-003-002" -> "Tyre Repair"
            "MC-003-003" -> "Wheel Alignment"
            "MC-003-004" -> "Wheel Balancing"

            // Electrical & Electronics (MC-G-004)
            "MC-004-001" -> "Battery"
            "MC-004-002" -> "Alternator/Starter"
            "MC-004-003" -> "Wiring/Lights"
            "MC-004-004" -> "AC Repair"

            // Fuel & Fluids (MC-G-005)
            "MC-005-001" -> "Engine Oil"
            "MC-005-002" -> "Coolant"
            "MC-005-003" -> "Brake Fluid"
            "MC-005-004" -> "Gear Oil"

            // Routine Service (MC-G-006)
            "MC-006-001" -> "Regular Service"
            "MC-006-002" -> "Washing/Cleaning"
            "MC-006-003" -> "Inspection"
            "MC-006-004" -> "Other"

            // ==================== Driver Costs (DC-*) ====================

            // Salary & Wages (DC-G-001)
            "DC-001-001" -> "Monthly Salary"
            "DC-001-002" -> "Daily Wages"
            "DC-001-003" -> "Overtime"
            "DC-001-004" -> "Holiday Pay"

            // Incentives & Bonuses (DC-G-002)
            "DC-002-001" -> "Trip Bonus"
            "DC-002-002" -> "Performance Bonus"
            "DC-002-003" -> "Fuel Savings"
            "DC-002-004" -> "On-Time Delivery"
            "DC-002-005" -> "Safety Bonus"

            // Deductions (DC-G-003)
            "DC-003-001" -> "Advance Recovery"
            "DC-003-002" -> "Damage Deduction"
            "DC-003-003" -> "Fine"
            "DC-003-004" -> "Loan EMI"
            "DC-003-005" -> "Insurance"

            // Other (DC-G-004)
            "DC-004-001" -> "Training"
            "DC-004-002" -> "Uniform"
            "DC-004-003" -> "Medical"
            "DC-004-004" -> "License Renewal"
            "DC-004-005" -> "Other"

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
            // ==================== Trip Costs (TC-*) ====================

            // Fuel & Energy (TC-G-001)
            "TC-001-001", "TC-001-002", "TC-001-003", "TC-001-005" -> "⛽"
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
            "TC-005-001", "TC-005-002" -> "🎫"
            "TC-005-003" -> "📄"  // Chalan/Fine
            "TC-005-004" -> "⚖️"  // Weighbridge

            // Miscellaneous (TC-G-006)
            "TC-006-001" -> "👮"  // Police/RTO
            "TC-006-002" -> "💼"  // Commission
            "TC-006-003" -> "💵"  // Other

            // ==================== Maintenance Costs (MC-*) ====================

            // Engine & Mechanical (MC-G-001)
            "MC-001-001", "MC-001-002", "MC-001-003", "MC-001-004" -> "⚙️"

            // Body & Exterior (MC-G-002)
            "MC-002-001", "MC-002-002", "MC-002-003" -> "🚗"

            // Tyres & Wheels (MC-G-003)
            "MC-003-001", "MC-003-002", "MC-003-003", "MC-003-004" -> "🛞"

            // Electrical & Electronics (MC-G-004)
            "MC-004-001" -> "🔋"        // Battery
            "MC-004-002", "MC-004-003" -> "⚡" // Alternator/Starter, Wiring/Lights
            "MC-004-004" -> "❄️"        // AC Repair

            // Fuel & Fluids (MC-G-005)
            "MC-005-001", "MC-005-002", "MC-005-003", "MC-005-004" -> "🛢️"

            // Routine Service (MC-G-006)
            "MC-006-001", "MC-006-002", "MC-006-003", "MC-006-004" -> "🔧"

            // ==================== Driver Costs (DC-*) ====================

            // Salary & Wages (DC-G-001)
            "DC-001-001", "DC-001-002", "DC-001-003", "DC-001-004" -> "💰"

            // Incentives & Bonuses (DC-G-002)
            "DC-002-001", "DC-002-002", "DC-002-003", "DC-002-004", "DC-002-005" -> "🏆"

            // Deductions (DC-G-003)
            "DC-003-001", "DC-003-002", "DC-003-003", "DC-003-004", "DC-003-005" -> "➖"

            // Other (DC-G-004)
            "DC-004-001" -> "📚"  // Training
            "DC-004-002" -> "👔"  // Uniform
            "DC-004-003" -> "🏥"  // Medical
            "DC-004-004" -> "🪪"  // License Renewal
            "DC-004-005" -> "💵"  // Other

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
        val upper = costTypeOrId.uppercase()
        // Handle new structured cost IDs - group by category prefix
        return when {
            // ==================== Trip Costs (TC-*) ====================

            // Fuel & Energy (TC-G-001) - Green shades
            upper.startsWith("TC-001") -> Color(0xFF4CAF50)

            // Toll & Parking (TC-G-002) - Blue/Purple shades
            upper == "TC-002-001" -> Color(0xFF2196F3) // Toll - Blue
            upper == "TC-002-002" -> Color(0xFF9C27B0) // Parking - Purple
            upper == "TC-002-003" -> Color(0xFF673AB7) // Entry - Deep Purple
            upper.startsWith("TC-002") -> Color(0xFF2196F3)

            // Loading & Unloading (TC-G-003) - Brown shades
            upper.startsWith("TC-003") -> Color(0xFF795548)

            // Driver Expenses (TC-G-004) - Orange shades
            upper.startsWith("TC-004") -> Color(0xFFFF9800)

            // Permits & Compliance (TC-G-005) - Cyan/Teal shades
            upper.startsWith("TC-005") -> Color(0xFF00BCD4)

            // Miscellaneous (TC-G-006) - Grey shades
            upper.startsWith("TC-006") -> Color(0xFF607D8B)

            // ==================== Maintenance Costs (MC-*) ====================

            // Engine & Mechanical (MC-G-001) - Light Green
            upper.startsWith("MC-001") -> Color(0xFF8BC34A)

            // Body & Exterior (MC-G-002) - Deep Orange
            upper.startsWith("MC-002") -> Color(0xFFFF5722)

            // Tyres & Wheels (MC-G-003) - Dark Grey
            upper.startsWith("MC-003") -> Color(0xFF424242)

            // Electrical & Electronics (MC-G-004) - varies per item
            upper == "MC-004-001" -> Color(0xFFFFEB3B) // Battery - Yellow
            upper == "MC-004-004" -> Color(0xFF03A9F4) // AC Repair - Light Blue
            upper.startsWith("MC-004") -> Color(0xFFFFC107) // Others - Amber

            // Fuel & Fluids (MC-G-005) - Teal
            upper.startsWith("MC-005") -> Color(0xFF009688)

            // Routine Service (MC-G-006) - Teal
            upper.startsWith("MC-006") -> Color(0xFF009688)

            // ==================== Driver Costs (DC-*) ====================

            // Salary & Wages (DC-G-001) - Indigo
            upper.startsWith("DC-001") -> Color(0xFF3F51B5)

            // Incentives & Bonuses (DC-G-002) - Green
            upper.startsWith("DC-002") -> Color(0xFF4CAF50)

            // Deductions (DC-G-003) - Red
            upper.startsWith("DC-003") -> Color(0xFFF44336)

            // Other (DC-G-004) - Blue Grey
            upper.startsWith("DC-004") -> Color(0xFF607D8B)

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
     * @param groupId The group ID (e.g., "TC-G-001", "MC-G-001", "DC-G-001")
     * @return Emoji icon for the group
     */
    fun getIconByGroup(groupId: String?): String {
        return when (groupId?.uppercase()) {
            // Trip Cost groups
            "TC-G-001" -> "⛽" // Fuel & Energy
            "TC-G-002" -> "🛣️" // Toll & Parking
            "TC-G-003" -> "📦" // Loading & Unloading
            "TC-G-004" -> "👤" // Driver Expenses
            "TC-G-005" -> "🎫" // Permits & Compliance
            "TC-G-006" -> "💵" // Miscellaneous

            // Maintenance Cost groups
            "MC-G-001" -> "⚙️" // Engine & Mechanical
            "MC-G-002" -> "🚗" // Body & Exterior
            "MC-G-003" -> "🛞" // Tyres & Wheels
            "MC-G-004" -> "⚡" // Electrical & Electronics
            "MC-G-005" -> "🛢️" // Fuel & Fluids
            "MC-G-006" -> "🔧" // Routine Service

            // Driver Cost groups
            "DC-G-001" -> "💰" // Salary & Wages
            "DC-G-002" -> "🏆" // Incentives & Bonuses
            "DC-G-003" -> "➖" // Deductions
            "DC-G-004" -> "📋" // Other

            else -> "💵"
        }
    }

    /**
     * Get color for a cost based on group ID.
     *
     * @param groupId The group ID (e.g., "TC-G-001", "MC-G-001", "DC-G-001")
     * @return Color for the group
     */
    fun getColorByGroup(groupId: String?): Color {
        return when (groupId?.uppercase()) {
            // Trip Cost groups
            "TC-G-001" -> Color(0xFF4CAF50) // Fuel - Green
            "TC-G-002" -> Color(0xFF2196F3) // Toll/Parking - Blue
            "TC-G-003" -> Color(0xFF795548) // Loading/Unloading - Brown
            "TC-G-004" -> Color(0xFFFF9800) // Driver - Orange
            "TC-G-005" -> Color(0xFF00BCD4) // Permits - Cyan
            "TC-G-006" -> Color(0xFF607D8B) // Misc - Blue Grey

            // Maintenance Cost groups
            "MC-G-001" -> Color(0xFF8BC34A) // Engine & Mechanical - Light Green
            "MC-G-002" -> Color(0xFFFF5722) // Body & Exterior - Deep Orange
            "MC-G-003" -> Color(0xFF424242) // Tyres & Wheels - Dark Grey
            "MC-G-004" -> Color(0xFFFFC107) // Electrical - Amber
            "MC-G-005" -> Color(0xFF009688) // Fuel & Fluids - Teal
            "MC-G-006" -> Color(0xFF009688) // Routine Service - Teal

            // Driver Cost groups
            "DC-G-001" -> Color(0xFF3F51B5) // Salary - Indigo
            "DC-G-002" -> Color(0xFF4CAF50) // Bonuses - Green
            "DC-G-003" -> Color(0xFFF44336) // Deductions - Red
            "DC-G-004" -> Color(0xFF607D8B) // Other - Blue Grey

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
