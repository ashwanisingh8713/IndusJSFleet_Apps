# screen-payment Module

## Purpose
SaaS subscription billing module for IndusJSFleet. Gates access to the app behind a paid plan selection and payment after successful sign-in.

**Not** to be confused with `screen-trip-payment` (trip-level customer receivables).

## Package
`com.ijs.subscription`

## Architecture
Clean Architecture + MVI. All layers are contained within this module:
- `domain/` — entities, repository interface, use cases
- `data/` — DTOs, mappers, remote data source, repository impl
- `presentation/` — MVI contracts, ViewModels, Compose screens, feature facade

## When It Appears
After successful login, `App.kt` calls `checkSubscriptionGate()` on `DefaultViewModelProvider`.
If the gate returns `RequiresPlanSelection` or `RequiresPayment`, the back stack is initialised
with `FleetRoute.SubscriptionPlans` instead of `FleetRoute.Dashboard`.

## Key APIs (proxied via Fleet → IAM)
- `GET /api/v1/plans` — public plan catalog
- `GET /api/v1/onboarding/status` — gate check (returns `step`, `payment_required`, etc.)
- `POST /api/v1/onboarding/plan` — select a plan
- `POST /api/v1/payments/orders` — create Razorpay order
- `POST /api/v1/payments/verify` — verify Razorpay payment signature

## Platform Payment Integration
Razorpay checkout is launched via a lambda callback passed from each platform:
- Android — Razorpay Android SDK (bridged via `AppActivity`)
- iOS — WebView fallback / Razorpay CocoaPod
- JS/WasmJS — `checkout.js` loaded in `index.html`

The `PaymentCheckoutScreen` accepts a `razorpayLauncher` lambda so no
platform-specific code leaks into `commonMain`.

**Design doc (canonical):** [../Docs/Razorpay/screen-payment_RAZORPAY_INTEGRATION.md](../Docs/Razorpay/screen-payment_RAZORPAY_INTEGRATION.md) — index: [../Docs/Razorpay/README.md](../Docs/Razorpay/README.md).

## Facade
`SubscriptionFeatureFacade` — provides `@Composable` entry points consumed by `sharedUI`.
