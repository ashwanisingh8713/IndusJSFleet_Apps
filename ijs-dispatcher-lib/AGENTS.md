# AGENTS.md - ijs-dispatcher-lib

## Purpose

Standalone Kotlin Multiplatform library providing coroutine dispatcher abstractions. Enables testability by allowing dispatcher swapping in unit tests. Every ViewModel and repository in the fleet app receives `DispatcherProvider` as a constructor parameter.

**Package:** `com.indusjs.dispatcher`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS, WasmJS  
**Dependencies:** `kotlinx-coroutines-core` only

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/dispatcher/
├── DispatcherProvider.kt         # Interface: main, io, default, unconfined
├── DefaultDispatcherProvider.kt  # Default, Test, and Immediate implementations
├── DispatcherQualifiers.kt       # DI qualifier annotations (@MainDispatcher, @IoDispatcher, etc.)
├── CoroutineScopeProvider.kt     # SupervisorJob scope factory + logging exception handler
└── PlatformDispatcherProvider.kt # expect fun createPlatformDispatcherProvider()
```

---

## Key Types

### DispatcherProvider (interface)

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher      // UI thread updates
    val io: CoroutineDispatcher        // Network/DB/File I/O
    val default: CoroutineDispatcher   // CPU-intensive work
    val unconfined: CoroutineDispatcher // Immediate execution (testing)
}
```

### Implementations

| Class | When to Use |
|-------|-------------|
| `DefaultDispatcherProvider` | Production — `Dispatchers.Main` + `Dispatchers.Default` |
| `TestDispatcherProvider` | Unit tests — all dispatchers → `Unconfined` for synchronous execution |
| `ImmediateDispatcherProvider` | Same as Test — immediate execution |

**Important:** KMP does not have `Dispatchers.IO` on all platforms. `DefaultDispatcherProvider` uses `Dispatchers.Default` for the `io` property. Platform-specific implementations exist (Android uses `Dispatchers.IO`).

### CoroutineScopeProvider (object)

Factory for pre-configured `CoroutineScope` with `SupervisorJob`:
- `createMainScope()`, `createIoScope()`, `createDefaultScope()`
- `createLoggingExceptionHandler(tag)` — logs uncaught coroutine exceptions via Kermit

### DI Qualifiers

Annotation classes for Metro DI: `@MainDispatcher`, `@IoDispatcher`, `@DefaultDispatcher`, `@ApplicationScope`, `@ViewModelScope`

---

## Usage in Fleet App

```kotlin
// ViewModel constructor injection
class VehiclesViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getVehiclesUseCase: GetVehiclesUseCase
) : MviViewModel<State, Intent, Effect>(State()) {
    private suspend fun loadData() = withContext(dispatcherProvider.io) {
        // API call or DB access
    }
}

// DI wiring in DefaultViewModelProvider
override val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()
```
