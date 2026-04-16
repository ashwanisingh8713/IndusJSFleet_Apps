package com.ijs.subscription.presentation.platform

import kotlin.js.JSON

// JS (browser) Razorpay launcher using Razorpay checkout.js.
// Prerequisite: index.html must load https://checkout.razorpay.com/v1/checkout.js before the app bundle.

/** Razorpay often sends `error.code` as a string; normalize for [RazorpayResult.Failed]. */
@Suppress("UNUSED_PARAMETER")
private fun coerceRazorpayErrorCode(code: dynamic): Int =
    js(
        """(function(c){
            if (c == null || c === undefined) return 0;
            if (typeof c === 'number' && !isNaN(c)) return c | 0;
            var p = parseInt(String(c), 10);
            return isNaN(p) ? 0 : p;
        })"""
    )(code) as Int

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun createWebRazorpayLauncher(): RazorpayLauncher = { data, onResult ->
    val hasRzp = js("(typeof Razorpay !== 'undefined')") as Boolean
    if (!hasRzp) {
        onResult(
            RazorpayResult.Failed(
                errorCode = 0,
                description = "Razorpay checkout.js is not loaded. Add <script src=\"https://checkout.razorpay.com/v1/checkout.js\"></script> before your bundle in index.html."
            )
        )
    } else {
        val hasWebBridge = js(
            """(function(){
                var b = (typeof globalThis !== 'undefined' && globalThis.__IndusFleetRazorpay)
                    || (typeof window !== 'undefined' && window.__IndusFleetRazorpay);
                return !!(b && typeof b.open === 'function');
            })()"""
        ) as Boolean
        if (hasWebBridge) {
            // Same path as Wasm: guarded single completion in index.html bridge.
            val bridge: dynamic = js(
                """(typeof globalThis !== 'undefined' && globalThis.__IndusFleetRazorpay)
                    || (typeof window !== 'undefined' && window.__IndusFleetRazorpay)"""
            )
            bridge.open(data.toRazorpaySdkOptionsJson()) { json: String ->
                onResult(parseRazorpayWebBridgeResultJson(json))
            }
        } else {
            val options = JSON.parse<dynamic>(data.toRazorpaySdkOptionsJson())

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
                        errorCode = coerceRazorpayErrorCode(error?.code),
                        description = (error?.description as? String) ?: "Payment failed"
                    )
                )
            }

            val cancelCallback: () -> Unit = {
                onResult(RazorpayResult.Cancelled)
            }

            options.handler = successCallback

            val modal: dynamic = js("({})")
            modal.ondismiss = cancelCallback
            options.modal = modal

            @Suppress("UNUSED_VARIABLE")
            val rzp: dynamic = js("new Razorpay(options)")
            rzp.on("payment.failed", failCallback)
            rzp.open()
        }
    }
}
