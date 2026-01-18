package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.payments.PaymentsListPdfData
import com.indusjs.fleet.presentation.payments.PaymentReceiptPdfData

/**
 * iOS implementation of PaymentsListPdfExportHandler.
 * TODO: Implement using iOS PDFKit
 */
@Composable
actual fun PaymentsListPdfExportHandler(
    pdfData: PaymentsListPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // iOS implementation - placeholder
    if (pdfData != null) {
        onExportError("PDF export is not yet available on iOS")
    }
}

/**
 * iOS implementation of PaymentReceiptPdfExportHandler.
 * TODO: Implement using iOS PDFKit
 */
@Composable
actual fun PaymentReceiptPdfExportHandler(
    pdfData: PaymentReceiptPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // iOS implementation - placeholder
    if (pdfData != null) {
        onExportError("PDF export is not yet available on iOS")
    }
}
