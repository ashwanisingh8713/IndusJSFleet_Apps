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
    val onRoute = stringResource(Res.string.driver_state_on_route)
    val onLeave = stringResource(Res.string.driver_state_on_leave)
    val suspended = stringResource(Res.string.driver_state_suspended)
    val terminated = stringResource(Res.string.driver_state_terminated)
    return mapOf(
        StatusConstants.DriverState.INACTIVE to inactive,
        StatusConstants.DriverState.ACTIVE to active,
        StatusConstants.DriverState.ON_ROUTE to onRoute,
        StatusConstants.DriverState.ON_LEAVE to onLeave,
        StatusConstants.DriverState.SUSPENDED to suspended,
        StatusConstants.DriverState.TERMINATED to terminated,
        "on_trip" to onRoute
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
