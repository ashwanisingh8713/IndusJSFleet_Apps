package com.ijs.finance.presentation

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FinanceColors
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetFormSection
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.finance.domain.entity.PaymentType
import com.ijs.finance.presentation.VehicleFinanceContract.Effect
import com.ijs.finance.presentation.VehicleFinanceContract.Intent
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    var showVehicleDropdown by remember { mutableStateOf(false) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is Effect.PurchaseSaved -> onNavigateBack()
                is Effect.NavigateBack -> onNavigateBack()
                else -> { /* Ignore other effects */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.finance_add_purchase_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
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
                            Text(stringResource(Res.string.save))
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
                FleetFormSection(title = stringResource(Res.string.finance_section_vehicle_required)) {
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
                                placeholder = { Text(stringResource(Res.string.finance_placeholder_vehicle)) },
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
                                        text = { Text(stringResource(Res.string.finance_dropdown_all_have_purchase)) },
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
                                text = stringResource(Res.string.finance_all_vehicles_have_purchase),
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
                FleetFormSection(title = stringResource(Res.string.finance_section_purchase)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Purchase Date
                        FleetDateTimePicker(
                            date = state.formPurchaseDate,
                            time = "",
                            onDateTimeChange = { date, _ ->
                                viewModel.sendIntent(Intent.UpdatePurchaseDate(date))
                            },
                            mode = PickerMode.DATE_ONLY,
                            label = stringResource(Res.string.finance_label_purchase_date),
                            isError = state.purchaseDateError != null,
                            errorMessage = state.purchaseDateError?.resolve()
                        )

                        // Purchase Price
                        FleetInputField(
                            value = state.formPurchasePrice,
                            onValueChange = { viewModel.sendIntent(Intent.UpdatePurchasePrice(it)) },
                            fieldType = FieldType.NUMBER,
                            label = stringResource(Res.string.finance_label_purchase_price),
                            placeholder = stringResource(Res.string.finance_placeholder_enter_amount),
                            leadingIcon = { Text("₹") },
                            isError = state.purchasePriceError != null,
                            errorMessage = state.purchasePriceError?.resolve()
                        )

                        // Vendor Name
                        FleetInputField(
                            value = state.formVendorName,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateVendorName(it)) },
                            fieldType = FieldType.DEFAULT,
                            label = stringResource(Res.string.finance_label_vendor),
                            placeholder = stringResource(Res.string.finance_placeholder_vendor)
                        )

                        // Invoice Number
                        FleetInputField(
                            value = state.formInvoiceNumber,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateInvoiceNumber(it)) },
                            fieldType = FieldType.DEFAULT,
                            label = stringResource(Res.string.finance_label_invoice),
                            placeholder = stringResource(Res.string.finance_placeholder_invoice)
                        )
                    }
                }
            }

            // Section 3: Payment Type
            item {
                FleetFormSection(title = stringResource(Res.string.finance_section_payment_type)) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PaymentTypeOption(
                            icon = "💵",
                            title = stringResource(Res.string.finance_cash_title),
                            description = stringResource(Res.string.finance_cash_desc),
                            isSelected = state.formPaymentType == PaymentType.CASH,
                            color = CashGreen,
                            onClick = { viewModel.sendIntent(Intent.UpdatePaymentType(PaymentType.CASH)) }
                        )

                        PaymentTypeOption(
                            icon = "🏦",
                            title = stringResource(Res.string.finance_loan_title),
                            description = stringResource(Res.string.finance_loan_desc),
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
                    FleetFormSection(title = stringResource(Res.string.finance_section_loan)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Down Payment
                            FleetInputField(
                                value = state.formDownPayment,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateDownPayment(it)) },
                                fieldType = FieldType.NUMBER,
                                label = stringResource(Res.string.finance_label_down_payment),
                                placeholder = stringResource(Res.string.finance_placeholder_enter_amount),
                                leadingIcon = { Text("₹") },
                                isError = state.downPaymentError != null,
                                errorMessage = state.downPaymentError?.resolve()
                            )

                            // Loan Amount (auto-calculated)
                            OutlinedTextField(
                                value = formatCurrency(state.calculatedLoanAmount),
                                onValueChange = {},
                                label = { Text(stringResource(Res.string.finance_label_loan_amount)) },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            HorizontalDivider()

                            // Financier Name
                            FleetInputField(
                                value = state.formFinancierName,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateFinancierName(it)) },
                                fieldType = FieldType.DEFAULT,
                                label = stringResource(Res.string.finance_label_financier),
                                placeholder = stringResource(Res.string.finance_placeholder_financier),
                                isError = state.financierError != null,
                                errorMessage = state.financierError?.resolve()
                            )

                            // Loan Account Number
                            FleetInputField(
                                value = state.formLoanAccountNumber,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateLoanAccountNumber(it)) },
                                fieldType = FieldType.DEFAULT,
                                label = stringResource(Res.string.finance_label_loan_account),
                                placeholder = stringResource(Res.string.finance_placeholder_loan_account)
                            )

                            // Interest Rate and Tenure in row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FleetInputField(
                                    value = state.formInterestRate,
                                    onValueChange = { viewModel.sendIntent(Intent.UpdateInterestRate(it)) },
                                    fieldType = FieldType.DECIMAL,
                                    label = stringResource(Res.string.finance_label_rate),
                                    placeholder = stringResource(Res.string.finance_placeholder_rate),
                                    trailingIcon = { Text("%", style = MaterialTheme.typography.bodySmall) },
                                    isError = state.interestRateError != null,
                                    errorMessage = state.interestRateError?.resolve(),
                                    modifier = Modifier.weight(1f)
                                )

                                FleetInputField(
                                    value = state.formTenureMonths,
                                    onValueChange = { viewModel.sendIntent(Intent.UpdateTenureMonths(it)) },
                                    fieldType = FieldType.NUMBER,
                                    label = stringResource(Res.string.finance_label_tenure),
                                    placeholder = stringResource(Res.string.finance_placeholder_tenure),
                                    trailingIcon = { Text("mo", style = MaterialTheme.typography.bodySmall) },
                                    isError = state.tenureError != null,
                                    errorMessage = state.tenureError?.resolve(),
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
                                label = stringResource(Res.string.finance_label_emi_start_date),
                                isError = state.loanStartDateError != null,
                                errorMessage = state.loanStartDateError?.resolve()
                            )

                            HorizontalDivider()

                            // Bank Details
                            Text(
                                text = stringResource(Res.string.finance_bank_reference),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            FleetInputField(
                                value = state.formBankName,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateBankName(it)) },
                                fieldType = FieldType.DEFAULT,
                                label = stringResource(Res.string.finance_label_bank_name),
                                placeholder = stringResource(Res.string.finance_placeholder_bank_name)
                            )

                            FleetInputField(
                                value = state.formBankAccountNumber,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateBankAccountNumber(it)) },
                                fieldType = FieldType.DEFAULT,
                                label = stringResource(Res.string.finance_label_account_number),
                                placeholder = stringResource(Res.string.finance_placeholder_account_number)
                            )

                            FleetInputField(
                                value = state.formBankIfsc,
                                onValueChange = { viewModel.sendIntent(Intent.UpdateBankIfsc(it)) },
                                fieldType = FieldType.DEFAULT,
                                label = stringResource(Res.string.finance_label_ifsc),
                                placeholder = stringResource(Res.string.finance_placeholder_ifsc)
                            )

                            // Auto Debit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(Res.string.finance_label_auto_debit))
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
                FleetFormSection(title = stringResource(Res.string.finance_section_notes)) {
                    OutlinedTextField(
                        value = state.formNotes,
                        onValueChange = { viewModel.sendIntent(Intent.UpdateNotes(it)) },
                        placeholder = { Text(stringResource(Res.string.finance_placeholder_additional_notes)) },
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
                    Text(stringResource(Res.string.finance_save_purchase))
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
    FleetTitledSectionCard(
        title = stringResource(Res.string.finance_emi_calc_preview),
        emoji = "💡",
        accent = LoanBlue,
        containerColor = LoanBlue.copy(alpha = 0.1f)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(Res.string.finance_monthly_emi), style = MaterialTheme.typography.bodyMedium)
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
                Text(stringResource(Res.string.finance_total_interest), style = MaterialTheme.typography.bodyMedium)
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
                Text(stringResource(Res.string.finance_total_payable), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatCurrency(totalPayable),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
