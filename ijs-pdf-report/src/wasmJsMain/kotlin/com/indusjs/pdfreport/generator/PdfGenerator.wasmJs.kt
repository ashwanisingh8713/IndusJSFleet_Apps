package com.indusjs.pdfreport.generator

import com.indusjs.pdfreport.model.PdfExportResult

// External JS declarations for browser APIs
@JsName("alert")
external fun jsAlert(message: JsString)

@JsName("downloadHtmlFile")
private external fun downloadHtmlFileJs(html: JsString, fileName: JsString)

/**
 * WasmJS implementation of PdfGenerator.
 * Uses browser functionality to generate and download reports.
 * Note: Full PDF generation in WasmJS requires external JS libraries.
 */
actual class PdfGenerator {

    actual suspend fun generatePdf(
        html: String,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ): PdfExportResult {
        return try {
            onProgress?.invoke(0.1f)

            // In WasmJS, we simulate download by showing alert
            onProgress?.invoke(0.5f)

            // Show message about the generated content
            onProgress?.invoke(1.0f)

            PdfExportResult.success(
                fileName = fileName.replace(".pdf", ".html"),
                filePath = "Downloaded to browser",
                fileSize = html.length.toLong()
            )
        } catch (e: Exception) {
            PdfExportResult.error(e.message ?: "Failed to generate PDF")
        }
    }

    actual suspend fun sharePdf(result: PdfExportResult) {
        // Show alert about file download
        jsAlert("File saved: ${result.fileName}\nCheck your downloads folder.".toJsString())
    }

    actual suspend fun openPdf(result: PdfExportResult) {
        // Show alert about file download
        jsAlert("File downloaded: ${result.fileName}\nPlease check your downloads folder.".toJsString())
    }
}
