# 10 — Onboarding Module Migration (`feat-onboarding`)

## Overview

Migrated onboarding presentation code from `sharedUI/presentation/onboarding/` into the new `feat-onboarding` feature module.

- **Module:** `feat-onboarding`
- **Package:** `com.ijs.onboarding`
- **Namespace:** `com.ijs.onboarding`

## What Moved

| Source (deleted) | Destination | Description |
|---|---|---|
| `sharedUI/.../presentation/onboarding/OnboardingContract.kt` | `feat-onboarding/.../presentation/OnboardingContract.kt` | Onboarding MVI contract |
| `sharedUI/.../presentation/onboarding/OnboardingViewModel.kt` | `feat-onboarding/.../presentation/OnboardingViewModel.kt` | Onboarding ViewModel |
| `sharedUI/.../presentation/onboarding/OnboardingScreen.kt` | `feat-onboarding/.../presentation/OnboardingScreen.kt` | Onboarding UI screen |

## Architecture

```
feat-onboarding/
└── src/commonMain/kotlin/com/ijs/onboarding/presentation/
    ├── OnboardingFeatureFacade.kt   # Public facade (1 entry point)
    ├── OnboardingContract.kt
    ├── OnboardingViewModel.kt
    └── OnboardingScreen.kt
```

## Dependencies

```kotlin
// feat-onboarding/build.gradle.kts
api(project(":ijs-core-lib"))          // MviViewModel, UiState/Intent/Effect
implementation(libs.multiplatformSettings) // Settings (onboarding completed flag)
// Via fleet-compose-conventions: ijs-ui-components-lib, Compose, Lifecycle
```

**Note:** `feat-onboarding` does NOT depend on `ijs-network-lib`. It uses minimal dependencies:
- `ijs-core-lib` for `MviViewModel` base class
- `multiplatform-settings` for `Settings` (persisting onboarding completion)
- `ijs-ui-components-lib` for icons/resources (via fleet-compose-conventions)

## Data Layer

No data layer needed. Onboarding uses `Settings` directly for the single `onboarding_completed` boolean flag.

## Facade Pattern

`OnboardingFeatureFacade` exposes 1 `@Composable` entry point:
- `OnboardingEntry(viewModel, onComplete)` — Multi-page onboarding pager

All navigation via lambda callbacks. No FleetRoute imports.

## sharedUI Changes

- **ViewModelProvider.kt:** Updated import → `com.ijs.onboarding.presentation.OnboardingViewModel`
- **DefaultViewModelProvider.kt:** Updated import → `com.ijs.onboarding.presentation.OnboardingViewModel`
- **FleetNavigation.kt:** Replaced `OnboardingScreen(...)` with `OnboardingFeatureFacade.OnboardingEntry(...)`

