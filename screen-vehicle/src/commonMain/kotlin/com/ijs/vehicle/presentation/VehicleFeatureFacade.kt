package com.ijs.vehicle.presentation

import androidx.compose.runtime.Composable
import com.ijs.vehicle.domain.entity.DocumentType
import com.ijs.vehicle.presentation.costs.MaintenanceCostEntryScreen
import com.ijs.vehicle.presentation.costs.MaintenanceCostEntryViewModel
import com.ijs.vehicle.presentation.detail.VehicleDetailScreen
import com.ijs.vehicle.presentation.detail.VehicleDetailViewModel

/**
 * Facade for the Vehicle feature module.
 *
 * Provides @Composable entry points for each vehicle screen.
 * Navigation is handled via lambda callbacks — never FleetRoute.
 * ViewModels are passed from the outside (created by sharedUI's DI layer).
 */
object VehicleFeatureFacade {

    @Composable
    fun VehiclesListEntry(
        viewModel: VehiclesViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToDetail: (String) -> Unit,
        onNavigateToAdd: () -> Unit
    ) {
        VehiclesScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAdd = onNavigateToAdd
        )
    }

    @Composable
    fun AddVehicleEntry(
        viewModel: AddVehicleViewModel,
        onNavigateBack: () -> Unit,
        onVehicleRegistered: (String) -> Unit,
        onNavigateToCreateTeamMember: () -> Unit,
        onRequestFilePicker: (DocumentType, (String, ByteArray, String) -> Unit) -> Unit
    ) {
        AddVehicleScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onVehicleRegistered = onVehicleRegistered,
            onNavigateToCreateTeamMember = onNavigateToCreateTeamMember,
            onRequestFilePicker = onRequestFilePicker
        )
    }

    @Composable
    fun VehicleDetailEntry(
        viewModel: VehicleDetailViewModel,
        vehicleId: String,
        onNavigateBack: () -> Unit,
        onNavigateToMaintenanceCost: (vehicleId: String) -> Unit,
        onRequestFilePicker: ((documentType: String, callback: (fileName: String, fileBytes: ByteArray, mimeType: String) -> Unit) -> Unit)? = null,
        onOpenDocumentPreview: ((documentName: String, fileUrl: String) -> Unit)? = null,
        onDownloadDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
        onSaveDocument: ((documentName: String, fileBytes: ByteArray, mimeType: String) -> Unit)? = null
    ) {
        VehicleDetailScreen(
            viewModel = viewModel,
            vehicleId = vehicleId,
            onNavigateBack = onNavigateBack,
            onNavigateToMaintenanceCost = onNavigateToMaintenanceCost,
            onRequestFilePicker = onRequestFilePicker,
            onOpenDocumentPreview = onOpenDocumentPreview,
            onDownloadDocument = onDownloadDocument,
            onSaveDocument = onSaveDocument
        )
    }

    @Composable
    fun MaintenanceCostEntryEntry(
        viewModel: MaintenanceCostEntryViewModel,
        initialVehicleId: String? = null,
        onNavigateBack: () -> Unit
    ) {
        MaintenanceCostEntryScreen(
            viewModel = viewModel,
            initialVehicleId = initialVehicleId,
            onNavigateBack = onNavigateBack
        )
    }
}

