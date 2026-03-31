# ijs-error-lib — Error Handling Foundation

**Namespace:** `com.indusjs.error`
**Dependencies:** `kotlinx-coroutines-core` only (zero external dependencies)

## File Tree

```
ijs-error-lib/src/commonMain/kotlin/com/indusjs/error/
├── code/
│   ├── HttpErrorCode.kt          # HTTP status code enum
│   └── ErrorMessages.kt          # Centralized error message constants
├── exception/
│   ├── IjsException.kt           # Base sealed exception
│   ├── NetworkException.kt       # Network failures (connection, timeout, DNS, SSL)
│   ├── ApiException.kt           # HTTP API errors (4xx, 5xx)
│   ├── AuthException.kt          # Authentication/authorization errors
│   └── ValidationException.kt    # Input validation errors
├── handler/
│   ├── ErrorType.kt              # Error classification enum
│   ├── ErrorInfo.kt              # User-friendly error display model
│   ├── ErrorContext.kt           # Context-aware error messages
│   ├── ErrorClassifier.kt        # Classifies exceptions → ErrorType
│   └── ErrorHandler.kt           # Main API: exception → ErrorInfo
└── result/
    └── Result.kt                  # Result<T> sealed class
```

## Result<T> Sealed Class

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

**Properties:** `isSuccess`, `isError`, `isLoading`

**Methods:**
| Method | Returns |
|--------|---------|
| `getOrNull()` | `T?` |
| `getOrDefault(default)` | `T` |
| `getOrThrow()` | `T` (throws on Error/Loading) |
| `exceptionOrNull()` | `Throwable?` |
| `map { }` | `Result<R>` |
| `flatMap { }` | `Result<R>` |
| `onSuccess { }` | `Result<T>` (chainable) |
| `onError { }` | `Result<T>` (chainable) |
| `onLoading { }` | `Result<T>` (chainable) |
| `fold(onSuccess, onError, onLoading)` | `R` |

**Companion Factories:** `success(data)`, `error(exception, message?)`, `error(message)`, `loading()`, `runCatching { }`, `runCatchingSuspend { }`

**Extensions:** `T?.toResult(errorMessage)`, `Result<A>.combine(Result<B>)`, `List<Result<T>>.sequence()`

## Exception Hierarchy

```
IjsException (sealed abstract)
  ├── NetworkException (open class)
  │     Types: CONNECTION, TIMEOUT, DNS, SSL, RESET, UNKNOWN
  │     Factories: connection(), timeout(), dns(), ssl()
  │     Always recoverable
  │
  ├── ApiException
  │     Props: httpCode, errorBody
  │     Factories: badRequest(400), notFound(404), serverError(500),
  │                rateLimited(429), fromCode(code, message?, errorBody?)
  │     Computed: isClientError (4xx), isServerError (5xx)
  │
  ├── AuthException
  │     Types: UNAUTHENTICATED, TOKEN_EXPIRED, UNAUTHORIZED, INVALID_CREDENTIALS
  │     Factories: unauthenticated(), tokenExpired(), unauthorized(), invalidCredentials()
  │     requiresReAuthentication: true for UNAUTHENTICATED, TOKEN_EXPIRED, INVALID_CREDENTIALS
  │
  └── ValidationException
        Props: field, validationErrors (Map<String, String>)
        Factories: forField(), required(), invalidFormat(), multiple(),
                   minLength(), maxLength(), range()
```

## HttpErrorCode Enum

17 HTTP status codes with `isRetryable` flag:

| Code | Retryable |
|------|-----------|
| 400 Bad Request | No |
| 401 Unauthorized | Yes |
| 403 Forbidden | No |
| 404 Not Found | No |
| 408 Request Timeout | Yes |
| 422 Unprocessable | No |
| 429 Too Many Requests | Yes |
| 500 Internal Server | Yes |
| 502 Bad Gateway | Yes |
| 503 Service Unavailable | Yes |
| 504 Gateway Timeout | Yes |

**Companion:** `fromCode(code)`, `getMessage(code)`, `isRetryable(code)`, `isClientError`, `isServerError`, `isSuccess`

## ErrorClassifier

Classifies exceptions into `ErrorType` enum values:
- `NETWORK_CONNECTION`, `NETWORK_TIMEOUT`, `SERVER_ERROR`
- `AUTHENTICATION`, `AUTHORIZATION`, `NOT_FOUND`
- `VALIDATION`, `RATE_LIMITED`, `UNKNOWN`

Two entry points:
- `classifyFromException(throwable)` — type-safe matching on `IjsException` subtypes
- `classifyFromMessage(error: String?)` — pattern matching on error message strings

## ErrorHandler

Main API for UI consumption:

```kotlin
ErrorHandler.getErrorInfo(error: String?, context: ErrorContext) → ErrorInfo
ErrorHandler.getErrorInfo(throwable: Throwable, context: ErrorContext) → ErrorInfo
ErrorHandler.getUserFriendlyMessage(error/throwable) → String
```

`ErrorInfo` contains: `icon`, `title`, `message`, `actionLabel`, `errorType`, `isRetryable`

## ErrorContext Interface

Allows screens to customize error messages:

```kotlin
interface ErrorContext {
    val contextName: String  // e.g., "vehicles", "drivers"
    fun getNetworkErrorPrefix(): String      // "Unable to load vehicles"
    fun getTimeoutErrorPrefix(): String      // "Loading vehicles is taking too long"
    fun getNotFoundMessage(): String         // "Vehicle not found"
    fun getUnauthorizedMessage(): String     // "access vehicles"
}
```

`FleetErrorContext` in `ijs-core-lib` provides: `DASHBOARD`, `VEHICLES`, `DRIVERS`, `TRIPS`, `COSTS`, `AUTH`

## ErrorMessages Object

Centralized constants:
- `Network.*` — NO_CONNECTION, TIMEOUT, DNS_FAILURE, SSL_ERROR, etc.
- `Auth.*` — UNAUTHENTICATED, SESSION_EXPIRED, UNAUTHORIZED, etc.
- `Validation.*` — field helpers (required, invalidFormat, minLength, maxLength, range)
- `Api.*` — status/business error strings
- `Generic.*` — UNKNOWN, RETRY, CONTACT_SUPPORT
- `Actions.*` — RETRY, TRY_AGAIN, GO_BACK, SIGN_IN
