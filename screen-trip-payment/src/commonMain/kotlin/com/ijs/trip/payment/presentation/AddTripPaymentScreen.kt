package com.ijs.trip.payment.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.uicomponents.components.UiText
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Add/Edit Payment Screen.
 * Orchestrates the form sections and trip selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripPaymentScreen(
    viewModel: AddTripPaymentViewModel,
    tripId: String? = null,
    paymentId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val notApplicableLabel = stringResource(Res.string.label_not_applicable)
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Initialize
    LaunchedEffect(tripId, paymentId) {
        viewModel.sendIntent(AddPaymentContract.Intent.Initialize(tripId, paymentId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AddPaymentContract.Effect.NavigateBack -> onNavigateBack()
                is AddPaymentContract.Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is AddPaymentContract.Effect.ShowError -> pendingSnackbar = effect.message
                is AddPaymentContract.Effect.PaymentSaved -> { /* Handled in NavigateBack */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) {
                            stringResource(Res.string.payments_edit)
                        } else {
                            stringResource(Res.string.payments_add)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(AddPaymentContract.Intent.Cancel) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sendIntent(AddPaymentContract.Intent.Save) },
                        enabled = state.canSave
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check),
                                contentDescription = stringResource(Res.string.save)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Calculate date constraints based on selected trip
            val minPaymentDate = state.selectedTrip?.let { trip ->
                val dateSource = trip.tripStartDate ?: trip.scheduledDate
                if (dateSource != null) {
                    com.indusjs.datetimeutils.FleetDateTime.getMinDateForTripCost(dateSource)
                } else null
            }
            val maxPaymentDate = com.indusjs.datetimeutils.FleetDateTime.getDateFromToday(1)

            // Calculate trip start/end date & time for display
            val tripStartDateTimeDisplay = state.selectedTrip?.let { trip ->
                val date = trip.tripStartDate ?: trip.scheduledDate?.let {
                    com.indusjs.datetimeutils.FleetDateTime.getMinDateForTripCost(it)
                }
                val time = trip.tripStartTime
                if (date != null && time != null) "$date $time" else date ?: notApplicableLabel
            } ?: notApplicableLabel

            val tripEndDateTimeDisplay = state.selectedTrip?.let { trip ->
                val date = trip.tripEndDate ?: trip.scheduledDate
                val time = trip.tripEndTime
                if (date != null && time != null) "$date $time" else date ?: notApplicableLabel
            } ?: notApplicableLabel

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Trip Selection Section
                TripSelectionSection(
                    selectedTrip = state.selectedTrip,
                    error = state.tripError,
                    onSelectTrip = { viewModel.sendIntent(AddPaymentContract.Intent.ShowTripSelector) }
                )

                // Trip Info Card (if selected)
                state.selectedTrip?.let { trip ->
                    SelectedTripInfoCard(
                        trip = trip,
                        startDateTimeDisplay = tripStartDateTimeDisplay,
                        endDateTimeDisplay = tripEndDateTimeDisplay
                    )
                }

                HorizontalDivider()

                // Amount Section
                AmountSection(
                    amount = state.amount,
                    tdsAmount = state.tdsAmount,
                    discountAmount = state.discountAmount,
                    netAmount = state.netAmountDisplay,
                    amountError = state.amountError,
                    onAmountChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateAmount(it)) },
                    onTdsChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateTdsAmount(it)) },
                    onDiscountChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateDiscountAmount(it)) }
                )

                HorizontalDivider()

                // Payment Type Section (Dropdown)
                PaymentTypeDropdown(
                    selectedType = state.paymentType,
                    onTypeSelected = { viewModel.sendIntent(AddPaymentContract.Intent.UpdatePaymentType(it)) }
                )

                // Due Date (for Advance/Partial payments)
                if (state.showDueDate) {
                    FleetDateTimePicker(
                        date = state.dueDate,
                        time = "",
                        onDateTimeChange = { date, _ ->
                            viewModel.sendIntent(AddPaymentContract.Intent.UpdateDueDate(date))
                        },
                        mode = PickerMode.DATE_ONLY,
                        label = stringResource(Res.string.payment_add_due_date),
                        isError = false,
                        minDate = minPaymentDate
                    )
                }

                // Payment Mode Section
                Text(
                    text = stringResource(Res.string.payment_add_payment_mode),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                PaymentModeChips(
                    selectedMode = state.paymentMode,
                    onModeSelected = { viewModel.sendIntent(AddPaymentContract.Intent.UpdatePaymentMode(it)) }
                )

                HorizontalDivider()

                // Date & Time Section
                DateTimeSection(
                    date = state.paymentDate,
                    time = state.paymentTime,
                    error = state.dateError,
                    minDate = minPaymentDate,
                    maxDate = maxPaymentDate,
                    onDateTimeChange = { date, time ->
                        viewModel.sendIntent(AddPaymentContract.Intent.UpdatePaymentDateTime(date, time))
                    }
                )

                // Transaction Details (for Bank/UPI)
                if (state.showTransactionFields) {
                    TransactionDetailsSection(
                        paymentMode = state.paymentMode,
                        transactionId = state.transactionId,
                        bankName = state.bankName,
                        paymentSource = state.paymentSource,
                        onTransactionIdChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateTransactionId(it)) },
                        onBankNameChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateBankName(it)) },
                        onPaymentSourceChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdatePaymentSource(it)) }
                    )
                }

                HorizontalDivider()

                // Additional Info Section
                AdditionalInfoSection(
                    notes = state.notes,
                    receivedBy = state.receivedBy,
                    receivedAtLocation = state.receivedAtLocation,
                    onNotesChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateNotes(it)) },
                    onReceivedByChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateReceivedBy(it)) },
                    onLocationChange = { viewModel.sendIntent(AddPaymentContract.Intent.UpdateReceivedAtLocation(it)) }
                )

                // Error message
                state.error?.let { error ->
                    Text(
                        text = error.resolve(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Save Button
                Button(
                    onClick = { viewModel.sendIntent(AddPaymentContract.Intent.Save) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSave
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (state.isEditMode) stringResource(Res.string.payment_action_update) else stringResource(Res.string.payment_action_record))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Trip Selector Bottom Sheet
    if (state.showTripSelector) {
        TripSelectorBottomSheet(
            trips = state.trips,
            searchQuery = state.tripSearchQuery,
            isLoading = state.isLoadingTrips,
            onDismiss = { viewModel.sendIntent(AddPaymentContract.Intent.HideTripSelector) },
            onSearch = { viewModel.sendIntent(AddPaymentContract.Intent.SearchTrips(it)) },
            onSelect = { viewModel.sendIntent(AddPaymentContract.Intent.SelectTrip(it)) }
        )
    }
}

