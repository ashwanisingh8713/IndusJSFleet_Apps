package com.indusjs.uicomponents.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Data class representing a state option for display.
 */
data class StateOption(
    val value: String,
    val label: String,
    val icon: String,
    val colorScheme: StateColorScheme,
    val isCurrentState: Boolean = false,
    val isValidTransition: Boolean = true
)

// ==================== State Change Dialog ====================

/**
 * Elegant State Change Dialog with Material 3 design.
 *
 * Design:
 * - Prominent current-state banner with accent color
 * - Single-selection radio style: filled circle = current, open circle = selectable
 * - Disabled rows clearly readable (no aggressive fade) with muted radio ring
 * - No left border bars, no trailing arrows — clean and minimal
 * - Animated loading overlay with "Updating…" text
 *
 * Feature-specific state option builders (getVehicleStateOptions, etc.)
 * live in their respective feature modules.
 */
@Composable
fun StateChangeDialog(
    title: String,
    currentStateLabel: String,
    stateOptions: List<StateOption>,
    onStateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Box {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // ── Header ──
                    StateDialogHeader(title = title)

                    // ── Current State Banner ──
                    CurrentStateBanner(
                        label = currentStateLabel,
                        colorScheme = stateOptions
                            .firstOrNull { it.isCurrentState }?.colorScheme
                            ?: StateColorScheme.NEUTRAL
                    )

                    // ── Section label ──
                    Text(
                        text = stringResource(Res.string.state_dialog_select_new_status),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            start = 24.dp, end = 24.dp,
                            top = 14.dp, bottom = 8.dp
                        )
                    )

                    // ── State Options List ──
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(stateOptions) { option ->
                            StateOptionRow(
                                option = option,
                                onClick = {
                                    if (option.isValidTransition && !option.isCurrentState) {
                                        onStateSelected(option.value)
                                    }
                                }
                            )
                        }
                    }

                    // ── Footer ──
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss, enabled = !isLoading) {
                            Text(
                                text = stringResource(Res.string.cancel),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ── Loading Overlay ──
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(200))
                ) {
                    LoadingOverlay()
                }
            }
        }
    }
}

// ==================== Dialog Sub-Components ====================

@Composable
private fun StateDialogHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_rotate_right),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CurrentStateBanner(
    label: String,
    colorScheme: StateColorScheme
) {
    val accentColor = getColorForScheme(colorScheme)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.current_state).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }
            Icon(
                painter = painterResource(Res.drawable.ic_check_circle),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = accentColor
            )
        }
    }
}

/**
 * Single state option row — radio-selection style.
 *
 * Visual states:
 * - **Current**: filled radio circle in accent color, bold accent label
 * - **Valid transition**: open radio ring in state color, normal text, tappable
 * - **Disabled**: faint radio ring, readable muted text, subtle background
 */
@Composable
private fun StateOptionRow(
    option: StateOption,
    onClick: () -> Unit
) {
    val stateColor = getColorForScheme(option.colorScheme)
    val isEnabled = option.isValidTransition && !option.isCurrentState
    val isDisabled = !option.isValidTransition && !option.isCurrentState

    val backgroundColor = when {
        option.isCurrentState -> stateColor.copy(alpha = 0.08f)
        isDisabled -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.45f)
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = isEnabled) { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Radio indicator ──
            RadioIndicator(
                stateColor = stateColor,
                isCurrentState = option.isCurrentState,
                isDisabled = isDisabled
            )

            Spacer(modifier = Modifier.width(14.dp))

            // ── Emoji icon in tinted circle ──
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = if (isDisabled)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        else
                            stateColor.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.icon,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Label + subtitle ──
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = when {
                        option.isCurrentState -> FontWeight.Bold
                        isDisabled -> FontWeight.Normal
                        else -> FontWeight.Medium
                    },
                    color = when {
                        option.isCurrentState -> stateColor
                        isDisabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (option.isCurrentState) {
                    Text(
                        text = stringResource(Res.string.current_state),
                        style = MaterialTheme.typography.labelSmall,
                        color = stateColor.copy(alpha = 0.75f)
                    )
                } else if (isDisabled) {
                    Text(
                        text = stringResource(Res.string.not_available_from_current_state),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Custom radio-style indicator.
 *
 * - Current: solid filled circle with white center dot
 * - Enabled: outlined ring in state color
 * - Disabled: faint outlined ring
 */
@Composable
private fun RadioIndicator(
    stateColor: Color,
    isCurrentState: Boolean,
    isDisabled: Boolean
) {
    val size = 20.dp
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isCurrentState) {
            // Filled circle with white inner dot
            Box(
                modifier = Modifier
                    .size(size)
                    .background(stateColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
            }
        } else {
            // Outlined ring
            val ringColor = if (isDisabled) stateColor.copy(alpha = 0.30f) else stateColor
            val strokeWidth = if (isDisabled) 1.5.dp else 2.dp
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = ringColor,
                    radius = (size / 2).toPx() - 1.dp.toPx(),
                    style = Stroke(width = strokeWidth.toPx())
                )
            }
        }
    }
}

@Composable
private fun LoadingOverlay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(Res.string.state_dialog_updating),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


