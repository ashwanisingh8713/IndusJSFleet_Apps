package com.ijs.onboarding.presentation

import androidx.compose.runtime.Composable

/**
 * Facade for the Onboarding feature module.
 *
 * Provides a @Composable entry point for the onboarding screen.
 * The sharedUI module uses this facade to render the onboarding screen
 * without knowing internal implementation details.
 *
 * Navigation is handled via lambda callbacks — this module
 * never imports FleetRoute or any navigation infrastructure.
 *
 * ViewModel is passed from the outside (created by sharedUI's DI layer)
 * to maintain the existing rememberViewModel pattern.
 */
object OnboardingFeatureFacade {

    /**
     * Entry point for the Onboarding screen.
     */
    @Composable
    fun OnboardingEntry(
        viewModel: OnboardingViewModel,
        onComplete: () -> Unit
    ) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = onComplete
        )
    }
}

