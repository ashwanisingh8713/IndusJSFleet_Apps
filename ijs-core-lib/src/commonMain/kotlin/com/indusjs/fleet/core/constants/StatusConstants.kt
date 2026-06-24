package com.indusjs.fleet.core.constants

/**
 * Single source of truth for all entity states in the Fleet Management system.
 * These constants are used throughout the app to ensure consistency between
 * UI, domain entities, database storage, and API communication.
 *
 * Based on entity_state_management.md v2.1.0
 */
object StatusConstants {

    // ==================== Vehicle States ====================
    /**
     * Vehicle status constants matching API values.
     *
     * States:
     * - inactive: Vehicle is not in use, disabled
     * - active: Vehicle is available for assignment
     * - on_route: Vehicle is currently on a trip
     * - maintenance: Vehicle is under maintenance
     * - damaged: Vehicle has damage, needs repair
     * - decommissioned: Vehicle permanently out of service (optional)
     */
    object VehicleState {
        const val INACTIVE = "inactive"
        const val ACTIVE = "active"
        const val ON_ROUTE = "on_route"
        const val MAINTENANCE = "maintenance"
        const val DAMAGED = "damaged"
        const val DECOMMISSIONED = "decommissioned"

        /**
         * All valid vehicle states.
         */
        val ALL = listOf(INACTIVE, ACTIVE, ON_ROUTE, MAINTENANCE, DAMAGED, DECOMMISSIONED)

        /**
         * Core vehicle states (excluding optional).
         */
        val CORE = listOf(INACTIVE, ACTIVE, ON_ROUTE, MAINTENANCE, DAMAGED)

        /**
         * States where vehicle is available for assignment.
         */
        val AVAILABLE_FOR_ASSIGNMENT = listOf(ACTIVE)

        /**
         * States where vehicle is unavailable.
         */
        val UNAVAILABLE = listOf(INACTIVE, ON_ROUTE, MAINTENANCE, DAMAGED, DECOMMISSIONED)

        /**
         * Display labels for each state.
         */
        fun getDisplayLabel(state: String): String = when (state.lowercase()) {
            INACTIVE -> "Inactive"
            ACTIVE -> "Active"
            ON_ROUTE -> "On Route"
            MAINTENANCE -> "Maintenance"
            DAMAGED -> "Damaged"
            DECOMMISSIONED -> "Decommissioned"
            else -> state.replaceFirstChar { it.uppercase() }
        }

        /**
         * Emoji icons for each state.
         */
        fun getIcon(state: String): String = when (state.lowercase()) {
            INACTIVE -> "⚫"
            ACTIVE -> "🟢"
            ON_ROUTE -> "🚗"
            MAINTENANCE -> "🔧"
            DAMAGED -> "⚠️"
            DECOMMISSIONED -> "🚫"
            else -> "❓"
        }

        /**
         * Color scheme for each state (returns color name).
         */
        fun getColorScheme(state: String): StateColorScheme = when (state.lowercase()) {
            INACTIVE -> StateColorScheme.NEUTRAL
            ACTIVE -> StateColorScheme.SUCCESS
            ON_ROUTE -> StateColorScheme.INFO
            MAINTENANCE -> StateColorScheme.WARNING
            DAMAGED -> StateColorScheme.ERROR
            DECOMMISSIONED -> StateColorScheme.NEUTRAL
            else -> StateColorScheme.NEUTRAL
        }

        /**
         * Check if state is valid.
         */
        fun isValid(state: String): Boolean = state.lowercase() in ALL

        /**
         * Check if vehicle is available for trip assignment.
         */
        fun isAvailableForAssignment(state: String): Boolean = state.lowercase() in AVAILABLE_FOR_ASSIGNMENT
    }

    // ==================== Driver States ====================
    /**
     * Driver status constants matching API values.
     *
     * Backend contract: PATCH /drivers/:id/status accepts oneof =
     * active | inactive | on_trip | on_leave | suspended
     * (see IndusJSFleet_GoLang_Backend internal/application/driver/dto.go).
     *
     * States:
     * - inactive: Driver is disabled/not working
     * - active: Driver is available for assignment
     * - on_trip: Driver is currently on a trip
     * - on_leave: Driver is on approved leave
     * - suspended: Driver privileges suspended
     *
     * Note: legacy "on_route" / "terminated" are NOT accepted by the backend
     * (returns 400). The "on_route" wire value is still tolerated on READ via
     * the legacy branches below so old data renders, but it is never sent.
     */
    object DriverState {
        const val INACTIVE = "inactive"
        const val ACTIVE = "active"
        const val ON_TRIP = "on_trip"
        const val ON_LEAVE = "on_leave"
        const val SUSPENDED = "suspended"

        /**
         * All valid driver states (matches backend status oneof).
         */
        val ALL = listOf(INACTIVE, ACTIVE, ON_TRIP, ON_LEAVE, SUSPENDED)

        /**
         * Core driver states.
         */
        val CORE = listOf(INACTIVE, ACTIVE, ON_TRIP, ON_LEAVE, SUSPENDED)

        /**
         * States where driver is available for assignment.
         */
        val AVAILABLE_FOR_ASSIGNMENT = listOf(ACTIVE)

        /**
         * States where driver is unavailable.
         */
        val UNAVAILABLE = listOf(INACTIVE, ON_TRIP, ON_LEAVE, SUSPENDED)

        /**
         * Display labels for each state.
         */
        fun getDisplayLabel(state: String): String = when (state.lowercase()) {
            INACTIVE -> "Inactive"
            ACTIVE -> "Active"
            ON_TRIP, "on_route" -> "On Trip" // legacy "on_route" tolerated on read
            ON_LEAVE -> "On Leave"
            SUSPENDED -> "Suspended"
            else -> state.replaceFirstChar { it.uppercase() }
        }

        /**
         * Emoji icons for each state.
         */
        fun getIcon(state: String): String = when (state.lowercase()) {
            INACTIVE -> "⚫"
            ACTIVE -> "🟢"
            ON_TRIP, "on_route" -> "🚗" // legacy "on_route" tolerated on read
            ON_LEAVE -> "🏖️"
            SUSPENDED -> "⏸️"
            else -> "❓"
        }

        /**
         * Color scheme for each state.
         */
        fun getColorScheme(state: String): StateColorScheme = when (state.lowercase()) {
            INACTIVE -> StateColorScheme.NEUTRAL
            ACTIVE -> StateColorScheme.SUCCESS
            ON_TRIP, "on_route" -> StateColorScheme.INFO // legacy "on_route" tolerated on read
            ON_LEAVE -> StateColorScheme.WARNING
            SUSPENDED -> StateColorScheme.ERROR
            else -> StateColorScheme.NEUTRAL
        }

        /**
         * Check if state is valid.
         * Accepts legacy "on_route" for backward compatibility on read.
         */
        fun isValid(state: String): Boolean =
            state.lowercase() in ALL || state.lowercase() == "on_route"

        /**
         * Check if driver is available for trip assignment.
         */
        fun isAvailableForAssignment(state: String): Boolean = state.lowercase() in AVAILABLE_FOR_ASSIGNMENT
    }

    // ==================== Trip States ====================
    /**
     * Trip status constants matching API values.
     *
     * States:
     * - planned: Trip created but not started
     * - on_route: Trip is in progress
     * - completed: Trip finished successfully
     * - cancelled: Trip cancelled before completion
     * - failed: Trip could not be completed (optional)
     * - delayed: Trip is behind schedule (optional)
     *
     * Note: 'assigned' state has been removed. Trips now transition directly from planned → on_route.
     */
    object TripState {
        const val PLANNED = "planned"
        const val ON_ROUTE = "on_route"
        const val COMPLETED = "completed"
        const val CANCELLED = "cancelled"
        const val FAILED = "failed"
        const val DELAYED = "delayed"

        // Legacy constant for backward compatibility (maps to PLANNED)
        @Deprecated("Use PLANNED instead. Assigned state has been removed from backend.", ReplaceWith("PLANNED"))
        const val ASSIGNED = "assigned"

        /**
         * All valid trip states.
         */
        val ALL = listOf(PLANNED, ON_ROUTE, COMPLETED, CANCELLED, FAILED, DELAYED)

        /**
         * Core trip states (excluding optional).
         */
        val CORE = listOf(PLANNED, ON_ROUTE, COMPLETED, CANCELLED)

        /**
         * States where trip is active/ongoing.
         */
        val ACTIVE = listOf(PLANNED, ON_ROUTE, DELAYED)

        /**
         * States where trip is finished (success or failure).
         */
        val FINISHED = listOf(COMPLETED, CANCELLED, FAILED)

        /**
         * States where trip can be modified.
         */
        val EDITABLE = listOf(PLANNED)

        /**
         * States where trip can be cancelled.
         */
        val CANCELLABLE = listOf(PLANNED, DELAYED, ON_ROUTE)

        /**
         * Display labels for each state.
         */
        fun getDisplayLabel(state: String): String = when (state.lowercase()) {
            PLANNED, "assigned" -> "Planned" // 'assigned' is legacy, map to Planned
            ON_ROUTE -> "On Route"
            COMPLETED -> "Completed"
            CANCELLED -> "Cancelled"
            FAILED -> "Failed"
            DELAYED -> "Delayed"
            // Legacy support
            "in_progress" -> "On Route"
            else -> state.replaceFirstChar { it.uppercase() }
        }

        /**
         * Emoji icons for each state.
         */
        fun getIcon(state: String): String = when (state.lowercase()) {
            PLANNED, "assigned" -> "📋" // 'assigned' is legacy, map to planned icon
            ON_ROUTE -> "🚗"
            COMPLETED -> "🏁"
            CANCELLED -> "❌"
            FAILED -> "⚠️"
            DELAYED -> "⏰"
            "in_progress" -> "🚗"
            else -> "❓"
        }

        /**
         * Color scheme for each state.
         */
        fun getColorScheme(state: String): StateColorScheme = when (state.lowercase()) {
            PLANNED, "assigned" -> StateColorScheme.INFO // 'assigned' is legacy, map to planned color
            ON_ROUTE -> StateColorScheme.WARNING
            COMPLETED -> StateColorScheme.SUCCESS
            CANCELLED -> StateColorScheme.NEUTRAL
            FAILED -> StateColorScheme.ERROR
            DELAYED -> StateColorScheme.WARNING
            "in_progress" -> StateColorScheme.WARNING
            else -> StateColorScheme.NEUTRAL
        }

        /**
         * Check if state is valid.
         * Accepts legacy 'assigned' and 'in_progress' for backward compatibility.
         */
        fun isValid(state: String): Boolean = state.lowercase() in ALL ||
            state.lowercase() == "in_progress" ||
            state.lowercase() == "assigned" // Legacy support

        /**
         * Check if trip is in progress.
         */
        fun isInProgress(state: String): Boolean = state.lowercase() == ON_ROUTE || state.lowercase() == "in_progress"

        /**
         * Check if trip is editable.
         * Note: Legacy 'assigned' maps to PLANNED which is editable.
         */
        fun isEditable(state: String): Boolean =
            state.lowercase() in EDITABLE || state.lowercase() == "assigned"

        /**
         * Check if trip can be cancelled.
         * Note: Legacy 'assigned' maps to PLANNED which is cancellable.
         */
        fun isCancellable(state: String): Boolean =
            state.lowercase() in CANCELLABLE || state.lowercase() == "assigned"

        /**
         * Check if trip is finished.
         */
        fun isFinished(state: String): Boolean = state.lowercase() in FINISHED
    }

    // ==================== State Color Schemes ====================
    /**
     * Color scheme categories for state display.
     */
    enum class StateColorScheme {
        SUCCESS,   // Green - positive states
        WARNING,   // Orange/Yellow - attention needed
        ERROR,     // Red - negative states
        INFO,      // Blue - informational states
        NEUTRAL    // Gray - inactive/neutral states
    }

    // ==================== State Transitions ====================
    /**
     * Valid state transitions for vehicles.
     *
     * Transitions:
     * - Inactive → Active (Enable vehicle)
     * - Active → On Route (Trip started)
     * - Active → Maintenance (Scheduled maintenance)
     * - Active → Damaged (Accident/damage reported)
     * - Active → Inactive (Disable vehicle)
     * - Active → Decommissioned (Retire vehicle)
     * - On Route → Active (Trip completed/cancelled)
     * - Maintenance → Active (Maintenance complete)
     * - Maintenance → Inactive (Disable during maintenance)
     * - Maintenance → Decommissioned (Beyond repair)
     * - Damaged → Maintenance (Repair started)
     * - Damaged → Decommissioned (Beyond repair)
     * - Damaged → Inactive (Disable damaged vehicle)
     */
    object VehicleTransitions {
        private val transitions = mapOf(
            VehicleState.INACTIVE to listOf(VehicleState.ACTIVE),
            VehicleState.ACTIVE to listOf(
                VehicleState.INACTIVE,
                VehicleState.ON_ROUTE,
                VehicleState.MAINTENANCE,
                VehicleState.DAMAGED,
                VehicleState.DECOMMISSIONED
            ),
            VehicleState.ON_ROUTE to listOf(VehicleState.ACTIVE, VehicleState.MAINTENANCE, VehicleState.DAMAGED),
            VehicleState.MAINTENANCE to listOf(VehicleState.ACTIVE, VehicleState.INACTIVE, VehicleState.DECOMMISSIONED),
            VehicleState.DAMAGED to listOf(VehicleState.MAINTENANCE, VehicleState.DECOMMISSIONED, VehicleState.INACTIVE),
            VehicleState.DECOMMISSIONED to emptyList<String>()
        )

        /**
         * Check if transition from one state to another is valid.
         */
        fun canTransition(from: String, to: String): Boolean {
            return transitions[from.lowercase()]?.contains(to.lowercase()) == true
        }

        /**
         * Get list of valid states to transition to from current state.
         */
        fun getValidTransitions(from: String): List<String> {
            return transitions[from.lowercase()] ?: emptyList()
        }

        /**
         * Validate transition and return error message if invalid.
         */
        fun validateTransition(from: String, to: String): TransitionResult {
            return if (canTransition(from, to)) {
                TransitionResult.Valid
            } else {
                TransitionResult.Invalid(
                    "Cannot transition vehicle from '${VehicleState.getDisplayLabel(from)}' to '${VehicleState.getDisplayLabel(to)}'"
                )
            }
        }
    }

    /**
     * Valid state transitions for drivers.
     *
     * Backend only validates the target status oneof
     * (active|inactive|on_trip|on_leave|suspended); these transitions are a
     * client-side UX guide for the status-change dialog.
     *
     * Transitions:
     * - Inactive → Active (Activate driver)
     * - Active → On Trip (Trip started)
     * - Active → On Leave (Leave approved)
     * - Active → Suspended (Suspension applied)
     * - Active → Inactive (Disable driver)
     * - On Trip → Active (Trip completed)
     * - On Leave → Active (Leave ended)
     * - On Leave → Inactive (Disable during leave)
     * - Suspended → Active (Suspension lifted)
     * - Suspended → Inactive (Disable suspended driver)
     */
    object DriverTransitions {
        private val transitions = mapOf(
            DriverState.INACTIVE to listOf(DriverState.ACTIVE),
            DriverState.ACTIVE to listOf(
                DriverState.INACTIVE,
                DriverState.ON_TRIP,
                DriverState.ON_LEAVE,
                DriverState.SUSPENDED
            ),
            DriverState.ON_TRIP to listOf(DriverState.ACTIVE),
            DriverState.ON_LEAVE to listOf(DriverState.ACTIVE, DriverState.INACTIVE),
            DriverState.SUSPENDED to listOf(DriverState.ACTIVE, DriverState.INACTIVE)
        )

        /**
         * Check if transition from one state to another is valid.
         * Legacy "on_route" is treated as "on_trip".
         */
        fun canTransition(from: String, to: String): Boolean {
            val normalizedFrom = if (from.lowercase() == "on_route") DriverState.ON_TRIP else from.lowercase()
            val normalizedTo = if (to.lowercase() == "on_route") DriverState.ON_TRIP else to.lowercase()
            return transitions[normalizedFrom]?.contains(normalizedTo) == true
        }

        /**
         * Get list of valid states to transition to from current state.
         * Legacy "on_route" is treated as "on_trip".
         */
        fun getValidTransitions(from: String): List<String> {
            val normalizedFrom = if (from.lowercase() == "on_route") DriverState.ON_TRIP else from.lowercase()
            return transitions[normalizedFrom] ?: emptyList()
        }

        /**
         * Validate transition and return error message if invalid.
         */
        fun validateTransition(from: String, to: String): TransitionResult {
            return if (canTransition(from, to)) {
                TransitionResult.Valid
            } else {
                TransitionResult.Invalid(
                    "Cannot transition driver from '${DriverState.getDisplayLabel(from)}' to '${DriverState.getDisplayLabel(to)}'"
                )
            }
        }
    }

    /**
     * Valid state transitions for trips.
     *
     * Transitions (Updated - 'assigned' state removed):
     * - Planned → On Route (Trip started)
     * - Planned → Cancelled (Cancel before start)
     * - On Route → Completed (Trip finished)
     * - On Route → Delayed (Behind schedule)
     * - On Route → Failed (Cannot complete)
     * - On Route → Cancelled (Cancel mid-trip)
     * - Delayed → On Route (Back on schedule)
     * - Delayed → Completed (Finished despite delay)
     * - Delayed → Failed (Cannot complete)
     * - Delayed → Cancelled (Cancel delayed trip)
     */
    object TripTransitions {
        private val transitions = mapOf(
            TripState.PLANNED to listOf(TripState.ON_ROUTE, TripState.CANCELLED),
            TripState.ON_ROUTE to listOf(TripState.COMPLETED, TripState.DELAYED, TripState.FAILED, TripState.CANCELLED),
            TripState.DELAYED to listOf(TripState.ON_ROUTE, TripState.COMPLETED, TripState.FAILED, TripState.CANCELLED),
            TripState.COMPLETED to emptyList<String>(),
            TripState.CANCELLED to emptyList<String>(),
            TripState.FAILED to emptyList<String>()
        )

        /**
         * Check if transition from one state to another is valid.
         */
        fun canTransition(from: String, to: String): Boolean {
            // Handle legacy states: "in_progress" → "on_route", "assigned" → "planned"
            val normalizedFrom = when (from.lowercase()) {
                "in_progress" -> TripState.ON_ROUTE
                "assigned" -> TripState.PLANNED // Legacy: treat assigned as planned
                else -> from.lowercase()
            }
            val normalizedTo = when (to.lowercase()) {
                "in_progress" -> TripState.ON_ROUTE
                "assigned" -> TripState.PLANNED // Legacy: treat assigned as planned
                else -> to.lowercase()
            }
            return transitions[normalizedFrom]?.contains(normalizedTo) == true
        }

        /**
         * Get list of valid states to transition to from current state.
         */
        fun getValidTransitions(from: String): List<String> {
            // Handle legacy states: "in_progress" → "on_route", "assigned" → "planned"
            val normalizedFrom = when (from.lowercase()) {
                "in_progress" -> TripState.ON_ROUTE
                "assigned" -> TripState.PLANNED // Legacy: treat assigned as planned
                else -> from.lowercase()
            }
            return transitions[normalizedFrom] ?: emptyList()
        }

        /**
         * Validate transition and return error message if invalid.
         */
        fun validateTransition(from: String, to: String): TransitionResult {
            return if (canTransition(from, to)) {
                TransitionResult.Valid
            } else {
                TransitionResult.Invalid(
                    "Cannot transition trip from '${TripState.getDisplayLabel(from)}' to '${TripState.getDisplayLabel(to)}'"
                )
            }
        }
    }

    /**
     * Result of a state transition validation.
     */
    sealed class TransitionResult {
        data object Valid : TransitionResult()
        data class Invalid(val message: String) : TransitionResult()
    }
}

