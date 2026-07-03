package com.ijs.subscription.presentation.checkout

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.checkout_payment_cancelled
import indusjsfleet.ijs_ui_components_lib.generated.resources.err_initiate_payment
import indusjsfleet.ijs_ui_components_lib.generated.resources.err_payment_failed
import indusjsfleet.ijs_ui_components_lib.generated.resources.err_verify_payment
import com.ijs.subscription.SubscriptionPaymentTestConfig
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
                    error = UiText.StringRes(Res.string.checkout_payment_cancelled)
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
                    val serverKey = order.providerKey
                    val isValidRazorpayKey = serverKey.startsWith("rzp_test_") || serverKey.startsWith("rzp_live_")
                    val effectiveKey = if (isValidRazorpayKey) serverKey else SubscriptionPaymentTestConfig.FALLBACK_PROVIDER_KEY_WHEN_EMPTY
                    sendEffect(
                        Effect.LaunchRazorpayCheckout(
                            RazorpayCheckoutData(
                                orderId = order.orderId,
                                amount = order.amount,
                                currency = order.currency,
                                providerKey = effectiveKey,
                                receiptId = order.receiptId,
                                customerEmail = order.customerEmail,
                                customerName = order.customerName,
                                planName = plan.name
                            )
                        )
                    )
                },
                onFailure = { e ->
                    val err = e.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.err_initiate_payment)
                    updateState { copy(isCreatingOrder = false, error = err) }
                    sendEffect(Effect.ShowError(err))
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
                    val err = e.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.err_verify_payment)
                    updateState { copy(isVerifyingPayment = false, error = err) }
                    sendEffect(Effect.ShowError(err))
                }
            )
        }
    }

    private suspend fun onPaymentFailed(code: Int, description: String) {
        val err = if (description.isNotBlank()) UiText.Raw(description)
            else UiText.StringRes(Res.string.err_payment_failed)
        updateState {
            copy(
                isCreatingOrder = false,
                isVerifyingPayment = false,
                error = err
            )
        }
        sendEffect(Effect.ShowError(err))
    }
}
