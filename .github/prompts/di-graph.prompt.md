# DI Graph Template

Create Metro dependency injection graphs for the IndusJS Fleet app.

## Feature Information
- **Feature Name**: [FEATURE_NAME]
- **Scope**: [AppScope / FeatureScope]

---

## Key Metro Annotations

| Annotation | Usage |
|------------|-------|
| `@Inject` | Mark class for constructor injection (on class, not constructor) |
| `@DependencyGraph` | Define a DI component/graph |
| `@DependencyGraph.Factory` | Factory for graphs with external dependencies |
| `@Provides` | Provide an instance (use in graph class) |
| `@Binds` | Bind interface to implementation (abstract function) |
| `@SingleIn(Scope::class)` | Scope a dependency to a lifecycle |

---

## Feature Graph Template

```kotlin
package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.{feature}.{Entity}RemoteDataSource
import com.indusjs.fleet.data.repository.{feature}.{Feature}RepositoryImpl
import com.indusjs.fleet.domain.repository.{feature}.{Feature}Repository
import com.indusjs.fleet.domain.usecase.{feature}.*
import com.indusjs.fleet.presentation.{feature}.{Feature}ViewModel
import com.indusjs.fleet.presentation.{feature}.{Feature}DetailViewModel
import com.indusjs.fleet.presentation.{feature}.Create{Feature}ViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Dependency graph for {Feature} feature.
 * Provides all dependencies for {feature} screens.
 */
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class {Feature}FeatureGraph {

    // ==================== Bindings ====================

    /**
     * Bind repository interface to implementation.
     */
    @Binds
    abstract fun bindRepository(impl: {Feature}RepositoryImpl): {Feature}Repository

    // ==================== Exposed Dependencies ====================

    /**
     * ViewModel for {feature} list screen.
     */
    abstract val {feature}ViewModel: {Feature}ViewModel

    /**
     * ViewModel for {feature} detail screen.
     */
    abstract val {feature}DetailViewModel: {Feature}DetailViewModel

    /**
     * ViewModel for create {feature} screen.
     */
    abstract val create{Feature}ViewModel: Create{Feature}ViewModel

    // ==================== Factory ====================

    /**
     * Factory for creating this graph with external dependencies.
     * 
     * @param httpClient Provided from RootGraph
     * @param dispatcherProvider Provided from RootGraph
     */
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider
        ): {Feature}FeatureGraph
    }
}
```

---

## Root Graph Template

```kotlin
package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DefaultDispatcherProvider
import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Root dependency graph for the entire application.
 * Provides app-wide singleton dependencies.
 */
@SingleIn(AppScope::class)
@DependencyGraph
abstract class RootGraph : NetworkModule {

    /**
     * Provides the DispatcherProvider for coroutines.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    /**
     * Provides a configured Json instance for serialization.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json = NetworkConfig.createJson()

    /**
     * Provides a configured HttpClient for API calls.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(json: Json): HttpClient = NetworkConfig.createHttpClient(json)

    /**
     * Provides Settings for local storage.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideSettings(): Settings = Settings()

    // Accessors
    abstract val dispatcherProvider: DispatcherProvider
    abstract override val httpClient: HttpClient
    abstract override val json: Json
    abstract val settings: Settings

    companion object
}
```

---

## Scope Classes

```kotlin
package com.indusjs.fleet.di

/**
 * App-level scope for singletons that live for the app's lifetime.
 */
abstract class AppScope private constructor()

/**
 * Feature-level scope for dependencies that live for a feature's lifetime.
 */
abstract class FeatureScope private constructor()

/**
 * Screen-level scope for dependencies that live for a screen's lifetime.
 */
abstract class ScreenScope private constructor()
```

---

## App Dependencies (Holder)

```kotlin
package com.indusjs.fleet.di

/**
 * Central access point for dependency injection graphs.
 * Initialize at app startup before any DI access.
 */
object AppDependencies {
    private var _rootGraph: RootGraph? = null
    
    val rootGraph: RootGraph
        get() = _rootGraph ?: error("AppDependencies not initialized. Call initialize() first.")
    
    /**
     * Initialize the root dependency graph.
     * Call this in Application.onCreate() or equivalent.
     */
    fun initialize(factory: () -> RootGraph) {
        if (_rootGraph == null) {
            _rootGraph = factory()
        }
    }
    
    /**
     * Check if dependencies are initialized.
     */
    val isInitialized: Boolean
        get() = _rootGraph != null
}
```

---

## ViewModel Provider

```kotlin
package com.indusjs.fleet.di

import com.indusjs.fleet.presentation.{feature}.{Feature}ViewModel
import com.indusjs.fleet.presentation.{feature}.{Feature}DetailViewModel

/**
 * Interface for providing ViewModels.
 * Implemented by platform-specific ViewModel factories.
 */
interface ViewModelProvider {
    // Auth
    fun getLoginViewModel(): LoginViewModel
    fun getSignUpViewModel(): SignUpViewModel
    
    // {Feature}
    fun get{Feature}ViewModel(): {Feature}ViewModel
    fun get{Feature}DetailViewModel({entity}Id: String): {Feature}DetailViewModel
    fun getCreate{Feature}ViewModel(): Create{Feature}ViewModel
    fun getEdit{Feature}ViewModel({entity}Id: String): Edit{Feature}ViewModel
    
    // Add more as features are added
}

/**
 * Default implementation using Metro DI graphs.
 */
class DefaultViewModelProvider(
    private val rootGraph: RootGraph
) : ViewModelProvider {
    
    // Lazy creation of feature graphs
    private val {feature}Graph by lazy {
        {Feature}FeatureGraph.Factory.create(
            httpClient = rootGraph.httpClient,
            dispatcherProvider = rootGraph.dispatcherProvider
        )
    }
    
    override fun get{Feature}ViewModel(): {Feature}ViewModel {
        return {feature}Graph.{feature}ViewModel
    }
    
    override fun get{Feature}DetailViewModel({entity}Id: String): {Feature}DetailViewModel {
        // For ViewModels needing parameters, create new graph or use assisted injection
        return {Feature}DetailFeatureGraph.Factory.create(
            httpClient = rootGraph.httpClient,
            dispatcherProvider = rootGraph.dispatcherProvider,
            {entity}Id = {entity}Id
        ).viewModel
    }
    
    override fun getCreate{Feature}ViewModel(): Create{Feature}ViewModel {
        return {feature}Graph.create{Feature}ViewModel
    }
}
```

---

## Graph with Assisted Injection (for parameters)

```kotlin
/**
 * Graph for detail screen requiring entity ID.
 */
@SingleIn(ScreenScope::class)
@DependencyGraph
abstract class {Feature}DetailFeatureGraph {

    @Binds
    abstract fun bindRepository(impl: {Feature}RepositoryImpl): {Feature}Repository

    abstract val viewModel: {Feature}DetailViewModel

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides {entity}Id: String
        ): {Feature}DetailFeatureGraph
    }
}

/**
 * ViewModel using injected ID.
 */
@Inject
class {Feature}DetailViewModel(
    private val {entity}Id: String,  // Injected from graph factory
    private val get{Entity}UseCase: Get{Entity}UseCase,
    private val delete{Entity}UseCase: Delete{Entity}UseCase
) : MviViewModel<State, Intent, Effect>(State()) {
    
    init {
        sendIntent(Intent.Load{Entity})
    }
    
    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.Load{Entity} -> load{Entity}()
            // ...
        }
    }
    
    private suspend fun load{Entity}() {
        updateState { copy(isLoading = true) }
        when (val result = get{Entity}UseCase({entity}Id)) {
            is Result.Success -> updateState { copy(isLoading = false, {entity} = result.data) }
            is Result.Error -> updateState { copy(isLoading = false, error = result.message) }
            is Result.Loading -> { }
        }
    }
}
```

---

## Module Pattern (for grouping providers)

```kotlin
/**
 * Network module providing HTTP-related dependencies.
 */
interface NetworkModule {
    val httpClient: HttpClient
    val json: Json
}

/**
 * Network configuration.
 */
object NetworkConfig {
    fun createJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
    }
    
    fun createHttpClient(json: Json): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d("HTTP") { message }
                }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = ApiConfig.TIMEOUT_MS
            connectTimeoutMillis = ApiConfig.TIMEOUT_MS
            socketTimeoutMillis = ApiConfig.TIMEOUT_MS
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}
```

---

## Validation Checklist

- [ ] `@Inject` used on classes, not constructors
- [ ] `@SingleIn(Scope::class)` for singleton dependencies
- [ ] `@Binds` for interface → implementation binding (abstract function)
- [ ] `@Provides` for instance creation (concrete function)
- [ ] Factory interface for graphs needing external dependencies
- [ ] All ViewModels exposed via abstract properties
- [ ] FeatureGraph creates from RootGraph dependencies
- [ ] ViewModelProvider updated with new ViewModels

