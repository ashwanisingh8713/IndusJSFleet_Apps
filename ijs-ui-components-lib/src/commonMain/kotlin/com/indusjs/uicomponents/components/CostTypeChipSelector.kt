package com.indusjs.uicomponents.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.data.model.costs.CostTypeGroupDto
import com.indusjs.fleet.data.model.costs.CostTypeItemDto
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_check
import org.jetbrains.compose.resources.painterResource

/**
 * Type alias for CostTypeGroupDto - used across UI layer for cost type groups.
 */
typealias CostTypeGroup = CostTypeGroupDto

/**
 * Type alias for CostTypeItemDto - used across UI layer for cost type items.
 */
typealias CostTypeItem = CostTypeItemDto

/**
 * Result of cost type selection containing all relevant data.
 */
data class CostTypeSelection(
    val costTypeId: String,
    val costTypeLabel: String,
    val groupId: String,
    val groupName: String
) {
    companion object {
        // Group IDs for reliable category detection
        const val FUEL_ENERGY_GROUP_ID = "TC-G-001"
        const val MISCELLANEOUS_GROUP_ID = "TC-G-006"
        const val OTHER_COST_TYPE_ID = "TC-006-004"
    }

    val isFuelCategory: Boolean get() = groupId == FUEL_ENERGY_GROUP_ID
    val isOtherCostType: Boolean get() = costTypeId == OTHER_COST_TYPE_ID
}


/**
 * Two-level cost type selector with full group information.
 * Uses CostTypeGroup which includes groupId for reliable category detection.
 *
 * @param groups List of cost type groups with groupId, groupName, and items
 * @param selectedCostType The currently selected cost type ID
 * @param onCostTypeSelected Callback when a cost type is selected with full selection data
 * @param onCategoryChanged Callback when category dropdown changes with groupId and groupName
 * @param modifier Modifier for the component
 * @param categoryLabel Label for the category dropdown
 * @param itemLabel Label for the item selection
 * @param isError Whether to show error styling
 * @param errorMessage Optional error message to display
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CostTypeTwoLevelSelector(
    groups: List<CostTypeGroup>,
    selectedCostType: String?,
    onCostTypeSelected: (CostTypeSelection) -> Unit,
    onCategoryChanged: ((groupId: String, groupName: String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    categoryLabel: String = "Cost Category",
    itemLabel: String = "Cost Type",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    // Find currently selected group
    val selectedGroup = remember(selectedCostType, groups) {
        groups.find { group -> group.items.any { it.id == selectedCostType } }
    }

    var currentGroup by remember(selectedGroup) {
        mutableStateOf(selectedGroup)
    }

    var isCategoryExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Category Dropdown
        Text(
            text = "$categoryLabel *",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isError && currentGroup == null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isCategoryExpanded,
            onExpandedChange = { isCategoryExpanded = it }
        ) {
            OutlinedTextField(
                value = currentGroup?.groupName ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = isCategoryExpanded,
                onDismissRequest = { isCategoryExpanded = false }
            ) {
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(group.groupName, style = MaterialTheme.typography.bodyMedium)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "${group.items.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            currentGroup = group
                            isCategoryExpanded = false
                            // Notify with groupId and groupName
                            onCategoryChanged?.invoke(group.groupId, group.groupName)
                        },
                        leadingIcon = if (group.groupId == currentGroup?.groupId) {
                            {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cost Type Chips - only show if category is selected
        AnimatedVisibility(
            visible = currentGroup != null && currentGroup!!.items.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Text(
                    text = "$itemLabel *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isError && selectedCostType == null)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentGroup?.items?.forEach { item ->
                            val isSelected = selectedCostType == item.id

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onCostTypeSelected(
                                        CostTypeSelection(
                                            costTypeId = item.id,
                                            costTypeLabel = item.label,
                                            groupId = currentGroup!!.groupId,
                                            groupName = currentGroup!!.groupName
                                        )
                                    )
                                },
                                label = {
                                    Text(item.label, style = MaterialTheme.typography.bodySmall)
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_check),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = if (isError && selectedCostType == null) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                } else {
                                    FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Hint when no category selected
        if (currentGroup == null) {
            Text(
                text = "Select a category to see available cost types",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

