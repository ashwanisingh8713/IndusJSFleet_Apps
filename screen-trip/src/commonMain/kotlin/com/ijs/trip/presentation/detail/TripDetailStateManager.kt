package com.ijs.trip.presentation.detail

import com.ijs.trip.presentation.detail.TripDetailContract.Effect
import com.ijs.trip.presentation.detail.TripDetailContract.State

/**
 * Internal interface for state and effect management.
 * Allows handler classes to read and update ViewModel state without direct ViewModel access.
 */
internal interface TripDetailStateManager {
    val currentTripState: State
    fun updateTripState(reducer: State.() -> State)
    fun emitEffect(effect: Effect)
}

