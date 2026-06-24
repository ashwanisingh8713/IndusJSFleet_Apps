package com.ijs.trip.domain.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Config-driven cargo material → unit mapping for the Create-Trip screen.
 *
 * Loaded from a bundled KMP asset (`composeResources/files/cargo_materials.json`) so the set
 * of materials and the units valid for each can be tweaked without a code change. A graceful
 * fallback ([com.ijs.trip.data.CargoConfigProvider]) keeps the form working if the asset is
 * missing or corrupt.
 */

// ── Wire DTOs (mirror the JSON asset). DTO discipline: every field has a @SerialName + default. ──

/**
 * Root DTO for `cargo_materials.json`.
 */
@Serializable
data class CargoMaterialConfigDto(
    @SerialName("materials")
    val materials: List<CargoMaterialDto> = emptyList(),
    @SerialName("allUnits")
    val allUnits: List<String> = emptyList(),
    @SerialName("unitLabels")
    val unitLabels: List<UnitLabelDto> = emptyList()
)

/**
 * One unit-label entry in the asset's top-level `unitLabels` array.
 *
 * `value` is the STABLE backend value (matches the strings in [CargoMaterialDto.units] and
 * `allUnits`); `label`/`label_hi` are display-only and never sent to the backend.
 */
@Serializable
data class UnitLabelDto(
    @SerialName("value")
    val value: String = "",
    @SerialName("label")
    val label: String = "",
    @SerialName("label_hi")
    val labelHi: String = ""
)

/**
 * One material entry in the asset.
 */
@Serializable
data class CargoMaterialDto(
    @SerialName("id")
    val id: String = "",
    @SerialName("label")
    val label: String = "",
    @SerialName("label_hi")
    val labelHi: String = "",
    @SerialName("units")
    val units: List<String> = emptyList(),
    @SerialName("defaultUnit")
    val defaultUnit: String = ""
)

// ── Domain types ──

/**
 * A cargo material and the weight units that make sense for it.
 *
 * @property id matches the existing cargo-type values sent to the backend (lowercased).
 * @property label human-readable name shown in the cargo-type dropdown.
 * @property units the units valid for this material (a subset of the global unit set).
 * @property defaultUnit the unit auto-selected when this material is chosen.
 */
data class CargoMaterial(
    val id: String,
    val label: String,
    // Hindi label; blank falls back to [label]. Resolved by locale at render time.
    val labelHi: String = "",
    val units: List<String>,
    val defaultUnit: String
)

/**
 * Display labels for a single unit value. [value] is the backend value; [label]/[labelHi] are
 * shown to the user (Hindi falls back to [label] when blank).
 */
data class UnitLabel(
    val value: String,
    val label: String,
    val labelHi: String = ""
)

/**
 * The whole materials config: the list of materials plus the global set of all units.
 */
data class CargoMaterialConfig(
    val materials: List<CargoMaterial>,
    val allUnits: List<String>,
    val unitLabels: List<UnitLabel> = emptyList()
)

// ── Mappers ──

private fun CargoMaterialDto.toDomain(): CargoMaterial = CargoMaterial(
    id = id,
    label = label,
    labelHi = labelHi,
    units = units,
    defaultUnit = defaultUnit
)

private fun UnitLabelDto.toDomain(): UnitLabel = UnitLabel(
    value = value,
    label = label,
    labelHi = labelHi
)

fun CargoMaterialConfigDto.toDomain(): CargoMaterialConfig = CargoMaterialConfig(
    // Drop any malformed entries (blank id or no units) so the UI never shows a broken option.
    materials = materials
        .filter { it.id.isNotBlank() && it.units.isNotEmpty() }
        .map { it.toDomain() },
    allUnits = allUnits,
    // Drop entries with a blank value (nothing to map a unit to).
    unitLabels = unitLabels
        .filter { it.value.isNotBlank() }
        .map { it.toDomain() }
)
