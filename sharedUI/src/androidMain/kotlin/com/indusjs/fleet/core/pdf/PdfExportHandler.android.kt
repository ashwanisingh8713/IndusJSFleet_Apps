package com.indusjs.fleet.core.pdf

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract
import java.io.File
import java.io.FileOutputStream

private const val TAG = "PdfExportHandler"

/**
 * Result of PDF generation containing file path and metadata.
 */
data class PdfExportResult(
    val file: File,
    val fileName: String,
    val filePath: String,
    val vehicleNumber: String,
    val tripNumber: String,
    val exportDateTime: String
)

/**
 * Android implementation of PdfExportHandler.
 * Uses native Android PDF APIs to generate PDF.
 * Saves PDF to: /sdcard/IndusJSFleet/exportedPdf/
 * Shows dialog with "Done" and "Share" options after export.
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
    var exportResult by remember { mutableStateOf<PdfExportResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingPdfData by remember { mutableStateOf<TripDetailContract.TripCostsPdfData?>(null) }

    // Permission launcher for legacy storage permission (Android 9 and below)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with export
            pendingPdfData?.let { data ->
                performExport(data, context,
                    onSuccess = { result ->
                        exportResult = result
                        showResultDialog = true
                        isExporting = false
                    },
                    onError = { error ->
                        isExporting = false
                        onExportError(error)
                    }
                )
            }
        } else {
            isExporting = false
            onExportError("Storage permission is required to save PDF")
        }
        pendingPdfData = null
    }

    // For Android 11+ (API 30+), we need MANAGE_EXTERNAL_STORAGE
    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasStoragePermission(context)) {
            pendingPdfData?.let { data ->
                performExport(data, context,
                    onSuccess = { result ->
                        exportResult = result
                        showResultDialog = true
                        isExporting = false
                    },
                    onError = { error ->
                        isExporting = false
                        onExportError(error)
                    }
                )
            }
        } else {
            isExporting = false
            onExportError("Storage permission is required to save PDF")
        }
        pendingPdfData = null
    }

    val currentDataId = pdfData?.tripId

    LaunchedEffect(currentDataId) {
        if (pdfData != null && !isExporting) {
            isExporting = true
            Log.d(TAG, "Starting PDF export for trip: ${pdfData.tripId}")

            // Check permissions first
            when {
                hasStoragePermission(context) -> {
                    // Permission already granted, proceed
                    performExport(pdfData, context,
                        onSuccess = { result ->
                            exportResult = result
                            showResultDialog = true
                            isExporting = false
                        },
                        onError = { error ->
                            isExporting = false
                            onExportError(error)
                        }
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    // Android 11+, need MANAGE_EXTERNAL_STORAGE
                    pendingPdfData = pdfData
                    showPermissionDialog = true
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    // Android 6-10, request WRITE_EXTERNAL_STORAGE
                    pendingPdfData = pdfData
                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                else -> {
                    // Below Android 6, permission is granted at install time
                    performExport(pdfData, context,
                        onSuccess = { result ->
                            exportResult = result
                            showResultDialog = true
                            isExporting = false
                        },
                        onError = { error ->
                            isExporting = false
                            onExportError(error)
                        }
                    )
                }
            }
        }
    }

    // Permission dialog for Android 11+
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                isExporting = false
                pendingPdfData = null
            },
            title = { Text("Storage Permission Required") },
            text = {
                Text("To save PDFs to your device, please grant 'All files access' permission in the next screen.\n\nGo to Settings > Allow access to manage all files")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            manageStorageLauncher.launch(intent)
                        }
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPermissionDialog = false
                        isExporting = false
                        pendingPdfData = null
                        onExportError("Storage permission is required to save PDF")
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show result dialog after successful export
    if (showResultDialog && exportResult != null) {
        PdfExportResultDialog(
            result = exportResult!!,
            onDone = {
                showResultDialog = false
                exportResult = null
                onExportComplete()
            },
            onShare = {
                sharePdfFile(context, exportResult!!.file, exportResult!!.tripNumber, exportResult!!.exportDateTime)
                showResultDialog = false
                exportResult = null
                onExportComplete()
            }
        )
    }
}

/**
 * Check if storage permission is granted.
 */
private fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Below Android 6, permission is granted at install time
    }
}

/**
 * Perform the actual PDF export.
 */
private fun performExport(
    pdfData: TripDetailContract.TripCostsPdfData,
    context: Context,
    onSuccess: (PdfExportResult) -> Unit,
    onError: (String) -> Unit
) {
    Handler(Looper.getMainLooper()).post {
        try {
            val result = generateNativePdf(pdfData)
            if (result != null && result.file.exists()) {
                Log.d(TAG, "PDF generated successfully: ${result.filePath}")
                onSuccess(result)
            } else {
                Log.e(TAG, "PDF file was not created")
                onError("Failed to create PDF file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "PDF export exception", e)
            onError(e.message ?: "Failed to export PDF")
        }
    }
}

/**
 * Dialog shown after PDF export is complete.
 * Shows saved location and provides "Done" and "Share" options.
 */
@Composable
private fun PdfExportResultDialog(
    result: PdfExportResult,
    onDone: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDone) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title with 8dp padding
                Text(
                    text = "${result.vehicleNumber} Current Trip Costs exported successfully",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // File info card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "📄", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "File Name",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = result.fileName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "📁", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Saved Location",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = result.filePath,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDone,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Done", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(text = "📤", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Share", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Generate PDF file name based on: VehicleNumber_TripId_DepartureDate_to_ArrivalDate
 * Example: UP64AB1234_Trip1_10-Feb-2026_to_12-Mar-2026.pdf
 */
private fun generatePdfFileName(pdfData: TripDetailContract.TripCostsPdfData): String {
    val vehicleNumber = pdfData.vehicleNumber
        ?.replace(" ", "")
        ?.replace("/", "-")
        ?.replace("\\", "-")
        ?: "Vehicle"

    val tripId = pdfData.tripId

    // Format departure date as DD-MMM-YYYY (e.g., 10-Feb-2026)
    val startDate = formatDateForFileName(pdfData.departureDate) ?: "NA"

    // Format arrival date as DD-MMM-YYYY (e.g., 12-Mar-2026)
    val endDate = formatDateForFileName(pdfData.arrivalDate)
        ?: formatDateForFileName(pdfData.exportDate)
        ?: "NA"

    return "${vehicleNumber}_Trip${tripId}_${startDate}_to_${endDate}.pdf"
}

/**
 * Format date from DD-MM-YYYY to DD-MMM-YYYY for file naming.
 * Example: 10-02-2026 -> 10-Feb-2026
 */
private fun formatDateForFileName(dateString: String?): String? {
    if (dateString.isNullOrBlank()) return null

    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    return try {
        // Handle DD-MM-YYYY format
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val day = parts[0]
            val monthIndex = parts[1].toIntOrNull()?.minus(1) ?: return dateString.replace("-", "")
            val year = parts[2]

            if (monthIndex in 0..11) {
                "$day-${months[monthIndex]}-$year"
            } else {
                dateString.replace("-", "")
            }
        } else {
            dateString.replace("-", "")
        }
    } catch (e: Exception) {
        dateString.replace("-", "")
    }
}

/**
 * Get the PDF export directory: /sdcard/IndusJSFleet/exportedPdf/
 */
private fun getPdfExportDirectory(): File {
    val sdcard = Environment.getExternalStorageDirectory()
    val pdfDir = File(sdcard, "IndusJSFleet/exportedPdf")

    if (!pdfDir.exists()) {
        val created = pdfDir.mkdirs()
        Log.d(TAG, "Created PDF directory: ${pdfDir.absolutePath}, success: $created")
    }

    return pdfDir
}

/**
 * Generate PDF using native Android Canvas-based drawing.
 * Returns PdfExportResult with file path and metadata.
 */
private fun generateNativePdf(
    pdfData: TripDetailContract.TripCostsPdfData
): PdfExportResult? {
    Log.d(TAG, "Generating native PDF...")

    val fileName = generatePdfFileName(pdfData)
    Log.d(TAG, "PDF file name: $fileName")

    val pageWidth = 595
    val pageHeight = 842

    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    drawPdfContent(canvas, pdfData, pageWidth, pageHeight)

    pdfDocument.finishPage(page)

    val pdfDir = getPdfExportDirectory()
    val pdfFile = File(pdfDir, fileName)

    try {
        FileOutputStream(pdfFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        Log.d(TAG, "PDF saved: ${pdfFile.absolutePath}, size: ${pdfFile.length()} bytes")
    } finally {
        pdfDocument.close()
    }

    return PdfExportResult(
        file = pdfFile,
        fileName = fileName,
        filePath = "/sdcard/IndusJSFleet/exportedPdf/$fileName",
        vehicleNumber = pdfData.vehicleNumber ?: "Vehicle",
        tripNumber = pdfData.tripNumber ?: "Trip #${pdfData.tripId}",
        exportDateTime = "${pdfData.exportDate} at ${pdfData.exportTime}"
    )
}

private fun drawPdfContent(
    canvas: Canvas,
    pdfData: TripDetailContract.TripCostsPdfData,
    pageWidth: Int,
    pageHeight: Int
) {
    val margin = 40f
    var yPos = margin

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
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 100f, bgPaint)
    yPos += 15f

    // Vehicle
    canvas.drawText("VEHICLE REGISTRATION", margin + 10, yPos, labelPaint)
    canvas.drawText(pdfData.vehicleNumber ?: "N/A", margin + 10, yPos + 15f, valuePaint)

    // Trip Status
    canvas.drawText("TRIP STATUS", pageWidth / 2f, yPos, labelPaint)
    canvas.drawText(pdfData.tripStatusLabel ?: "N/A", pageWidth / 2f, yPos + 15f, valuePaint)

    yPos += 35f

    // Driver
    canvas.drawText("DRIVER", margin + 10, yPos, labelPaint)
    canvas.drawText(pdfData.driverName ?: "N/A", margin + 10, yPos + 15f, valuePaint)

    // Customer
    canvas.drawText("CUSTOMER", pageWidth / 2f, yPos, labelPaint)
    canvas.drawText(pdfData.customerName ?: "N/A", pageWidth / 2f, yPos + 15f, valuePaint)

    yPos += 55f

    // Route Section
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 60f, primaryBgPaint)
    yPos += 20f

    canvas.drawText("FROM: ${pdfData.startLocation ?: "N/A"}", margin + 10, yPos, normalPaint)
    yPos += 25f
    canvas.drawText("TO: ${pdfData.endLocation ?: "N/A"}", margin + 10, yPos, normalPaint)
    yPos += 30f

    // Schedule Section
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 50f, bgPaint)
    yPos += 15f

    // Departure
    canvas.drawText("DEPARTURE", margin + 10, yPos, labelPaint)
    val departureText = buildString {
        append(pdfData.departureDate ?: "N/A")
        pdfData.departureTime?.let { append(" at $it") }
    }
    canvas.drawText(departureText, margin + 10, yPos + 15f, valuePaint)

    // Arrival
    canvas.drawText("ARRIVAL", pageWidth / 2f, yPos, labelPaint)
    val arrivalText = buildString {
        append(pdfData.arrivalDate ?: "N/A")
        pdfData.arrivalTime?.let { append(" at $it") }
    }
    canvas.drawText(arrivalText, pageWidth / 2f, yPos + 15f, valuePaint)

    yPos += 45f

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
        if (yPos > pageHeight - 70) return@forEachIndexed // Prevent overflow

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
    yPos = pageHeight - 55f
    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
    yPos += 15f

    val footerPaint = Paint().apply {
        color = Color.parseColor("#999999")
        textSize = 10f
        isAntiAlias = true
    }

    val companyPaint = Paint().apply {
        color = Color.parseColor("#1976D2")
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val companyName = "IndusJS Fleet"
    val companyWidth = companyPaint.measureText(companyName)
    canvas.drawText(companyName, (pageWidth - companyWidth) / 2, yPos, companyPaint)
    yPos += 15f

    val exportInfo = "Exported on ${pdfData.exportDate} at ${pdfData.exportTime}"
    val exportWidth = footerPaint.measureText(exportInfo)
    canvas.drawText(exportInfo, (pageWidth - exportWidth) / 2, yPos, footerPaint)
    yPos += 12f

    val tripIdPaint = Paint().apply {
        color = Color.parseColor("#BBBBBB")
        textSize = 9f
        isAntiAlias = true
    }
    val tripIdInfo = "Trip ID: ${pdfData.tripId} | ${pdfData.vehicleNumber ?: "N/A"}"
    val tripIdWidth = tripIdPaint.measureText(tripIdInfo)
    canvas.drawText(tripIdInfo, (pageWidth - tripIdWidth) / 2, yPos, tripIdPaint)
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

/**
 * Share PDF file via Intent with subject containing date and time.
 */
private fun sharePdfFile(context: Context, pdfFile: File, tripNumber: String, exportDateTime: String) {
    Log.d(TAG, "Sharing PDF file: ${pdfFile.absolutePath}")

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        Log.d(TAG, "FileProvider URI: $uri")

        val subject = "Trip Costs Report - $tripNumber - $exportDateTime"

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, "Please find attached the Trip Costs Report.\n\nTrip: $tripNumber\nExported: $exportDateTime")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserTitle = "Share: $tripNumber ($exportDateTime)"
        val chooserIntent = Intent.createChooser(shareIntent, chooserTitle)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)

        Log.d(TAG, "Share intent started successfully")
    } catch (e: Exception) {
        Log.e(TAG, "Error sharing PDF", e)
        throw e
    }
}
