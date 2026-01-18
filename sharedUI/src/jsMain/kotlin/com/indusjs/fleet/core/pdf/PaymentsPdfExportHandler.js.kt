package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.payments.PaymentsListPdfData
import com.indusjs.fleet.presentation.payments.PaymentReceiptPdfData

/**
 * JS implementation of PaymentsListPdfExportHandler.
 * TODO: Implement using JS PDF libraries
 */
@Composable
actual fun PaymentsListPdfExportHandler(
    pdfData: PaymentsListPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // JS implementation - placeholder
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}

/**
 * JS implementation of PaymentReceiptPdfExportHandler.
 * TODO: Implement using JS PDF libraries
 */
@Composable
actual fun PaymentReceiptPdfExportHandler(
    pdfData: PaymentReceiptPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // JS implementation - placeholder
    if (pdfData != null) {
        onExportError("PDF export is not yet available on Web")
    }
}
