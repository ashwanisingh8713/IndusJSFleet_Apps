package com.indusjs.fleet.core.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract
import java.io.File
import java.io.FileOutputStream

private const val TAG = "PdfExportHandler"

/**
 * Android implementation of PdfExportHandler.
 * Uses native Android PDF APIs to generate PDF and share via Intent.
 * Saves PDF to: internalStorage/IndusJSFleet/exportedPdf/
 * Naming: VehicleNumber_TripId_StartEndDate.pdf
 */
@Composable
actual fun PdfExportHandler(
    pdfData: TripDetailContract.TripCostsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    // Track if we should process this pdfData
    val currentDataId = pdfData?.tripId

    LaunchedEffect(currentDataId) {
        if (pdfData != null && !isExporting) {
            isExporting = true
            Log.d(TAG, "Starting PDF export for trip: ${pdfData.tripId}")

            // Run on main thread using Handler
            Handler(Looper.getMainLooper()).post {
                try {
                    val pdfFile = generateNativePdf(context, pdfData)
                    if (pdfFile != null && pdfFile.exists()) {
                        Log.d(TAG, "PDF generated successfully: ${pdfFile.absolutePath}")
                        sharePdfFile(context, pdfFile, pdfData.tripNumber ?: "Trip Costs")
                        isExporting = false
                        onExportComplete()
                    } else {
                        Log.e(TAG, "PDF file was not created")
                        isExporting = false
                        onExportError("Failed to create PDF file")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "PDF export exception", e)
                    isExporting = false
                    onExportError(e.message ?: "Failed to export PDF")
                }
            }
        }
    }
}

/**
 * Generate PDF file name based on: VehicleNumber_TripId_StartEndDate
 */
private fun generatePdfFileName(pdfData: TripDetailContract.TripCostsPdfData): String {
    val vehicleNumber = pdfData.vehicleNumber
        ?.replace(" ", "")
        ?.replace("/", "-")
        ?.replace("\\", "-")
        ?: "Vehicle"

    val tripId = pdfData.tripId

    // Format start date (from scheduledDate or extract from costs)
    val startDate = pdfData.scheduledDate
        ?.replace("-", "")
        ?.replace("/", "")
        ?.take(8)
        ?: "NA"

    // Get end date from the last cost entry or use export date
    val endDate = pdfData.exportDate
        .replace("-", "")
        .replace("/", "")
        .take(8)

    return "${vehicleNumber}_Trip${tripId}_${startDate}_${endDate}.pdf"
}

/**
 * Get the PDF export directory: sdcard/Documents/IndusJSFleet/exportedPdf/
 * Uses public Documents directory for easy access from file managers.
 */
private fun getPdfExportDirectory(context: Context): File {
    // Use public Documents directory on external storage (sdcard)
    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val pdfDir = File(documentsDir, "IndusJSFleet/exportedPdf")

    if (!pdfDir.exists()) {
        val created = pdfDir.mkdirs()
        Log.d(TAG, "Created PDF directory: ${pdfDir.absolutePath}, success: $created")
    }

    return pdfDir
}

/**
 * Generate PDF using native Android Canvas-based drawing.
 * This is more reliable than WebView-based approach.
 */
private fun generateNativePdf(
    context: Context,
    pdfData: TripDetailContract.TripCostsPdfData
): File? {
    Log.d(TAG, "Generating native PDF...")

    val fileName = generatePdfFileName(pdfData)
    Log.d(TAG, "PDF file name: $fileName")

    // A4 size in points (72 points per inch)
    val pageWidth = 595
    val pageHeight = 842

    val pdfDocument = PdfDocument()

    // Create page
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // Draw content
    drawPdfContent(canvas, pdfData, pageWidth, pageHeight)

    pdfDocument.finishPage(page)

    // Save to IndusJSFleet/exportedPdf directory
    val pdfDir = getPdfExportDirectory(context)
    val pdfFile = File(pdfDir, fileName)

    try {
        FileOutputStream(pdfFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        Log.d(TAG, "PDF saved: ${pdfFile.absolutePath}, size: ${pdfFile.length()} bytes")
    } finally {
        pdfDocument.close()
    }

    return pdfFile
}

private fun drawPdfContent(
    canvas: Canvas,
    pdfData: TripDetailContract.TripCostsPdfData,
    pageWidth: Int,
    pageHeight: Int
) {
    val margin = 40f
    var yPos = margin

    // Paints
    val titlePaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val subtitlePaint = Paint().apply {
        color = Color.parseColor("#666666")
        textSize = 14f
        isAntiAlias = true
    }

    val headerPaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val labelPaint = Paint().apply {
        color = Color.parseColor("#888888")
        textSize = 10f
        isAntiAlias = true
    }

    val valuePaint = Paint().apply {
        color = Color.parseColor("#333333")
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val normalPaint = Paint().apply {
        color = Color.parseColor("#333333")
        textSize = 11f
        isAntiAlias = true
    }

    val linePaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
    }

    val bgPaint = Paint().apply {
        color = Color.parseColor("#F5F5F5")
        style = Paint.Style.FILL
    }

    val primaryBgPaint = Paint().apply {
        color = Color.parseColor("#E3F2FD")
        style = Paint.Style.FILL
    }

    // Title
    val title = "Trip Costs Report"
    val titleWidth = titlePaint.measureText(title)
    canvas.drawText(title, (pageWidth - titleWidth) / 2, yPos + 24, titlePaint)
    yPos += 40f

    // Subtitle
    val subtitle = pdfData.tripNumber ?: "Trip Report"
    val subtitleWidth = subtitlePaint.measureText(subtitle)
    canvas.drawText(subtitle, (pageWidth - subtitleWidth) / 2, yPos, subtitlePaint)
    yPos += 30f

    // Divider line
    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint.apply { color = Color.parseColor("#1976D2"); strokeWidth = 2f })
    yPos += 20f

    // Trip Info Section
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 80f, bgPaint)
    yPos += 15f

    // Vehicle
    canvas.drawText("VEHICLE", margin + 10, yPos, labelPaint)
    canvas.drawText(pdfData.vehicleNumber ?: "N/A", margin + 10, yPos + 15f, valuePaint)

    // Driver
    canvas.drawText("DRIVER", pageWidth / 2f, yPos, labelPaint)
    canvas.drawText(pdfData.driverName ?: "N/A", pageWidth / 2f, yPos + 15f, valuePaint)

    yPos += 40f

    // Scheduled Date
    canvas.drawText("SCHEDULED DATE", margin + 10, yPos, labelPaint)
    canvas.drawText(pdfData.scheduledDate ?: "N/A", margin + 10, yPos + 15f, valuePaint)

    // Export Date
    canvas.drawText("EXPORT DATE", pageWidth / 2f, yPos, labelPaint)
    canvas.drawText(pdfData.exportDate, pageWidth / 2f, yPos + 15f, valuePaint)

    yPos += 45f

    // Route Section
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 60f, primaryBgPaint)
    yPos += 20f

    canvas.drawText("📍 FROM: ${pdfData.startLocation ?: "N/A"}", margin + 10, yPos, normalPaint)
    yPos += 25f
    canvas.drawText("📍 TO: ${pdfData.endLocation ?: "N/A"}", margin + 10, yPos, normalPaint)
    yPos += 30f

    // Total Cost Box
    yPos += 10f
    val totalBgPaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        style = Paint.Style.FILL
    }
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 60f, totalBgPaint)

    val whitePaint = Paint().apply {
        color = Color.WHITE
        textSize = 12f
        isAntiAlias = true
    }
    val totalAmountPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    canvas.drawText("TOTAL COST", margin + 15, yPos + 20f, whitePaint)
    canvas.drawText("₹${formatAmount(pdfData.totalCost)}", margin + 15, yPos + 48f, totalAmountPaint)

    val entriesText = "${pdfData.costs.size} ${if (pdfData.costs.size == 1) "entry" else "entries"}"
    canvas.drawText(entriesText, pageWidth - margin - 80, yPos + 35f, whitePaint)

    yPos += 80f

    // Cost Breakdown Section
    canvas.drawText("Cost Breakdown by Type", margin, yPos, headerPaint)
    yPos += 20f

    linePaint.color = Color.parseColor("#E0E0E0")
    linePaint.strokeWidth = 1f

    pdfData.costsByType.forEach { (type, amount) ->
        // Draw row background
        canvas.drawRect(margin, yPos - 5f, pageWidth - margin, yPos + 20f, bgPaint)

        // Type name
        canvas.drawText(formatCostType(type), margin + 10, yPos + 12f, normalPaint)

        // Amount (right-aligned)
        val amountText = "₹${formatAmount(amount)}"
        val amountWidth = valuePaint.measureText(amountText)
        canvas.drawText(amountText, pageWidth - margin - amountWidth - 10, yPos + 12f, valuePaint)

        yPos += 25f
    }

    yPos += 10f

    // Detailed Entries Section
    canvas.drawText("Detailed Cost Entries", margin, yPos, headerPaint)
    yPos += 20f

    // Table header
    val tableHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 20f, totalBgPaint)
    canvas.drawText("DATE", margin + 5, yPos + 14f, tableHeaderPaint)
    canvas.drawText("TYPE", margin + 100, yPos + 14f, tableHeaderPaint)
    canvas.drawText("NOTES", margin + 200, yPos + 14f, tableHeaderPaint)
    canvas.drawText("AMOUNT", pageWidth - margin - 60, yPos + 14f, tableHeaderPaint)
    yPos += 25f

    // Table rows
    pdfData.costs.forEachIndexed { index, cost ->
        if (yPos > pageHeight - 60) return@forEachIndexed // Prevent overflow

        if (index % 2 == 0) {
            canvas.drawRect(margin, yPos - 3f, pageWidth - margin, yPos + 17f, bgPaint)
        }

        canvas.drawText(formatDate(cost.date), margin + 5, yPos + 10f, normalPaint)
        canvas.drawText(formatCostType(cost.costType), margin + 100, yPos + 10f, normalPaint)

        val notes = (cost.notes ?: "-").take(25) + if ((cost.notes?.length ?: 0) > 25) "..." else ""
        canvas.drawText(notes, margin + 200, yPos + 10f, normalPaint)

        val costAmountText = "₹${formatAmount(cost.amount)}"
        val costAmountWidth = valuePaint.measureText(costAmountText)
        canvas.drawText(costAmountText, pageWidth - margin - costAmountWidth - 5, yPos + 10f, valuePaint)

        yPos += 20f
    }

    // Footer
    yPos = pageHeight - 40f
    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
    yPos += 15f

    val footerPaint = Paint().apply {
        color = Color.parseColor("#999999")
        textSize = 10f
        isAntiAlias = true
    }
    val footer = "IndusJS Fleet • Generated on ${pdfData.exportDate}"
    val footerWidth = footerPaint.measureText(footer)
    canvas.drawText(footer, (pageWidth - footerWidth) / 2, yPos, footerPaint)
}

private fun formatAmount(amount: Double): String {
    val intPart = amount.toLong()
    val decPart = ((amount - intPart) * 100).toInt()
    val decStr = if (decPart < 10) "0$decPart" else "$decPart"
    return if (intPart >= 1000) {
        val formatted = intPart.toString().reversed().chunked(3).joinToString(",").reversed()
        "$formatted.$decStr"
    } else {
        "$intPart.$decStr"
    }
}

private fun formatCostType(costType: String): String {
    return costType.replace("_", " ").split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercaseChar() }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        when {
            dateString.contains("T") -> {
                val datePart = dateString.substringBefore("T")
                val parts = datePart.split("-")
                if (parts.size == 3) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else dateString
            }
            else -> dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

private fun sharePdfFile(context: Context, pdfFile: File, title: String) {
    Log.d(TAG, "Sharing PDF file: ${pdfFile.absolutePath}")

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        Log.d(TAG, "FileProvider URI: $uri")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Trip Costs Report - $title")
            putExtra(Intent.EXTRA_TEXT, "Please find attached the Trip Costs Report for $title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share PDF via")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)

        Log.d(TAG, "Share intent started successfully")
    } catch (e: Exception) {
        Log.e(TAG, "Error sharing PDF", e)
        throw e
    }
}

