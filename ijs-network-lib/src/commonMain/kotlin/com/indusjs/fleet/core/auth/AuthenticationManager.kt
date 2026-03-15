package com.indusjs.fleet.core.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Authentication events that can be emitted by the AuthenticationManager.
 */
sealed interface AuthenticationEvent {
    /**
     * Emitted when a 401 Unauthorized response is received from the API.
     * The app should redirect to the login screen when this event is received.
     */
    data object Unauthorized : AuthenticationEvent

    /**
     * Emitted when the user explicitly logs out.
     */
    data object LoggedOut : AuthenticationEvent

    /**
     * Emitted when the user's session expires.
     */
    data class SessionExpired(val message: String = "Your session has expired. Please log in again.") : AuthenticationEvent
}

/**
 * Singleton manager for handling authentication state and events across the app.
 *
 * This manager provides a centralized way to handle authentication-related events,
 * particularly 401 Unauthorized responses from the API.
 *
 * Usage:
 * ```kotlin
 * // Register session clear callback at app startup:
 * AuthenticationManager.registerSessionClearCallback {
 *     userRepository.logout()
 * }
 *
 * // In your App composable, observe auth events:
 * LaunchedEffect(Unit) {
 *     AuthenticationManager.authEvents.collect { event ->
 *         when (event) {
 *             is AuthenticationEvent.Unauthorized,
 *             is AuthenticationEvent.SessionExpired,
 *             is AuthenticationEvent.LoggedOut -> {
 *                 // Navigate to login screen
 *                 currentRoute = AppRoute.Login
 *             }
 *         }
 *     }
 * }
 *
 * // In HTTP interceptor or repository:
 * if (response.status == HttpStatusCode.Unauthorized) {
 *     AuthenticationManager.emitUnauthorized()
 * }
 * ```
 */
object AuthenticationManager {

    private val _authEvents = MutableSharedFlow<AuthenticationEvent>(
        replay = 1, // Ensures late collectors receive the last event
        extraBufferCapacity = 1
    )

    /**
     * Flow of authentication events.
     * Collect this flow to react to authentication state changes.
     */
    val authEvents: SharedFlow<AuthenticationEvent> = _authEvents.asSharedFlow()

    /**
     * Callback to clear the user session.
     * This should be registered at app startup with the appropriate repository method.
     */
    private var sessionClearCallback: (suspend () -> Unit)? = null

    /**
     * Flag to prevent multiple 401 events being processed simultaneously.
     */
    private var isHandlingUnauthorized = false

    /**
     * Register a callback to clear the user session.
     * This callback will be invoked when a 401 is received before emitting the event.
     *
     * @param callback Suspend function that clears the session (e.g., userRepository.logout())
     */
    fun registerSessionClearCallback(callback: suspend () -> Unit) {
        sessionClearCallback = callback
    }

    /**
     * Emit an unauthorized event (401 response received).
     * This will clear the session and trigger navigation to the login screen.
     */
    suspend fun emitUnauthorized() {
        if (isHandlingUnauthorized) return
        isHandlingUnauthorized = true

        try {
            // Clear session first
            sessionClearCallback?.invoke()
            _authEvents.emit(AuthenticationEvent.Unauthorized)
        } finally {
            isHandlingUnauthorized = false
        }
    }

    /**
     * Emit a session expired event.
     * This will clear the session and trigger navigation to the login screen with a message.
     */
    suspend fun emitSessionExpired(message: String = "Your session has expired. Please log in again.") {
        if (isHandlingUnauthorized) return
        isHandlingUnauthorized = true

        try {
            // Clear session first
            sessionClearCallback?.invoke()
            _authEvents.emit(AuthenticationEvent.SessionExpired(message))
        } finally {
            isHandlingUnauthorized = false
        }
    }

    /**
     * Emit a logged out event.
     * This will trigger navigation to the login screen.
     */
    suspend fun emitLoggedOut() {
        sessionClearCallback?.invoke()
        _authEvents.emit(AuthenticationEvent.LoggedOut)
    }

    /**
     * Reset the manager state (useful for testing).
     */
    fun reset() {
        isHandlingUnauthorized = false
        sessionClearCallback = null
    }
}

