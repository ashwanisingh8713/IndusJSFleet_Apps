package com.ijs.vehicle.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Config-driven Vehicle Type → Fuel Type mapping for the Register-Vehicle screen.
 *
 * Loaded from a bundled KMP asset (`composeResources/files/vehicle_types.json`, owned by
 * `ijs-ui-components-lib`) so the set of vehicle types and the fuel types valid for each can be
 * tweaked without a code change — mirrors the cargo-material config used by Create-Trip. A graceful
 * fallback ([com.ijs.vehicle.data.VehicleTypeConfigProvider]) keeps the form working if the asset
 * is missing or corrupt.
 *
 * ### Wire format
 * The vehicle `id` matches the [VehicleType] enum name lowercased (e.g. `"truck"`), which is exactly
 * what the backend expects (`VehicleMapper.vehicleTypeToApiString`). Fuel `value`s are display
 * strings ("Diesel"); the mapper lowercases them on send. This config only decides which options are
 * *shown* — it never changes the value that flows to the backend.
 */

// ── Wire DTOs (mirror the JSON asset). DTO discipline: every field has a @SerialName + default. ──

/**
 * Root DTO for `vehicle_types.json`.
 */
@Serializable
data class VehicleTypeConfigDto(
    @SerialName("vehicleTypes")
    val vehicleTypes: List<VehicleTypeOptionDto> = emptyList(),
    @SerialName("allFuelTypes")
    val allFuelTypes: List<FuelTypeLabelDto> = emptyList()
)

/**
 * One vehicle-type entry in the asset: the type plus the fuel types that make sense for it.
 */
@Serializable
data class VehicleTypeOptionDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("label")
    val label: String = "",
    @SerialName("label_hi")
    val labelHi: String = "",
    @SerialName("fuelTypes")
    val fuelTypes: List<String> = emptyList(),
    @SerialName("defaultFuel")
    val defaultFuel: String = ""
)

/**
 * One fuel-label entry in the asset's top-level `allFuelTypes` array.
 *
 * `value` is the STABLE value (matches the strings in [VehicleTypeOptionDto.fuelTypes]);
 * `label`/`label_hi` are display-only and never sent to the backend.
 */
@Serializable
data class FuelTypeLabelDto(
    @SerialName("value")
    val value: String = "",
    @SerialName("label")
    val label: String = "",
    @SerialName("label_hi")
    val labelHi: String = ""
)

// ── Domain types ──

/**
 * A vehicle type and the fuel types valid for it.
 *
 * @property id matches the [VehicleType] enum name lowercased (the backend wire value).
 * @property label human-readable name shown in the Vehicle Type dropdown.
 * @property labelHi Hindi label; blank falls back to [label]. Resolved by locale at render time.
 * @property fuelTypes the fuel options shown when this type is selected.
 * @property defaultFuel the fuel auto-selected when this type is chosen.
 */
data class VehicleTypeOption(
    val id: String,
    val label: String,
    val labelHi: String = "",
    val fuelTypes: List<String>,
    val defaultFuel: String
)

/**
 * Display labels for a single fuel value. [value] is the stable value; [label]/[labelHi] are shown
 * to the user (Hindi falls back to [label] when blank).
 */
data class FuelTypeLabel(
    val value: String,
    val label: String,
    val labelHi: String = ""
)

/**
 * The whole vehicle-type config: the list of types plus the global set of fuel labels.
 */
data class VehicleTypeConfig(
    val vehicleTypes: List<VehicleTypeOption>,
    val allFuelTypes: List<FuelTypeLabel> = emptyList()
)

// ── Mappers ──

private fun VehicleTypeOptionDto.toDomain(): VehicleTypeOption = VehicleTypeOption(
    id = id,
    label = label,
    labelHi = labelHi,
    fuelTypes = fuelTypes,
    defaultFuel = defaultFuel.ifBlank { fuelTypes.firstOrNull().orEmpty() }
)

private fun FuelTypeLabelDto.toDomain(): FuelTypeLabel = FuelTypeLabel(
    value = value,
    label = label.ifBlank { value },
    labelHi = labelHi
)

fun VehicleTypeConfigDto.toDomain(): VehicleTypeConfig = VehicleTypeConfig(
    // Drop any malformed entries (blank id or no fuel types) so the UI never shows a broken option.
    vehicleTypes = vehicleTypes
        .filter { it.id.isNotBlank() && it.fuelTypes.isNotEmpty() }
        .map { it.toDomain() },
    // Drop entries with a blank value (nothing to map a fuel to).
    allFuelTypes = allFuelTypes
        .filter { it.value.isNotBlank() }
        .map { it.toDomain() }
)
