# Razorpay — Integration & Design (`webApp`)

**Module:** `webApp`  
**Canonical path:** `Docs/Razorpay/webApp_RAZORPAY_INTEGRATION.md`

`webApp` is the **browser executable** for Kotlin/JS and Kotlin/Wasm. It owns the **HTML shell** and the **JavaScript bridge** that Wasm (and optionally JS) use to call Razorpay Standard Checkout.

---

## 1. Responsibilities

| Item | Owner |
|------|--------|
| Load `checkout.js` before the app bundle | `webApp/src/commonMain/resources/index.html` |
| Expose `__IndusFleetRazorpay.open(payloadJson, onDone)` | Same `index.html` (inline script) |
| Map JS vs Wasm to `RazorpayLauncher` | `expect`/`actual` in `com.indusjs.fleet.web` |
| Pass launcher into shared `App()` | `webApp/.../main.kt` |

Gradle copies `index.html` to:

- `webApp/build/processedResources/js/main/index.html`
- `webApp/build/processedResources/wasmJs/main/index.html`

Both must keep **script order**: Razorpay CDN → bridge → `webApp.js`.

---

## 2. HTML bridge contract

`onDone` receives a **single JSON string** per checkout attempt (the bridge uses an internal `finish()` guard so success / dismiss / failed are mutually exclusive).

| `type` | Fields | Kotlin mapping |
|--------|--------|----------------|
| `success` | `razorpay_order_id`, `razorpay_payment_id`, `razorpay_signature` | `RazorpayResult.Success` |
| `failed` | `code` (number, optional), `description` | `RazorpayResult.Failed` |
| `dismiss` | — | `RazorpayResult.Cancelled` |

Parsed in **`screen-payment`** by `parseRazorpayWebBridgeResultJson` (shared by Wasm and by Kotlin/JS when the bridge is used).

---

## 3. Kotlin wiring

```
commonMain/kotlin/main.kt
    ComposeViewport { App(razorpayLauncher = platformRazorpayLauncher()) }

commonMain/.../PlatformRazorpay.kt
    expect fun platformRazorpayLauncher(): RazorpayLauncher

jsMain/.../PlatformRazorpay.js.kt
    actual → createWebRazorpayLauncher()   // screen-payment

wasmJsMain/.../PlatformRazorpay.wasmJs.kt
    actual → createWasmJsRazorpayLauncher()   // screen-payment → @JsFun → bridge
```

**Gradle:** `implementation(project(":screen-payment"))` in `webApp/build.gradle.kts` so factories and types resolve.

---

## 4. Kotlin/JS vs Wasm

| Target | Checkout path |
|--------|----------------|
| **JS** | If `__IndusFleetRazorpay.open` exists → bridge (same as Wasm). Else direct `new Razorpay(options)` fallback. |
| **Wasm** | Always `@JsFun` → `__IndusFleetRazorpay.open` (no direct `Razorpay` from Wasm). |

---

## 5. Operational requirements

- **HTTPS** in production (Razorpay and card networks).
- **CORS** on Fleet APIs for the web origin (`POST …/payments/orders`, `POST …/payments/verify`).
- **Ad blockers** may block `checkout.razorpay.com`; surface a clear error if `Razorpay` is undefined after script load.
- **CSP** (if added later) must allow script src for Razorpay checkout domain.

---

## 6. Local development

```bash
./gradlew :webApp:jsBrowserDevelopmentRun
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

Verify in DevTools **Network** that `checkout.js` loads **200** before `webApp.js`.

---

## 7. Related documents

- [screen-payment_RAZORPAY_INTEGRATION.md](./screen-payment_RAZORPAY_INTEGRATION.md) — JSON shape, `parseRazorpayWebBridgeResultJson`, JS/Wasm launchers.
- [sharedUI_RAZORPAY_INTEGRATION.md](./sharedUI_RAZORPAY_INTEGRATION.md) — `LocalRazorpayLauncher`, navigation.
- [../SCREEN_PAYMENT_PLAN.md](../SCREEN_PAYMENT_PLAN.md) — API catalog.

See also `webApp/AGENTS.md` for a shorter agent-oriented summary.
