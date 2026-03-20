package com.indusjs.fleet.data.model.costs

/**
 * Result of cost type selection containing all relevant data.
 * Moved from sharedUI/core/ui/ to ijs-core-lib for cross-module access.
 *
 * Represents a user's selection of a cost type from the two-level selector
 * (category → item). Contains both the selected item and its parent group info.
 */
data class CostTypeSelection(
    val costTypeId: String,
    val costTypeLabel: String,
    val groupId: String,
    val groupName: String
) {
    companion object {
        // Group IDs for reliable category detection
        const val FUEL_ENERGY_GROUP_ID = "TC-G-001"
        const val MISCELLANEOUS_GROUP_ID = "TC-G-006"
        const val OTHER_COST_TYPE_ID = "TC-006-004"
    }

    val isFuelCategory: Boolean get() = groupId == FUEL_ENERGY_GROUP_ID
    val isOtherCostType: Boolean get() = costTypeId == OTHER_COST_TYPE_ID
}

