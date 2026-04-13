package com.ijs.subscription.presentation.platform

// JS (browser) Razorpay launcher using Razorpay checkout.js.
// Prerequisite: add to index.html before the Kotlin/JS bundle:
//   <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun createWebRazorpayLauncher(): RazorpayLauncher = { data, onResult ->

    val successCallback: (dynamic) -> Unit = { response ->
        @Suppress("UNCHECKED_CAST")
        onResult(
            RazorpayResult.Success(
                orderId = (response.razorpay_order_id as? String) ?: data.orderId,
                paymentId = (response.razorpay_payment_id as? String) ?: "",
                signature = (response.razorpay_signature as? String) ?: ""
            )
        )
    }

    val failCallback: (dynamic) -> Unit = { response ->
        val error: dynamic = response.error
        @Suppress("UNCHECKED_CAST")
        onResult(
            RazorpayResult.Failed(
                errorCode = (error?.code as? Int) ?: 0,
                description = (error?.description as? String) ?: "Payment failed"
            )
        )
    }

    val cancelCallback: () -> Unit = {
        onResult(RazorpayResult.Cancelled)
    }

    val options: dynamic = js("({})")
    options.key = data.providerKey
    options.amount = data.amount
    options.currency = data.currency
    options.order_id = data.orderId
    options.name = "IndusJS Fleet"
    options.description = data.planName
    options.receipt = data.receiptId

    val prefill: dynamic = js("({})")
    prefill.email = data.customerEmail
    prefill.name = data.customerName
    options.prefill = prefill

    val theme: dynamic = js("({})")
    theme.color = "#1976D2"
    options.theme = theme

    options.handler = successCallback

    val modal: dynamic = js("({})")
    modal.ondismiss = cancelCallback
    options.modal = modal

    @Suppress("UNUSED_VARIABLE")
    val rzp: dynamic = js("new Razorpay(options)")
    rzp.on("payment.failed", failCallback)
    rzp.open()
}
