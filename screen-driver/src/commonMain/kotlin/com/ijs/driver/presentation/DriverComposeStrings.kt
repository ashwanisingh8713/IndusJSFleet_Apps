package com.ijs.driver.presentation

import androidx.compose.runtime.Composable
import com.indusjs.fleet.core.constants.StatusConstants
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.entity.LicenseType
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Localized driver status strings for use in composables (avoids calling [stringResource] inside lambdas).
 */
@Composable
fun driverStatusLabelsByApi(): Map<String, String> {
    val inactive = stringResource(Res.string.driver_state_inactive)
    val active = stringResource(Res.string.driver_state_active)
    val onTrip = stringResource(Res.string.driver_state_on_trip)
    val onLeave = stringResource(Res.string.driver_state_on_leave)
    val suspended = stringResource(Res.string.driver_state_suspended)
    return mapOf(
        StatusConstants.DriverState.INACTIVE to inactive,
        StatusConstants.DriverState.ACTIVE to active,
        StatusConstants.DriverState.ON_TRIP to onTrip,
        StatusConstants.DriverState.ON_LEAVE to onLeave,
        StatusConstants.DriverState.SUSPENDED to suspended,
        // Legacy wire value tolerated on read; reuses the "On Trip" label.
        "on_route" to onTrip
    )
}

fun driverStatusLabel(status: DriverStatus, labelsByApi: Map<String, String>): String =
    labelsByApi[DriverStatus.toApiString(status)] ?: DriverStatus.getDisplayLabel(status)

@Composable
fun driverLicenseTypeLong(type: LicenseType): String = when (type) {
    LicenseType.LMV -> stringResource(Res.string.driver_license_type_lmv)
    LicenseType.HMV -> stringResource(Res.string.driver_license_type_hmv)
    LicenseType.MCWG -> stringResource(Res.string.driver_license_type_mcwg)
    LicenseType.MCWOG -> stringResource(Res.string.driver_license_type_mcwog)
}

@Composable
fun driverLicenseTypeShort(type: LicenseType): String = when (type) {
    LicenseType.LMV -> stringResource(Res.string.driver_license_type_short_lmv)
    LicenseType.HMV -> stringResource(Res.string.driver_license_type_short_hmv)
    LicenseType.MCWG -> stringResource(Res.string.driver_license_type_short_mcwg)
    LicenseType.MCWOG -> stringResource(Res.string.driver_license_type_short_mcwog)
}
