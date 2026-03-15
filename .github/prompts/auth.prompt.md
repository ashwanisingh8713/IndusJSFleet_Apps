# Auth Feature

> Use this prompt when working on login, signup, forgot password, profile, or session management.

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `LoginScreen` | `Login` | Email + password → Dashboard |
| `SignUpScreen` | `SignUp` | Owner registration → Dashboard |
| `ForgotPasswordScreen` | `ForgotPassword` | Email → reset link |
| `ProfileScreen` | `Profile` | View/edit profile, logout |
| `ChangePasswordScreen` | `ChangePassword` | Current + new password |

## API Endpoints

```
POST /auth/login            → { email, password } → { token, user }
POST /auth/signup           → { name, email, password, mobile, company_name } → { token, user }
POST /auth/forgot-password  → { email } → sends reset email
POST /auth/reset-password   → { token, new_password }
GET  /profile               → Current user profile
PUT  /profile               → Update profile
PUT  /profile/change-password → { current_password, new_password }
```

## Auth Token Flow

```
Login/SignUp success
  → UserLocalDataSource.saveAuthToken(token)
  → UserLocalDataSource.saveUserInfo(user)
  → Navigate to Dashboard (navigateAndClear)

App Launch
  → App.kt checks UserLocalDataSource.isLoggedIn()
  → If logged in → Dashboard
  → If not → Login

API call returns 401
  → HttpClientProvider interceptor detects
  → AuthenticationManager.emitUnauthorized()
  → Session cleared via registered callback
  → App.kt collects event → navigateAndClear(Login)
  → Snackbar: "Your session has expired"

Logout
  → UserLocalDataSource.clearSession()
  → AuthenticationManager.emitLoggedOut()
  → App.kt → navigateAndClear(Login)
```

## Token Storage

```kotlin
// UserLocalDataSourceImpl uses multiplatform-settings
class UserLocalDataSourceImpl(private val settings: Settings) {
    fun saveAuthToken(token: String) { settings.putString("auth_token", token) }
    fun getAuthToken(): String? = settings.getStringOrNull("auth_token")
    fun clearSession() {
        settings.remove("auth_token")
        settings.remove("user_info")
    }
    fun isLoggedIn(): Boolean = getAuthToken() != null
    fun getUserRole(): String? = settings.getStringOrNull("user_role")
}
```

## Session Expiry Handling

`AuthenticationManager` is a singleton that:
1. Receives 401 events from HTTP interceptor
2. Clears session via registered callback
3. Emits `AuthenticationEvent.SessionExpired`
4. `App.kt` collects and navigates to Login

The callback is registered in `DefaultViewModelProvider.init {}`:
```kotlin
AuthenticationManager.registerSessionClearCallback {
    userLocalDataSource.clearSession()
}
```

## Key Files

| Layer | File |
|-------|------|
| DataSource | `data/datasource/user/UserLocalDataSourceImpl.kt` |
| DataSource | `data/datasource/user/UserRemoteDataSourceImpl.kt` |
| Repository | `data/repository/user/UserRepositoryImpl.kt` |
| Auth Manager | `core/auth/AuthenticationManager.kt` |
| Login | `presentation/auth/LoginViewModel.kt`, `LoginScreen.kt` |
| SignUp | `presentation/user/signup/` |
| ForgotPassword | `presentation/user/forgotpassword/` |
| Profile | `presentation/user/profile/` |
| ChangePassword | `presentation/user/changepassword/` |

