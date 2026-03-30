# ijs-logger-lib — IndusJS Fleet

## Purpose

Logging implementation using Kermit. Provides `KermitFleetLogger` that implements
the `FleetLogger` interface defined in `ijs-core-lib`.

## Package: `com.indusjs.logger`

## Architecture

```
ijs-core-lib:  FleetLogger (interface)    ← feature modules depend on this
ijs-logger-lib: KermitFleetLogger (impl)  ← only sharedUI/app modules reference this
```

## FleetLogger Interface (in ijs-core-lib)

```kotlin
interface FleetLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
```

## KermitFleetLogger (in ijs-logger-lib)

Wraps Kermit's `Logger` to implement `FleetLogger`. Created once in
`DefaultViewModelProvider` and passed to all repositories/data sources.

## Usage in Feature Modules

```kotlin
class VehicleRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger  // Interface from ijs-core-lib
) {
    fun getVehicles(token: String) {
        logger.d("VehicleDS", "Fetching vehicles")
    }
}
```

## Module Path

`ijs-logger-lib/src/commonMain/kotlin/com/indusjs/logger/`

## Depends On: `ijs-core-lib` (for FleetLogger interface), Kermit
## Depended On By: `sharedUI` (creates instance), `androidApp`, `webApp`

