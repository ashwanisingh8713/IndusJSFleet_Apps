package com.ijs.vehicle.data

import com.indusjs.dispatcher.DispatcherProvider
import com.ijs.vehicle.domain.entity.FuelTypeLabel
import com.ijs.vehicle.domain.entity.VehicleTypeConfig
import com.ijs.vehicle.domain.entity.VehicleTypeConfigDto
import com.ijs.vehicle.domain.entity.VehicleTypeOption
import com.ijs.vehicle.domain.entity.toDomain
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Loads the Vehicle Type → Fuel Type config from the bundled KMP asset
 * (`composeResources/files/vehicle_types.json`, owned by `ijs-ui-components-lib`).
 *
 * Intentionally a plain `object` the ViewModel calls with its existing [DispatcherProvider] —
 * not DI-injected (sharedUI DI wiring is out of scope). The load is non-fatal: any read/parse
 * failure (or an empty/missing asset) returns [fallback], so the Register-Vehicle form NEVER breaks.
 *
 * Mirrors `com.ijs.trip.data.CargoConfigProvider`.
 */
object VehicleTypeConfigProvider {

    private const val ASSET_PATH = "files/vehicle_types.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Reads + parses the asset on the IO dispatcher. Falls back to hardcoded defaults on any error.
     */
    suspend fun load(dispatcher: DispatcherProvider): VehicleTypeConfig =
        withContext(dispatcher.io) {
            try {
                val bytes = Res.readBytes(ASSET_PATH)
                val dto = json.decodeFromString<VehicleTypeConfigDto>(bytes.decodeToString())
                dto.toDomain().takeIf { it.vehicleTypes.isNotEmpty() } ?: fallback()
            } catch (e: Exception) {
                fallback()
            }
        }

    /**
     * Hardcoded defaults mirroring the asset: the 6 vehicle types, each mapped to its sensible fuel
     * set (Truck/Bus/Trailer → Diesel only; Car → all five; Motorcycle → no diesel). Used when the
     * asset is absent or corrupt. `id`s match the [com.ijs.vehicle.domain.entity.VehicleType] enum
     * name lowercased.
     */
    fun fallback(): VehicleTypeConfig {
        val diesel = listOf("Diesel")
        val allFuels = listOf("Diesel", "Petrol", "CNG", "Electric", "Hybrid")
        return VehicleTypeConfig(
            vehicleTypes = listOf(
                VehicleTypeOption("truck", "Truck", "", diesel, "Diesel"),
                VehicleTypeOption("van", "Van", "", listOf("Diesel", "Petrol", "CNG", "Electric"), "Diesel"),
                VehicleTypeOption("car", "Car", "", allFuels, "Petrol"),
                VehicleTypeOption("bus", "Bus", "", diesel, "Diesel"),
                VehicleTypeOption("motorcycle", "Motorcycle", "", listOf("Petrol", "Electric"), "Petrol"),
                VehicleTypeOption("trailer", "Trailer", "", diesel, "Diesel")
            ),
            allFuelTypes = allFuels.map { FuelTypeLabel(value = it, label = it) }
        )
    }
}
