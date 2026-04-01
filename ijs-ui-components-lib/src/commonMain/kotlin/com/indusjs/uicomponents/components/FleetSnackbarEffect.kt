package com.indusjs.uicomponents.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow

/**
 * Convenience holder for snackbar messages from MVI effects.
 */
data class SnackbarMessage(
    val text: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val isError: Boolean = false
)

/**
 * Creates and remembers a [SnackbarHostState].
 *
 * Convenience wrapper so every screen doesn't repeat:
 * ```
 * val snackbarHostState = remember { SnackbarHostState() }
 * ```
 */
@Composable
fun rememberFleetSnackbarHostState(): SnackbarHostState {
    return remember { SnackbarHostState() }
}

/**
 * Collects a [Flow] of [SnackbarMessage] and displays each as a snackbar.
 *
 * Replaces the manual LaunchedEffect + collect + showSnackbar boilerplate
 * that's duplicated across every screen. Prevents the bug where snackbar
 * effects are silently dropped (UX Finding #69: ReportsScreen).
 *
 * UX Finding #84: "Each screen manually wires SnackbarHostState +
 * LaunchedEffect; ReportsScreen forgot to wire it."
 *
 * Usage:
 * ```
 * val snackbarHostState = rememberFleetSnackbarHostState()
 *
 * FleetSnackbarEffect(
 *     snackbarHostState = snackbarHostState,
 *     effectFlow = viewModel.effect,
 *     extractMessage = { effect ->
 *         when (effect) {
 *             is Effect.ShowSnackbar -> SnackbarMessage(effect.message)
 *             is Effect.ShowError -> SnackbarMessage(effect.message, isError = true)
 *             else -> null
 *         }
 *     }
 * )
 *
 * Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { ... }
 * ```
 *
 * @param snackbarHostState The SnackbarHostState to show messages on.
 * @param effectFlow The MVI effect flow from the ViewModel.
 * @param extractMessage Lambda that maps an effect to a [SnackbarMessage],
 *        or null if the effect is not a snackbar message.
 * @param onActionPerformed Optional callback when the snackbar action is tapped.
 */
@Composable
fun <E> FleetSnackbarEffect(
    snackbarHostState: SnackbarHostState,
    effectFlow: Flow<E>,
    extractMessage: (E) -> SnackbarMessage?,
    onActionPerformed: (() -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            val message = extractMessage(effect) ?: return@collect
            val result = snackbarHostState.showSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
                duration = message.duration
            )
            if (result == SnackbarResult.ActionPerformed) {
                onActionPerformed?.invoke()
            }
        }
    }
}

/**
 * Simplified variant that collects effects and shows snackbar messages.
 * Handles both success and error messages from MVI effects.
 *
 * Usage:
 * ```
 * HandleFleetEffects(
 *     snackbarHostState = snackbarHostState,
 *     effectFlow = viewModel.effect,
 * ) { effect ->
 *     when (effect) {
 *         is Effect.NavigateBack -> onNavigateBack()
 *         is Effect.NavigateTo -> onNavigate(effect.route)
 *         is Effect.ShowSnackbar -> showSnackbar(effect.message)
 *         is Effect.ShowError -> showSnackbar(effect.message, isError = true)
 *     }
 * }
 * ```
 *
 * @param snackbarHostState The SnackbarHostState to show messages on.
 * @param effectFlow The MVI effect flow from the ViewModel.
 * @param onEffect Lambda that handles each effect. Call the provided
 *        `showSnackbar` function for snackbar effects, handle navigation etc. directly.
 */
@Composable
fun <E> HandleFleetEffects(
    snackbarHostState: SnackbarHostState,
    effectFlow: Flow<E>,
    onEffect: suspend EffectScope.(E) -> Unit
) {
    LaunchedEffect(Unit) {
        effectFlow.collect { effect ->
            val scope = EffectScopeImpl(snackbarHostState)
            scope.onEffect(effect)
        }
    }
}

/**
 * Scope for handling effects with built-in snackbar support.
 */
interface EffectScope {
    /**
     * Show a snackbar message.
     *
     * @param message The text to display.
     * @param isError Whether this is an error message (affects duration).
     * @param actionLabel Optional action label.
     */
    suspend fun showSnackbar(
        message: String,
        isError: Boolean = false,
        actionLabel: String? = null
    ): SnackbarResult
}

private class EffectScopeImpl(
    private val snackbarHostState: SnackbarHostState
) : EffectScope {
    override suspend fun showSnackbar(
        message: String,
        isError: Boolean,
        actionLabel: String?
    ): SnackbarResult {
        return snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = if (isError) SnackbarDuration.Long else SnackbarDuration.Short
        )
    }
}

