@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.ijs.subscription.presentation.platform

import kotlin.js.ExperimentalWasmJsInterop

// Global bridge is defined in webApp index.html (window.__IndusFleetRazorpay.open).
@JsFun(
    """(payloadJson, onDone) => {
        const bridge = (typeof globalThis !== 'undefined' && globalThis.__IndusFleetRazorpay)
            || (typeof window !== 'undefined' && window.__IndusFleetRazorpay);
        if (!bridge || typeof bridge.open !== 'function') {
            onDone(JSON.stringify({ type: 'failed', code: 0, description: 'Razorpay bridge missing: add index.html scripts for checkout.js and __IndusFleetRazorpay.' }));
            return;
        }
        bridge.open(payloadJson, onDone);
    }"""
)
private external fun wasmRazorpayBridgeOpen(payloadJson: String, onDone: (String) -> Unit)

/**
 * WasmJS uses the same **index.html** bridge as documented for checkout.js (see webApp resources).
 */
fun createWasmJsRazorpayLauncher(): RazorpayLauncher = { data, onResult ->
    val payload = data.toRazorpaySdkOptionsJson()
    wasmRazorpayBridgeOpen(payload) { doneJson ->
        onResult(parseRazorpayWebBridgeResultJson(doneJson))
    }
}
