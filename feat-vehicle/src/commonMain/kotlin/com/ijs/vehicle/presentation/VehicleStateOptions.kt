package com.ijs.vehicle.presentation

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.uicomponents.components.StateOption
import com.ijs.vehicle.domain.entity.VehicleStatus

/**
 * Vehicle-specific state option builder.
 */
fun getVehicleStateOptions(currentStatus: VehicleStatus): List<StateOption> {
    val currentApiValue = VehicleStatus.toApiString(currentStatus)
    val validTransitions = StatusConstants.VehicleTransitions.getValidTransitions(currentApiValue)

    return StatusConstants.VehicleState.ALL.map { stateValue ->
        StateOption(
            value = stateValue,
            label = StatusConstants.VehicleState.getDisplayLabel(stateValue),
            icon = StatusConstants.VehicleState.getIcon(stateValue),
            colorScheme = StatusConstants.VehicleState.getColorScheme(stateValue),
            isCurrentState = stateValue == currentApiValue,
            isValidTransition = stateValue in validTransitions || stateValue == currentApiValue
        )
    }
}

