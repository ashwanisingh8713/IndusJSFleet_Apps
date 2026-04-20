package com.ijs.subscription

/**
 * **Testing only.** When the create-order API returns an empty `provider_key`, checkout still needs
 * a Razorpay **Key ID** (`rzp_test_…` / `rzp_live_…`) for `setKeyID` / Standard Checkout.
 *
 * Clear this constant (set to `""`) or remove usage before production, and always return
 * `provider_key` from the server instead.
 */
object SubscriptionPaymentTestConfig {
    const val FALLBACK_PROVIDER_KEY_WHEN_EMPTY: String = "rzp_test_SbOh5B6Y2Rcg4K"
}
