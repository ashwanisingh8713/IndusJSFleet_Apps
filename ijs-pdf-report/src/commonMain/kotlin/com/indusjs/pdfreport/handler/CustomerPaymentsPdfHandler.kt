package com.indusjs.pdfreport.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.indusjs.pdfreport.PdfReportFacade
import com.indusjs.pdfreport.model.CustomerPaymentsPdfData
import com.indusjs.pdfreport.model.PdfExportResult
import com.indusjs.pdfreport.ui.PdfExportDialog
import kotlinx.coroutines.launch

/**
 * Composable handler for Customer Payments PDF export.
 *
 * @param pdfData The customer payments data to export (null when not exporting)
 * @param onExportComplete Called when export is complete or dialog dismissed
 * @param onExportError Called when export fails with error message
 */
@Composable
fun CustomerPaymentsPdfHandler(
    pdfData: CustomerPaymentsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    // Initialize context for Android
    InitializePdfContext()

    val scope = rememberCoroutineScope()
    var exportResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pdfData) {
        if (pdfData != null && !isExporting) {
            isExporting = true
            try {
                val result = PdfReportFacade.generateCustomerPaymentsReport(pdfData)
                exportResult = result
                if (result.success) {
                    showDialog = true
                } else {
                    onExportError(result.errorMessage ?: "Failed to generate PDF")
                    isExporting = false
                }
            } catch (e: Exception) {
                onExportError(e.message ?: "Failed to generate PDF")
                isExporting = false
            }
        }
    }

    if (showDialog && exportResult != null) {
        PdfExportDialog(
            result = exportResult!!,
            onDismiss = {
                showDialog = false
                isExporting = false
                exportResult = null
                onExportComplete()
            },
            onShare = {
                scope.launch {
                    exportResult?.let { PdfReportFacade.shareReport(it) }
                }
            },
            onOpen = {
                scope.launch {
                    exportResult?.let { PdfReportFacade.openReport(it) }
                }
            }
        )
    }
}
