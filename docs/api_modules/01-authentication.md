# 01. Authentication

User authentication endpoints (no authentication required).

## Overview

All authentication endpoints are public and do not require a Bearer token. Upon successful login or signup, a JWT token is returned that must be used for all subsequent authenticated API calls.

---

## Endpoints

### Sign Up

```http
POST {{base_url}}/auth/signup
Content-Type: application/json
```

**Request Body:**
```json
{
    "email": "owner@example.com",
    "password": "password123",
    "first_name": "John",
    "last_name": "Doe",
    "mobile": "+919876543210"
}
```

**Required Fields:**

| Field | Type | Validation |
|-------|------|------------|
| `email` | string | Valid email format |
| `password` | string | Minimum 8 characters |
| `first_name` | string | Non-empty |
| `last_name` | string | Non-empty |
| `mobile` | string | Valid mobile number |

**Response (201 Created):**
```json
{
    "success": true,
    "message": "User created successfully",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIs...",
        "user": {
            "id": 1,
            "email": "owner@example.com",
            "first_name": "John",
            "last_name": "Doe",
            "mobile": "+919876543210",
            "role": "owner"
        }
    }
}
```

**Notes:**
- New users are always created with the `owner` role
- The JWT token is valid for 24 hours
- Store the token securely for subsequent API calls

---

### Login

```http
POST {{base_url}}/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
    "email": "owner@example.com",
    "password": "password123"
}
```

**Required Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `email` | string | Registered email address |
| `password` | string | Account password |

**Response (200 OK):**
```json
{
    "success": true,
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIs...",
        "user": {
            "id": 1,
            "email": "owner@example.com",
            "first_name": "John",
            "last_name": "Doe",
            "mobile": "+919876543210",
            "role": "owner"
        }
    }
}
```

**User Roles Returned:**
- `owner` — Full access to all features
- `general_manager` — Financial & operational access
- `manager` — Operational access (no financial data)
- `supervisor` — Limited view-only access

---

### Forgot Password

```http
POST {{base_url}}/auth/forgot-password
Content-Type: application/json
```

**Request Body:**
```json
{
    "email": "owner@example.com"
}
```

**Required Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `email` | string | Registered email address |

**Response (200 OK):**
```json
{
    "success": true,
    "message": "Password reset email sent"
}
```

**Notes:**
- A password reset link/token is sent to the registered email
- The reset token has a limited validity period
- Returns success even if email is not registered (security best practice)

---

### Reset Password

```http
POST {{base_url}}/auth/reset-password
Content-Type: application/json
```

**Request Body:**
```json
{
    "token": "reset-token-from-email",
    "password": "new_password123"
}
```

**Required Fields:**

| Field | Type | Validation |
|-------|------|------------|
| `token` | string | Valid reset token from email |
| `password` | string | Minimum 8 characters |

**Response (200 OK):**
```json
{
    "success": true,
    "message": "Password reset successfully"
}
```

---

## Token Usage

After login or signup, include the token in all authenticated requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## Error Responses

### Invalid Credentials (401)
```json
{
    "success": false,
    "message": "Invalid credentials",
    "error": "email or password is incorrect"
}
```

### Email Already Exists (409)
```json
{
    "success": false,
    "message": "Email already exists",
    "error": "email is already registered"
}
```

### Validation Error (400)
```json
{
    "success": false,
    "message": "Validation failed",
    "error": "password must be at least 8 characters"
}
```

### Invalid Reset Token
```json
{
    "success": false,
    "message": "Invalid token",
    "error": "reset token is invalid or expired"
}
```

---

*Last Updated: April 2026*

