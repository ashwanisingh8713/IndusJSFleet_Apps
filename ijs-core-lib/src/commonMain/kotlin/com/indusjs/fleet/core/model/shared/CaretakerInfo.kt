package com.indusjs.fleet.core.model.shared

/**
 * Lightweight team member representation for caretaker selection.
 * Used in feature modules that need team member lists without depending on ijs-team-lib.
 */
data class CaretakerInfo(
    val id: String,
    val name: String,
    val role: String,
    val mobile: String = "",
    val email: String = ""
)

