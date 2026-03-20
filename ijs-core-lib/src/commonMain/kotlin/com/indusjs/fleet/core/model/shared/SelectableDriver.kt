package com.indusjs.fleet.core.model.shared

/**
 * Lightweight driver representation for cross-feature selection/display.
 * Used in feature modules that need driver lists without depending on ijs-driver-lib.
 */
data class SelectableDriver(
    val id: String,
    val name: String,
    val mobile: String,
    val status: String,
    val licenseNumber: String = ""
)

