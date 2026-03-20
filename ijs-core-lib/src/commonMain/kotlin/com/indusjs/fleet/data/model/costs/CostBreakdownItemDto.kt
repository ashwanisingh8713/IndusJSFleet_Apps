package com.indusjs.fleet.data.model.costs

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cost Breakdown Item DTO — unified version.
 * Consolidated from ijs-reports-lib and ijs-network-lib into ijs-core-lib
 * so it can be used by ijs-ui-components-lib and any feature module.
 *
 * Includes both structured cost fields (cost_id, cost_label, group_id)
 * and legacy field (cost_type) for backward compatibility.
 */
@Serializable
data class CostBreakdownItemDto(
    @SerialName("cost_id")
    val costId: String? = null,
    @SerialName("cost_label")
    val costLabel: String? = null,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("cost_type")
    val costType: String = "",
    @SerialName("amount")
    val amount: Double = 0.0,
    @SerialName("count")
    val count: Int = 0,
    @SerialName("percentage")
    val percentage: Double = 0.0
) : Dto {
    /**
     * Returns the display label — prefers cost_label, falls back to cost_type.
     */
    val displayLabel: String
        get() = costLabel ?: costType.replace("_", " ")
            .replaceFirstChar { it.uppercase() }
}

