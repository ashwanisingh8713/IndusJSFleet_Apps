package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint

/**
 * Visual variant of a [FleetButton].
 */
enum class ButtonVariant {
    /** Filled with brand colour. */
    PRIMARY,
    /** Outlined with brand colour border. */
    SECONDARY,
    /** Text only, no background or border. */
    GHOST,
    /** Filled with error colour for destructive actions. */
    DESTRUCTIVE
}

/**
 * Size of a [FleetButton]. Heights come from [FleetTokens.Height].
 */
enum class ButtonSize {
    /** 36dp height. */
    SMALL,
    /** 44dp height (default). Meets minimum touch target. */
    MEDIUM,
    /** 52dp height. */
    LARGE
}

/**
 * The canonical, sole button component for the entire application.
 *
 * ### Variants
 * [PRIMARY] (filled, brand), [SECONDARY] (outlined), [GHOST] (text only),
 * [DESTRUCTIVE] (filled, error colour).
 *
 * ### Sizes
 * [SMALL] (36dp), [MEDIUM] (44dp, default), [LARGE] (52dp).
 * Heights come from [FleetTokens.Height], not inline values.
 *
 * ### Adaptive width
 * On Compact breakpoints, buttons fill maximum width by default.
 * On Medium / Expanded they wrap their content.
 * Callers can override with an explicit [modifier].
 *
 * ### Loading state
 * When [isLoading] is true, a spinner replaces the label and leading icon.
 * Click events are silently ignored. Dimensions do not change.
 *
 * @param text Button label.
 * @param onClick Click handler.
 * @param variant Visual variant.
 * @param size Button size.
 * @param modifier Modifier for the button.
 * @param enabled Whether the button is enabled.
 * @param isLoading Whether to show loading spinner.
 * @param leadingIcon Optional leading icon slot.
 * @param accessibilityLabel Content description for screen readers.
 */
@Composable
fun FleetButton(
    text: String,
    onClick: () -> Unit,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    size: ButtonSize = ButtonSize.MEDIUM,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    accessibilityLabel: String? = text
) {
    val heightDp = when (size) {
        ButtonSize.SMALL -> FleetTokens.Height.ButtonSmall
        ButtonSize.MEDIUM -> FleetTokens.Height.ButtonMedium
        ButtonSize.LARGE -> FleetTokens.Height.ButtonLarge
    }

    val padding = PaddingValues(
        horizontal = FleetTokens.Spacing.XL,
        vertical = FleetTokens.Spacing.M
    )

    val semanticsModifier = if (accessibilityLabel != null) {
        Modifier.semantics { contentDescription = accessibilityLabel }
    } else {
        Modifier
    }

    BoxWithConstraints {
        val bp = rememberFleetBreakpoint()
        val widthModifier = when (bp) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier
        }

        val baseModifier = modifier
            .then(widthModifier)
            .height(heightDp)
            .defaultMinSize(minHeight = FleetTokens.Height.MinTouchTarget)
            .then(semanticsModifier)

        val effectiveEnabled = enabled && !isLoading

        val content: @Composable () -> Unit = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(FleetTokens.IconSize.Default),
                    strokeWidth = FleetTokens.Height.ProgressStroke,
                    color = when (variant) {
                        ButtonVariant.PRIMARY -> MaterialTheme.colorScheme.onPrimary
                        ButtonVariant.DESTRUCTIVE -> MaterialTheme.colorScheme.onError
                        ButtonVariant.SECONDARY -> MaterialTheme.colorScheme.primary
                        ButtonVariant.GHOST -> MaterialTheme.colorScheme.primary
                    }
                )
            } else {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                }
                Text(text = text, fontWeight = FontWeight.Medium)
            }
        }

        when (variant) {
            ButtonVariant.PRIMARY -> {
                Button(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = effectiveEnabled,
                    contentPadding = padding,
                    content = { content() }
                )
            }

            ButtonVariant.SECONDARY -> {
                OutlinedButton(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = effectiveEnabled,
                    contentPadding = padding,
                    content = { content() }
                )
            }

            ButtonVariant.GHOST -> {
                TextButton(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = effectiveEnabled,
                    contentPadding = padding,
                    content = { content() }
                )
            }

            ButtonVariant.DESTRUCTIVE -> {
                Button(
                    onClick = onClick,
                    modifier = baseModifier,
                    enabled = effectiveEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = padding,
                    content = { content() }
                )
            }
        }
    }
}
