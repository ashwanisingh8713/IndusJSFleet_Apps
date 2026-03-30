# Error Handling — IndusJS Fleet

## Result Sealed Class (`ijs-error-lib`)

```kotlin
// com.indusjs.error.result.Result
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

## Exception Hierarchy

```kotlin
// com.indusjs.error.exception
sealed class IjsException(message: String, cause: Throwable?) : Exception
├── ApiException(message, code: Int?)       // API returned error
├── NetworkException(message)               // No connectivity
├── AuthException(message)                  // 401 / token expired
└── ValidationException(message, field?)    // Input validation failure
```

## Usage in Repository

```kotlin
override fun getData(): Flow<Result<T>> = flow {
    emit(Result.Loading)
    try {
        val token = userLocalDataSource.getAuthToken()
        val response = remoteDataSource.fetch(token)
        if (response.success) emit(Result.Success(mapper.toDomain(response.data!!)))
        else emit(Result.Error(ApiException(response.message), response.message))
    } catch (e: Exception) {
        emit(Result.Error(e, e.message))
    }
}
```

## Usage in ViewModel

```kotlin
useCase().collect { result ->
    when (result) {
        is Result.Loading -> updateState { copy(isLoading = true) }
        is Result.Success -> updateState { copy(isLoading = false, data = result.data) }
        is Result.Error -> updateState { copy(isLoading = false, error = result.message) }
    }
}
```

## Error Display in Screen

```kotlin
ScreenContent(
    isLoading = state.isLoading,
    error = state.error,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadData) }
) { /* content */ }
```

## Key Files

- `ijs-error-lib/.../error/result/Result.kt` — Result sealed class
- `ijs-error-lib/.../error/exception/` — Exception hierarchy
- `ijs-error-lib/.../error/handler/ErrorHandler.kt` — Error classification
- `ijs-core-lib/.../core/error/FleetErrorContext.kt` — Screen-specific error context

## Common Mistakes

- ❌ Using `try/catch` without emitting `Result.Error`
- ❌ Ignoring `Result.Loading` state — always show loading indicator
- ❌ Throwing exceptions from Repository — always wrap in `Result.Error`

