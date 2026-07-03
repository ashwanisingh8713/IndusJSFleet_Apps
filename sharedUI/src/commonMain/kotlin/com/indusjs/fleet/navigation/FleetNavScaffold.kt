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
private const val KEY_VEHICLES = "vehicles"
private const val KEY_DRIVERS = "drivers"
private const val KEY_CUSTOMERS = "customers"
private const val KEY_TEAM = "team"
private const val KEY_FINANCE = "finance"
private const val KEY_REPORTS = "reports"
private const val KEY_PROFILE = "profile"
private const val KEY_MORE = "more"

/** How many primary slots the bottom bar/rail fills before spilling to More (Home + 3 backfilled). */
private const val PRIMARY_SLOTS = 3

/** One navigable destination: its stable [key], resolved [label], [icon], and [route]. */
private data class NavDest(
    val key: String,
    val label: String,
    val icon: DrawableResource,
    val route: FleetRoute,
)

/**
 * App-root nav chrome (Calm Fintech step 5) — wraps the [content] (the NavDisplay) with a persistent
 * bottom nav bar (Compact) / [FleetNavRail] (Medium+) on top-level destinations, plus a "More" sheet for
 * the overflow set. Destinations are gated on explicit permission slugs (A's RBAC enforcement is live).
 * Replaces the old dashboard-scoped hamburger drawer.
 *
 * Bottom bar = Home + the top 3 permission-visible destinations backfilled from an ordered priority
 * (Trips, Live Map, Payments, Vehicles, Drivers, Customers, Team, Vehicle Finance, Reports) + More — so
 * a role missing a preferred middle pulls the next permitted item up and the bar never shows a gap.
 * More sheet = the overflow (everything not promoted into the bar) + Profile + theme toggle.
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
    val vehiclesLabel = stringResource(Res.string.nav_vehicles)
    val driversLabel = stringResource(Res.string.nav_drivers)
    val customersLabel = stringResource(Res.string.nav_customers)
    val teamLabel = stringResource(Res.string.nav_team_members)
    val financeLabel = stringResource(Res.string.nav_vehicle_finance)
    val reportsLabel = stringResource(Res.string.nav_reports)
    val profileLabel = stringResource(Res.string.nav_profile)
    val moreLabel = stringResource(Res.string.nav_more)

    // Ordered priority of every permission-visible destination (Home is always first; Profile always
    // lives in More). The bar backfills its primary slots from the TOP of this list — a role missing a
    // preferred middle (e.g. Payments) pulls the next permitted item up so the bar never shows a gap.
    val backfillOrder = buildList {
        if (perms.has(Permissions.TRIPS_VIEW)) add(NavDest(KEY_TRIPS, tripsLabel, Res.drawable.ic_trip, FleetRoute.Trips))
        if (perms.has(Permissions.LIVE_MAP_VIEW)) add(NavDest(KEY_MAPS, mapsLabel, Res.drawable.ic_map, FleetRoute.Maps))
        if (perms.has(Permissions.PAYMENTS_VIEW)) add(NavDest(KEY_PAYMENTS, paymentsLabel, Res.drawable.ic_cost, FleetRoute.Payments))
        if (perms.has(Permissions.VEHICLES_VIEW)) add(NavDest(KEY_VEHICLES, vehiclesLabel, Res.drawable.ic_vehicle, FleetRoute.Vehicles))
        if (perms.has(Permissions.DRIVERS_VIEW)) add(NavDest(KEY_DRIVERS, driversLabel, Res.drawable.ic_driver, FleetRoute.Drivers))
        if (perms.has(Permissions.CUSTOMERS_VIEW)) add(NavDest(KEY_CUSTOMERS, customersLabel, Res.drawable.ic_profile, FleetRoute.Customers))
        if (perms.canManageTeam()) add(NavDest(KEY_TEAM, teamLabel, Res.drawable.ic_team, FleetRoute.TeamList))
        if (perms.has(Permissions.FINANCIALS_READ)) {
            add(NavDest(KEY_FINANCE, financeLabel, Res.drawable.ic_cost, FleetRoute.VehicleFinance))
            add(NavDest(KEY_REPORTS, reportsLabel, Res.drawable.ic_dashboard, FleetRoute.Reports))
        }
    }
    val barMiddles = backfillOrder.take(PRIMARY_SLOTS)
    // Everything not promoted into the bar spills to More; Profile is always in More.
    val overflow = backfillOrder.drop(PRIMARY_SLOTS) + NavDest(KEY_PROFILE, profileLabel, Res.drawable.ic_settings, FleetRoute.Profile)

    val navItems = buildList {
        add(FleetNavItem(KEY_HOME, homeLabel, Res.drawable.ic_dashboard))
        barMiddles.forEach { add(FleetNavItem(it.key, it.label, it.icon)) }
        add(FleetNavItem(KEY_MORE, moreLabel, Res.drawable.ic_more_vert))
    }

    // The current route maps to a destination key; if that key sits in the bar it highlights directly,
    // otherwise (an overflow/Profile destination) the bar highlights More.
    val currentKey = when (current) {
        is FleetRoute.Dashboard -> KEY_HOME
        is FleetRoute.Trips -> KEY_TRIPS
        is FleetRoute.Maps -> KEY_MAPS
        is FleetRoute.Payments -> KEY_PAYMENTS
        is FleetRoute.Vehicles -> KEY_VEHICLES
        is FleetRoute.Drivers -> KEY_DRIVERS
        is FleetRoute.Customers -> KEY_CUSTOMERS
        is FleetRoute.TeamList -> KEY_TEAM
        is FleetRoute.VehicleFinance -> KEY_FINANCE
        is FleetRoute.Reports -> KEY_REPORTS
        is FleetRoute.Profile -> KEY_PROFILE
        else -> null
    }
    val selectedKey = when {
        currentKey == KEY_HOME -> KEY_HOME
        currentKey != null && barMiddles.any { it.key == currentKey } -> currentKey
        currentKey != null -> KEY_MORE
        else -> null
    }

    val onSelect: (String) -> Unit = { key ->
        when (key) {
            KEY_HOME -> backStack.navigateTopLevel(FleetRoute.Dashboard)
            KEY_MORE -> showMore = true
            else -> barMiddles.firstOrNull { it.key == key }?.let { backStack.navigateTopLevel(it.route) }
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
            items = overflow,
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
    items: List<NavDest>,
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
            // The overflow set (everything not promoted into the bar, plus Profile) — already gated upstream.
            items.forEach { dest ->
                MoreRow(dest.icon, dest.label) { onNavigate(dest.route) }
            }
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
