package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

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

/**
 * Visual transformation for mobile number input (10 digits).
 * Displays as XXX-XXX-XXXX format.
 */
class MobileVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(10) // Max 10 digits
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 2 || i == 5) {
                out.append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 6 -> offset + 1
                    offset <= 10 -> offset + 2
                    else -> 12
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 3 -> offset
                    offset <= 7 -> offset - 1
                    offset <= 12 -> offset - 2
                    else -> 10
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

// ============================================
// Validation Helpers
// ============================================

/**
 * Validates email format.
 */
fun isValidEmail(email: String): Boolean {
    if (email.isBlank()) return false
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return emailRegex.matches(email)
}

/**
 * Validates mobile number (10 digits).
 */
fun isValidMobile(mobile: String): Boolean {
    val digits = mobile.filter { it.isDigit() }
    return digits.length == 10
}

/**
 * Validates date in raw format (8 digits: DDMMYYYY).
 * Checks for valid day (1-31), month (1-12), year (1900-2100).
 */
fun isValidDateRaw(rawDigits: String): Boolean {
    if (rawDigits.length != 8) return false
    val day = rawDigits.substring(0, 2).toIntOrNull() ?: return false
    val month = rawDigits.substring(2, 4).toIntOrNull() ?: return false
    val year = rawDigits.substring(4, 8).toIntOrNull() ?: return false

    if (month < 1 || month > 12) return false
    if (year < 1900 || year > 2100) return false

    val maxDay = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

    return day in 1..maxDay
}

/**
 * Validates time in raw format (4 digits: HHMM).
 * Checks for valid hour (0-23) and minute (0-59).
 */
fun isValidTimeRaw(rawDigits: String): Boolean {
    if (rawDigits.length != 4) return false
    val hour = rawDigits.substring(0, 2).toIntOrNull() ?: return false
    val minute = rawDigits.substring(2, 4).toIntOrNull() ?: return false
    return hour in 0..23 && minute in 0..59
}

/**
 * Formats raw time digits (HHMM) to HH:MM display format.
 */
fun formatTimeRaw(rawDigits: String): String {
    if (rawDigits.length != 4) return rawDigits
    return "${rawDigits.substring(0, 2)}:${rawDigits.substring(2, 4)}"
}

/**
 * Parses HH:MM format to raw digits (HHMM).
 */
fun parseTimeToRaw(timeStr: String): String {
    return timeStr.replace(":", "").filter { it.isDigit() }.take(4)
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
            rawValue.length == 8 -> {{ Text(stringResource(Res.string.date_entered), color = MaterialTheme.colorScheme.primary) }}
            rawValue.isNotEmpty() -> {{ Text(stringResource(Res.string.enter_complete_date), color = MaterialTheme.colorScheme.onSurfaceVariant) }}
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
            rawValue.length == 4 -> {{ Text(stringResource(Res.string.time_entered), color = MaterialTheme.colorScheme.primary) }}
            rawValue.isNotEmpty() -> {{ Text(stringResource(Res.string.enter_complete_time), color = MaterialTheme.colorScheme.onSurfaceVariant) }}
            else -> null
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = timeVisualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}

// ============================================
// Compact Date/Time Fields (for Row layouts)
// ============================================

/**
 * Compact date input field for use in Row layouts.
 * No leading icon, configurable shape, uses DD-MM-YYYY format.
 *
 * @param rawValue The raw digit string (e.g., "31122025")
 * @param onRawValueChange Callback when raw value changes (receives raw digits only)
 * @param modifier Modifier for the field
 * @param label Label for the field
 * @param placeholder Placeholder text
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 */
@Composable
fun FleetDateFieldCompact(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    placeholder: String = "DD-MM-YYYY",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
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
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = dateVisualTransformation,
        modifier = modifier
    )
}

/**
 * Compact time input field for use in Row layouts.
 * No leading icon, configurable shape, uses HH:MM (24-hour) format.
 *
 * @param rawValue The raw digit string (e.g., "1430")
 * @param onRawValueChange Callback when raw value changes (receives raw digits only)
 * @param modifier Modifier for the field
 * @param label Label for the field
 * @param placeholder Placeholder text
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 */
@Composable
fun FleetTimeFieldCompact(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Time (24hr)",
    placeholder: String = "HH:MM",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
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
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = timeVisualTransformation,
        modifier = modifier
    )
}

// ============================================
// Mobile Field with Visual Transformation
// ============================================

/**
 * Mobile number input field with 10-digit limit and optional formatting.
 * Uses visual transformation to display as XXX-XXX-XXXX.
 *
 * @param rawValue The raw digit string (e.g., "9876543210")
 * @param onRawValueChange Callback when raw value changes (receives raw digits only)
 * @param modifier Modifier for the field
 * @param label Label for the field
 * @param placeholder Placeholder text
 * @param leadingEmoji Emoji to show as leading icon
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 * @param useFormatting Whether to display with XXX-XXX-XXXX formatting
 */
@Composable
fun FleetMobileField(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mobile",
    placeholder: String = "Enter mobile number",
    leadingEmoji: String = "📱",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    useFormatting: Boolean = false
) {
    val mobileVisualTransformation = remember { MobileVisualTransformation() }

    OutlinedTextField(
        value = rawValue,
        onValueChange = { input ->
            val filtered = filterDigitsOnly(input, 10)
            onRawValueChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Text(leadingEmoji, modifier = Modifier.padding(start = 12.dp)) },
        isError = isError,
        supportingText = when {
            isError && errorMessage != null -> {{ Text(errorMessage, color = MaterialTheme.colorScheme.error) }}
            rawValue.length == 10 -> {{ Text("✓ Valid mobile number", color = MaterialTheme.colorScheme.primary) }}
            rawValue.isNotEmpty() && rawValue.length < 10 -> {{ Text("Enter 10 digits", color = MaterialTheme.colorScheme.onSurfaceVariant) }}
            else -> null
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        visualTransformation = if (useFormatting) mobileVisualTransformation else VisualTransformation.None,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Compact mobile number input field for use in Row layouts.
 * No leading icon, 10-digit limit.
 *
 * @param rawValue The raw digit string (e.g., "9876543210")
 * @param onRawValueChange Callback when raw value changes (receives raw digits only)
 * @param modifier Modifier for the field
 * @param label Label for the field
 * @param placeholder Placeholder text
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display
 * @param enabled Whether the field is enabled
 */
@Composable
fun FleetMobileFieldCompact(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Mobile",
    placeholder: String = "10-digit number",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = rawValue,
        onValueChange = { input ->
            val filtered = filterDigitsOnly(input, 10)
            onRawValueChange(filtered)
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        modifier = modifier
    )
}

/**
 * Visual transformation for date input (YYYY-MM-DD format - ISO format).
 * Displays delimiters visually while keeping raw digits as actual value.
 */
class IsoDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8) // Max 8 digits: YYYYMMDD
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 3 || i == 5) {
                out.append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 4 -> offset
                    offset <= 6 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 4 -> offset
                    offset <= 7 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> 8
                }
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/**
 * Date input field with YYYY-MM-DD format (ISO format) and visual transformation.
 * Accepts raw digits and displays formatted date.
 *
 * @param rawValue The raw digit string (e.g., "20251231")
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
fun FleetIsoDateField(
    rawValue: String,
    onRawValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    placeholder: String = "YYYY-MM-DD",
    leadingEmoji: String = "📅",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    supportingText: @Composable (() -> Unit)? = null
) {
    val isoDateVisualTransformation = remember { IsoDateVisualTransformation() }

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
            rawValue.isNotEmpty() -> {{ Text("Enter 8 digits for date", color = MaterialTheme.colorScheme.onSurfaceVariant) }}
            else -> null
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = isoDateVisualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Formats raw digits to ISO date string (YYYY-MM-DD).
 */
fun formatToIsoDate(rawDigits: String): String {
    if (rawDigits.length != 8) return rawDigits
    return "${rawDigits.substring(0, 4)}-${rawDigits.substring(4, 6)}-${rawDigits.substring(6, 8)}"
}

/**
 * Parses ISO date string to raw digits.
 */
fun parseIsoDateToRaw(isoDate: String): String {
    return isoDate.replace("-", "")
}

/**
 * Formats raw digits (DDMMYYYY) to DD-MM-YYYY display format.
 */
fun formatToDdMmYyyy(rawDigits: String): String {
    if (rawDigits.length != 8) return rawDigits
    return "${rawDigits.substring(0, 2)}-${rawDigits.substring(2, 4)}-${rawDigits.substring(4, 8)}"
}

/**
 * Parses DD-MM-YYYY string to raw digits (DDMMYYYY).
 */
fun parseDdMmYyyyToRaw(dateStr: String): String {
    return dateStr.replace("-", "")
}

/**
 * Converts raw digits (DDMMYYYY) to ISO format (YYYY-MM-DD) for backend API.
 */
fun convertDdMmYyyyToIso(rawDigits: String): String {
    if (rawDigits.length != 8) return rawDigits
    val day = rawDigits.substring(0, 2)
    val month = rawDigits.substring(2, 4)
    val year = rawDigits.substring(4, 8)
    return "$year-$month-$day"
}

/**
 * Converts ISO format (YYYY-MM-DD) to raw digits (DDMMYYYY) for display.
 */
fun convertIsoToDdMmYyyyRaw(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    val parts = isoDate.split("-")
    if (parts.size != 3) return isoDate.replace("-", "")
    return "${parts[2]}${parts[1]}${parts[0]}" // DDMMYYYY
}

/**
 * Converts ISO format (YYYY-MM-DD) to DD-MM-YYYY display format.
 */
fun convertIsoToDdMmYyyy(isoDate: String): String {
    if (isoDate.isBlank()) return ""
    val parts = isoDate.split("-")
    if (parts.size != 3) return isoDate
    return "${parts[2]}-${parts[1]}-${parts[0]}" // DD-MM-YYYY
}

// ============================================
// Dropdown Components
// ============================================

/**
 * Standard dropdown field with consistent styling.
 * Reusable across all screens for dropdown selection.
 *
 * @param label Label for the field
 * @param value Currently selected value (display text)
 * @param placeholder Placeholder when no value is selected
 * @param isExpanded Whether the dropdown is expanded
 * @param error Error message to display (null if no error)
 * @param enabled Whether the dropdown is enabled
 * @param leadingIcon Optional leading icon composable
 * @param modifier Modifier for the field
 * @param onToggle Called when dropdown should toggle open/close
 * @param onDismiss Called when dropdown should dismiss
 * @param content Composable content for dropdown items
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDropdownField(
    label: String,
    value: String,
    placeholder: String,
    isExpanded: Boolean,
    error: String? = null,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded && enabled,
        onExpandedChange = { if (enabled) onToggle() },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            enabled = enabled,
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            leadingIcon = leadingIcon,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded && enabled)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = isExpanded && enabled,
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            content = content
        )
    }
}

/**
 * Simple dropdown field for selecting from a list of options.
 *
 * @param label Label for the field
 * @param selectedValue Currently selected value (key)
 * @param options List of key-label pairs for options
 * @param placeholder Placeholder when no value is selected
 * @param error Error message to display (null if no error)
 * @param enabled Whether the dropdown is enabled
 * @param modifier Modifier for the field
 * @param onSelect Called when an option is selected with the key
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetSimpleDropdown(
    label: String,
    selectedValue: String?,
    options: List<Pair<String, String>>,
    placeholder: String = "Select option",
    error: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.first == selectedValue }?.second ?: ""

    FleetDropdownField(
        label = label,
        value = selectedLabel,
        placeholder = placeholder,
        isExpanded = isExpanded,
        error = error,
        enabled = enabled,
        modifier = modifier,
        onToggle = { isExpanded = !isExpanded },
        onDismiss = { isExpanded = false }
    ) {
        options.forEach { (key, displayLabel) ->
            DropdownMenuItem(
                text = { Text(displayLabel) },
                onClick = {
                    onSelect(key)
                    isExpanded = false
                }
            )
        }
    }
}
