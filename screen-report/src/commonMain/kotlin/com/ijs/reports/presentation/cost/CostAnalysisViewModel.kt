package com.ijs.reports.presentation.cost

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.data.model.MultiCostTypePLRequest
import com.ijs.reports.domain.usecase.GetMultiCostTypeAnalysisUseCase
import com.ijs.reports.presentation.cost.CostAnalysisContract.COST_TYPES
import com.ijs.reports.presentation.cost.CostAnalysisContract.Effect
import com.ijs.reports.presentation.cost.CostAnalysisContract.Intent
import com.ijs.reports.presentation.cost.CostAnalysisContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_generate
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_select_cost_types
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_select_date_range

/**
 * ViewModel for Cost Analysis Screen
 */
@Inject
class CostAnalysisViewModel(
    private val getMultiCostTypeAnalysisUseCase: GetMultiCostTypeAnalysisUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadCostTypes -> loadCostTypes()
            is Intent.ToggleCostType -> toggleCostType(intent.costType)
            is Intent.SelectAllCostTypes -> updateState { copy(selectedCostTypes = allCostTypes.map { it.first }.toSet()) }
            is Intent.ClearCostTypes -> updateState { copy(selectedCostTypes = emptySet()) }
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.GenerateReport -> generateReport()
            is Intent.Refresh -> generateReport()
        }
    }

    private fun loadCostTypes() {
        // TODO: Load cost types from repository when CostTypesRepository is injected
        // For now, uses static fallback via State.allCostTypes computed property
    }

    private fun toggleCostType(costType: String) {
        updateState {
            val newSelection = if (selectedCostTypes.contains(costType)) {
                selectedCostTypes - costType
            } else {
                selectedCostTypes + costType
            }
            copy(selectedCostTypes = newSelection)
        }
    }

    private suspend fun generateReport() {
        val currentState = state.value

        if (currentState.selectedCostTypes.isEmpty()) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_select_cost_types)))
            return
        }

        // Picker state holds DD-MM-YYYY digits; convert to UTC epoch millis at the request boundary.
        // Backend requires non-zero start_date/end_date, so bail out if conversion fails.
        val startMillis = convertToEpochMillis(currentState.startDate)
        val endMillis = convertToEpochMillis(currentState.endDate)
        if (startMillis == null || endMillis == null) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_select_date_range)))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val request = MultiCostTypePLRequest(
            costIds = currentState.selectedCostTypes.toList(),
            startDate = startMillis,
            endDate = endMillis
        )

        when (val result = getMultiCostTypeAnalysisUseCase(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, results = result.data) }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_generate)) }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}

