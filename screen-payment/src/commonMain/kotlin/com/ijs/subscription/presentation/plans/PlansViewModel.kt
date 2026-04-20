package com.ijs.subscription.presentation.plans

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
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
                        error = UiText.Raw(
                            plansResult.exceptionOrNull()?.message ?: "Failed to load plans. Please retry."
                        )
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
            sendEffect(Effect.ShowError(UiText.Raw("Please select a plan to continue.")))
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
                    val msg = e.message ?: "Failed to select plan. Please try again."
                    updateState { copy(isConfirming = false, error = UiText.Raw(msg)) }
                    sendEffect(Effect.ShowError(UiText.Raw(msg)))
                }
            )
        }
    }
}
