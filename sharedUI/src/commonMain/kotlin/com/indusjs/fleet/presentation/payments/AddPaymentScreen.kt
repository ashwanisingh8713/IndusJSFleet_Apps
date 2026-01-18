package com.indusjs.fleet.presentation.payments

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
import com.indusjs.fleet.domain.entity.payment.*
import indusjsfleet.sharedui.generated.resources.*
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
                    SelectedTripInfoCard(trip = trip)
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
                        isError = false
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
private fun SelectedTripInfoCard(trip: TripSummaryForPayment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
    onDateTimeChange: (String, String) -> Unit
) {
    FleetDateTimePicker(
        date = date,
        time = time,
        onDateTimeChange = onDateTimeChange,
        mode = PickerMode.DATE_TIME,
        label = "Payment Date & Time *",
        isError = error != null,
        errorMessage = error
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

@OptIn(ExperimentalMaterial3Api::class)
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

    val filteredTrips = remember(trips, searchQuery) {
        if (searchQuery.isBlank()) {
            trips
        } else {
            trips.filter {
                it.id.contains(searchQuery, ignoreCase = true) ||
                it.vehicleRegistration.contains(searchQuery, ignoreCase = true) ||
                it.startLocation.contains(searchQuery, ignoreCase = true) ||
                it.endLocation.contains(searchQuery, ignoreCase = true) ||
                (it.customerName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Trip",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearch,
                placeholder = { Text("Search by trip ID, vehicle, route...") },
                leadingIcon = { Icon(painter = painterResource(Res.drawable.ic_search), contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    Text(
                        text = if (searchQuery.isBlank()) "No trips available" else "No matching trips",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTrips) { trip ->
                        Card(
                            onClick = { onSelect(trip) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Trip #${trip.id} • ${trip.vehicleRegistration}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = trip.routeDisplay,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    trip.customerName?.let { customer ->
                                        Text(
                                            text = customer,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = trip.pendingDisplay,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (trip.hasPendingAmount) Color(0xFFE65100) else Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "pending",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
