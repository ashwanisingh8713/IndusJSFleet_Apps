package com.indusjs.fleet.core.model.shared

/**
 * Lightweight vehicle representation for cross-feature selection/display.
 * Used in feature modules that need vehicle lists without depending on ijs-vehicle-lib.
 */
data class SelectableVehicle(
    val id: String,
    val registrationNumber: String,
    val displayName: String,
    val status: String,
    val vehicleType: String = ""
)

