package com.ijs.subscription.presentation.platform

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val razorpayJson = Json { ignoreUnknownKeys = true; isLenient = true }

private val bridgeResultJson = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * Parses the JSON line produced by `__IndusFleetRazorpay.open` in `webApp` `index.html`
 * (`type`: `success` | `failed` | `dismiss`). Used by Wasm and optionally Kotlin/JS.
 */
fun parseRazorpayWebBridgeResultJson(json: String): RazorpayResult {
    return try {
        val root = bridgeResultJson.parseToJsonElement(json).jsonObject
        val type = root["type"]?.jsonPrimitive?.content
            ?: return RazorpayResult.Failed(0, "Invalid Razorpay bridge response")
        when (type) {
            "success" -> RazorpayResult.Success(
                orderId = root["razorpay_order_id"]?.jsonPrimitive?.content.orEmpty(),
                paymentId = root["razorpay_payment_id"]?.jsonPrimitive?.content.orEmpty(),
                signature = root["razorpay_signature"]?.jsonPrimitive?.content.orEmpty()
            )
            "failed" -> RazorpayResult.Failed(
                errorCode = root["code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                description = root["description"]?.jsonPrimitive?.content ?: "Payment failed"
            )
            "dismiss" -> RazorpayResult.Cancelled
            else -> RazorpayResult.Failed(0, "Unknown bridge type: $type")
        }
    } catch (_: Throwable) {
        RazorpayResult.Failed(0, "Failed to parse Razorpay bridge response")
    }
}

/**
 * JSON object for Razorpay Checkout (JS / Wasm bridge / WKWebView), aligned with Android SDK options.
 */
fun RazorpayCheckoutData.toRazorpaySdkOptionsJson(): String {
    val o: JsonObject = buildJsonObject {
        put("key", providerKey)
        put("amount", amount)
        put("currency", currency)
        put("order_id", orderId)
        put("name", "IndusJS Fleet")
        put("description", planName)
        put("receipt", receiptId)
        put("prefill", buildJsonObject {
            put("email", customerEmail)
            put("name", customerName)
        })
        put("theme", buildJsonObject { put("color", "#1976D2") })
    }
    return razorpayJson.encodeToString(o)
}

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
