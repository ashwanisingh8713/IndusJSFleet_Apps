package com.indusjs.uicomponents.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Data class representing a state option for display.
 *
 * [iconRes] is a design-system vector icon (resolved via [stateIconRes] from the state value) —
 * NOT an emoji. Feature builders set it with `iconRes = stateIconRes(value)`.
 */
data class StateOption(
    val value: String,
    val label: String,
    val iconRes: DrawableResource,
    val colorScheme: StateColorScheme,
    val isCurrentState: Boolean = false,
    val isValidTransition: Boolean = true
)

/**
 * Maps a backend state VALUE (driver / vehicle / trip) to a design-system vector icon.
 * Centralized so all three status dialogs render consistent, emoji-free iconography.
 * Unknown values fall back to a neutral info icon.
 */
fun stateIconRes(value: String): DrawableResource = when (value.lowercase()) {
    "active" -> Res.drawable.ic_check_circle
    "inactive" -> Res.drawable.ic_power
    "on_trip", "on_route", "in_progress", "assigned" -> Res.drawable.ic_car
    "on_leave" -> Res.drawable.ic_time
    "suspended" -> Res.drawable.ic_pause
    "maintenance", "in_maintenance" -> Res.drawable.ic_wrench
    "damaged" -> Res.drawable.ic_warning
    "decommissioned", "out_of_service", "retired" -> Res.drawable.ic_block
    "planned" -> Res.drawable.ic_trip
    "completed" -> Res.drawable.ic_flag
    "cancelled" -> Res.drawable.ic_close
    "failed" -> Res.drawable.ic_warning
    "delayed" -> Res.drawable.ic_time
    else -> Res.drawable.ic_info
}

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
                    val currentOption = stateOptions.firstOrNull { it.isCurrentState }
                    CurrentStateBanner(
                        label = currentStateLabel,
                        colorScheme = currentOption?.colorScheme ?: StateColorScheme.NEUTRAL,
                        iconRes = currentOption?.iconRes
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
    colorScheme: StateColorScheme,
    iconRes: DrawableResource? = null
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
            // State icon in a tinted circle (falls back to a plain dot if no icon).
            if (iconRes != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
            }
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
 * Single state option row — a clean Material radio list item.
 *
 * - **Current**: tinted background + state-coloured outline, filled radio, bold accent label.
 * - **Selectable**: transparent, state-coloured leading icon, open radio, tappable.
 * - **Disabled**: faded content (no heavy box), inert radio, "not available" subtitle.
 *
 * One selection indicator only (the trailing [RadioButton]); the leading icon is decorative/identity,
 * in a consistent subtle tinted circle so no row looks pre-selected.
 */
@Composable
private fun StateOptionRow(
    option: StateOption,
    onClick: () -> Unit
) {
    val stateColor = getColorForScheme(option.colorScheme)
    val isCurrent = option.isCurrentState
    val isEnabled = option.isValidTransition && !isCurrent
    val isDisabled = !option.isValidTransition && !isCurrent
    val contentAlpha = if (isDisabled) 0.45f else 1f

    val shape = RoundedCornerShape(FleetTokens.Radius.L)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (isCurrent) {
                    Modifier
                        .background(stateColor.copy(alpha = 0.10f), shape)
                        .border(FleetTokens.Border.Default, stateColor.copy(alpha = 0.5f), shape)
                } else {
                    Modifier
                }
            )
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Leading: state icon in a consistent, subtle tinted circle (identity, not selection) ──
        Box(
            modifier = Modifier
                .size(FleetTokens.IconSize.XL)
                .background(stateColor.copy(alpha = if (isDisabled) 0.08f else 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(option.iconRes),
                contentDescription = null,
                tint = stateColor.copy(alpha = contentAlpha),
                modifier = Modifier.size(FleetTokens.IconSize.M)
            )
        }

        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))

        // ── Label + subtitle ──
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                color = (if (isCurrent) stateColor else MaterialTheme.colorScheme.onSurface)
                    .copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitle = when {
                isCurrent -> stringResource(Res.string.current_state)
                isDisabled -> stringResource(Res.string.not_available_from_current_state)
                else -> null
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) {
                        stateColor.copy(alpha = 0.75f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

        // ── Trailing: the single selection indicator (Material radio, tinted to the state) ──
        RadioButton(
            selected = isCurrent,
            onClick = null,
            enabled = isEnabled || isCurrent,
            colors = RadioButtonDefaults.colors(
                selectedColor = stateColor,
                unselectedColor = stateColor.copy(alpha = 0.7f),
                disabledSelectedColor = stateColor.copy(alpha = 0.5f),
                disabledUnselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )
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


