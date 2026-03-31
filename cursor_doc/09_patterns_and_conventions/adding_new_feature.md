# Adding a New Feature — Step-by-Step Guide

## Checklist

### 1. Domain Layer (in feature module)

- [ ] **Domain Entity** — `{feature-lib}/domain/entity/{feature}/`
  ```kotlin
  data class Vehicle(
      val id: String,
      val name: String,
      val status: String,
      // ... pure Kotlin, no framework dependencies
  )
  ```

- [ ] **Repository Interface** — `{feature-lib}/domain/repository/{feature}/`
  ```kotlin
  interface VehicleRepository {
      fun getVehicles(): Flow<Result<List<Vehicle>>>
      fun getVehicleById(id: String): Flow<Result<VehicleDetail>>
  }
  ```

- [ ] **Use Cases** — `{feature-lib}/domain/usecase/{feature}/`
  ```kotlin
  class GetVehiclesUseCase(private val repository: VehicleRepository) {
      operator fun invoke(): Flow<Result<List<Vehicle>>> = repository.getVehicles()
  }
  ```

### 2. Data Layer (in feature module)

- [ ] **DTOs** — `{feature-lib}/data/model/{feature}/`
  ```kotlin
  @Serializable
  data class VehicleDto(
      @SerialName("id") val id: Int,
      @SerialName("registration_number") val registrationNumber: String,
      // All fields with @SerialName
  )
  ```

- [ ] **Mapper** — `{feature-lib}/data/mapper/{feature}/`
  ```kotlin
  object VehicleMapper : Mapper<VehicleDto, Vehicle> {
      override fun mapToDomain(data: VehicleDto): Vehicle = ...
      override fun mapToData(entity: Vehicle): VehicleDto = ...
  }
  ```

- [ ] **Data Source** — `{feature-lib}/data/datasource/{feature}/`
  ```kotlin
  interface VehicleRemoteDataSource : RemoteDataSource {
      suspend fun getVehicles(token: String): ApiResponse<List<VehicleDto>>
  }

  @Inject
  class VehicleRemoteDataSourceImpl(private val client: HttpClient) : VehicleRemoteDataSource {
      override suspend fun getVehicles(token: String) =
          client.get("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.VEHICLES}") {
              bearerAuth(token)
          }.body<ApiResponse<List<VehicleDto>>>()
  }
  ```

- [ ] **Repository Impl** — `{feature-lib}/data/repository/{feature}/`
  ```kotlin
  @Inject
  class VehicleRepositoryImpl(
      private val remoteDataSource: VehicleRemoteDataSource,
      private val mapper: VehicleMapper,
      private val userLocalDataSource: UserLocalDataSource
  ) : VehicleRepository {
      override fun getVehicles() = flow {
          emit(Result.Loading)
          val token = userLocalDataSource.getAuthToken() ?: ...
          try {
              val response = remoteDataSource.getVehicles(token)
              emit(Result.Success(mapper.mapToDomainList(response.data!!)))
          } catch (e: Exception) {
              emit(Result.Error(e, ApiErrorHandler.getNetworkErrorMessage(e)))
          }
      }
  }
  ```

### 3. Presentation Layer (in feature module)

- [ ] **Contract** — `{feature-lib}/presentation/{feature}/`
  ```kotlin
  object VehiclesContract {
      data class State(val isLoading: Boolean = false, ...) : UiState
      sealed interface Intent : UiIntent { ... }
      sealed interface Effect : UiEffect { ... }
  }
  ```

- [ ] **ViewModel** — `{feature-lib}/presentation/{feature}/`
  ```kotlin
  @Inject
  class VehiclesViewModel(private val useCase: GetVehiclesUseCase)
      : MviViewModel<State, Intent, Effect>(State()) { ... }
  ```

- [ ] **Screen** — `{feature-lib}/presentation/{feature}/`
  ```kotlin
  @Composable
  fun VehiclesScreen(viewModel: VehiclesViewModel, ...) { ... }
  ```

- [ ] **Facade** — `{feature-lib}/presentation/{feature}/`
  ```kotlin
  object VehicleFeatureFacade {
      @Composable
      fun VehiclesListEntry(viewModel: VehiclesViewModel, ...) {
          VehiclesScreen(viewModel, ...)
      }
  }
  ```

### 4. Wire in sharedUI

- [ ] **FeatureRepositoryFactory** — Add repository lazy property
  ```kotlin
  val vehicleRepository by lazy {
      VehicleRepositoryImpl(VehicleRemoteDataSourceImpl(httpClient), VehicleMapper(), userLocalDataSource)
  }
  ```

- [ ] **ViewModelProvider** — Add factory method to interface
  ```kotlin
  fun vehiclesViewModel(): VehiclesViewModel
  ```

- [ ] **DefaultViewModelProvider** — Implement factory method
  ```kotlin
  override fun vehiclesViewModel() = VehiclesViewModel(
      GetVehiclesUseCase(featureRepositoryFactory.vehicleRepository)
  )
  ```

- [ ] **FleetRoute.kt** — Add route definition
  ```kotlin
  @Serializable data object Vehicles : FleetRoute
  @Serializable data class VehicleDetail(val vehicleId: String) : FleetRoute
  ```

- [ ] **FleetNavigation.kt** — Wire NavEntry
  ```kotlin
  is FleetRoute.Vehicles -> NavEntry(route) {
      val vm = rememberViewModel { viewModelProvider.vehiclesViewModel() }
      VehicleFeatureFacade.VehiclesListEntry(vm, ...)
  }
  ```

## Module Build Setup

```kotlin
// {feature-lib}/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
}

apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))

kotlin {
    android { namespace = "com.ijs.{feature}" }
    iosX64(); iosArm64(); iosSimulatorArm64()
    js { browser() }; wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":ijs-network-lib"))
            // Compose, Material3, lifecycle, etc.
        }
    }
}
```

Don't forget to add to `settings.gradle.kts`:
```kotlin
include(":{feature-module-name}")
```

And add as dependency in `sharedUI/build.gradle.kts`:
```kotlin
implementation(project(":{feature-module-name}"))
```
