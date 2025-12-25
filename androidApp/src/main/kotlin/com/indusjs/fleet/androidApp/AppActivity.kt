package com.indusjs.fleet.androidApp

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { 
            AndroidApp()
        }
    }
}

@Composable
private fun AndroidApp() {
    val context = LocalContext.current
    var pendingRequest by remember { mutableStateOf<FilePickerRequest?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            pendingRequest?.let { request ->
                try {
                    // Read file content
                    val contentResolver = context.contentResolver
                    val mimeType = contentResolver.getType(selectedUri) ?: "application/octet-stream"

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
        onPickFile = { request ->
            pendingRequest = request
            // Launch file picker for documents
            filePickerLauncher.launch(arrayOf(
                "application/pdf",
                "image/jpeg",
                "image/png"
            ))
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
