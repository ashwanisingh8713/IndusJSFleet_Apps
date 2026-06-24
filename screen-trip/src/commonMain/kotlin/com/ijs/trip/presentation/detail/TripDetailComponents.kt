package com.ijs.trip.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.ijs.trip.domain.entity.TripStatus

/**
 * Reusable section card with icon header used across trip detail sections.
 */
@Composable
internal fun EnhancedSectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetTitledSectionCard(title = title, emoji = icon, content = content)
}

/**
 * Reusable info row with icon, label, and value.
 */
@Composable
internal fun EnhancedInfoRow(
    icon: String,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.4f)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

/**
 * Enhanced status badge for trip status display.
 */
@Composable
internal fun EnhancedStatusBadge(
    status: TripStatus,
    onClick: () -> Unit,
    stateLabels: Map<String, String> = emptyMap()
) {
    val colorScheme = TripStatus.getColorScheme(status)
    val baseColor = com.indusjs.uicomponents.components.stateColorSchemeToChipColor(colorScheme)
    val containerColor = baseColor.copy(alpha = 0.15f)
    val iconText = TripStatus.getIcon(status)
    val apiValue = TripStatus.toApiString(status)
    val text = stateLabels[apiValue] ?: TripStatus.getDisplayLabel(status)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = iconText, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = baseColor
            )
            if (status == TripStatus.PLANNED) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = baseColor
                )
            }
        }
    }
}


/**
 * Dropdown field for edit mode selections.
 */
@Composable
internal fun EditDropdownField(
    label: String,
    selectedText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    error: String?,
    icon: String
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() },
            shape = RoundedCornerShape(12.dp),
            color = if (error != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(if (isExpanded) "▲" else "▼", style = MaterialTheme.typography.labelMedium)
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Returns emoji icon for trip priority level.
 */
internal fun getPriorityIcon(priority: String?): String {
    return when (priority?.lowercase()) {
        "high" -> "🔴"
        "medium" -> "🟡"
        "low" -> "🟢"
        else -> "⚪"
    }
}

