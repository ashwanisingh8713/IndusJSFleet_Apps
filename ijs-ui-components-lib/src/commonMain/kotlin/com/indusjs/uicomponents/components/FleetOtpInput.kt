package com.indusjs.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.indusjs.uicomponents.theme.FleetTokens

/**
 * Segmented one-time-passcode input — the single, reusable OTP component for the app.
 *
 * A transparent, full-width [BasicTextField] (numeric keyboard, paste aware) is overlaid
 * on top of the visible digit boxes, so a tap anywhere focuses the field and shows the
 * keyboard while the boxes render the value. Input is filtered to digits and clamped to
 * [length]; the box at the caret position is highlighted with the primary colour, filled
 * boxes show their digit, and [isError] turns every box border to the error colour.
 * Day/night safe — colours from [MaterialTheme.colorScheme], sizes from [FleetTokens].
 *
 * @param value current digits (callers keep state; this is stateless)
 * @param onValueChange receives the digits-only, length-clamped value
 * @param length number of digits (e.g. 6 for email, 4 for mobile)
 * @param isError draw all boxes in the error colour
 */
@Composable
fun FleetOtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Visible boxes (behind).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            repeat(length) { index ->
                val char = value.getOrNull(index)?.toString() ?: ""
                val isFocusedBox = enabled && !isError && index == value.length
                val borderColor = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocusedBox -> MaterialTheme.colorScheme.primary
                    char.isNotEmpty() -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outlineVariant
                }
                val borderWidth = if (isFocusedBox || isError) FleetTokens.Border.Emphasis else FleetTokens.Border.Default
                val boxColor = if (isFocusedBox) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                }
                val shape = RoundedCornerShape(FleetTokens.Radius.L)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(FleetTokens.Height.InputField)
                        .clip(shape)
                        .background(boxColor)
                        .border(borderWidth, borderColor, shape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Transparent input on top — captures taps + keyboard, drives [value].
        BasicTextField(
            value = value,
            onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() }.take(length)) },
            modifier = Modifier.matchParentSize(),
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent)
        )
    }
}
