package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.pdfreport.model.CustomerTripsPdfData

/**
 * JS implementation of CustomerTripsPdfExportHandler.
 */
@Composable
fun CustomerTripsPdfExportHandler(
    pdfData: CustomerTripsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // JS implementation - not supported
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}
