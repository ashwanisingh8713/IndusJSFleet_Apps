package com.ijs.trip.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ijs.customer.presentation.toSelectableCustomer
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.customer.CustomerDetailsSection
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Cargo and customer editing section for trip edit mode.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditCargoCustomerSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    FleetTitledSectionCard(title = stringResource(Res.string.trip_edit_section_cargo_customer), emoji = "📦") {
            // Cargo Type Dropdown
            CargoTypeDropdown(state = state, viewModel = viewModel)

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            FleetInputField(
                value = state.cargoDescription,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoDescription(it)) },
                fieldType = FieldType.NOTES,
                label = stringResource(Res.string.trip_create_cargo_desc_label),
                leadingIcon = { Text("📝", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Cargo Weight with Unit
            CargoWeightRow(state = state, viewModel = viewModel)

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

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

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            // Trip Price - Only visible to Owner and General Manager
            if (state.canViewTripPrice) {
                // ₹ leading icon shared by the price inputs.
                val rupeeIcon: @Composable () -> Unit = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = FleetTokens.Spacing.M)
                    )
                }

                // Quoted/expected price (expected_trip_price) - optional, inline amount validation.
                val tripPriceError = if (state.tripPrice.isNotBlank()) {
                    ValidationUtils.validateAmount(state.tripPrice, required = false).errorMessage
                } else null
                FleetInputField(
                    value = state.tripPrice,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            viewModel.sendIntent(TripDetailContract.Intent.UpdateTripPrice(newValue))
                        }
                    },
                    fieldType = FieldType.DECIMAL,
                    label = stringResource(Res.string.trip_edit_label_trip_price),
                    placeholder = stringResource(Res.string.trip_edit_enter_trip_price),
                    leadingIcon = rupeeIcon,
                    isError = tripPriceError != null,
                    errorMessage = tripPriceError,
                    modifier = Modifier.fillMaxWidth()
                )
                // FleetInputField only shows supporting text on error, so the (non-error)
                // quote hint is rendered explicitly below the field.
                if (tripPriceError == null) {
                    Text(
                        text = stringResource(Res.string.trip_edit_trip_price_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = FleetTokens.Spacing.L, top = FleetTokens.Spacing.XS)
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                // Actual Price (Revenue) - editable, defaults to the quoted trip price.
                // Sent to the backend as selling_value (the actual amount the customer owes).
                val actualPriceError = if (state.actualPrice.isNotBlank()) {
                    ValidationUtils.validateAmount(state.actualPrice, required = false).errorMessage
                } else null
                FleetInputField(
                    value = state.actualPrice,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            viewModel.sendIntent(TripDetailContract.Intent.UpdateActualPrice(newValue))
                        }
                    },
                    fieldType = FieldType.DECIMAL,
                    label = stringResource(Res.string.trip_actual_price_revenue_label),
                    placeholder = stringResource(Res.string.trip_edit_enter_trip_price),
                    leadingIcon = rupeeIcon,
                    isError = actualPriceError != null,
                    errorMessage = actualPriceError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (actualPriceError == null) {
                    Text(
                        text = stringResource(Res.string.trip_actual_price_revenue_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = FleetTokens.Spacing.L, top = FleetTokens.Spacing.XS)
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                // Purchase Price (COGS) - editable cost of goods sold; sent as purchase_price.
                val purchasePriceError = if (state.purchasePrice.isNotBlank()) {
                    ValidationUtils.validateAmount(state.purchasePrice, required = false).errorMessage
                } else null
                FleetInputField(
                    value = state.purchasePrice,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            viewModel.sendIntent(TripDetailContract.Intent.UpdatePurchasePrice(newValue))
                        }
                    },
                    fieldType = FieldType.DECIMAL,
                    label = stringResource(Res.string.trip_purchase_price_cogs_label),
                    placeholder = stringResource(Res.string.trip_edit_enter_trip_price),
                    leadingIcon = rupeeIcon,
                    isError = purchasePriceError != null,
                    errorMessage = purchasePriceError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (purchasePriceError == null) {
                    Text(
                        text = stringResource(Res.string.trip_purchase_price_cogs_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = FleetTokens.Spacing.L, top = FleetTokens.Spacing.XS)
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            }

            // Priority selector
            Text(stringResource(Res.string.trip_edit_priority), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                state.priorityOptions.forEach { priority ->
                    FilterChip(
                        selected = state.priority.equals(priority, ignoreCase = true),
                        onClick = { viewModel.sendIntent(TripDetailContract.Intent.UpdatePriority(priority)) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getPriorityIcon(priority))
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
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
@Composable
private fun CargoTypeDropdown(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    FleetDropdown(
        label = stringResource(Res.string.trip_label_cargo_type),
        options = state.cargoTypeOptions.map { cargoType ->
            DropdownOption(
                id = cargoType,
                label = "${cargoTypeIcon(cargoType)} ${cargoType.replaceFirstChar { it.uppercaseChar() }}"
            )
        },
        selectedOptionId = state.cargoType.takeIf { it.isNotBlank() },
        onOptionSelected = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoType(it)) },
        placeholder = stringResource(Res.string.trip_edit_select_cargo_type),
        leadingIcon = { Text("📦", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Returns the emoji associated with a cargo type.
 */
private fun cargoTypeIcon(cargoType: String): String = when (cargoType.lowercase()) {
    "gitti" -> "🪨"
    "balu" -> "🏖️"
    "bhakshi" -> "🧱"
    "enta" -> "🧱"
    "hazardous" -> "⚠️"
    "valuable" -> "💎"
    else -> "📦"
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
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
        verticalAlignment = Alignment.Top
    ) {
        FleetInputField(
            value = state.cargoWeight,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoWeight(newValue))
                }
            },
            fieldType = FieldType.DECIMAL,
            label = stringResource(Res.string.trip_edit_cargo_weight),
            placeholder = stringResource(Res.string.trip_create_cargo_weight_placeholder),
            leadingIcon = { Text("⚖️", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
            modifier = Modifier.weight(1f)
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
        FleetInputField(
            value = state.notes,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateNotes(it)) },
            fieldType = FieldType.NOTES,
            label = stringResource(Res.string.trip_edit_additional_notes),
            placeholder = stringResource(Res.string.trip_edit_notes_placeholder),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

