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
 * - Offline caching with Settings-based storage
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

    val snackbarHostState = remember { SnackbarHostState() }
    val viewModelProvider = remember { DefaultViewModelProvider() }

    // Check if user is already logged in to determine initial route
    var isCheckingAuth by remember { mutableStateOf(true) }
    var initialRoute by remember { mutableStateOf<FleetRoute>(FleetRoute.Login) }

    // Check auth status on app launch
    LaunchedEffect(Unit) {
        co.touchlab.kermit.Logger.d("App") { "Checking auth status on app launch..." }
        try {
            val isLoggedIn = viewModelProvider.userRepository.isLoggedIn()
            co.touchlab.kermit.Logger.d("App") { "Auth check result: isLoggedIn=$isLoggedIn" }
            initialRoute = if (isLoggedIn) FleetRoute.Dashboard else FleetRoute.Login
            co.touchlab.kermit.Logger.d("App") { "Initial route set to: $initialRoute" }
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("App", e) { "Auth check failed: ${e.message}" }
            // If check fails, default to login
            initialRoute = FleetRoute.Login
        } finally {
            isCheckingAuth = false
            co.touchlab.kermit.Logger.d("App") { "Auth check complete, isCheckingAuth=$isCheckingAuth" }
        }
    }

    // Navigation 3 back stack - initialized with the correct route after auth check
    val backStack: NavBackStack<FleetRoute> = remember(initialRoute, isCheckingAuth) {
        if (!isCheckingAuth) NavBackStack(initialRoute) else NavBackStack(FleetRoute.Login)
    }

    // Initialize app on first composition (cost types caching, etc.)
    val scope = rememberCoroutineScope()
    LaunchedEffect(isCheckingAuth) {
        if (!isCheckingAuth) {
            viewModelProvider.appInitializer.initialize(scope)
        }
    }

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
        if (isCheckingAuth) {
            // Show loading while checking auth status
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
}
