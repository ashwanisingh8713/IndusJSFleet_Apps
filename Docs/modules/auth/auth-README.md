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
