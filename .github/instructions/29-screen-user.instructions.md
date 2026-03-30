# screen-user — IndusJS Fleet

## Purpose

User/Auth feature: login, signup, forgot password, profile, change password.
Data layer in `ijs-network-lib` (UserRemoteDataSource, UserLocalDataSource, UserRepository).

## Package: `com.ijs.user`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| LoginScreen | `Login` | Email/password → Dashboard |
| SignUpScreen | `SignUp` | Owner registration → Dashboard |
| ForgotPasswordScreen | `ForgotPassword` | Email reset link |
| ProfileScreen | `Profile` | View/edit profile, logout |
| ChangePasswordScreen | `ChangePassword` | Update password |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/UserFeatureFacade.kt` | Facade — 5 entry points |
| `presentation/login/Login*.kt` | Login Contract/VM/Screen |
| `presentation/signup/SignUp*.kt` | SignUp Contract/VM/Screen |
| `presentation/forgotpassword/ForgotPassword*.kt` | Forgot Password Contract/VM/Screen |
| `presentation/profile/Profile*.kt` | Profile Contract/VM/Screen |
| `presentation/changepassword/ChangePassword*.kt` | Change Password Contract/VM/Screen |

## Auth Flow

```
Login → UserRemoteDataSource.login(email, password)
  → Success: store token + user data in Settings → navigate to Dashboard
  → Error: show error message

401 during any API call → AuthenticationManager.notifyAuthExpired()
  → App.kt observes → redirect to Login, clear back stack
```

## Module Path

`screen-user/src/commonMain/kotlin/com/ijs/user/`

## Depends On: `ijs-network-lib`

