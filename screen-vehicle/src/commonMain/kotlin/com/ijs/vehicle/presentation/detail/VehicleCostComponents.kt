package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompactCostCard(cost: CostDisplayItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (cost.category == "trip") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ) {
                Text(cost.icon, modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cost.typeLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = if (cost.category == "trip") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            if (cost.category == "trip") stringResource(Res.string.vehicle_costs_trip_label) else stringResource(Res.string.vehicle_costs_maint_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cost.category == "trip") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
                cost.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${formatAmount(cost.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Text("🗑️", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

internal data class CostDisplayItem(
    val id: String,
    val category: String,
    val costType: String,
    val amount: Double,
    /** UTC epoch millis (null/0 when unset). */
    val date: Long?,
    val dateLabel: String,
    val description: String?,
    val vendorName: String?,
    val icon: String,
    val typeLabel: String
) {
    companion object {
        fun fromTripCost(dto: com.indusjs.fleet.data.model.costs.TripCostDto): CostDisplayItem {
            return CostDisplayItem(
                id = dto.id.toString(),
                category = "trip",
                costType = dto.effectiveCostType,
                amount = dto.amount,
                date = dto.date,
                dateLabel = formatCostDateLabel(dto.date),
                description = dto.notes,
                vendorName = null,
                icon = getCostTypeIcon(dto.effectiveCostType),
                typeLabel = dto.displayLabel
            )
        }

        fun fromMaintenanceCost(dto: com.indusjs.fleet.data.model.costs.MaintenanceCostDto): CostDisplayItem {
            return CostDisplayItem(
                id = dto.id.toString(),
                category = "maintenance",
                costType = dto.effectiveCostType,
                amount = dto.amount,
                date = dto.date,
                dateLabel = formatCostDateLabel(dto.date),
                description = dto.description,
                vendorName = dto.vendorName,
                icon = getCostTypeIcon(dto.effectiveCostType),
                typeLabel = dto.displayLabel
            )
        }

        /** Format a cost date (UTC epoch millis) as a "DD-MMM-YYYY" label. */
        internal fun formatCostDateLabel(date: Long?): String {
            if (date == null || date <= 0L) return "Unknown"
            return com.indusjs.fleet.core.util.formatDateToHumanReadable(date)
                .ifBlank { "Unknown" }
        }
    }
}

internal fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        val intAmount = amount.toLong()
        val formatted = StringBuilder()
        val str = intAmount.toString()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                formatted.insert(0, ',')
            }
            formatted.insert(0, str[i])
            count++
        }
        formatted.toString()
    } else {
        val intPart = amount.toLong()
        val decPart = ((amount - intPart) * 100).toInt()
        "$intPart.${decPart.toString().padStart(2, '0')}"
    }
}

internal fun getCostTypeIcon(type: String): String = when (type.lowercase()) {
    "fuel" -> "⛽"
    "toll" -> "🛣️"
    "tyre" -> "🛞"
    "battery" -> "🔋"
    "oil_change" -> "🛢️"
    "brake", "brake_service" -> "🛑"
    "engine", "engine_repair" -> "🔧"
    "driver_allowance" -> "👤"
    "loading" -> "📦"
    "unloading" -> "📤"
    "parking" -> "🅿️"
    "cleaning" -> "🧹"
    "servicing" -> "🔩"
    "electrical" -> "⚡"
    "body_work" -> "🚗"
    "rto" -> "📋"
    "police" -> "🚔"
    "food" -> "🍽️"
    "halt" -> "⏸️"
    "commission" -> "💵"
    "weighing" -> "⚖️"
    "detention" -> "⏰"
    else -> "💰"
}

internal fun getCostTypeLabel(type: String): String = when (type.lowercase()) {
    "all" -> "All"
    // Trip Cost Types
    "fuel" -> "Fuel"
    "toll" -> "Toll"
    "driver_allowance" -> "Driver Allowance"
    "parking" -> "Parking"
    "loading_charges" -> "Loading"
    "unloading_charges" -> "Unloading"
    "insurance" -> "Insurance"
    "permit" -> "Permit"
    "registration_renewal" -> "Registration"
    "fitness_check" -> "Fitness Check"
    "emission_test" -> "Emission Test"
    "state_permit" -> "State Permit"
    "national_permit" -> "National Permit"
    "chalan" -> "Chalan/Fine"
    // Maintenance Cost Types
    "tyre" -> "Tyre"
    "battery" -> "Battery"
    "oil_change" -> "Oil Change"
    "brake_service" -> "Brake"
    "engine_repair" -> "Engine"
    "clutch_repair" -> "Clutch"
    "suspension" -> "Suspension"
    "electrical" -> "Electrical"
    "body_work" -> "Body Work"
    "cleaning" -> "Cleaning"
    "servicing" -> "Servicing"
    // Legacy/Other
    "brake" -> "Brake"
    "engine" -> "Engine"
    "loading" -> "Loading"
    "unloading" -> "Unloading"
    "rto" -> "RTO"
    "police" -> "Police"
    "food" -> "Food"
    "halt" -> "Halt"
    "commission" -> "Commission"
    "weighing" -> "Weighing"
    "detention" -> "Detention"
    "other", "miscellaneous" -> "Other"
    else -> type.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
}



