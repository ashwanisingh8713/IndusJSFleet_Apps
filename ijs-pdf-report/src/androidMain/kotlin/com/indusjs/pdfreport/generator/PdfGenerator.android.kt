package com.indusjs.pdfreport.generator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.indusjs.pdfreport.PdfContextInitializer
import com.indusjs.pdfreport.model.PdfExportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

private const val TAG = "PdfGenerator"
private const val PDF_DIRECTORY = "IndusJSFleet/exportedPdf"

/**
 * Android implementation of PdfGenerator.
 * Uses WebView to render HTML and PrintDocumentAdapter to generate PDF.
 */
actual class PdfGenerator {

    private var webView: WebView? = null

    /**
     * Get Android context from PdfContextInitializer.
     */
    private fun getContext(): Context? = PdfContextInitializer.getContext()

    actual suspend fun generatePdf(
        html: String,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ): PdfExportResult = withContext(Dispatchers.Main) {
        val ctx = getContext() ?: return@withContext PdfExportResult.error("Context not set. Call PdfContextInitializer.initialize() first.")

        try {
            onProgress?.invoke(0.1f)

            // Create PDF directory
            val pdfDir = getPdfDirectory()
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }
            onProgress?.invoke(0.2f)

            val pdfFile = File(pdfDir, fileName)

            // Create WebView and load HTML
            val result = suspendCancellableCoroutine<PdfExportResult> { continuation ->
                val webView = WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = true
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                }

                this@PdfGenerator.webView = webView

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "WebView page loaded, generating PDF...")
                        onProgress?.invoke(0.5f)

                        // Generate PDF using PrintDocumentAdapter
                        try {
                            val printManager = ctx.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            val printAdapter = webView.createPrintDocumentAdapter(fileName)

                            // Use custom print adapter to save to file
                            val attributes = PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build()

                            // Save PDF directly using the print adapter
                            savePdfFromWebView(webView, pdfFile, onProgress) { success, error ->
                                if (success && pdfFile.exists()) {
                                    Log.d(TAG, "PDF saved: ${pdfFile.absolutePath}")
                                    continuation.resume(
                                        PdfExportResult.success(
                                            fileName = fileName,
                                            filePath = pdfFile.absolutePath,
                                            fileSize = pdfFile.length()
                                        )
                                    )
                                } else {
                                    Log.e(TAG, "PDF generation failed: $error")
                                    continuation.resume(PdfExportResult.error(error ?: "Failed to generate PDF"))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error generating PDF", e)
                            continuation.resume(PdfExportResult.error(e.message ?: "Failed to generate PDF"))
                        }
                    }
                }

                // Load HTML content
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                onProgress?.invoke(0.3f)

                continuation.invokeOnCancellation {
                    webView.destroy()
                    this@PdfGenerator.webView = null
                }
            }

            // Clean up
            webView?.destroy()
            webView = null

            result
        } catch (e: Exception) {
            Log.e(TAG, "PDF generation error", e)
            PdfExportResult.error(e.message ?: "Failed to generate PDF")
        }
    }

    /**
     * Save PDF from WebView using print functionality.
     */
    private fun savePdfFromWebView(
        webView: WebView,
        outputFile: File,
        onProgress: ((Float) -> Unit)?,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            val printAdapter = webView.createPrintDocumentAdapter(outputFile.nameWithoutExtension)

            val attributes = PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                .build()

            // For Android, we need to use a different approach since createPrintDocumentAdapter
            // doesn't directly save to file. We'll use PdfDocument API instead.
            generatePdfUsingPdfDocument(webView, outputFile, onProgress, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error in savePdfFromWebView", e)
            callback(false, e.message)
        }
    }

    /**
     * Generate PDF using Android's PdfDocument API.
     */
    private fun generatePdfUsingPdfDocument(
        webView: WebView,
        outputFile: File,
        onProgress: ((Float) -> Unit)?,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            onProgress?.invoke(0.6f)

            val pdfDocument = android.graphics.pdf.PdfDocument()

            // Calculate page dimensions (A4 at 72 DPI)
            val pageWidth = 595
            val pageHeight = 842

            // Measure WebView content
            webView.measure(
                android.view.View.MeasureSpec.makeMeasureSpec(pageWidth, android.view.View.MeasureSpec.EXACTLY),
                android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
            )

            val contentHeight = webView.measuredHeight
            val totalPages = (contentHeight / pageHeight) + 1

            onProgress?.invoke(0.7f)

            for (pageNum in 0 until totalPages) {
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create()
                val page = pdfDocument.startPage(pageInfo)

                val canvas = page.canvas
                canvas.translate(0f, -(pageNum * pageHeight).toFloat())

                webView.draw(canvas)

                pdfDocument.finishPage(page)
                onProgress?.invoke(0.7f + (0.2f * (pageNum + 1) / totalPages))
            }

            // Write to file
            outputFile.outputStream().use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            onProgress?.invoke(1.0f)
            Log.d(TAG, "PDF generated successfully: ${outputFile.absolutePath}, size: ${outputFile.length()}")
            callback(true, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating PDF document", e)
            callback(false, e.message)
        }
    }

    actual suspend fun sharePdf(result: PdfExportResult) = withContext(Dispatchers.Main) {
        val ctx = getContext() ?: run {
            Log.e(TAG, "Context not available for sharing PDF")
            return@withContext
        }

        try {
            val file = File(result.filePath)
            if (!file.exists()) {
                Log.e(TAG, "File not found: ${result.filePath}")
                return@withContext
            }

            Log.d(TAG, "Sharing PDF: ${file.absolutePath}, size: ${file.length()}")

            val uri: Uri = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                } else {
                    Uri.fromFile(file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "FileProvider error, trying alternative", e)
                // Try with .provider suffix as fallback
                try {
                    FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
                } catch (e2: Exception) {
                    Log.e(TAG, "Both FileProvider attempts failed", e2)
                    Uri.fromFile(file)
                }
            }

            Log.d(TAG, "URI created: $uri")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            ctx.startActivity(chooserIntent)
            Log.d(TAG, "Share intent started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing PDF", e)
        }
    }

    actual suspend fun openPdf(result: PdfExportResult) = withContext(Dispatchers.Main) {
        val ctx = getContext() ?: run {
            Log.e(TAG, "Context not available for opening PDF")
            return@withContext
        }

        try {
            val file = File(result.filePath)
            if (!file.exists()) {
                Log.e(TAG, "File not found: ${result.filePath}")
                return@withContext
            }

            Log.d(TAG, "Opening PDF: ${file.absolutePath}, size: ${file.length()}")

            val uri: Uri = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
                } else {
                    Uri.fromFile(file)
                }
            } catch (e: Exception) {
                Log.e(TAG, "FileProvider error, trying alternative", e)
                // Try with .provider suffix as fallback
                try {
                    FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
                } catch (e2: Exception) {
                    Log.e(TAG, "Both FileProvider attempts failed", e2)
                    Uri.fromFile(file)
                }
            }

            Log.d(TAG, "URI created: $uri")

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Check if there's an app to handle PDF
            if (openIntent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(openIntent)
                Log.d(TAG, "Open intent started successfully")
            } else {
                Log.e(TAG, "No app available to open PDF")
                // Try with a chooser instead
                val chooserIntent = Intent.createChooser(openIntent, "Open PDF with").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PDF", e)
        }
    }

    /**
     * Get the PDF export directory.
     * Uses app's external files directory for better compatibility with FileProvider.
     */
    private fun getPdfDirectory(): File {
        val ctx = getContext()
        return if (ctx != null) {
            // Use app's external files directory - doesn't require WRITE_EXTERNAL_STORAGE permission
            // and works well with FileProvider
            val appFilesDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (appFilesDir != null) {
                File(appFilesDir, "exportedPdf").also {
                    if (!it.exists()) it.mkdirs()
                }
            } else {
                // Fallback to cache directory
                File(ctx.cacheDir, "exportedPdf").also {
                    if (!it.exists()) it.mkdirs()
                }
            }
        } else {
            // Fallback to legacy external storage
            val externalStorage = Environment.getExternalStorageDirectory()
            File(externalStorage, PDF_DIRECTORY)
        }
    }

    companion object {
        /**
         * Check if storage permission is granted.
         */
        fun hasStoragePermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }
    }
}
