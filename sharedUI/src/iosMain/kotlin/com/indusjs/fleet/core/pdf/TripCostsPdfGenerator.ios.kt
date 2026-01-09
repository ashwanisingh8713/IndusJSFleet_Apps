package com.indusjs.fleet.core.pdf

import com.indusjs.fleet.presentation.trips.detail.TripDetailContract

/**
 * iOS implementation of TripCostsPdfGenerator.
 * Uses a placeholder implementation - full PDF generation would use native iOS APIs.
 */
actual class TripCostsPdfGenerator {

    actual fun generateAndSharePdf(
        pdfData: TripDetailContract.TripCostsPdfData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // For iOS, PDF generation requires platform-specific implementation
        // using UIPrintPageRenderer and UIActivityViewController
        // This would typically be implemented using Swift/UIKit interop

        // For now, show a message that PDF export is not yet available on iOS
        onError("PDF export is not yet available on iOS. Coming soon!")
    }
}

