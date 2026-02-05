package com.indusjs.fleet.core.pdf

import android.Manifest
import android.content.Intent
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.indusjs.fleet.presentation.customers.detail.CustomerTripsPdfData
import java.io.File
import java.io.FileOutputStream

private const val TAG = "CustomerTripsPdf"

/**
 * Android implementation of CustomerTripsPdfExportHandler.
 */
@Composable
actual fun CustomerTripsPdfExportHandler(
    pdfData: CustomerTripsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<CustomerTripsPdfResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingPdfData by remember { mutableStateOf<CustomerTripsPdfData?>(null) }

    // Permission launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPdfData?.let { data ->
                performExport(data,
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

    // For Android 11+
    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasStoragePermission(context)) {
            pendingPdfData?.let { data ->
                performExport(data,
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

    val currentDataId = pdfData?.generatedDate

    LaunchedEffect(currentDataId) {
        if (pdfData != null && !isExporting) {
            isExporting = true
            Log.d(TAG, "Starting Customer Trips PDF export")

            when {
                hasStoragePermission(context) -> {
                    performExport(pdfData,
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
                    pendingPdfData = pdfData
                    showPermissionDialog = true
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    pendingPdfData = pdfData
                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                else -> {
                    performExport(pdfData,
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

    // Loading dialog
    if (isExporting) {
        Dialog(onDismissRequest = {}) {
            Card {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Generating PDF...")
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
            },
            title = { Text("Storage Permission") },
            text = { Text("Storage access is needed to save the PDF file.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        manageStorageLauncher.launch(intent)
                    }
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    isExporting = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Result dialog
    if (showResultDialog && exportResult != null) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                exportResult = null
                onExportComplete()
            },
            title = { Text("PDF Exported") },
            text = {
                Column {
                    Text("Customer Trips Report has been saved.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exportResult?.filePath ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Share button
                    TextButton(onClick = {
                        exportResult?.let { result ->
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    result.file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                            } catch (e: Exception) {
                                Log.e(TAG, "Error sharing PDF", e)
                            }
                        }
                    }) {
                        Text("Share")
                    }
                    // Open button
                    TextButton(onClick = {
                        exportResult?.let { result ->
                            try {
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    result.file
                                )
                                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(viewIntent)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error opening PDF", e)
                                // Try with chooser if direct open fails
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        result.file
                                    )
                                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(Intent.createChooser(viewIntent, "Open PDF with"))
                                } catch (e2: Exception) {
                                    Log.e(TAG, "Error opening PDF with chooser", e2)
                                }
                            }
                        }
                        showResultDialog = false
                        exportResult = null
                        onExportComplete()
                    }) {
                        Text("Open")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    exportResult = null
                    onExportComplete()
                }) {
                    Text("Close")
                }
            }
        )
    }
}

private data class CustomerTripsPdfResult(
    val file: File,
    val filePath: String
)

private fun performExport(
    pdfData: CustomerTripsPdfData,
    onSuccess: (CustomerTripsPdfResult) -> Unit,
    onError: (String) -> Unit
) {
    Handler(Looper.getMainLooper()).post {
        try {
            val fileName = "Customer_Trips_${pdfData.generatedDate.replace("-", "")}_${System.currentTimeMillis()}.pdf"
            val result = generateNativePdf(pdfData, fileName)

            if (result != null && result.file.exists()) {
                Log.d(TAG, "PDF generated: ${result.filePath}")
                onSuccess(result)
            } else {
                onError("Failed to create PDF file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "PDF export error", e)
            onError(e.message ?: "Failed to export PDF")
        }
    }
}

private fun getPdfExportDirectory(): File {
    val sdcard = Environment.getExternalStorageDirectory()
    val pdfDir = File(sdcard, "IndusJSFleet/exportedPdf/customers")
    if (!pdfDir.exists()) {
        pdfDir.mkdirs()
    }
    return pdfDir
}

private fun generateNativePdf(pdfData: CustomerTripsPdfData, fileName: String): CustomerTripsPdfResult? {
    val pageWidth = 595
    val pageHeight = 842

    val pdfDocument = PdfDocument()
    val itemsPerPage = 12
    val totalPages = ((pdfData.trips.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)

    for (pageNum in 1..totalPages) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1a73e8")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#1a73e8")
            style = Paint.Style.FILL
        }

        var yPos = 40f

        if (pageNum == 1) {
            // Title
            canvas.drawText("Customer Trips Report", 40f, yPos, titlePaint)
            yPos += 25f

            // Customer info
            textPaint.textSize = 11f
            canvas.drawText(pdfData.customerName, 40f, yPos, textPaint)
            yPos += 15f
            textPaint.textSize = 10f
            canvas.drawText("Contact: ${pdfData.customerContact}", 40f, yPos, textPaint)
            yPos += 25f

            // Summary boxes
            val boxWidth = 120f
            val boxHeight = 45f
            val startX = 40f
            val boxPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#f5f5f5")
                style = Paint.Style.FILL
            }

            val summaryData = listOf(
                "Total Trips" to pdfData.totalTrips.toString(),
                "Completed" to pdfData.completedTrips.toString(),
                "Revenue" to pdfData.totalRevenue,
                "Due" to pdfData.totalPending
            )

            summaryData.forEachIndexed { index, (label, value) ->
                val x = startX + (index * (boxWidth + 10))
                canvas.drawRect(x, yPos, x + boxWidth, yPos + boxHeight, boxPaint)

                val valuePaint = Paint().apply {
                    color = if (index == 3) android.graphics.Color.parseColor("#d93025")
                           else if (index == 2) android.graphics.Color.parseColor("#137333")
                           else android.graphics.Color.parseColor("#1a73e8")
                    textSize = 14f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText(value, x + 10, yPos + 25, valuePaint)

                val labelPaint = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 9f
                    isAntiAlias = true
                }
                canvas.drawText(label, x + 10, yPos + 40, labelPaint)
            }

            yPos += boxHeight + 25
        }

        // Table header
        val colWidths = floatArrayOf(30f, 70f, 120f, 65f, 65f, 55f, 50f, 50f, 50f)
        val headers = listOf("#", "Vehicle", "Route", "Start", "End", "Status", "Price", "Paid", "Due")

        canvas.drawRect(40f, yPos - 5, pageWidth - 40f, yPos + 20f, headerBgPaint)

        var xPos = 45f
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, xPos, yPos + 12, headerPaint)
            xPos += colWidths[index]
        }
        yPos += 25f

        // Table rows
        val startIndex = (pageNum - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, pdfData.trips.size)

        val rowPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#f9f9f9")
            style = Paint.Style.FILL
        }

        for (i in startIndex until endIndex) {
            val trip = pdfData.trips[i]

            if ((i - startIndex) % 2 == 1) {
                canvas.drawRect(40f, yPos - 12, pageWidth - 40f, yPos + 8f, rowPaint)
            }

            xPos = 45f
            val rowData = listOf(
                trip.id,
                trip.vehicleRegistration.take(10),
                trip.route.take(20),
                trip.startDate.take(11),
                trip.endDate.take(11),
                trip.state.take(10),
                trip.price,
                trip.paid,
                trip.pending
            )

            textPaint.textSize = 8f
            rowData.forEachIndexed { index, value ->
                val paint = if (index == 8 && value != "₹0") {
                    Paint(textPaint).apply { color = android.graphics.Color.parseColor("#d93025") }
                } else if (index == 7) {
                    Paint(textPaint).apply { color = android.graphics.Color.parseColor("#1a73e8") }
                } else {
                    textPaint
                }
                canvas.drawText(value, xPos, yPos, paint)
                xPos += colWidths[index]
            }
            yPos += 20f
        }

        // Footer
        val footerPaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("Generated on ${pdfData.generatedDate} | IndusJS Fleet | Page $pageNum of $totalPages",
            40f, pageHeight - 30f, footerPaint)

        pdfDocument.finishPage(page)
    }

    // Save file
    val pdfDir = getPdfExportDirectory()
    val file = File(pdfDir, fileName)

    FileOutputStream(file).use { out ->
        pdfDocument.writeTo(out)
    }
    pdfDocument.close()

    return CustomerTripsPdfResult(file, file.absolutePath)
}

private fun hasStoragePermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
