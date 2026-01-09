package com.indusjs.fleet.core.pdf

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of TripCostsPdfGenerator.
 * Uses WebView to render HTML and convert to PDF.
 */
actual class TripCostsPdfGenerator {

    private var context: Context? = null

    /**
     * Set the context for PDF generation.
     * Must be called before generateAndSharePdf.
     */
    fun setContext(context: Context) {
        this.context = context
    }

    actual fun generateAndSharePdf(
        pdfData: TripDetailContract.TripCostsPdfData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val ctx = context
        if (ctx == null) {
            onError("Context not set")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val htmlContent = TripCostsPdfHtmlGenerator.generateHtml(pdfData)
                val fileName = "trip_costs_${pdfData.tripId}_${System.currentTimeMillis()}.pdf"

                // Generate PDF using WebView
                generatePdfFromHtml(ctx, htmlContent, fileName) { pdfFile ->
                    if (pdfFile != null) {
                        sharePdfFile(ctx, pdfFile, pdfData.tripNumber ?: "Trip Costs")
                        onSuccess()
                    } else {
                        onError("Failed to generate PDF")
                    }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    private fun generatePdfFromHtml(
        context: Context,
        htmlContent: String,
        fileName: String,
        callback: (File?) -> Unit
    ) {
        val webView = WebView(context)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowFileAccess = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Wait a bit for content to render
                webView.postDelayed({
                    try {
                        // Create PDF document
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()

                        val pdfDocument = PrintedPdfDocument(context, printAttributes)

                        // Create a page
                        val pageInfo = PdfDocument.PageInfo.Builder(
                            printAttributes.mediaSize?.widthMils?.div(1000) ?: 595,
                            printAttributes.mediaSize?.heightMils?.div(1000) ?: 842,
                            1
                        ).create()

                        val page = pdfDocument.startPage(pageInfo)

                        // Scale WebView to fit page
                        val scale = pageInfo.pageWidth.toFloat() / webView.width
                        page.canvas.scale(scale, scale)

                        // Draw WebView content
                        webView.draw(page.canvas)

                        pdfDocument.finishPage(page)

                        // Save PDF
                        val pdfDir = File(context.cacheDir, "pdfs")
                        if (!pdfDir.exists()) pdfDir.mkdirs()
                        val pdfFile = File(pdfDir, fileName)

                        FileOutputStream(pdfFile).use { outputStream ->
                            pdfDocument.writeTo(outputStream)
                        }

                        pdfDocument.close()
                        callback(pdfFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(null)
                    }
                }, 500)
            }
        }

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun sharePdfFile(context: Context, pdfFile: File, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Trip Costs Report - $title")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share PDF")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

