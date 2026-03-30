package com.indusjs.uicomponents.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A sealed interface for representing text that can be either:
 * - A localizable string resource (resolved at composition time)
 * - A raw string (e.g., server messages passed through as-is)
 *
 * ViewModels emit [UiText] instances instead of raw strings, and
 * Compose screens call [resolve] to obtain the localized text.
 *
 * Usage in ViewModel:
 * ```
 * updateState { copy(error = UiText.StringRes(Res.string.error_xxx)) }
 * sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_xxx)))
 * ```
 *
 * Usage in Screen:
 * ```
 * state.error?.let { Text(it.resolve()) }
 * ```
 */
sealed interface UiText {

    /**
     * A localizable string resource with optional format arguments.
     *
     * @param resId The [StringResource] identifier.
     * @param args  Optional format arguments (e.g., for `%1$s`, `%1$d`).
     */
    data class StringRes(
        val resId: StringResource,
        val args: List<Any> = emptyList()
    ) : UiText

    /**
     * A raw string value (e.g., server-provided error messages).
     * Displayed as-is — no localization applied.
     *
     * @param value The raw text to display.
     */
    data class Raw(val value: String) : UiText

    /**
     * Resolves this [UiText] to a displayable [String].
     * Must be called within a @Composable scope.
     */
    @Composable
    fun resolve(): String = when (this) {
        is StringRes -> {
            if (args.isEmpty()) {
                stringResource(resId)
            } else {
                stringResource(resId, *args.toTypedArray())
            }
        }
        is Raw -> value
    }
}

