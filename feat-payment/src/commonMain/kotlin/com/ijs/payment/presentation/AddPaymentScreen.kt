package com.ijs.payment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.ijs.payment.domain.entity.*
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Add/Edit Payment Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    viewModel: AddPaymentViewModel,
    tripId: String? = null,
    paymentId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Initialize
    LaunchedEffect(tripId, paymentId) {
        viewModel.sendIntent(AddPaymentContract.Intent.Initialize(tripId, paymentId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddPaymentContract.Effect.NavigateBack -> onNavigateBack()
                is AddPaymentContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is AddPaymentContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is AddPaymentContract.Effect.PaymentSaved -> { /* Handled in NavigateBack */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(AddPaymentContract.Intent.Cancel) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
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
                                contentDescription = "Save"
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
            // Calculate date constraints based on selected trip (similar to TripCostEntryScreen)
            val minPaymentDate = state.selectedTrip?.let { trip ->
                // Try tripStartDate first, then scheduledDate
                val dateSource = trip.tripStartDate ?: trip.scheduledDate
                if (dateSource != null) {
                    com.indusjs.datetimeutils.FleetDateTime.getMinDateForTripCost(dateSource)
                } else null
            }
            val maxPaymentDate = com.indusjs.datetimeutils.FleetDateTime.getDateFromToday(1) // Tomorrow

            // Calculate trip start/end date & time for display
            val tripStartDateTimeDisplay = state.selectedTrip?.let { trip ->
                val date = trip.tripStartDate ?: trip.scheduledDate?.let {
                    com.indusjs.datetimeutils.FleetDateTime.getMinDateForTripCost(it)
                }
                val time = trip.tripStartTime
                if (date != null && time != null) "$date $time" else date ?: "N/A"
            } ?: "N/A"

            val tripEndDateTimeDisplay = state.selectedTrip?.let { trip ->
                val date = trip.tripEndDate ?: trip.scheduledDate // fallback
                val time = trip.tripEndTime
                if (date != null && time != null) "$date $time" else date ?: "N/A"
            } ?: "N/A"

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
                        label = "Due Date (Optional)",
                        isError = false,
                        minDate = minPaymentDate  // Due date must be after trip start date
                    )
                }

                // Payment Mode Section
                Text(
                    text = "Payment Mode",
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
                        text = error,
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
                    Text(if (state.isEditMode) "Update Payment" else "Record Payment")
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

@Composable
private fun TripSelectionSection(
    selectedTrip: TripSummaryForPayment?,
    error: String?,
    onSelectTrip: () -> Unit
) {
    Column {
        Text(
            text = "Select Trip *",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            onClick = onSelectTrip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedTrip != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trip #${selectedTrip.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = selectedTrip.routeDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedTrip.vehicleRegistration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "Tap to select a trip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = "Select",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun SelectedTripInfoCard(trip: TripSummaryForPayment, startDateTimeDisplay: String, endDateTimeDisplay: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Trip Price, Pending, Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Trip Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.tripPriceDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.pendingDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.hasPendingAmount) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Customer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.customerName?.take(12) ?: "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Row 2: Start & End Date/Time
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🚀 Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = startDateTimeDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🏁 End",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = endDateTimeDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountSection(
    amount: String,
    tdsAmount: String,
    discountAmount: String,
    netAmount: String,
    amountError: String?,
    onAmountChange: (String) -> Unit,
    onTdsChange: (String) -> Unit,
    onDiscountChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Amount Details",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Amount field
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Amount *") },
            placeholder = { Text("Enter amount") },
            prefix = { Text("₹") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } },
            singleLine = true
        )


        Spacer(modifier = Modifier.height(8.dp))

        // TDS and Discount row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = tdsAmount,
                onValueChange = onTdsChange,
                label = { Text("TDS") },
                prefix = { Text("₹") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = discountAmount,
                onValueChange = onDiscountChange,
                label = { Text("Discount") },
                prefix = { Text("₹") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        // Net amount display
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Net Amount",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = netAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentTypeDropdown(
    selectedType: PaymentType,
    onTypeSelected: (PaymentType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "Payment Type",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = "${selectedType.icon} ${selectedType.displayName}",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                supportingText = {
                    Text(
                        text = selectedType.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                PaymentType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${type.icon} ${type.displayName} - ${type.description}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentModeChips(
    selectedMode: PaymentMode,
    onModeSelected: (PaymentMode) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMode.entries.take(3).forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text("${mode.icon} ${mode.displayName}") }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMode.entries.drop(3).forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = { Text("${mode.icon} ${mode.displayName}") }
                )
            }
        }
    }
}

@Composable
private fun DateTimeSection(
    date: String,
    time: String,
    error: String?,
    minDate: String? = null,
    maxDate: String? = null,
    onDateTimeChange: (String, String) -> Unit
) {
    FleetDateTimePicker(
        date = date,
        time = time,
        onDateTimeChange = onDateTimeChange,
        mode = PickerMode.DATE_TIME,
        label = "Payment Date & Time *",
        isError = error != null,
        errorMessage = error,
        minDate = minDate,
        maxDate = maxDate
    )
}

@Composable
private fun TransactionDetailsSection(
    paymentMode: PaymentMode,
    transactionId: String,
    bankName: String,
    paymentSource: String,
    onTransactionIdChange: (String) -> Unit,
    onBankNameChange: (String) -> Unit,
    onPaymentSourceChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Transaction Details",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (paymentMode) {
            PaymentMode.UPI -> {
                // UPI: Only UPI ID field required
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("UPI ID *") },
                    placeholder = { Text("e.g., user@upi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            PaymentMode.BANK_TRANSFER -> {
                // Bank Transfer: Bank Name, Account Number, and Transaction Reference
                OutlinedTextField(
                    value = bankName,
                    onValueChange = onBankNameChange,
                    label = { Text("Bank Name *") },
                    placeholder = { Text("Enter bank name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = paymentSource,
                    onValueChange = onPaymentSourceChange,
                    label = { Text("Account Number *") },
                    placeholder = { Text("Enter account number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("Transaction Reference") },
                    placeholder = { Text("Enter transaction ID (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            PaymentMode.CARD -> {
                // Card: Transaction ID only
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("Transaction ID") },
                    placeholder = { Text("Enter card transaction ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            else -> {
                // Cash/Credit: Optional transaction reference
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = onTransactionIdChange,
                    label = { Text("Reference (Optional)") },
                    placeholder = { Text("Enter reference if any") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
private fun AdditionalInfoSection(
    notes: String,
    receivedBy: String,
    receivedAtLocation: String,
    onNotesChange: (String) -> Unit,
    onReceivedByChange: (String) -> Unit,
    onLocationChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Additional Info",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = receivedBy,
                onValueChange = onReceivedByChange,
                label = { Text("Received By") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = receivedAtLocation,
                onValueChange = onLocationChange,
                label = { Text("Location") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TripSelectorBottomSheet(
    trips: List<TripSummaryForPayment>,
    searchQuery: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSearch: (String) -> Unit,
    onSelect: (TripSummaryForPayment) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Filter state
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var selectedPaymentFilter by remember { mutableStateOf<String?>(null) }
    var showOnlyWithPending by remember { mutableStateOf(true) }

    // Filter trips based on search and filters
    val filteredTrips = remember(trips, searchQuery, selectedStatusFilter, selectedPaymentFilter, showOnlyWithPending) {
        trips.filter { trip ->
            val matchesSearch = searchQuery.isBlank() ||
                trip.id.contains(searchQuery, ignoreCase = true) ||
                trip.vehicleRegistration.contains(searchQuery, ignoreCase = true) ||
                trip.startLocation.contains(searchQuery, ignoreCase = true) ||
                trip.endLocation.contains(searchQuery, ignoreCase = true) ||
                (trip.customerName?.contains(searchQuery, ignoreCase = true) == true)

            val matchesTripStatus = selectedStatusFilter == null ||
                trip.state?.lowercase() == selectedStatusFilter?.lowercase()

            val matchesPaymentStatus = selectedPaymentFilter == null ||
                trip.paymentStatus?.lowercase() == selectedPaymentFilter?.lowercase()

            val matchesPending = !showOnlyWithPending || trip.hasPendingAmount

            matchesSearch && matchesTripStatus && matchesPaymentStatus && matchesPending
        }.sortedByDescending { it.pendingAmount } // Show highest pending first
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Trip",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredTrips.size} trips",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search trip, vehicle, customer, route...") },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show only pending toggle
                FilterChip(
                    selected = showOnlyWithPending,
                    onClick = { showOnlyWithPending = !showOnlyWithPending },
                    label = { Text("With Pending", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = if (showOnlyWithPending) {
                        { Text("✓", style = MaterialTheme.typography.labelSmall) }
                    } else null
                )

                // Trip Status filters
                listOf("on_route" to "On Route", "completed" to "Completed", "planned" to "Planned").forEach { (value, label) ->
                    FilterChip(
                        selected = selectedStatusFilter == value,
                        onClick = {
                            selectedStatusFilter = if (selectedStatusFilter == value) null else value
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }

                // Payment Status filters
                listOf("pending" to "Unpaid", "partial" to "Partial").forEach { (value, label) ->
                    FilterChip(
                        selected = selectedPaymentFilter == value,
                        onClick = {
                            selectedPaymentFilter = if (selectedPaymentFilter == value) null else value
                        },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (value == "pending")
                                Color(0xFFE65100).copy(alpha = 0.2f)
                            else
                                Color(0xFFFFA000).copy(alpha = 0.2f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredTrips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No trips available" else "No matching trips",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (showOnlyWithPending) {
                            TextButton(onClick = { showOnlyWithPending = false }) {
                                Text("Show all trips")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTrips, key = { it.id }) { trip ->
                        EnhancedTripCard(
                            trip = trip,
                            onSelect = { onSelect(trip) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EnhancedTripCard(
    trip: TripSummaryForPayment,
    onSelect: () -> Unit
) {
    val paymentStatusColor = when {
        trip.isFullyPaid -> Color(0xFF2E7D32) // Green
        trip.paidAmount > 0 -> Color(0xFFFFA000) // Orange - Partial
        else -> Color(0xFFE65100) // Red-Orange - Pending
    }

    val tripStateColor = when (trip.state?.lowercase()) {
        "completed" -> Color(0xFF2E7D32)
        "on_route" -> Color(0xFF1976D2)
        "planned" -> Color(0xFF7B1FA2)
        "cancelled", "failed" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Trip ID, Vehicle, Trip Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${trip.id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = trip.vehicleRegistration,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = tripStateColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = trip.tripStateLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = tripStateColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Route (compact)
            Text(
                text = "📍 ${trip.routeDisplay}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Row 3: Customer & Date (if available)
            if (trip.customerName != null || trip.scheduledDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trip.customerName?.let { customer ->
                        Text(
                            text = "👤 ${customer.take(20)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    trip.scheduledDate?.let { date ->
                        Text(
                            text = "📅 $date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Row 4: Financial info (Trip Price, Received, Pending)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Trip Price
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Trip Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.tripPriceDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                // Received
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Received",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.paidAmountDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }
                // Pending - highlighted
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.pendingDisplay,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = paymentStatusColor
                    )
                }
            }
        }
    }
}
