# ijs-error-lib — IndusJS Fleet

## Purpose

Error handling foundation. Provides `Result<T>` sealed class and exception hierarchy.
**Zero external dependencies** beyond coroutines.

## Package: `com.indusjs.error`

## Key Types

| Type | File | Purpose |
|------|------|---------|
| `Result<T>` | `result/Result.kt` | `Success<T>`, `Error`, `Loading` |
| `IjsException` | `exception/IjsException.kt` | Base sealed exception |
| `ApiException` | `exception/ApiException.kt` | API errors with HTTP code |
| `NetworkException` | `exception/NetworkException.kt` | Connectivity failures |
| `AuthException` | `exception/AuthException.kt` | Authentication (401) |
| `ValidationException` | `exception/ValidationException.kt` | Input validation |
| `ErrorHandler` | `handler/ErrorHandler.kt` | Classifies exceptions |
| `ErrorClassifier` | `handler/ErrorClassifier.kt` | Determines error type |
| `ErrorContext` | `handler/ErrorContext.kt` | Screen-specific context |

## Usage

```kotlin
import com.indusjs.error.result.Result
import com.indusjs.error.exception.ApiException

when (result) {
    is Result.Success -> handleData(result.data)
    is Result.Error -> handleError(result.exception, result.message)
    is Result.Loading -> showLoading()
}
```

## Module Path

`ijs-error-lib/src/commonMain/kotlin/com/indusjs/error/`

## Depends On: Nothing (pure utility)
## Depended On By: `ijs-core-lib` (via `api()`)

