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
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.DocumentStats
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
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
                iconRes = Res.drawable.ic_warning,
                accent = MaterialTheme.colorScheme.error,
                actionLabel = stringResource(Res.string.action_view_all),
                onActionClick = onViewAllClick
            )

            // Enhanced Alerts Summary with detailed counts
            if (alertsSummary.totalAlerts > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
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
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                    ) {
                        if (alertsSummary.documentExpired > 0) {
                            ExpiryInfoChip(
                                iconRes = Res.drawable.ic_edit,
                                count = alertsSummary.documentExpired,
                                label = stringResource(Res.string.dashboard_label_docs_expired),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.documentExpiring7Days > 0) {
                            ExpiryInfoChip(
                                iconRes = Res.drawable.ic_edit,
                                count = alertsSummary.documentExpiring7Days,
                                label = stringResource(Res.string.dashboard_label_docs_7d),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        if (alertsSummary.licenseExpired > 0) {
                            ExpiryInfoChip(
                                iconRes = Res.drawable.ic_edit,
                                count = alertsSummary.licenseExpired,
                                label = stringResource(Res.string.dashboard_label_license_expired),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.licenseExpiring7Days > 0) {
                            ExpiryInfoChip(
                                iconRes = Res.drawable.ic_edit,
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
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                ) {
                    if (documentStats.expiredDocuments > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
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
                            Icon(
                                painter = painterResource(Res.drawable.ic_time),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
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

            val expiredDocs = documentStats?.expiredDocuments ?: 0
            val expiringDocs = documentStats?.expiringDocuments ?: 0

            val hasDocumentIssues = expiredDocs > 0 || expiringDocs > 0

            val hasNoAlerts = displayAlerts.isEmpty() &&
                             alertsSummary.totalAlerts == 0 &&
                             !hasDocumentIssues

            if (hasNoAlerts) {
                SectionEmptyState(
                    iconRes = Res.drawable.ic_check,
                    title = stringResource(Res.string.alerts_all_clear_title),
                    message = stringResource(Res.string.alerts_all_clear_message),
                    successStyle = true
                )
            } else {
                // "Missing documents" now arrives as a real backend alert (type MISSING_DOCUMENTS)
                // inside displayAlerts, so it is rendered AND counted with the alerts below — no more
                // client-side vehicles-vs-docs derivation. The expired/expiring-document banner is the
                // one remaining DERIVED nudge (from documentStats), separate from the counted alerts.
                if (hasDocumentIssues) {
                    Text(
                        text = stringResource(Res.string.alerts_other_reminders),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = FleetTokens.Spacing.S)
                    )
                }

                // Show expired/expiring documents warning
                if (hasDocumentIssues) {
                    AlertWarningBanner(
                        iconRes = if (expiredDocs > 0) Res.drawable.ic_warning else Res.drawable.ic_time,
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
    iconRes: DrawableResource,
    title: String,
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FleetTokens.Radius.M),
        color = if (isError)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(FleetTokens.Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(FleetTokens.IconSize.M)
            )
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
        AlertType.MISSING_DOCUMENTS -> MaterialTheme.colorScheme.error
    }

    val alertIcon = when (alert.type) {
        AlertType.DOCUMENT_EXPIRY -> Res.drawable.ic_edit
        AlertType.LICENSE_EXPIRY -> Res.drawable.ic_edit
        AlertType.MISSING_DOCUMENTS -> Res.drawable.ic_edit
        AlertType.MAINTENANCE -> Res.drawable.ic_settings
        AlertType.FUEL_LOW -> Res.drawable.ic_fuel
        else -> Res.drawable.ic_warning
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FleetTokens.Spacing.XS),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(alertIcon),
            contentDescription = null,
            tint = alertColor,
            modifier = Modifier
                .padding(top = FleetTokens.Spacing.XXS)
                .size(FleetTokens.IconSize.S)
        )
        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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
                        shape = RoundedCornerShape(FleetTokens.Radius.M),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
                        )
                    }
                }
            }

            if ((alert.type == AlertType.DOCUMENT_EXPIRY || alert.type == AlertType.MISSING_DOCUMENTS) && alert.vehicleRegistrationNumber != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_truck),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Text(
                        text = alert.vehicleRegistrationNumber ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (alert.type == AlertType.LICENSE_EXPIRY && alert.driverName != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_profile),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Text(
                        text = alert.driverName ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(FleetTokens.IconSize.Default)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = stringResource(Res.string.cd_dismiss),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(FleetTokens.IconSize.S)
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
        shape = RoundedCornerShape(FleetTokens.Radius.M),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
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
    iconRes: DrawableResource,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(FleetTokens.IconSize.S)
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
