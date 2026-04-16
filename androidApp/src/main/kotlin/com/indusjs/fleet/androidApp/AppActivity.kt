package com.indusjs.fleet.androidApp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.indusjs.fleet.App
import com.indusjs.fleet.FilePickerRequest
import com.ijs.subscription.presentation.platform.AndroidRazorpayBridge
import com.ijs.subscription.presentation.platform.RazorpayResult
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class AppActivity : ComponentActivity(), PaymentResultWithDataListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidApp(activity = this)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        AndroidRazorpayBridge.emit(
            RazorpayResult.Success(
                orderId = paymentData?.orderId ?: "",
                paymentId = razorpayPaymentId ?: "",
                signature = paymentData?.signature ?: ""
            )
        )
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?, paymentData: PaymentData?) {
        // Razorpay Android: user closed checkout / back without paying (commonly code 3).
        if (errorCode == Checkout.PAYMENT_CANCELED) {
            AndroidRazorpayBridge.emit(RazorpayResult.Cancelled)
            return
        }
        AndroidRazorpayBridge.emit(
            RazorpayResult.Failed(
                errorCode = errorCode,
                description = errorDescription ?: "Payment failed"
            )
        )
    }
}

/**
 * Custom contract for picking documents with multiple MIME types
 */
class PickDocumentContract : ActivityResultContract<Array<String>, Uri?>() {
    override fun createIntent(context: Context, input: Array<String>): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK) intent?.data else null
    }
}

@Composable
private fun AndroidApp(activity: AppActivity) {
    val context = LocalContext.current
    var pendingRequest by remember { mutableStateOf<FilePickerRequest?>(null) }

    // Supported MIME types for document upload
    val supportedMimeTypes = arrayOf(
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/*"
    )

    // File picker launcher using custom contract for multiple MIME types
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = PickDocumentContract()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            pendingRequest?.let { request ->
                try {
                    // Read file content
                    val contentResolver = context.contentResolver
                    val mimeType = contentResolver.getType(selectedUri) ?: "application/octet-stream"

                    // Validate MIME type
                    val isValidType = mimeType.startsWith("image/") ||
                                      mimeType == "application/pdf"

                    if (!isValidType) {
                        // Invalid file type - could show error
                        pendingRequest = null
                        return@rememberLauncherForActivityResult
                    }

                    // Get file name
                    val fileName = contentResolver.query(selectedUri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        if (nameIndex >= 0) cursor.getString(nameIndex) else "document"
                    } ?: "document"

                    // Read file bytes
                    val fileBytes = contentResolver.openInputStream(selectedUri)?.use {
                        it.readBytes()
                    } ?: ByteArray(0)

                    // Call the callback
                    request.callback(fileName, fileBytes, mimeType)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        pendingRequest = null
    }

    App(
        onThemeChanged = { ThemeChanged(it) },
        razorpayLauncher = com.ijs.subscription.presentation.platform.createAndroidRazorpayLauncher(activity),
        onPickFile = { request ->
            pendingRequest = request
            // Launch file picker for documents (PDF, JPEG, PNG)
            filePickerLauncher.launch(supportedMimeTypes)
        },
        onOpenDocument = { documentName, fileUrl ->
            // Open document in browser or external app
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(fileUrl)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        onDownloadDocument = { documentName, fileUrl ->
            // Open download URL in browser to trigger download
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(fileUrl)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        onSaveDocument = { documentName, fileBytes, mimeType ->
            // Save document to Downloads folder and open it
            try {
                val fileName = if (documentName.contains(".")) documentName else "$documentName.pdf"
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val file = java.io.File(downloadsDir, fileName)
                file.writeBytes(fileBytes)

                // Open the downloaded file
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: just save without opening
                try {
                    val fileName = if (documentName.contains(".")) documentName else "$documentName.pdf"
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                    val file = java.io.File(downloadsDir, fileName)
                    file.writeBytes(fileBytes)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
        }
    )
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Light appearance = dark icons, so we want dark icons in light mode
            // isDark=true (dark mode) -> light icons -> isAppearanceLightStatusBars = false
            // isDark=false (light mode) -> dark icons -> isAppearanceLightStatusBars = true
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }
}
