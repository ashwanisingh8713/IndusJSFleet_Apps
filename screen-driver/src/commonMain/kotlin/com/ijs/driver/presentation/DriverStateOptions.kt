package com.ijs.driver.presentation

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.uicomponents.components.StateOption
import com.indusjs.uicomponents.components.stateIconRes
import com.ijs.driver.domain.entity.DriverStatus

/**
 * Driver-specific state option builder.
 * Uses DB-cached labels when available, falls back to StatusConstants.
 *
 * @param currentStatus Current driver status enum
 * @param stateLabels DB-cached state labels map (apiValue -> label). When non-empty, labels come from DB.
 */
fun getDriverStateOptions(
    currentStatus: DriverStatus,
    stateLabels: Map<String, String> = emptyMap()
): List<StateOption> {
    val currentApiValue = DriverStatus.toApiString(currentStatus)
    val validTransitions = StatusConstants.DriverTransitions.getValidTransitions(currentApiValue)

    return StatusConstants.DriverState.ALL.map { stateValue ->
        StateOption(
            value = stateValue,
            label = stateLabels[stateValue] ?: StatusConstants.DriverState.getDisplayLabel(stateValue),
            iconRes = stateIconRes(stateValue),
            colorScheme = StatusConstants.DriverState.getColorScheme(stateValue),
            isCurrentState = stateValue == currentApiValue,
            isValidTransition = stateValue in validTransitions || stateValue == currentApiValue
        )
    }
}

