package com.ijs.reports.presentation.vehicle

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.reports.TAG_VEHICLE_PL_VM
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.fleet.core.util.currentTimeMillis
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.data.model.MultiVehiclePLRequest
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.domain.usecase.GetMultiVehiclePLUseCase
import com.ijs.reports.domain.usecase.GetVehicleProfitLossUseCase
import com.ijs.reports.presentation.RecentReport
import com.ijs.vehicle.domain.entity.Vehicle
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_generate
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_load_fleet_overview
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_invalid_vehicle_id
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_no_valid_ids
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_select_date_range

/**
 * Handles report generation logic for Vehicle P&L.
 * Extracted from VehiclePLViewModel to keep it under 500 lines.
 */
class VehiclePLReportGenerator(
    private val getVehicleProfitLossUseCase: GetVehicleProfitLossUseCase,
    private val getMultiVehiclePLUseCase: GetMultiVehiclePLUseCase,
    private val logger: FleetLogger
) {

    /**
     * Load fleet overview data for all (or filtered) vehicles.
     *
     * @return [FleetOverviewResult] containing either success data or error message.
     */
    suspend fun loadFleetOverview(
        vehicles: List<Vehicle>,
        selectedVehicleIds: Set<String>,
        period: String,
        customStartDate: String,
        customEndDate: String
    ): FleetOverviewResult {
        val allVehicleIds: List<Int> = vehicles.mapNotNull { it.id.toIntOrNull() }
        val selectedIds: List<Int> = selectedVehicleIds.mapNotNull { it.toIntOrNull() }
        val vehicleIdsToUse: List<Int> = if (selectedIds.isEmpty()) allVehicleIds else selectedIds

        if (vehicleIdsToUse.isEmpty()) {
            logger.d(TAG_VEHICLE_PL_VM, "loadFleetOverview: Empty vehicle IDs, returning empty")
            return FleetOverviewResult.Empty
        }

        val (startDate, endDate) = VehiclePLDateCalculator.getDateRangeForPeriod(
            period, customStartDate, customEndDate
        )
        logger.d(TAG_VEHICLE_PL_VM, "loadFleetOverview: Date range $startDate to $endDate, vehicles=${vehicleIdsToUse.size}")

        // Backend requires non-zero start_date/end_date on the multi-vehicle request.
        if (startDate == null || endDate == null) {
            logger.e(TAG_VEHICLE_PL_VM, "loadFleetOverview: could not resolve date range for period=$period")
            return FleetOverviewResult.Error(UiText.StringRes(Res.string.report_failed_load_fleet_overview))
        }

        val request = MultiVehiclePLRequest(
            vehicleIds = vehicleIdsToUse,
            startDate = startDate,
            endDate = endDate
        )

        return when (val result = getMultiVehiclePLUseCase(request)) {
            is Result.Success -> FleetOverviewResult.Success(result.data)
            is Result.Error -> FleetOverviewResult.Error(result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_load_fleet_overview))
            is Result.Loading -> FleetOverviewResult.Loading
        }
    }

    /**
     * Generate P&L report for a single vehicle.
     *
     * @return [SingleVehicleResult] containing either success data or error.
     */
    suspend fun generateSingleVehicleReport(
        vehicleId: String,
        period: String,
        vehicle: Vehicle?,
        existingRecentReports: List<RecentReport>
    ): SingleVehicleResult {
        val numericId = vehicleId.toIntOrNull()
            ?: return SingleVehicleResult.Error(UiText.StringRes(Res.string.report_invalid_vehicle_id))

        return when (val result = getVehicleProfitLossUseCase(numericId, period)) {
            is Result.Success -> {
                val plResult = result.data
                val updatedReports = if (vehicle != null && plResult != null) {
                    val recentReport = RecentReport(
                        vehicleId = vehicle.id,
                        vehicleNumber = vehicle.registrationNumber,
                        vehicleMakeModel = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim(),
                        period = period,
                        profitLoss = plResult.netProfit,
                        isProfit = plResult.netProfit >= 0,
                        generatedAt = currentTimeMillis()
                    )
                    listOf(recentReport) + existingRecentReports
                        .filter { it.vehicleId != vehicle.id || it.period != period }
                        .take(9)
                } else {
                    existingRecentReports
                }
                SingleVehicleResult.Success(plResult, updatedReports)
            }
            is Result.Error -> SingleVehicleResult.Error(result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_generate))
            is Result.Loading -> SingleVehicleResult.Loading
        }
    }

    /**
     * Generate P&L report for multiple vehicles.
     *
     * @return [MultiVehicleResult] containing either success data or error.
     */
    suspend fun generateMultiVehicleReport(
        vehicleIds: Set<String>,
        startDate: String,
        endDate: String
    ): MultiVehicleResult {
        val numericIds = vehicleIds.mapNotNull { it.toIntOrNull() }
        if (numericIds.isEmpty()) {
            return MultiVehicleResult.Error(UiText.StringRes(Res.string.report_no_valid_ids))
        }

        // Picker state holds DD-MM-YYYY; convert to UTC epoch millis at the request boundary.
        // Backend requires non-zero start_date/end_date, so bail out if either is missing/unparseable.
        val startMillis = startDate.takeIf { it.isNotBlank() }?.let { convertToEpochMillis(it) }
        val endMillis = endDate.takeIf { it.isNotBlank() }?.let { convertToEpochMillis(it) }
        if (startMillis == null || endMillis == null) {
            return MultiVehicleResult.Error(UiText.StringRes(Res.string.report_select_date_range))
        }

        val request = MultiVehiclePLRequest(
            vehicleIds = numericIds,
            startDate = startMillis,
            endDate = endMillis
        )

        return when (val result = getMultiVehiclePLUseCase(request)) {
            is Result.Success -> MultiVehicleResult.Success(result.data)
            is Result.Error -> MultiVehicleResult.Error(result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_generate))
            is Result.Loading -> MultiVehicleResult.Loading
        }
    }

    // Result types

    sealed interface FleetOverviewResult {
        data class Success(val data: List<VehicleProfitLoss>) : FleetOverviewResult
        data class Error(val message: UiText) : FleetOverviewResult
        data object Empty : FleetOverviewResult
        data object Loading : FleetOverviewResult
    }

    sealed interface SingleVehicleResult {
        data class Success(
            val data: VehicleProfitLoss?,
            val updatedRecentReports: List<RecentReport>
        ) : SingleVehicleResult
        data class Error(val message: UiText) : SingleVehicleResult
        data object Loading : SingleVehicleResult
    }

    sealed interface MultiVehicleResult {
        data class Success(val data: List<VehicleProfitLoss>) : MultiVehicleResult
        data class Error(val message: UiText) : MultiVehicleResult
        data object Loading : MultiVehicleResult
    }
}

