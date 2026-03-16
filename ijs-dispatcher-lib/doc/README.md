# ijs-dispatcher-lib — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.dispatcher`

---

## 1. Purpose

Coroutine dispatcher abstractions for testability. Enables dispatcher swapping in unit tests so ViewModels and repositories can be tested synchronously.

## 2. Package Structure

```
src/commonMain/kotlin/com/indusjs/dispatcher/
├── DispatcherProvider.kt         # Interface: main, io, default, unconfined
├── DefaultDispatcherProvider.kt  # Production + Test + Immediate implementations
├── DispatcherQualifiers.kt       # DI qualifier annotations
├── CoroutineScopeProvider.kt     # SupervisorJob scope factory
└── PlatformDispatcherProvider.kt # expect fun for platform-specific dispatchers
```

## 3. Dependencies

- `kotlinx-coroutines-core` only

## 4. Key Types

- `DispatcherProvider` — interface with `main`, `io`, `default`, `unconfined`
- `DefaultDispatcherProvider` — production implementation
- `TestDispatcherProvider` — all dispatchers → `Unconfined`
- `CoroutineScopeProvider` — supervised scope factory with error logging

