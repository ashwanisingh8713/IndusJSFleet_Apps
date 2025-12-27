package com.indusjs.fleet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.ui.NavDisplay
import com.indusjs.fleet.core.auth.AuthenticationEvent
import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.di.DefaultViewModelProvider
import com.indusjs.fleet.di.ProvideViewModels
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.navigation.FleetRoute
import com.indusjs.fleet.navigation.fleetEntryProvider
import com.indusjs.fleet.navigation.navigateAndClear
import com.indusjs.fleet.theme.AppTheme

/**
 * File picker request for platform-specific file selection.
 */
data class FilePickerRequest(
    val documentType: DocumentType,
    val callback: (fileName: String, fileBytes: ByteArray, mimeType: String) -> Unit
)

/**
 * Main App composable with Navigation 3.
 *
 * Features:
 * - Automatic back stack management
 * - System back button/gesture handling (Android & iOS)
 * - Session expiry handling with auto-redirect to login
 */
@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    onPickFile: ((FilePickerRequest) -> Unit)? = null,
    onOpenDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onDownloadDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onSaveDocument: ((documentName: String, fileBytes: ByteArray, mimeType: String) -> Unit)? = null
) = AppTheme(onThemeChanged) {

    // Navigation 3 back stack
    val backStack: NavBackStack<FleetRoute> = remember { NavBackStack(FleetRoute.Login) }
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModelProvider = remember { DefaultViewModelProvider() }

    // Handle authentication events (session expiry, logout)
    LaunchedEffect(Unit) {
        AuthenticationManager.authEvents.collect { event ->
            when (event) {
                is AuthenticationEvent.Unauthorized,
                is AuthenticationEvent.SessionExpired -> {
                    backStack.navigateAndClear(FleetRoute.Login)
                    snackbarHostState.showSnackbar(
                        when (event) {
                            is AuthenticationEvent.SessionExpired -> event.message
                            else -> "Your session has expired. Please log in again."
                        }
                    )
                }
                is AuthenticationEvent.LoggedOut -> {
                    backStack.navigateAndClear(FleetRoute.Login)
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                ProvideViewModels(viewModelProvider) {
                    NavDisplay(
                        backStack = backStack,
                        entryProvider = fleetEntryProvider(
                            backStack = backStack,
                            onPickFile = onPickFile,
                            onOpenDocument = onOpenDocument,
                            onDownloadDocument = onDownloadDocument,
                            onSaveDocument = onSaveDocument
                        ),
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    }
}
