package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.customers.detail.CustomerPaymentsPdfData

/**
 * WasmJS implementation of CustomerPaymentsPdfExportHandler.
 */
@Composable
actual fun CustomerPaymentsPdfExportHandler(
    pdfData: CustomerPaymentsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}
