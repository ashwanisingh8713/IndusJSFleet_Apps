package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Standardized Fleet Top App Bar.
 *
 * Ensures consistent surface background, icon tinting, and back navigation
 * across all 40+ screens. Replaces ad-hoc TopAppBar implementations.
 *
 * UX Finding #15: "Every screen manually creates TopAppBar with different
 * colors, icon tinting, and back button patterns."
 *
 * @param title The title text displayed in the app bar.
 * @param onNavigateBack Optional callback for the back button. Pass null to hide it.
 * @param subtitle Optional subtitle displayed below the title.
 * @param actions Composable slot for toolbar action icons.
 * @param scrollBehavior Optional scroll behavior for collapsing/expanding.
 * @param modifier Modifier for the TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            if (subtitle != null) {
                FleetTopAppBarTitleWithSubtitle(title = title, subtitle = subtitle)
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = stringResource(Res.string.back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

/**
 * Title with subtitle layout for FleetTopAppBar.
 */
@Composable
private fun FleetTopAppBarTitleWithSubtitle(
    title: String,
    subtitle: String
) {
    androidx.compose.foundation.layout.Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Standardized Fleet Top App Bar with a leading menu icon.
 * Used for root-level screens (Dashboard) with a navigation drawer.
 *
 * @param title The title text displayed in the app bar.
 * @param onMenuClick Callback when the hamburger menu icon is tapped.
 * @param actions Composable slot for toolbar action icons.
 * @param scrollBehavior Optional scroll behavior.
 * @param modifier Modifier for the TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetMenuTopAppBar(
    title: String,
    onMenuClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(Res.drawable.ic_menu),
                    contentDescription = stringResource(Res.string.cd_open_navigation_menu),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

