# AGENTS.md - ijs-error-lib

## Purpose

Standalone Kotlin Multiplatform library providing error handling primitives for all IndusJS apps. Every error type, result wrapper, and user-friendly error classification used across the fleet app originates here.

**Package:** `com.indusjs.error`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS, WasmJS  
**Dependencies:** `kotlinx-coroutines-core` only (no Compose, no Ktor)

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/error/
├── exception/
│   ├── IjsException.kt          # Base sealed class (errorCode, isRecoverable)
│   ├── NetworkException.kt      # CONNECTION | TIMEOUT | DNS | SSL
│   ├── ApiException.kt          # HTTP 4xx/5xx with httpCode, errorBody
│   ├── AuthException.kt         # UNAUTHENTICATED | TOKEN_EXPIRED | UNAUTHORIZED | INVALID_CREDENTIALS
│   └── ValidationException.kt   # Field-level validation (field, validationErrors map)
├── code/
│   ├── HttpErrorCode.kt         # Enum for HTTP 400-504 with human messages
│   └── ErrorMessages.kt         # Centralized error message constants
├── handler/
│   ├── ErrorType.kt             # Classification: NETWORK_CONNECTION, SERVER_ERROR, AUTH, VALIDATION, etc.
│   ├── ErrorInfo.kt             # User-friendly error data (icon, title, message, actionLabel, isRetryable)
│   ├── ErrorContext.kt          # Interface for screen-specific error messages
│   ├── ErrorClassifier.kt       # Classify errors from messages or exceptions
│   └── ErrorHandler.kt          # Main: getErrorInfo(error, context) → ErrorInfo
└── result/
    └── Result.kt                # Result<T> sealed class (258 lines)
```

---

## Key Types

### Result<T> (`result/Result.kt`)

The universal return type for all async operations in the app:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

**Extensions:** `getOrNull()`, `getOrDefault(default)`, `map { }`, `flatMap { }`, `onSuccess { }`, `onError { }`, `fold(onSuccess, onError)`, `toUnit()`

### Exception Hierarchy

```
IjsException (sealed, abstract: errorCode, isRecoverable)
├── NetworkException     - type: CONNECTION | TIMEOUT | DNS | SSL
├── ApiException         - httpCode: Int?, errorBody: String?, isClientError, isServerError
├── AuthException        - type: UNAUTHENTICATED | TOKEN_EXPIRED | UNAUTHORIZED | INVALID_CREDENTIALS
└── ValidationException  - field: String?, validationErrors: Map<String, String>
```

**Factory methods on ApiException:** `badRequest()`, `unauthorized()`, `forbidden()`, `notFound()`, `conflict()`, `serverError()`

### ErrorHandler

Main entry for user-facing messages:
```kotlin
val errorInfo = ErrorHandler.getErrorInfo(throwable, FleetErrorContext.VEHICLES)
// → ErrorInfo(icon, title, message, actionLabel, isRetryable)

// Convenience extension:
val info = "Some error".toErrorInfo(FleetErrorContext.TRIPS)
```

### ErrorContext (interface)

Implement in the app for screen-specific error messages:
```kotlin
override fun getNetworkErrorPrefix(): String  // "Unable to load vehicles"
override fun getTimeoutErrorPrefix(): String
override fun getNotFoundMessage(): String     // "Vehicle not found"
override fun getUnauthorizedMessage(): String
```

The fleet app implements this as `FleetErrorContext` enum in `sharedUI/core/error/FleetErrorContext.kt`.

---

## Usage from sharedUI

```kotlin
import com.indusjs.error.result.Result
import com.indusjs.error.exception.ApiException
import com.indusjs.error.handler.ErrorHandler

// In repositories
emit(Result.Error(ApiException.notFound("Vehicle not found"), "Vehicle not found"))

// In ViewModels
when (result) {
    is Result.Success -> updateState { copy(data = result.data) }
    is Result.Error -> updateState { copy(error = result.errorMessage) }
    is Result.Loading -> updateState { copy(isLoading = true) }
}

// In UI composables
ErrorContent(
    error = state.error,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadData) }
)
```
