@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.indusjs.pdfreport.generator

import com.indusjs.pdfreport.model.PdfExportResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation of PdfGenerator.
 * Uses WKWebView to render HTML and UIPrintPageRenderer to generate PDF.
 */
actual class PdfGenerator {

    actual suspend fun generatePdf(
        html: String,
        fileName: String,
        onProgress: ((Float) -> Unit)?
    ): PdfExportResult = withContext(Dispatchers.Main) {
        try {
            onProgress?.invoke(0.1f)

            // Get Documents directory
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String ?: return@withContext PdfExportResult.error("Could not access Documents directory")

            val pdfDirPath = "$documentsPath/IndusJSFleet"
            val fileManager = NSFileManager.defaultManager

            // Create directory if needed
            if (!fileManager.fileExistsAtPath(pdfDirPath)) {
                fileManager.createDirectoryAtPath(pdfDirPath, true, null, null)
            }

            val filePath = "$pdfDirPath/$fileName"
            onProgress?.invoke(0.2f)

            // Generate PDF using HTML rendering
            val result = generatePdfFromHtml(html, filePath, onProgress)

            if (result) {
                val fileUrl = NSURL.fileURLWithPath(filePath)
                val attributes = fileManager.attributesOfItemAtPath(filePath, null)
                val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L

                PdfExportResult.success(
                    fileName = fileName,
                    filePath = filePath,
                    fileSize = fileSize
                )
            } else {
                PdfExportResult.error("Failed to generate PDF")
            }
        } catch (e: Exception) {
            PdfExportResult.error(e.message ?: "Failed to generate PDF")
        }
    }

    private suspend fun generatePdfFromHtml(
        html: String,
        filePath: String,
        onProgress: ((Float) -> Unit)?
    ): Boolean = suspendCoroutine { continuation ->
        val printRenderer = UIPrintPageRenderer()

        // Create a UIMarkupTextPrintFormatter from HTML
        val formatter = UIMarkupTextPrintFormatter(markupText = html)
        printRenderer.addPrintFormatter(formatter, startingAtPageAtIndex = 0)

        // A4 page size in points (72 points per inch)
        val pageWidth = 595.0
        val pageHeight = 842.0
        val margin = 36.0 // 0.5 inch margins

        val printableRect = CGRectMake(margin, margin, pageWidth - (margin * 2), pageHeight - (margin * 2))
        val paperRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)

        printRenderer.setValue(NSValue.valueWithCGRect(paperRect), forKey = "paperRect")
        printRenderer.setValue(NSValue.valueWithCGRect(printableRect), forKey = "printableRect")

        onProgress?.invoke(0.5f)

        // Create PDF data
        val pdfData = NSMutableData()
        UIGraphicsBeginPDFContextToData(pdfData, paperRect, null)

        val numberOfPages = printRenderer.numberOfPages
        for (pageIndex in 0 until numberOfPages.toInt()) {
            UIGraphicsBeginPDFPage()
            printRenderer.drawPageAtIndex(pageIndex.toLong(), inRect = paperRect)
            onProgress?.invoke(0.5f + (0.4f * (pageIndex + 1) / numberOfPages.toFloat()))
        }

        UIGraphicsEndPDFContext()

        onProgress?.invoke(0.95f)

        // Write to file
        val success = pdfData.writeToFile(filePath, atomically = true)

        onProgress?.invoke(1.0f)
        continuation.resume(success)
    }

    actual suspend fun sharePdf(result: PdfExportResult) = withContext(Dispatchers.Main) {
        try {
            val fileUrl = NSURL.fileURLWithPath(result.filePath)

            // Get the root view controller
            val keyWindow = UIApplication.sharedApplication.keyWindow
            val rootViewController = keyWindow?.rootViewController ?: return@withContext

            val activityController = UIActivityViewController(
                activityItems = listOf(fileUrl),
                applicationActivities = null
            )

            rootViewController.presentViewController(activityController, animated = true, completion = null)
        } catch (e: Exception) {
            println("Error sharing PDF: ${e.message}")
        }
    }

    actual suspend fun openPdf(result: PdfExportResult) = withContext(Dispatchers.Main) {
        try {
            val fileUrl = NSURL.fileURLWithPath(result.filePath)

            // Get the root view controller
            val keyWindow = UIApplication.sharedApplication.keyWindow
            val rootViewController = keyWindow?.rootViewController ?: return@withContext

            val documentController = UIDocumentInteractionController.interactionControllerWithURL(fileUrl)
            documentController.presentPreviewAnimated(true)
        } catch (e: Exception) {
            // Fallback to activity controller
            sharePdf(result)
        }
    }
}
