package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import com.indusjs.uicomponents.theme.rememberThemeToggle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Navigation Drawer Content with Quick Actions menu items.
 */
@Composable
internal fun NavigationDrawerContent(
    userName: String,
    userRole: String,
    hasFinancialAccess: Boolean,
    onNavigateToVehicles: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToVehicleFinance: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        // Enhanced Profile Header with gradient background
        ProfileDrawerHeader(
            userName = userName,
            userRole = userRole,
            onClick = onNavigateToProfile
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

        // Menu Items
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_vehicle),
                    contentDescription = stringResource(Res.string.nav_vehicles),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_vehicles)) },
            selected = false,
            onClick = onNavigateToVehicles,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_driver),
                    contentDescription = stringResource(Res.string.nav_drivers),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_drivers)) },
            selected = false,
            onClick = onNavigateToDrivers,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_trip),
                    contentDescription = stringResource(Res.string.nav_trips),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_trips)) },
            selected = false,
            onClick = onNavigateToTrips,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_profile),
                    contentDescription = stringResource(Res.string.nav_customers),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_customers)) },
            selected = false,
            onClick = onNavigateToCustomers,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_cost),
                    contentDescription = stringResource(Res.string.nav_payments),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_payments)) },
            selected = false,
            onClick = onNavigateToPayments,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_map),
                    contentDescription = stringResource(Res.string.nav_live_map),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_live_map)) },
            selected = false,
            onClick = onNavigateToMaps,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = FleetTokens.Spacing.S, horizontal = FleetTokens.Spacing.L))

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_team),
                    contentDescription = stringResource(Res.string.nav_team_members),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_team_members)) },
            selected = false,
            onClick = onNavigateToTeam,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        // Reports & P/L - Only visible to Owner and General Manager
        if (hasFinancialAccess) {
            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_cost),
                        contentDescription = stringResource(Res.string.nav_vehicle_finance),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.Default)
                    )
                },
                label = { Text(stringResource(Res.string.nav_vehicle_finance)) },
                selected = false,
                onClick = onNavigateToVehicleFinance,
                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
            )

            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_dashboard),
                        contentDescription = stringResource(Res.string.nav_reports),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.Default)
                    )
                },
                label = { Text(stringResource(Res.string.nav_reports)) },
                selected = false,
                onClick = onNavigateToReports,
                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
            )
        }

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = stringResource(Res.string.nav_profile),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(stringResource(Res.string.nav_profile)) },
            selected = false,
            onClick = onNavigateToProfile,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = FleetTokens.Spacing.S, horizontal = FleetTokens.Spacing.L))

        // Theme Toggle
        val isDarkTheme = isAppInDarkTheme()
        val toggleTheme = rememberThemeToggle()
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(
                        if (isDarkTheme) Res.drawable.ic_sun else Res.drawable.ic_moon
                    ),
                    contentDescription = if (isDarkTheme) stringResource(Res.string.cd_light_mode) else stringResource(Res.string.cd_dark_mode),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            },
            label = { Text(if (isDarkTheme) stringResource(Res.string.cd_light_mode) else stringResource(Res.string.cd_dark_mode)) },
            selected = false,
            onClick = toggleTheme,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Text(
            text = stringResource(Res.string.app_version_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(FleetTokens.Spacing.L)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * Enhanced profile header for the navigation drawer.
 * Features a gradient background, prominent avatar with status indicator,
 * styled role badge and a "View Profile" affordance.
 */
@Composable
private fun ProfileDrawerHeader(
    userName: String,
    userRole: String,
    onClick: () -> Unit
) {
    val initials = remember(userName) { com.indusjs.uicomponents.components.initialsOf(userName) }
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val statusColor = FleetStatusColors.SuccessGreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(top = 52.dp, bottom = FleetTokens.Spacing.XL, start = FleetTokens.Spacing.XL, end = FleetTokens.Spacing.XL)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with ring + status dot
                Box(contentAlignment = Alignment.Center) {
                    // Outer soft ring
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(primary.copy(alpha = 0.10f), CircleShape)
                    )
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .border(FleetTokens.Spacing.XXS, primary.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Online status dot (bottom-end)
                    Box(
                        modifier = Modifier.size(68.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(FleetTokens.Spacing.L)
                                .background(statusColor, CircleShape)
                                .border(FleetTokens.Spacing.XXS, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.profile_greeting_prefix_default),
                        style = MaterialTheme.typography.labelMedium,
                        color = onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXS))
                    Text(
                        text = userName.ifBlank { "—" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        maxLines = 1
                    )
                }

                // Chevron affordance
                Surface(
                    shape = CircleShape,
                    color = primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(FleetTokens.IconSize.L)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_chevron_right),
                            contentDescription = stringResource(Res.string.profile_title),
                            tint = primary,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    }
                }
            }

            if (userRole.isNotBlank()) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.XXL),
                        color = primary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_profile),
                                contentDescription = null,
                                tint = primary,
                                modifier = Modifier.size(FleetTokens.IconSize.XS)
                            )
                            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    // Active status pill
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.XXL),
                        color = statusColor.copy(alpha = 0.14f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(FleetTokens.Spacing.S)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                            Text(
                                text = stringResource(Res.string.profile_status_active),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            Text(
                text = stringResource(Res.string.profile_tap_to_view),
                style = MaterialTheme.typography.labelSmall,
                color = onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    }
}


