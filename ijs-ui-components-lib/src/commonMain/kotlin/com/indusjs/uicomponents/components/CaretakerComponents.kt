package com.indusjs.uicomponents.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.model.shared.CaretakerInfo

/**
 * Reusable Caretaker UI Components.
 *
 * Refactored to use [CaretakerInfo] (shared contract from ijs-core-lib)
 * instead of TeamMemberDto, enabling cross-module usage without feature dependencies.
 */

/**
 * Caretaker selection dropdown field.
 *
 * @param selectedCaretaker Currently selected caretaker
 * @param caretakers List of available caretakers
 * @param onCaretakerSelected Callback when caretaker is selected
 * @param isLoading Whether caretakers are being loaded
 * @param enabled Whether the field is enabled
 * @param label Label for the field
 * @param modifier Modifier for the field
 */
@Composable
fun CaretakerDropdownField(
    selectedCaretaker: CaretakerInfo?,
    caretakers: List<CaretakerInfo>,
    onCaretakerSelected: (CaretakerInfo?) -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    label: String = "Assign Caretaker",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled && !isLoading) { expanded = true },
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column {
                            Text(
                                text = selectedCaretaker?.name?.ifBlank { "Select a caretaker" }
                                    ?: "Select a caretaker",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedCaretaker != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            selectedCaretaker?.role?.let { role ->
                                Text(
                                    text = role.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedCaretaker != null) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onCaretakerSelected(null) }
                        )
                    }
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (selectedCaretaker != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Clear Selection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        onCaretakerSelected(null)
                        expanded = false
                    },
                    leadingIcon = {
                        Text(text = "✕", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                )
                HorizontalDivider()
            }

            if (caretakers.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "No team members available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { },
                    enabled = false
                )
            } else {
                caretakers.forEach { caretaker ->
                    val isSelected = selectedCaretaker?.id == caretaker.id

                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = caretaker.name.ifBlank { "Unknown" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = caretaker.role.replaceFirstChar { it.uppercase() }.ifBlank { "Team Member" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = {
                            onCaretakerSelected(caretaker)
                            expanded = false
                        },
                        leadingIcon = {
                            Text(
                                text = "👤",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}

/**
 * Caretaker section card for forms - compact and cohesive layout.
 */
@Composable
fun CaretakerSectionCard(
    selectedCaretaker: CaretakerInfo?,
    caretakers: List<CaretakerInfo>,
    onCaretakerSelected: (CaretakerInfo?) -> Unit,
    onRefresh: (() -> Unit)? = null,
    onCreateTeamMember: (() -> Unit)? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Caretaker Assignment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Optional • Assign a Manager or Supervisor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onRefresh != null && caretakers.isNotEmpty() && !isLoading) {
                    TextButton(
                        onClick = onRefresh,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Refresh", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                caretakers.isEmpty() -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "No team members available",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (onCreateTeamMember != null) {
                            TextButton(
                                onClick = onCreateTeamMember,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("+ Add", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
                else -> {
                    CaretakerDropdownField(
                        selectedCaretaker = selectedCaretaker,
                        caretakers = caretakers,
                        onCaretakerSelected = onCaretakerSelected,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

/**
 * Caretaker info display card (for detail screens).
 */
@Composable
fun CaretakerInfoCard(
    caretaker: CaretakerInfo?,
    onChangeCaretaker: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Column {
                    Text(
                        text = "Caretaker",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = caretaker?.name?.ifBlank { "Not Assigned" } ?: "Not Assigned",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    caretaker?.role?.let { role ->
                        Text(
                            text = role.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            onChangeCaretaker?.let {
                TextButton(onClick = it) {
                    Text(if (caretaker != null) "Change" else "Assign")
                }
            }
        }
    }
}

