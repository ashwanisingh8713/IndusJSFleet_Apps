package com.ijs.trip.presentation.cost.util

import com.indusjs.fleet.data.model.driver.CreateDriverCostRequest
import com.ijs.trip.presentation.cost.CostEntryRow

/**
 * Utility for mapping Trip Cost entries to Driver Cost entries.
 * Used when adding driver-related expenses in trip costs that should also
 * appear in the driver's cost history.
 *
 * Implements one-way sync: Trip Cost → Driver Cost
 * Driver expenses added to trips are automatically synced to the driver's cost history
 * with a reference to the trip for traceability.
 */
object TripCostToDriverCostMapper {

    // Trip Cost Group ID for Driver Expenses
    const val TRIP_DRIVER_EXPENSE_GROUP_ID = "TC-G-004"

    // Driver Cost Group ID for Other Allowances
    const val DRIVER_OTHER_ALLOWANCE_GROUP_ID = "DC-G-004"

    /**
     * Trip Cost ID to Driver Cost ID mapping.
     * TC-G-004 (Driver Expenses) → DC-G-004 (Other Allowances)
     *
     * Mapping:
     * - TC-004-001 (Driver Allowance) → DC-004-001 (Expense Reimbursement)
     * - TC-004-002 (Driver Food) → DC-004-002 (Travel Allowance)
     * - TC-004-003 (Driver Accommodation) → DC-004-002 (Travel Allowance)
     */
    private val costIdMapping = mapOf(
        // Driver Allowance → Expense Reimbursement
        "TC-004-001" to Pair("DC-004-001", "Driver Allowance (Trip)"),
        // Driver Food → Travel Allowance (closest match)
        "TC-004-002" to Pair("DC-004-002", "Driver Food (Trip)"),
        // Driver Accommodation → Travel Allowance
        "TC-004-003" to Pair("DC-004-002", "Driver Accommodation (Trip)")
    )

    /**
     * Check if a cost entry is a driver expense that should be synced.
     *
     * @param entry The cost entry row to check
     * @return true if the entry is from the Driver Expenses group (TC-G-004)
     */
    fun isDriverExpense(entry: CostEntryRow): Boolean {
        return entry.selectedGroupId == TRIP_DRIVER_EXPENSE_GROUP_ID ||
               entry.costType.startsWith("TC-004")
    }

    /**
     * Check if a cost type belongs to the driver expense group.
     *
     * @param groupId The group ID to check
     * @return true if the group ID is TC-G-004 (Driver Expenses)
     */
    fun isDriverExpenseGroup(groupId: String): Boolean {
        return groupId == TRIP_DRIVER_EXPENSE_GROUP_ID
    }

    /**
     * Check if a cost ID is a driver expense type.
     *
     * @param costId The cost ID to check (e.g., "TC-004-001")
     * @return true if the cost ID belongs to driver expenses
     */
    fun isDriverExpenseCostId(costId: String): Boolean {
        return costId.startsWith("TC-004")
    }

    /**
     * Create a Driver Cost request from a Trip Cost entry.
     *
     * @param entry The trip cost entry row
     * @param driverId The driver ID from the trip
     * @param tripId The trip ID for linking
     * @param date The date in DD-MM-YYYY format (for driver cost API)
     * @return CreateDriverCostRequest or null if not a driver expense
     */
    fun mapToDriverCost(
        entry: CostEntryRow,
        driverId: Int,
        tripId: Int,
        date: String
    ): CreateDriverCostRequest? {
        if (!isDriverExpense(entry)) return null

        val mapping = costIdMapping[entry.costType]
        val (driverCostId, defaultLabel) = mapping ?: Pair("DC-004-001", entry.costTypeLabel)

        // Use the original trip cost label, with trip reference
        val costLabel = if (entry.costTypeLabel.isNotBlank()) {
            entry.costTypeLabel
        } else {
            defaultLabel
        }

        // Extract month from date (DD-MM-YYYY → YYYY-MM)
        val month = extractMonthFromDate(date)

        return CreateDriverCostRequest(
            driverId = driverId,
            tripId = tripId,
            costId = driverCostId,
            costLabel = costLabel,
            groupId = DRIVER_OTHER_ALLOWANCE_GROUP_ID,
            customCostLabel = if (entry.isOtherCostType && entry.customCostTypeName.isNotBlank()) {
                entry.customCostTypeName
            } else null,
            amount = entry.amount.toDoubleOrNull() ?: 0.0,
            date = date, // DD-MM-YYYY format for driver cost API
            month = month,
            description = "Trip expense: $costLabel (Trip #$tripId)",
            notes = entry.notes.takeIf { it.isNotBlank() },
            isDeduction = false
        )
    }

    /**
     * Extract month (YYYY-MM) from date string (DD-MM-YYYY).
     *
     * @param date Date string in DD-MM-YYYY format
     * @return Month string in YYYY-MM format, or null if parsing fails
     */
    private fun extractMonthFromDate(date: String): String? {
        return try {
            val parts = date.split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}" // YYYY-MM
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the driver cost label for a given trip cost type.
     *
     * @param tripCostId The trip cost ID (e.g., "TC-004-001")
     * @param fallbackLabel Fallback label if no mapping exists
     * @return The driver cost label
     */
    fun getDriverCostLabel(tripCostId: String, fallbackLabel: String): String {
        return costIdMapping[tripCostId]?.second ?: fallbackLabel
    }

    /**
     * Get the driver cost ID for a given trip cost type.
     *
     * @param tripCostId The trip cost ID (e.g., "TC-004-001")
     * @return The driver cost ID (e.g., "DC-004-001") or default
     */
    fun getDriverCostId(tripCostId: String): String {
        return costIdMapping[tripCostId]?.first ?: "DC-004-001"
    }
}

