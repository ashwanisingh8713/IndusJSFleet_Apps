# Architecture Guide

## Overview

IndusJS Fleet follows **Clean Architecture** principles with the **MVI (Model-View-Intent)** pattern for the presentation layer. This guide documents the architectural decisions and patterns used throughout the application.

---

## Architecture Layers

```
┌───────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                           │
│         UI (Compose)  ←→  ViewModel (MVI)  ←→  Contract           │
└───────────────────────────────────────────────────────────────────┘
                                ↓ Uses
┌───────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                               │
│        Use Cases  ←→  Domain Entities  ←→  Repository Interfaces  │
└───────────────────────────────────────────────────────────────────┘
                                ↓ Implements
┌───────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                               │
│      Repository Impl  ←→  Data Sources  ←→  DTOs / Mappers        │
└───────────────────────────────────────────────────────────────────┘
```

---

## Folder Structure

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── core/                          # Core utilities
├── data/                          # Data layer
│   ├── datasource/{feature}/      # Remote & Local data sources
│   ├── mapper/{feature}/          # DTO ↔ Entity mappers
│   ├── model/{feature}/           # DTOs
│   └── repository/{feature}/      # Repository implementations
├── di/                            # Dependency Injection
├── domain/                        # Domain layer
│   ├── entity/{feature}/          # Domain entities
│   ├── repository/{feature}/      # Repository interfaces
│   └── usecase/{feature}/         # Use cases
├── navigation/                    # Navigation
├── presentation/{feature}/        # Presentation layer
│   ├── {Feature}Contract.kt       # State, Intent, Effect
│   ├── {Feature}ViewModel.kt      # MVI ViewModel
│   └── {Feature}Screen.kt         # Compose UI
└── theme/                         # App theming
```

---

## MVI Pattern

### Overview

MVI (Model-View-Intent) provides unidirectional data flow:

```
┌────────────────────────────────────────────────────────────────┐
│   User Action                                                  │
│        │                                                       │
│        ▼                                                       │
│   ┌─────────┐    Intent    ┌────────────┐    State    ┌─────┐ │
│   │   UI    │─────────────▶│  ViewModel │────────────▶│ UI  │ │
│   │ (View)  │              │  (Model)   │             │     │ │
│   └─────────┘              └────────────┘             └─────┘ │
│        ▲                         │                            │
│        │                         │ Effect                     │
│        │                         ▼                            │
│        └─────────── One-time Events ──────────────────────────┘
│              (Navigation, Snackbar, etc.)                      │
└────────────────────────────────────────────────────────────────┘
```

### Components

| Component | Purpose | Example |
|-----------|---------|---------|
| **State** | Immutable UI state | isLoading, vehicles list, error message |
| **Intent** | User actions | LoadData, Refresh, ItemClick |
| **Effect** | One-time events | Navigate, ShowSnackbar, ShowDialog |

---

## Contract Pattern

Every feature defines a Contract object containing State, Intent, and Effect.

### State

Represents the complete UI state at any moment:

| Property Type | Example |
|---------------|---------|
| Loading flags | isLoading, isRefreshing |
| Data | vehicles list, selectedItem |
| Error | error message string |
| UI state | selectedTab, filterOptions |

### Intent

Represents user actions and system events:

| Intent Type | Example |
|-------------|---------|
| Load data | LoadVehicles, Refresh |
| User interaction | ItemClick, FilterSelect |
| Input change | UpdateEmail, UpdatePassword |
| Navigation | NavigateToDetail |

### Effect

One-time events that don't persist in state:

| Effect Type | Example |
|-------------|---------|
| Navigation | NavigateToDetail, NavigateBack |
| Messages | ShowSnackbar, ShowToast |
| Dialogs | ShowConfirmDialog |
| External | OpenDialer, ShareContent |

---

## Base ViewModel

All ViewModels extend the base MviViewModel which provides:

| Feature | Description |
|---------|-------------|
| State Flow | StateFlow for reactive UI updates |
| Effect Flow | SharedFlow for one-time events |
| Intent Handler | Abstract method to process intents |
| State Update | Helper to update state immutably |
| Effect Emit | Helper to emit one-time effects |

---

## Data Layer

### Repository Pattern

Repositories abstract data sources from the domain layer:

| Component | Responsibility |
|-----------|----------------|
| Repository Interface | Defines data operations (domain layer) |
| Repository Implementation | Implements interface, coordinates sources |
| Remote Data Source | API calls using Ktor |
| Local Data Source | Room database operations |
| Mapper | Converts DTOs ↔ Domain Entities |

### Data Flow

```
ViewModel → UseCase → Repository → DataSource → API/Database
    ↑                                              │
    └──────── Result<Entity> ◄─────── Response ────┘
```

### DTOs vs Entities

| DTO (Data Transfer Object) | Domain Entity |
|----------------------------|---------------|
| Matches API response | Clean domain model |
| Uses @SerialName for JSON | Uses meaningful property names |
| May have nullable fields | Has business logic |
| Lives in data layer | Lives in domain layer |

---

## Dependency Injection

IndusJS Fleet uses **Metro** for dependency injection.

### Key Concepts

| Concept | Description |
|---------|-------------|
| @Inject | Marks class for constructor injection |
| @DependencyGraph | Defines a DI container |
| @Provides | Provides a dependency instance |
| @Binds | Binds interface to implementation |
| @SingleIn | Scopes to a lifecycle |

### Graph Hierarchy

| Graph | Scope | Contents |
|-------|-------|----------|
| RootGraph | App-level | HttpClient, DispatcherProvider, Json |
| FeatureGraph | Feature-level | Repository, ViewModel |

---

## Navigation

### Type-Safe Routes

Navigation uses Navigation 3 with serializable routes:

| Route Type | Example |
|------------|---------|
| Object | FleetRoute.Dashboard |
| Data Class | FleetRoute.VehicleDetail(vehicleId) |

### Navigation Handling

| Location | Responsibility |
|----------|----------------|
| FleetRoute.kt | Route definitions |
| FleetNavigation.kt | NavHost with composable destinations |
| ViewModel | Emit navigation effects |
| Screen | Handle effects and call navController |

---

## Error Handling

### Result Sealed Class

All async operations return Result:

| Type | Description |
|------|-------------|
| Success | Contains data |
| Error | Contains exception and message |
| Loading | Indicates loading state |

### Exception Types

| Exception | Use Case |
|-----------|----------|
| NotAuthenticatedException | Token missing or expired |
| ApiException | Server returned error |
| NetworkException | No internet connection |
| ValidationException | Input validation failed |

### Error Handling Flow

```
API Call → Try/Catch → Map to Result → Update State → Show in UI
```

---

## Best Practices

### DO ✅

| Practice | Reason |
|----------|--------|
| Use @Inject on class | Metro convention |
| Use Result for async ops | Consistent error handling |
| Use updateState with copy() | Immutable state updates |
| Use sendEffect for navigation | One-time events |
| Add @SerialName to DTOs | Explicit JSON mapping |
| Handle all Result types | Complete error handling |

### DON'T ❌

| Anti-Pattern | Alternative |
|--------------|-------------|
| mutableStateOf in ViewModel | Use MviViewModel state |
| Direct API calls in UI | Use Repository pattern |
| Hardcoded colors | Use MaterialTheme |
| Skip error handling | Always handle Result.Error |
| Use !! operator | Handle nulls properly |

---

## Testing Strategy

### Unit Tests

| Layer | What to Test |
|-------|--------------|
| ViewModel | Intent → State transformations |
| Repository | Data source coordination |
| UseCase | Business logic |
| Mapper | DTO ↔ Entity conversion |

### Integration Tests

| Test | Scope |
|------|-------|
| Repository + DataSource | API integration |
| ViewModel + Repository | Feature flow |

---

## Platform-Specific Implementation

### Multiplatform Structure

| Target | Implementation |
|--------|----------------|
| Common | Shared business logic, UI |
| Android | Android-specific (Activity, Firebase) |
| iOS | iOS-specific (Swift interop) |

### Expect/Actual Pattern

Used for platform-specific implementations:
- Dispatchers
- Secure storage
- File handling
- Native features

---

## Related Documentation

- [User Roles & Permissions](../user-roles/README.md)
- [Modules Overview](../modules/README.md)
- [API Reference](../postman_collections/Fleet_Management_API_v2.postman_collection.json)
