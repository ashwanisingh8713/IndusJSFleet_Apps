package com.indusjs.fleet.core.model.shared

/**
 * Lightweight customer representation for cross-feature selection/display.
 * Used in feature modules that need customer lists without depending on ijs-customer-lib.
 */
data class SelectableCustomer(
    val id: String,
    val companyName: String,
    val personName: String,
    val primaryContact: String,
    val email: String = "",
    val gstNumber: String = ""
)

