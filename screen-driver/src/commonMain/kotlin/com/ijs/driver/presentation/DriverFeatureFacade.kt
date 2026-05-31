package com.ijs.driver.presentation

import androidx.compose.runtime.Composable
import com.ijs.driver.presentation.cost.DriverCostEntryScreen
import com.ijs.driver.presentation.cost.DriverCostEntryViewModel
import com.ijs.driver.presentation.create.CreateDriverScreen
import com.ijs.driver.presentation.create.CreateDriverViewModel
import com.ijs.driver.presentation.detail.DriverDetailScreen
import com.ijs.driver.presentation.detail.DriverDetailViewModel

/**
 * Facade for the Driver feature module.
 */
object DriverFeatureFacade {

    @Composable
    fun DriversListEntry(
        viewModel: DriversViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToDetail: (String) -> Unit,
        onNavigateToAdd: () -> Unit
    ) {
        DriversScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAdd = onNavigateToAdd
        )
    }

    @Composable
    fun CreateDriverEntry(
        viewModel: CreateDriverViewModel,
        onNavigateBack: () -> Unit,
        onDriverCreated: (String) -> Unit
    ) {
        CreateDriverScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onDriverCreated = onDriverCreated
        )
    }

    @Composable
    fun DriverDetailEntry(
        viewModel: DriverDetailViewModel,
        driverId: String,
        onNavigateBack: () -> Unit,
        onNavigateToAddDriverCost: (String) -> Unit
    ) {
        DriverDetailScreen(
            viewModel = viewModel,
            driverId = driverId,
            onNavigateBack = onNavigateBack,
            onNavigateToAddDriverCost = onNavigateToAddDriverCost
        )
    }

    @Composable
    fun DriverCostEntryEntry(
        viewModel: DriverCostEntryViewModel,
        initialDriverId: String? = null,
        onNavigateBack: () -> Unit,
        onCostsSaved: (driverId: String?, count: Int) -> Unit = { _, _ -> }
    ) {
        DriverCostEntryScreen(
            viewModel = viewModel,
            initialDriverId = initialDriverId,
            onNavigateBack = onNavigateBack,
            onCostsSaved = onCostsSaved
        )
    }
}

