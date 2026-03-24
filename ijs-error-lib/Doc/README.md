# ijs-error-lib — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.error`

---

## 1. Purpose

Standalone error handling primitives for all IndusJS apps. Provides the universal `Result<T>` sealed class, structured exception hierarchy (`IjsException`), HTTP error codes, and user-friendly error classification.

## 2. Package Structure

```
src/commonMain/kotlin/com/indusjs/error/
├── exception/
│   ├── IjsException.kt          # Base sealed class (errorCode, isRecoverable)
│   ├── NetworkException.kt      # CONNECTION | TIMEOUT | DNS | SSL
│   ├── ApiException.kt          # HTTP 4xx/5xx with httpCode, errorBody
│   ├── AuthException.kt         # UNAUTHENTICATED | TOKEN_EXPIRED | UNAUTHORIZED
│   └── ValidationException.kt   # Field-level validation errors
├── code/
│   ├── HttpErrorCode.kt         # Enum for HTTP 400-504 with human messages
│   └── ErrorMessages.kt         # Centralized error message constants
├── handler/
│   ├── ErrorType.kt             # Classification enum
│   ├── ErrorInfo.kt             # User-friendly error data (icon, title, message)
│   ├── ErrorContext.kt          # Interface for screen-specific messages
│   ├── ErrorClassifier.kt       # Classifies errors from messages/exceptions
│   └── ErrorHandler.kt          # getErrorInfo(error, context) → ErrorInfo
└── result/
    └── Result.kt                # Result<T> sealed class
```

## 3. Dependencies

- `kotlinx-coroutines-core` only (no Compose, no Ktor)

## 4. Key Types

- `Result<T>` — `Success<T>`, `Error`, `Loading`
- `IjsException` — base with `errorCode`, `isRecoverable`
- `ApiException` — HTTP errors with status code
- `AuthException` — authentication/authorization failures
- `ErrorHandler.getErrorInfo()` — user-friendly error info for UI display

