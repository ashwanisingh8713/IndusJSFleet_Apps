package com.ijs.trip.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ijs.customer.presentation.toSelectableCustomer
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.customer.CustomerDetailsSection
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Cargo and customer editing section for trip edit mode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EditCargoCustomerSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    FleetTitledSectionCard(title = stringResource(Res.string.trip_edit_section_cargo_customer), emoji = "📦") {
            // Cargo Type Dropdown
            CargoTypeDropdown(state = state, viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.cargoDescription,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoDescription(it)) },
                label = { Text(stringResource(Res.string.trip_create_cargo_desc_label)) },
                leadingIcon = { Text("📝", modifier = Modifier.padding(start = 8.dp)) },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cargo Weight with Unit
            CargoWeightRow(state = state, viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            // Customer Selection
            CustomerDetailsSection(
                selectedCustomer = state.selectedCustomer?.toSelectableCustomer(),
                customerName = state.customerName,
                customerContact = state.customerContact,
                isRefreshing = state.isRefreshingCustomers,
                hasCustomers = state.customers.isNotEmpty(),
                validationError = null,
                onSelectClick = { viewModel.sendIntent(TripDetailContract.Intent.ToggleCustomerBottomSheet) },
                onClearClick = { viewModel.sendIntent(TripDetailContract.Intent.ClearCustomerSelection) },
                onRefreshClick = { viewModel.sendIntent(TripDetailContract.Intent.RefreshCustomers) },
                onAddNewClick = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddCustomer) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trip Price - Only visible to Owner and General Manager
            if (state.canViewTripPrice) {
                OutlinedTextField(
                    value = state.tripPrice,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateTripPrice(it)) },
                    label = { Text(stringResource(Res.string.trip_edit_label_trip_price)) },
                    leadingIcon = { Text("💰", modifier = Modifier.padding(start = 8.dp)) },
                    placeholder = { Text(stringResource(Res.string.trip_edit_enter_trip_price)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = stringResource(Res.string.trip_edit_trip_price_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Actual Price (Revenue) - editable, defaults to the quoted trip price.
                // Sent to the backend as selling_value (the actual amount the customer owes).
                OutlinedTextField(
                    value = state.actualPrice,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            viewModel.sendIntent(TripDetailContract.Intent.UpdateActualPrice(newValue))
                        }
                    },
                    label = { Text(stringResource(Res.string.trip_actual_price_revenue_label)) },
                    leadingIcon = { Text("₹", modifier = Modifier.padding(start = 8.dp)) },
                    placeholder = { Text(stringResource(Res.string.trip_edit_enter_trip_price)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = stringResource(Res.string.trip_actual_price_revenue_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Priority selector
            Text(stringResource(Res.string.trip_edit_priority), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.priorityOptions.forEach { priority ->
                    FilterChip(
                        selected = state.priority.equals(priority, ignoreCase = true),
                        onClick = { viewModel.sendIntent(TripDetailContract.Intent.UpdatePriority(priority)) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getPriorityIcon(priority))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(priority.replaceFirstChar { it.uppercaseChar() })
                            }
                        }
                    )
                }
            }
    }
}

/**
 * Cargo type dropdown menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CargoTypeDropdown(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    var showCargoTypeDropdown by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = showCargoTypeDropdown,
        onExpandedChange = { showCargoTypeDropdown = it }
    ) {
        OutlinedTextField(
            value = state.cargoType.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercaseChar() } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.trip_label_cargo_type)) },
            placeholder = { Text(stringResource(Res.string.trip_edit_select_cargo_type)) },
            leadingIcon = { Text("📦", modifier = Modifier.padding(start = 8.dp)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCargoTypeDropdown) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = showCargoTypeDropdown,
            onDismissRequest = { showCargoTypeDropdown = false }
        ) {
            state.cargoTypeOptions.forEach { cargoType ->
                DropdownMenuItem(
                    text = { Text(cargoType.replaceFirstChar { it.uppercaseChar() }) },
                    onClick = {
                        viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoType(cargoType))
                        showCargoTypeDropdown = false
                    },
                    leadingIcon = {
                        val icon = when (cargoType.lowercase()) {
                            "gitti" -> "🪨"
                            "balu" -> "🏖️"
                            "bhakshi" -> "🧱"
                            "enta" -> "🧱"
                            "hazardous" -> "⚠️"
                            "valuable" -> "💎"
                            else -> "📦"
                        }
                        Text(icon)
                    }
                )
            }
        }
    }
}

/**
 * Cargo weight input with unit dropdown.
 */
@Composable
private fun CargoWeightRow(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        OutlinedTextField(
            value = state.cargoWeight,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoWeight(newValue))
                }
            },
            label = { Text(stringResource(Res.string.trip_edit_cargo_weight)) },
            placeholder = { Text(stringResource(Res.string.trip_create_cargo_weight_placeholder)) },
            leadingIcon = { Text("⚖️", modifier = Modifier.padding(start = 8.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )

        FleetDropdown(
            label = stringResource(Res.string.trip_edit_unit),
            options = state.weightUnitOptions.map { DropdownOption(id = it, label = it) },
            selectedOptionId = state.weightUnit.takeIf { it.isNotBlank() },
            onOptionSelected = { viewModel.sendIntent(TripDetailContract.Intent.UpdateWeightUnit(it)) },
            placeholder = stringResource(Res.string.trip_edit_unit),
            modifier = Modifier.weight(0.6f)
        )
    }
}

/**
 * Notes editing section for trip edit mode.
 */
@Composable
internal fun EditNotesSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    FleetTitledSectionCard(title = stringResource(Res.string.trip_edit_section_notes), emoji = "📝") {
        OutlinedTextField(
            value = state.notes,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateNotes(it)) },
            label = { Text(stringResource(Res.string.trip_edit_additional_notes)) },
            placeholder = { Text(stringResource(Res.string.trip_edit_notes_placeholder)) },
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

