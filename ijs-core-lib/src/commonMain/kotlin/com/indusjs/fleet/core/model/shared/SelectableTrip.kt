package com.indusjs.fleet.core.model.shared

/**
 * Lightweight trip representation for cross-feature selection/display.
 * Used in feature modules that need trip lists without depending on ijs-trip-lib.
 */
data class SelectableTrip(
    val id: String,
    val routeLabel: String,
    val vehicleInfo: String,
    val driverName: String = "",
    val status: String,
    val scheduledDate: String = "",
    val tripPrice: Double = 0.0
)

