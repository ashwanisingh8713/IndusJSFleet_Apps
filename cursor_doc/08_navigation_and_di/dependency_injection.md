# Dependency Injection

## DI Strategy

The project uses a **hybrid approach**:
- **Metro annotations** (`@Inject`, `@Provides`, `@Binds`) on data layer classes
- **Manual wiring** in `DefaultViewModelProvider` and `FeatureRepositoryFactory` (not Metro-generated graphs)
- **CompositionLocal** to provide `ViewModelProvider` to Compose tree

## Key Files

| File | Location | Role |
|------|----------|------|
| `ViewModelProvider.kt` | `sharedUI/.../di/` | Interface + CompositionLocal |
| `DefaultViewModelProvider.kt` | `sharedUI/.../di/` | Singleton factory (production) |
| `FeatureRepositoryFactory.kt` | `sharedUI/.../di/` | Wires feature-lib repositories |
| `NetworkDataGraph.kt` | `ijs-network-lib/.../di/` | Network layer DI graph |

## ViewModelProvider Interface

```kotlin
interface ViewModelProvider {
    // Auth
    fun loginViewModel(): LoginViewModel
    fun signUpViewModel(): SignUpViewModel
    fun profileViewModel(): ProfileViewModel
    fun changePasswordViewModel(): ChangePasswordViewModel
    fun forgotPasswordViewModel(): ForgotPasswordViewModel

    // Dashboard
    fun dashboardViewModel(): DashboardViewModel
    fun alertsListViewModel(): AlertsListViewModel
    fun mapsViewModel(): MapsViewModel

    // Vehicles
    fun vehiclesViewModel(): VehiclesViewModel
    fun vehicleDetailViewModel(vehicleId: String): VehicleDetailViewModel
    fun addVehicleViewModel(): AddVehicleViewModel
    fun maintenanceCostEntryViewModel(vehicleId: String?): MaintenanceCostEntryViewModel

    // Drivers
    fun driversViewModel(): DriversViewModel
    fun driverDetailViewModel(driverId: String): DriverDetailViewModel
    fun createDriverViewModel(): CreateDriverViewModel
    fun driverCostEntryViewModel(driverId: String?): DriverCostEntryViewModel

    // Trips
    fun tripsViewModel(): TripsViewModel
    fun tripDetailViewModel(tripId: String): TripDetailViewModel
    fun createTripViewModel(): CreateTripViewModel
    fun tripCostEntryViewModel(tripId: String?, vehicleId: String?): TripCostEntryViewModel

    // Customers, Payments, Team, Reports, Finance
    // ... (every ViewModel has a factory method)

    // Onboarding
    fun onboardingViewModel(): OnboardingViewModel
}
```

## Compose Integration

```kotlin
// CompositionLocal
val LocalViewModelProvider = staticCompositionLocalOf<ViewModelProvider> { error("...") }

// Provider wrapper
@Composable
fun ProvideViewModelProvider(provider: ViewModelProvider, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalViewModelProvider provides provider, content = content)
}

// ViewModel creation
@Composable
inline fun <reified T : ViewModel> rememberViewModel(crossinline factory: () -> T): T {
    return viewModel { factory() }
}

// Shared ViewModel (across related screens)
@Composable
inline fun <reified T : ViewModel> rememberSharedViewModel(key: String, crossinline factory: () -> T): T
fun clearSharedViewModel(key: String)
```

## DefaultViewModelProvider (Singleton)

```kotlin
class DefaultViewModelProvider private constructor() : ViewModelProvider {
    companion object {
        fun getInstance(): DefaultViewModelProvider  // Lazy singleton
    }

    // Lazy infrastructure
    private val settings: Settings by lazy { Settings() }
    private val json: Json by lazy { HttpClientProvider.createJson() }
    private val httpClient: HttpClient by lazy { HttpClientProvider.createHttpClient(json) }
    private val fleetLogger: FleetLogger by lazy { IjsLogger.createFleetLogger() }

    // Room database
    private val database: FleetDatabase by lazy { FleetDatabase.create() }

    // Local data sources (Room DAOs)
    private val dashboardLocalDataSource by lazy { ... }
    private val costsLocalDataSource by lazy { ... }
    private val customerLocalDataSource by lazy { ... }
    private val teamLocalDataSource by lazy { ... }

    // Network graph (user, dashboard, costs repos)
    private val networkDataGraph by lazy {
        NetworkDataGraph.create(httpClient, json, settings, DispatcherProvider(), ...)
    }

    // Feature repositories
    private val featureRepositoryFactory by lazy {
        FeatureRepositoryFactory(httpClient, json, settings, DispatcherProvider(), ...)
    }

    // Init: register session clear callback
    init {
        AuthenticationManager.registerSessionClearCallback {
            networkDataGraph.userLocalDataSource.clearSession()
        }
    }

    // ViewModel factory methods create VMs with injected dependencies
    override fun vehiclesViewModel() = VehiclesViewModel(
        getVehiclesUseCase = GetVehiclesUseCase(featureRepositoryFactory.vehicleRepository)
    )
    // ... etc.
}
```

## FeatureRepositoryFactory

Wires each feature module's data layer classes:

```kotlin
class FeatureRepositoryFactory(
    private val httpClient: HttpClient,
    private val json: Json,
    private val settings: Settings,
    private val dispatcherProvider: DispatcherProvider,
    private val fleetLogger: FleetLogger,
    private val costsLocalDataSource: CostsLocalDataSource,
    private val customerLocalDataSource: CustomerLocalDataSource,
    private val teamLocalDataSource: TeamLocalDataSource
) {
    // Shared across all feature repos
    private val userLocalDataSource = UserLocalDataSourceImpl(settings)

    val vehicleRepository by lazy {
        VehicleRepositoryImpl(
            remoteDataSource = VehicleRemoteDataSourceImpl(httpClient),
            mapper = VehicleMapper(),
            userLocalDataSource = userLocalDataSource
        )
    }

    val driverRepository by lazy { ... }
    val tripRepository by lazy { ... }
    val customerRepository by lazy { ... }
    val paymentRepository by lazy { ... }
    val teamRepository by lazy { ... }
    val reportsRepository by lazy { ... }
    val vehicleFinanceRepository by lazy { ... }
}
```

## NetworkDataGraph

Manual DI for network-level components:

```kotlin
class NetworkDataGraph private constructor(...) {
    val userRepository: UserRepository
    val dashboardRepository: DashboardRepository
    val costsRepository: CostsRepository
    val costTypesRepository: CostTypesRepository
    val userLocalDataSource: UserLocalDataSource

    companion object {
        fun create(httpClient, json, settings, dispatcher, dashboardLocal, costsLocal, logger)
    }
}
```

## DI Flow

```
App.kt
  → DefaultViewModelProvider.getInstance()
    → Creates lazy infrastructure (HttpClient, Settings, Room, etc.)
    → Creates NetworkDataGraph (user, dashboard, costs)
    → Creates FeatureRepositoryFactory (vehicle, driver, trip, etc.)
    → Registers AuthenticationManager callback
  → ProvideViewModelProvider(provider)
    → FleetNavigation uses rememberViewModel { provider.xyzViewModel() }
      → ViewModel created with injected use cases / repositories
```

## Metro Annotations (on Data Classes)

While DI wiring is manual, data layer classes use Metro annotations for consistency:

```kotlin
@Inject
class VehicleRemoteDataSourceImpl(private val client: HttpClient) : VehicleRemoteDataSource

@Inject
class VehicleRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val mapper: VehicleMapper,
    private val userLocalDataSource: UserLocalDataSource
) : VehicleRepository
```

These `@Inject` annotations serve as documentation and future-proofing for when Metro graph generation may replace manual wiring.
