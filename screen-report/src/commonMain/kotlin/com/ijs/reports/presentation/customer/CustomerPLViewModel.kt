package com.ijs.reports.presentation.customer

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.domain.usecase.GetCustomerPLUseCase
import com.ijs.reports.presentation.customer.CustomerPLContract.Effect
import com.ijs.reports.presentation.customer.CustomerPLContract.Intent
import com.ijs.reports.presentation.customer.CustomerPLContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_load_report

/**
 * ViewModel for the P&L-by-customer report.
 */
@Inject
class CustomerPLViewModel(
    private val getCustomerPLUseCase: GetCustomerPLUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.Load)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Load -> load()
            is Intent.Refresh -> load()
            is Intent.SelectPeriod -> {
                if (intent.period != currentState.selectedPeriod) {
                    updateState { copy(selectedPeriod = intent.period) }
                    load()
                }
            }
        }
    }

    private suspend fun load() {
        updateState { copy(isLoading = true, error = null) }
        when (val result = getCustomerPLUseCase(currentState.selectedPeriod)) {
            is Result.Success -> updateState { copy(isLoading = false, report = result.data, error = null) }
            is Result.Error -> updateState { copy(isLoading = false, error = result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_load_report)) }
            is Result.Loading -> { /* not applicable */ }
        }
    }
}
