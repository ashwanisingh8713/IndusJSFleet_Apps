# Clean Architecture — IndusJS Fleet

## Layer Rules (Strict)

Every feature module follows **Domain → Data → Presentation** layering:

| Layer | Contains | Depends On |
|-------|----------|------------|
| **Domain** | Entities, Repository interfaces, Use Cases | Nothing (pure Kotlin) |
| **Data** | DTOs (`@Serializable`), Mappers, DataSources, Repository Impls | Domain layer |
| **Presentation** | Contract, ViewModel, Screen, Facade | Domain layer (via Use Cases) |

## Folder Structure (Feature Module)

```
screen-{feature}/src/commonMain/kotlin/com/ijs/{feature}/
├── data/
│   ├── datasource/{feature}/    # RemoteDataSource + Impl
│   ├── mapper/{feature}/        # DTO ↔ Entity mappers
│   ├── model/{feature}/         # DTOs with @Serializable + @SerialName
│   └── repository/{feature}/    # RepositoryImpl
├── domain/
│   ├── entity/{feature}/        # Pure Kotlin data classes
│   ├── repository/{feature}/    # Repository interfaces
│   └── usecase/{feature}/       # Use case classes
└── presentation/                # Contract, ViewModel, Screen, Facade
```

## Data Flow (Request Lifecycle)

```
User action → Screen → viewModel.sendIntent(Intent)
  → ViewModel.handleIntent() → UseCase() → Repository.getData()
    → Repository: emit(Loading) → DataSource.apiCall(token) → emit(Success/Error)
  → ViewModel: updateState { copy(...) }
  → Screen: state.collectAsStateWithLifecycle() → recompose
```

## Key Rules

- Domain layer has **zero** framework dependencies (no Ktor, no Compose, no Android)
- Repository interface lives in `domain/`; implementation in `data/`
- DTOs and Entities are **separate** classes — use Mappers to convert
- Use Cases wrap a single repository call or compose multiple
- Presentation never calls DataSource directly — always via UseCase → Repository
- `ijs-core-lib` provides base interfaces: `DataSource`, `Mapper`, `Repository`, `UseCase`, `Entity`

## Key Files

- Base classes: `ijs-core-lib/src/commonMain/.../core/base/`
- Error handling: `ijs-error-lib/src/commonMain/.../error/result/Result.kt`
- Dispatchers: `ijs-dispatcher-lib/src/commonMain/.../dispatcher/DispatcherProvider.kt`

## 500-Line Class Limit (STRICT)

Every class file **must not exceed 500 lines**. Split into focused classes:
- `{Feature}DataLoader` — data fetching logic
- `{Feature}StateReducer` — state update logic
- `{Feature}ActionHandler` — action/command logic

