# screen-user

## Overview

**Package:** `com.ijs.user`
**Module type:** Presentation-only feature module
**Purpose:** Authentication and user profile management — Login, Sign Up, Forgot Password, Profile viewing/editing, and Change Password. Handles JWT token management, session persistence, and auto-login on app launch.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `UserFeatureFacade`, Login (Contract/VM/Screen), SignUp (Contract/VM/Screen), ForgotPassword (Contract/VM/Screen), Profile (Contract/VM/Screen), ChangePassword (Contract/VM/Screen) |
| **Domain** | None — uses `UserProfile` entity from `ijs-network-lib` |
| **Data** | None — uses `UserLocalDataSource`/`UserRemoteDataSource` from `ijs-network-lib` |

All auth/user data operations happen through `ijs-network-lib`'s user data sources. This module is presentation-only.

---

## Dependencies

```
screen-user → ijs-network-lib → ijs-core-lib
screen-user → multiplatform-settings (token/session persistence)
```

No cross-feature module dependencies.

---

## Screens

### 1. LoginScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Login` |
| ViewModel | `LoginViewModel` |
| Contract | `LoginContract` |

**Features:**
- Email and password fields
- Password visibility toggle
- "Login" button with loading state
- "Forgot Password?" link
- "Sign Up" link for new users
- **Auto-login check:** On screen load, checks for existing auth token → auto-navigates to Dashboard if valid
- Error display for invalid credentials

**State:** `email`, `password`, `isLoading`, `isCheckingAuth`, `error`, `isPasswordVisible`
**Key Intents:** `UpdateEmail`, `UpdatePassword`, `TogglePasswordVisibility`, `Login`, `CheckAuthStatus`
**Key Effects:** `NavigateToDashboard`, `ShowError`

---

### 2. SignUpScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.SignUp` |
| ViewModel | `SignUpViewModel` |
| Contract | `SignUpContract` |

**Features:**
- Registration form: First Name, Last Name, Email, Mobile (10 digits), Password, Confirm Password
- Password visibility toggles
- Mobile validation (10 digits, Indian format)
- Password match validation
- Creates **Owner** role account
- On success → navigates to Dashboard

**State:** `firstName`, `lastName`, `email`, `mobile`, `mobileError`, `password`, `confirmPassword`, `isPasswordVisible`, `isConfirmPasswordVisible`, `isLoading`, `error`
**Key Intents:** `UpdateFirstName`, `UpdateLastName`, `UpdateEmail`, `UpdateMobile`, `UpdatePassword`, `UpdateConfirmPassword`, `SignUp`, `NavigateToLogin`
**Key Effects:** `NavigateToDashboard`, `NavigateToLogin`, `ShowSnackbar`

---

### 3. ForgotPasswordScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.ForgotPassword` |
| ViewModel | `ForgotPasswordViewModel` |
| Contract | `ForgotPasswordContract` |

**Features:**
- Email input field
- "Send Reset Link" button
- Success message display
- Back to Login link

---

### 4. ProfileScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Profile` |
| ViewModel | `ProfileViewModel` |
| Contract | `ProfileContract` |

**Features:**
- Display: user avatar (initials), full name, email, mobile, role badge
- **Edit mode:** Inline editing of first name, last name, email, mobile
- "Change Password" navigation
- "Logout" button → clears auth token → navigates to Login
- Pull-to-refresh

**State:** `profile: UserProfile?`, `isEditing`, edit fields, `isUpdating`
**Key Intents:** `LoadProfile`, `StartEditing`, `CancelEditing`, `SaveProfile`, `NavigateToChangePassword`, `Logout`
**Key Effects:** `NavigateToChangePassword`, `Logout`, `ShowSnackbar`

---

### 5. ChangePasswordScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.ChangePassword` |
| ViewModel | `ChangePasswordViewModel` |
| Contract | `ChangePasswordContract` |

**Features:**
- Current password field
- New password field
- Confirm new password field
- Password visibility toggles
- Password strength validation
- Password match validation

---

## Facade

```kotlin
object UserFeatureFacade {
    fun LoginEntry(viewModel, onLoginSuccess, onNavigateToSignUp, onNavigateToForgotPassword)
    fun SignUpEntry(viewModel, onSignUpSuccess, onNavigateToLogin)
    fun ProfileEntry(viewModel, onNavigateToChangePassword, onNavigateBack, onLogout)
    fun ChangePasswordEntry(viewModel, onNavigateBack)
    fun ForgotPasswordEntry(viewModel, onNavigateToLogin)
}
```

---

## Authentication Flow

```
App Launch
  → Check onboarding_completed flag
    → If false: Show Onboarding → Login
    → If true: Show Login
      → LoginViewModel.CheckAuthStatus
        → If valid token exists → NavigateToDashboard (auto-login)
        → If no token → Show login form
          → User enters email/password → Login intent
            → UserRemoteDataSource.login(email, password)
              → POST /auth/login → JWT token
            → UserLocalDataSource.saveToken(token)
            → NavigateToDashboard effect
```

---

## Logout Flow

```
Profile → Logout intent
  → UserLocalDataSource.clearAll() (token, user data)
  → AuthenticationManager.notifyLogout()
  → Navigate to Login (clear back stack)
```

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/login` | POST | Authenticate, returns JWT token |
| `/auth/signup` | POST | Register new owner account |
| `/auth/forgot-password` | POST | Send password reset email |
| `/auth/change-password` | POST | Change password (requires current password) |
| `/users/profile` | GET | Get current user profile |
| `/users/profile` | PUT | Update user profile |

---

## Token Management

- JWT token stored via `multiplatform-settings` in `UserLocalDataSource`
- Token attached to all API requests via `AuthTokenHelper` interceptor in `ijs-network-lib`
- 401 responses trigger `AuthenticationManager.notifyUnauthorized()` → auto-logout → Login screen

