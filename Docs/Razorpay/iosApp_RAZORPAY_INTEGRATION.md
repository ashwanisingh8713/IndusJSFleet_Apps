# Razorpay — Integration & Design (`iosApp`)

**Module:** `iosApp`  
**Canonical path:** `Docs/Razorpay/iosApp_RAZORPAY_INTEGRATION.md`

The **`iosApp`** module is the **Xcode / iOS application shell** (Swift entry, plist, assets). **Razorpay checkout is not implemented here**; it lives in **`screen-payment` (`iosMain`)** and is wired through **`sharedUI`’s iOS `MainViewController`**.

---

## 1. Division of labor

| Layer | Razorpay-related work |
|-------|------------------------|
| **`iosApp`** | None in Kotlin/Swift source for checkout itself. Ensure the app can load **HTTPS** content (ATS) for WKWebView when used by `createIosRazorpayLauncher()`. |
| **`sharedUI/iosMain`** | `MainViewController` → `App(razorpayLauncher = createIosRazorpayLauncher())`. |
| **`screen-payment/iosMain`** | WKWebView + `https://checkout.razorpay.com/v1/checkout.js` + script message handler → `RazorpayResult`. |

So: **`iosApp` = host binary**; **Razorpay UX = KMP libraries** above it.

---

## 2. Host app concerns (still important)

### App Transport Security (ATS)

Checkout loads Razorpay scripts and payment UI over **HTTPS**. Default ATS usually allows this. If your `Info.plist` uses strict ATS exceptions, ensure:

- `checkout.razorpay.com` (and any Razorpay CDN domains the script pulls) are reachable, **or**
- Exceptions are justified and minimal (avoid global `NSAllowsArbitraryLoads` in production).

### Keychain / privacy strings

Razorpay flows may use camera (QR) or other features depending on payment method; add **Privacy usage descriptions** only if your integration enables those capabilities (follow Razorpay iOS / Apple guidelines for your SDK version).

---

## 3. Where to change behavior

| Need | Go to |
|------|--------|
| Change how checkout opens (WebView, messaging) | `screen-payment/.../RazorpayLauncher.ios.kt` |
| Change how `App` gets the launcher | `sharedUI/iosMain/kotlin/main.kt` |
| Bundle id, signing, Firebase, icons | `iosApp` |

---

## 4. Testing checklist (iOS)

- [ ] Simulator / device: open subscription checkout, complete test payment.
- [ ] Dismiss WebView / checkout → expect cancel path and correct VM state.
- [ ] Failed payment → `Failed` and snackbar / error from `PaymentCheckoutViewModel`.

---

## 5. Related documents

- [screen-payment_RAZORPAY_INTEGRATION.md](./screen-payment_RAZORPAY_INTEGRATION.md) — iOS WKWebView design.
- [sharedUI_RAZORPAY_INTEGRATION.md](./sharedUI_RAZORPAY_INTEGRATION.md) — `MainViewController` wiring.
- [../SCREEN_PAYMENT_PLAN.md](../SCREEN_PAYMENT_PLAN.md) — full payment product design.

---

## 6. Optional future: native Razorpay iOS SDK

If you later add **Razorpay’s CocoaPod** to `iosApp` and call it from Kotlin/Native, this document should be updated with Pod name, version pinning, and how callbacks map to the same `RazorpayResult` contract. Current implementation is **WebView + checkout.js** only.
