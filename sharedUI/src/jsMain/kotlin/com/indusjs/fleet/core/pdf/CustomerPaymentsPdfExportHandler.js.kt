package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.pdfreport.model.CustomerPaymentsPdfData

/**
 * JS implementation of CustomerPaymentsPdfExportHandler.
 */
@Composable
fun CustomerPaymentsPdfExportHandler(
    pdfData: CustomerPaymentsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}
