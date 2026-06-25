package com.ijs.reports.presentation.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.reports.domain.entity.CustomerPLItem
import com.ijs.reports.domain.entity.CustomerPLSummary
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPLScreen(
    viewModel: CustomerPLViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.customer_pl_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(CustomerPLContract.Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = rememberFleetBreakpoint().isExpanded
            val contentModifier = if (isWide) {
                Modifier.fillMaxSize().widthIn(max = FleetTokens.Width.MaxContent)
            } else {
                Modifier.fillMaxSize()
            }
            LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(FleetTokens.Spacing.L),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                item { PeriodRow(state.selectedPeriod) { viewModel.sendIntent(CustomerPLContract.Intent.SelectPeriod(it)) } }

                when {
                    state.isLoading && state.report == null -> item {
                        Box(Modifier.fillMaxWidth().padding(FleetTokens.Spacing.XXL), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null && state.report == null -> item {
                        ErrorRetry(state.error!!.resolve()) { viewModel.sendIntent(CustomerPLContract.Intent.Refresh) }
                    }
                    else -> {
                        state.report?.summary?.let { item { SummaryCard(it) } }
                        if (state.hasData) {
                            items(state.report!!.customers, key = { it.customerId }) { c -> CustomerRow(c) }
                        } else {
                            item { EmptyState() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodRow(selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
        CustomerPLContract.PERIODS.forEach { period ->
            val isSelected = period == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = FleetTokens.Spacing.XXXL)
                    .clip(RoundedCornerShape(FleetTokens.Radius.L))
                    .clickable { onSelect(period) },
                shape = RoundedCornerShape(FleetTokens.Radius.L),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    FleetTokens.Height.Divider,
                    MaterialTheme.colorScheme.primary.copy(alpha = if (isSelected) 1f else 0.3f)
                )
            ) {
                Box(Modifier.fillMaxSize().padding(vertical = FleetTokens.Spacing.S), contentAlignment = Alignment.Center) {
                    Text(
                        text = periodLabel(period),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun periodLabel(period: String): String = when (period) {
    "daily" -> stringResource(Res.string.customer_pl_period_daily)
    "weekly" -> stringResource(Res.string.customer_pl_period_weekly)
    "monthly" -> stringResource(Res.string.customer_pl_period_monthly)
    "quarterly" -> stringResource(Res.string.customer_pl_period_quarterly)
    "yearly" -> stringResource(Res.string.customer_pl_period_yearly)
    else -> period
}

@Composable
private fun SummaryCard(summary: CustomerPLSummary) {
    FleetSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                Kpi(Modifier.weight(1f), stringResource(Res.string.reports_revenue), formatCurrency(summary.totalRevenue), MaterialTheme.colorScheme.primary)
                Kpi(
                    Modifier.weight(1f),
                    stringResource(Res.string.customer_pl_net),
                    formatCurrency(summary.netProfit),
                    if (summary.netProfit >= 0) FleetStatusColors.ProfitGreen else FleetStatusColors.LossRed
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                Kpi(Modifier.weight(1f), stringResource(Res.string.customer_pl_pending), formatCurrency(summary.totalPending), FleetStatusColors.ExpenseAmber)
                Kpi(Modifier.weight(1f), stringResource(Res.string.customer_pl_active_customers), summary.activeCustomers.toString(), MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun Kpi(modifier: Modifier, label: String, value: String, accent: androidx.compose.ui.graphics.Color) {
    FleetMetricTile(
        value = value,
        label = label,
        modifier = modifier,
        accent = accent,
        valueColor = accent
    )
}

@Composable
private fun CustomerRow(c: CustomerPLItem) {
    val netColor = if (c.netProfit >= 0) FleetStatusColors.ProfitGreen else FleetStatusColors.LossRed
    FleetSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(c.customerName.ifBlank { "—" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(Res.string.customer_pl_trips_count, c.totalTrips), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatCurrency(c.netProfit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = netColor, maxLines = 1)
                    Text("${formatPercent(c.profitMargin)} • ${stringResource(Res.string.customer_pl_margin)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                MiniStat(Modifier.weight(1f), stringResource(Res.string.reports_revenue), formatCurrency(c.totalRevenue))
                MiniStat(Modifier.weight(1f), stringResource(Res.string.customer_pl_collection), formatPercent(c.collectionRate))
                MiniStat(Modifier.weight(1f), stringResource(Res.string.customer_pl_pending), formatCurrency(c.totalPending))
            }
        }
    }
}

@Composable
private fun MiniStat(modifier: Modifier, label: String, value: String) {
    FleetMetricTile(
        value = value,
        label = label,
        modifier = modifier,
        showBackground = false
    )
}

@Composable
private fun EmptyState() {
    EmptyContent(
        iconRes = Res.drawable.ic_dashboard,
        title = stringResource(Res.string.customer_pl_empty),
        fillMaxSize = false
    )
}

@Composable
private fun ErrorRetry(message: String, onRetry: () -> Unit) {
    ErrorContent(
        error = message,
        onRetry = onRetry,
        fillMaxSize = false
    )
}

private fun formatPercent(value: Double): String {
    val rounded = (value * 10).toInt() / 10.0
    return "$rounded%"
}
