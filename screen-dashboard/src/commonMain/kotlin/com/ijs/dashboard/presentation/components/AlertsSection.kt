package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.DocumentStats
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


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
    DashboardSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            val alertCount = if (alertsSummary.totalAlerts > 0) alertsSummary.totalAlerts else alerts.size
            DashboardSectionHeader(
                title = stringResource(Res.string.dashboard_alerts) + if (alertCount > 0) " ($alertCount)" else "",
                emoji = "⚠️",
                accent = MaterialTheme.colorScheme.error,
                actionLabel = stringResource(Res.string.action_view_all),
                onActionClick = onViewAllClick
            )

            // Enhanced Alerts Summary with detailed counts
            if (alertsSummary.totalAlerts > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (alertsSummary.criticalAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.criticalAlerts,
                            label = stringResource(Res.string.dashboard_label_critical),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (alertsSummary.warningAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.warningAlerts,
                            label = stringResource(Res.string.dashboard_label_warning),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (alertsSummary.infoAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.infoAlerts,
                            label = stringResource(Res.string.dashboard_label_info),
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
                                label = stringResource(Res.string.dashboard_label_docs_expired),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.documentExpiring7Days > 0) {
                            ExpiryInfoChip(
                                icon = "📄",
                                count = alertsSummary.documentExpiring7Days,
                                label = stringResource(Res.string.dashboard_label_docs_7d),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (alertsSummary.licenseExpired > 0) {
                            ExpiryInfoChip(
                                icon = "📋",
                                count = alertsSummary.licenseExpired,
                                label = stringResource(Res.string.dashboard_label_license_expired),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.licenseExpiring7Days > 0) {
                            ExpiryInfoChip(
                                icon = "📋",
                                count = alertsSummary.licenseExpiring7Days,
                                label = stringResource(Res.string.dashboard_label_license_7d),
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
                                text = stringResource(Res.string.alerts_count_expired, documentStats.expiredDocuments),
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
                                text = stringResource(Res.string.alerts_count_expiring_soon, documentStats.expiringDocuments),
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
                    title = stringResource(Res.string.alerts_all_clear_title),
                    message = stringResource(Res.string.alerts_all_clear_message),
                    successStyle = true
                )
            } else {
                // The missing/expired-document banners are DERIVED nudges, separate from the
                // counted alerts shown in the header badge (and the "View All" list). Label them
                // so the badge count doesn't look wrong vs the number of cards rendered.
                if (hasMissingDocuments || hasDocumentIssues) {
                    Text(
                        text = stringResource(Res.string.alerts_other_reminders),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                // Show missing documents warning
                if (hasMissingDocuments) {
                    AlertWarningBanner(
                        icon = "📄",
                        title = stringResource(Res.string.alerts_missing_documents),
                        message = if (vehiclesWithoutDocs > 0)
                            stringResource(Res.string.alerts_vehicles_need_docs, vehiclesWithoutDocs)
                        else
                            stringResource(Res.string.alerts_vehicles_missing_docs),
                        isError = true
                    )
                }

                // Show expired/expiring documents warning
                if (!hasMissingDocuments && hasDocumentIssues) {
                    AlertWarningBanner(
                        icon = if (expiredDocs > 0) "⚠️" else "⏰",
                        title = if (expiredDocs > 0) stringResource(Res.string.alerts_documents_expired) else stringResource(Res.string.alerts_documents_expiring),
                        message = buildString {
                            if (expiredDocs > 0) append(stringResource(Res.string.alerts_expired_count, expiredDocs))
                            if (expiredDocs > 0 && expiringDocs > 0) append(", ")
                            if (expiringDocs > 0) append(stringResource(Res.string.alerts_expiring_count, expiringDocs))
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
                        days < 0 -> stringResource(Res.string.alerts_days_overdue, -days)
                        days == 0 -> stringResource(Res.string.alerts_today)
                        else -> stringResource(Res.string.alerts_days_left, days)
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
                contentDescription = stringResource(Res.string.cd_dismiss),
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
