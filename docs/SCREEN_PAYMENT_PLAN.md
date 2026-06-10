# `screen-payment` Module — Full Planning, Design & Implementation Document

> **Author**: AI Architecture Analysis  
> **Date**: April 2026  
> **Status**: Pre-implementation  
> **Target Platforms**: Android · iOS · Web (JS + WasmJS)  
> **KMP Stack**: Kotlin 2.3.0 · Compose Multiplatform 1.10.0-rc01 · Ktor 3.3.3

---

## Table of Contents

1. [Overview & Problem Statement](#1-overview--problem-statement)
2. [User Journeys & All Use Cases](#2-user-journeys--all-use-cases)
3. [Backend API Reference](#3-backend-api-reference)
4. [Decision: Razorpay Integration Strategy](#4-decision-razorpay-integration-strategy)
5. [Architecture Design](#5-architecture-design)
6. [Complete File Tree](#6-complete-file-tree)
7. [Domain Layer — Entities, Repository, Use Cases](#7-domain-layer)
8. [Data Layer — DTOs, Mappers, DataSources, Repository Impl](#8-data-layer)
9. [Presentation Layer — MVI Contracts, ViewModels, Screens, Facade](#9-presentation-layer)
10. [Platform-Specific Implementations (expect/actual)](#10-platform-specific-implementations)
11. [Navigation Integration — FleetRoute + FleetNavigation](#11-navigation-integration)
12. [DI Integration — ViewModelProvider + FeatureRepositoryFactory](#12-di-integration)
13. [App.kt — Subscription Gate Hook](#13-appkt--subscription-gate-hook)
14. [`ijs-network-lib` Additions](#14-ijs-network-lib-additions)
15. [build.gradle.kts for `screen-payment`](#15-buildgradlekts)
16. [settings.gradle.kts Additions](#16-settingsgradlekts-additions)
17. [UI Design Specifications](#17-ui-design-specifications)
18. [Implementation Phases & Milestones](#18-implementation-phases--milestones)
19. [Open Questions & Risks](#19-open-questions--risks)
20. [Complete Code Implementation](#20-complete-code-implementation)

---

## 1. Overview & Problem Statement

### What is this module?

`screen-payment` is a **SaaS subscription billing module** that gates access to the IndusJSFleet app behind a paid plan. It is **not** the trip-level customer payment module (that is `screen-trip-payment`).

### When does it appear?

After successful **Sign In**, the app checks the user's subscription status via `GET /api/v1/onboarding/status`. If the response indicates payment is required, the user is redirected to this screen **before** reaching the Dashboard.

### Two Trigger Conditions

| Condition | `OnboardingStatusResponse` field | Description |
|-----------|----------------------------------|-------------|
| **First-time payment** | `step == "payment"` OR `payment_required == true && payment_done == false` | User signed up, selected a paid plan but never completed payment |
| **Subscription expired** | *(future)* `subscription_status == "expired"` OR `subscription_status == "past_due"` | Existing user whose active subscription has lapsed |

### Why a separate module?

- **Separation of concerns**: subscription lifecycle is owned by IAM, not Fleet domain
- **Feature boundary isolation**: follows the same pattern as all other `screen-*` modules (facade, MVI, Clean Architecture layers)
- **Independent testability**: can be unit-tested without the rest of the app graph
- **Future extensibility**: plan upgrades, annual billing switches, invoice history can all live here

---

## 2. User Journeys & All Use Cases

### 2.1 First-Time Paid Plan Selection & Payment

```
[User] → Sign In → (Auth Success)
       → App checks GET /onboarding/status
       → step == "plan"? → Show Plan Selection Screen
       → User selects plan + billing interval
       → POST /api/v1/onboarding/plan
       → step == "payment"? → Show Payment Screen
       → User taps "Pay Now"
       → POST /api/v1/payments/orders (creates Razorpay order)
       → Launch Razorpay Checkout (platform-specific)
       → User completes Razorpay Checkout
       → POST /api/v1/payments/verify (with signature)
       → Success → Navigate to Dashboard
```

### 2.2 Free Plan Selection (No Payment Required)

```
[User] → Sign In → (Auth Success)
       → App checks GET /onboarding/status
       → step == "plan"? → Show Plan Selection Screen
       → User selects FREE plan
       → POST /api/v1/onboarding/plan
       → payment_required == false → Skip payment
       → Navigate to Dashboard
```

### 2.3 Returning User — Plan Already Selected, Payment Pending

```
[User] → Sign In → (Auth Success)
       → App checks GET /onboarding/status
       → step == "payment", selected_plan is already set
       → Show Payment Summary Screen (plan already pre-selected)
       → User taps "Pay Now" → Razorpay flow → Dashboard
```

### 2.4 Subscription Expired (Renewal Flow)

```
[User] → Sign In → (Auth Success)
       → App checks subscription status
       → subscription expired/past_due
       → Show Subscription Renewal Screen
       → User can: upgrade plan / pay same plan again / contact support
       → Payment flow → Dashboard
```

### 2.5 Changing Billing Interval (Monthly ↔ Annual)

```
[User] → On Plan Selection Screen
       → Toggles "Monthly / Annual" switch
       → Prices update reactively (annual shows savings badge)
       → Selects plan → proceeds to payment
```

### 2.6 Payment Failure

```
[User] → Razorpay Checkout → Payment fails / user cancels
       → Returns to Payment Screen with error state
       → Can retry or choose different plan
```

### 2.7 Network Error / Retry

```
[User] → Payment screen loads
       → GET /plans fails (no network)
       → Show error state with retry CTA
       → User taps Retry → reload plans
```

### 2.8 Back Navigation Guard

```
[User] → On Payment screen
       → Presses back
       → Cannot navigate back to Dashboard (payment gate is enforced)
       → Can go back between plan selection and payment checkout steps
       → Can logout (clears session → Login screen)
```

---

## 3. Backend API Reference

All API calls go to Fleet backend (`/api/v1`), which proxies subscription/plan calls to IAM.

### 3.1 GET `/api/v1/onboarding/status` — Check Payment Gate

**Auth**: Bearer token required  
**Method**: GET

**Response `data`:**
```json
{
  "step": "payment",
  "email_verified": true,
  "mobile_verified": true,
  "plan_selected": true,
  "payment_done": false,
  "selected_plan": {
    "id": "uuid-string",
    "name": "Pro",
    "description": "For growing fleets",
    "monthly_price": 4900,
    "annual_price": 47040,
    "discount_percent": 20.0,
    "effective_monthly_price_annual": 3920,
    "annual_savings": 11760,
    "currency": "INR",
    "trial_days": 14,
    "features": ["Unlimited vehicles", "Driver management"],
    "feature_limits": [
      { "key": "max_team_members", "label": "Team Members", "value": 10, "unlimited": false }
    ],
    "is_active": true
  },
  "payment_required": true,
  "ready_to_create_tenant": false
}
```

**Step values**: `"verify"` | `"plan"` | `"payment"` | `"complete"`

**Gate logic**: Show payment screen if `step == "payment"` OR `(payment_required == true AND payment_done == false)`

---

### 3.2 GET `/api/v1/plans` — List Available Plans

**Auth**: None (public endpoint)  
**Method**: GET

**Response `data`:**
```json
{
  "plans": [
    {
      "id": "uuid",
      "name": "Free",
      "monthly_price": 0,
      "annual_price": 0,
      "currency": "INR",
      "trial_days": 0,
      "features": ["Up to 3 vehicles"],
      "feature_limits": [],
      "is_active": true
    },
    {
      "id": "uuid",
      "name": "Pro",
      "monthly_price": 4900,
      "annual_price": 47040,
      "discount_percent": 20.0,
      "effective_monthly_price_annual": 3920,
      "annual_savings": 11760,
      "currency": "INR",
      "trial_days": 14,
      "features": ["Unlimited vehicles", "Driver management", "Reports"],
      "feature_limits": [
        { "key": "max_team_members", "label": "Team Members", "value": 10, "unlimited": false }
      ]
    }
  ]
}
```

---

### 3.3 POST `/api/v1/onboarding/plan` — Select a Plan

**Auth**: Bearer required  
**Method**: POST

**Request body:**
```json
{ "plan_id": "uuid-string" }
```

**Response `data`:**
```json
{
  "message": "Plan selected. If free plan, you're ready to create tenant. If paid, proceed to payment."
}
```

---

### 3.4 POST `/api/v1/payments/orders` — Create Razorpay Order

**Auth**: Bearer required  
**Method**: POST

**Request body:**
```json
{
  "plan_id": "uuid-string",
  "billing_interval": "monthly"
}
```

`billing_interval`: `"monthly"` | `"annual"`

**Response `data`:**
```json
{
  "order_id": "order_AbC123xyz",
  "amount": 4900,
  "currency": "INR",
  "provider_key": "rzp_test_xxxxxxxxxx",
  "provider_name": "razorpay",
  "receipt_id": "rcpt_fleet_xxx",
  "customer_email": "user@example.com",
  "customer_name": "John Doe",
  "payment_id": "internal-uuid-of-payment-record"
}
```

---

### 3.5 POST `/api/v1/payments/verify` — Verify Razorpay Payment

**Auth**: Bearer required  
**Method**: POST

**Request body:**
```json
{
  "order_id": "order_AbC123xyz",
  "provider_payment_id": "pay_AbC123xyz",
  "signature": "hmac-sha256-signature-from-razorpay"
}
```

**Response `data`:**
```json
{
  "payment_id": "internal-uuid",
  "provider_id": "pay_AbC123xyz",
  "status": "captured",
  "method": "card",
  "amount": 4900,
  "currency": "INR",
  "plan_name": "Pro",
  "subscription_id": "uuid",
  "message": "Payment successful. Your Pro subscription is now active."
}
```

---

### 3.6 GET `/api/v1/payments` — List User Payments

**Auth**: Bearer required  

**Response `data`:** Array of `PaymentStatusResponse` (payment history)

---

### 3.7 GET `/api/v1/payments/:id` — Payment Detail

**Auth**: Bearer required  

**Response `data`:** Single `PaymentStatusResponse`

---

## 4. Decision: Razorpay Integration Strategy

### The Challenge

Razorpay Checkout is a native SDK for Android and iOS, and a JavaScript library for Web. In KMP, there is no single cross-platform Razorpay library.

### Decision: `expect/actual` PaymentLauncher

We define a `PaymentLauncher` interface in `commonMain` and provide platform-specific `actual` implementations:

| Platform | Implementation |
|----------|---------------|
| **Android** | Razorpay Android SDK (`com.razorpay:checkout`) via `startPaymentForResult` |
| **iOS** | Razorpay iOS SDK via `RazorpayCheckout.open(options)` from CocoaPods |
| **JS/WasmJS** | Razorpay `checkout.js` loaded via `<script>` tag + `Razorpay(options).open()` |

### Stub Strategy (Dev/Test)

- When `PAYMENT_PROVIDER=stub` (dev env), the backend accepts any payment — we can simulate success via a mock `provider_payment_id` and `signature`.
- Dev/test builds provide a **stub `PaymentLauncher`** that immediately returns fake Razorpay callback data without opening a real checkout.

### Flow Sequence with expect/actual

```
ViewModel: CreateOrderIntent
  → POST /payments/orders → gets {order_id, amount, provider_key, ...}
  → sendEffect(LaunchRazorpayCheckout(orderDetails))

Screen: collects Effect
  → calls platformPaymentLauncher.launch(orderDetails, callback)

Callback returns:
  - Success(orderId, paymentId, signature) → viewModel.sendIntent(VerifyPayment(...))
  - Failed(errorCode, message) → viewModel.sendIntent(PaymentFailed(message))
  - Cancelled → viewModel.sendIntent(PaymentCancelled)

ViewModel: POST /payments/verify
  → Success → sendEffect(NavigateToDashboard)
  → Error → updateState { error = ... }
```

---

## 5. Architecture Design

### Module Dependency Graph

```
screen-payment
    ├── api(ijs-network-lib)        ← SubscriptionRepository interface lives here
    ├── implementation(ijs-ui-components-lib)
    └── implementation(ijs-core-lib)
```

### Layer Breakdown

```
screen-payment/src/commonMain/kotlin/com/ijs/payment/
│
├── domain/
│   ├── entity/
│   │   ├── Plan.kt                     ← Domain model for a subscription plan
│   │   ├── FeatureLimit.kt             ← Plan feature limit (key, label, value)
│   │   ├── BillingInterval.kt          ← Enum: MONTHLY, ANNUAL
│   │   ├── OnboardingStatus.kt         ← Onboarding check result domain entity
│   │   ├── PaymentOrder.kt             ← Created Razorpay order domain entity
│   │   └── PaymentResult.kt            ← Verified payment result domain entity
│   ├── repository/
│   │   └── SubscriptionRepository.kt   ← Interface
│   └── usecase/
│       ├── GetOnboardingStatusUseCase.kt
│       ├── GetPlansUseCase.kt
│       ├── SelectPlanUseCase.kt
│       ├── CreatePaymentOrderUseCase.kt
│       └── VerifyPaymentUseCase.kt
│
├── data/
│   ├── model/
│   │   ├── SubscriptionDto.kt          ← All API DTOs in one file
│   │   └── SubscriptionRequests.kt     ← All request bodies
│   ├── mapper/
│   │   └── SubscriptionMapper.kt       ← DTO → Domain mappers
│   ├── datasource/
│   │   ├── SubscriptionRemoteDataSource.kt     ← Interface + Impl
│   └── repository/
│       └── SubscriptionRepositoryImpl.kt
│
└── presentation/
    ├── PaymentFeatureFacade.kt
    ├── plans/
    │   ├── PlansContract.kt
    │   ├── PlansViewModel.kt
    │   └── PlansScreen.kt
    ├── checkout/
    │   ├── PaymentCheckoutContract.kt
    │   ├── PaymentCheckoutViewModel.kt
    │   └── PaymentCheckoutScreen.kt
    ├── success/
    │   ├── PaymentSuccessContract.kt
    │   ├── PaymentSuccessViewModel.kt  (optional — can be stateless)
    │   └── PaymentSuccessScreen.kt
    └── components/
        ├── PlanCard.kt
        ├── BillingToggle.kt
        ├── FeatureLimitRow.kt
        └── PriceDisplay.kt
```

### Where Domain + Data Live

Following the existing codebase pattern where `screen-user` presentation is in `screen-user` but domain+data live in `ijs-network-lib`:

| Layer | Location | Reason |
|-------|----------|--------|
| **Domain entities** | `screen-payment/commonMain` | Subscription entities are payment-module-specific |
| **Repository interface** | `ijs-network-lib` (add to existing pattern) | Shared across modules if needed, follows existing convention |
| **DTOs + Mappers + DataSource + RepoImpl** | `ijs-network-lib` | Keeps all network code in the network lib |
| **Presentation (VM + Screen + Facade)** | `screen-payment/commonMain` | Feature-module-owned UI code |

> **Alternative**: Since subscription is entirely owned by this module and not cross-feature, you could keep ALL layers within `screen-payment`. This is acceptable and simpler. This document uses the **in-module approach** for domain + presentation, and `ijs-network-lib` only for DataSource + RepoImpl (matching how other feature modules like `screen-vehicle` work).

---

## 6. Complete File Tree

```
screen-payment/
├── build.gradle.kts
├── AGENTS.md
└── src/
    ├── commonMain/
    │   └── kotlin/
    │       └── com/ijs/payment/
    │           ├── LogTags.kt
    │           │
    │           ├── domain/
    │           │   ├── entity/
    │           │   │   ├── Plan.kt
    │           │   │   ├── FeatureLimit.kt
    │           │   │   ├── BillingInterval.kt
    │           │   │   ├── OnboardingStatus.kt
    │           │   │   ├── PaymentOrder.kt
    │           │   │   └── PaymentResult.kt
    │           │   ├── repository/
    │           │   │   └── SubscriptionRepository.kt
    │           │   └── usecase/
    │           │       ├── GetOnboardingStatusUseCase.kt
    │           │       ├── GetPlansUseCase.kt
    │           │       ├── SelectPlanUseCase.kt
    │           │       ├── CreatePaymentOrderUseCase.kt
    │           │       └── VerifyPaymentUseCase.kt
    │           │
    │           ├── data/
    │           │   ├── model/
    │           │   │   ├── SubscriptionDto.kt
    │           │   │   └── SubscriptionRequests.kt
    │           │   ├── mapper/
    │           │   │   └── SubscriptionMapper.kt
    │           │   ├── datasource/
    │           │   │   └── SubscriptionRemoteDataSource.kt
    │           │   └── repository/
    │           │       └── SubscriptionRepositoryImpl.kt
    │           │
    │           └── presentation/
    │               ├── PaymentFeatureFacade.kt
    │               ├── plans/
    │               │   ├── PlansContract.kt
    │               │   ├── PlansViewModel.kt
    │               │   └── PlansScreen.kt
    │               ├── checkout/
    │               │   ├── PaymentCheckoutContract.kt
    │               │   ├── PaymentCheckoutViewModel.kt
    │               │   └── PaymentCheckoutScreen.kt
    │               ├── success/
    │               │   └── PaymentSuccessScreen.kt
    │               └── components/
    │                   ├── PlanCard.kt
    │                   ├── BillingToggle.kt
    │                   ├── FeatureLimitRow.kt
    │                   └── PriceDisplay.kt
    │
    ├── androidMain/
    │   └── kotlin/
    │       └── com/ijs/payment/
    │           └── platform/
    │               └── RazorpayLauncher.android.kt
    │
    ├── iosMain/
    │   └── kotlin/
    │       └── com/ijs/payment/
    │           └── platform/
    │               └── RazorpayLauncher.ios.kt
    │
    ├── jsMain/
    │   └── kotlin/
    │       └── com/ijs/payment/
    │           └── platform/
    │               └── RazorpayLauncher.js.kt
    │
    └── wasmJsMain/
        └── kotlin/
            └── com/ijs/payment/
                └── platform/
                    └── RazorpayLauncher.wasmJs.kt
```

**Additions to `ijs-network-lib`:**
```
ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/
├── core/network/
│   └── ApiConfig.kt                        ← Add new endpoint constants
├── domain/repository/subscription/
│   └── SubscriptionRepository.kt           ← (moved here if cross-feature needed)
├── data/
│   ├── model/subscription/
│   │   ├── SubscriptionDto.kt
│   │   └── SubscriptionRequests.kt
│   ├── mapper/subscription/
│   │   └── SubscriptionMapper.kt
│   ├── datasource/subscription/
│   │   └── SubscriptionRemoteDataSource.kt
│   └── repository/subscription/
│       └── SubscriptionRepositoryImpl.kt
```

**Additions to `sharedUI`:**
```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── navigation/
│   ├── FleetRoute.kt                        ← Add SubscriptionPlans, PaymentCheckout, PaymentSuccess
│   └── FleetNavigation.kt                   ← Wire new routes
├── di/
│   ├── ViewModelProvider.kt                 ← Add 2 new factory methods
│   ├── DefaultViewModelProvider.kt          ← Add SubscriptionRepository + VM construction
│   └── FeatureRepositoryFactory.kt          ← Add subscriptionRepository
└── App.kt                                   ← Add subscription gate after isLoggedIn
```

---

## 7. Domain Layer

### 7.1 `Plan.kt`

```kotlin
package com.ijs.payment.domain.entity

data class Plan(
    val id: String,
    val name: String,
    val description: String,
    val monthlyPrice: Long,      // in smallest currency unit (paise for INR)
    val annualPrice: Long,
    val discountPercent: Double,
    val effectiveMonthlyPriceAnnual: Long,
    val annualSavings: Long,
    val currency: String,
    val trialDays: Int,
    val features: List<String>,
    val featureLimits: List<FeatureLimit>,
    val isActive: Boolean
) {
    val isFree: Boolean get() = monthlyPrice == 0L && annualPrice == 0L

    fun priceFor(interval: BillingInterval): Long = when (interval) {
        BillingInterval.MONTHLY -> monthlyPrice
        BillingInterval.ANNUAL -> annualPrice
    }

    fun formattedPrice(interval: BillingInterval, currency: String = this.currency): String {
        val amount = priceFor(interval)
        return if (amount == 0L) "Free" else "${currency} ${amount / 100.0}"
    }
}
```

### 7.2 `FeatureLimit.kt`

```kotlin
package com.ijs.payment.domain.entity

data class FeatureLimit(
    val key: String,
    val label: String,
    val value: Long,          // -1 = boolean on; positive = quota
    val unlimited: Boolean
) {
    val displayValue: String get() = when {
        unlimited -> "Unlimited"
        value == -1L -> "✓"
        else -> value.toString()
    }
}
```

### 7.3 `BillingInterval.kt`

```kotlin
package com.ijs.payment.domain.entity

enum class BillingInterval(val apiValue: String, val label: String) {
    MONTHLY("monthly", "Monthly"),
    ANNUAL("annual", "Annual")
}
```

### 7.4 `OnboardingStatus.kt`

```kotlin
package com.ijs.payment.domain.entity

data class OnboardingStatus(
    val step: OnboardingStep,
    val emailVerified: Boolean,
    val mobileVerified: Boolean,
    val planSelected: Boolean,
    val paymentDone: Boolean,
    val selectedPlan: Plan?,
    val paymentRequired: Boolean,
    val readyToCreateTenant: Boolean
) {
    val needsPlanSelection: Boolean get() = step == OnboardingStep.PLAN
    val needsPayment: Boolean get() = step == OnboardingStep.PAYMENT
            || (paymentRequired && !paymentDone)
    val isComplete: Boolean get() = step == OnboardingStep.COMPLETE
}

enum class OnboardingStep(val apiValue: String) {
    VERIFY("verify"),
    PLAN("plan"),
    PAYMENT("payment"),
    COMPLETE("complete");

    companion object {
        fun from(value: String): OnboardingStep =
            entries.firstOrNull { it.apiValue == value } ?: COMPLETE
    }
}
```

### 7.5 `PaymentOrder.kt`

```kotlin
package com.ijs.payment.domain.entity

data class PaymentOrder(
    val orderId: String,
    val internalPaymentId: String,
    val amount: Long,
    val currency: String,
    val providerKey: String,    // Razorpay key_id for SDK init
    val providerName: String,
    val receiptId: String,
    val customerEmail: String,
    val customerName: String
)
```

### 7.6 `PaymentResult.kt`

```kotlin
package com.ijs.payment.domain.entity

data class PaymentResult(
    val internalPaymentId: String,
    val providerPaymentId: String,
    val status: String,
    val method: String,
    val amount: Long,
    val currency: String,
    val planName: String,
    val subscriptionId: String,
    val message: String
)
```

### 7.7 `SubscriptionRepository.kt` (Interface)

```kotlin
package com.ijs.payment.domain.repository

import com.ijs.payment.domain.entity.BillingInterval
import com.ijs.payment.domain.entity.OnboardingStatus
import com.ijs.payment.domain.entity.PaymentOrder
import com.ijs.payment.domain.entity.PaymentResult
import com.ijs.payment.domain.entity.Plan

interface SubscriptionRepository {
    suspend fun getOnboardingStatus(): Result<OnboardingStatus>
    suspend fun getPlans(): Result<List<Plan>>
    suspend fun selectPlan(planId: String): Result<Unit>
    suspend fun createPaymentOrder(planId: String, billingInterval: BillingInterval): Result<PaymentOrder>
    suspend fun verifyPayment(orderId: String, providerPaymentId: String, signature: String): Result<PaymentResult>
}
```

### 7.8 Use Cases

```kotlin
// GetOnboardingStatusUseCase.kt
class GetOnboardingStatusUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(): Result<OnboardingStatus> =
        repository.getOnboardingStatus()
}

// GetPlansUseCase.kt
class GetPlansUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(): Result<List<Plan>> =
        repository.getPlans()
}

// SelectPlanUseCase.kt
class SelectPlanUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.selectPlan(planId)
}

// CreatePaymentOrderUseCase.kt
class CreatePaymentOrderUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(planId: String, interval: BillingInterval): Result<PaymentOrder> =
        repository.createPaymentOrder(planId, interval)
}

// VerifyPaymentUseCase.kt
class VerifyPaymentUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(
        orderId: String,
        providerPaymentId: String,
        signature: String
    ): Result<PaymentResult> =
        repository.verifyPayment(orderId, providerPaymentId, signature)
}
```

---

## 8. Data Layer

### 8.1 `SubscriptionDto.kt` — All API Response DTOs

```kotlin
package com.ijs.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlansResponseDto(
    @SerialName("plans") val plans: List<PlanDto> = emptyList()
)

@Serializable
data class PlanDto(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("monthly_price") val monthlyPrice: Long = 0,
    @SerialName("annual_price") val annualPrice: Long = 0,
    @SerialName("discount_percent") val discountPercent: Double = 0.0,
    @SerialName("effective_monthly_price_annual") val effectiveMonthlyPriceAnnual: Long = 0,
    @SerialName("annual_savings") val annualSavings: Long = 0,
    @SerialName("currency") val currency: String = "INR",
    @SerialName("trial_days") val trialDays: Int = 0,
    @SerialName("features") val features: List<String> = emptyList(),
    @SerialName("feature_limits") val featureLimits: List<FeatureLimitDto> = emptyList(),
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class FeatureLimitDto(
    @SerialName("key") val key: String = "",
    @SerialName("label") val label: String = "",
    @SerialName("value") val value: Long = 0,
    @SerialName("unlimited") val unlimited: Boolean = false
)

@Serializable
data class OnboardingStatusDto(
    @SerialName("step") val step: String = "complete",
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("mobile_verified") val mobileVerified: Boolean = false,
    @SerialName("plan_selected") val planSelected: Boolean = false,
    @SerialName("payment_done") val paymentDone: Boolean = false,
    @SerialName("selected_plan") val selectedPlan: PlanDto? = null,
    @SerialName("payment_required") val paymentRequired: Boolean = false,
    @SerialName("ready_to_create_tenant") val readyToCreateTenant: Boolean = false
)

@Serializable
data class PaymentOrderDto(
    @SerialName("order_id") val orderId: String = "",
    @SerialName("payment_id") val internalPaymentId: String = "",
    @SerialName("amount") val amount: Long = 0,
    @SerialName("currency") val currency: String = "INR",
    @SerialName("provider_key") val providerKey: String = "",
    @SerialName("provider_name") val providerName: String = "",
    @SerialName("receipt_id") val receiptId: String = "",
    @SerialName("customer_email") val customerEmail: String = "",
    @SerialName("customer_name") val customerName: String = ""
)

@Serializable
data class PaymentVerifyResponseDto(
    @SerialName("payment_id") val paymentId: String = "",
    @SerialName("provider_id") val providerId: String = "",
    @SerialName("status") val status: String = "",
    @SerialName("method") val method: String = "",
    @SerialName("amount") val amount: Long = 0,
    @SerialName("currency") val currency: String = "",
    @SerialName("plan_name") val planName: String = "",
    @SerialName("subscription_id") val subscriptionId: String = "",
    @SerialName("message") val message: String = ""
)

// Generic API wrapper (re-use from ijs-core-lib pattern)
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null
)
```

### 8.2 `SubscriptionRequests.kt` — Request Bodies

```kotlin
package com.ijs.payment.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelectPlanRequest(
    @SerialName("plan_id") val planId: String
)

@Serializable
data class CreatePaymentOrderRequest(
    @SerialName("plan_id") val planId: String,
    @SerialName("billing_interval") val billingInterval: String   // "monthly" | "annual"
)

@Serializable
data class VerifyPaymentRequest(
    @SerialName("order_id") val orderId: String,
    @SerialName("provider_payment_id") val providerPaymentId: String,
    @SerialName("signature") val signature: String
)
```

### 8.3 `SubscriptionMapper.kt`

```kotlin
package com.ijs.payment.data.mapper

import com.ijs.payment.data.model.*
import com.ijs.payment.domain.entity.*

object SubscriptionMapper {

    fun PlanDto.toDomain(): Plan = Plan(
        id = id,
        name = name,
        description = description,
        monthlyPrice = monthlyPrice,
        annualPrice = annualPrice,
        discountPercent = discountPercent,
        effectiveMonthlyPriceAnnual = effectiveMonthlyPriceAnnual,
        annualSavings = annualSavings,
        currency = currency,
        trialDays = trialDays,
        features = features,
        featureLimits = featureLimits.map { it.toDomain() },
        isActive = isActive
    )

    fun FeatureLimitDto.toDomain(): FeatureLimit = FeatureLimit(
        key = key,
        label = label,
        value = value,
        unlimited = unlimited
    )

    fun OnboardingStatusDto.toDomain(): OnboardingStatus = OnboardingStatus(
        step = OnboardingStep.from(step),
        emailVerified = emailVerified,
        mobileVerified = mobileVerified,
        planSelected = planSelected,
        paymentDone = paymentDone,
        selectedPlan = selectedPlan?.toDomain(),
        paymentRequired = paymentRequired,
        readyToCreateTenant = readyToCreateTenant
    )

    fun PaymentOrderDto.toDomain(): PaymentOrder = PaymentOrder(
        orderId = orderId,
        internalPaymentId = internalPaymentId,
        amount = amount,
        currency = currency,
        providerKey = providerKey,
        providerName = providerName,
        receiptId = receiptId,
        customerEmail = customerEmail,
        customerName = customerName
    )

    fun PaymentVerifyResponseDto.toDomain(): PaymentResult = PaymentResult(
        internalPaymentId = paymentId,
        providerPaymentId = providerId,
        status = status,
        method = method,
        amount = amount,
        currency = currency,
        planName = planName,
        subscriptionId = subscriptionId,
        message = message
    )
}
```

### 8.4 `SubscriptionRemoteDataSource.kt`

```kotlin
package com.ijs.payment.data.datasource

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.payment.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface SubscriptionRemoteDataSource {
    suspend fun getOnboardingStatus(token: String): ApiResponse<OnboardingStatusDto>
    suspend fun getPlans(): ApiResponse<PlansResponseDto>
    suspend fun selectPlan(token: String, request: SelectPlanRequest): ApiResponse<Unit>
    suspend fun createPaymentOrder(token: String, request: CreatePaymentOrderRequest): ApiResponse<PaymentOrderDto>
    suspend fun verifyPayment(token: String, request: VerifyPaymentRequest): ApiResponse<PaymentVerifyResponseDto>
}

class SubscriptionRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : SubscriptionRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL
    private val tag = "SubscriptionRemoteDS"

    override suspend fun getOnboardingStatus(token: String): ApiResponse<OnboardingStatusDto> {
        logger.d(tag, "Fetching onboarding status")
        return httpClient.get("$baseUrl${ApiConfig.Endpoints.ONBOARDING_STATUS}") {
            bearerAuth(token)
        }.body()
    }

    override suspend fun getPlans(): ApiResponse<PlansResponseDto> {
        logger.d(tag, "Fetching plans")
        return httpClient.get("$baseUrl${ApiConfig.Endpoints.PLANS}").body()
    }

    override suspend fun selectPlan(token: String, request: SelectPlanRequest): ApiResponse<Unit> {
        logger.d(tag, "Selecting plan: ${request.planId}")
        return httpClient.post("$baseUrl${ApiConfig.Endpoints.SELECT_PLAN}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun createPaymentOrder(
        token: String,
        request: CreatePaymentOrderRequest
    ): ApiResponse<PaymentOrderDto> {
        logger.d(tag, "Creating payment order for plan: ${request.planId}")
        return httpClient.post("$baseUrl${ApiConfig.Endpoints.PAYMENT_ORDERS}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun verifyPayment(
        token: String,
        request: VerifyPaymentRequest
    ): ApiResponse<PaymentVerifyResponseDto> {
        logger.d(tag, "Verifying payment for order: ${request.orderId}")
        return httpClient.post("$baseUrl${ApiConfig.Endpoints.PAYMENT_VERIFY}") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
```

### 8.5 `SubscriptionRepositoryImpl.kt`

```kotlin
package com.ijs.payment.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.payment.data.datasource.SubscriptionRemoteDataSource
import com.ijs.payment.data.mapper.SubscriptionMapper.toDomain
import com.ijs.payment.data.model.CreatePaymentOrderRequest
import com.ijs.payment.data.model.SelectPlanRequest
import com.ijs.payment.data.model.VerifyPaymentRequest
import com.ijs.payment.domain.entity.BillingInterval
import com.ijs.payment.domain.entity.OnboardingStatus
import com.ijs.payment.domain.entity.PaymentOrder
import com.ijs.payment.domain.entity.PaymentResult
import com.ijs.payment.domain.entity.Plan
import com.ijs.payment.domain.repository.SubscriptionRepository
import com.indusjs.fleet.core.exception.ApiException

class SubscriptionRepositoryImpl(
    private val remoteDataSource: SubscriptionRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : SubscriptionRepository {

    private val tag = "SubscriptionRepo"

    private suspend fun requireToken(): String =
        userLocalDataSource.getAuthToken()
            ?: throw ApiException("Not authenticated. Please log in again.")

    override suspend fun getOnboardingStatus(): Result<OnboardingStatus> = runCatching {
        val token = requireToken()
        val response = remoteDataSource.getOnboardingStatus(token)
        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to get onboarding status")
    }

    override suspend fun getPlans(): Result<List<Plan>> = runCatching {
        val response = remoteDataSource.getPlans()
        response.data?.plans?.map { it.toDomain() }
            ?: throw ApiException(response.message ?: "Failed to load plans")
    }

    override suspend fun selectPlan(planId: String): Result<Unit> = runCatching {
        val token = requireToken()
        remoteDataSource.selectPlan(token, SelectPlanRequest(planId = planId))
        Unit
    }

    override suspend fun createPaymentOrder(
        planId: String,
        billingInterval: BillingInterval
    ): Result<PaymentOrder> = runCatching {
        val token = requireToken()
        val response = remoteDataSource.createPaymentOrder(
            token,
            CreatePaymentOrderRequest(planId = planId, billingInterval = billingInterval.apiValue)
        )
        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to create payment order")
    }

    override suspend fun verifyPayment(
        orderId: String,
        providerPaymentId: String,
        signature: String
    ): Result<PaymentResult> = runCatching {
        val token = requireToken()
        val response = remoteDataSource.verifyPayment(
            token,
            VerifyPaymentRequest(
                orderId = orderId,
                providerPaymentId = providerPaymentId,
                signature = signature
            )
        )
        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Payment verification failed")
    }
}
```

---

## 9. Presentation Layer

### 9.1 `PlansContract.kt`

```kotlin
package com.ijs.payment.presentation.plans

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.payment.domain.entity.BillingInterval
import com.ijs.payment.domain.entity.Plan

object PlansContract {

    data class State(
        val isLoading: Boolean = true,
        val plans: List<Plan> = emptyList(),
        val selectedPlan: Plan? = null,
        val billingInterval: BillingInterval = BillingInterval.MONTHLY,
        val preSelectedPlan: Plan? = null,      // from onboarding status (already chosen)
        val isSelectingPlan: Boolean = false,   // loading state while POST /onboarding/plan
        val error: UiText? = null,
        val isRenewal: Boolean = false          // true when expired subscription flow
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadPlans : Intent
        data class SelectPlan(val plan: Plan) : Intent
        data class ChangeBillingInterval(val interval: BillingInterval) : Intent
        data object ConfirmPlanSelection : Intent
        data object RetryLoad : Intent
        data object Logout : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToPaymentCheckout(
            val plan: Plan,
            val billingInterval: BillingInterval
        ) : Effect
        data object NavigateToDashboard : Effect   // free plan selected
        data object NavigateToLogin : Effect       // logout
        data class ShowError(val message: UiText) : Effect
    }
}
```

### 9.2 `PlansViewModel.kt`

```kotlin
package com.ijs.payment.presentation.plans

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.payment.domain.entity.BillingInterval
import com.ijs.payment.domain.entity.OnboardingStatus
import com.ijs.payment.domain.usecase.GetOnboardingStatusUseCase
import com.ijs.payment.domain.usecase.GetPlansUseCase
import com.ijs.payment.domain.usecase.SelectPlanUseCase
import com.ijs.payment.presentation.plans.PlansContract.*
import kotlinx.coroutines.withContext

class PlansViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getPlansUseCase: GetPlansUseCase,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    private val selectPlanUseCase: SelectPlanUseCase,
    private val isRenewal: Boolean = false
) : MviViewModel<State, Intent, Effect>(State(isRenewal = isRenewal)) {

    init {
        sendIntent(Intent.LoadPlans)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadPlans, is Intent.RetryLoad -> loadPlans()
            is Intent.SelectPlan -> updateState { copy(selectedPlan = intent.plan) }
            is Intent.ChangeBillingInterval -> updateState { copy(billingInterval = intent.interval) }
            is Intent.ConfirmPlanSelection -> confirmSelection()
            is Intent.Logout -> sendEffect(Effect.NavigateToLogin)
        }
    }

    private suspend fun loadPlans() {
        updateState { copy(isLoading = true, error = null) }
        withContext(dispatcherProvider.io) {
            val plansResult = getPlansUseCase()
            val statusResult = getOnboardingStatusUseCase()

            val plans = plansResult.getOrNull() ?: emptyList()
            val status: OnboardingStatus? = statusResult.getOrNull()

            if (plansResult.isFailure) {
                updateState {
                    copy(
                        isLoading = false,
                        error = UiText.DynamicString(
                            plansResult.exceptionOrNull()?.message ?: "Failed to load plans"
                        )
                    )
                }
                return@withContext
            }

            val preSelectedPlan = status?.selectedPlan
            updateState {
                copy(
                    isLoading = false,
                    plans = plans.filter { it.isActive },
                    preSelectedPlan = preSelectedPlan,
                    selectedPlan = preSelectedPlan ?: plans.firstOrNull { !it.isFree }
                )
            }
        }
    }

    private suspend fun confirmSelection() {
        val plan = state.value.selectedPlan ?: return
        val interval = state.value.billingInterval

        if (plan.isFree) {
            updateState { copy(isSelectingPlan = true) }
            withContext(dispatcherProvider.io) {
                selectPlanUseCase(plan.id).fold(
                    onSuccess = {
                        updateState { copy(isSelectingPlan = false) }
                        sendEffect(Effect.NavigateToDashboard)
                    },
                    onFailure = { e ->
                        updateState {
                            copy(isSelectingPlan = false, error = UiText.DynamicString(e.message ?: "Error"))
                        }
                        sendEffect(Effect.ShowError(UiText.DynamicString(e.message ?: "Error selecting plan")))
                    }
                )
            }
        } else {
            withContext(dispatcherProvider.io) {
                selectPlanUseCase(plan.id).fold(
                    onSuccess = { sendEffect(Effect.NavigateToPaymentCheckout(plan, interval)) },
                    onFailure = { e ->
                        sendEffect(Effect.ShowError(UiText.DynamicString(e.message ?: "Error")))
                    }
                )
            }
        }
    }
}
```

### 9.3 `PaymentCheckoutContract.kt`

```kotlin
package com.ijs.payment.presentation.checkout

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.payment.domain.entity.BillingInterval
import com.ijs.payment.domain.entity.PaymentOrder
import com.ijs.payment.domain.entity.Plan

object PaymentCheckoutContract {

    data class State(
        val plan: Plan? = null,
        val billingInterval: BillingInterval = BillingInterval.MONTHLY,
        val isCreatingOrder: Boolean = false,
        val isVerifyingPayment: Boolean = false,
        val error: UiText? = null,
        val paymentOrder: PaymentOrder? = null  // populated after order creation
    ) : UiState

    sealed interface Intent : UiIntent {
        data class Initialize(val plan: Plan, val interval: BillingInterval) : Intent
        data object InitiatePayment : Intent
        data class RazorpaySuccess(
            val orderId: String,
            val providerPaymentId: String,
            val signature: String
        ) : Intent
        data class RazorpayFailed(val errorCode: Int, val errorDescription: String) : Intent
        data object RazorpayCancelled : Intent
        data object ChangePlan : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data class LaunchRazorpayCheckout(val order: PaymentOrder) : Effect
        data class NavigateToSuccess(val planName: String, val amount: Long, val currency: String) : Effect
        data object NavigateBackToPlans : Effect
        data class ShowError(val message: UiText) : Effect
    }
}
```

### 9.4 `PaymentCheckoutViewModel.kt`

```kotlin
package com.ijs.payment.presentation.checkout

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.payment.domain.usecase.CreatePaymentOrderUseCase
import com.ijs.payment.domain.usecase.VerifyPaymentUseCase
import com.ijs.payment.presentation.checkout.PaymentCheckoutContract.*
import kotlinx.coroutines.withContext

class PaymentCheckoutViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val createPaymentOrderUseCase: CreatePaymentOrderUseCase,
    private val verifyPaymentUseCase: VerifyPaymentUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Initialize -> updateState {
                copy(plan = intent.plan, billingInterval = intent.interval)
            }
            is Intent.InitiatePayment -> initiatePayment()
            is Intent.RazorpaySuccess -> verifyPayment(
                intent.orderId, intent.providerPaymentId, intent.signature
            )
            is Intent.RazorpayFailed -> handlePaymentFailed(intent.errorCode, intent.errorDescription)
            is Intent.RazorpayCancelled -> updateState {
                copy(
                    isCreatingOrder = false,
                    isVerifyingPayment = false,
                    error = UiText.DynamicString("Payment was cancelled.")
                )
            }
            is Intent.ChangePlan -> sendEffect(Effect.NavigateBackToPlans)
            is Intent.DismissError -> updateState { copy(error = null) }
        }
    }

    private suspend fun initiatePayment() {
        val plan = state.value.plan ?: return
        val interval = state.value.billingInterval
        updateState { copy(isCreatingOrder = true, error = null) }
        withContext(dispatcherProvider.io) {
            createPaymentOrderUseCase(plan.id, interval).fold(
                onSuccess = { order ->
                    updateState { copy(isCreatingOrder = false, paymentOrder = order) }
                    sendEffect(Effect.LaunchRazorpayCheckout(order))
                },
                onFailure = { e ->
                    updateState {
                        copy(
                            isCreatingOrder = false,
                            error = UiText.DynamicString(e.message ?: "Failed to initiate payment")
                        )
                    }
                }
            )
        }
    }

    private suspend fun verifyPayment(orderId: String, paymentId: String, signature: String) {
        updateState { copy(isVerifyingPayment = true, error = null) }
        withContext(dispatcherProvider.io) {
            verifyPaymentUseCase(orderId, paymentId, signature).fold(
                onSuccess = { result ->
                    updateState { copy(isVerifyingPayment = false) }
                    sendEffect(Effect.NavigateToSuccess(result.planName, result.amount, result.currency))
                },
                onFailure = { e ->
                    updateState {
                        copy(
                            isVerifyingPayment = false,
                            error = UiText.DynamicString(e.message ?: "Payment verification failed")
                        )
                    }
                }
            )
        }
    }

    private suspend fun handlePaymentFailed(code: Int, description: String) {
        updateState {
            copy(
                isCreatingOrder = false,
                isVerifyingPayment = false,
                error = UiText.DynamicString("Payment failed (code $code): $description")
            )
        }
        sendEffect(Effect.ShowError(UiText.DynamicString(description)))
    }
}
```

### 9.5 `PaymentFeatureFacade.kt`

```kotlin
package com.ijs.payment.presentation

import androidx.compose.runtime.Composable
import com.ijs.payment.domain.entity.BillingInterval
import com.ijs.payment.domain.entity.Plan
import com.ijs.payment.presentation.checkout.PaymentCheckoutScreen
import com.ijs.payment.presentation.checkout.PaymentCheckoutViewModel
import com.ijs.payment.presentation.plans.PlansScreen
import com.ijs.payment.presentation.plans.PlansViewModel
import com.ijs.payment.presentation.success.PaymentSuccessScreen

object PaymentFeatureFacade {

    @Composable
    fun PlansEntry(
        viewModel: PlansViewModel,
        onPlanSelected: (plan: Plan, interval: BillingInterval) -> Unit,
        onNavigateToDashboard: () -> Unit,
        onLogout: () -> Unit
    ) {
        PlansScreen(
            viewModel = viewModel,
            onNavigateToPayment = onPlanSelected,
            onNavigateToDashboard = onNavigateToDashboard,
            onLogout = onLogout
        )
    }

    @Composable
    fun PaymentCheckoutEntry(
        viewModel: PaymentCheckoutViewModel,
        plan: Plan,
        billingInterval: BillingInterval,
        onPaymentSuccess: (planName: String, amount: Long, currency: String) -> Unit,
        onNavigateBackToPlans: () -> Unit
    ) {
        PaymentCheckoutScreen(
            viewModel = viewModel,
            plan = plan,
            billingInterval = billingInterval,
            onPaymentSuccess = onPaymentSuccess,
            onNavigateBackToPlans = onNavigateBackToPlans
        )
    }

    @Composable
    fun PaymentSuccessEntry(
        planName: String,
        amount: Long,
        currency: String,
        onContinue: () -> Unit
    ) {
        PaymentSuccessScreen(
            planName = planName,
            amount = amount,
            currency = currency,
            onContinue = onContinue
        )
    }
}
```

---

## 10. Platform-Specific Implementations

### 10.1 Common expect interface (`commonMain`)

```kotlin
// com/ijs/payment/platform/RazorpayCheckoutData.kt (commonMain)
package com.ijs.payment.platform

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
    data class Success(val orderId: String, val paymentId: String, val signature: String) : RazorpayResult()
    data class Failed(val errorCode: Int, val description: String) : RazorpayResult()
    data object Cancelled : RazorpayResult()
}
```

The platform integration is **passed as a lambda callback** from the screen into the ViewModel effect handler — this avoids platform-specific imports in commonMain entirely.

In `PaymentCheckoutScreen.kt` (commonMain):

```kotlin
@Composable
fun PaymentCheckoutScreen(
    viewModel: PaymentCheckoutViewModel,
    plan: Plan,
    billingInterval: BillingInterval,
    onPaymentSuccess: (planName: String, amount: Long, currency: String) -> Unit,
    onNavigateBackToPlans: () -> Unit,
    // Platform-provided: launches Razorpay and returns result via callback
    razorpayLauncher: (data: RazorpayCheckoutData, onResult: (RazorpayResult) -> Unit) -> Unit
) {
    // ...
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PaymentCheckoutContract.Effect.LaunchRazorpayCheckout -> {
                    val order = effect.order
                    razorpayLauncher(
                        RazorpayCheckoutData(
                            orderId = order.orderId,
                            amount = order.amount,
                            currency = order.currency,
                            providerKey = order.providerKey,
                            receiptId = order.receiptId,
                            customerEmail = order.customerEmail,
                            customerName = order.customerName,
                            planName = plan.name
                        )
                    ) { result ->
                        when (result) {
                            is RazorpayResult.Success -> viewModel.sendIntent(
                                PaymentCheckoutContract.Intent.RazorpaySuccess(
                                    result.orderId, result.paymentId, result.signature
                                )
                            )
                            is RazorpayResult.Failed -> viewModel.sendIntent(
                                PaymentCheckoutContract.Intent.RazorpayFailed(result.errorCode, result.description)
                            )
                            RazorpayResult.Cancelled -> viewModel.sendIntent(
                                PaymentCheckoutContract.Intent.RazorpayCancelled
                            )
                        }
                    }
                }
                // ... other effects
            }
        }
    }
}
```

### 10.2 Android — Razorpay SDK Launcher

`androidMain/kotlin/com/ijs/payment/platform/RazorpayLauncher.android.kt`

```kotlin
// Requires: implementation("com.razorpay:checkout:1.6.40") in androidApp build.gradle
// or screen-payment/build.gradle.kts androidMain dependencies

package com.ijs.payment.platform

import android.app.Activity
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

fun createAndroidRazorpayLauncher(activity: Activity): (RazorpayCheckoutData, (RazorpayResult) -> Unit) -> Unit {
    return { data, onResult ->
        val checkout = Checkout()
        checkout.setKeyID(data.providerKey)
        val options = JSONObject().apply {
            put("name", "IndusJS Fleet")
            put("description", data.planName)
            put("order_id", data.orderId)
            put("amount", data.amount)
            put("currency", data.currency)
            put("prefill", JSONObject().apply {
                put("email", data.customerEmail)
                put("name", data.customerName)
            })
        }
        checkout.open(activity, options)
        // Razorpay SDK requires Activity to implement PaymentResultWithDataListener
        // The Activity captures the callback and routes back through a shared state/channel
    }
}
```

> **Note**: Android Razorpay requires the `Activity` to implement `PaymentResultWithDataListener`. Use a `SharedFlow` in `androidApp/AppActivity.kt` to bridge the callback to the screen's lambda.

### 10.3 iOS — Razorpay Pod Launcher

`iosMain/kotlin/com/ijs/payment/platform/RazorpayLauncher.ios.kt`

```kotlin
// Uses Razorpay iOS CocoaPod (add to Podfile)
// pod 'razorpay-pod', '~> 1.3.0'

package com.ijs.payment.platform

// On iOS, call Razorpay via UIKit interop from the MainViewController
// This is a stub — actual implementation uses ObjC/Swift bridging in iosApp

fun createIosRazorpayLauncher(): (RazorpayCheckoutData, (RazorpayResult) -> Unit) -> Unit {
    return { data, onResult ->
        // TODO: Implement via Kotlin/Objective-C interop
        // Razorpay.initWithKey(data.providerKey)
        // Razorpay.open(options) → delegate callbacks
        // For now: stub returns success for dev builds
        onResult(
            RazorpayResult.Failed(0, "iOS Razorpay integration pending — use web flow")
        )
    }
}
```

### 10.4 JS/WasmJS — Razorpay checkout.js

`jsMain/kotlin/com/ijs/payment/platform/RazorpayLauncher.js.kt`

```kotlin
package com.ijs.payment.platform

import kotlinx.browser.window

// Razorpay checkout.js must be loaded in index.html:
// <script src="https://checkout.razorpay.com/v1/checkout.js"></script>

fun createWebRazorpayLauncher(): (RazorpayCheckoutData, (RazorpayResult) -> Unit) -> Unit {
    return { data, onResult ->
        val options = js("({})")
        options.key = data.providerKey
        options.amount = data.amount
        options.currency = data.currency
        options.order_id = data.orderId
        options.name = "IndusJS Fleet"
        options.description = data.planName
        options.prefill = js("({})")
        options.prefill.email = data.customerEmail
        options.prefill.name = data.customerName
        options.handler = { response: dynamic ->
            onResult(
                RazorpayResult.Success(
                    orderId = response.razorpay_order_id as String,
                    paymentId = response.razorpay_payment_id as String,
                    signature = response.razorpay_signature as String
                )
            )
        }
        options.modal = js("({})")
        options.modal.ondismiss = {
            onResult(RazorpayResult.Cancelled)
        }
        val rzp = js("new Razorpay(options)")
        rzp.on("payment.failed") { response: dynamic ->
            onResult(
                RazorpayResult.Failed(
                    errorCode = (response.error.code as? Int) ?: 0,
                    description = response.error.description as String
                )
            )
        }
        rzp.open()
    }
}
```

---

## 11. Navigation Integration

### 11.1 New Routes in `FleetRoute.kt`

Add to `FleetRoute.kt` in `sharedUI`:

```kotlin
// ==================== Subscription / Payment Routes ====================

@Serializable
data object SubscriptionPlans : FleetRoute

@Serializable
data class PaymentCheckout(
    val planId: String,
    val planName: String,
    val monthlyPrice: Long,
    val annualPrice: Long,
    val currency: String,
    val billingInterval: String = "monthly"   // "monthly" | "annual"
) : FleetRoute

@Serializable
data class PaymentSuccess(
    val planName: String,
    val amount: Long,
    val currency: String
) : FleetRoute
```

### 11.2 New Entries in `FleetNavigation.kt`

Add to `fleetEntryProvider`:

```kotlin
is FleetRoute.SubscriptionPlans -> NavEntry(route) {
    val viewModel = rememberViewModel { plansViewModel() }
    PaymentFeatureFacade.PlansEntry(
        viewModel = viewModel,
        onPlanSelected = { plan, interval ->
            backStack.add(
                FleetRoute.PaymentCheckout(
                    planId = plan.id,
                    planName = plan.name,
                    monthlyPrice = plan.monthlyPrice,
                    annualPrice = plan.annualPrice,
                    currency = plan.currency,
                    billingInterval = interval.apiValue
                )
            )
        },
        onNavigateToDashboard = { backStack.navigateAndClear(FleetRoute.Dashboard) },
        onLogout = { backStack.navigateAndClear(FleetRoute.Login) }
    )
}

is FleetRoute.PaymentCheckout -> NavEntry(route) {
    val viewModel = rememberViewModel { paymentCheckoutViewModel() }
    val plan = Plan(
        id = route.planId,
        name = route.planName,
        monthlyPrice = route.monthlyPrice,
        annualPrice = route.annualPrice,
        // fill other fields from route or re-fetch
        description = "",
        discountPercent = 0.0,
        effectiveMonthlyPriceAnnual = route.monthlyPrice,
        annualSavings = 0,
        currency = route.currency,
        trialDays = 0,
        features = emptyList(),
        featureLimits = emptyList(),
        isActive = true
    )
    val interval = BillingInterval.entries.firstOrNull { it.apiValue == route.billingInterval }
        ?: BillingInterval.MONTHLY

    PaymentFeatureFacade.PaymentCheckoutEntry(
        viewModel = viewModel,
        plan = plan,
        billingInterval = interval,
        onPaymentSuccess = { planName, amount, currency ->
            backStack.navigateAndClear(FleetRoute.PaymentSuccess(planName, amount, currency))
        },
        onNavigateBackToPlans = { backStack.removeLastOrNull() }
    )
}

is FleetRoute.PaymentSuccess -> NavEntry(route) {
    PaymentFeatureFacade.PaymentSuccessEntry(
        planName = route.planName,
        amount = route.amount,
        currency = route.currency,
        onContinue = { backStack.navigateAndClear(FleetRoute.Dashboard) }
    )
}
```

---

## 12. DI Integration

### 12.1 `ViewModelProvider.kt` — Add new factory methods

```kotlin
// Add to ViewModelProvider interface:
fun plansViewModel(): PlansViewModel
fun paymentCheckoutViewModel(): PaymentCheckoutViewModel
```

### 12.2 `FeatureRepositoryFactory.kt` — Add SubscriptionRepository

```kotlin
// Add import:
import com.ijs.payment.data.datasource.SubscriptionRemoteDataSourceImpl
import com.ijs.payment.data.repository.SubscriptionRepositoryImpl
import com.ijs.payment.domain.repository.SubscriptionRepository

// Add property:
val subscriptionRepository: SubscriptionRepository by lazy {
    SubscriptionRepositoryImpl(
        remoteDataSource = SubscriptionRemoteDataSourceImpl(httpClient, logger),
        userLocalDataSource = userLocalDataSource,
        logger = logger
    )
}
```

### 12.3 `DefaultViewModelProvider.kt` — Wire new ViewModels

```kotlin
// Add imports for all payment use cases + ViewModels:
import com.ijs.payment.domain.usecase.GetOnboardingStatusUseCase
import com.ijs.payment.domain.usecase.GetPlansUseCase
import com.ijs.payment.domain.usecase.SelectPlanUseCase
import com.ijs.payment.domain.usecase.CreatePaymentOrderUseCase
import com.ijs.payment.domain.usecase.VerifyPaymentUseCase
import com.ijs.payment.presentation.plans.PlansViewModel
import com.ijs.payment.presentation.checkout.PaymentCheckoutViewModel

// Add inside class:
private val subscriptionRepository get() = featureRepos.subscriptionRepository

private val getOnboardingStatusUseCase by lazy { GetOnboardingStatusUseCase(subscriptionRepository) }
private val getPlansUseCase by lazy { GetPlansUseCase(subscriptionRepository) }
private val selectPlanUseCase by lazy { SelectPlanUseCase(subscriptionRepository) }
private val createPaymentOrderUseCase by lazy { CreatePaymentOrderUseCase(subscriptionRepository) }
private val verifyPaymentUseCase by lazy { VerifyPaymentUseCase(subscriptionRepository) }

override fun plansViewModel() = PlansViewModel(
    dispatcherProvider, getPlansUseCase, getOnboardingStatusUseCase, selectPlanUseCase
)

override fun paymentCheckoutViewModel() = PaymentCheckoutViewModel(
    dispatcherProvider, createPaymentOrderUseCase, verifyPaymentUseCase
)
```

### 12.4 New method on `DefaultViewModelProvider`

```kotlin
// Expose for App.kt startup check:
suspend fun checkSubscriptionGate(): SubscriptionGateResult {
    return try {
        val status = getOnboardingStatusUseCase()
        val onboardingStatus = status.getOrNull()
        when {
            onboardingStatus == null -> SubscriptionGateResult.NoGate
            onboardingStatus.needsPlanSelection -> SubscriptionGateResult.RequiresPlanSelection
            onboardingStatus.needsPayment -> SubscriptionGateResult.RequiresPayment
            else -> SubscriptionGateResult.NoGate
        }
    } catch (e: Exception) {
        SubscriptionGateResult.NoGate
    }
}

enum class SubscriptionGateResult {
    NoGate, RequiresPlanSelection, RequiresPayment
}
```

---

## 13. App.kt — Subscription Gate Hook

### Modified startup `LaunchedEffect` in `App.kt`

```kotlin
LaunchedEffect(Unit) {
    fleetLogger.d(TAG_APP, "Checking app launch status...")
    try {
        val onboardingCompleted = viewModelProvider.hasCompletedOnboarding()
        if (!onboardingCompleted) {
            initialRoute = FleetRoute.Onboarding
        } else {
            val isLoggedIn = viewModelProvider.userRepository.isLoggedIn()
            if (isLoggedIn) {
                // ── NEW: Check subscription gate ──────────────────────────
                val gate = viewModelProvider.checkSubscriptionGate()
                initialRoute = when (gate) {
                    SubscriptionGateResult.RequiresPlanSelection,
                    SubscriptionGateResult.RequiresPayment -> FleetRoute.SubscriptionPlans
                    SubscriptionGateResult.NoGate -> FleetRoute.Dashboard
                }
                // ─────────────────────────────────────────────────────────
            } else {
                initialRoute = FleetRoute.Login
            }
        }
    } catch (e: Exception) {
        fleetLogger.e(TAG_APP, "Startup check failed: ${e.message}", e)
        initialRoute = FleetRoute.Login
    } finally {
        isCheckingAuth = false
    }
}
```

### What changes in `LoginViewModel` — `onLoginSuccess` Effect

The `LoginViewModel` currently emits `NavigateToDashboard` on success. With the subscription gate:

- **Option A** (preferred): `LoginViewModel` continues to emit `NavigateToDashboard`. After login, the **FleetNavigation's `FleetRoute.Dashboard` handler** performs a lightweight subscription check BEFORE rendering the dashboard. If payment required, it re-routes to `SubscriptionPlans` before the dashboard is visible.
- **Option B**: `LoginViewModel` emits a new `NavigateToSubscriptionCheck` effect, and the check happens in login navigation. This is less clean since login shouldn't know about subscriptions.

**Recommended: Option A** — `App.kt` startup check covers this for returning users. For the very first login flow, the `LoginViewModel` navigates to Dashboard, and then the Dashboard's `NavEntry` performs the check. This is simpler.

Actually the simplest approach: **App.kt startup handles it for app restart.** For the post-login case, the `FleetNavigation` Dashboard entry checks `subscriptionGate` and redirects:

```kotlin
is FleetRoute.Dashboard -> NavEntry(route) {
    // Lightweight subscription check before rendering dashboard
    val provider = LocalViewModelProvider.current
    LaunchedEffect(Unit) {
        val gate = provider.checkSubscriptionGate()
        if (gate != SubscriptionGateResult.NoGate) {
            backStack.navigateAndClear(FleetRoute.SubscriptionPlans)
        }
    }
    val viewModel = rememberViewModel { dashboardViewModel() }
    DashboardFeatureFacade.DashboardEntry(...)
}
```

---

## 14. `ijs-network-lib` Additions

### Add to `ApiConfig.kt`

```kotlin
// Inside object Endpoints:

// ── Subscription / Onboarding ──────────────────────────────────────────
const val PLANS = "/plans"
const val ONBOARDING_STATUS = "/onboarding/status"
const val SELECT_PLAN = "/onboarding/plan"
const val PAYMENT_ORDERS = "/payments/orders"
const val PAYMENT_VERIFY = "/payments/verify"
const val PAYMENTS = "/payments"
fun paymentById(id: String) = "/payments/$id"
```

---

## 15. build.gradle.kts

```kotlin
// screen-payment/build.gradle.kts

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
apply(from = rootProject.file("gradle/fleet-compose-conventions.gradle"))

kotlin {
    androidTarget {
        namespace = "com.ijs.payment"
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            api(project(":ijs-network-lib"))
            implementation(project(":ijs-ui-components-lib"))
            implementation(project(":ijs-core-lib"))
            implementation(project(":ijs-dispatcher-lib"))
        }

        androidMain.dependencies {
            // Razorpay Android SDK
            implementation("com.razorpay:checkout:1.6.40")
        }

        // iOS: Razorpay pod declared in iosApp/Podfile
        // JS: Razorpay checkout.js loaded via <script> in index.html
    }
}
```

---

## 16. settings.gradle.kts Additions

```kotlin
// In settings.gradle.kts, add to the include list:
include(":screen-payment")
```

And in `sharedUI/build.gradle.kts` add:

```kotlin
implementation(project(":screen-payment"))
```

---

## 17. UI Design Specifications

### 17.1 Plans Screen

```
┌─────────────────────────────────────┐
│  ← (no back)         [Log out]      │
│                                     │
│  Choose Your Plan                   │
│  Start your 14-day free trial       │
│                                     │
│  ┌─ Monthly ──── Annual (save 20%) ─┐│
│  └────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ FREE         ₹0 / month         ││
│  │ • Up to 3 vehicles              ││
│  │ • Basic reporting               ││
│  │           [Select Free]         ││
│  └─────────────────────────────────┘│
│                                     │
│  ┌─────────────────────────────────┐│
│  │ PRO  ★ POPULAR  ₹49/month       ││
│  │ Annual: ₹470/yr (save ₹118)     ││
│  │ 14-day free trial               ││
│  │ • Unlimited vehicles            ││
│  │ • Driver management             ││
│  │ • Advanced reports              ││
│  │ • 10 team members               ││
│  │         [Continue with Pro]     ││
│  └─────────────────────────────────┘│
│  (Card is highlighted/elevated for  │
│   recommended plan)                 │
└─────────────────────────────────────┘
```

**Key UI details:**
- `BillingToggle` — pill-style `Monthly | Annual` toggle at top; Annual shows "save X%" badge
- `PlanCard` — elevated Card with border highlight for recommended plan; features as checkmark rows
- `FeatureLimitRow` — shows feature key label + limit value (Unlimited / number / ✓)
- `PriceDisplay` — large bold price + per-period label; strikethrough original for annual
- Logout icon button in top-right (back navigation is blocked — user must log out to exit)
- Loading: `FleetShimmerPlaceholder` cards during API call
- Error: `ErrorContent` from ijs-ui-components-lib with Retry CTA

### 17.2 Payment Checkout Screen

```
┌─────────────────────────────────────┐
│  ← Change Plan      Payment         │
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Order Summary                   ││
│  │                                 ││
│  │ Plan:    Pro (Monthly)          ││
│  │ Amount:  ₹49.00                 ││
│  │ Trial:   14 days free           ││
│  │                                 ││
│  │ Billed: Monthly after trial     ││
│  └─────────────────────────────────┘│
│                                     │
│  Secure payment powered by          │
│  [Razorpay logo]                    │
│                                     │
│  • 256-bit SSL encryption           │
│  • Supports UPI, Cards, Net Banking │
│  • Cancel anytime                   │
│                                     │
│         [  Pay ₹49  ]               │
│  (disabled when isCreatingOrder)    │
│                                     │
│  By continuing you agree to our     │
│  Terms of Service                   │
└─────────────────────────────────────┘
```

**Key UI details:**
- `FleetDisplayField` for order summary rows
- Loading overlay with `CircularProgressIndicator` during order creation and verification
- Error snackbar via `FleetSnackbarEffect` 
- Back button goes to Plans screen
- Payment button shows spinner inside when processing

### 17.3 Payment Success Screen

```
┌─────────────────────────────────────┐
│                                     │
│         ✅ (animated icon)          │
│                                     │
│     Payment Successful!             │
│                                     │
│  Your Pro subscription is active.   │
│  ₹49 charged.                       │
│                                     │
│  Welcome to IndusJS Fleet!          │
│  Start managing your fleet now.     │
│                                     │
│       [Go to Dashboard →]           │
│                                     │
└─────────────────────────────────────┘
```

---

## 18. Implementation Phases & Milestones

### Phase 1 — Foundation (Days 1–2)

- [ ] Create `screen-payment` module with `build.gradle.kts`
- [ ] Add module to `settings.gradle.kts`
- [ ] Add `ApiConfig.kt` endpoint constants
- [ ] Implement all domain entities (`Plan`, `BillingInterval`, `OnboardingStatus`, `PaymentOrder`, `PaymentResult`, `FeatureLimit`)
- [ ] Implement `SubscriptionRepository` interface + all use cases
- [ ] Verify module compiles on all targets

### Phase 2 — Data Layer (Days 2–3)

- [ ] Implement `SubscriptionDto.kt` and `SubscriptionRequests.kt`
- [ ] Implement `SubscriptionMapper.kt`
- [ ] Implement `SubscriptionRemoteDataSource` interface + `Impl`
- [ ] Implement `SubscriptionRepositoryImpl`
- [ ] Register in `FeatureRepositoryFactory`
- [ ] Unit test: Repository returns correct domain entities from mock DTOs

### Phase 3 — Presentation: Plans Screen (Days 3–4)

- [ ] Implement `PlansContract`, `PlansViewModel`
- [ ] Implement `PlansScreen.kt` (full UI with BillingToggle, PlanCard components)
- [ ] Implement `BillingToggle`, `PlanCard`, `FeatureLimitRow`, `PriceDisplay` components
- [ ] Wire via `PaymentFeatureFacade.PlansEntry`
- [ ] Unit test: `PlansViewModel` intents and state transitions

### Phase 4 — Presentation: Checkout + Success (Days 4–5)

- [ ] Implement `PaymentCheckoutContract`, `PaymentCheckoutViewModel`
- [ ] Implement `PaymentCheckoutScreen.kt` with `RazorpayCheckoutData` + lambda
- [ ] Implement `PaymentSuccessScreen.kt`
- [ ] Wire via `PaymentFeatureFacade`
- [ ] Unit test: `PaymentCheckoutViewModel` order creation + verify flow

### Phase 5 — Platform Integration (Days 5–7)

- [ ] Android: Integrate Razorpay Android SDK in `androidApp`; bridge `AppActivity` → screen callback
- [ ] Web (JS): Add `checkout.js` to `index.html`; implement `createWebRazorpayLauncher`
- [ ] iOS: Add Razorpay CocoaPod; implement `createIosRazorpayLauncher` via ObjC interop
- [ ] WasmJS: Same as jsMain (checkout.js)
- [ ] Test end-to-end payment on each platform with Razorpay test keys

### Phase 6 — Navigation + DI + App Gate (Days 7–8)

- [ ] Add `FleetRoute.SubscriptionPlans`, `PaymentCheckout`, `PaymentSuccess`
- [ ] Wire all routes in `FleetNavigation.kt`
- [ ] Add `plansViewModel()` + `paymentCheckoutViewModel()` to `ViewModelProvider` interface
- [ ] Implement in `DefaultViewModelProvider`
- [ ] Add `checkSubscriptionGate()` to `DefaultViewModelProvider`
- [ ] Modify `App.kt` startup `LaunchedEffect` to check gate
- [ ] Add subscription check in `FleetRoute.Dashboard` NavEntry
- [ ] Add `screen-payment` to `sharedUI` dependencies

### Phase 7 — Polish & Edge Cases (Days 8–9)

- [ ] Handle `step == "verify"` (email not verified) — show verification prompt
- [ ] Handle network errors with retry on all screens
- [ ] Handle back navigation guard (no back from plans to dashboard when gate is active)
- [ ] Handle `PAYMENT_PROVIDER=stub` dev mode (auto-fill test payment data)
- [ ] Add loading shimmer to Plans screen
- [ ] Add payment history screen (`GET /payments`) — optional Phase 2

### Phase 8 — Testing & Review (Days 9–10)

- [ ] Full UI flow test on Android device (Razorpay test mode)
- [ ] Full UI flow test in browser (JS/WasmJS)
- [ ] iOS simulator test
- [ ] Test subscription expiry renewal path
- [ ] Test free plan → skip payment → dashboard
- [ ] Test payment failure + retry
- [ ] Test session expiry during payment
- [ ] Code review + lint pass

---

## 19. Open Questions & Risks

| # | Question / Risk | Recommended Resolution |
|---|-----------------|----------------------|
| 1 | **Razorpay Android SDK requires Activity context** — current KMP composables don't have Activity access | Bridge via `SharedFlow<RazorpayCallback>` in `AppActivity`; wire to screen lambda via `LocalContext` + Activity casting |
| 2 | **iOS Razorpay SDK** — requires Kotlin/ObjC interop; pod integration is non-trivial | Start with a WebView-based fallback on iOS using `checkout.html`; move to native SDK in a later phase |
| 3 | **`PAYMENT_PROVIDER=stub`** in dev — backend accepts mock signatures | Create a "dev mode" stub in the launcher that returns fake `razorpay_payment_id` for development without real Razorpay credentials |
| 4 | **Plan data passing via route args** — `FleetRoute.PaymentCheckout` has limited fields vs full `Plan` object | Option A: Pass minimal fields in route, re-fetch full plan in checkout VM. Option B: Use `SharedViewModelStore` to pass plan between Plans → Checkout VMs |
| 5 | **Subscription renewal** — IAM has `POST /payments/change-plan` but no "renew same plan" endpoint visible | Use `POST /payments/orders` with existing `plan_id` and `billing_interval` for renewals (this creates a new order) |
| 6 | **`GET /onboarding/status` on every app start** — adds latency to startup | Cache result with short TTL (e.g., 5 min in `multiplatform-settings`); revalidate after login/payment |
| 7 | **`PaymentHandler` may be nil on server** if no order-based gateway configured | The stub gateway in dev satisfies this; production must set `PAYMENT_PROVIDER=razorpay` |
| 8 | **Back navigation during payment** — user presses back while Razorpay checkout is open | Razorpay SDK handles this; emits `Cancelled` result; map to `Intent.RazorpayCancelled` |
| 9 | **HTTP LogLevel.BODY** exposes payment data in logs | Change to `LogLevel.HEADERS` for production builds; use a build flag |
| 10 | **WasmJS CORS** — `checkout.js` cross-origin load | Ensure `Content-Security-Policy` in webApp's HTML allows `checkout.razorpay.com` |

---

## 20. Complete Code Implementation

All code for each file is specified in the sections above (sections 7–15). Below is the summary of every file to create/modify:

### Files to CREATE

| File | Module | Notes |
|------|--------|-------|
| `screen-payment/build.gradle.kts` | `screen-payment` | New module build file |
| `screen-payment/AGENTS.md` | `screen-payment` | Module documentation |
| `LogTags.kt` | `screen-payment` | `TAG_PAYMENT_PLANS`, `TAG_PAYMENT_CHECKOUT` |
| `domain/entity/Plan.kt` | `screen-payment` | Domain model |
| `domain/entity/FeatureLimit.kt` | `screen-payment` | Feature limit entity |
| `domain/entity/BillingInterval.kt` | `screen-payment` | Enum |
| `domain/entity/OnboardingStatus.kt` | `screen-payment` | Gate check entity |
| `domain/entity/PaymentOrder.kt` | `screen-payment` | Order domain entity |
| `domain/entity/PaymentResult.kt` | `screen-payment` | Verification result entity |
| `domain/repository/SubscriptionRepository.kt` | `screen-payment` | Interface |
| `domain/usecase/GetOnboardingStatusUseCase.kt` | `screen-payment` | Use case |
| `domain/usecase/GetPlansUseCase.kt` | `screen-payment` | Use case |
| `domain/usecase/SelectPlanUseCase.kt` | `screen-payment` | Use case |
| `domain/usecase/CreatePaymentOrderUseCase.kt` | `screen-payment` | Use case |
| `domain/usecase/VerifyPaymentUseCase.kt` | `screen-payment` | Use case |
| `data/model/SubscriptionDto.kt` | `screen-payment` | All response DTOs |
| `data/model/SubscriptionRequests.kt` | `screen-payment` | All request bodies |
| `data/mapper/SubscriptionMapper.kt` | `screen-payment` | DTO → Domain |
| `data/datasource/SubscriptionRemoteDataSource.kt` | `screen-payment` | Interface + Impl |
| `data/repository/SubscriptionRepositoryImpl.kt` | `screen-payment` | Impl |
| `presentation/plans/PlansContract.kt` | `screen-payment` | MVI contract |
| `presentation/plans/PlansViewModel.kt` | `screen-payment` | ViewModel |
| `presentation/plans/PlansScreen.kt` | `screen-payment` | Compose screen |
| `presentation/checkout/PaymentCheckoutContract.kt` | `screen-payment` | MVI contract |
| `presentation/checkout/PaymentCheckoutViewModel.kt` | `screen-payment` | ViewModel |
| `presentation/checkout/PaymentCheckoutScreen.kt` | `screen-payment` | Compose screen |
| `presentation/success/PaymentSuccessScreen.kt` | `screen-payment` | Compose screen |
| `presentation/components/PlanCard.kt` | `screen-payment` | UI component |
| `presentation/components/BillingToggle.kt` | `screen-payment` | UI component |
| `presentation/components/FeatureLimitRow.kt` | `screen-payment` | UI component |
| `presentation/components/PriceDisplay.kt` | `screen-payment` | UI component |
| `presentation/PaymentFeatureFacade.kt` | `screen-payment` | Feature entry points |
| `platform/RazorpayCheckoutData.kt` | `screen-payment/commonMain` | Common data types |
| `platform/RazorpayLauncher.android.kt` | `screen-payment/androidMain` | Android impl |
| `platform/RazorpayLauncher.ios.kt` | `screen-payment/iosMain` | iOS impl |
| `platform/RazorpayLauncher.js.kt` | `screen-payment/jsMain` | Web impl |
| `platform/RazorpayLauncher.wasmJs.kt` | `screen-payment/wasmJsMain` | WasmJS impl |

### Files to MODIFY

| File | Module | Change |
|------|--------|--------|
| `settings.gradle.kts` | root | Add `include(":screen-payment")` |
| `sharedUI/build.gradle.kts` | `sharedUI` | Add `implementation(project(":screen-payment"))` |
| `ijs-network-lib/ApiConfig.kt` | `ijs-network-lib` | Add 5 new endpoint constants |
| `sharedUI/navigation/FleetRoute.kt` | `sharedUI` | Add 3 new route classes |
| `sharedUI/navigation/FleetNavigation.kt` | `sharedUI` | Add 3 new NavEntry blocks |
| `sharedUI/di/ViewModelProvider.kt` | `sharedUI` | Add 2 new factory methods |
| `sharedUI/di/DefaultViewModelProvider.kt` | `sharedUI` | Add repo + use cases + VM constructors + `checkSubscriptionGate()` |
| `sharedUI/di/FeatureRepositoryFactory.kt` | `sharedUI` | Add `subscriptionRepository` |
| `sharedUI/App.kt` | `sharedUI` | Add subscription gate in startup `LaunchedEffect` |
| `iosApp/Podfile` | `iosApp` | Add Razorpay pod (Phase 5) |
| `androidApp/build.gradle.kts` | `androidApp` | Razorpay SDK dependency (via screen-payment androidMain) |

---

*Document Version: 1.0 — Ready for implementation review and Phase 1 kickoff.*
