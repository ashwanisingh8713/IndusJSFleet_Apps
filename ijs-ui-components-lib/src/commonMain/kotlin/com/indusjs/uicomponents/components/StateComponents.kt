package com.indusjs.uicomponents.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme

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

/**
 * Reusable State Change Dialog for updating entity states.
 * Displays available state transitions with validation.
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
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔄", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Current: $currentStateLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stateOptions) { option ->
                        StateOptionItem(
                            option = option,
                            onClick = {
                                if (option.isValidTransition && !option.isCurrentState) {
                                    onStateSelected(option.value)
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StateOptionItem(
    option: StateOption,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        option.isCurrentState -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        !option.isValidTransition -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> Color.Transparent
    }

    val contentColor = when {
        option.isCurrentState -> MaterialTheme.colorScheme.primary
        !option.isValidTransition -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val stateColor = getColorForScheme(option.colorScheme)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = option.isValidTransition && !option.isCurrentState) { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.icon,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (option.isCurrentState) FontWeight.Bold else FontWeight.Normal,
                    color = contentColor
                )
                if (option.isCurrentState) {
                    Text(
                        text = "Current state",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (!option.isValidTransition) {
                    Text(
                        text = "Not available from current state",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(stateColor, CircleShape)
            )
        }
    }
}

/**
 * Compact state chip with change button.
 */
@Composable
fun StateChipWithAction(
    stateLabel: String,
    stateIcon: String,
    colorScheme: StateColorScheme,
    onChangeClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val stateColor = getColorForScheme(colorScheme)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = stateColor.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stateIcon, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = stateColor
                )
            }
        }

        if (enabled) {
            TextButton(
                onClick = onChangeClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * State action button for quick state changes.
 */
@Composable
fun StateActionButton(
    label: String,
    icon: String,
    colorScheme: StateColorScheme,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = getColorForScheme(colorScheme)

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color,
            disabledContentColor = color.copy(alpha = 0.5f)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (enabled) color else color.copy(alpha = 0.5f))
        )
    ) {
        Text(icon, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

// ==================== Helper Functions ====================

@Composable
fun getColorForScheme(colorScheme: StateColorScheme): Color {
    return when (colorScheme) {
        StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        StateColorScheme.WARNING -> Color(0xFFF59E0B)
        StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        StateColorScheme.INFO -> Color(0xFF3B82F6)
        StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
}

