package com.indusjs.pdfreport.generator

import com.indusjs.pdfreport.model.PdfExportResult

/**
 * Platform-specific PDF generator.
 * Uses HTML WebView rendering to generate PDF.
 */
expect class PdfGenerator() {

    /**
     * Generate PDF from HTML content.
     * @param html The HTML content to convert to PDF
     * @param fileName The desired filename for the PDF
     * @param onProgress Optional progress callback (0.0 to 1.0)
     * @return PdfExportResult with file path or error
     */
    suspend fun generatePdf(
        html: String,
        fileName: String,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult

    /**
     * Share a generated PDF file.
     * @param result The PdfExportResult from generatePdf
     */
    suspend fun sharePdf(result: PdfExportResult)

    /**
     * Open a generated PDF file.
     * @param result The PdfExportResult from generatePdf
     */
    suspend fun openPdf(result: PdfExportResult)
}
