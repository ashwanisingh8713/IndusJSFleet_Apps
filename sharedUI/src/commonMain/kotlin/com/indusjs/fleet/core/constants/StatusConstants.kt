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
     * States:
     * - inactive: Driver is disabled/not working
     * - active: Driver is available for assignment
     * - on_route: Driver is currently on a trip
     * - on_leave: Driver is on approved leave
     * - suspended: Driver privileges suspended
     * - terminated: Driver employment terminated (optional)
     */
    object DriverState {
        const val INACTIVE = "inactive"
        const val ACTIVE = "active"
        const val ON_ROUTE = "on_route"
        const val ON_LEAVE = "on_leave"
        const val SUSPENDED = "suspended"
        const val TERMINATED = "terminated"

        /**
         * All valid driver states.
         */
        val ALL = listOf(INACTIVE, ACTIVE, ON_ROUTE, ON_LEAVE, SUSPENDED, TERMINATED)

        /**
         * Core driver states (excluding optional).
         */
        val CORE = listOf(INACTIVE, ACTIVE, ON_ROUTE, ON_LEAVE, SUSPENDED)

        /**
         * States where driver is available for assignment.
         */
        val AVAILABLE_FOR_ASSIGNMENT = listOf(ACTIVE)

        /**
         * States where driver is unavailable.
         */
        val UNAVAILABLE = listOf(INACTIVE, ON_ROUTE, ON_LEAVE, SUSPENDED, TERMINATED)

        /**
         * Display labels for each state.
         */
        fun getDisplayLabel(state: String): String = when (state.lowercase()) {
            INACTIVE -> "Inactive"
            ACTIVE -> "Active"
            ON_ROUTE -> "On Route"
            ON_LEAVE -> "On Leave"
            SUSPENDED -> "Suspended"
            TERMINATED -> "Terminated"
            else -> state.replaceFirstChar { it.uppercase() }
        }

        /**
         * Emoji icons for each state.
         */
        fun getIcon(state: String): String = when (state.lowercase()) {
            INACTIVE -> "⚫"
            ACTIVE -> "🟢"
            ON_ROUTE -> "🚗"
            ON_LEAVE -> "🏖️"
            SUSPENDED -> "⏸️"
            TERMINATED -> "🚫"
            else -> "❓"
        }

        /**
         * Color scheme for each state.
         */
        fun getColorScheme(state: String): StateColorScheme = when (state.lowercase()) {
            INACTIVE -> StateColorScheme.NEUTRAL
            ACTIVE -> StateColorScheme.SUCCESS
            ON_ROUTE -> StateColorScheme.INFO
            ON_LEAVE -> StateColorScheme.WARNING
            SUSPENDED -> StateColorScheme.ERROR
            TERMINATED -> StateColorScheme.NEUTRAL
            else -> StateColorScheme.NEUTRAL
        }

        /**
         * Check if state is valid.
         */
        fun isValid(state: String): Boolean = state.lowercase() in ALL

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
     * - assigned: Vehicle and driver confirmed
     * - on_route: Trip is in progress
     * - completed: Trip finished successfully
     * - cancelled: Trip cancelled before completion
     * - failed: Trip could not be completed (optional)
     * - delayed: Trip is behind schedule (optional)
     */
    object TripState {
        const val PLANNED = "planned"
        const val ASSIGNED = "assigned"
        const val ON_ROUTE = "on_route"
        const val COMPLETED = "completed"
        const val CANCELLED = "cancelled"
        const val FAILED = "failed"
        const val DELAYED = "delayed"

        /**
         * All valid trip states.
         */
        val ALL = listOf(PLANNED, ASSIGNED, ON_ROUTE, COMPLETED, CANCELLED, FAILED, DELAYED)

        /**
         * Core trip states (excluding optional).
         */
        val CORE = listOf(PLANNED, ASSIGNED, ON_ROUTE, COMPLETED, CANCELLED)

        /**
         * States where trip is active/ongoing.
         */
        val ACTIVE = listOf(PLANNED, ASSIGNED, ON_ROUTE, DELAYED)

        /**
         * States where trip is finished (success or failure).
         */
        val FINISHED = listOf(COMPLETED, CANCELLED, FAILED)

        /**
         * States where trip can be modified.
         */
        val EDITABLE = listOf(PLANNED, ASSIGNED)

        /**
         * States where trip can be cancelled.
         */
        val CANCELLABLE = listOf(PLANNED, ASSIGNED, DELAYED, ON_ROUTE)

        /**
         * Display labels for each state.
         */
        fun getDisplayLabel(state: String): String = when (state.lowercase()) {
            PLANNED -> "Planned"
            ASSIGNED -> "Assigned"
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
            PLANNED -> "📋"
            ASSIGNED -> "✅"
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
            PLANNED -> StateColorScheme.INFO
            ASSIGNED -> StateColorScheme.INFO
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
         */
        fun isValid(state: String): Boolean = state.lowercase() in ALL || state.lowercase() == "in_progress"

        /**
         * Check if trip is in progress.
         */
        fun isInProgress(state: String): Boolean = state.lowercase() == ON_ROUTE || state.lowercase() == "in_progress"

        /**
         * Check if trip is editable.
         */
        fun isEditable(state: String): Boolean = state.lowercase() in EDITABLE

        /**
         * Check if trip can be cancelled.
         */
        fun isCancellable(state: String): Boolean = state.lowercase() in CANCELLABLE

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
     * Transitions:
     * - Inactive → Active (Activate driver)
     * - Active → On Route (Trip started)
     * - Active → On Leave (Leave approved)
     * - Active → Suspended (Suspension applied)
     * - Active → Inactive (Disable driver)
     * - Active → Terminated (Terminate employment)
     * - On Route → Active (Trip completed)
     * - On Leave → Active (Leave ended)
     * - On Leave → Inactive (Disable during leave)
     * - Suspended → Active (Suspension lifted)
     * - Suspended → Terminated (Termination)
     * - Suspended → Inactive (Disable suspended driver)
     */
    object DriverTransitions {
        private val transitions = mapOf(
            DriverState.INACTIVE to listOf(DriverState.ACTIVE),
            DriverState.ACTIVE to listOf(
                DriverState.INACTIVE,
                DriverState.ON_ROUTE,
                DriverState.ON_LEAVE,
                DriverState.SUSPENDED,
                DriverState.TERMINATED
            ),
            DriverState.ON_ROUTE to listOf(DriverState.ACTIVE),
            DriverState.ON_LEAVE to listOf(DriverState.ACTIVE, DriverState.INACTIVE),
            DriverState.SUSPENDED to listOf(DriverState.ACTIVE, DriverState.TERMINATED, DriverState.INACTIVE),
            DriverState.TERMINATED to emptyList<String>()
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
                    "Cannot transition driver from '${DriverState.getDisplayLabel(from)}' to '${DriverState.getDisplayLabel(to)}'"
                )
            }
        }
    }

    /**
     * Valid state transitions for trips.
     *
     * Transitions:
     * - Planned → Assigned (Vehicle/Driver assigned)
     * - Planned → Cancelled (Cancel before start)
     * - Assigned → On Route (Trip started)
     * - Assigned → Cancelled (Cancel before start)
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
            TripState.PLANNED to listOf(TripState.ASSIGNED, TripState.CANCELLED),
            TripState.ASSIGNED to listOf(TripState.ON_ROUTE, TripState.CANCELLED),
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
            // Handle legacy "in_progress" as "on_route"
            val normalizedFrom = if (from.lowercase() == "in_progress") TripState.ON_ROUTE else from.lowercase()
            val normalizedTo = if (to.lowercase() == "in_progress") TripState.ON_ROUTE else to.lowercase()
            return transitions[normalizedFrom]?.contains(normalizedTo) == true
        }

        /**
         * Get list of valid states to transition to from current state.
         */
        fun getValidTransitions(from: String): List<String> {
            val normalizedFrom = if (from.lowercase() == "in_progress") TripState.ON_ROUTE else from.lowercase()
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

