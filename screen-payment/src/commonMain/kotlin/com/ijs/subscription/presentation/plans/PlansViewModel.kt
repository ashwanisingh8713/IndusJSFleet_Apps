package com.ijs.subscription.presentation.plans

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.err_load_plans
import indusjsfleet.ijs_ui_components_lib.generated.resources.err_select_plan
import indusjsfleet.ijs_ui_components_lib.generated.resources.plans_select_to_continue
import com.ijs.subscription.TAG_PLANS_VM
import com.ijs.subscription.domain.usecase.GetOnboardingStatusUseCase
import com.ijs.subscription.domain.usecase.GetPlansUseCase
import com.ijs.subscription.domain.usecase.SelectPlanUseCase
import com.ijs.subscription.presentation.plans.PlansContract.Effect
import com.ijs.subscription.presentation.plans.PlansContract.Intent
import com.ijs.subscription.presentation.plans.PlansContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

@Inject
class PlansViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getPlansUseCase: GetPlansUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val selectPlanUseCase: SelectPlanUseCase,
    private val pageMode: PlanPageMode = PlanPageMode.CHOOSE
) : MviViewModel<State, Intent, Effect>(State(pageMode = pageMode)) {

    init {
        sendIntent(Intent.LoadPlans)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadPlans, is Intent.RetryLoad -> loadPlansAndStatus()
            is Intent.SelectPlan -> updateState { copy(selectedPlan = intent.plan) }
            is Intent.ChangeBillingInterval -> updateState { copy(billingInterval = intent.interval) }
            is Intent.ConfirmPlanSelection -> confirmSelection()
            is Intent.Logout -> sendEffect(Effect.NavigateToLogin)
        }
    }

    private suspend fun loadPlansAndStatus() {
        updateState { copy(isLoading = true, error = null) }
        withContext(dispatcherProvider.io) {
            val plansResult = getPlansUseCase()
            val statusResult = getOnboardingStatusUseCase()

            if (plansResult.isFailure) {
                updateState {
                    copy(
                        isLoading = false,
                        error = plansResult.exceptionOrNull()?.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.err_load_plans)
                    )
                }
                return@withContext
            }

            val plans = plansResult.getOrDefault(emptyList()).filter { it.isActive }
            val preSelectedPlan = statusResult.getOrNull()?.selectedPlan

            updateState {
                copy(
                    isLoading = false,
                    plans = plans,
                    preSelectedPlan = preSelectedPlan,
                    selectedPlan = preSelectedPlan ?: plans.firstOrNull { !it.isFree }
                )
            }
        }
    }

    private suspend fun confirmSelection() {
        val plan = currentState.selectedPlan ?: run {
            sendEffect(Effect.ShowError(UiText.StringRes(Res.string.plans_select_to_continue)))
            return
        }
        val interval = currentState.billingInterval

        updateState { copy(isConfirming = true, error = null) }

        withContext(dispatcherProvider.io) {
            selectPlanUseCase(plan.id).fold(
                onSuccess = {
                    updateState { copy(isConfirming = false) }
                    if (plan.isFree) {
                        sendEffect(Effect.NavigateToDashboard)
                    } else {
                        sendEffect(Effect.NavigateToPaymentCheckout(plan, interval))
                    }
                },
                onFailure = { e ->
                    val err = e.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.err_select_plan)
                    updateState { copy(isConfirming = false, error = err) }
                    sendEffect(Effect.ShowError(err))
                }
            )
        }
    }
}
