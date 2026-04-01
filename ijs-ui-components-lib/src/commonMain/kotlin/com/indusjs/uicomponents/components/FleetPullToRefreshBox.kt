package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Pull-to-refresh wrapper for list screens.
 *
 * Wraps Material 3 PullToRefreshBox with sensible defaults so that
 * every list screen gets consistent swipe-to-refresh behaviour without
 * manual boilerplate.
 *
 * UX Finding #3: "No pull-to-refresh gesture on list screens."
 * UX Finding #83: "No pull-to-refresh wrapper component. Each screen must
 * manually implement pull-to-refresh; most don't."
 *
 * Usage:
 * ```
 * FleetPullToRefreshBox(
 *     isRefreshing = state.isRefreshing,
 *     onRefresh = { viewModel.sendIntent(Intent.Refresh) }
 * ) {
 *     LazyColumn { ... }
 * }
 * ```
 *
 * @param isRefreshing Whether the refresh indicator should be shown.
 * @param onRefresh Callback triggered when the user pulls to refresh.
 * @param modifier Modifier for the container.
 * @param enabled Whether pull-to-refresh is enabled.
 * @param content The scrollable content (typically LazyColumn).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    if (enabled) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                )
            },
            content = content
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            content = content
        )
    }
}

