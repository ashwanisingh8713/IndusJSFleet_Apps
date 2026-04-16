@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ijs.subscription.presentation.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import platform.CoreGraphics.CGRect
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private val jsonEncoder = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * iOS Razorpay via **WKWebView + checkout.js** (same contract as Android / web).
 *
 * Covers: payment success, `payment.failed`, checkout `ondismiss`, and unexpected modal teardown.
 */
fun createIosRazorpayLauncher(): RazorpayLauncher = { data, onResult ->
    dispatch_async(dispatch_get_main_queue()) {
        try {
            presentIosRazorpayWebCheckout(data, onResult)
        } catch (e: Throwable) {
            onResult(RazorpayResult.Failed(0, e.message ?: "Failed to start Razorpay"))
        }
    }
}

private fun findKeyWindow() = UIApplication.sharedApplication.keyWindow

private fun topMostViewController(root: UIViewController): UIViewController {
    var top = root
    while (top.presentedViewController != null) {
        top = top.presentedViewController ?: break
    }
    if (top is UINavigationController) {
        top.visibleViewController?.let { return topMostViewController(it) }
    }
    if (top is platform.UIKit.UITabBarController) {
        top.selectedViewController?.let { return topMostViewController(it) }
    }
    return top
}

private fun base64Utf8(utf8: String): String {
    val bytes = utf8.encodeToByteArray()
    val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val out = StringBuilder((bytes.size + 2) / 3 * 4)
    var i = 0
    while (i < bytes.size) {
        val b1 = bytes[i].toInt() and 0xFF
        val b2 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
        val b3 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
        val pad = when {
            i + 2 < bytes.size -> 0
            i + 1 < bytes.size -> 1
            else -> 2
        }
        val n = (b1 shl 16) or (b2 shl 8) or b3
        out.append(alphabet[(n ushr 18) and 63])
        out.append(alphabet[(n ushr 12) and 63])
        out.append(if (pad < 2) alphabet[(n ushr 6) and 63] else '=')
        out.append(if (pad < 1) alphabet[n and 63] else '=')
        i += 3
    }
    return out.toString()
}

private fun buildCheckoutHtml(payloadB64: String): String = """
<!DOCTYPE html>
<html><head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
<style>html,body{margin:0;height:100%;background:#000}</style>
</head><body>
<script>
(function () {
  var b64 = "$payloadB64";
  function post(msg) {
    try {
      window.webkit.messageHandlers.razorpayNative.postMessage(msg);
    } catch (e) {}
  }
  try {
    var binary = atob(b64);
    var bytes = new Uint8Array(binary.length);
    for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
    var json = new TextDecoder('utf-8').decode(bytes);
    var options = JSON.parse(json);
    options.handler = function (response) {
      post(JSON.stringify({
        type: 'success',
        razorpay_order_id: response.razorpay_order_id,
        razorpay_payment_id: response.razorpay_payment_id,
        razorpay_signature: response.razorpay_signature
      }));
    };
    options.modal = {
      ondismiss: function () {
        post(JSON.stringify({ type: 'dismiss' }));
      }
    };
    if (typeof Razorpay === 'undefined') {
      post(JSON.stringify({ type: 'failed', code: 0, description: 'Razorpay SDK failed to load' }));
      return;
    }
    var rzp = new Razorpay(options);
    rzp.on('payment.failed', function (resp) {
      var e = (resp && resp.error) || {};
      post(JSON.stringify({ type: 'failed', code: e.code || 0, description: e.description || 'Payment failed' }));
    });
    rzp.open();
  } catch (err) {
    post(JSON.stringify({ type: 'failed', code: 0, description: String(err) }));
  }
})();
</script></body></html>
""".trimIndent()

private class RazorpayScriptHandler(
    private val onText: (String) -> Unit
) : NSObject(), WKScriptMessageHandlerProtocol {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        val raw = didReceiveScriptMessage.body
        val text = (raw as? String) ?: raw.toString()
        dispatch_async(dispatch_get_main_queue()) {
            onText(text)
        }
    }
}

private fun handleRazorpayMessage(json: String, complete: (RazorpayResult) -> Unit) {
    try {
        val root = jsonEncoder.parseToJsonElement(json).jsonObject
        val type = root["type"]?.jsonPrimitive?.content ?: return
        when (type) {
            "success" -> complete(
                RazorpayResult.Success(
                    orderId = root["razorpay_order_id"]?.jsonPrimitive?.content.orEmpty(),
                    paymentId = root["razorpay_payment_id"]?.jsonPrimitive?.content.orEmpty(),
                    signature = root["razorpay_signature"]?.jsonPrimitive?.content.orEmpty()
                )
            )
            "failed" -> complete(
                RazorpayResult.Failed(
                    errorCode = root["code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    description = root["description"]?.jsonPrimitive?.content ?: "Payment failed"
                )
            )
            "dismiss" -> complete(RazorpayResult.Cancelled)
            else -> complete(RazorpayResult.Failed(0, "Unknown Razorpay message: $type"))
        }
    } catch (_: Throwable) {
        complete(RazorpayResult.Failed(0, "Invalid Razorpay callback payload"))
    }
}

private fun presentIosRazorpayWebCheckout(data: RazorpayCheckoutData, onResult: (RazorpayResult) -> Unit) {
    val root = findKeyWindow()?.rootViewController ?: run {
        onResult(RazorpayResult.Failed(0, "No key window — cannot present Razorpay checkout."))
        return
    }
    val presenter = topMostViewController(root)

    val html = buildCheckoutHtml(base64Utf8(data.toRazorpaySdkOptionsJson()))
    val messageName = "razorpayNative"

    var completed = false
    var modalRef: UIViewController? = null

    fun finish(result: RazorpayResult) {
        if (completed) return
        completed = true
        modalRef?.dismissViewControllerAnimated(true, completion = null)
        onResult(result)
    }

    val config = WKWebViewConfiguration()
    val userContent = config.userContentController
    val handler = RazorpayScriptHandler { text ->
        if (completed) return@RazorpayScriptHandler
        handleRazorpayMessage(text, ::finish)
    }
    userContent.addScriptMessageHandler(handler, messageName)

    val webFrameZero = cValue<CGRect> {
        origin.x = 0.0
        origin.y = 0.0
        size.width = 0.0
        size.height = 0.0
    }
    val webView = WKWebView(frame = webFrameZero, configuration = config).apply {
        translatesAutoresizingMaskIntoConstraints = true
        setOpaque(false)
        backgroundColor = UIColor.blackColor()
        scrollView.setScrollEnabled(false)
        autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
    }

    val modal = object : UIViewController(null, null) {
        override fun viewDidLoad() {
            super.viewDidLoad()
            view.setBackgroundColor(UIColor.blackColor())
            view.addSubview(webView)
            webView.loadHTMLString(html, baseURL = null)
        }

        override fun viewDidLayoutSubviews() {
            super.viewDidLayoutSubviews()
            view.bounds.useContents {
                val w = size.width
                val h = size.height
                val r = cValue<CGRect> {
                    origin.x = 0.0
                    origin.y = 0.0
                    size.width = w
                    size.height = h
                }
                webView.setFrame(r)
            }
        }

        override fun viewDidDisappear(animated: Boolean) {
            super.viewDidDisappear(animated)
            if (!completed) {
                completed = true
                onResult(RazorpayResult.Cancelled)
            }
        }
    }

    modalRef = modal
    modal.setModalPresentationStyle(platform.UIKit.UIModalPresentationFullScreen)
    presenter.presentViewController(modal, animated = true, completion = null)
}
