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
import androidx.compose.ui.text.style.TextOverflow
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
 * The breakpoint is measured per button from its own available width (via
 * [BoxWithConstraints]): on Compact the button fills its slot, on Medium / Expanded
 * it wraps its content. The caller [modifier] — including a
 * [androidx.compose.foundation.layout.RowScope.weight] for side-by-side buttons —
 * is applied to the [BoxWithConstraints] root, i.e. the Row's direct child, so the
 * weight is honoured. (Applying the caller modifier to the inner button instead
 * would swallow a RowScope.weight and collapse the second of two side-by-side
 * buttons to zero width — e.g. the OTP screen's Resend / Verify row.)
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

    // Vertical content padding scales with size. The button height is FIXED (.height below), so a
    // large constant vertical padding (e.g. 12dp on both sides of a 36dp SMALL button) over-constrains
    // the content area and CLIPS the label. Scaling it keeps ~consistent content room per size and
    // never lets the text get cut off. (Outer height is unchanged — this only affects inner room.)
    val verticalPadding = when (size) {
        ButtonSize.SMALL -> FleetTokens.Spacing.XS
        ButtonSize.MEDIUM -> FleetTokens.Spacing.S
        ButtonSize.LARGE -> FleetTokens.Spacing.M
    }
    val padding = PaddingValues(
        horizontal = FleetTokens.Spacing.XL,
        vertical = verticalPadding
    )

    val semanticsModifier = if (accessibilityLabel != null) {
        Modifier.semantics { contentDescription = accessibilityLabel }
    } else {
        Modifier
    }

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
            Text(
                text = text,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // The caller [modifier] (which may carry a RowScope.weight) goes on the
    // BoxWithConstraints root — the Row's direct child — so weight / size are
    // honoured. The breakpoint is then measured from this (possibly weighted) slot:
    // a narrow slot is Compact and the inner button fills it; a wide standalone slot
    // is Medium / Expanded and the button wraps its content.
    BoxWithConstraints(modifier = modifier) {
        val widthModifier = when (rememberFleetBreakpoint()) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier
        }

        val baseModifier = widthModifier
            .height(heightDp)
            .defaultMinSize(minHeight = FleetTokens.Height.MinTouchTarget)
            .then(semanticsModifier)

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
