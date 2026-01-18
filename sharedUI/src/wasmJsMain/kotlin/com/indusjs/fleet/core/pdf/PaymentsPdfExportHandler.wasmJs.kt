package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.payments.PaymentsListPdfData
import com.indusjs.fleet.presentation.payments.PaymentReceiptPdfData

/**
 * WasmJs implementation of PaymentsListPdfExportHandler.
 * TODO: Implement using JS PDF libraries
 */
@Composable
actual fun PaymentsListPdfExportHandler(
    pdfData: PaymentsListPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // WasmJs implementation - placeholder
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}

/**
 * WasmJs implementation of PaymentReceiptPdfExportHandler.
 * TODO: Implement using JS PDF libraries
 */
@Composable
actual fun PaymentReceiptPdfExportHandler(
    pdfData: PaymentReceiptPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // WasmJs implementation - placeholder
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}
