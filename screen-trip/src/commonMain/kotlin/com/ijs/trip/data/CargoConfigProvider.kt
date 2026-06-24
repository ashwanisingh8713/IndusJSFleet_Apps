package com.ijs.trip.data

import com.indusjs.dispatcher.DispatcherProvider
import com.ijs.trip.domain.entity.CargoMaterial
import com.ijs.trip.domain.entity.CargoMaterialConfig
import com.ijs.trip.domain.entity.CargoMaterialConfigDto
import com.ijs.trip.domain.entity.UnitLabel
import com.ijs.trip.domain.entity.toDomain
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Loads the cargo material → unit config from the bundled KMP asset
 * (`composeResources/files/cargo_materials.json`, owned by `ijs-ui-components-lib`).
 *
 * Intentionally a plain `object` the ViewModel calls with its existing [DispatcherProvider] —
 * not DI-injected (sharedUI DI wiring is out of scope). The load is non-fatal: any read/parse
 * failure (or an empty/missing asset) returns [fallback], so the Create-Trip form NEVER breaks.
 */
object CargoConfigProvider {

    private const val ASSET_PATH = "files/cargo_materials.json"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Reads + parses the asset on the IO dispatcher. Falls back to hardcoded defaults on any error.
     */
    suspend fun load(dispatcher: DispatcherProvider): CargoMaterialConfig =
        withContext(dispatcher.io) {
            try {
                val bytes = Res.readBytes(ASSET_PATH)
                val dto = json.decodeFromString<CargoMaterialConfigDto>(bytes.decodeToString())
                dto.toDomain().takeIf { it.materials.isNotEmpty() } ?: fallback()
            } catch (e: Exception) {
                fallback()
            }
        }

    /**
     * Hardcoded defaults mirroring the previous in-code behaviour: the 7 cargo types, each mapped
     * to all 6 units with a "KG" default. Used when the asset is absent or corrupt.
     */
    fun fallback(): CargoMaterialConfig {
        val allUnits = listOf("KG", "M.Ton", "Quintal", "Liter", "CFT", "Bags")
        val ids = listOf("gitti", "balu", "bhakshi", "enta", "hazardous", "valuable", "others")
        // Display labels for each unit value. Without a translated asset we mirror the value into
        // both English and Hindi labels (label = labelHi = value) so the value still renders.
        val unitLabelValues = listOf("KG", "M.Ton", "Quintal", "Liter", "CFT", "Bags", "Unit")
        return CargoMaterialConfig(
            materials = ids.map { id ->
                CargoMaterial(
                    id = id,
                    label = id.replaceFirstChar { it.uppercaseChar() },
                    units = allUnits,
                    defaultUnit = "KG"
                )
            },
            allUnits = allUnits,
            unitLabels = unitLabelValues.map { value ->
                UnitLabel(value = value, label = value, labelHi = value)
            }
        )
    }
}
