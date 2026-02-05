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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.indusjs.fleet.presentation.customers.detail.CustomerPaymentsPdfData
import java.io.File
import java.io.FileOutputStream

private const val TAG = "CustomerPaymentsPdf"

/**
 * Android implementation of CustomerPaymentsPdfExportHandler.
 */
@Composable
actual fun CustomerPaymentsPdfExportHandler(
    pdfData: CustomerPaymentsPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<PaymentsPdfResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingPdfData by remember { mutableStateOf<CustomerPaymentsPdfData?>(null) }

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
            Log.d(TAG, "Starting Customer Payments PDF export")

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
                    Text("Payments Report has been saved.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exportResult?.filePath ?: "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

private data class PaymentsPdfResult(
    val file: File,
    val filePath: String
)

private fun performExport(
    pdfData: CustomerPaymentsPdfData,
    onSuccess: (PaymentsPdfResult) -> Unit,
    onError: (String) -> Unit
) {
    Handler(Looper.getMainLooper()).post {
        try {
            val fileName = "Customer_Payments_${pdfData.generatedDate.replace("-", "")}_${System.currentTimeMillis()}.pdf"
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

private fun generateNativePdf(pdfData: CustomerPaymentsPdfData, fileName: String): PaymentsPdfResult? {
    val pageWidth = 595
    val pageHeight = 842

    val pdfDocument = PdfDocument()
    val itemsPerPage = 15
    val totalPages = ((pdfData.payments.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)

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
            canvas.drawText("Customer Payments Report", 40f, yPos, titlePaint)
            yPos += 25f

            textPaint.textSize = 11f
            canvas.drawText(pdfData.customerName, 40f, yPos, textPaint)
            yPos += 15f
            textPaint.textSize = 10f
            canvas.drawText("Contact: ${pdfData.customerContact}", 40f, yPos, textPaint)
            yPos += 25f

            // Summary
            val summaryBgPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#e8f5e9")
                style = Paint.Style.FILL
            }
            canvas.drawRect(40f, yPos, pageWidth - 40f, yPos + 50f, summaryBgPaint)

            val summaryPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#137333")
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("Total Received: ${pdfData.totalReceived}", 50f, yPos + 25, summaryPaint)
            canvas.drawText("${pdfData.paymentCount} Payments", 50f, yPos + 42, textPaint)

            yPos += 65
        }

        // Table header
        val colWidths = floatArrayOf(40f, 60f, 100f, 70f, 70f, 70f, 100f)
        val headers = listOf("#", "Trip", "Date", "Amount", "Mode", "Type", "Receipt")

        canvas.drawRect(40f, yPos - 5, pageWidth - 40f, yPos + 20f, headerBgPaint)

        var xPos = 45f
        headers.forEachIndexed { index, header ->
            canvas.drawText(header, xPos, yPos + 12, headerPaint)
            xPos += colWidths[index]
        }
        yPos += 25f

        val startIndex = (pageNum - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, pdfData.payments.size)

        val rowPaint = Paint().apply {
            color = android.graphics.Color.parseColor("#f9f9f9")
            style = Paint.Style.FILL
        }

        for (i in startIndex until endIndex) {
            val payment = pdfData.payments[i]

            if ((i - startIndex) % 2 == 1) {
                canvas.drawRect(40f, yPos - 12, pageWidth - 40f, yPos + 8f, rowPaint)
            }

            xPos = 45f
            val rowData = listOf(
                payment.id,
                payment.tripId.take(8),
                payment.date.take(16),
                payment.amount,
                payment.mode.take(10),
                payment.type.take(10),
                payment.receipt.take(15)
            )

            textPaint.textSize = 8f
            rowData.forEachIndexed { index, value ->
                val paint = if (index == 3) {
                    Paint(textPaint).apply { color = android.graphics.Color.parseColor("#1a73e8") }
                } else {
                    textPaint
                }
                canvas.drawText(value, xPos, yPos, paint)
                xPos += colWidths[index]
            }
            yPos += 20f
        }

        val footerPaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("Generated on ${pdfData.generatedDate} | IndusJS Fleet | Page $pageNum of $totalPages",
            40f, pageHeight - 30f, footerPaint)

        pdfDocument.finishPage(page)
    }

    val pdfDir = getPdfExportDirectory()
    val file = File(pdfDir, fileName)

    FileOutputStream(file).use { out ->
        pdfDocument.writeTo(out)
    }
    pdfDocument.close()

    return PaymentsPdfResult(file, file.absolutePath)
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
