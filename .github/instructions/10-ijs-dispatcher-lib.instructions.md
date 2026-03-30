# ijs-dispatcher-lib — IndusJS Fleet

## Purpose

Coroutine dispatcher abstraction. Enables testable coroutine code via dispatcher injection.
**Must be used wherever background work is required** — never use `Dispatchers.IO` directly.

## Package: `com.indusjs.dispatcher`

## Key Types

| Type | Purpose |
|------|---------|
| `DispatcherProvider` | Interface: `main`, `io`, `default`, `unconfined` |
| `DefaultDispatcherProvider` | Production implementation |
| `AndroidDispatcherProvider` | Android-specific |
| `IosDispatcherProvider` | iOS-specific |
| `JsDispatcherProvider` | JS-specific |
| `WasmJsDispatcherProvider` | WasmJS-specific |

## Usage

```kotlin
class MyRepository(private val dispatchers: DispatcherProvider) {
    suspend fun fetchData() = withContext(dispatchers.io) {
        // background work
    }
}
```

## Platform Implementations

Each platform provides its own `actual` implementation via `expect`/`actual` pattern or
direct platform-specific classes.

## Module Path

`ijs-dispatcher-lib/src/commonMain/kotlin/com/indusjs/dispatcher/`

## Depends On: Nothing (pure utility)
## Depended On By: `ijs-core-lib` (via `api()`)

## Common Mistakes

- ❌ `withContext(Dispatchers.IO)` — use `dispatchers.io` from injected `DispatcherProvider`
- ❌ Creating `DispatcherProvider` manually — get from DI graph

