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
import com.ijs.driver.domain.entity.LicenseType
import com.ijs.team.presentation.toCaretakerInfo
import com.indusjs.uicomponents.components.CaretakerInfoCard
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.indusjs.uicomponents.components.FleetStatusBadge
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DriverOverviewContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel,
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
                Text("🗑️ Delete Driver")
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
                        status = getStatusDisplayName(driver.status),
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
                    label = "Rating"
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🛣️",
                    value = "${driver.totalTrips}",
                    label = "Trips"
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🪪",
                    value = driver.licenseType.name,
                    label = "License"
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
    SectionCard(title = "📞 Contact Information") {
        // Mobile with call icon
        ClickablePhoneRow(
            phoneNumber = driver.mobile,
            label = "Mobile",
            icon = "📱"
        )
        if (driver.email.isNotBlank()) {
            InfoRow(label = "Email", value = driver.email)
        }
        // Emergency Contact with call icon
        driver.emergencyContact?.let {
            ClickablePhoneRow(
                phoneNumber = it,
                label = "Emergency Contact",
                icon = "🆘"
            )
        }
        driver.address?.let { InfoRow(label = "Address", value = it) }
    }
}


@Composable
internal fun LicenseSection(driver: Driver) {
    SectionCard(title = "🪪 License Details") {
        InfoRow(label = "License Number", value = driver.licenseNumber)
        InfoRow(label = "License Type", value = getLicenseTypeLabel(driver.licenseType))
        if (driver.licenseExpiry > 0) {
            InfoRow(label = "Expiry Date", value = formatDate(driver.licenseExpiry))
        }
    }
}


@Composable
internal fun PersonalSection(driver: Driver) {
    val hasPersonalInfo = driver.dateOfBirth != null || driver.bloodGroup != null || driver.joiningDate != null

    if (hasPersonalInfo) {
        SectionCard(title = "👤 Personal Details") {
            driver.dateOfBirth?.let { InfoRow(label = "Date of Birth", value = formatDate(it)) }
            driver.bloodGroup?.let { InfoRow(label = "Blood Group", value = it) }
            driver.joiningDate?.let { InfoRow(label = "Joining Date", value = formatDate(it)) }
        }
    }
}


@Composable
internal fun MetadataSection(driver: Driver) {
    SectionCard(title = "ℹ️ Additional Info") {
        InfoRow(label = "Driver ID", value = "#${driver.id}")
        driver.owner?.let { owner ->
            val ownerName = "${owner.firstName ?: ""} ${owner.lastName ?: ""}".trim()
            if (ownerName.isNotBlank()) {
                InfoRow(label = "Added by", value = ownerName)
            }
        }
        driver.createdAt?.let { InfoRow(label = "Created on", value = formatDate(it)) }
        driver.updatedAt?.let { InfoRow(label = "Last Updated", value = formatDate(it)) }
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
    return com.indusjs.uicomponents.components.stateColorSchemeToColor(colorScheme)
}


internal fun getStatusDisplayName(status: DriverStatus): String =
    DriverStatus.getDisplayLabel(status)


internal fun getLicenseTypeLabel(type: LicenseType): String = when (type) {
    LicenseType.LMV -> "LMV"
    LicenseType.HMV -> "HMV"
    LicenseType.MCWG -> "MCWG"
    LicenseType.MCWOG -> "MCWOG"
}


internal fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return "N/A"
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
        "N/A"
    }
}

