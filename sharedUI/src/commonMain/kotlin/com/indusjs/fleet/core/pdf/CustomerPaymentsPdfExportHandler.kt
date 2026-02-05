package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.customers.detail.CustomerPaymentsPdfData

/**
 * Composable to handle Customer Payments PDF export.
 * Platform-specific implementation handles actual PDF generation.
 */
@Composable
expect fun CustomerPaymentsPdfExportHandler(
    pdfData: CustomerPaymentsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
)
