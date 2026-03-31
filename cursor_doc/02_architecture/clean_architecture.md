# Clean Architecture in IndusJS Fleet

## Layer Structure

```
┌───────────────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                                │
│  Screen (Compose) ←→ ViewModel (MviViewModel) ←→ Contract (S/I/E)    │
│  Location: screen-*/presentation/ or sharedUI (nav wiring only)       │
└───────────────────────────────────────┬───────────────────────────────┘
                                        ↓ Uses
┌───────────────────────────────────────┴───────────────────────────────┐
│                       DOMAIN LAYER                                    │
│  Entities (data class) ←→ Use Cases ←→ Repository Interfaces          │
│  Location: screen-*/domain/                                           │
└───────────────────────────────────────┬───────────────────────────────┘
                                        ↓ Implements
┌───────────────────────────────────────┴───────────────────────────────┐
│                        DATA LAYER                                     │
│  DTOs (@Serializable) ←→ Mappers ←→ DataSources ←→ Repository Impls  │
│  Location: screen-*/data/                                             │
└───────────────────────────────────────────────────────────────────────┘
```

## Folder Structure (Feature Module)

Every full-stack feature module follows this layout:

```
screen-{feature}/src/commonMain/kotlin/com/ijs/{feature}/
├── domain/
│   ├── entity/          # Pure Kotlin data classes (domain models)
│   ├── repository/      # Repository interfaces (contracts)
│   └── usecase/         # Use case classes (business logic orchestration)
├── data/
│   ├── model/           # DTOs with @Serializable + @SerialName
│   ├── datasource/      # Remote data source interface + implementation
│   ├── mapper/          # DTO ↔ Entity bidirectional mappers
│   └── repository/      # Repository implementations
└── presentation/
    ├── {Feature}FeatureFacade.kt    # Composable entry points (navigation-agnostic)
    ├── {Feature}Contract.kt         # State, Intent, Effect
    ├── {Feature}ViewModel.kt        # MVI ViewModel
    └── {Feature}Screen.kt           # Compose UI
```

## Layer Rules

### Domain Layer
- **Pure Kotlin** — no framework dependencies (no Compose, no Ktor, no serialization annotations)
- Entities are `data class` with business logic methods
- Repository interfaces define contracts only
- Use cases orchestrate repository calls and return `Flow<Result<T>>`

### Data Layer
- DTOs always use `@Serializable` and `@SerialName("snake_case")`
- Mappers convert between DTOs and domain entities
- Remote data sources handle HTTP calls via Ktor
- Repository implementations: emit `Result.Loading` → call API → emit `Result.Success` or `Result.Error`
- Auth tokens obtained from `UserLocalDataSource` per request

### Presentation Layer
- Contract defines immutable State, sealed Intent, sealed Effect
- ViewModel extends `MviViewModel<State, Intent, Effect>`
- Screens are `@Composable` functions observing state via `collectAsStateWithLifecycle()`
- Navigation via `sendEffect(Effect.NavigateTo(...))` — screen never knows about nav library

## Module Responsibility Split

The project splits responsibilities across modules:

| Concern | Where It Lives |
|---------|---------------|
| Domain entities + use cases | `screen-*` feature modules |
| DTOs + data sources + repos | `screen-*` feature modules |
| Screens + ViewModels + Contracts | `screen-*` feature modules |
| Feature facades (composable entry) | `screen-*` feature modules |
| Navigation wiring | `sharedUI/navigation/` |
| ViewModel creation (DI) | `sharedUI/di/DefaultViewModelProvider` |
| Repository wiring (DI) | `sharedUI/di/FeatureRepositoryFactory` |
| HTTP client + auth | `ijs-network-lib` |
| Shared DTOs (costs, history) | `ijs-core-lib` |
| MVI base classes | `ijs-core-lib` |
| Error handling | `ijs-error-lib` |
| Theme + reusable UI | `ijs-ui-components-lib` |

## Deviations from Pure Clean Architecture

1. **Payment, Team, Finance** modules skip the use case layer — ViewModels call repositories directly
2. **Trip detail** decomposes the ViewModel into handler classes (`TripDetailDataLoader`, `TripDetailActionHandler`, etc.)
3. **Customer detail** has handler classes per concern + PDF exporter
4. **Finance** uses a single shared ViewModel across 4 screens (list/detail/purchase/EMI)
5. **Shared DTOs** for costs, history, state history live in `ijs-core-lib` to avoid circular dependencies
6. **User/auth data layer** lives in `ijs-network-lib`, not in `screen-user`
