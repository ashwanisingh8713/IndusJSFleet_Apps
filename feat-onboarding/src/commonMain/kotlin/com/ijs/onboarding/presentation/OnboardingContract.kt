package com.ijs.onboarding.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState

/**
 * MVI Contract for the Onboarding screen.
 *
 * Handles a multi-page onboarding flow shown only on first app launch.
 * Pages highlight key fleet management features with illustrations and descriptions.
 */
object OnboardingContract {

    /**
     * UI State for the Onboarding screen.
     */
    data class State(
        val currentPage: Int = 0,
        val totalPages: Int = 4
    ) : UiState {
        val isFirstPage: Boolean get() = currentPage == 0
        val isLastPage: Boolean get() = currentPage == totalPages - 1
        val progress: Float get() = (currentPage + 1).toFloat() / totalPages
    }

    /**
     * User intents for the Onboarding screen.
     */
    sealed interface Intent : UiIntent {
        data object NextPage : Intent
        data object PreviousPage : Intent
        data object Skip : Intent
        data object GetStarted : Intent
        data class GoToPage(val page: Int) : Intent
    }

    /**
     * Side effects for the Onboarding screen.
     */
    sealed interface Effect : UiEffect {
        data object NavigateToLogin : Effect
        data class AnimateToPage(val page: Int) : Effect
    }
}

