# 09 — User Module Migration (`screen-user`)

## Overview

Migrated all user/auth presentation code from `sharedUI/presentation/auth/` and `sharedUI/presentation/user/` into the new `screen-user` feature module.

- **Module:** `screen-user`
- **Package:** `com.ijs.user`
- **Namespace:** `com.ijs.user`

## What Moved

| Source (deleted) | Destination | Description |
|---|---|---|
| `sharedUI/.../presentation/auth/LoginContract.kt` | `screen-user/.../presentation/login/LoginContract.kt` | Login MVI contract |
| `sharedUI/.../presentation/auth/LoginViewModel.kt` | `screen-user/.../presentation/login/LoginViewModel.kt` | Login ViewModel |
| `sharedUI/.../presentation/auth/LoginScreen.kt` | `screen-user/.../presentation/login/LoginScreen.kt` | Login UI screen |
| `sharedUI/.../presentation/user/signup/*` | `screen-user/.../presentation/signup/*` | Sign up flow |
| `sharedUI/.../presentation/user/profile/*` | `screen-user/.../presentation/profile/*` | User profile |
| `sharedUI/.../presentation/user/changepassword/*` | `screen-user/.../presentation/changepassword/*` | Change password |
| `sharedUI/.../presentation/user/forgotpassword/*` | `screen-user/.../presentation/forgotpassword/*` | Forgot/reset password |

## Architecture

```
screen-user/
└── src/commonMain/kotlin/com/ijs/user/presentation/
    ├── UserFeatureFacade.kt         # Public facade (5 entry points)
    ├── login/
    │   ├── LoginContract.kt
    │   ├── LoginViewModel.kt
    │   └── LoginScreen.kt
    ├── signup/
    │   ├── SignUpContract.kt
    │   ├── SignUpViewModel.kt
    │   └── SignUpScreen.kt
    ├── profile/
    │   ├── ProfileContract.kt
    │   ├── ProfileViewModel.kt
    │   └── ProfileScreen.kt          # ⚠️ 1008 lines — split deferred
    ├── changepassword/
    │   ├── ChangePasswordContract.kt
    │   ├── ChangePasswordViewModel.kt
    │   └── ChangePasswordScreen.kt
    └── forgotpassword/
        ├── ForgotPasswordContract.kt
        ├── ForgotPasswordViewModel.kt
        └── ForgotPasswordScreen.kt
```

## Dependencies

```kotlin
// screen-user/build.gradle.kts
api(project(":ijs-network-lib"))  // UserRepository, MviViewModel, auth infra
// Transitive: ijs-core-lib (MVI, entities), ijs-error-lib, ijs-dispatcher-lib
// Via fleet-compose-conventions: ijs-ui-components-lib, Compose, Lifecycle
```

## Data Layer

The user data layer (DTOs, DataSources, Mapper, Repository) **stays in `ijs-network-lib`** because `UserLocalDataSource` is shared infrastructure used by every feature module for auth tokens.

## Facade Pattern

`UserFeatureFacade` exposes 5 `@Composable` entry points:
- `LoginEntry` — Login screen
- `SignUpEntry` — Sign up screen  
- `ProfileEntry` — User profile (view/edit)
- `ChangePasswordEntry` — Change password
- `ForgotPasswordEntry` — Forgot/reset password

All navigation via lambda callbacks. No FleetRoute imports.

## sharedUI Changes

- **ViewModelProvider.kt:** Updated 6 imports → `com.ijs.user.presentation.*`
- **DefaultViewModelProvider.kt:** Updated 6 imports → `com.ijs.user.presentation.*`
- **FleetNavigation.kt:** Replaced 5 direct screen calls with `UserFeatureFacade.*` facade calls

## TODO

- [ ] Split `ProfileScreen.kt` (1008 lines) into `ProfileScreen.kt` + `ProfileContent.kt` + `EditProfileContent.kt`

