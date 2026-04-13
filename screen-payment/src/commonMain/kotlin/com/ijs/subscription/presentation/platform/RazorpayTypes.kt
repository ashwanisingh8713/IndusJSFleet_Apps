package com.ijs.subscription.presentation.platform

data class RazorpayCheckoutData(
    val orderId: String,
    val amount: Long,
    val currency: String,
    val providerKey: String,
    val receiptId: String,
    val customerEmail: String,
    val customerName: String,
    val planName: String
)

sealed class RazorpayResult {
    data class Success(
        val orderId: String,
        val paymentId: String,
        val signature: String
    ) : RazorpayResult()

    data class Failed(
        val errorCode: Int,
        val description: String
    ) : RazorpayResult()

    data object Cancelled : RazorpayResult()
}

typealias RazorpayLauncher = (data: RazorpayCheckoutData, onResult: (RazorpayResult) -> Unit) -> Unit
