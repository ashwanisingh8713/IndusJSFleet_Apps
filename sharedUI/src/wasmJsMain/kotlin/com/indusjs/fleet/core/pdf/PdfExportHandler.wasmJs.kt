package com.indusjs.fleet.core.pdf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.indusjs.pdfreport.model.TripCostsPdfData

/**
 * WasmJS implementation of PdfExportHandler.
 * TODO: Implement using JS PDF libraries like jsPDF or pdfmake
 */
@Composable
fun PdfExportHandler(
    pdfData: TripCostsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    LaunchedEffect(pdfData) {
        if (pdfData != null) {
            // WasmJS PDF export not yet implemented
            onExportError("PDF export is not yet available on Web. Coming soon!")
        }
    }
}
