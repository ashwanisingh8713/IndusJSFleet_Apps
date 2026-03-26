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
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.di.DefaultViewModelProvider
import com.indusjs.fleet.di.ProvideViewModels
import com.ijs.vehicle.domain.entity.DocumentType
import com.indusjs.fleet.navigation.FleetRoute
import com.indusjs.fleet.navigation.fleetEntryProvider
import com.indusjs.fleet.navigation.navigateAndClear
import com.indusjs.uicomponents.theme.AppTheme
import com.indusjs.fleet.core.logger.initPlatformLogger

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

    // Initialize platform logger (no-op on Android/iOS where it's done earlier;
    // initializes IjsLogger with marker PlatformContext on JS/WasmJS).
    // Idempotent — IjsLogger.init() guards against re-initialization.
    initPlatformLogger()

    val snackbarHostState = remember { SnackbarHostState() }
    // Use singleton instance to ensure Settings persistence across app lifecycle
    val viewModelProvider = remember { DefaultViewModelProvider.getInstance() }
    val fleetLogger: FleetLogger = remember { viewModelProvider.fleetLogger }

    // Check if user is already logged in to determine initial route
    var isCheckingAuth by remember { mutableStateOf(true) }
    var initialRoute by remember { mutableStateOf<FleetRoute>(FleetRoute.Login) }

    // Check auth status on app launch
    LaunchedEffect(Unit) {
        fleetLogger.d(TAG_APP, "Checking app launch status...")
        try {
            // Check if onboarding has been completed
            val onboardingCompleted = viewModelProvider.hasCompletedOnboarding()
            fleetLogger.d(TAG_APP, "Onboarding completed: $onboardingCompleted")

            if (!onboardingCompleted) {
                initialRoute = FleetRoute.Onboarding
                fleetLogger.d(TAG_APP, "Initial route set to: Onboarding")
            } else {
                val isLoggedIn = viewModelProvider.userRepository.isLoggedIn()
                fleetLogger.d(TAG_APP, "Auth check result: isLoggedIn=$isLoggedIn")
                initialRoute = if (isLoggedIn) FleetRoute.Dashboard else FleetRoute.Login
                fleetLogger.d(TAG_APP, "Initial route set to: $initialRoute")
            }
        } catch (e: Exception) {
            fleetLogger.e(TAG_APP, "Startup check failed: ${e.message}", e)
            // If check fails, default to login
            initialRoute = FleetRoute.Login
        } finally {
            isCheckingAuth = false
            fleetLogger.d(TAG_APP, "Startup check complete, isCheckingAuth=$isCheckingAuth")
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
    // IMPORTANT: Use backStack as key so collector restarts when backStack is recreated
    // This fixes the issue where auth events were collected with stale backStack reference
    LaunchedEffect(backStack) {
        fleetLogger.d(TAG_APP, "Starting auth event collector with backStack hash: ${backStack.hashCode()}")
        AuthenticationManager.authEvents.collect { event ->
            fleetLogger.w(TAG_APP, "Auth event received: $event")
            try {
                when (event) {
                    is AuthenticationEvent.Unauthorized,
                    is AuthenticationEvent.SessionExpired -> {
                        fleetLogger.w(TAG_APP, "Session expired/unauthorized - navigating to Login")
                        backStack.navigateAndClear(FleetRoute.Login)
                        fleetLogger.d(TAG_APP, "Navigation to Login completed")
                        val message = when (event) {
                            is AuthenticationEvent.SessionExpired -> event.message
                            else -> "Your session has expired. Please log in again."
                        }
                        snackbarHostState.showSnackbar(message)
                        fleetLogger.d(TAG_APP, "Snackbar shown: $message")
                    }
                    is AuthenticationEvent.LoggedOut -> {
                        fleetLogger.d(TAG_APP, "User logged out - navigating to Login")
                        backStack.navigateAndClear(FleetRoute.Login)
                        fleetLogger.d(TAG_APP, "Navigation to Login completed (logout)")
                    }
                }
            } catch (e: Exception) {
                fleetLogger.e(TAG_APP, "Error handling auth event: ${e.message}", e)
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
