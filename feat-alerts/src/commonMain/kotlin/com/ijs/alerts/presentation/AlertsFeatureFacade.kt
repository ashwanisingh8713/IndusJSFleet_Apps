package com.ijs.alerts.presentation

import androidx.compose.runtime.Composable

/**
 * Facade for the Alerts feature module.
 *
 * Provides @Composable entry points for the alerts screen.
 * The sharedUI module uses this facade to render alert screens
 * without knowing internal implementation details.
 *
 * Navigation is handled via lambda callbacks — this module
 * never imports FleetRoute or any navigation infrastructure.
 *
 * ViewModels are passed from the outside (created by sharedUI's DI layer)
 * to maintain the existing rememberViewModel pattern.
 */
object AlertsFeatureFacade {

    /**
     * Entry point for the Alerts List screen.
     */
    @Composable
    fun AlertsListEntry(
        viewModel: AlertsListViewModel,
        onNavigateBack: () -> Unit = {}
    ) {
        AlertsListScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

