package com.indusjs.fleet.presentation.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.core.ui.FinanceColors
import com.indusjs.fleet.core.ui.FleetSectionCard
import com.indusjs.fleet.core.ui.FleetTextField
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.finance.PaymentType
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Effect
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Intent
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

// Use FinanceColors from core.ui
private val LoanBlue = FinanceColors.LoanBlue
private val CashGreen = FinanceColors.CashGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseInfoScreen(
    viewModel: VehicleFinanceViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showVehicleDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is Effect.PurchaseSaved -> onNavigateBack()
                is Effect.NavigateBack -> onNavigateBack()
                else -> { /* Ignore other effects */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Purchase Information") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.sendIntent(Intent.SavePurchase) },
                        enabled = state.isFormValid && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Select Vehicle
            item {
                FleetSectionCard(title = "🚛 Select Vehicle *") {
                    Column {
                        ExposedDropdownMenuBox(
                            expanded = showVehicleDropdown,
                            onExpandedChange = { showVehicleDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = state.vehiclesWithoutPurchase
                                    .find { (it.id.toIntOrNull() ?: 0) == state.formVehicleId }
                                    ?.let { "${it.registrationNumber} - ${it.make} ${it.model}" }
                                    ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Select a vehicle") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showVehicleDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = showVehicleDropdown,
                                onDismissRequest = { showVehicleDropdown = false }
                            ) {
                                if (state.vehiclesWithoutPurchase.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("All vehicles have purchase info") },
                                        onClick = { showVehicleDropdown = false },
                                        enabled = false
                                    )
                                } else {
                                    state.vehiclesWithoutPurchase.forEach { vehicle ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        vehicle.registrationNumber,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        "${vehicle.make} ${vehicle.model}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.sendIntent(
                                                    Intent.SelectVehicleForPurchase(
                                                        vehicle.id.toIntOrNull() ?: 0
                                                    )
                                                )
                                                showVehicleDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (state.vehiclesWithoutPurchase.isEmpty()) {
                            Text(
                                text = "All vehicles already have purchase information",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Section 2: Purchase Details
            item {
                FleetSectionCard(title = "📝 Purchase Details") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Purchase Date
                        FleetDateTimePicker(
                            date = state.formPurchaseDate,
                            time = "",
                            onDateTimeChange = { date, _ ->
                                viewModel.sendIntent(Intent.UpdatePurchaseDate(date))
                            },
                            mode = PickerMode.DATE_ONLY,
                            label = "Purchase Date *",
                            isError = state.purchaseDateError != null,
                            errorMessage = state.purchaseDateError
                        )

                        // Purchase Price
                        FleetTextField(
                            value = state.formPurchasePrice,
                            onValueChange = { viewModel.sendIntent(Intent.UpdatePurchasePrice(it)) },
                            label = "Purchase Price *",
                            placeholder = "Enter amount",
                            leadingIcon = { Text("₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = state.purchasePriceError != null,
                            errorMessage = state.purchasePriceError
                        )

                        // Vendor Name
                        FleetTextField(
                            value = state.formVendorName,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateVendorName(it)) },
                            label = "Vendor/Dealer",
                            placeholder = "Dealer name"
                        )

                        // Invoice Number
                        FleetTextField(
                            value = state.formInvoiceNumber,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateInvoiceNumber(it)) },
                            label = "Invoice Number",
                            placeholder = "INV-XXXX"
                        )
                    }
                }
            }

            // Section 3: Payment Type
            item {
                FleetSectionCard(title = "💳 Payment Type *") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentTypeOption(
                            icon = "💵",
                            title = "Cash / Full Payment",
                            description = "Vehicle purchased with full payment",
                            isSelected = state.formPaymentType == PaymentType.CASH,
                            color = CashGreen,
                            onClick = { viewModel.sendIntent(Intent.UpdatePaymentType(PaymentType.CASH)) }
                        )

                        PaymentTypeOption(
                            icon = "🏦",
                            title = "Loan (EMI)",
                            description = "Financed with bank/NBFC",
                            isSelected = state.formPaymentType == PaymentType.LOAN,
                            color = LoanBlue,
                            onClick = { viewModel.sendIntent(Intent.UpdatePaymentType(PaymentType.LOAN)) }
                        )
                    }
                }
            }

            // Section 4: Loan Details (only if loan selected)
            if (state.formPaymentType == PaymentType.LOAN) {
                item {
                    FleetSectionCard(title = "🏦 Loan Details") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Down Payment
                            FleetTextField(
                                value = state.formDownPayment,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateDownPayment(it)) },
                                label = "Down Payment *",
                                placeholder = "Enter amount",
                                leadingIcon = { Text("₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = state.downPaymentError != null,
                                errorMessage = state.downPaymentError
                            )

                            // Loan Amount (auto-calculated)
                            OutlinedTextField(
                                value = formatCurrency(state.calculatedLoanAmount),
                                onValueChange = {},
                                label = { Text("Loan Amount (auto-calculated)") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            HorizontalDivider()

                            // Financier Name
                            FleetTextField(
                                value = state.formFinancierName,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateFinancierName(it)) },
                                label = "Financier/Bank *",
                                placeholder = "e.g., HDFC Bank",
                                isError = state.financierError != null,
                                errorMessage = state.financierError
                            )

                            // Loan Account Number
                            FleetTextField(
                                value = state.formLoanAccountNumber,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateLoanAccountNumber(it)) },
                                label = "Loan Account Number",
                                placeholder = "LOAN-XXXX"
                            )

                            // Interest Rate and Tenure in row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FleetTextField(
                                    value = state.formInterestRate,
                                    onValueChange = { viewModel.sendIntent(Intent.UpdateInterestRate(it)) },
                                    label = "Rate *",
                                    placeholder = "9.5",
                                    trailingIcon = { Text("%", style = MaterialTheme.typography.bodySmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    isError = state.interestRateError != null,
                                    errorMessage = state.interestRateError,
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                FleetTextField(
                                    value = state.formTenureMonths,
                                    onValueChange = { viewModel.sendIntent(Intent.UpdateTenureMonths(it)) },
                                    label = "Tenure *",
                                    placeholder = "48",
                                    trailingIcon = { Text("mo", style = MaterialTheme.typography.bodySmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = state.tenureError != null,
                                    errorMessage = state.tenureError,
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // EMI Calculation Preview
                            AnimatedVisibility(visible = state.calculatedEmi > 0) {
                                EmiCalculationPreview(
                                    emi = state.calculatedEmi,
                                    totalInterest = state.calculatedTotalInterest,
                                    totalPayable = state.calculatedTotalPayable
                                )
                            }

                            HorizontalDivider()

                            // EMI Start Date
                            FleetDateTimePicker(
                                date = state.formLoanStartDate,
                                time = "",
                                onDateTimeChange = { date, _ ->
                                    viewModel.sendIntent(Intent.UpdateLoanStartDate(date))
                                },
                                mode = PickerMode.DATE_ONLY,
                                label = "EMI Start Date *",
                                isError = state.loanStartDateError != null,
                                errorMessage = state.loanStartDateError
                            )

                            HorizontalDivider()

                            // Bank Details
                            Text(
                                text = "Bank Account (for payment reference)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FleetTextField(
                                value = state.formBankName,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateBankName(it)) },
                                label = "Bank Name",
                                placeholder = "Your bank name"
                            )

                            FleetTextField(
                                value = state.formBankAccountNumber,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateBankAccountNumber(it)) },
                                label = "Account Number",
                                placeholder = "Account number"
                            )

                            FleetTextField(
                                value = state.formBankIfsc,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateBankIfsc(it)) },
                                label = "IFSC Code",
                                placeholder = "HDFC0001234"
                            )

                            // Auto Debit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-debit enabled")
                                Switch(
                                    checked = state.formAutoDebitEnabled,
                                    onCheckedChange = { viewModel.sendIntent(Intent.UpdateAutoDebitEnabled(it)) }
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Notes
            item {
                FleetSectionCard(title = "📝 Notes") {
                    OutlinedTextField(
                        value = state.formNotes,
                        onValueChange = { viewModel.sendIntent(Intent.UpdateNotes(it)) },
                        placeholder = { Text("Additional notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Save Button
            item {
                Button(
                    onClick = { viewModel.sendIntent(Intent.SavePurchase) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.isFormValid && !state.isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Save Purchase Information")
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun PaymentTypeOption(
    icon: String,
    title: String,
    description: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        color = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color)
            )
        }
    }
}

@Composable
private fun EmiCalculationPreview(
    emi: Double,
    totalInterest: Double,
    totalPayable: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = LoanBlue.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💡", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EMI Calculation Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monthly EMI:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatCurrency(emi),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LoanBlue
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Interest:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatCurrency(totalInterest),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Payable:", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatCurrency(totalPayable),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
