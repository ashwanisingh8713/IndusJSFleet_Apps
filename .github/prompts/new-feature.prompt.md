# New Feature Implementation

Create a new feature for the IndusJS Fleet app following Clean Architecture and MVI pattern.

## Feature Details
- **Feature Name**: [FEATURE_NAME]
- **Description**: [BRIEF_DESCRIPTION]
- **API Endpoints**: [LIST_API_ENDPOINTS_FROM_POSTMAN]

## Implementation Steps

Follow these 13 steps in order:

### 1. Domain Entity
Create domain entity in `domain/entity/{feature}/`:
```kotlin
package com.indusjs.fleet.domain.entity.{feature}

data class {Entity}(
    val id: String,
    // Domain-specific fields (not DTOs)
)
```

### 2. Repository Interface
Create repository interface in `domain/repository/{feature}/`:
```kotlin
package com.indusjs.fleet.domain.repository.{feature}

import com.indusjs.fleet.core.result.Result

interface {Feature}Repository {
    suspend fun get{Entity}s(): Result<List<{Entity}>>
    suspend fun get{Entity}(id: String): Result<{Entity}>
    suspend fun create{Entity}(entity: {Entity}): Result<{Entity}>
    suspend fun update{Entity}(entity: {Entity}): Result<{Entity}>
    suspend fun delete{Entity}(id: String): Result<Unit>
}
```

### 3. Use Cases
Create use cases in `domain/usecase/{feature}/`:
```kotlin
package com.indusjs.fleet.domain.usecase.{feature}

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.usecase.SuspendUseCase
import dev.zacsweers.metro.Inject

@Inject
class Get{Entity}sUseCase(
    private val repository: {Feature}Repository
) : SuspendUseCase<List<{Entity}>> {
    override suspend fun invoke(): Result<List<{Entity}>> = repository.get{Entity}s()
}

@Inject
class Get{Entity}UseCase(
    private val repository: {Feature}Repository
) {
    suspend operator fun invoke(id: String): Result<{Entity}> = repository.get{Entity}(id)
}

// Add Create, Update, Delete use cases as needed
```

### 4. DTOs
Create DTOs in `data/model/{feature}/`:
```kotlin
package com.indusjs.fleet.data.model.{feature}

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class {Entity}Dto(
    @SerialName("id")
    val id: Int,
    @SerialName("field_name")  // Always use snake_case
    val fieldName: String,
    // Map ALL API fields with @SerialName
)

@Serializable
data class {Entity}ListResponseDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<{Entity}Dto>? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("total")
    val total: Int? = null
)

@Serializable
data class {Entity}ResponseDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: {Entity}Dto? = null
)

@Serializable
data class Create{Entity}Request(
    @SerialName("field_name")
    val fieldName: String,
    // Request body fields
)
```

### 5. Mapper
Create mapper in `data/mapper/{feature}/`:
```kotlin
package com.indusjs.fleet.data.mapper.{feature}

import com.indusjs.fleet.data.mapper.Mapper
import com.indusjs.fleet.data.model.{feature}.{Entity}Dto
import com.indusjs.fleet.domain.entity.{feature}.{Entity}

object {Entity}Mapper : Mapper<{Entity}Dto, {Entity}> {
    override fun mapToDomain(data: {Entity}Dto): {Entity} = {Entity}(
        id = data.id.toString(),
        fieldName = data.fieldName ?: ""
    )

    override fun mapToData(entity: {Entity}): {Entity}Dto = {Entity}Dto(
        id = entity.id.toIntOrNull() ?: 0,
        fieldName = entity.fieldName
    )
}
```

### 6. Remote Data Source
Create data source in `data/datasource/{feature}/`:
```kotlin
package com.indusjs.fleet.data.datasource.{feature}

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.{feature}.*
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

@Inject
class {Entity}RemoteDataSource(
    private val httpClient: HttpClient
) : RemoteDataSource {

    suspend fun get{Entity}s(page: Int = 1): {Entity}ListResponseDto {
        return httpClient.get("${ApiConfig.BASE_URL}/{entities}") {
            parameter("page", page)
            parameter("per_page", 10)
        }.body()
    }

    suspend fun get{Entity}(id: String): {Entity}ResponseDto {
        return httpClient.get("${ApiConfig.BASE_URL}/{entities}/$id").body()
    }

    suspend fun create{Entity}(request: Create{Entity}Request): {Entity}ResponseDto {
        return httpClient.post("${ApiConfig.BASE_URL}/{entities}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun update{Entity}(id: String, request: Create{Entity}Request): {Entity}ResponseDto {
        return httpClient.put("${ApiConfig.BASE_URL}/{entities}/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun delete{Entity}(id: String): {Entity}ResponseDto {
        return httpClient.delete("${ApiConfig.BASE_URL}/{entities}/$id").body()
    }
}
```

### 7. Repository Implementation
Create repository impl in `data/repository/{feature}/`:
```kotlin
package com.indusjs.fleet.data.repository.{feature}

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.datasource.{feature}.{Entity}RemoteDataSource
import com.indusjs.fleet.data.mapper.{feature}.{Entity}Mapper
import com.indusjs.fleet.domain.entity.{feature}.{Entity}
import com.indusjs.fleet.domain.repository.{feature}.{Feature}Repository
import dev.zacsweers.metro.Inject

@Inject
class {Feature}RepositoryImpl(
    private val remoteDataSource: {Entity}RemoteDataSource
) : {Feature}Repository {

    override suspend fun get{Entity}s(): Result<List<{Entity}>> = try {
        val response = remoteDataSource.get{Entity}s()
        if (response.success && response.data != null) {
            Result.Success(response.data.map { {Entity}Mapper.mapToDomain(it) })
        } else {
            Result.Error(Exception(response.message), response.message)
        }
    } catch (e: Exception) {
        Result.Error(e, e.message)
    }

    override suspend fun get{Entity}(id: String): Result<{Entity}> = try {
        val response = remoteDataSource.get{Entity}(id)
        if (response.success && response.data != null) {
            Result.Success({Entity}Mapper.mapToDomain(response.data))
        } else {
            Result.Error(Exception(response.message), response.message)
        }
    } catch (e: Exception) {
        Result.Error(e, e.message)
    }

    // Implement create, update, delete similarly
}
```

### 8. MVI Contract
Create contract in `presentation/{feature}/`:
```kotlin
package com.indusjs.fleet.presentation.{feature}

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.{feature}.{Entity}

object {Feature}Contract {
    data class State(
        val isLoading: Boolean = false,
        val {entities}: List<{Entity}> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val isRefreshing: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data object Load{Entity}s : Intent
        data object Refresh{Entity}s : Intent
        data class Search(val query: String) : Intent
        data class Select{Entity}(val id: String) : Intent
        data class Delete{Entity}(val id: String) : Intent
        data object Add{Entity} : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateTo{Entity}Detail(val id: String) : Effect
        data object NavigateToAdd{Entity} : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
```

### 9. ViewModel
Create ViewModel in `presentation/{feature}/`:
```kotlin
package com.indusjs.fleet.presentation.{feature}

import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.usecase.{feature}.*
import com.indusjs.fleet.presentation.{feature}.{Feature}Contract.*
import dev.zacsweers.metro.Inject

@Inject
class {Feature}ViewModel(
    private val get{Entity}sUseCase: Get{Entity}sUseCase,
    private val delete{Entity}UseCase: Delete{Entity}UseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.Load{Entity}s)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Load{Entity}s -> load{Entity}s()
            is Intent.Refresh{Entity}s -> refresh{Entity}s()
            is Intent.Search -> search(intent.query)
            is Intent.Select{Entity} -> sendEffect(Effect.NavigateTo{Entity}Detail(intent.id))
            is Intent.Delete{Entity} -> delete{Entity}(intent.id)
            is Intent.Add{Entity} -> sendEffect(Effect.NavigateToAdd{Entity})
        }
    }

    private suspend fun load{Entity}s() {
        updateState { copy(isLoading = true, error = null) }
        when (val result = get{Entity}sUseCase()) {
            is Result.Success -> updateState { copy(isLoading = false, {entities} = result.data) }
            is Result.Error -> updateState { copy(isLoading = false, error = result.message) }
            is Result.Loading -> { }
        }
    }

    private suspend fun refresh{Entity}s() {
        updateState { copy(isRefreshing = true) }
        when (val result = get{Entity}sUseCase()) {
            is Result.Success -> updateState { copy(isRefreshing = false, {entities} = result.data) }
            is Result.Error -> {
                updateState { copy(isRefreshing = false) }
                sendEffect(Effect.ShowSnackbar(result.message ?: "Refresh failed"))
            }
            is Result.Loading -> { }
        }
    }

    private fun search(query: String) {
        updateState { copy(searchQuery = query) }
        // Filter entities based on query
    }

    private suspend fun delete{Entity}(id: String) {
        when (val result = delete{Entity}UseCase(id)) {
            is Result.Success -> {
                sendEffect(Effect.ShowSnackbar("{Entity} deleted"))
                load{Entity}s()
            }
            is Result.Error -> sendEffect(Effect.ShowSnackbar(result.message ?: "Delete failed"))
            is Result.Loading -> { }
        }
    }
}
```

### 10. Compose Screen
Create screen in `presentation/{feature}/`:
```kotlin
package com.indusjs.fleet.presentation.{feature}

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.mvi.HandleEffects
import com.indusjs.fleet.core.ui.*
import com.indusjs.fleet.presentation.{feature}.{Feature}Contract.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun {Feature}Screen(
    viewModel: {Feature}ViewModel,
    onNavigateBack: () -> Unit,
    onNavigateTo{Entity}Detail: (String) -> Unit,
    onNavigateToAdd{Entity}: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateTo{Entity}Detail -> onNavigateTo{Entity}Detail(effect.id)
            is Effect.NavigateToAdd{Entity} -> onNavigateToAdd{Entity}()
            is Effect.ShowSnackbar -> { /* Show snackbar */ }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("{Feature}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Add{Entity}) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = "Add"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.sendIntent(Intent.Add{Entity}) }) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = "Add"
                )
            }
        }
    ) { paddingValues ->
        ScreenContent(
            modifier = Modifier.padding(paddingValues),
            isLoading = state.isLoading,
            error = state.error,
            onRetry = { viewModel.sendIntent(Intent.Load{Entity}s) },
            isEmpty = state.{entities}.isEmpty(),
            emptyIcon = Res.drawable.ic_{feature},
            emptyTitle = "No {entities} yet",
            emptySubtitle = "Add your first {entity}",
            emptyActionLabel = "Add {Entity}",
            onEmptyAction = { viewModel.sendIntent(Intent.Add{Entity}) }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.{entities}, key = { it.id }) { {entity} ->
                    {Entity}Card(
                        {entity} = {entity},
                        onClick = { viewModel.sendIntent(Intent.Select{Entity}({entity}.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun {Entity}Card(
    {entity}: {Entity},
    onClick: () -> Unit
) {
    FleetCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Card content
    }
}
```

### 11. Feature DI Graph
Create/update graph in `di/`:
```kotlin
package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.repository.{feature}.{Feature}RepositoryImpl
import com.indusjs.fleet.domain.repository.{feature}.{Feature}Repository
import com.indusjs.fleet.presentation.{feature}.{Feature}ViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class {Feature}FeatureGraph {

    @Binds
    abstract fun bindRepository(impl: {Feature}RepositoryImpl): {Feature}Repository

    abstract val viewModel: {Feature}ViewModel

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider
        ): {Feature}FeatureGraph
    }
}
```

### 12. Navigation Route
Add route in `navigation/FleetRoute.kt`:
```kotlin
// In FleetRoute sealed interface
@Serializable data object {Feature}s : FleetRoute
@Serializable data class {Feature}Detail(val {entity}Id: String) : FleetRoute
@Serializable data object Create{Feature} : FleetRoute
```

### 13. Wire Navigation
Add to `FleetNavigation.kt`:
```kotlin
composable<FleetRoute.{Feature}s> {
    val viewModel = viewModelProvider.get{Feature}ViewModel()
    {Feature}Screen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateTo{Entity}Detail = { id -> navController.navigate(FleetRoute.{Feature}Detail(id)) },
        onNavigateToAdd{Entity} = { navController.navigate(FleetRoute.Create{Feature}) }
    )
}
```

---

## Validation Checklist

- [ ] All DTOs have `@SerialName` with snake_case
- [ ] Dates converted to ISO 8601 before API calls
- [ ] Error handling with `Result<T>` in all layers
- [ ] Loading state handled in ViewModel
- [ ] Empty state UI implemented
- [ ] Back navigation working
- [ ] DI graph created and wired
- [ ] Route added to FleetRoute
- [ ] Navigation wired in FleetNavigation

