package com.ijs.subscription.presentation.checkout

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.subscription.TAG_CHECKOUT_VM
import com.ijs.subscription.domain.usecase.CreatePaymentOrderUseCase
import com.ijs.subscription.domain.usecase.VerifyPaymentUseCase
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.Effect
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.Intent
import com.ijs.subscription.presentation.checkout.PaymentCheckoutContract.State
import com.ijs.subscription.presentation.platform.RazorpayCheckoutData
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

@Inject
class PaymentCheckoutViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val createPaymentOrderUseCase: CreatePaymentOrderUseCase,
    private val verifyPaymentUseCase: VerifyPaymentUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Initialize -> updateState {
                copy(plan = intent.plan, billingInterval = intent.interval)
            }
            is Intent.InitiatePayment -> initiatePayment()
            is Intent.RazorpaySuccess -> verifyPayment(
                intent.orderId, intent.providerPaymentId, intent.signature
            )
            is Intent.RazorpayFailed -> onPaymentFailed(intent.errorCode, intent.errorDescription)
            is Intent.RazorpayCancelled -> updateState {
                copy(
                    isCreatingOrder = false,
                    isVerifyingPayment = false,
                    error = UiText.Raw("Payment was cancelled.")
                )
            }
            is Intent.ChangePlan -> sendEffect(Effect.NavigateBackToPlans)
            is Intent.DismissError -> updateState { copy(error = null) }
        }
    }

    private suspend fun initiatePayment() {
        val plan = currentState.plan ?: return
        val interval = currentState.billingInterval
        updateState { copy(isCreatingOrder = true, error = null) }

        withContext(dispatcherProvider.io) {
            createPaymentOrderUseCase(plan.id, interval).fold(
                onSuccess = { order ->
                    updateState { copy(isCreatingOrder = false, currentOrder = order) }
                    sendEffect(
                        Effect.LaunchRazorpayCheckout(
                            RazorpayCheckoutData(
                                orderId = order.orderId,
                                amount = order.amount,
                                currency = order.currency,
                                providerKey = order.providerKey,
                                receiptId = order.receiptId,
                                customerEmail = order.customerEmail,
                                customerName = order.customerName,
                                planName = plan.name
                            )
                        )
                    )
                },
                onFailure = { e ->
                    val msg = e.message ?: "Failed to initiate payment. Please try again."
                    updateState { copy(isCreatingOrder = false, error = UiText.Raw(msg)) }
                    sendEffect(Effect.ShowError(UiText.Raw(msg)))
                }
            )
        }
    }

    private suspend fun verifyPayment(orderId: String, paymentId: String, signature: String) {
        updateState { copy(isVerifyingPayment = true, error = null) }
        withContext(dispatcherProvider.io) {
            verifyPaymentUseCase(orderId, paymentId, signature).fold(
                onSuccess = { result ->
                    updateState { copy(isVerifyingPayment = false) }
                    sendEffect(
                        Effect.NavigateToSuccess(
                            planName = result.planName,
                            amount = result.amount,
                            currency = result.currency
                        )
                    )
                },
                onFailure = { e ->
                    val msg = e.message ?: "Payment verification failed. Please contact support."
                    updateState { copy(isVerifyingPayment = false, error = UiText.Raw(msg)) }
                    sendEffect(Effect.ShowError(UiText.Raw(msg)))
                }
            )
        }
    }

    private suspend fun onPaymentFailed(code: Int, description: String) {
        val msg = if (description.isNotBlank()) description else "Payment failed. Please try again."
        updateState {
            copy(
                isCreatingOrder = false,
                isVerifyingPayment = false,
                error = UiText.Raw(msg)
            )
        }
        sendEffect(Effect.ShowError(UiText.Raw(msg)))
    }
}
