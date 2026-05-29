package com.ijs.trip.payment.presentation

import androidx.compose.runtime.Composable

/**
 * Facade for the Payment feature module.
 */
object PaymentFeatureFacade {

    @Composable
    fun PaymentsListEntry(
        viewModel: PaymentsViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToDetail: (String) -> Unit,
        onNavigateToAddPayment: (tripId: String?) -> Unit
    ) {
        PaymentsScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAddPayment = onNavigateToAddPayment
        )
    }

    @Composable
    fun AddPaymentEntry(
        viewModel: AddTripPaymentViewModel,
        tripId: String? = null,
        paymentId: String? = null,
        onNavigateBack: () -> Unit
    ) {
        CreateTripPaymentScreen(
            viewModel = viewModel,
            tripId = tripId,
            paymentId = paymentId,
            onNavigateBack = onNavigateBack
        )
    }

    @Composable
    fun PaymentDetailEntry(
        viewModel: PaymentDetailViewModel,
        paymentId: String,
        onNavigateBack: () -> Unit,
        onNavigateToEdit: (String) -> Unit
    ) {
        PaymentDetailScreen(
            viewModel = viewModel,
            paymentId = paymentId,
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit
        )
    }
}

