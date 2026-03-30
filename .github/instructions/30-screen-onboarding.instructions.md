# screen-onboarding — IndusJS Fleet

## Purpose

First-time user onboarding flow. Shown once before Login.

## Package: `com.ijs.onboarding`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| OnboardingScreen | `Onboarding` | Welcome pages, feature highlights, get started |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/OnboardingFeatureFacade.kt` | Facade — 1 entry point |
| `presentation/OnboardingContract.kt` | State/Intent/Effect |
| `presentation/OnboardingViewModel.kt` | ViewModel |
| `presentation/OnboardingScreen.kt` | UI — paged content |

## Onboarding Check

```kotlin
// In ViewModelProvider:
fun hasCompletedOnboarding(): Boolean  // Checks Settings flag

// In App.kt — initial route:
val startRoute = if (provider.hasCompletedOnboarding()) FleetRoute.Login else FleetRoute.Onboarding
```

## Module Path

`screen-onboarding/src/commonMain/kotlin/com/ijs/onboarding/`

## Depends On: `ijs-network-lib`

