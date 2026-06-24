package com.ijs.driver.presentation.detail

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.permission.PermissionChecker
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.util.ValidationUtils
import com.ijs.team.data.model.TeamMemberDto
import com.ijs.driver.domain.entity.DriverStatus
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.ijs.driver.domain.repository.DriverRepository
import com.ijs.team.domain.repository.TeamRepository
import com.ijs.driver.domain.usecase.DeleteDriverUseCase
import com.ijs.driver.domain.usecase.GetDriverByIdUseCase
import com.ijs.driver.domain.usecase.ToggleDriverActiveUseCase
import com.ijs.driver.domain.usecase.UpdateDriverStatusUseCase
import com.ijs.driver.domain.usecase.UpdateDriverUseCase
import com.ijs.driver.presentation.detail.DriverDetailContract.Effect
import com.ijs.driver.presentation.detail.DriverDetailContract.Intent
import com.ijs.driver.presentation.detail.DriverDetailContract.State
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import androidx.lifecycle.viewModelScope
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_first_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_last_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_fill_required_fields
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_no_costs_export
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_updated
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_deleted
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_activated
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_deactivated
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_driver_status_updated
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_driver
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_update_status
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_toggle_active
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_update_driver
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_delete_driver
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_costs
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_more_costs
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_history
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_update_driver_status
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Driver Detail screen implementing MVI pattern.
 */
@Inject
class DriverDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getDriverByIdUseCase: GetDriverByIdUseCase,
    private val updateDriverUseCase: UpdateDriverUseCase,
    private val updateDriverStatusUseCase: UpdateDriverStatusUseCase,
    private val toggleDriverActiveUseCase: ToggleDriverActiveUseCase,
    private val deleteDriverUseCase: DeleteDriverUseCase,
    private val driverRepository: DriverRepository,
    private val teamRepository: TeamRepository,
    private val costsRepository: CostsRepository,
    private val permissionChecker: PermissionChecker,
    private val statesRepository: StatesRepository? = null
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        // Compute permission flag from the user's actual permission set
        updateState { copy(canViewCostsPermission = permissionChecker.canViewFinancials()) }
        // Load DB-cached state labels
        viewModelScope.launch {
            try {
                val labels = statesRepository?.getDriverStatesFlat()?.toMap() ?: emptyMap()
                if (labels.isNotEmpty()) updateState { copy(stateLabels = labels) }
            } catch (_: Exception) { /* fallback to empty */ }
        }
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadDriver -> loadDriver(intent.driverId)
            is Intent.Refresh -> currentState.driver?.let { loadDriver(it.id) }

            // Edit mode
            is Intent.EnterEditMode -> enterEditMode()
            is Intent.ExitEditMode -> exitEditMode()

            // Field updates
            is Intent.UpdateFirstName -> updateFirstName(intent.value)
            is Intent.UpdateLastName -> updateLastName(intent.value)
            is Intent.UpdateEmail -> updateEmail(intent.value)
            is Intent.UpdateMobile -> updateMobile(intent.value)
            is Intent.UpdateLicenseNumber -> updateState { copy(licenseNumber = intent.value.uppercase()) }
            is Intent.UpdateLicenseType -> updateState { copy(licenseType = intent.type) }
            is Intent.UpdateLicenseExpiry -> updateState { copy(licenseExpiry = intent.value) }
            is Intent.UpdateDateOfBirth -> updateState { copy(dateOfBirth = intent.value) }
            is Intent.UpdateAddress -> updateState { copy(address = intent.value) }
            is Intent.UpdateEmergencyContact -> updateState { copy(emergencyContact = intent.value) }
            is Intent.UpdateBloodGroup -> updateState { copy(bloodGroup = intent.value) }
            is Intent.UpdateJoiningDate -> updateState { copy(joiningDate = intent.value) }

            // Actions
            is Intent.UpdateStatus -> updateStatus(intent.status)
            is Intent.ToggleActive -> toggleActive()

            // Caretaker assignment
            is Intent.LoadCaretakers -> loadCaretakers()
            is Intent.RefreshCaretakers -> refreshCaretakers()
            is Intent.ToggleCaretakerDropdown -> updateState { copy(showCaretakerDropdown = !showCaretakerDropdown) }
            is Intent.SelectCaretaker -> updateState { copy(selectedCaretaker = intent.caretaker, showCaretakerDropdown = false) }

            is Intent.SaveChanges -> saveChanges()
            is Intent.DeleteDriver -> sendEffect(Effect.ShowDeleteConfirmation)
            is Intent.ConfirmDelete -> confirmDelete()

            // Navigation & errors
            is Intent.NavigateBack -> sendEffect(Effect.NavigateBack)
            is Intent.NavigateToAddDriverCost -> navigateToAddDriverCost()
            is Intent.ClearError -> updateState { copy(error = null) }

            // Tab selection
            is Intent.SelectTab -> selectTab(intent.tabIndex)

            // Costs tab intents
            is Intent.LoadCosts -> loadCosts()
            is Intent.LoadMoreCosts -> loadMoreCosts()
            is Intent.RefreshCosts -> refreshCosts()
            is Intent.UpdateCostsDateRange -> updateState { copy(costsStartDate = intent.startDate, costsEndDate = intent.endDate) }
            is Intent.UpdateCostsMonth -> updateState { copy(costsMonth = intent.month) }
            is Intent.ShowCostsFilterSheet -> updateState { copy(showCostsFilterSheet = true) }
            is Intent.HideCostsFilterSheet -> updateState { copy(showCostsFilterSheet = false) }
            is Intent.ApplyCostFilters -> applyCostFilters(intent.startDate, intent.endDate, intent.month)
            is Intent.ClearCostFilters -> clearCostFilters()

            // Cost group expand/collapse
            is Intent.ToggleCostGroup -> toggleCostGroup(intent.groupId)
            is Intent.ExpandAllCostGroups -> expandAllCostGroups()
            is Intent.CollapseAllCostGroups -> collapseAllCostGroups()

            // PDF Export
            is Intent.ExportCostsToPdf -> exportCostsToPdf()

            // Trip navigation
            is Intent.NavigateToTripDetail -> sendEffect(Effect.NavigateToTripDetail(intent.tripId))

            // History tab intents
            is Intent.LoadHistory -> loadHistory()
            is Intent.LoadMoreHistory -> loadMoreHistory()
            is Intent.RefreshHistory -> refreshHistory()

            // State change intents
            is Intent.ShowStateChangeDialog -> updateState { copy(showStateChangeDialog = true) }
            is Intent.HideStateChangeDialog -> updateState { copy(showStateChangeDialog = false) }
            is Intent.UpdateDriverState -> updateDriverState(intent.newStatus, intent.reason)
        }
    }

    private fun selectTab(tabIndex: Int) {
        updateState { copy(selectedTab = tabIndex) }
        // Load tab data if needed (0 = Overview, 1 = Costs, 2 = History)
        when (tabIndex) {
            1 -> if (currentState.costs.isEmpty() && !currentState.isLoadingCosts) {
                viewModelScope.launch {
                    loadCosts()
                }
            }
            2 -> if (currentState.historyItems.isEmpty() && !currentState.isLoadingHistory) {
                viewModelScope.launch {
                    loadHistory()
                }
            }
        }
    }

    private suspend fun loadDriver(driverId: String) {
        updateState { copy(isLoading = true, error = null, driverId = driverId) }

        withContext(dispatcherProvider.io) {
            when (val result = getDriverByIdUseCase(driverId)) {
                is Result.Success -> {
                    val driver = result.data
                    updateState {
                        copy(
                            isLoading = false,
                            driver = driver,
                            // Populate editable fields
                            firstName = driver.firstName,
                            lastName = driver.lastName,
                            email = driver.email,
                            mobile = driver.mobile,
                            licenseNumber = driver.licenseNumber,
                            licenseType = driver.licenseType,
                            licenseExpiry = formatTimestamp(driver.licenseExpiry),
                            dateOfBirth = driver.dateOfBirth?.let { formatTimestamp(it) } ?: "",
                            address = driver.address ?: "",
                            emergencyContact = driver.emergencyContact ?: "",
                            bloodGroup = driver.bloodGroup ?: "",
                            joiningDate = driver.joiningDate?.let { formatTimestamp(it) } ?: ""
                        )
                    }
                }
                is Result.Error -> {
                    val errorText = result.message?.let { UiText.Raw(it) }
                        ?: UiText.StringRes(Res.string.error_load_driver)
                    updateState {
                        copy(
                            isLoading = false,
                            error = errorText
                        )
                    }
                    sendEffect(Effect.ShowError(errorText))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun enterEditMode() {
        updateState { copy(isEditMode = true, isLoadingCaretakers = true) }
        // Load caretakers
        viewModelScope.launch {
            loadCaretakers()
        }
    }

    private fun exitEditMode() {
        // Reset to original values
        val driver = currentState.driver
        if (driver != null) {
            updateState {
                copy(
                    isEditMode = false,
                    firstName = driver.firstName,
                    lastName = driver.lastName,
                    email = driver.email,
                    mobile = driver.mobile,
                    licenseType = driver.licenseType,
                    licenseNumber = driver.licenseNumber,
                    licenseExpiry = formatTimestamp(driver.licenseExpiry),
                    dateOfBirth = driver.dateOfBirth?.let { formatTimestamp(it) } ?: "",
                    address = driver.address ?: "",
                    emergencyContact = driver.emergencyContact ?: "",
                    bloodGroup = driver.bloodGroup ?: "",
                    joiningDate = driver.joiningDate?.let { formatTimestamp(it) } ?: "",
                    // Clear errors
                    firstNameError = null,
                    lastNameError = null,
                    mobileError = null,
                    emailError = null
                )
            }
        } else {
            updateState { copy(isEditMode = false) }
        }
    }

    private fun updateFirstName(value: String) {
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_first_name_required) else null
        updateState { copy(firstName = value, firstNameError = error) }
    }

    private fun updateLastName(value: String) {
        val error = if (value.isBlank()) UiText.StringRes(Res.string.error_last_name_required) else null
        updateState { copy(lastName = value, lastNameError = error) }
    }

    private fun updateEmail(value: String) {
        val error = if (value.isNotBlank() && !isValidEmail(value)) UiText.StringRes(Res.string.error_email_invalid) else null
        updateState { copy(email = value, emailError = error) }
    }

    private fun updateMobile(value: String) {
        val error = validateMobile(value)
        updateState { copy(mobile = value, mobileError = error) }
    }

    private fun validateMobile(value: String): UiText? {
        return when {
            value.isBlank() -> UiText.StringRes(Res.string.error_mobile_required)
            !ValidationUtils.isValidIndianMobile(value) -> UiText.StringRes(Res.string.error_mobile_invalid)
            else -> null
        }
    }

    private fun isValidEmail(email: String): Boolean = ValidationUtils.isValidEmail(email)

    private suspend fun updateStatus(status: DriverStatus) {
        val driverId = currentState.driver?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = updateDriverStatusUseCase(driverId, status)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            driver = result.data
                        )
                    }
                    val apiValue = DriverStatus.toApiString(status)
                    val label = currentState.stateLabels[apiValue]
                        ?: DriverStatus.getDisplayLabel(status)
                    sendEffect(
                        Effect.ShowSnackbar(
                            UiText.StringRes(
                                Res.string.success_driver_status_updated,
                                args = listOf(label)
                            )
                        )
                    )
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_update_status)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun toggleActive() {
        val driverId = currentState.driver?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = toggleDriverActiveUseCase(driverId)) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            driver = result.data
                        )
                    }
                    val message = if (result.data.isActive) {
                        UiText.StringRes(Res.string.success_driver_activated)
                    } else {
                        UiText.StringRes(Res.string.success_driver_deactivated)
                    }
                    sendEffect(Effect.ShowSnackbar(message))
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_toggle_active)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private suspend fun saveChanges() {
        if (!validateForm()) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_fill_required_fields)))
            return
        }

        val currentDriver = currentState.driver ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            val updatedDriver = currentDriver.copy(
                firstName = currentState.firstName.trim(),
                lastName = currentState.lastName.trim(),
                email = currentState.email.trim(),
                mobile = currentState.mobile.trim(),
                licenseNumber = currentState.licenseNumber.trim(),
                licenseType = currentState.licenseType,
                licenseExpiry = parseDateToTimestamp(currentState.licenseExpiry),
                dateOfBirth = currentState.dateOfBirth.takeIf { it.isNotBlank() }?.let { parseDateToTimestamp(it) },
                address = currentState.address.takeIf { it.isNotBlank() },
                emergencyContact = currentState.emergencyContact.takeIf { it.isNotBlank() },
                bloodGroup = currentState.bloodGroup.takeIf { it.isNotBlank() },
                joiningDate = currentState.joiningDate.takeIf { it.isNotBlank() }?.let { parseDateToTimestamp(it) }
            )

            when (val result = updateDriverUseCase(
                driver = updatedDriver,
                caretakerId = currentState.selectedCaretaker?.id
            )) {
                is Result.Success -> {
                    updateState {
                        copy(
                            isSaving = false,
                            isEditMode = false,
                            driver = result.data,
                            // Update fields with response
                            firstName = result.data.firstName,
                            lastName = result.data.lastName,
                            email = result.data.email,
                            mobile = result.data.mobile
                        )
                    }
                    sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_driver_updated)))
                    sendEffect(Effect.DriverUpdated)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_update_driver)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    private fun validateForm(): Boolean {
        val firstNameError = if (currentState.firstName.isBlank()) UiText.StringRes(Res.string.error_first_name_required) else null
        val lastNameError = if (currentState.lastName.isBlank()) UiText.StringRes(Res.string.error_last_name_required) else null
        val mobileError = validateMobile(currentState.mobile)
        val emailError = if (currentState.email.isNotBlank() && !isValidEmail(currentState.email)) UiText.StringRes(Res.string.error_email_invalid) else null

        updateState {
            copy(
                firstNameError = firstNameError,
                lastNameError = lastNameError,
                mobileError = mobileError,
                emailError = emailError
            )
        }

        return firstNameError == null && lastNameError == null && mobileError == null && emailError == null
    }

    private suspend fun confirmDelete() {
        val driverId = currentState.driver?.id ?: return
        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            when (val result = deleteDriverUseCase(driverId)) {
                is Result.Success -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.success_driver_deleted)))
                    sendEffect(Effect.DriverDeleted(driverId))
                    sendEffect(Effect.NavigateBack)
                }
                is Result.Error -> {
                    updateState { copy(isSaving = false) }
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_delete_driver)
                        )
                    )
                }
                is Result.Loading -> { /* Not applicable */ }
            }
        }
    }

    /**
     * Epoch millis -> ISO "YYYY-MM-DD" form string (the format the edit fields hold).
     */
    private fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val value = com.indusjs.datetimeutils.FleetEpoch.toValue(timestamp) ?: return ""
        val monthStr = value.month.toString().padStart(2, '0')
        val dayStr = value.day.toString().padStart(2, '0')
        return "${value.year}-$monthStr-$dayStr"
    }

    /**
     * ISO "YYYY-MM-DD" form string -> UTC epoch millis (canonical, via FleetEpoch).
     */
    private fun parseDateToTimestamp(dateString: String): Long {
        if (dateString.isBlank()) return 0L
        val parts = dateString.split("-")
        if (parts.size != 3) return 0L
        val year = parts[0].toIntOrNull() ?: return 0L
        val month = parts[1].toIntOrNull() ?: return 0L
        val day = parts[2].toIntOrNull() ?: return 0L
        val value = com.indusjs.datetimeutils.FleetDateTimeValue(
            year = year, month = month, day = day, hour = 0, minute = 0, second = 0
        )
        return com.indusjs.datetimeutils.FleetEpoch.fromValue(value) ?: 0L
    }

    // ==================== Costs Tab Functions ====================

    /**
     * Get default from date for API calls when user hasn't entered a date
     */
    private fun getDefaultFromDate(): String = "01-01-1971"

    /**
     * Get default to date (current date + 1 week) for API calls.
     * Uses FleetDateTime utility for cross-platform date calculation.
     */
    private fun getDefaultToDate(): String {
        return com.indusjs.datetimeutils.FleetDateTime.getDateFromToday(7)
    }

    private suspend fun loadCosts() {
        val driverId = currentState.driver?.id ?: currentState.driverId
        if (driverId.isBlank()) return

        updateState { copy(isLoadingCosts = true, costsError = null, costsPage = 1) }

        val startDate = currentState.costsStartDate.takeIf { it.isNotBlank() } ?: getDefaultFromDate()
        val endDate = currentState.costsEndDate.takeIf { it.isNotBlank() } ?: getDefaultToDate()
        val month = currentState.costsMonth.takeIf { it.isNotBlank() }

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.getDriverCosts(
                driverId = driverId,
                page = 1,
                perPage = 50,
                groupId = null,
                month = month,
                startDate = if (month == null) startDate else null,
                endDate = if (month == null) endDate else null
            )) {
                is Result.Success -> {
                    val data = result.data
                    val costs = data.costs
                    val summary = data.summary

                    // Calculate totals locally to ensure accuracy. The API summary may
                    // miss driver-cost entries added via trips (those are tracked
                    // differently on the backend), so local sums are the source of truth.
                    val localEarnings = costs.filter { !it.isDeductionCost }.sumOf { it.amount }
                    val localDeductions = costs.filter { it.isDeductionCost }.sumOf { it.amount }
                    val localNetAmount = localEarnings - localDeductions

                    // Prefer backend summary only when it has meaningful values.
                    val totalEarnings = if (summary != null && summary.totalEarnings > 0) {
                        summary.totalEarnings
                    } else {
                        localEarnings
                    }
                    val totalDeductions = if (summary != null && summary.totalDeductions > 0) {
                        summary.totalDeductions
                    } else {
                        localDeductions
                    }
                    val netAmount = if (summary != null && (summary.totalEarnings > 0 || summary.totalDeductions > 0)) {
                        summary.netEarnings
                    } else {
                        localNetAmount
                    }

                    // Compute cost breakdown by group
                    val byGroup = costs.groupBy { it.groupId }

                    // Compute cost breakdown by type
                    val byType = costs.groupBy { it.costId }

                    // Compute group totals
                    val groupTotals = byGroup.mapValues { (_, groupCosts) ->
                        groupCosts.sumOf { it.amount }
                    }

                    // All groups expanded by default
                    val allGroups = byGroup.keys

                    updateState {
                        copy(
                            isLoadingCosts = false,
                            costs = costs,
                            costsTotalAmount = totalEarnings,
                            costsDeductionsAmount = totalDeductions,
                            costsNetAmount = netAmount,
                            costsPage = data.page,
                            hasMoreCosts = data.page < data.totalPages,
                            costsByGroup = byGroup,
                            costsByType = byType,
                            groupTotals = groupTotals,
                            expandedGroups = allGroups
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingCosts = false,
                            costsError = result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_load_costs)
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    private suspend fun loadMoreCosts() {
        val driverId = currentState.driver?.id ?: currentState.driverId
        if (driverId.isBlank() || !currentState.hasMoreCosts || currentState.isLoadingCosts) return

        val nextPage = currentState.costsPage + 1
        updateState { copy(isLoadingCosts = true) }

        val startDate = currentState.costsStartDate.takeIf { it.isNotBlank() } ?: getDefaultFromDate()
        val endDate = currentState.costsEndDate.takeIf { it.isNotBlank() } ?: getDefaultToDate()
        val month = currentState.costsMonth.takeIf { it.isNotBlank() }

        withContext(dispatcherProvider.io) {
            when (val result = costsRepository.getDriverCosts(
                driverId = driverId,
                page = nextPage,
                perPage = 50,
                groupId = null,
                month = month,
                startDate = if (month == null) startDate else null,
                endDate = if (month == null) endDate else null
            )) {
                is Result.Success -> {
                    val data = result.data
                    updateState {
                        copy(
                            isLoadingCosts = false,
                            costs = costs + data.costs,
                            costsPage = data.page,
                            hasMoreCosts = data.page < data.totalPages
                        )
                    }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingCosts = false) }
                    sendEffect(
                        Effect.ShowSnackbar(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_load_more_costs)
                        )
                    )
                }
                is Result.Loading -> {}
            }
        }
    }

    private suspend fun refreshCosts() {
        updateState { copy(costs = emptyList(), costsPage = 1, hasMoreCosts = false) }
        loadCosts()
    }

    private suspend fun applyCostFilters(startDate: String, endDate: String, month: String) {
        updateState {
            copy(
                costsStartDate = startDate,
                costsEndDate = endDate,
                costsMonth = month,
                showCostsFilterSheet = false
            )
        }
        refreshCosts()
    }

    private suspend fun clearCostFilters() {
        updateState {
            copy(
                costsStartDate = "",
                costsEndDate = "",
                costsMonth = "",
                showCostsFilterSheet = false
            )
        }
        refreshCosts()
    }

    // ==================== Cost Group Functions ====================

    private fun toggleCostGroup(groupId: String) {
        updateState {
            val newExpanded = if (expandedGroups.contains(groupId)) {
                expandedGroups - groupId
            } else {
                expandedGroups + groupId
            }
            copy(expandedGroups = newExpanded)
        }
    }

    private fun expandAllCostGroups() {
        updateState {
            copy(expandedGroups = costsByGroup.keys)
        }
    }

    private fun collapseAllCostGroups() {
        updateState {
            copy(expandedGroups = emptySet())
        }
    }

    // ==================== PDF Export Functions ====================

    private suspend fun exportCostsToPdf() {
        val driver = currentState.driver ?: return
        val costs = currentState.costs

        if (costs.isEmpty()) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.error_no_costs_export)))
            return
        }

        // Build period string
        val period = when {
            currentState.costsMonth.isNotBlank() -> currentState.costsMonth
            currentState.costsStartDate.isNotBlank() && currentState.costsEndDate.isNotBlank() ->
                "${currentState.costsStartDate} to ${currentState.costsEndDate}"
            else -> "All Time"
        }

        // Get group names mapping
        val groupNames = mapOf(
            "DC-G-001" to "Salary & Wages",
            "DC-G-002" to "Incentives & Bonuses",
            "DC-G-003" to "Deductions",
            "DC-G-004" to "Other"
        )

        // Convert costs to PDF items
        val pdfCosts = costs.map { cost ->
            com.indusjs.pdfreport.model.DriverCostItem(
                id = cost.id,
                costId = cost.costId,
                costLabel = cost.displayLabel,
                groupId = cost.groupId,
                groupName = groupNames[cost.groupId] ?: "Other",
                amount = cost.amount,
                date = cost.date?.takeIf { it > 0L }
                    ?.let { com.indusjs.fleet.core.util.formatDateToHumanReadable(it) } ?: "",
                isDeduction = cost.isDeductionCost,
                tripId = cost.tripId,
                description = cost.description,
                notes = cost.notes
            )
        }

        // Group costs for PDF
        val pdfCostsByGroup = pdfCosts.groupBy { it.groupId }
            .mapKeys { (groupId, _) -> groupNames[groupId] ?: "Other" }

        // Generate timestamp using FleetDateTime
        val generatedAt = com.indusjs.datetimeutils.FleetDateTime.currentDateTime()

        val pdfData = com.indusjs.pdfreport.model.DriverCostsPdfData(
            driverId = driver.id.toIntOrNull() ?: 0,
            driverName = "${driver.firstName} ${driver.lastName}",
            mobile = driver.mobile,
            licenseNumber = driver.licenseNumber,
            period = period,
            costs = pdfCosts,
            costsByGroup = pdfCostsByGroup,
            totalEarnings = currentState.costsTotalAmount,
            totalDeductions = currentState.costsDeductionsAmount,
            netAmount = currentState.costsNetAmount,
            entryCount = costs.size,
            categoryCount = currentState.costCategoryCount,
            generatedAt = generatedAt
        )

        sendEffect(Effect.ExportPdf(pdfData))
    }

    // ==================== History Tab Functions ====================

    private suspend fun loadHistory() {
        val driverId = currentState.driver?.id ?: currentState.driverId
        if (driverId.isBlank()) return

        updateState { copy(isLoadingHistory = true, historyError = null, historyPage = 1) }

        withContext(dispatcherProvider.io) {
            when (val result = driverRepository.getDriverHistory(driverId, 1, 20)) {
                is Result.Success -> {
                    val data = result.data
                    updateState {
                        copy(
                            isLoadingHistory = false,
                            historyItems = data.history ?: emptyList(),
                            historyPage = data.page,
                            hasMoreHistory = data.page < data.totalPages,
                            historyTotalCount = data.totalCount
                        )
                    }
                }
                is Result.Error -> {
                    updateState {
                        copy(
                            isLoadingHistory = false,
                            historyError = result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_load_history)
                        )
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    private suspend fun loadMoreHistory() {
        val driverId = currentState.driver?.id ?: currentState.driverId
        if (driverId.isBlank()) return
        if (currentState.isLoadingHistory || !currentState.hasMoreHistory) return

        val nextPage = currentState.historyPage + 1
        updateState { copy(isLoadingHistory = true) }

        withContext(dispatcherProvider.io) {
            when (val result = driverRepository.getDriverHistory(driverId, nextPage, 20)) {
                is Result.Success -> {
                    val data = result.data
                    updateState {
                        copy(
                            isLoadingHistory = false,
                            historyItems = historyItems + (data.history ?: emptyList()),
                            historyPage = data.page,
                            hasMoreHistory = data.page < data.totalPages
                        )
                    }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingHistory = false) }
                }
                is Result.Loading -> {}
            }
        }
    }

    private suspend fun refreshHistory() {
        updateState { copy(historyItems = emptyList(), historyPage = 1, hasMoreHistory = false) }
        loadHistory()
    }

    // ==================== Caretaker Management ====================

    private suspend fun loadCaretakers() {
        updateState { copy(isLoadingCaretakers = true) }

        withContext(dispatcherProvider.io) {
            // First try to load from cache
            val cacheResult = teamRepository.getCaretakersFromCache()
            cacheResult.fold(
                onSuccess = { cachedCaretakers ->
                    if (cachedCaretakers.isNotEmpty()) {
                        val caretakers = cachedCaretakers.map { it.toDto() }
                        updateState { copy(isLoadingCaretakers = false, caretakers = caretakers) }
                    } else {
                        // Cache is empty, fetch from API
                        fetchCaretakersFromApi()
                    }
                },
                onFailure = {
                    // Cache failed, fetch from API
                    fetchCaretakersFromApi()
                }
            )
        }
    }

    private suspend fun refreshCaretakers() {
        updateState { copy(isLoadingCaretakers = true) }
        withContext(dispatcherProvider.io) {
            fetchCaretakersFromApi()
        }
    }

    private suspend fun fetchCaretakersFromApi() {
        val result = teamRepository.refreshTeamMembers()
        result.fold(
            onSuccess = { teamMembers ->
                val caretakers = teamMembers
                    .filter { member ->
                        member.isCaretakerEligible
                    }
                    .map { it.toDto() }
                updateState { copy(isLoadingCaretakers = false, caretakers = caretakers) }
            },
            onFailure = {
                updateState { copy(isLoadingCaretakers = false) }
            }
        )
    }

    private fun com.ijs.team.domain.entity.TeamMember.toDto(): TeamMemberDto {
        return TeamMemberDto(
            id = id.toIntOrNull() ?: 0,
            email = email,
            mobile = mobile,
            firstName = firstName,
            lastName = lastName,
            role = role.toApiString(),
            ownerId = ownerId.toIntOrNull() ?: 0,
            isActive = isActive,
            isCaretakerEligible = isCaretakerEligible,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    // ==================== State Change ====================

    private suspend fun updateDriverState(newStatus: String, reason: String?) {
        val driverId = currentState.driverId
        if (driverId.isBlank()) return

        updateState { copy(isUpdatingState = true) }

        withContext(dispatcherProvider.io) {
            when (val result = driverRepository.updateDriverStatusWithReason(driverId, newStatus, reason)) {
                is Result.Success -> {
                    val updatedDriver = result.data
                    updateState {
                        copy(
                            isUpdatingState = false,
                            showStateChangeDialog = false,
                            driver = updatedDriver
                        )
                    }
                    sendEffect(Effect.StateUpdated(newStatus))
                    val label = currentState.stateLabels[newStatus]
                        ?: com.indusjs.fleet.core.constants.StatusConstants.DriverState.getDisplayLabel(newStatus)
                    sendEffect(
                        Effect.ShowSnackbar(
                            UiText.StringRes(
                                Res.string.success_driver_status_updated,
                                args = listOf(label)
                            )
                        )
                    )
                }
                is Result.Error -> {
                    updateState { copy(isUpdatingState = false) }
                    sendEffect(
                        Effect.ShowError(
                            result.message?.let { UiText.Raw(it) }
                                ?: UiText.StringRes(Res.string.error_update_driver_status)
                        )
                    )
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private fun navigateToAddDriverCost() {
        val driverId = currentState.driver?.id ?: return
        sendEffect(Effect.NavigateToAddDriverCost(driverId))
    }
}
