package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.payments.PaymentsListPdfData
import com.indusjs.fleet.presentation.payments.PaymentReceiptPdfData

/**
 * Composable to handle Payments PDF export.
 * Supports both list report and individual receipt.
 */
@Composable
expect fun PaymentsListPdfExportHandler(
    pdfData: PaymentsListPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
)

/**
 * Composable to handle single Payment Receipt PDF export.
 */
@Composable
expect fun PaymentReceiptPdfExportHandler(
    pdfData: PaymentReceiptPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
)
