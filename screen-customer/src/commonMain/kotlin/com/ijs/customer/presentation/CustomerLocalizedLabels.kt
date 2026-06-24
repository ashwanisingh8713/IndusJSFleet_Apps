package com.ijs.customer.presentation

import androidx.compose.runtime.Composable
import com.ijs.customer.domain.entity.CustomerPayment
import com.ijs.customer.domain.entity.CustomerTrip
import com.ijs.customer.domain.entity.FinancialPeriod
import com.ijs.customer.domain.entity.PaymentByMode
import com.ijs.customer.domain.entity.PaymentMode
import com.ijs.customer.presentation.detail.CustomerDetailContract.CustomerDetailTab
import com.ijs.customer.presentation.detail.CustomerDetailContract.TripStateFilter
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.customer_tab_financials
import indusjsfleet.ijs_ui_components_lib.generated.resources.driver_tab_overview
import indusjsfleet.ijs_ui_components_lib.generated.resources.filter_all
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_other
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_mode_bank_transfer
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_mode_card
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_mode_cash
import indusjsfleet.ijs_ui_components_lib.generated.resources.payment_mode_upi
import indusjsfleet.ijs_ui_components_lib.generated.resources.payments_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.period_custom
import indusjsfleet.ijs_ui_components_lib.generated.resources.period_monthly
import indusjsfleet.ijs_ui_components_lib.generated.resources.period_quarterly
import indusjsfleet.ijs_ui_components_lib.generated.resources.period_yearly
import indusjsfleet.ijs_ui_components_lib.generated.resources.trip_state_cancelled
import indusjsfleet.ijs_ui_components_lib.generated.resources.trip_state_completed
import indusjsfleet.ijs_ui_components_lib.generated.resources.trip_state_delayed
import indusjsfleet.ijs_ui_components_lib.generated.resources.trip_state_failed
import indusjsfleet.ijs_ui_components_lib.generated.resources.trip_state_on_route
import indusjsfleet.ijs_ui_components_lib.generated.resources.trip_state_planned
import indusjsfleet.ijs_ui_components_lib.generated.resources.trips_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.unknown
import org.jetbrains.compose.resources.stringResource

/**
 * @Composable resolvers for customer enum/domain display getters that are
 * rendered at composable call sites. Mirrors the TeamLocalizedLabels pattern:
 * non-composable display getters (e.g. [PaymentMode.displayName]) remain for
 * PDF/non-UI use, while the UI renders the localized variant.
 */

@Composable
fun CustomerDetailTab.localizedTitle(): String = when (this) {
    CustomerDetailTab.OVERVIEW -> stringResource(Res.string.driver_tab_overview)
    CustomerDetailTab.TRIPS -> stringResource(Res.string.trips_title)
    CustomerDetailTab.PAYMENTS -> stringResource(Res.string.payments_title)
    CustomerDetailTab.FINANCIALS -> stringResource(Res.string.customer_tab_financials)
}

@Composable
fun TripStateFilter.localizedDisplayName(): String = when (this) {
    TripStateFilter.ALL -> stringResource(Res.string.filter_all)
    TripStateFilter.PLANNED -> stringResource(Res.string.trip_state_planned)
    TripStateFilter.ON_ROUTE -> stringResource(Res.string.trip_state_on_route)
    TripStateFilter.COMPLETED -> stringResource(Res.string.trip_state_completed)
    TripStateFilter.CANCELLED -> stringResource(Res.string.trip_state_cancelled)
}

@Composable
fun FinancialPeriod.localizedDisplayName(): String = when (this) {
    FinancialPeriod.MONTHLY -> stringResource(Res.string.period_monthly)
    FinancialPeriod.QUARTERLY -> stringResource(Res.string.period_quarterly)
    FinancialPeriod.YEARLY -> stringResource(Res.string.period_yearly)
    FinancialPeriod.CUSTOM -> stringResource(Res.string.period_custom)
}

@Composable
fun PaymentMode.localizedDisplayName(): String = when (this) {
    PaymentMode.CASH -> stringResource(Res.string.payment_mode_cash)
    PaymentMode.UPI -> stringResource(Res.string.payment_mode_upi)
    PaymentMode.BANK_TRANSFER -> stringResource(Res.string.payment_mode_bank_transfer)
    PaymentMode.CARD -> stringResource(Res.string.payment_mode_card)
}

/**
 * Localized state label for a trip row. Mirrors [CustomerTrip.stateDisplay]
 * but resolves to string resources; unknown states fall back to the raw,
 * capitalized backend token.
 */
@Composable
fun CustomerTrip.localizedStateDisplay(): String = when (state?.lowercase()) {
    "planned" -> stringResource(Res.string.trip_state_planned)
    "in_progress" -> stringResource(Res.string.trip_state_on_route)
    "completed" -> stringResource(Res.string.trip_state_completed)
    "cancelled" -> stringResource(Res.string.trip_state_cancelled)
    "delayed" -> stringResource(Res.string.trip_state_delayed)
    "failed" -> stringResource(Res.string.trip_state_failed)
    else -> state?.replaceFirstChar { it.uppercase() } ?: stringResource(Res.string.unknown)
}

/** Localized payment mode label for a received payment row. */
@Composable
fun CustomerPayment.localizedModeDisplay(): String =
    mode?.localizedDisplayName() ?: stringResource(Res.string.unknown)

/** Localized payment mode label for a payment-by-mode breakdown row. */
@Composable
fun PaymentByMode.localizedModeDisplay(): String =
    mode?.localizedDisplayName() ?: stringResource(Res.string.finance_payment_mode_other)
