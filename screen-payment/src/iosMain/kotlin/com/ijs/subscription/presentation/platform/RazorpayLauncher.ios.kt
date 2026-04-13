package com.ijs.subscription.presentation.platform

/**
 * iOS Razorpay launcher.
 *
 * Two integration strategies available:
 *
 * Strategy A — WebView (recommended as a quick start):
 *   Open `checkout.html` in a WKWebView passing Razorpay params as query args.
 *   The HTML page calls Razorpay checkout.js and posts the result back via
 *   `window.webkit.messageHandlers.razorpay.postMessage(...)`.
 *
 * Strategy B — Native Razorpay iOS SDK (CocoaPod `razorpay-pod`):
 *   Requires Kotlin/ObjC interop. Add pod to iosApp/Podfile:
 *   `pod 'razorpay-pod', '~> 1.3.0'`
 *   Then call `RazorpayCheckout.initWithKey(providerKey)` and `open(options)`.
 *
 * Current status: returns a stub result so the CI/build does not break.
 * Replace with real implementation when iOS deployment begins.
 */
fun createIosRazorpayLauncher(): RazorpayLauncher = { data, onResult ->
    // TODO(iOS): Replace this stub with real Razorpay iOS SDK or WebView checkout.
    // For development / stub payment provider the verify endpoint accepts any values.
    onResult(
        RazorpayResult.Failed(
            errorCode = 0,
            description = "iOS Razorpay integration is pending. " +
                    "Use the web app or implement via WKWebView checkout for testing."
        )
    )
}
