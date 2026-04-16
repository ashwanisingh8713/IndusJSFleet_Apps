# Razorpay — Integration & Design (`sharedUI`)

**Module:** `sharedUI`  
**Canonical path:** `Docs/Razorpay/sharedUI_RAZORPAY_INTEGRATION.md`

`sharedUI` is the **Compose shell** that hosts navigation, `App()`, view-model providers, and **composition locals**. It does **not** embed Razorpay SDKs or `checkout.js`; it **routes** a platform-provided `RazorpayLauncher` into subscription checkout.

---

## 1. Responsibilities

| Responsibility | Where |
|----------------|--------|
| Provide `RazorpayLauncher` to deep composables | `CompositionLocalProvider(LocalRazorpayLauncher provides …)` in `App.kt` |
| Default when host omits launcher | `App(razorpayLauncher = { _, onResult -> onResult(RazorpayResult.Cancelled) })` — **always cancel**; real hosts must override. |
| Subscription checkout route | `FleetNavigation.kt` — `FleetRoute.SubscriptionCheckout` |
| DI for checkout ViewModel | `DefaultViewModelProvider.subscriptionCheckoutViewModel()` |
| **iOS** concrete launcher | `iosMain/kotlin/main.kt` — `createIosRazorpayLauncher()` passed into `App()` |

---

## 2. Data flow

```
Host (androidApp / webApp / iOS MainViewController)
    └── App(razorpayLauncher = platformLauncher)
            └── CompositionLocalProvider(LocalRazorpayLauncher provides razorpayLauncher)
                    └── fleetEntryProvider → SubscriptionCheckout
                            └── SubscriptionFeatureFacade.PaymentCheckoutEntry(..., razorpayLauncher = LocalRazorpayLauncher.current)
```

Any composable under `App()` can read `LocalRazorpayLauncher.current`; subscription checkout **must** use the same instance the host configured.

---

## 3. Key files

| File | Role |
|------|------|
| `App.kt` | `razorpayLauncher` parameter + `LocalRazorpayLauncher` provider. |
| `navigation/LocalRazorpayLauncher.kt` | `CompositionLocal` definition + KDoc for platform factories. |
| `navigation/FleetNavigation.kt` | Wires `razorpayLauncher = LocalRazorpayLauncher.current` into `PaymentCheckoutEntry`. |
| `di/DefaultViewModelProvider.kt` | `PaymentCheckoutViewModel` construction (`CreatePaymentOrderUseCase`, `VerifyPaymentUseCase`). |
| `di/ViewModelProvider.kt` | Interface declaring `subscriptionCheckoutViewModel()`. |
| `iosMain/kotlin/main.kt` | **iOS only:** `createIosRazorpayLauncher()` singleton passed to `App()`. |

---

## 4. Navigation contract

- **Route:** `FleetRoute.SubscriptionCheckout(planId, planName, prices, …, billingInterval)`.
- **Screen:** `SubscriptionFeatureFacade.PaymentCheckoutEntry` (from `screen-payment`).
- **Back:** `onNavigateBackToPlans`; **success:** `FleetRoute.SubscriptionSuccess`.

Changing route keys or parameters requires aligning with `screen-payment` facade and any deep-link builders.

---

## 5. Platform matrix (who sets `razorpayLauncher`)

| Host | Sets launcher |
|------|----------------|
| `androidApp` | `createAndroidRazorpayLauncher(activity)` |
| `webApp` | `platformRazorpayLauncher()` → JS / Wasm factories |
| iOS (`sharedUI` `iosMain`) | `createIosRazorpayLauncher()` |
| Previews / tests | Default stub (immediate cancel) unless overridden |

---

## 6. Design constraints

1. **No Razorpay imports** in `sharedUI/commonMain` — only `screen-payment` types (`RazorpayLauncher`, `RazorpayResult`).
2. **Single source of truth** for “which launcher” — the root `App()` call; avoid ad-hoc `remember { … }` launchers inside feature screens.
3. **Threading** — Razorpay callbacks arrive on the platform main / JS turn; `sendIntent` from callbacks must remain safe for the ViewModel’s dispatcher (current code invokes from effect collector on the composition coroutine context).

---

## 7. Troubleshooting

| Symptom | Check |
|---------|--------|
| Checkout never opens, instant “cancelled” | Host did not pass a real `razorpayLauncher` (default stub). |
| Opens on Android but not Web | `webApp` / `index.html` + `main.kt` — see [webApp_RAZORPAY_INTEGRATION.md](./webApp_RAZORPAY_INTEGRATION.md). |
| iOS opens blank | WKWebView / network / `screen-payment` iOS launcher logs. |

---

## 8. Related documents

- [screen-payment_RAZORPAY_INTEGRATION.md](./screen-payment_RAZORPAY_INTEGRATION.md)
- [webApp_RAZORPAY_INTEGRATION.md](./webApp_RAZORPAY_INTEGRATION.md)
- [androidApp_RAZORPAY_INTEGRATION.md](./androidApp_RAZORPAY_INTEGRATION.md)
- [iosApp_RAZORPAY_INTEGRATION.md](./iosApp_RAZORPAY_INTEGRATION.md)
