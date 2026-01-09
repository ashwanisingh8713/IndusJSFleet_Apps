package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract

/**
 * Composable to handle PDF export effect.
 * Platform-specific implementation handles actual PDF generation.
 */
@Composable
expect fun PdfExportHandler(
    pdfData: TripDetailContract.TripCostsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
)

