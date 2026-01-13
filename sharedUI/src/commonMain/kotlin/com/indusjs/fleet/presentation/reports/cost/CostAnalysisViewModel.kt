package com.indusjs.fleet.presentation.reports.cost

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.data.model.reports.MultiCostTypePLRequest
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.COST_TYPES
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.Effect
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.Intent
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.State
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Cost Analysis Screen
 */
@Inject
class CostAnalysisViewModel(
    private val reportsRepository: ReportsRepository
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
            sendEffect(Effect.ShowSnackbar("Please select at least one cost type"))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val request = MultiCostTypePLRequest(
            costTypes = currentState.selectedCostTypes.toList(),
            startDate = currentState.startDate.takeIf { it.isNotBlank() },
            endDate = currentState.endDate.takeIf { it.isNotBlank() }
        )

        when (val result = reportsRepository.getMultiCostTypeAnalysis(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, results = result.data) }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message ?: "Failed to generate report") }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}

