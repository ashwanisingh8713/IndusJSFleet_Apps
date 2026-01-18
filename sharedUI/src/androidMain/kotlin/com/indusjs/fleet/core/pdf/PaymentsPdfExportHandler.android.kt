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
import com.indusjs.fleet.presentation.payments.PaymentsListPdfData
import com.indusjs.fleet.presentation.payments.PaymentReceiptPdfData
import java.io.File
import java.io.FileOutputStream

private const val TAG = "PaymentsPdfExport"

/**
 * Result of PDF generation.
 */
data class PaymentPdfExportResult(
    val file: File,
    val fileName: String,
    val filePath: String,
    val title: String,
    val exportDateTime: String
)

/**
 * Android implementation of PaymentsListPdfExportHandler.
 */
@Composable
actual fun PaymentsListPdfExportHandler(
    pdfData: PaymentsListPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<PaymentPdfExportResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingPdfData by remember { mutableStateOf<PaymentsListPdfData?>(null) }

    // Permission launcher for legacy storage permission
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPdfData?.let { data ->
                performPaymentsListExport(data, context,
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
                performPaymentsListExport(data, context,
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
            Log.d(TAG, "Starting Payments List PDF export")

            when {
                hasStoragePermission(context) -> {
                    performPaymentsListExport(pdfData, context,
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
                    performPaymentsListExport(pdfData, context,
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
                Text("To save PDFs to your device, please grant 'All files access' permission.")
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
                        onExportError("Storage permission is required")
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Result dialog
    if (showResultDialog && exportResult != null) {
        PaymentPdfExportResultDialog(
            result = exportResult!!,
            onDone = {
                showResultDialog = false
                exportResult = null
                onExportComplete()
            },
            onShare = {
                sharePaymentPdfFile(context, exportResult!!.file, exportResult!!.title)
                showResultDialog = false
                exportResult = null
                onExportComplete()
            }
        )
    }
}

/**
 * Android implementation of PaymentReceiptPdfExportHandler.
 */
@Composable
actual fun PaymentReceiptPdfExportHandler(
    pdfData: PaymentReceiptPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<PaymentPdfExportResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingPdfData by remember { mutableStateOf<PaymentReceiptPdfData?>(null) }

    // Permission launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingPdfData?.let { data ->
                performPaymentReceiptExport(data, context,
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
                performPaymentReceiptExport(data, context,
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
            onExportError("Storage permission is required")
        }
        pendingPdfData = null
    }

    val currentDataId = pdfData?.paymentId

    LaunchedEffect(currentDataId) {
        if (pdfData != null && !isExporting) {
            isExporting = true
            Log.d(TAG, "Starting Payment Receipt PDF export: ${pdfData.paymentId}")

            when {
                hasStoragePermission(context) -> {
                    performPaymentReceiptExport(pdfData, context,
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
                    performPaymentReceiptExport(pdfData, context,
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

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                isExporting = false
                pendingPdfData = null
            },
            title = { Text("Storage Permission Required") },
            text = {
                Text("To save PDFs to your device, please grant 'All files access' permission.")
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
                        onExportError("Storage permission is required")
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Result dialog
    if (showResultDialog && exportResult != null) {
        PaymentPdfExportResultDialog(
            result = exportResult!!,
            onDone = {
                showResultDialog = false
                exportResult = null
                onExportComplete()
            },
            onShare = {
                sharePaymentPdfFile(context, exportResult!!.file, exportResult!!.title)
                showResultDialog = false
                exportResult = null
                onExportComplete()
            }
        )
    }
}

// ==================== Helper Functions ====================

private fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun performPaymentsListExport(
    pdfData: PaymentsListPdfData,
    @Suppress("UNUSED_PARAMETER") context: Context,
    onSuccess: (PaymentPdfExportResult) -> Unit,
    onError: (String) -> Unit
) {
    Handler(Looper.getMainLooper()).post {
        try {
            val fileName = generatePaymentsListFileName(pdfData)
            val result = generatePaymentsListNativePdf(pdfData, fileName)

            if (result != null && result.file.exists()) {
                Log.d(TAG, "Payments List PDF generated: ${result.filePath}")
                onSuccess(result)
            } else {
                onError("Failed to create PDF file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Payments List PDF export error", e)
            onError(e.message ?: "Failed to export PDF")
        }
    }
}

private fun performPaymentReceiptExport(
    pdfData: PaymentReceiptPdfData,
    @Suppress("UNUSED_PARAMETER") context: Context,
    onSuccess: (PaymentPdfExportResult) -> Unit,
    onError: (String) -> Unit
) {
    Handler(Looper.getMainLooper()).post {
        try {
            val fileName = generatePaymentReceiptFileName(pdfData)
            val result = generatePaymentReceiptNativePdf(pdfData, fileName)

            if (result != null && result.file.exists()) {
                Log.d(TAG, "Payment Receipt PDF generated: ${result.filePath}")
                onSuccess(result)
            } else {
                onError("Failed to create PDF file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Payment Receipt PDF export error", e)
            onError(e.message ?: "Failed to export PDF")
        }
    }
}

private fun generatePaymentsListFileName(pdfData: PaymentsListPdfData): String {
    val fromDate = pdfData.fromDate?.replace("-", "") ?: "All"
    val toDate = pdfData.toDate?.replace("-", "") ?: "Today"
    val timestamp = System.currentTimeMillis()
    return "Payments_Report_${fromDate}_to_${toDate}_$timestamp.pdf"
}

private fun generatePaymentReceiptFileName(pdfData: PaymentReceiptPdfData): String {
    val receiptNo = pdfData.receiptNumber ?: "PMT${pdfData.paymentId}"
    val date = pdfData.paymentDate.replace("-", "")
    return "Payment_Receipt_${receiptNo}_$date.pdf"
}

/**
 * Get PDF export directory.
 */
private fun getPdfExportDirectory(): File {
    val sdcard = Environment.getExternalStorageDirectory()
    val pdfDir = File(sdcard, "IndusJSFleet/exportedPdf/payments")
    if (!pdfDir.exists()) {
        pdfDir.mkdirs()
    }
    return pdfDir
}

/**
 * Generate native PDF for Payments List Report.
 */
private fun generatePaymentsListNativePdf(
    pdfData: PaymentsListPdfData,
    fileName: String
): PaymentPdfExportResult? {
    Log.d(TAG, "Generating Payments List native PDF...")

    val pageWidth = 595 // A4 width in points
    val pageHeight = 842 // A4 height in points

    val pdfDocument = PdfDocument()

    // Calculate how many pages we need
    val itemsPerPage = 15
    val totalPages = ((pdfData.payments.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)

    for (pageNum in 1..totalPages) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawPaymentsListPage(canvas, pdfData, pageWidth, pageHeight, pageNum, totalPages, itemsPerPage)

        pdfDocument.finishPage(page)
    }

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

    return PaymentPdfExportResult(
        file = pdfFile,
        fileName = fileName,
        filePath = "/sdcard/IndusJSFleet/exportedPdf/payments/$fileName",
        title = pdfData.reportTitle,
        exportDateTime = "${pdfData.generatedDate} ${pdfData.generatedTime}"
    )
}

/**
 * Draw a single page of the Payments List PDF.
 */
private fun drawPaymentsListPage(
    canvas: Canvas,
    pdfData: PaymentsListPdfData,
    pageWidth: Int,
    pageHeight: Int,
    pageNum: Int,
    totalPages: Int,
    itemsPerPage: Int
) {
    val margin = 40f
    var yPos = margin

    // Paints
    val titlePaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val subtitlePaint = Paint().apply {
        color = Color.parseColor("#666666")
        textSize = 11f
        isAntiAlias = true
    }

    val headerPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF")
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val labelPaint = Paint().apply {
        color = Color.parseColor("#888888")
        textSize = 9f
        isAntiAlias = true
    }

    val valuePaint = Paint().apply {
        color = Color.parseColor("#333333")
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val normalPaint = Paint().apply {
        color = Color.parseColor("#333333")
        textSize = 9f
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

    val headerBgPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    val receivedBgPaint = Paint().apply {
        color = Color.parseColor("#E8F5E9")
        style = Paint.Style.FILL
    }

    val pendingBgPaint = Paint().apply {
        color = Color.parseColor("#FFF3E0")
        style = Paint.Style.FILL
    }

    // Only draw header on first page
    if (pageNum == 1) {
        // Title
        val title = "💳 ${pdfData.reportTitle}"
        canvas.drawText(title, margin, yPos + 22, titlePaint)
        yPos += 30f

        // Subtitle with date range
        val subtitle = "Generated: ${pdfData.generatedDate} at ${pdfData.generatedTime}"
        canvas.drawText(subtitle, margin, yPos, subtitlePaint)
        yPos += 15f

        if (pdfData.fromDate != null || pdfData.toDate != null) {
            val dateRange = "Date Range: ${pdfData.fromDate ?: "All"} to ${pdfData.toDate ?: "Today"}"
            canvas.drawText(dateRange, margin, yPos, subtitlePaint)
            yPos += 20f
        } else {
            yPos += 5f
        }

        // Divider
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint.apply {
            color = Color.parseColor("#4CAF50")
            strokeWidth = 2f
        })
        yPos += 15f

        // Summary cards
        val cardWidth = (pageWidth - margin * 2 - 20) / 3
        val cardHeight = 50f

        // Received card
        canvas.drawRect(margin, yPos, margin + cardWidth, yPos + cardHeight, receivedBgPaint)
        canvas.drawText("RECEIVED", margin + 10, yPos + 15, labelPaint)
        val receivedText = "₹${formatAmount(pdfData.totalReceived)}"
        valuePaint.color = Color.parseColor("#2E7D32")
        canvas.drawText(receivedText, margin + 10, yPos + 35, valuePaint)

        // Pending card
        canvas.drawRect(margin + cardWidth + 10, yPos, margin + cardWidth * 2 + 10, yPos + cardHeight, pendingBgPaint)
        canvas.drawText("PENDING", margin + cardWidth + 20, yPos + 15, labelPaint)
        val pendingText = "₹${formatAmount(pdfData.totalPending)}"
        valuePaint.color = Color.parseColor("#E65100")
        canvas.drawText(pendingText, margin + cardWidth + 20, yPos + 35, valuePaint)

        // This month card
        canvas.drawRect(margin + cardWidth * 2 + 20, yPos, pageWidth - margin, yPos + cardHeight, bgPaint)
        canvas.drawText("THIS MONTH", margin + cardWidth * 2 + 30, yPos + 15, labelPaint)
        val monthText = "₹${formatAmount(pdfData.thisMonthTotal)}"
        valuePaint.color = Color.parseColor("#1976D2")
        canvas.drawText(monthText, margin + cardWidth * 2 + 30, yPos + 35, valuePaint)

        yPos += cardHeight + 20f
    } else {
        // Continuation header for subsequent pages
        canvas.drawText("${pdfData.reportTitle} (Page $pageNum)", margin, yPos + 15, subtitlePaint)
        yPos += 30f
    }

    // Table header
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 25f, headerBgPaint)

    val col1 = margin + 5
    val col2 = margin + 35
    val col3 = margin + 100
    val col4 = margin + 200
    val col5 = margin + 280
    val col6 = margin + 350
    val col7 = margin + 430

    canvas.drawText("#", col1, yPos + 17, headerPaint)
    canvas.drawText("DATE", col2, yPos + 17, headerPaint)
    canvas.drawText("VEHICLE", col3, yPos + 17, headerPaint)
    canvas.drawText("CUSTOMER", col4, yPos + 17, headerPaint)
    canvas.drawText("TYPE", col5, yPos + 17, headerPaint)
    canvas.drawText("MODE", col6, yPos + 17, headerPaint)
    canvas.drawText("AMOUNT", col7, yPos + 17, headerPaint)

    yPos += 25f

    // Table rows
    val startIdx = (pageNum - 1) * itemsPerPage
    val endIdx = minOf(startIdx + itemsPerPage, pdfData.payments.size)

    for (i in startIdx until endIdx) {
        val payment = pdfData.payments[i]
        val rowHeight = 30f

        // Alternate row background
        if ((i - startIdx) % 2 == 0) {
            canvas.drawRect(margin, yPos, pageWidth - margin, yPos + rowHeight, bgPaint)
        }

        val textY = yPos + 19

        normalPaint.color = Color.parseColor("#333333")
        canvas.drawText("${i + 1}", col1, textY, normalPaint)
        canvas.drawText(payment.paymentDate.take(10), col2, textY, normalPaint)
        canvas.drawText((payment.vehicleNumber ?: "-").take(12), col3, textY, normalPaint)
        canvas.drawText((payment.customerName ?: "-").take(15), col4, textY, normalPaint)
        canvas.drawText(payment.paymentType.take(10), col5, textY, normalPaint)
        canvas.drawText(payment.paymentMode.take(10), col6, textY, normalPaint)

        // Amount with color based on status
        valuePaint.color = when (payment.paymentStatus.lowercase()) {
            "received" -> Color.parseColor("#2E7D32")
            "pending" -> Color.parseColor("#E65100")
            else -> Color.parseColor("#333333")
        }
        canvas.drawText("₹${formatAmount(payment.amount)}", col7, textY, valuePaint)

        yPos += rowHeight
    }

    // Footer
    yPos = pageHeight - 40f
    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
    yPos += 15f

    val footerPaint = Paint().apply {
        color = Color.parseColor("#999999")
        textSize = 8f
        isAntiAlias = true
    }

    canvas.drawText("IndusJS Fleet Management • Computer generated report", margin, yPos, footerPaint)
    canvas.drawText("Page $pageNum of $totalPages", pageWidth - margin - 60, yPos, footerPaint)
}

/**
 * Generate native PDF for Payment Receipt.
 */
private fun generatePaymentReceiptNativePdf(
    pdfData: PaymentReceiptPdfData,
    fileName: String
): PaymentPdfExportResult? {
    Log.d(TAG, "Generating Payment Receipt native PDF...")

    val pageWidth = 595
    val pageHeight = 842

    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    drawPaymentReceiptContent(canvas, pdfData, pageWidth, pageHeight)

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

    return PaymentPdfExportResult(
        file = pdfFile,
        fileName = fileName,
        filePath = "/sdcard/IndusJSFleet/exportedPdf/payments/$fileName",
        title = "Payment Receipt #${pdfData.receiptNumber ?: pdfData.paymentId}",
        exportDateTime = "${pdfData.generatedDate} ${pdfData.generatedTime}"
    )
}

/**
 * Draw Payment Receipt content.
 */
private fun drawPaymentReceiptContent(
    canvas: Canvas,
    pdfData: PaymentReceiptPdfData,
    pageWidth: Int,
    pageHeight: Int
) {
    val margin = 60f
    var yPos = margin

    // Paints
    val titlePaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val companyPaint = Paint().apply {
        color = Color.parseColor("#666666")
        textSize = 12f
        isAntiAlias = true
    }

    val receiptNoPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        textSize = 16f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val sectionPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        textSize = 11f
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
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val normalPaint = Paint().apply {
        color = Color.parseColor("#333333")
        textSize = 10f
        isAntiAlias = true
    }

    val linePaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
    }

    val bgPaint = Paint().apply {
        color = Color.parseColor("#E8F5E9")
        style = Paint.Style.FILL
    }

    val amountBgPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }

    // Receipt border
    val borderPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    canvas.drawRect(margin - 20, margin - 20, pageWidth - margin + 20f, pageHeight - margin + 20f, borderPaint)

    // Header
    val title = "🧾 Payment Receipt"
    val titleWidth = titlePaint.measureText(title)
    canvas.drawText(title, (pageWidth - titleWidth) / 2, yPos + 24, titlePaint)
    yPos += 35f

    val company = "IndusJS Fleet Management"
    val companyWidth = companyPaint.measureText(company)
    canvas.drawText(company, (pageWidth - companyWidth) / 2, yPos, companyPaint)
    yPos += 25f

    // Dashed line
    drawDashedLine(canvas, margin, yPos, pageWidth - margin, linePaint)
    yPos += 20f

    // Receipt number box
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 45f, bgPaint)
    canvas.drawText("RECEIPT NUMBER", (pageWidth / 2f) - 35, yPos + 15, labelPaint)
    val receiptNo = pdfData.receiptNumber ?: "PMT-${pdfData.paymentId}"
    val receiptWidth = receiptNoPaint.measureText(receiptNo)
    canvas.drawText(receiptNo, (pageWidth - receiptWidth) / 2, yPos + 35, receiptNoPaint)
    yPos += 55f

    // Amount section (large green box)
    canvas.drawRect(margin, yPos, pageWidth - margin, yPos + 80f, amountBgPaint)

    val amountLabelPaint = Paint().apply {
        color = Color.WHITE
        textSize = 11f
        isAntiAlias = true
    }
    canvas.drawText("AMOUNT RECEIVED", (pageWidth / 2f) - 45, yPos + 20, amountLabelPaint)

    val amountPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val amountText = "₹${formatAmount(pdfData.netAmount)}"
    val amountWidth = amountPaint.measureText(amountText)
    canvas.drawText(amountText, (pageWidth - amountWidth) / 2, yPos + 55, amountPaint)

    val statusPaint = Paint().apply {
        color = Color.WHITE
        textSize = 10f
        isAntiAlias = true
    }
    val statusText = pdfData.paymentStatus
    val statusWidth = statusPaint.measureText(statusText)
    canvas.drawText(statusText, (pageWidth - statusWidth) / 2, yPos + 72, statusPaint)

    yPos += 95f

    // Payment Details Section
    canvas.drawText("📅 PAYMENT DETAILS", margin, yPos, sectionPaint)
    yPos += 5f
    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
    yPos += 20f

    yPos = drawDetailRow(canvas, "Date", pdfData.paymentDate, margin, yPos, pageWidth, labelPaint, normalPaint)
    pdfData.paymentTime?.let {
        yPos = drawDetailRow(canvas, "Time", it, margin, yPos, pageWidth, labelPaint, normalPaint)
    }
    yPos = drawDetailRow(canvas, "Payment Type", pdfData.paymentType, margin, yPos, pageWidth, labelPaint, normalPaint)
    yPos = drawDetailRow(canvas, "Payment Mode", pdfData.paymentMode, margin, yPos, pageWidth, labelPaint, normalPaint)
    pdfData.transactionId?.let {
        yPos = drawDetailRow(canvas, "Transaction ID", it, margin, yPos, pageWidth, labelPaint, normalPaint)
    }
    pdfData.bankName?.let {
        yPos = drawDetailRow(canvas, "Bank", it, margin, yPos, pageWidth, labelPaint, normalPaint)
    }
    yPos += 10f

    // Amount Breakdown (if TDS or discount)
    if (pdfData.tdsAmount > 0 || pdfData.discountAmount > 0) {
        canvas.drawText("💰 AMOUNT BREAKDOWN", margin, yPos, sectionPaint)
        yPos += 5f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        yPos = drawDetailRow(canvas, "Gross Amount", "₹${formatAmount(pdfData.amount)}", margin, yPos, pageWidth, labelPaint, normalPaint)
        if (pdfData.tdsAmount > 0) {
            yPos = drawDetailRow(canvas, "TDS Deduction", "- ₹${formatAmount(pdfData.tdsAmount)}", margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        if (pdfData.discountAmount > 0) {
            yPos = drawDetailRow(canvas, "Discount", "- ₹${formatAmount(pdfData.discountAmount)}", margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        yPos = drawDetailRow(canvas, "Net Amount", "₹${formatAmount(pdfData.netAmount)}", margin, yPos, pageWidth, labelPaint, valuePaint)
        yPos += 10f
    }

    // Trip Information
    if (pdfData.tripId != null || pdfData.vehicleNumber != null) {
        canvas.drawText("🚛 TRIP INFORMATION", margin, yPos, sectionPaint)
        yPos += 5f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        pdfData.tripId?.let {
            yPos = drawDetailRow(canvas, "Trip ID", "#$it", margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        pdfData.vehicleNumber?.let {
            yPos = drawDetailRow(canvas, "Vehicle", it, margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        pdfData.driverName?.let {
            yPos = drawDetailRow(canvas, "Driver", it, margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        if (pdfData.startLocation != null && pdfData.endLocation != null) {
            yPos = drawDetailRow(canvas, "Route", "${pdfData.startLocation} → ${pdfData.endLocation}", margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        pdfData.tripPrice?.let {
            yPos = drawDetailRow(canvas, "Trip Price", "₹${formatAmount(it)}", margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        yPos += 10f
    }

    // Customer Information
    if (pdfData.customerName != null) {
        canvas.drawText("👤 CUSTOMER INFORMATION", margin, yPos, sectionPaint)
        yPos += 5f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 20f

        pdfData.customerName?.let {
            yPos = drawDetailRow(canvas, "Name", it, margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        pdfData.customerContact?.let {
            yPos = drawDetailRow(canvas, "Contact", it, margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        pdfData.customerCompany?.let {
            yPos = drawDetailRow(canvas, "Company", it, margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        pdfData.customerGst?.let {
            yPos = drawDetailRow(canvas, "GST No.", it, margin, yPos, pageWidth, labelPaint, normalPaint)
        }
        yPos += 10f
    }

    // Notes
    if (pdfData.notes != null) {
        canvas.drawText("📝 NOTES", margin, yPos, sectionPaint)
        yPos += 5f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
        yPos += 15f
        canvas.drawText(pdfData.notes.take(80), margin, yPos, normalPaint)
        yPos += 20f
    }

    // Footer
    yPos = pageHeight - margin - 50f
    drawDashedLine(canvas, margin, yPos, pageWidth - margin, linePaint)
    yPos += 20f

    val thankYouPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    val thankYou = "Thank You for Your Business!"
    val thankYouWidth = thankYouPaint.measureText(thankYou)
    canvas.drawText(thankYou, (pageWidth - thankYouWidth) / 2, yPos, thankYouPaint)
    yPos += 20f

    val footerPaint = Paint().apply {
        color = Color.parseColor("#999999")
        textSize = 8f
        isAntiAlias = true
    }
    val footer1 = "Generated on ${pdfData.generatedDate} at ${pdfData.generatedTime}"
    val footer1Width = footerPaint.measureText(footer1)
    canvas.drawText(footer1, (pageWidth - footer1Width) / 2, yPos, footerPaint)
    yPos += 12f

    val footer2 = "This is a computer-generated receipt."
    val footer2Width = footerPaint.measureText(footer2)
    canvas.drawText(footer2, (pageWidth - footer2Width) / 2, yPos, footerPaint)
}

private fun drawDetailRow(
    canvas: Canvas,
    label: String,
    value: String,
    margin: Float,
    yPos: Float,
    pageWidth: Int,
    labelPaint: Paint,
    valuePaint: Paint
): Float {
    canvas.drawText(label, margin, yPos, labelPaint)
    canvas.drawText(value.take(40), pageWidth - margin - valuePaint.measureText(value.take(40)), yPos, valuePaint)
    return yPos + 18f
}

private fun drawDashedLine(canvas: Canvas, startX: Float, y: Float, endX: Float, paint: Paint) {
    val dashLength = 5f
    val gapLength = 3f
    var x = startX
    while (x < endX) {
        canvas.drawLine(x, y, minOf(x + dashLength, endX), y, paint)
        x += dashLength + gapLength
    }
}

private fun formatAmount(value: Double): String {
    val intPart = value.toLong()
    return formatWithIndianCommas(intPart)
}

private fun formatWithIndianCommas(value: Long): String {
    if (value < 1000) return value.toString()
    val str = value.toString()
    val len = str.length
    val sb = StringBuilder()
    var count = 0
    for (i in len - 1 downTo 0) {
        sb.insert(0, str[i])
        count++
        if (i > 0) {
            if (count == 3 || (count > 3 && (count - 3) % 2 == 0)) {
                sb.insert(0, ',')
            }
        }
    }
    return sb.toString()
}

private fun sharePaymentPdfFile(context: Context, file: File, title: String) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Please find the attached $title")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share $title"))
    } catch (e: Exception) {
        Log.e(TAG, "Error sharing PDF", e)
    }
}

@Composable
private fun PaymentPdfExportResultDialog(
    result: PaymentPdfExportResult,
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
                // Success icon
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${result.title} exported successfully!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "📤 Share", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
