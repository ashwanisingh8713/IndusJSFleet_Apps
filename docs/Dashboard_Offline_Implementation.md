# Dashboard Offline/Online Implementation

## Overview

This document describes the implementation of offline-first caching for the DashboardScreen using a Settings-based approach that works across Android and iOS platforms.

## Architecture

Following CLEAN Architecture + MVI pattern:

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                             │
├─────────────────────────────────────────────────────────────────────┤
│  DashboardScreen.kt ←→ DashboardViewModel.kt ←→ DashboardContract.kt│
│                              │                                       │
│                              ↓ uses                                  │
└─────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                                 │
├─────────────────────────────────────────────────────────────────────┤
│  Use Cases:                                                          │
│  • GetDashboardUseCase     - Offline-first fetch (Flow)             │
│  • RefreshDashboardUseCase - Force network refresh                  │
│  • ObserveDashboardUseCase - Observe cache changes                  │
│  • HasCachedDashboardUseCase                                        │
│  • ClearDashboardCacheUseCase                                       │
│                              │                                       │
│  Repository Interface:                                               │
│  DashboardRepository                                                 │
└─────────────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                                  │
├─────────────────────────────────────────────────────────────────────┤
│  DashboardRepositoryImpl (offline-first strategy)                   │
│                    ↓                           ↓                    │
│  ┌─────────────────────────┐    ┌───────────────────────────────┐  │
│  │  DashboardRemoteSource  │    │   DashboardLocalDataSource    │  │
│  │  (Ktor HTTP client)     │    │   (Settings-based cache)      │  │
│  └─────────────────────────┘    └───────────────────────────────┘  │
│                                              ↓                      │
│  ┌───────────────────────────────────────────────────────────────┐ │
│  │                    FleetDatabase                              │ │
│  │  SettingsDashboardDao (multiplatform-settings)               │ │
│  └───────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

## Key Files

### Core Utilities
- `core/util/TimeUtils.kt` - Platform-agnostic time utilities (expect/actual)

### Data Layer
- `data/database/FleetDatabase.kt` - Database class using Settings
- `data/database/dao/DashboardDao.kt` - DAO interface + SettingsDashboardDao
- `data/database/entity/DashboardCacheEntity.kt` - Serializable cache entity
- `data/datasource/dashboard/DashboardLocalDataSource.kt` - Local data source
- `data/mapper/dashboard/DashboardCacheMapper.kt` - Entity ↔ DTO mapper
- `data/repository/dashboard/DashboardRepositoryImpl.kt` - Offline-first repository

### Domain Layer
- `domain/repository/dashboard/DashboardRepository.kt` - Repository interface
- `domain/usecase/dashboard/DashboardUseCases.kt` - All use cases

### DI
- `di/DashboardFeatureGraph.kt` - Feature DI graph
- `di/DefaultViewModelProvider.kt` - Manual DI wiring

## Data Flow

### Online Flow (Has Cache):
1. User opens app → ViewModel calls `GetDashboardUseCase`
2. Repository emits cached data first (instant UI)
3. Repository fetches fresh from API in background
4. On success: saves to cache, emits fresh data
5. UI updates with fresh data

### Offline Flow (Has Cache):
1. User opens app (no network)
2. Repository emits cached data from Settings
3. API call fails → error is suppressed (we have cache)
4. UI shows cached data with offline banner

### First Launch (No Cache, Online):
1. Repository emits `Result.Loading`
2. API call made → on success, saves and emits data
3. UI shows loading spinner, then content

### First Launch (No Cache, Offline):
1. Repository emits `Result.Loading`
2. API fails → emits error
3. UI shows error screen with retry button

## Platform Support

| Platform | Offline Support | Time Utils |
|----------|-----------------|------------|
| Android  | ✅ Settings-based caching | ✅ System.currentTimeMillis() |
| iOS      | ✅ Settings-based caching | ✅ NSDate.timeIntervalSince1970 |
| JS       | ❌ No offline support | ✅ kotlin.js.Date.now() |
| WasmJS   | ❌ No offline support | ✅ JsFun Date.now() |

### Platform-Specific Time Implementation

Each platform has its own `TimeUtils` implementation:

- **Android** (`androidMain`): Uses `System.currentTimeMillis()`
- **iOS** (`iosMain`): Uses `NSDate().timeIntervalSince1970 * 1000`
- **JS** (`jsMain`): Uses `kotlin.js.Date.now()`
- **WasmJS** (`wasmJsMain`): Uses `@JsFun("() => Date.now()")` external function

## Key Design Decisions

1. **Settings-based Storage**: Uses `multiplatform-settings` for cross-platform persistence
2. **Offline-First Strategy**: Cache emitted first for instant UI
3. **Use Cases**: Business logic in domain layer
4. **Flow-based**: Reactive cache observation using Kotlin Flow
5. **Result Wrapper**: Consistent error handling with `Result<T>`

## Refactoring Completed

### Code Cleanup
- Removed redundant `else` branch in AddVehicleScreen
- Added `@Suppress("DEPRECATION")` for Ktor's `readBytes()` deprecation
- Separated JS and WasmJS time implementations into their own source sets
- Removed empty webMain directories

### File Structure
```
src/
├── androidMain/kotlin/.../core/util/TimeUtils.android.kt
├── iosMain/kotlin/.../core/util/TimeUtils.ios.kt
├── jsMain/kotlin/.../core/util/TimeUtils.js.kt
├── wasmJsMain/kotlin/.../core/util/TimeUtils.wasmJs.kt
└── commonMain/kotlin/.../core/util/TimeUtils.kt (expect declaration)
```



