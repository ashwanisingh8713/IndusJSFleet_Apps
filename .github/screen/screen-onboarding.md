# screen-onboarding

## Overview

**Package:** `com.ijs.onboarding`
**Module type:** Presentation-only feature module
**Purpose:** First-time user onboarding flow. A multi-page carousel introducing key fleet management features with illustrations and descriptions. Shown only once (persisted via `multiplatform-settings`).

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `OnboardingFeatureFacade`, `OnboardingContract`, `OnboardingViewModel`, `OnboardingScreen` |
| **Domain** | None |
| **Data** | None — uses `multiplatform-settings` directly for onboarding completion flag |

**Unique dependency:** Depends on `ijs-core-lib` (not `ijs-network-lib`) since no networking is required. Uses `multiplatform-settings` for local persistence of `onboarding_completed` flag.

---

## Dependencies

```
screen-onboarding → ijs-core-lib → ijs-error-lib, ijs-dispatcher-lib, ijs-datetime-utils
screen-onboarding → multiplatform-settings (local storage)
```

No cross-feature module dependencies. No network calls.

---

## Screens

### OnboardingScreen

| Property | Value |
|----------|-------|
| Route | Conditional — shown before `Login` if not completed |
| ViewModel | `OnboardingViewModel` |
| Contract | `OnboardingContract` |

**Features:**
- 4-page horizontal carousel with swipe navigation
- Each page: illustration (emoji/icon), title, description
- Progress indicator (dots or bar)
- "Next" button to advance, "Skip" to jump to end
- "Get Started" on final page → marks onboarding complete → navigates to Login
- "Back" on non-first pages

**Pages (typical content):**
1. Fleet Overview — Track all your vehicles in one place
2. Trip Management — Plan routes, assign drivers, monitor trips
3. Cost Tracking — Record fuel, tolls, maintenance costs instantly
4. Reports & Analytics — Get profit/loss insights per vehicle and trip

**State highlights:**
- `currentPage: Int` — current carousel position (0-indexed)
- `totalPages: Int = 4` — fixed page count
- `isFirstPage`, `isLastPage` — computed properties
- `progress: Float` — (currentPage + 1) / totalPages

**Key Intents:** `NextPage`, `PreviousPage`, `Skip`, `GetStarted`, `GoToPage(page)`

**Key Effects:** `NavigateToLogin`, `AnimateToPage(page)`

---

## Facade

```kotlin
object OnboardingFeatureFacade {
    fun OnboardingEntry(viewModel, onComplete)
}
```

**Navigation callbacks:** `onComplete: () -> Unit` — called when onboarding finishes; sharedUI navigates to `FleetRoute.Login`.

---

## Persistence

The `OnboardingViewModel` reads/writes a boolean flag (`onboarding_completed`) in `multiplatform-settings`. On `GetStarted` or `Skip`, it sets the flag to `true` so the onboarding is never shown again.

`sharedUI/App.kt` checks this flag at startup to decide whether to show `Onboarding` or `Login` as the initial route.

