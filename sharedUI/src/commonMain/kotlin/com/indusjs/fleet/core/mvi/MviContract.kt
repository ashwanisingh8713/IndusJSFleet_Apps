package com.indusjs.fleet.core.mvi

/**
 * Marker interface for UI State in MVI pattern.
 * Represents the current state of the UI.
 */
interface UiState

/**
 * Marker interface for UI Intent in MVI pattern.
 * Represents user actions or events that trigger state changes.
 */
interface UiIntent

/**
 * Marker interface for UI Side Effects in MVI pattern.
 * Represents one-time events like navigation, showing snackbars, etc.
 */
interface UiEffect


