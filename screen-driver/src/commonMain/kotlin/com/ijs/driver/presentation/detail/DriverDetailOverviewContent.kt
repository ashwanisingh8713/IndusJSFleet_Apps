package com.ijs.driver.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.presentation.driverLicenseTypeShort
import com.ijs.driver.presentation.driverStatusLabel
import com.ijs.team.presentation.toCaretakerInfo
import com.indusjs.uicomponents.components.CaretakerInfoCard
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.indusjs.uicomponents.components.FleetStatusBadge
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DriverOverviewContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel,
    driverStatusLabels: Map<String, String>,
    onStatusClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DriverHeader(
                driver = state.driver!!,
                driverStatusLabels = driverStatusLabels,
                onStatusClick = onStatusClick
            )
        }

        item { ContactSection(driver = state.driver!!) }

        item { LicenseSection(driver = state.driver!!) }

        item { PersonalSection(driver = state.driver!!) }

        item { MetadataSection(driver = state.driver!!) }

        // Caretaker Assignment Section
        item {
            CaretakerInfoCard(
                caretaker = state.selectedCaretaker?.toCaretakerInfo(),
                onChangeCaretaker = if (state.isEditMode) {
                    { viewModel.sendIntent(DriverDetailContract.Intent.LoadCaretakers) }
                } else null
            )
        }

        // Delete button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.sendIntent(DriverDetailContract.Intent.DeleteDriver) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(Res.string.driver_overview_delete_with_icon))
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Costs tab content displaying driver earnings, deductions, and costs.
 * Features:
 * - Enhanced Hero card with animated metrics
 * - Collapsible cost groups (Salary, Incentives, Deductions, Other)
 * - Cost breakdown by type with trip links
 * - PDF export functionality
 */

@Composable
internal fun DriverHeader(
    driver: Driver,
    driverStatusLabels: Map<String, String>,
    onStatusClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${driver.firstName.firstOrNull() ?: ""}${driver.lastName.firstOrNull() ?: ""}".uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = driver.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mobile
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📱",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = driver.mobile,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge - Clickable to change status
            Surface(
                onClick = onStatusClick,
                shape = RoundedCornerShape(16.dp),
                color = getStatusColor(driver.status).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FleetStatusBadge(
                        status = driverStatusLabel(driver.status, driverStatusLabels),
                        color = getStatusColor(driver.status)
                    )
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(driver.status)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickStatItem(
                    icon = "⭐",
                    value = "${driver.rating}",
                    label = stringResource(Res.string.driver_overview_rating)
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🛣️",
                    value = "${driver.totalTrips}",
                    label = stringResource(Res.string.driver_overview_trips)
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🪪",
                    value = driverLicenseTypeShort(driver.licenseType),
                    label = stringResource(Res.string.driver_overview_license)
                )
            }
        }
    }
}


@Composable
internal fun QuickStatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
internal fun ContactSection(driver: Driver) {
    SectionCard(title = stringResource(Res.string.driver_section_contact)) {
        // Mobile with call icon
        ClickablePhoneRow(
            phoneNumber = driver.mobile,
            label = stringResource(Res.string.driver_label_mobile),
            icon = "📱"
        )
        if (driver.email.isNotBlank()) {
            InfoRow(label = stringResource(Res.string.driver_overview_email), value = driver.email)
        }
        // Emergency Contact with call icon
        driver.emergencyContact?.let {
            ClickablePhoneRow(
                phoneNumber = it,
                label = stringResource(Res.string.driver_overview_emergency_contact),
                icon = "🆘"
            )
        }
        driver.address?.let { InfoRow(label = stringResource(Res.string.driver_overview_address), value = it) }
    }
}


@Composable
internal fun LicenseSection(driver: Driver) {
    SectionCard(title = stringResource(Res.string.driver_section_license)) {
        InfoRow(label = stringResource(Res.string.driver_overview_license_number), value = driver.licenseNumber)
        InfoRow(label = stringResource(Res.string.driver_overview_license_type), value = driverLicenseTypeShort(driver.licenseType))
        if (driver.licenseExpiry > 0) {
            InfoRow(
                label = stringResource(Res.string.driver_overview_expiry_date),
                value = formatDate(driver.licenseExpiry, stringResource(Res.string.not_applicable_short))
            )
        }
    }
}


@Composable
internal fun PersonalSection(driver: Driver) {
    val hasPersonalInfo = driver.dateOfBirth != null || driver.bloodGroup != null || driver.joiningDate != null

    if (hasPersonalInfo) {
        SectionCard(title = stringResource(Res.string.driver_section_personal)) {
            val na = stringResource(Res.string.not_applicable_short)
            driver.dateOfBirth?.let { InfoRow(label = stringResource(Res.string.driver_overview_dob), value = formatDate(it, na)) }
            driver.bloodGroup?.let { InfoRow(label = stringResource(Res.string.driver_overview_blood_group), value = it) }
            driver.joiningDate?.let { InfoRow(label = stringResource(Res.string.driver_overview_joining_date), value = formatDate(it, na)) }
        }
    }
}


@Composable
internal fun MetadataSection(driver: Driver) {
    val na = stringResource(Res.string.not_applicable_short)
    SectionCard(title = stringResource(Res.string.driver_section_additional)) {
        InfoRow(label = stringResource(Res.string.driver_overview_driver_id), value = "#${driver.id}")
        driver.owner?.let { owner ->
            val ownerName = "${owner.firstName ?: ""} ${owner.lastName ?: ""}".trim()
            if (ownerName.isNotBlank()) {
                InfoRow(label = stringResource(Res.string.driver_overview_added_by), value = ownerName)
            }
        }
        driver.createdAt?.let { InfoRow(label = stringResource(Res.string.driver_overview_created_on), value = formatDate(it, na)) }
        driver.updatedAt?.let { InfoRow(label = stringResource(Res.string.driver_overview_last_updated), value = formatDate(it, na)) }
    }
}


@Composable
internal fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}


@Composable
internal fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)

@Composable
internal fun getStatusColor(status: DriverStatus): androidx.compose.ui.graphics.Color {
    val colorScheme = DriverStatus.getColorScheme(status)
    return com.indusjs.uicomponents.components.stateColorSchemeToChipColor(colorScheme)
}


internal fun formatDate(timestamp: Long, naLabel: String): String {
    if (timestamp <= 0) return naLabel
    return try {
        val days = timestamp / (24 * 60 * 60 * 1000)
        val years = (days / 365.25).toInt() + 1970
        val remainingDays = (days % 365.25).toInt()
        val months = (remainingDays / 30) + 1
        val dayOfMonth = (remainingDays % 30) + 1
        val monthStr = months.coerceIn(1, 12).toString().padStart(2, '0')
        val dayStr = dayOfMonth.coerceIn(1, 28).toString().padStart(2, '0')
        "$dayStr-$monthStr-$years" // DD-MM-YYYY format
    } catch (_: Exception) {
        naLabel
    }
}

