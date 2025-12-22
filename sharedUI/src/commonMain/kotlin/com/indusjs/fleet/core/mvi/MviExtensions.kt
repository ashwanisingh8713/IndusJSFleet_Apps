package com.indusjs.fleet.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable helper for handling MVI side effects.
 *
 * @param viewModel The MVI ViewModel
 * @param onEffect Callback invoked when an effect is received
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> HandleEffects(
    viewModel: MviViewModel<S, I, E>,
    onEffect: (E) -> Unit
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            onEffect(effect)
        }
    }
}

/**
 * Collects the state from an MVI ViewModel in a lifecycle-aware manner.
 *
 * @return The current UI state
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.collectState(): State<S> {
    return this.state.collectAsStateWithLifecycle()
}

/**
 * Extension for sending intents from Compose.
 * Creates a lambda that can be used as event handler.
 */
fun <S : UiState, I : UiIntent, E : UiEffect> MviViewModel<S, I, E>.intentHandler(
    intent: I
): () -> Unit = { sendIntent(intent) }

/**
 * Extension for creating intent handlers with parameters.
 */
fun <S : UiState, I : UiIntent, E : UiEffect, T> MviViewModel<S, I, E>.intentHandler(
    createIntent: (T) -> I
): (T) -> Unit = { param -> sendIntent(createIntent(param)) }

