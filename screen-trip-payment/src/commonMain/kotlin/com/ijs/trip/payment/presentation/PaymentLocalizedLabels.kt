package com.ijs.trip.payment.presentation

import androidx.compose.runtime.Composable
import com.ijs.trip.payment.domain.entity.PaymentMode
import com.ijs.trip.payment.domain.entity.PaymentStatus
import com.ijs.trip.payment.domain.entity.PaymentType
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaymentMode.localizedDisplayName(): String = when (this) {
    PaymentMode.CASH -> stringResource(Res.string.payment_mode_cash)
    PaymentMode.UPI -> stringResource(Res.string.payment_mode_upi)
    PaymentMode.BANK_TRANSFER -> stringResource(Res.string.payment_mode_bank_transfer)
    PaymentMode.CARD -> stringResource(Res.string.payment_mode_card)
    PaymentMode.CREDIT -> stringResource(Res.string.payment_mode_credit)
}

@Composable
internal fun PaymentType.localizedDisplayName(): String = when (this) {
    PaymentType.ADVANCE -> stringResource(Res.string.payment_type_advance)
    PaymentType.PARTIAL -> stringResource(Res.string.payment_type_partial)
    PaymentType.FINAL -> stringResource(Res.string.payment_type_final)
    PaymentType.REFUND -> stringResource(Res.string.payment_type_refund)
}

@Composable
internal fun PaymentStatus.localizedDisplayName(): String = when (this) {
    PaymentStatus.RECEIVED -> stringResource(Res.string.payment_status_received)
    PaymentStatus.PENDING -> stringResource(Res.string.payment_status_pending)
    PaymentStatus.CANCELLED -> stringResource(Res.string.payment_status_cancelled)
}

@Composable
internal fun PaymentType.localizedDescription(): String = when (this) {
    PaymentType.ADVANCE -> stringResource(Res.string.payment_type_advance_desc)
    PaymentType.PARTIAL -> stringResource(Res.string.payment_type_partial_desc)
    PaymentType.FINAL -> stringResource(Res.string.payment_type_final_desc)
    PaymentType.REFUND -> stringResource(Res.string.payment_type_refund_desc)
}

@Composable
internal fun tripCardStateLabel(state: String?): String =
    if (state.isNullOrBlank()) stringResource(Res.string.unknown) else tripStateFilterLabel(state)

@Composable
internal fun tripStateFilterLabel(stateKey: String): String = when (stateKey.lowercase()) {
    "on_route" -> stringResource(Res.string.payment_trip_state_on_route)
    "completed" -> stringResource(Res.string.payment_trip_state_completed)
    "planned" -> stringResource(Res.string.payment_trip_state_planned)
    "cancelled" -> stringResource(Res.string.payment_status_cancelled)
    "failed" -> stringResource(Res.string.payment_trip_state_failed)
    "delayed" -> stringResource(Res.string.payment_trip_state_delayed)
    else -> stateKey.replaceFirstChar { it.uppercase() }.ifBlank { stringResource(Res.string.unknown) }
}

@Composable
internal fun tripCardPaymentStatusLabel(paymentStatusKey: String?): String = when (paymentStatusKey?.lowercase()) {
    "pending" -> stringResource(Res.string.payment_trip_payment_unpaid)
    "partial" -> stringResource(Res.string.payment_trip_payment_partial_label)
    "paid" -> stringResource(Res.string.payment_trip_payment_paid)
    else -> stringResource(Res.string.payment_status_pending)
}
