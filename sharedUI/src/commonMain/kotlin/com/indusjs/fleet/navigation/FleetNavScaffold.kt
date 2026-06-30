package com.indusjs.fleet.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import com.indusjs.fleet.core.permission.PermissionChecker
import com.indusjs.fleet.core.permission.Permissions
import com.indusjs.fleet.di.LocalViewModelProvider
import com.indusjs.uicomponents.components.FleetBottomNavBar
import com.indusjs.uicomponents.components.FleetNavItem
import com.indusjs.uicomponents.components.FleetNavRail
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.uicomponents.theme.rememberThemeToggle
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.cd_dark_mode
import indusjsfleet.ijs_ui_components_lib.generated.resources.cd_light_mode
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_cost
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_dashboard
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_driver
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_map
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_moon
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_more_vert
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_profile
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_settings
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_sun
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_trip
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_vehicle
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_customers
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_drivers
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_home
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_live_map
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_more
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_payments
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_profile
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_reports
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_team_members
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_trips
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_vehicle_finance
import indusjsfleet.ijs_ui_components_lib.generated.resources.nav_vehicles
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val KEY_HOME = "home"
private const val KEY_TRIPS = "trips"
private const val KEY_MAPS = "maps"
private const val KEY_PAYMENTS = "payments"
private const val KEY_MORE = "more"

/**
 * App-root nav chrome (Calm Fintech step 5) — wraps the [content] (the NavDisplay) with a persistent
 * bottom nav bar (Compact) / [FleetNavRail] (Medium+) on top-level destinations, plus a "More" sheet for
 * the overflow set. Destinations are gated on explicit permission slugs (A's RBAC enforcement is live).
 * Replaces the old dashboard-scoped hamburger drawer.
 *
 * Bottom bar = Home + first 3 permission-visible of [Trips, Live Map, Payments] + More.
 * More sheet = Vehicles / Drivers / Customers / Team / Vehicle Finance / Reports / Profile + theme toggle.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetNavScaffold(
    backStack: NavBackStack<FleetRoute>,
    snackbarHostState: SnackbarHostState,
    content: @Composable () -> Unit,
) {
    val perms = LocalViewModelProvider.current.permissionChecker
    val current = backStack.lastOrNull()
    val showNav = current.isTopLevelNav()
    var showMore by remember { mutableStateOf(false) }

    // Resolve labels unconditionally, then assemble the gated item list (plain data — no composable calls).
    val homeLabel = stringResource(Res.string.nav_home)
    val tripsLabel = stringResource(Res.string.nav_trips)
    val mapsLabel = stringResource(Res.string.nav_live_map)
    val paymentsLabel = stringResource(Res.string.nav_payments)
    val moreLabel = stringResource(Res.string.nav_more)

    val navItems = buildList {
        add(FleetNavItem(KEY_HOME, homeLabel, Res.drawable.ic_dashboard))
        val middles = buildList {
            if (perms.has(Permissions.TRIPS_VIEW)) add(FleetNavItem(KEY_TRIPS, tripsLabel, Res.drawable.ic_trip))
            if (perms.has(Permissions.LIVE_MAP_VIEW)) add(FleetNavItem(KEY_MAPS, mapsLabel, Res.drawable.ic_map))
            if (perms.has(Permissions.PAYMENTS_VIEW)) add(FleetNavItem(KEY_PAYMENTS, paymentsLabel, Res.drawable.ic_cost))
        }.take(3)
        addAll(middles)
        add(FleetNavItem(KEY_MORE, moreLabel, Res.drawable.ic_more_vert))
    }

    val selectedKey = when (current) {
        is FleetRoute.Dashboard -> KEY_HOME
        is FleetRoute.Trips -> KEY_TRIPS
        is FleetRoute.Maps -> KEY_MAPS
        is FleetRoute.Payments -> KEY_PAYMENTS
        is FleetRoute.Vehicles, is FleetRoute.Drivers, is FleetRoute.Customers,
        is FleetRoute.TeamList, is FleetRoute.Reports, is FleetRoute.VehicleFinance,
        is FleetRoute.Profile -> KEY_MORE
        else -> null
    }

    val onSelect: (String) -> Unit = { key ->
        when (key) {
            KEY_HOME -> backStack.navigateTopLevel(FleetRoute.Dashboard)
            KEY_TRIPS -> backStack.navigateTopLevel(FleetRoute.Trips)
            KEY_MAPS -> backStack.navigateTopLevel(FleetRoute.Maps)
            KEY_PAYMENTS -> backStack.navigateTopLevel(FleetRoute.Payments)
            KEY_MORE -> showMore = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = rememberFleetBreakpoint().isAtLeastMedium
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (showNav && !useRail) {
                    FleetBottomNavBar(items = navItems, selectedKey = selectedKey, onSelect = onSelect)
                }
            }
        ) { padding ->
            Row(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (showNav && useRail) {
                    FleetNavRail(items = navItems, selectedKey = selectedKey, onSelect = onSelect)
                }
                Box(modifier = Modifier.weight(1f).fillMaxSize()) { content() }
            }
        }
    }

    if (showMore) {
        MoreSheet(
            perms = perms,
            onDismiss = { showMore = false },
            onNavigate = { route -> showMore = false; backStack.navigateTopLevel(route) },
        )
    }
}

private fun FleetRoute?.isTopLevelNav(): Boolean = when (this) {
    is FleetRoute.Dashboard, is FleetRoute.Trips, is FleetRoute.Maps, is FleetRoute.Payments,
    is FleetRoute.Vehicles, is FleetRoute.Drivers, is FleetRoute.Customers,
    is FleetRoute.TeamList, is FleetRoute.Reports, is FleetRoute.VehicleFinance, is FleetRoute.Profile -> true
    else -> false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreSheet(
    perms: PermissionChecker,
    onDismiss: () -> Unit,
    onNavigate: (FleetRoute) -> Unit,
) {
    val isDark = isAppInDarkTheme()
    val toggleTheme = rememberThemeToggle()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = FleetTokens.Spacing.M)) {
            Text(
                text = stringResource(Res.string.nav_more),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.S),
            )
            if (perms.has(Permissions.VEHICLES_VIEW)) {
                MoreRow(Res.drawable.ic_vehicle, stringResource(Res.string.nav_vehicles)) { onNavigate(FleetRoute.Vehicles) }
            }
            if (perms.has(Permissions.DRIVERS_VIEW)) {
                MoreRow(Res.drawable.ic_driver, stringResource(Res.string.nav_drivers)) { onNavigate(FleetRoute.Drivers) }
            }
            if (perms.has(Permissions.CUSTOMERS_VIEW)) {
                MoreRow(Res.drawable.ic_profile, stringResource(Res.string.nav_customers)) { onNavigate(FleetRoute.Customers) }
            }
            if (perms.canManageTeam()) {
                MoreRow(Res.drawable.ic_team, stringResource(Res.string.nav_team_members)) { onNavigate(FleetRoute.TeamList) }
            }
            if (perms.has(Permissions.FINANCIALS_READ)) {
                MoreRow(Res.drawable.ic_cost, stringResource(Res.string.nav_vehicle_finance)) { onNavigate(FleetRoute.VehicleFinance) }
                MoreRow(Res.drawable.ic_dashboard, stringResource(Res.string.nav_reports)) { onNavigate(FleetRoute.Reports) }
            }
            MoreRow(Res.drawable.ic_settings, stringResource(Res.string.nav_profile)) { onNavigate(FleetRoute.Profile) }
            HorizontalDivider(modifier = Modifier.padding(vertical = FleetTokens.Spacing.S))
            MoreRow(
                iconRes = if (isDark) Res.drawable.ic_sun else Res.drawable.ic_moon,
                label = stringResource(if (isDark) Res.string.cd_light_mode else Res.string.cd_dark_mode),
            ) { toggleTheme() }
        }
    }
}

@Composable
private fun MoreRow(iconRes: DrawableResource, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(FleetTokens.IconSize.Default),
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
