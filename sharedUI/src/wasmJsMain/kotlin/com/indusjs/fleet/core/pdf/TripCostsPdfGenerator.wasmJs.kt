package com.indusjs.fleet.core.pdf

import com.indusjs.pdfreport.model.TripCostsPdfData

/**
 * WasmJS implementation of TripCostsPdfGenerator.
 * TODO: Implement using JS PDF libraries like jsPDF or pdfmake
 */
class TripCostsPdfGenerator {

    fun generateAndSharePdf(
        pdfData: TripCostsPdfData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // For WasmJS, PDF generation would typically use jsPDF or pdfmake libraries
        // This would require additional JavaScript interop setup

        // For now, show a message that PDF export is not yet available on Web
        onError("PDF export is not yet available on Web. Coming soon!")
    }
}
