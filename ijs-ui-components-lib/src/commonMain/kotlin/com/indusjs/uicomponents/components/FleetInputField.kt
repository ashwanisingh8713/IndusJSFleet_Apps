package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.painterResource

/**
 * Classification of every text input in the application.
 *
 * The component derives [KeyboardType], [ImeAction], `singleLine`, and
 * `maxLines` internally from this type. Callers cannot override the
 * multiline policy — only [ADDRESS] and [NOTES] allow multiple lines.
 */
enum class FieldType {
    DEFAULT,
    EMAIL,
    PASSWORD,
    PHONE,
    SEARCH,
    NUMBER,
    DECIMAL,
    ADDRESS,
    NOTES
}

/**
 * The canonical, sole editable text input for the entire application.
 *
 * ### Single-line enforcement
 * All [FieldType]s are single-line (`maxLines = 1`) **except**
 * [FieldType.ADDRESS] and [FieldType.NOTES] which allow up to 5 lines.
 * There is no parameter to override this — it is intentional.
 *
 * ### Keyboard derivation
 * [KeyboardType] and [ImeAction] are derived automatically from [fieldType].
 * Callers do not set keyboard options manually.
 *
 * ### Adaptive width
 * On [FleetBreakpoint.Compact] the field fills maximum available width.
 * On Medium / Expanded it respects its [modifier] but defaults to filling
 * its column.
 *
 * ### IME padding
 * The component applies `imePadding()` so the keyboard never covers it.
 *
 * @param value Current text value.
 * @param onValueChange Callback for value changes.
 * @param fieldType Determines keyboard, IME action, and multiline policy.
 * @param modifier Modifier for the text field.
 * @param label Label displayed above the field.
 * @param placeholder Placeholder inside the field.
 * @param leadingIcon Optional leading icon slot.
 * @param trailingIcon Optional trailing icon slot.
 * @param isError Whether the field is in error state.
 * @param errorMessage Error message rendered below the field in error colour.
 * @param enabled Whether the field accepts input.
 * @param readOnly Whether the field is read-only (focusable but not editable).
 * @param visualTransformation Visual transformation (e.g. date formatting).
 * @param keyboardActions Override keyboard actions (onSearch, onDone, etc.).
 * @param accessibilityLabel Content description for screen readers.
 */
@Composable
fun FleetInputField(
    value: String,
    onValueChange: (String) -> Unit,
    fieldType: FieldType = FieldType.DEFAULT,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    accessibilityLabel: String? = label
) {
    val isSingleLine = fieldType != FieldType.ADDRESS && fieldType != FieldType.NOTES
    val maxLines = if (isSingleLine) 1 else 5

    val keyboardType = when (fieldType) {
        FieldType.DEFAULT -> KeyboardType.Text
        FieldType.EMAIL -> KeyboardType.Email
        FieldType.PASSWORD -> KeyboardType.Password
        FieldType.PHONE -> KeyboardType.Phone
        FieldType.SEARCH -> KeyboardType.Text
        FieldType.NUMBER -> KeyboardType.Number
        FieldType.DECIMAL -> KeyboardType.Decimal
        FieldType.ADDRESS -> KeyboardType.Text
        FieldType.NOTES -> KeyboardType.Text
    }

    val imeAction = when (fieldType) {
        FieldType.SEARCH -> ImeAction.Search
        FieldType.PASSWORD -> ImeAction.Done
        FieldType.ADDRESS, FieldType.NOTES -> ImeAction.Default
        else -> ImeAction.Next
    }

    var passwordVisible by remember { mutableStateOf(false) }

    val effectiveVisualTransformation = when {
        fieldType == FieldType.PASSWORD && !passwordVisible -> PasswordVisualTransformation()
        visualTransformation != VisualTransformation.None -> visualTransformation
        else -> VisualTransformation.None
    }

    val effectiveTrailingIcon: @Composable (() -> Unit)? = when {
        fieldType == FieldType.PASSWORD -> {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) Res.drawable.ic_visibility
                            else Res.drawable.ic_visibility_off
                        ),
                        contentDescription = stringResource(if (passwordVisible) Res.string.cd_hide_password else Res.string.cd_show_password),
                        modifier = Modifier.size(FleetTokens.IconSize.Default),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        trailingIcon != null -> trailingIcon
        else -> null
    }

    val semanticsModifier = if (accessibilityLabel != null) {
        Modifier.semantics { contentDescription = accessibilityLabel }
    } else {
        Modifier
    }

    BoxWithConstraints(modifier = modifier) {
        val bp = rememberFleetBreakpoint()
        val widthModifier = when (bp) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .then(widthModifier)
                .then(semanticsModifier)
                .imePadding(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = effectiveTrailingIcon,
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = isSingleLine,
            maxLines = maxLines,
            shape = RoundedCornerShape(FleetTokens.Radius.L),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            visualTransformation = effectiveVisualTransformation
        )
    }
}
