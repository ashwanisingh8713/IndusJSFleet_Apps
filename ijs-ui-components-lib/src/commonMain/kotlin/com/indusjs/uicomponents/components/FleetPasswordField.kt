package com.indusjs.uicomponents.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The canonical password input for the entire application.
 *
 * A thin, intention-revealing wrapper over [FleetInputField] with
 * [FieldType.PASSWORD] — it provides masked input, the show/hide visibility
 * toggle, and the password keyboard, with consistent styling everywhere.
 *
 * **No client-side validation.** Password policy (length, complexity, etc.) is
 * owned entirely by the backend; the app only collects the value and renders any
 * error supplied via [errorMessage] (e.g. a backend field error, or a UI-only
 * confirm-password mismatch). Do not add password rules here.
 *
 * @param value Current password text.
 * @param onValueChange Callback for value changes.
 * @param modifier Modifier for the field.
 * @param label Label displayed above the field.
 * @param placeholder Placeholder inside the field.
 * @param isError Whether the field is in error state.
 * @param errorMessage Error message rendered below the field (backend error or confirm mismatch).
 * @param enabled Whether the field accepts input.
 * @param keyboardActions Override keyboard actions (e.g. onDone to submit a login form).
 * @param accessibilityLabel Content description for screen readers.
 */
@Composable
fun FleetPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    accessibilityLabel: String? = label
) {
    FleetInputField(
        value = value,
        onValueChange = onValueChange,
        fieldType = FieldType.PASSWORD,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardActions = keyboardActions,
        accessibilityLabel = accessibilityLabel
    )
}
