package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        // Header with user info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            // User Info - clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToProfile)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (userRole.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = stringResource(Res.string.profile_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Menu Items
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_vehicle),
                    contentDescription = stringResource(Res.string.nav_vehicles),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_vehicles)) },
            selected = false,
            onClick = onNavigateToVehicles,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_driver),
                    contentDescription = stringResource(Res.string.nav_drivers),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_drivers)) },
            selected = false,
            onClick = onNavigateToDrivers,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_trip),
                    contentDescription = stringResource(Res.string.nav_trips),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_trips)) },
            selected = false,
            onClick = onNavigateToTrips,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Text(
                    text = "👤",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_customers)) },
            selected = false,
            onClick = onNavigateToCustomers,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Text(
                    text = "💰",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_payments)) },
            selected = false,
            onClick = onNavigateToPayments,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_map),
                    contentDescription = stringResource(Res.string.nav_live_map),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_live_map)) },
            selected = false,
            onClick = onNavigateToMaps,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_team),
                    contentDescription = stringResource(Res.string.nav_team_members),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_team_members)) },
            selected = false,
            onClick = onNavigateToTeam,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Reports & P/L - Only visible to Owner and General Manager
        if (hasFinancialAccess) {
            NavigationDrawerItem(
                icon = {
                    Text(
                        text = "🏦",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(Res.string.nav_vehicle_finance)) },
                selected = false,
                onClick = onNavigateToVehicleFinance,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                icon = {
                    Text(
                        text = "📊",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(stringResource(Res.string.nav_reports)) },
                selected = false,
                onClick = onNavigateToReports,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = stringResource(Res.string.nav_profile),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(Res.string.nav_profile)) },
            selected = false,
            onClick = onNavigateToProfile,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

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
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(if (isDarkTheme) stringResource(Res.string.cd_light_mode) else stringResource(Res.string.cd_dark_mode)) },
            selected = false,
            onClick = toggleTheme,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Text(
            text = stringResource(Res.string.app_version_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
