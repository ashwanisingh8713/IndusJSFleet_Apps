package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract

/**
 * iOS implementation of PdfExportHandler.
 * Currently shows a placeholder message - full implementation would use native iOS APIs.
 */
@Composable
actual fun PdfExportHandler(
    pdfData: TripDetailContract.TripCostsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    LaunchedEffect(pdfData) {
        if (pdfData != null) {
            // iOS PDF export not yet implemented
            onExportError("PDF export is not yet available on iOS. Coming soon!")
        }
    }
}

