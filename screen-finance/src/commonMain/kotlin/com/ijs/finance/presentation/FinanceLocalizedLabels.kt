package com.ijs.finance.presentation

import androidx.compose.runtime.Composable
import com.ijs.finance.domain.entity.LoanPayment
import com.ijs.finance.domain.entity.PaymentMode
import com.ijs.finance.domain.entity.PaymentStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_emi_label
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_emi_payment_fallback
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_auto_debit
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_cash
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_cheque
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_netbanking
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_other
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_mode_upi
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_status_cancelled
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_status_failed
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_status_overdue
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_status_paid
import indusjsfleet.ijs_ui_components_lib.generated.resources.finance_payment_status_pending
import org.jetbrains.compose.resources.stringResource

/**
 * Localized @Composable display-name resolvers for finance domain enums and
 * display getters that are rendered inside @Composable scope.
 *
 * Mirrors the pattern in screen-team's TeamLocalizedLabels. ViewModels keep
 * emitting [com.indusjs.uicomponents.components.UiText]; these resolvers cover
 * the enum/domain labels that screens render directly.
 */

@Composable
fun PaymentMode.localizedLabel(): String = when (this) {
    PaymentMode.CASH -> stringResource(Res.string.finance_payment_mode_cash)
    PaymentMode.NETBANKING -> stringResource(Res.string.finance_payment_mode_netbanking)
    PaymentMode.UPI -> stringResource(Res.string.finance_payment_mode_upi)
    PaymentMode.AUTO_DEBIT -> stringResource(Res.string.finance_payment_mode_auto_debit)
    PaymentMode.CHEQUE -> stringResource(Res.string.finance_payment_mode_cheque)
    PaymentMode.OTHER -> stringResource(Res.string.finance_payment_mode_other)
}

@Composable
fun PaymentStatus.localizedLabel(): String = when (this) {
    PaymentStatus.PENDING -> stringResource(Res.string.finance_payment_status_pending)
    PaymentStatus.PAID -> stringResource(Res.string.finance_payment_status_paid)
    PaymentStatus.OVERDUE -> stringResource(Res.string.finance_payment_status_overdue)
    PaymentStatus.FAILED -> stringResource(Res.string.finance_payment_status_failed)
    PaymentStatus.CANCELLED -> stringResource(Res.string.finance_payment_status_cancelled)
}

/**
 * Localized EMI label for a [LoanPayment]: "EMI #<n>" when an EMI number is
 * present, else a generic "Payment" fallback.
 */
@Composable
fun LoanPayment.localizedEmiLabel(): String =
    emiNumber?.let { stringResource(Res.string.finance_emi_label, it) }
        ?: stringResource(Res.string.finance_emi_payment_fallback)
