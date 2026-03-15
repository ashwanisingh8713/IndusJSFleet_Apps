# New Feature Implementation Guide

> Use this prompt when adding a complete new feature to the IndusJS Fleet app.

## Architecture

This project uses **Clean Architecture + MVI** in Kotlin Multiplatform. Every feature spans 3 layers + DI + Navigation.

## Step-by-Step Checklist

### 1. Domain Layer (Pure Kotlin — no framework deps)

**Entity** → `sharedUI/.../domain/entity/{feature}/`
```kotlin
data class MyEntity(
    val id: String,
    val name: String,
    val status: String,
    val createdAt: String? = null
)
```

**Repository Interface** → `sharedUI/.../domain/repository/{feature}/`
```kotlin
interface MyRepository {
    fun getAll(): Flow<Result<List<MyEntity>>>
    fun getById(id: String): Flow<Result<MyEntity>>
    suspend fun create(request: CreateMyRequest): Result<MyEntity>
    suspend fun update(id: String, request: UpdateMyRequest): Result<MyEntity>
    suspend fun delete(id: String): Result<Unit>
}
```

**Use Cases** → `sharedUI/.../domain/usecase/{feature}/`
```kotlin
class GetMyListUseCase(private val repository: MyRepository) {
    operator fun invoke(): Flow<Result<List<MyEntity>>> = repository.getAll()
}
class GetMyByIdUseCase(private val repository: MyRepository) {
    operator fun invoke(id: String): Flow<Result<MyEntity>> = repository.getById(id)
}
```

### 2. Data Layer

**DTOs** → `sharedUI/.../data/model/{feature}/`
```kotlin
@Serializable
data class MyDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class MyListResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("data") val data: List<MyDto>? = null,
    @SerialName("message") val message: String? = null
)
```
- Always use `@SerialName` with `snake_case` matching the API
- Kotlin properties use `camelCase`
- All fields nullable with defaults where API may omit them

**Mapper** → `sharedUI/.../data/mapper/{feature}/`
```kotlin
class MyMapper {
    fun toDomain(dto: MyDto): MyEntity = MyEntity(
        id = dto.id.toString(),
        name = dto.name,
        status = dto.status,
        createdAt = dto.createdAt
    )
    fun toDomainList(dtos: List<MyDto>): List<MyEntity> = dtos.map { toDomain(it) }
}
```

**DataSource** → `sharedUI/.../data/datasource/{feature}/`
```kotlin
class MyRemoteDataSourceImpl(private val httpClient: HttpClient) {
    suspend fun getAll(token: String): MyListResponse {
        return httpClient.get("${ApiConfig.BASE_URL}/my-feature") {
            headers { append(HttpHeaders.Authorization, "Bearer $token") }
        }.body()
    }
}
```

**Repository Impl** → `sharedUI/.../data/repository/{feature}/`
```kotlin
class MyRepositoryImpl(
    private val remoteDataSource: MyRemoteDataSourceImpl,
    private val userLocalDataSource: UserLocalDataSourceImpl,
    private val mapper: MyMapper
) : MyRepository {
    override fun getAll(): Flow<Result<List<MyEntity>>> = flow {
        emit(Result.Loading)
        try {
            val token = userLocalDataSource.getAuthToken() ?: throw AuthException.unauthenticated()
            val response = remoteDataSource.getAll(token)
            if (response.success && response.data != null) {
                emit(Result.Success(mapper.toDomainList(response.data)))
            } else {
                emit(Result.Error(ApiException(response.message ?: "Failed"), response.message))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }
}
```

### 3. Presentation Layer

**Contract** → `sharedUI/.../presentation/{feature}/MyContract.kt`
```kotlin
object MyContract {
    data class State(
        val isLoading: Boolean = false,
        val items: List<MyEntity> = emptyList(),
        val error: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadData : Intent
        data class SelectItem(val id: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val id: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
```

**ViewModel** → `sharedUI/.../presentation/{feature}/MyViewModel.kt`
```kotlin
class MyViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getListUseCase: GetMyListUseCase
) : MviViewModel<MyContract.State, MyContract.Intent, MyContract.Effect>(MyContract.State()) {
    init { sendIntent(MyContract.Intent.LoadData) }

    override suspend fun handleIntent(intent: MyContract.Intent) {
        when (intent) {
            is MyContract.Intent.LoadData -> loadData()
            is MyContract.Intent.SelectItem -> sendEffect(MyContract.Effect.NavigateToDetail(intent.id))
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }
        getListUseCase().collectLatest { result ->
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> updateState { copy(isLoading = false, items = result.data) }
                is Result.Error -> updateState { copy(isLoading = false, error = result.errorMessage) }
            }
        }
    }
}
```

**Screen** → `sharedUI/.../presentation/{feature}/MyScreen.kt`
- See `compose-screen.prompt.md` for templates

### 4. DI Wiring

See `di-wiring.prompt.md` for detailed steps.

### 5. Navigation

See `navigation.prompt.md` for route + NavEntry wiring.

## Files Touched (Summary)

| # | File | Action |
|---|------|--------|
| 1 | `domain/entity/{feature}/MyEntity.kt` | Create |
| 2 | `domain/repository/{feature}/MyRepository.kt` | Create |
| 3 | `domain/usecase/{feature}/GetMyListUseCase.kt` | Create |
| 4 | `data/model/{feature}/MyDto.kt` | Create |
| 5 | `data/mapper/{feature}/MyMapper.kt` | Create |
| 6 | `data/datasource/{feature}/MyRemoteDataSourceImpl.kt` | Create |
| 7 | `data/repository/{feature}/MyRepositoryImpl.kt` | Create |
| 8 | `presentation/{feature}/MyContract.kt` | Create |
| 9 | `presentation/{feature}/MyViewModel.kt` | Create |
| 10 | `presentation/{feature}/MyScreen.kt` | Create |
| 11 | `di/DefaultViewModelProvider.kt` | Add lazy deps + override |
| 12 | `di/ViewModelProvider.kt` | Add interface method |
| 13 | `navigation/FleetRoute.kt` | Add route |
| 14 | `navigation/FleetNavigation.kt` | Add NavEntry |

