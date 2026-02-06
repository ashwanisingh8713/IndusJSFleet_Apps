package com.indusjs.pdfreport.generator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import com.indusjs.pdfreport.PdfContextInitializer
import com.indusjs.pdfreport.model.PdfExportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

private const val TAG = "PdfGenerator"
private const val PDF_DIRECTORY = "IndusJSFleet/exportedPdf"
private const val PDF_GENERATION_TIMEOUT_MS = 60000L // 60 seconds timeout

/**
 * Android implementation of PdfGenerator.
 * Uses WebView attached to window to render HTML and PdfDocument to generate PDF.
 */
actual class PdfGenerator {

    private var webView: WebView? = null
    private var containerView: FrameLayout? = null

    private fun getContext(): Context? = PdfContextInitializer.getContext()

    actual suspend fun generatePdf(
        html: String,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ): PdfExportResult = withContext(Dispatchers.Main) {
        val ctx = getContext()
        Log.d(TAG, "=== PDF GENERATION START ===")
        Log.d(TAG, "generatePdf called - fileName: $fileName")
        Log.d(TAG, "Context type: ${ctx?.javaClass?.simpleName}, isActivity: ${ctx is Activity}")

        if (ctx == null) {
            Log.e(TAG, "ERROR: Context is null!")
            return@withContext PdfExportResult.error("Context not set.")
        }

        try {
            onProgress?.invoke(0.1f)
            Log.d(TAG, "Step 1: Starting PDF generation")

            val pdfDir = getPdfDirectory()
            Log.d(TAG, "Step 2: PDF directory: ${pdfDir.absolutePath}, exists: ${pdfDir.exists()}")
            if (!pdfDir.exists()) {
                val created = pdfDir.mkdirs()
                Log.d(TAG, "Step 2b: Created directory: $created")
            }

            val pdfFile = File(pdfDir, fileName)
            Log.d(TAG, "Step 3: PDF file path: ${pdfFile.absolutePath}")
            onProgress?.invoke(0.2f)

            Log.d(TAG, "Step 4: Starting generatePdfInternal with timeout ${PDF_GENERATION_TIMEOUT_MS}ms")
            val result = withTimeoutOrNull(PDF_GENERATION_TIMEOUT_MS) {
                generatePdfInternal(ctx, html, pdfFile, fileName, onProgress)
            }

            if (result == null) {
                Log.e(TAG, "ERROR: PDF generation timed out after ${PDF_GENERATION_TIMEOUT_MS}ms")
            } else {
                Log.d(TAG, "Result: success=${result.success}, error=${result.errorMessage}")
            }

            Log.d(TAG, "Step 5: Cleanup")
            cleanup(ctx)

            Log.d(TAG, "=== PDF GENERATION END ===")
            result ?: PdfExportResult.error("PDF generation timed out.")
        } catch (e: Exception) {
            Log.e(TAG, "ERROR: PDF generation exception", e)
            cleanup(ctx)
            PdfExportResult.error(e.message ?: "Failed to generate PDF")
        }
    }

    private fun cleanup(ctx: Context) {
        Log.d(TAG, "Cleanup: Starting")
        try {
            val activity = ctx as? Activity
            containerView?.let { container ->
                webView?.let { wv ->
                    Log.d(TAG, "Cleanup: Removing WebView from container")
                    container.removeView(wv)
                    wv.destroy()
                }
                activity?.window?.decorView?.let { decorView ->
                    Log.d(TAG, "Cleanup: Removing container from decorView")
                    (decorView as? ViewGroup)?.removeView(container)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cleanup: Error", e)
        }
        webView = null
        containerView = null
        Log.d(TAG, "Cleanup: Completed")
    }

    private suspend fun generatePdfInternal(
        ctx: Context,
        html: String,
        pdfFile: File,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ): PdfExportResult = suspendCancellableCoroutine { continuation ->
        var isResumed = false
        Log.d(TAG, "Internal: Started, HTML length: ${html.length} chars")

        // A4 dimensions in points (72 DPI)
        val pageWidth = 595
        val pageHeight = 842
        val scale = 2.0f
        val scaledWidth = (pageWidth * scale).toInt()
        Log.d(TAG, "Internal: Page dimensions ${pageWidth}x${pageHeight}, scaledWidth: $scaledWidth")

        // Create off-screen container
        val container = FrameLayout(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(scaledWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            x = -10000f  // Position off-screen
            y = -10000f
        }
        containerView = container
        Log.d(TAG, "Internal: Container created, positioned off-screen")

        val webView = WebView(ctx).apply {
            setBackgroundColor(Color.WHITE)
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.allowFileAccess = true
            settings.domStorageEnabled = true
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            layoutParams = FrameLayout.LayoutParams(scaledWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        this@PdfGenerator.webView = webView
        container.addView(webView)
        Log.d(TAG, "Internal: WebView created and added to container")

        // CRITICAL: Add container to window for WebView to render
        val activity = ctx as? Activity
        if (activity != null) {
            try {
                val decorView = activity.window.decorView as ViewGroup
                decorView.addView(container)
                Log.d(TAG, "Internal: ✅ Container added to Activity's decorView - WebView is in window hierarchy!")
            } catch (e: Exception) {
                Log.e(TAG, "Internal: ❌ Failed to add container to decorView", e)
            }
        } else {
            Log.w(TAG, "Internal: ⚠️ Context is NOT an Activity! WebView may not render properly!")
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d(TAG, "WebViewClient: onPageStarted")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d(TAG, "WebViewClient: onPageFinished")
                Log.d(TAG, "WebViewClient: contentHeight=${webView.contentHeight}")

                if (isResumed) {
                    Log.d(TAG, "WebViewClient: Already resumed, skipping")
                    return
                }

                onProgress?.invoke(0.4f)

                // Wait for content to fully render
                Log.d(TAG, "WebViewClient: Scheduling PDF creation in 3 seconds...")
                webView.postDelayed({
                    if (isResumed) {
                        Log.d(TAG, "PostDelayed: Already resumed, skipping")
                        return@postDelayed
                    }

                    Log.d(TAG, "PostDelayed: Starting PDF creation")
                    Log.d(TAG, "PostDelayed: contentHeight after delay = ${webView.contentHeight}")
                    onProgress?.invoke(0.5f)

                    try {
                        // Measure the WebView content
                        Log.d(TAG, "PostDelayed: Measuring WebView...")
                        webView.measure(
                            View.MeasureSpec.makeMeasureSpec(scaledWidth, View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        )

                        val measuredHeight = webView.measuredHeight
                        val contentHeight = webView.contentHeight
                        // Add 10% extra padding to prevent bottom truncation
                        val extraPadding = (maxOf(measuredHeight, contentHeight) * 0.1f).toInt()
                        val finalHeight = maxOf(measuredHeight, contentHeight, 100) + extraPadding

                        Log.d(TAG, "PostDelayed: measuredHeight=$measuredHeight, contentHeight=$contentHeight, extraPadding=$extraPadding, finalHeight=$finalHeight")

                        if (finalHeight <= 0) {
                            Log.e(TAG, "PostDelayed: ERROR - Height is 0, content not rendered!")
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(PdfExportResult.error("Failed to render content - height is 0"))
                            }
                            return@postDelayed
                        }

                        webView.layout(0, 0, scaledWidth, finalHeight)
                        Log.d(TAG, "PostDelayed: Layout complete: ${scaledWidth}x${finalHeight}")
                        onProgress?.invoke(0.6f)

                        // Create bitmap and draw WebView content
                        Log.d(TAG, "PostDelayed: Creating bitmap ${scaledWidth}x${finalHeight}")
                        val bitmap = Bitmap.createBitmap(scaledWidth, finalHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)

                        Log.d(TAG, "PostDelayed: Drawing WebView to canvas...")
                        webView.draw(canvas)

                        // Check if bitmap has content (sample a pixel)
                        val samplePixel = bitmap.getPixel(scaledWidth / 2, minOf(50, finalHeight - 1))
                        Log.d(TAG, "PostDelayed: Sample pixel at center-top: 0x${Integer.toHexString(samplePixel)}")
                        Log.d(TAG, "PostDelayed: Bitmap created: ${bitmap.width}x${bitmap.height}")
                        onProgress?.invoke(0.7f)

                        // Create PDF from bitmap
                        Log.d(TAG, "PostDelayed: Creating PDF from bitmap...")
                        val success = createPdfFromBitmap(bitmap, pdfFile, pageWidth, pageHeight, scale, onProgress)
                        bitmap.recycle()
                        Log.d(TAG, "PostDelayed: PDF creation result: $success")

                        if (!isResumed) {
                            isResumed = true
                            if (success && pdfFile.exists() && pdfFile.length() > 0) {
                                Log.d(TAG, "PostDelayed: ✅ PDF SUCCESS - ${pdfFile.absolutePath}, size: ${pdfFile.length()} bytes")
                                continuation.resume(
                                    PdfExportResult.success(fileName, pdfFile.absolutePath, pdfFile.length())
                                )
                            } else {
                                Log.e(TAG, "PostDelayed: ❌ PDF FAILED - exists=${pdfFile.exists()}, size=${pdfFile.length()}")
                                continuation.resume(PdfExportResult.error("Failed to create PDF"))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "PostDelayed: ERROR", e)
                        if (!isResumed) {
                            isResumed = true
                            continuation.resume(PdfExportResult.error(e.message ?: "Failed"))
                        }
                    }
                }, 3000) // Wait 3 seconds for full render
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebViewClient: ERROR - code=$errorCode, desc=$description, url=$failingUrl")
            }
        }

        Log.d(TAG, "Internal: Loading HTML (${html.length} chars)")
        Log.d(TAG, "Internal: HTML preview: ${html.take(300)}...")
        onProgress?.invoke(0.3f)
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
        Log.d(TAG, "Internal: HTML load initiated")

        continuation.invokeOnCancellation {
            Log.d(TAG, "Internal: Continuation cancelled")
            cleanup(ctx)
        }
    }

    /**
     * Create PDF document from a bitmap, splitting into pages as needed.
     * Adds margins to prevent content truncation at page boundaries.
     */
    private fun createPdfFromBitmap(
        bitmap: Bitmap,
        outputFile: File,
        pageWidth: Int,
        pageHeight: Int,
        scale: Float,
        onProgress: ((Float) -> Unit)?
    ): Boolean {
        Log.d(TAG, "createPdfFromBitmap: input=${bitmap.width}x${bitmap.height}, output=${outputFile.absolutePath}")
        return try {
            val pdfDocument = PdfDocument()

            // Add margins to prevent content truncation
            val topMargin = 20  // 20 points top margin
            val bottomMargin = 40  // 40 points bottom margin
            val usablePageHeight = pageHeight - topMargin - bottomMargin
            val scaledUsablePageHeight = (usablePageHeight * scale).toInt()

            val totalPages = maxOf(1, (bitmap.height + scaledUsablePageHeight - 1) / scaledUsablePageHeight)

            Log.d(TAG, "createPdfFromBitmap: Creating $totalPages pages, usablePageHeight=$usablePageHeight, scaledUsablePageHeight=$scaledUsablePageHeight")

            for (pageNum in 0 until totalPages) {
                Log.d(TAG, "createPdfFromBitmap: Creating page ${pageNum + 1}/$totalPages")
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawColor(Color.WHITE)

                val srcTop = (pageNum * scaledUsablePageHeight).coerceAtMost(bitmap.height)
                val srcBottom = ((pageNum + 1) * scaledUsablePageHeight).coerceAtMost(bitmap.height)
                val srcHeight = srcBottom - srcTop

                Log.d(TAG, "createPdfFromBitmap: Page $pageNum - srcTop=$srcTop, srcBottom=$srcBottom, srcHeight=$srcHeight")

                if (srcHeight > 0) {
                    val pageBitmap = Bitmap.createBitmap(bitmap, 0, srcTop, bitmap.width, srcHeight)

                    // Preserve aspect ratio when scaling
                    val srcAspectRatio = pageBitmap.width.toFloat() / pageBitmap.height.toFloat()
                    val destWidth = pageWidth
                    val destHeight = (destWidth / srcAspectRatio).toInt().coerceAtMost(usablePageHeight)

                    val scaledBitmap = Bitmap.createScaledBitmap(pageBitmap, destWidth, destHeight, true)

                    Log.d(TAG, "createPdfFromBitmap: Drawing ${scaledBitmap.width}x${scaledBitmap.height} at y=$topMargin")

                    // Draw with top margin offset
                    canvas.drawBitmap(
                        scaledBitmap,
                        0f,
                        topMargin.toFloat(),
                        Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
                    )

                    scaledBitmap.recycle()
                    pageBitmap.recycle()
                }

                pdfDocument.finishPage(page)
                onProgress?.invoke(0.7f + (0.2f * (pageNum + 1) / totalPages))
            }

            Log.d(TAG, "createPdfFromBitmap: Writing PDF to file...")
            FileOutputStream(outputFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            Log.d(TAG, "createPdfFromBitmap: ✅ PDF saved: ${outputFile.absolutePath}, size: ${outputFile.length()} bytes")
            onProgress?.invoke(1.0f)
            true
        } catch (e: Exception) {
            Log.e(TAG, "createPdfFromBitmap: ERROR", e)
            false
        }
    }

    actual suspend fun sharePdf(result: PdfExportResult) = withContext(Dispatchers.Main) {
        val ctx = getContext() ?: return@withContext
        Log.d(TAG, "sharePdf: ${result.filePath}")

        try {
            val file = File(result.filePath)
            if (!file.exists()) {
                Log.e(TAG, "sharePdf: File not found")
                return@withContext
            }

            val uri = getFileUri(ctx, file) ?: return@withContext
            Log.d(TAG, "sharePdf: URI=$uri")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            grantUriPermissions(ctx, shareIntent, uri)

            val chooser = Intent.createChooser(shareIntent, "Share PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(chooser)
            Log.d(TAG, "sharePdf: Intent started")
        } catch (e: Exception) {
            Log.e(TAG, "sharePdf: ERROR", e)
        }
    }

    actual suspend fun openPdf(result: PdfExportResult) = withContext(Dispatchers.Main) {
        val ctx = getContext() ?: return@withContext
        Log.d(TAG, "openPdf: ${result.filePath}")

        try {
            val file = File(result.filePath)
            if (!file.exists()) {
                Log.e(TAG, "openPdf: File not found")
                return@withContext
            }

            val uri = getFileUri(ctx, file) ?: return@withContext
            Log.d(TAG, "openPdf: URI=$uri")

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            grantUriPermissions(ctx, openIntent, uri)

            try {
                ctx.startActivity(openIntent)
                Log.d(TAG, "openPdf: Intent started")
            } catch (e: android.content.ActivityNotFoundException) {
                Log.w(TAG, "openPdf: No PDF viewer, using chooser")
                val chooser = Intent.createChooser(openIntent, "Open PDF with").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(chooser)
            }
        } catch (e: Exception) {
            Log.e(TAG, "openPdf: ERROR", e)
        }
    }

    private fun grantUriPermissions(ctx: Context, intent: Intent, uri: Uri) {
        val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.packageManager.queryIntentActivities(
                intent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(
                    android.content.pm.PackageManager.MATCH_DEFAULT_ONLY.toLong()
                )
            )
        } else {
            @Suppress("DEPRECATION")
            ctx.packageManager.queryIntentActivities(
                intent,
                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
            )
        }

        for (info in resInfoList) {
            ctx.grantUriPermission(
                info.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun getFileUri(ctx: Context, file: File): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getFileUri: FileProvider error", e)
            try {
                FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
            } catch (e2: Exception) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Uri.fromFile(file) else null
            }
        }
    }

    private fun getPdfDirectory(): File {
        val ctx = getContext()
        return if (ctx != null) {
            val dir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (dir != null) {
                File(dir, "exportedPdf").also { if (!it.exists()) it.mkdirs() }
            } else {
                File(ctx.cacheDir, "exportedPdf").also { if (!it.exists()) it.mkdirs() }
            }
        } else {
            @Suppress("DEPRECATION")
            File(Environment.getExternalStorageDirectory(), PDF_DIRECTORY)
        }
    }
}
