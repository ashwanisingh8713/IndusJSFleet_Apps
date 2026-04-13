package com.ijs.subscription.presentation.platform

/**
 * WasmJS (browser) Razorpay launcher stub.
 *
 * WasmJS does not have direct dynamic typing support, and bridging to
 * Razorpay checkout.js requires a dedicated JS interop module or
 * @JsExport adapter approach. This stub always returns a failure so that
 * the Wasm build compiles and the web target can fall back gracefully.
 *
 * For production, extract checkout logic into a JS adapter module and
 * call it via external declarations / @JsExport / JS interop wrappers.
 */
fun createWasmJsRazorpayLauncher(): RazorpayLauncher = { _, onResult ->
    onResult(
        RazorpayResult.Failed(
            errorCode = 0,
            description = "Razorpay checkout is not supported on the WasmJS target. " +
                    "Use the JS target or implement a JS interop adapter."
        )
    )
}
