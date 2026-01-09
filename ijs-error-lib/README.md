# ijs-error-lib

A Kotlin Multiplatform library for error handling with user-friendly messages.

## File Structure

```
ijs-error-lib/src/commonMain/kotlin/com/indusjs/error/
├── exception/
│   ├── IjsException.kt          # Base sealed exception class
│   ├── NetworkException.kt      # Connection, timeout, DNS, SSL errors
│   ├── ApiException.kt          # HTTP status code errors (400-5xx)
│   ├── AuthException.kt         # Authentication/authorization errors
│   └── ValidationException.kt   # Input validation errors with field support
├── code/
│   ├── HttpErrorCode.kt         # HTTP status code enum (400-504) with messages
│   └── ErrorMessages.kt         # Centralized human-readable error messages
├── handler/
│   ├── ErrorType.kt             # Error classification enum
│   ├── ErrorInfo.kt             # User-friendly error data (icon, title, message)
│   ├── ErrorContext.kt          # Interface for app-specific error contexts
│   ├── ErrorClassifier.kt       # Classify errors from messages/exceptions
│   └── ErrorHandler.kt          # Main error handler with extensions
└── result/
    └── Result.kt                # Result<T> sealed class (Success, Error, Loading)
```

## Features

- **Exception Hierarchy**: `IjsException` base class with specialized exceptions:
  - `NetworkException` - Connection, timeout, DNS errors
  - `ApiException` - HTTP status code errors
  - `AuthException` - Authentication/authorization errors
  - `ValidationException` - Input validation errors

- **HTTP Error Codes**: Comprehensive `HttpErrorCode` enum with human-readable messages

- **Error Classification**: `ErrorClassifier` to categorize errors by type

- **User-Friendly Messages**: `ErrorHandler` with context-aware error messages

- **Result Type**: Generic `Result<T>` sealed class for operation results

## Usage

### Basic Error Handling

```kotlin
import com.indusjs.error.result.Result
import com.indusjs.error.exception.*
import com.indusjs.error.handler.*

// Using Result
suspend fun fetchData(): Result<Data> {
    return try {
        val data = api.getData()
        Result.Success(data)
    } catch (e: Exception) {
        Result.Error(e, e.message)
    }
}

// Handling Result
when (val result = fetchData()) {
    is Result.Success -> handleSuccess(result.data)
    is Result.Error -> handleError(result.message)
    is Result.Loading -> showLoading()
}
```

### User-Friendly Error Messages

```kotlin
import com.indusjs.error.handler.*

// Get error info
val errorInfo = ErrorHandler.getErrorInfo(error, MyAppContext.VEHICLES)
showError(
    icon = errorInfo.icon,
    title = errorInfo.title,
    message = errorInfo.message
)

// Extension function
val userMessage = error.toUserFriendlyError(MyAppContext.VEHICLES)
```

### Custom Error Context

Implement `ErrorContext` for app-specific error messages:

```kotlin
enum class MyAppContext : ErrorContext {
    VEHICLES {
        override val contextName = "vehicles"
        override fun getNetworkErrorPrefix() = "Unable to load vehicles"
        override fun getNotFoundMessage() = "Vehicle not found"
    },
    // ... more contexts
}
```

## Targets

- Android
- iOS (x64, Arm64, Simulator Arm64)
- JavaScript
- WebAssembly (WasmJS)

## Dependencies

- `kotlinx-coroutines-core`
