package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.DocumentStats
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Compact empty state for dashboard sections.
 * Shows an icon, message, and optional action button in a horizontal layout.
 * Used by AlertsSection, VehicleStatusSection, DriversStatusSection, TripsStatusSection.
 */
@Composable
internal fun SectionEmptyState(
    iconRes: DrawableResource,
    title: String,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    successStyle: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (successStyle) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (successStyle) Color(0xFF4CAF50)
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (successStyle) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurface
            )
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (actionLabel != null && onAction != null) {
            FilledTonalButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * Alerts Section - Clean design without colored background.
 */
@Composable
internal fun AlertsSection(
    alerts: List<Alert>,
    documentStats: DocumentStats?,
    alertsSummary: AlertsSummary,
    vehicleStatus: VehicleStatusSummary,
    onAlertDismiss: (String) -> Unit,
    onViewAllClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with View All button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val alertCount = if (alertsSummary.totalAlerts > 0) alertsSummary.totalAlerts else alerts.size
                    if (alertCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "$alertCount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    TextButton(onClick = onViewAllClick) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(Res.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Enhanced Alerts Summary with detailed counts
            if (alertsSummary.totalAlerts > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (alertsSummary.criticalAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.criticalAlerts,
                            label = "Critical",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (alertsSummary.warningAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.warningAlerts,
                            label = "Warning",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (alertsSummary.infoAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.infoAlerts,
                            label = "Info",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Detailed expiry breakdown
                if (alertsSummary.documentExpired > 0 || alertsSummary.documentExpiring7Days > 0 ||
                    alertsSummary.licenseExpired > 0 || alertsSummary.licenseExpiring7Days > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (alertsSummary.documentExpired > 0) {
                            ExpiryInfoChip(
                                icon = "📄",
                                count = alertsSummary.documentExpired,
                                label = "Docs Expired",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.documentExpiring7Days > 0) {
                            ExpiryInfoChip(
                                icon = "📄",
                                count = alertsSummary.documentExpiring7Days,
                                label = "Docs 7d",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (alertsSummary.licenseExpired > 0) {
                            ExpiryInfoChip(
                                icon = "📋",
                                count = alertsSummary.licenseExpired,
                                label = "License Expired",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.licenseExpiring7Days > 0) {
                            ExpiryInfoChip(
                                icon = "📋",
                                count = alertsSummary.licenseExpiring7Days,
                                label = "License 7d",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            } else if (documentStats != null && (documentStats.expiringDocuments > 0 || documentStats.expiredDocuments > 0)) {
                // Fallback to document stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (documentStats.expiredDocuments > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📄", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${documentStats.expiredDocuments} Expired",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (documentStats.expiringDocuments > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏰", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${documentStats.expiringDocuments} Expiring Soon",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Determine which alerts to display
            val displayAlerts = if (alertsSummary.alerts.isNotEmpty()) alertsSummary.alerts else alerts

            val totalDocs = documentStats?.totalDocuments ?: 0
            val expiredDocs = documentStats?.expiredDocuments ?: 0
            val expiringDocs = documentStats?.expiringDocuments ?: 0

            val hasMissingDocuments = vehicleStatus.total > 0 && totalDocs < vehicleStatus.total
            val vehiclesWithoutDocs = if (hasMissingDocuments) (vehicleStatus.total - totalDocs).coerceAtLeast(0) else 0
            val hasDocumentIssues = expiredDocs > 0 || expiringDocs > 0

            val hasNoAlerts = displayAlerts.isEmpty() &&
                             alertsSummary.totalAlerts == 0 &&
                             !hasMissingDocuments &&
                             !hasDocumentIssues

            if (hasNoAlerts) {
                SectionEmptyState(
                    iconRes = Res.drawable.ic_check,
                    title = "All clear!",
                    message = "No alerts at this time",
                    successStyle = true
                )
            } else {
                // Show missing documents warning
                if (hasMissingDocuments) {
                    AlertWarningBanner(
                        icon = "📄",
                        title = "Missing Documents",
                        message = if (vehiclesWithoutDocs > 0)
                            "$vehiclesWithoutDocs vehicle(s) need documents uploaded"
                        else
                            "Some vehicles are missing required documents",
                        isError = true
                    )
                }

                // Show expired/expiring documents warning
                if (!hasMissingDocuments && hasDocumentIssues) {
                    AlertWarningBanner(
                        icon = if (expiredDocs > 0) "⚠️" else "⏰",
                        title = if (expiredDocs > 0) "Documents Expired" else "Documents Expiring Soon",
                        message = buildString {
                            if (expiredDocs > 0) append("$expiredDocs expired")
                            if (expiredDocs > 0 && expiringDocs > 0) append(", ")
                            if (expiringDocs > 0) append("$expiringDocs expiring soon")
                        },
                        isError = expiredDocs > 0
                    )
                }

                if (displayAlerts.isNotEmpty()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                displayAlerts.take(3).forEach { alert ->
                    CleanAlertItem(
                        alert = alert,
                        onDismiss = { onAlertDismiss(alert.id) }
                    )
                }
            }
        }
    }
}

/**
 * Alert warning banner used for missing/expired/expiring documents.
 */
@Composable
private fun AlertWarningBanner(
    icon: String,
    title: String,
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isError)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isError)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Clean Alert Item - minimal design without colored backgrounds.
 */
@Composable
private fun CleanAlertItem(
    alert: Alert,
    onDismiss: () -> Unit
) {
    val alertColor = when (alert.type) {
        AlertType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
        AlertType.FUEL_LOW -> MaterialTheme.colorScheme.error
        AlertType.SPEED_VIOLATION -> MaterialTheme.colorScheme.error
        AlertType.GEOFENCE_VIOLATION -> MaterialTheme.colorScheme.secondary
        AlertType.DRIVER_BEHAVIOR -> MaterialTheme.colorScheme.secondary
        AlertType.SYSTEM -> MaterialTheme.colorScheme.primary
        AlertType.DOCUMENT_EXPIRY -> MaterialTheme.colorScheme.error
        AlertType.LICENSE_EXPIRY -> MaterialTheme.colorScheme.error
    }

    val alertIcon = when (alert.type) {
        AlertType.DOCUMENT_EXPIRY -> "📄"
        AlertType.LICENSE_EXPIRY -> "📋"
        AlertType.MAINTENANCE -> "🔧"
        AlertType.FUEL_LOW -> "⛽"
        else -> "⚠️"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = alertIcon,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = alertColor
                )
                alert.daysUntilExpiry?.let { days ->
                    val badgeColor = when {
                        days < 0 -> MaterialTheme.colorScheme.error
                        days <= 7 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val badgeText = when {
                        days < 0 -> "${-days}d overdue"
                        days == 0 -> "Today"
                        else -> "${days}d left"
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (alert.type == AlertType.DOCUMENT_EXPIRY && alert.vehicleRegistrationNumber != null) {
                Text(
                    text = "🚛 ${alert.vehicleRegistrationNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (alert.type == AlertType.LICENSE_EXPIRY && alert.driverName != null) {
                Text(
                    text = "👤 ${alert.driverName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Alert Count Badge - Shows alert count by priority.
 */
@Composable
private fun AlertCountBadge(
    count: Int,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/**
 * Expiry Info Chip - Shows expiry info with icon.
 */
@Composable
private fun ExpiryInfoChip(
    icon: String,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.labelSmall)
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

