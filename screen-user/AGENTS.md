# AGENTS.md — screen-user

## Purpose

**User Authentication & Profile** feature module. Contains all auth-related screens: login, signup, forgot password, profile view/edit, and change password. This is a **presentation-only** module — it has no data layer of its own. All auth data operations go through `UserRepository` in `ijs-network-lib`.

**Package:** `com.ijs.user`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/user/
├── LogTags.kt
└── presentation/
    ├── UserFeatureFacade.kt               # DI entry point: creates all auth ViewModels
    ├── login/
    │   ├── LoginContract.kt               # Login MVI contract (State/Intent/Effect)
    │   ├── LoginScreen.kt                 # Email + password form → Dashboard
    │   └── LoginViewModel.kt              # Calls UserRepository.login()
    ├── signup/
    │   ├── SignUpContract.kt
    │   ├── SignUpScreen.kt                # Owner registration form → Dashboard
    │   └── SignUpViewModel.kt             # Calls UserRepository.signUp()
    ├── forgotpassword/
    │   ├── ForgotPasswordContract.kt
    │   ├── ForgotPasswordScreen.kt        # Email input → sends reset link
    │   └── ForgotPasswordViewModel.kt
    ├── profile/
    │   ├── ProfileContract.kt
    │   ├── ProfileScreen.kt               # View/edit profile, logout button
    │   └── ProfileViewModel.kt            # Loads profile, handles update + logout
    └── changepassword/
        ├── ChangePasswordContract.kt
        ├── ChangePasswordScreen.kt        # Current + new + confirm password fields
        └── ChangePasswordViewModel.kt     # Validates password strength, calls API
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:ijs-ui-components-lib` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client, multiplatform-settings |

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| LoginScreen | `Login` | Email/password → Dashboard |
| SignUpScreen | `SignUp` | Owner registration → Dashboard |
| ForgotPasswordScreen | `ForgotPassword` | Email reset link |
| ProfileScreen | `Profile` | View/edit profile, logout |
| ChangePasswordScreen | `ChangePassword` | Update password |

---

## Key Patterns

- **Presentation-only** — No `data/` or `domain/` layers. All auth operations use `UserRepository` and `UserLocalDataSource` from `ijs-network-lib`.
- **Session management** — `LoginViewModel` stores auth token via `UserLocalDataSource.saveAuthToken()` on success. `ProfileViewModel` clears it on logout, triggering `AuthenticationManager.sessionExpired`.
- **Password strength** — `ChangePasswordScreen` uses `PasswordStrengthIndicator` from `ijs-ui-components-lib` for visual feedback.
- **multiplatform-settings** — Direct dependency for persisting "remember me" preference on the login screen.
