package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.customers.detail.CustomerTripsPdfData

/**
 * Composable to handle Customer Trips PDF export.
 * Platform-specific implementation handles actual PDF generation.
 */
@Composable
expect fun CustomerTripsPdfExportHandler(
    pdfData: CustomerTripsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
)
