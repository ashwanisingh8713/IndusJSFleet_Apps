package com.indusjs.fleet.presentation

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.uicomponents.components.StateOption
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.vehicle.domain.entity.VehicleStatus

/**
 * Feature-specific state option builders.
 *
 * TEMPORARY: These functions live here until each feature's presentation
 * layer is migrated into its respective feat-* module, at which point
 * they move into that module's presentation package.
 */

// ==================== Vehicle State Helpers ====================

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

// ==================== Driver State Helpers ====================

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

// ==================== Trip State Helpers ====================

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

