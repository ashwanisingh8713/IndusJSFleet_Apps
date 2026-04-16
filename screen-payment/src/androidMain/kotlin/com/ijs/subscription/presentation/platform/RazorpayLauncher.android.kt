package com.ijs.subscription.presentation.platform

import android.app.Activity
import com.razorpay.Checkout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Singleton bridge between the Razorpay SDK callback (which lands in Activity)
 * and the Compose screen's [RazorpayLauncher] callback.
 *
 * AppActivity.onPaymentSuccess / onPaymentError call [AndroidRazorpayBridge.emit].
 * [createAndroidRazorpayLauncher] subscribes to [AndroidRazorpayBridge.results] once
 * per payment attempt, then cancels the collector.
 */
object AndroidRazorpayBridge {
    private val _results = kotlinx.coroutines.flow.MutableSharedFlow<RazorpayResult>(
        extraBufferCapacity = 1
    )
    val results = _results.asReplaySharedFlow()

    fun emit(result: RazorpayResult) {
        _results.tryEmit(result)
    }
}

private fun kotlinx.coroutines.flow.MutableSharedFlow<RazorpayResult>.asReplaySharedFlow() =
    this as kotlinx.coroutines.flow.SharedFlow<RazorpayResult>

/**
 * Creates an Android [RazorpayLauncher].
 *
 * Requirements:
 * - [activity] must implement `PaymentResultWithDataListener` and forward results
 *   to [AndroidRazorpayBridge.emit].
 * - Razorpay checkout SDK: `com.razorpay:checkout:1.6.40` added in androidApp/build.gradle.kts.
 */
fun createAndroidRazorpayLauncher(activity: Activity): RazorpayLauncher = { data, onResult ->
    Checkout.preload(activity.applicationContext)
    val checkout = Checkout()
    checkout.setKeyID(data.providerKey)

    val options = JSONObject(data.toRazorpaySdkOptionsJson())

    // Subscribe to the bridge for exactly one result, then cancel.
    var collectorJob: Job? = null
    val scope = CoroutineScope(Dispatchers.Main)
    collectorJob = scope.launch {
        AndroidRazorpayBridge.results.collect { result ->
            onResult(result)
            collectorJob?.cancel()
            scope.cancel()
        }
    }

    try {
        checkout.open(activity, options)
    } catch (e: Exception) {
        collectorJob.cancel()
        scope.cancel()
        onResult(RazorpayResult.Failed(0, e.message ?: "Failed to open Razorpay checkout"))
    }
}
