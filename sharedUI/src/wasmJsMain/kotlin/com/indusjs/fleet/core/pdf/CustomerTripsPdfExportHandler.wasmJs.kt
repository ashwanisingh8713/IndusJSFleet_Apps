package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.customers.detail.CustomerTripsPdfData

/**
 * WasmJS implementation of CustomerTripsPdfExportHandler.
 */
@Composable
actual fun CustomerTripsPdfExportHandler(
    pdfData: CustomerTripsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // WasmJS implementation - not supported
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}
