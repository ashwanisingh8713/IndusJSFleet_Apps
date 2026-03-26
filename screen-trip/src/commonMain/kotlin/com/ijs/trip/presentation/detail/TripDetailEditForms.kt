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
import com.indusjs.uicomponents.customer.CustomerDetailsSection

/**
 * Cargo and customer editing section for trip edit mode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EditCargoCustomerSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📦", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Cargo & Customer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cargo Type Dropdown
            CargoTypeDropdown(state = state, viewModel = viewModel)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.cargoDescription,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoDescription(it)) },
                label = { Text("Cargo Description") },
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
                    label = { Text("Trip Price (Expected) *") },
                    leadingIcon = { Text("💰", modifier = Modifier.padding(start = 8.dp)) },
                    placeholder = { Text("Enter trip price") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = "Expected Cost + Profit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Priority selector
            Text("Priority", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
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
            label = { Text("Cargo Type") },
            placeholder = { Text("Select cargo type") },
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
@OptIn(ExperimentalMaterial3Api::class)
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
            label = { Text("Cargo Weight") },
            placeholder = { Text("e.g., 500") },
            leadingIcon = { Text("⚖️", modifier = Modifier.padding(start = 8.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )

        var showWeightUnitDropdown by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = showWeightUnitDropdown,
            onExpandedChange = { showWeightUnitDropdown = it },
            modifier = Modifier.weight(0.6f)
        ) {
            OutlinedTextField(
                value = state.weightUnit.ifBlank { "KG" },
                onValueChange = {},
                label = { Text("Unit") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showWeightUnitDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = showWeightUnitDropdown,
                onDismissRequest = { showWeightUnitDropdown = false }
            ) {
                state.weightUnitOptions.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit) },
                        onClick = {
                            viewModel.sendIntent(TripDetailContract.Intent.UpdateWeightUnit(unit))
                            showWeightUnitDropdown = false
                        }
                    )
                }
            }
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("📝", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateNotes(it)) },
                label = { Text("Additional Notes") },
                placeholder = { Text("Add any additional notes or instructions...") },
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

