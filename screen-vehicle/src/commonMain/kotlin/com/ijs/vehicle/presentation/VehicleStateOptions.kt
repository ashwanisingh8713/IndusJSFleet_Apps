package com.ijs.vehicle.presentation

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.uicomponents.components.StateOption
import com.indusjs.uicomponents.components.stateIconRes
import com.ijs.vehicle.domain.entity.VehicleStatus

/**
 * Vehicle-specific state option builder.
 * Uses DB-cached labels when available, falls back to StatusConstants.
 *
 * @param currentStatus Current vehicle status enum
 * @param stateLabels DB-cached state labels map (apiValue -> label). When non-empty, labels come from DB.
 */
fun getVehicleStateOptions(
    currentStatus: VehicleStatus,
    stateLabels: Map<String, String> = emptyMap()
): List<StateOption> {
    val currentApiValue = VehicleStatus.toApiString(currentStatus)
    val validTransitions = StatusConstants.VehicleTransitions.getValidTransitions(currentApiValue)

    return StatusConstants.VehicleState.ALL.map { stateValue ->
        StateOption(
            value = stateValue,
            label = stateLabels[stateValue] ?: StatusConstants.VehicleState.getDisplayLabel(stateValue),
            iconRes = stateIconRes(stateValue),
            colorScheme = StatusConstants.VehicleState.getColorScheme(stateValue),
            isCurrentState = stateValue == currentApiValue,
            isValidTransition = stateValue in validTransitions || stateValue == currentApiValue
        )
    }
}

