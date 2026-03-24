package com.ijs.driver.presentation

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.uicomponents.components.StateOption
import com.ijs.driver.domain.entity.DriverStatus

/**
 * Driver-specific state option builder.
 */
fun getDriverStateOptions(currentStatus: DriverStatus): List<StateOption> {
    val currentApiValue = DriverStatus.toApiString(currentStatus)
    val validTransitions = StatusConstants.DriverTransitions.getValidTransitions(currentApiValue)

    return StatusConstants.DriverState.ALL.map { stateValue ->
        StateOption(
            value = stateValue,
            label = StatusConstants.DriverState.getDisplayLabel(stateValue),
            icon = StatusConstants.DriverState.getIcon(stateValue),
            colorScheme = StatusConstants.DriverState.getColorScheme(stateValue),
            isCurrentState = stateValue == currentApiValue,
            isValidTransition = stateValue in validTransitions || stateValue == currentApiValue
        )
    }
}

