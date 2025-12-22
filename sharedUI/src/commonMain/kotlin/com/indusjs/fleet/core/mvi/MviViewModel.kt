package com.indusjs.fleet.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing MVI (Model-View-Intent) pattern.
 *
 * @param State The UI state type that extends [UiState]
 * @param Intent The user intent type that extends [UiIntent]
 * @param Effect The side effect type that extends [UiEffect]
 */
abstract class MviViewModel<State : UiState, Intent : UiIntent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    // Private mutable state
    private val _state = MutableStateFlow(initialState)

    /**
     * The current UI state as an immutable StateFlow.
     * Observe this in your Composables to react to state changes.
     */
    val state: StateFlow<State> = _state.asStateFlow()

    // Channel for one-time side effects
    private val _effect = Channel<Effect>(Channel.BUFFERED)

    /**
     * Flow of one-time side effects.
     * Collect this in your Composables to handle navigation, snackbars, etc.
     */
    val effect = _effect.receiveAsFlow()

    // SharedFlow for intents
    private val _intent = MutableSharedFlow<Intent>()

    init {
        viewModelScope.launch {
            _intent.collect { intent ->
                handleIntent(intent)
            }
        }
    }

    /**
     * Current state value for quick access within the ViewModel.
     */
    protected val currentState: State
        get() = _state.value

    /**
     * Send an intent to be processed by the ViewModel.
     * Call this from your Composables when user actions occur.
     */
    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            _intent.emit(intent)
        }
    }

    /**
     * Override this method to handle incoming intents and update state accordingly.
     */
    protected abstract suspend fun handleIntent(intent: Intent)

    /**
     * Update the UI state using a reducer function.
     * The reducer receives the current state and returns a new state.
     */
    protected fun updateState(reducer: State.() -> State) {
        _state.value = currentState.reducer()
    }

    /**
     * Send a one-time side effect to the UI.
     * Use this for navigation, showing snackbars, etc.
     */
    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}

