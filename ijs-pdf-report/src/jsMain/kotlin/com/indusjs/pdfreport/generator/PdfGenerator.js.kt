package com.indusjs.pdfreport.generator

import com.indusjs.pdfreport.model.PdfExportResult
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

/**
 * JS implementation of PdfGenerator.
 * Uses browser functionality to generate and download reports.
 */
actual class PdfGenerator {

    actual suspend fun generatePdf(
        html: String,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ): PdfExportResult {
        return try {
            onProgress?.invoke(0.1f)

            // Create a hidden iframe to render the HTML
            val iframe = document.createElement("iframe") as HTMLIFrameElement
            iframe.style.visibility = "hidden"
            iframe.style.position = "absolute"
            iframe.style.width = "210mm"
            iframe.style.height = "297mm"
            document.body?.appendChild(iframe)

            onProgress?.invoke(0.3f)

            // Write HTML content to iframe
            val iframeDoc = iframe.contentDocument ?: iframe.contentWindow?.asDynamic()?.document
            iframeDoc?.open()
            iframeDoc?.write(html)
            iframeDoc?.close()

            onProgress?.invoke(0.5f)

            // Create a downloadable HTML file as PDF is not directly possible without external library
            val blob = Blob(arrayOf(html), BlobPropertyBag(type = "text/html"))
            val url = URL.createObjectURL(blob)

            onProgress?.invoke(0.8f)

            // Trigger download
            val link = document.createElement("a") as HTMLAnchorElement
            link.href = url
            link.download = fileName.replace(".pdf", ".html")
            document.body?.appendChild(link)
            link.click()
            document.body?.removeChild(link)

            // Clean up
            document.body?.removeChild(iframe)
            URL.revokeObjectURL(url)

            onProgress?.invoke(1.0f)

            // Note: For proper PDF generation in JS, integrate jsPDF or html2pdf.js
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
        // In browser, sharing is done through Web Share API if available
        try {
            val navigator = window.navigator.asDynamic()
            val shareAvailable = navigator.share != undefined
            if (shareAvailable) {
                val shareData = js("{}")
                shareData.title = "PDF Report"
                shareData.text = "Sharing: ${result.fileName}"
                navigator.share(shareData)
            } else {
                // Fallback: just show an alert
                window.alert("File saved: ${result.fileName}")
            }
        } catch (e: Exception) {
            kotlin.js.console.log("Share not available: ${e.message}")
        }
    }

    actual suspend fun openPdf(result: PdfExportResult) {
        // In browser context, we can't really "open" a local file
        // The file was already downloaded
        window.alert("File downloaded: ${result.fileName}\nPlease check your downloads folder.")
    }
}
