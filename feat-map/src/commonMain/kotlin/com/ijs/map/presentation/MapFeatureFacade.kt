package com.ijs.map.presentation

import androidx.compose.runtime.Composable

/**
 * Facade for the Map feature module.
 *
 * Provides @Composable entry points for the maps screen.
 * The sharedUI module uses this facade to render map screens
 * without knowing internal implementation details.
 *
 * Navigation is handled via lambda callbacks — this module
 * never imports FleetRoute or any navigation infrastructure.
 *
 * ViewModels are passed from the outside (created by sharedUI's DI layer)
 * to maintain the existing rememberViewModel pattern.
 */
object MapFeatureFacade {

    /**
     * Entry point for the Maps/Live Tracking screen.
     */
    @Composable
    fun MapsEntry(
        viewModel: MapsViewModel,
        onNavigateToVehicleDetail: (String) -> Unit = {},
        onNavigateBack: () -> Unit = {}
    ) {
        MapsScreen(
            viewModel = viewModel,
            onNavigateToVehicleDetail = onNavigateToVehicleDetail,
            onNavigateBack = onNavigateBack
        )
    }
}

