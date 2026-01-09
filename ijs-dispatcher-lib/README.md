# ijs-dispatcher-lib

A Kotlin Multiplatform library for coroutine dispatchers and scope management.

## File Structure

```
ijs-dispatcher-lib/src/
├── commonMain/kotlin/com/indusjs/dispatcher/
│   ├── DispatcherProvider.kt        # Interface for coroutine dispatchers (main, io, default, unconfined)
│   ├── DefaultDispatcherProvider.kt # Default, Test, and Immediate implementations
│   ├── DispatcherQualifiers.kt      # DI qualifiers (@MainDispatcher, @IoDispatcher, etc.)
│   ├── CoroutineScopeProvider.kt    # SupervisorJob scope utilities & ManagedCoroutineScope
│   └── PlatformDispatcherProvider.kt # expect fun createPlatformDispatcherProvider()
├── androidMain/kotlin/com/indusjs/dispatcher/
│   └── AndroidDispatcherProvider.kt # Android implementation with Dispatchers.IO
├── iosMain/kotlin/com/indusjs/dispatcher/
│   └── IosDispatcherProvider.kt     # iOS implementation
├── jsMain/kotlin/com/indusjs/dispatcher/
│   └── JsDispatcherProvider.kt      # JavaScript implementation
└── wasmJsMain/kotlin/com/indusjs/dispatcher/
    └── WasmJsDispatcherProvider.kt  # WebAssembly JS implementation
```

## Features

- **DispatcherProvider**: Interface for providing coroutine dispatchers
  - `main` - UI thread dispatcher
  - `io` - I/O operations dispatcher
  - `default` - CPU-intensive work dispatcher
  - `unconfined` - Immediate execution dispatcher

- **Platform Implementations**:
  - `AndroidDispatcherProvider` - Uses `Dispatchers.IO` on Android
  - `IosDispatcherProvider` - iOS-optimized dispatchers
  - `DefaultDispatcherProvider` - Cross-platform fallback

- **DI Qualifiers**: Annotations for dependency injection:
  - `@MainDispatcher`
  - `@IoDispatcher`
  - `@DefaultDispatcher`
  - `@ApplicationScope`
  - `@ViewModelScope`

- **Scope Management**: `CoroutineScopeProvider` with SupervisorJob support

## Usage

### Basic Usage

```kotlin
import com.indusjs.dispatcher.*

// Create dispatcher provider
val dispatchers = createPlatformDispatcherProvider()

// Use in repository
class MyRepository(
    private val dispatchers: DispatcherProvider
) {
    suspend fun fetchData() = withContext(dispatchers.io) {
        // Network call
    }
}
```

### With Dependency Injection (Metro)

```kotlin
@SingleIn(AppScope::class)
@DependencyGraph
abstract class AppGraph {
    
    @Provides
    @SingleIn(AppScope::class)
    fun provideDispatcherProvider(): DispatcherProvider = createPlatformDispatcherProvider()
    
    abstract val dispatcherProvider: DispatcherProvider
}
```

### Using Scope Provider

```kotlin
import com.indusjs.dispatcher.CoroutineScopeProvider

class MyClass {
    private val scope = CoroutineScopeProvider.createIoScope(
        exceptionHandler = CoroutineScopeProvider.createLoggingExceptionHandler("MyClass")
    )
    
    fun doWork() {
        scope.launch {
            // Work with SupervisorJob - failures don't cancel siblings
        }
    }
    
    fun cleanup() {
        scope.cancel()
    }
}
```

### Testing

```kotlin
class MyTest {
    private val testDispatchers = TestDispatcherProvider()
    private val repository = MyRepository(testDispatchers)
    
    @Test
    fun testFetch() = runTest {
        // Tests run immediately with Unconfined dispatchers
        val result = repository.fetchData()
        // assertions
    }
}
```

## Targets

- Android (with Dispatchers.IO support)
- iOS (x64, Arm64, Simulator Arm64)
- JavaScript
- WebAssembly (WasmJS)

## Dependencies

- `kotlinx-coroutines-core`
- `kotlinx-coroutines-android` (Android only)

