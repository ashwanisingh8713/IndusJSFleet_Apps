# Auth Module

## Overview

The Auth module handles user authentication, session management, and password operations for the IndusJS Fleet application.

---

## Features

- User registration (Owner signup)
- Email/mobile login with JWT
- Password reset flow
- Session persistence with auto-refresh
- Logout with token invalidation
- Unauthorized access handling

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Login | `FleetRoute.Login` | Email/password login form |
| Sign Up | `FleetRoute.SignUp` | New owner registration |
| Forgot Password | `FleetRoute.ForgotPassword` | Password reset request |

---

## User Flow

### Login Flow

1. User enters email/mobile and password
2. App validates input format
3. API call to authenticate
4. On success: Store JWT token, navigate to Dashboard
5. On failure: Show error message

### Sign Up Flow

1. User enters registration details (email, mobile, password, name)
2. App validates all fields
3. API call to register
4. On success: Auto-login, navigate to Dashboard
5. On failure: Show validation errors

### Password Reset Flow

1. User enters registered email
2. API sends reset link/OTP
3. User enters new password
4. API updates password
5. Navigate to Login

---

## State Management

### Login State

| Field | Type | Description |
|-------|------|-------------|
| email | String | User's email input |
| password | String | User's password input |
| isLoading | Boolean | Loading indicator |
| error | String? | Error message if any |
| isPasswordVisible | Boolean | Toggle password visibility |

### Intents

| Intent | Description |
|--------|-------------|
| UpdateEmail | User types email |
| UpdatePassword | User types password |
| TogglePasswordVisibility | Show/hide password |
| Login | Submit login form |
| NavigateToSignUp | Go to registration |
| NavigateToForgotPassword | Go to reset password |

### Effects

| Effect | Description |
|--------|-------------|
| NavigateToDashboard | Successful login |
| ShowError | Display error message |

---

## API Endpoints

| Endpoint | Method | Auth | Description |
|----------|--------|:----:|-------------|
| `/auth/signup` | POST | ❌ | Register new owner |
| `/auth/login` | POST | ❌ | Authenticate user |
| `/auth/forgot-password` | POST | ❌ | Request reset token |
| `/auth/reset-password` | POST | ❌ | Set new password |
| `/auth/refresh` | POST | ✅ | Refresh JWT token |
| `/auth/logout` | POST | ✅ | Invalidate session |

---

## Token Management

### Storage

- JWT token stored securely using `multiplatform-settings`
- Token retrieved for all authenticated API calls
- Token cleared on logout or expiry

### Auto-Refresh

- Token refresh triggered before expiry
- Failed refresh redirects to Login screen
- Background refresh maintains session

### Unauthorized Handling

- 401 response triggers logout
- User redirected to Login screen
- Pending API calls cancelled gracefully

---

## Session Handling

### Architecture

The auth session handling follows a centralized approach with single point of handling:

```
┌─────────────────────────────────────────────────────────────────┐
│                     Auth Session Handling Flow                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐     ┌──────────────────┐     ┌─────────────┐  │
│  │ API Request │────▶│ HttpClient       │────▶│ 401 Response│  │
│  │ (Repository)│     │ (with Auth token)│     │ from Server │  │
│  └─────────────┘     └──────────────────┘     └──────┬──────┘  │
│                                                      │          │
│                                                      ▼          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ HttpResponseValidator (check if had Authorization header)│  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ AuthenticationManager.emitSessionExpired()                │  │
│  │   1. Set isHandlingUnauthorized = true (prevent race)     │  │
│  │   2. Clear session via callback                           │  │
│  │   3. Emit SessionExpired event                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                              │                                  │
│                              ▼                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ App.kt - LaunchedEffect collecting authEvents             │  │
│  │   1. Navigate to Login (clear back stack)                 │  │
│  │   2. Show snackbar with message                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Components

| Component | File | Responsibility |
|-----------|------|----------------|
| `AuthenticationManager` | `core/auth/AuthenticationManager.kt` | Singleton that emits auth events |
| `AuthTokenHelper` | `core/auth/AuthTokenHelper.kt` | Validates token in repositories |
| `HttpClientProvider` | `core/network/HttpClientProvider.kt` | HTTP interceptor for 401 |
| `App.kt` | Root composable | Collects events, navigates to Login |

### Scenario Handling

| Scenario | Trigger | Action | Message |
|----------|---------|--------|---------|
| 401 from API (with Auth header) | `HttpResponseValidator` | Clear token → Navigate to Login → Show snackbar | "Your session has expired. Please log in again." |
| 401 from API (no Auth header) | `HttpResponseValidator` | Ignore (login/public API) | None |
| Token is null | `AuthTokenHelper.requireAuthTokenOrRedirect()` | Emit SessionExpired → Navigate to Login | "Please log in to continue." |
| Manual logout | User action | Clear token → Navigate to Login | None |

### Key Benefits

- **Single point of handling** - No duplicate logic in every ViewModel
- **Consistent UX** - User always redirected to Login on auth failure
- **Clean navigation** - Back stack cleared to prevent returning to protected screens
- **Proper messaging** - Snackbar instead of toast for better visibility
- **Race condition prevention** - `isHandlingUnauthorized` flag prevents duplicate events

### Troubleshooting

| Issue | Possible Cause | Solution |
|-------|---------------|----------|
| User not redirected to Login | Auth event collector not started | Check `LaunchedEffect(Unit)` in App.kt |
| Session clear callback not working | Callback not registered | Verify `registerSessionClearCallback` in DefaultViewModelProvider init |
| Multiple login redirects | Race condition | Ensure `isHandlingUnauthorized` flag is working |
| Toast instead of snackbar | Wrong handler | Use `snackbarHostState.showSnackbar()` |

### Logging Tags

Use these log tags to trace auth flow:

- `App` - Auth event collection and navigation
- `HTTP` - 401 response detection
- `UserLocalDataSource` - Token storage operations
- `AuthenticationManager` - Event emission

---

## Validation Rules

| Field | Rules |
|-------|-------|
| Email | Required, valid email format |
| Mobile | Required, 10 digits |
| Password | Required, minimum 6 characters |
| First Name | Required, alphabets only |
| Last Name | Required, alphabets only |

---

## Security Considerations

- Passwords never stored locally
- JWT token stored in secure storage
- Token expires after configurable duration
- Failed login attempts tracked server-side
- Rate limiting: 10 requests/minute for auth endpoints

---

## Related Modules

- [Dashboard](../dashboard/) - Post-login landing
- [User Roles](../../user-roles/) - Role-based access

---

## Related Documentation

- [Modules Overview](../README.md)
- [Architecture Guide](../../architecture/README.md)
