# ijs-dispatcher-lib — Coroutine Dispatchers

**Namespace:** `com.indusjs.dispatcher`
**Dependencies:** `kotlinx-coroutines-core` only

## File Tree

```
ijs-dispatcher-lib/src/
├── commonMain/kotlin/com/indusjs/dispatcher/
│   ├── CoroutineScopeProvider.kt
│   ├── DefaultDispatcherProvider.kt
│   ├── DispatcherProvider.kt
│   ├── DispatcherQualifiers.kt
│   └── PlatformDispatcherProvider.kt
├── androidMain/.../AndroidDispatcherProvider.kt
├── iosMain/.../IosDispatcherProvider.kt
├── jsMain/.../JsDispatcherProvider.kt
└── wasmJsMain/.../WasmJsDispatcherProvider.kt
```

## DispatcherProvider Interface

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher       // UI thread
    val io: CoroutineDispatcher         // I/O operations
    val default: CoroutineDispatcher    // CPU-intensive work
    val unconfined: CoroutineDispatcher // Unconfined
}
```

## Implementations

| Class | `io` Dispatcher | When Used |
|-------|----------------|-----------|
| `DefaultDispatcherProvider` | `Dispatchers.Default` | Default (KMP-safe) |
| `AndroidDispatcherProvider` | `Dispatchers.IO` | Android platform |
| `IosDispatcherProvider` | `Dispatchers.Default` | iOS (no IO dispatcher) |
| `JsDispatcherProvider` | `Dispatchers.Default` | JS browser |
| `WasmJsDispatcherProvider` | `Dispatchers.Default` | WasmJS browser |
| `TestDispatcherProvider` | `Dispatchers.Unconfined` | Unit tests |
| `ImmediateDispatcherProvider` | `Dispatchers.Unconfined` | Immediate execution |

## Platform Factory

```kotlin
expect fun createPlatformDispatcherProvider(): DispatcherProvider
// actual: Android → AndroidDispatcherProvider
// actual: iOS → IosDispatcherProvider
// actual: JS/WasmJS → DefaultDispatcherProvider
```

**Note:** `DefaultViewModelProvider` currently uses `DefaultDispatcherProvider()` (not `createPlatformDispatcherProvider()`), so Android's `Dispatchers.IO` is not used through the DI path.

## CoroutineScopeProvider

```kotlin
object CoroutineScopeProvider {
    fun createMainScope(tag): ManagedCoroutineScope
    fun createIoScope(dispatcherProvider, tag): ManagedCoroutineScope
    fun createDefaultScope(dispatcherProvider, tag): ManagedCoroutineScope
    fun createScope(dispatcher, tag): ManagedCoroutineScope
}
```

`ManagedCoroutineScope` wraps a `CoroutineScope` with `isActive`, `cancel()`, `reset()`.

## DI Qualifier Annotations

```kotlin
@MainDispatcher       // Qualify main dispatcher injection
@IoDispatcher         // Qualify IO dispatcher injection
@DefaultDispatcher    // Qualify default dispatcher injection
@UnconfinedDispatcher // Qualify unconfined dispatcher injection
@ApplicationScope     // Qualify app-scoped coroutine scope
@ViewModelScope       // Qualify VM-scoped coroutine scope
```

## Usage Pattern

```kotlin
// In a repository or data source
class MyRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider
) {
    suspend fun fetchData() = withContext(dispatcherProvider.io) {
        // Network call here
    }
}
```
