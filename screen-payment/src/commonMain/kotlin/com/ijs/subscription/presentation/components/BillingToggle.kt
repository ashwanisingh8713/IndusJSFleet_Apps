package com.ijs.subscription.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ijs.subscription.domain.entity.BillingInterval
import com.indusjs.uicomponents.components.FleetTab
import com.indusjs.uicomponents.components.FleetTabBar
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.billing_annual
import indusjsfleet.ijs_ui_components_lib.generated.resources.billing_monthly
import indusjsfleet.ijs_ui_components_lib.generated.resources.billing_save_annual
import org.jetbrains.compose.resources.stringResource

/**
 * Monthly / Annual billing switch. §1 anti-rainbow: the switch is the canonical neutral [FleetTabBar]
 * segmented pill (no indigo fill), and the annual-savings callout is a neutral chip — differentiation
 * is by copy, not colour.
 */
@Composable
fun BillingToggle(
    selected: BillingInterval,
    onToggle: (BillingInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf(
            FleetTab(BillingInterval.MONTHLY, stringResource(Res.string.billing_monthly)),
            FleetTab(BillingInterval.ANNUAL, stringResource(Res.string.billing_annual)),
        )
        FleetTabBar(
            tabs = tabs,
            selectedTabId = selected,
            onTabSelected = onToggle,
            scrollable = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Savings callout shown below the switch when annual is NOT yet selected — neutral chip.
        if (selected == BillingInterval.MONTHLY) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = stringResource(Res.string.billing_save_annual),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}
