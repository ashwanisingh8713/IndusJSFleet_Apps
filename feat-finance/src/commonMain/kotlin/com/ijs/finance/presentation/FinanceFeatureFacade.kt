package com.ijs.finance.presentation

import androidx.compose.runtime.Composable

/**
 * Facade for the Vehicle Finance feature module.
 *
 * All finance screens share a single [VehicleFinanceViewModel] instance,
 * which is created and managed by sharedUI via rememberSharedViewModel.
 * The Facade accepts this pre-created VM from the outside.
 */
object FinanceFeatureFacade {

    @Composable
    fun VehicleFinanceListEntry(
        viewModel: VehicleFinanceViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToDetail: (Int) -> Unit,
        onNavigateToAddPurchase: () -> Unit
    ) {
        VehicleFinanceScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAddPurchase = onNavigateToAddPurchase
        )
    }

    @Composable
    fun VehicleFinanceDetailEntry(
        viewModel: VehicleFinanceViewModel,
        vehicleId: Int,
        onNavigateBack: () -> Unit,
        onNavigateToEdit: () -> Unit,
        onNavigateToHistory: () -> Unit
    ) {
        VehicleFinanceDetailScreen(
            vehicleId = vehicleId,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit,
            onNavigateToHistory = onNavigateToHistory
        )
    }

    @Composable
    fun AddPurchaseInfoEntry(
        viewModel: VehicleFinanceViewModel,
        onNavigateBack: () -> Unit
    ) {
        AddPurchaseInfoScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }

    @Composable
    fun EmiPaymentHistoryEntry(
        viewModel: VehicleFinanceViewModel,
        vehicleId: Int,
        onNavigateBack: () -> Unit
    ) {
        EmiPaymentHistoryScreen(
            vehicleId = vehicleId,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

