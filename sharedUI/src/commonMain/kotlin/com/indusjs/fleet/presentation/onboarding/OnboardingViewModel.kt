package com.indusjs.fleet.presentation.onboarding

import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.presentation.onboarding.OnboardingContract.Effect
import com.indusjs.fleet.presentation.onboarding.OnboardingContract.Intent
import com.indusjs.fleet.presentation.onboarding.OnboardingContract.State
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Inject

/**
 * ViewModel for the Onboarding screen.
 *
 * Manages page navigation and persists onboarding completion status
 * via [Settings] so the onboarding is shown only on first launch.
 */
@Inject
class OnboardingViewModel(
    private val settings: Settings
) : MviViewModel<State, Intent, Effect>(State()) {

    companion object {
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.NextPage -> nextPage()
            is Intent.PreviousPage -> previousPage()
            is Intent.Skip -> completeOnboarding()
            is Intent.GetStarted -> completeOnboarding()
            is Intent.GoToPage -> goToPage(intent.page)
        }
    }

    private fun nextPage() {
        val current = currentState.currentPage
        if (current < currentState.totalPages - 1) {
            val nextPage = current + 1
            updateState { copy(currentPage = nextPage) }
            sendEffect(Effect.AnimateToPage(nextPage))
        } else {
            completeOnboarding()
        }
    }

    private fun previousPage() {
        val current = currentState.currentPage
        if (current > 0) {
            val prevPage = current - 1
            updateState { copy(currentPage = prevPage) }
            sendEffect(Effect.AnimateToPage(prevPage))
        }
    }

    private fun goToPage(page: Int) {
        if (page in 0 until currentState.totalPages) {
            updateState { copy(currentPage = page) }
        }
    }

    private fun completeOnboarding() {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, true)
        sendEffect(Effect.NavigateToLogin)
    }
}

