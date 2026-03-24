package com.ijs.trip.presentation

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.uicomponents.components.StateOption
import com.ijs.trip.domain.entity.TripStatus

/**
 * Trip-specific state option builder.
 */
fun getTripStateOptions(currentStatus: TripStatus): List<StateOption> {
    val currentApiValue = TripStatus.toApiString(currentStatus)
    val validTransitions = StatusConstants.TripTransitions.getValidTransitions(currentApiValue)

    return StatusConstants.TripState.ALL.map { stateValue ->
        StateOption(
            value = stateValue,
            label = StatusConstants.TripState.getDisplayLabel(stateValue),
            icon = StatusConstants.TripState.getIcon(stateValue),
            colorScheme = StatusConstants.TripState.getColorScheme(stateValue),
            isCurrentState = stateValue == currentApiValue,
            isValidTransition = stateValue in validTransitions || stateValue == currentApiValue
        )
    }
}

