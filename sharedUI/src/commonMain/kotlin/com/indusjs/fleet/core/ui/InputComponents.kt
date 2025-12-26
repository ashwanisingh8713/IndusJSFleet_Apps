package com.indusjs.fleet.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Styled input components for consistent UI across the app.
 */

// ============================================
// Visual Transformations for Date/Time Fields
// ============================================

/**
 * Visual transformation for date input (DD-MM-YYYY format).
 * Displays delimiters visually while keeping raw digits as actual value.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8) // Max 8 digits: DDMMYYYY
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1 || i == 3) {
                out.append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> 8
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Visual transformation for time input (HH:MM format).
 * Displays colon visually while keeping raw digits as actual value.
 */
class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4) // Max 4 digits: HHMM
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1) {
                out.append(":")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> 5
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> 4
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Filters input to only allow digits.
 * @param input The raw input string
 * @param maxLength Maximum number of digits allowed
 * @return Filtered string containing only digits up to maxLength
 */
fun filterDigitsOnly(input: String, maxLength: Int): String {
    return input.filter { it.isDigit() }.take(maxLength)
}

// ============================================
// Standard Text Input Components
// ============================================

/**
 * Standard text input field with consistent styling.
 */
@Composable
fun FleetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation
    )
}

/**
 * Email input field with validation styling.
 */
@Composable
fun FleetEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Email",
    placeholder: String = "Enter your email",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    FleetTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = { Text("📧") },
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions
    )
}

/**
 * Password input field with visibility toggle.
 */
@Composable
fun FleetPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Password",
    placeholder: String = "Enter your password",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    FleetTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = { Text("🔒") },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Text(if (passwordVisible) "👁️" else "👁️‍🗨️")
            }
        },
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        }
    )
}

/**
 * Phone number input field.
 */
@Composable
fun FleetPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Phone",
    placeholder: String = "Enter your phone number",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    FleetTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        leadingIcon = { Text("📱") },
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions
    )
}

/**
 * Search input field.
 */
@Composable
fun FleetSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    enabled: Boolean = true,
    onSearch: () -> Unit = {}
) {
    FleetTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = { Text("🔍") },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Text("✕")
                }
            }
        } else null,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

// ============================================
// Date and Time Input Components
// ============================================

/**
 * Date input field with DD-MM-YYYY format and visual transformation.
 * Accepts raw digits and displays formatted date.
 *
 * @param rawValue The raw digit string (e.g., "31122025")
 * @param onRawValueChange Callback when raw value changes
 * @param label Label for the field
 * @param placeholder Placeholder text
 * @param leadingEmoji Emoji to show as leading icon
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 * @param modifier Modifier for the field
 * @param supportingText Optional supporting text composable
 */
@Composable
fun FleetDateField(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    placeholder: String = "DD-MM-YYYY",
    leadingEmoji: String = "📅",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }

    OutlinedTextField(
        value = rawValue,
        onValueChange = { input ->
            val filtered = filterDigitsOnly(input, 8)
            onRawValueChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Text(leadingEmoji, modifier = Modifier.padding(start = 12.dp)) },
        isError = isError,
        supportingText = when {
            isError && errorMessage != null -> {{ Text(errorMessage, color = MaterialTheme.colorScheme.error) }}
            supportingText != null -> supportingText
            rawValue.length == 8 -> {{ Text("✓ Date entered", color = MaterialTheme.colorScheme.primary) }}
            rawValue.isNotEmpty() -> {{ Text("Enter complete date (8 digits)", color = MaterialTheme.colorScheme.onSurfaceVariant) }}
            else -> null
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = dateVisualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Time input field with HH:MM format (24-hour) and visual transformation.
 * Accepts raw digits and displays formatted time.
 *
 * @param rawValue The raw digit string (e.g., "1430")
 * @param onRawValueChange Callback when raw value changes
 * @param label Label for the field
 * @param placeholder Placeholder text
 * @param leadingEmoji Emoji to show as leading icon
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 * @param modifier Modifier for the field
 * @param supportingText Optional supporting text composable
 */
@Composable
fun FleetTimeField(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Time (24hr)",
    placeholder: String = "HH:MM",
    leadingEmoji: String = "🕐",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    val timeVisualTransformation = remember { TimeVisualTransformation() }

    OutlinedTextField(
        value = rawValue,
        onValueChange = { input ->
            val filtered = filterDigitsOnly(input, 4)
            onRawValueChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Text(leadingEmoji, modifier = Modifier.padding(start = 12.dp)) },
        isError = isError,
        supportingText = when {
            isError && errorMessage != null -> {{ Text(errorMessage, color = MaterialTheme.colorScheme.error) }}
            supportingText != null -> supportingText
            rawValue.length == 4 -> {{ Text("✓ Time entered", color = MaterialTheme.colorScheme.primary) }}
            rawValue.isNotEmpty() -> {{ Text("Enter complete time (4 digits)", color = MaterialTheme.colorScheme.onSurfaceVariant) }}
            else -> null
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = timeVisualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}

