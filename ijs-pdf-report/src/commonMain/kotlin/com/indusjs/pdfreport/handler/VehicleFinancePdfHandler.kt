package com.indusjs.pdfreport.handler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.indusjs.pdfreport.PdfReportFacade
import com.indusjs.pdfreport.model.VehicleFinancePdfData
import com.indusjs.pdfreport.model.PdfExportResult
import com.indusjs.pdfreport.ui.PdfExportDialog
import kotlinx.coroutines.launch

/**
 * Composable handler for Vehicle Finance PDF export.
 *
 * @param pdfData The vehicle finance data to export (null when not exporting)
 * @param onExportComplete Called when export is complete or dialog dismissed
 * @param onExportError Called when export fails with error message
 */
@Composable
fun VehicleFinancePdfHandler(
    pdfData: VehicleFinancePdfData?,
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
                val result = PdfReportFacade.generateVehicleFinanceReport(pdfData)
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
        val currentResult = exportResult!! // Capture the result before any callbacks
        PdfExportDialog(
            result = currentResult,
            onDismiss = {
                showDialog = false
                isExporting = false
                exportResult = null
                onExportComplete()
            },
            onShare = {
                scope.launch {
                    PdfReportFacade.shareReport(currentResult)
                }
            },
            onOpen = {
                scope.launch {
                    PdfReportFacade.openReport(currentResult)
                }
            }
        )
    }
}
