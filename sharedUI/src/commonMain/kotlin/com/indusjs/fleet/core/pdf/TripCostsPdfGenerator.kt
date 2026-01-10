package com.indusjs.fleet.core.pdf

import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.presentation.trips.detail.TripDetailContract

/**
 * Interface for platform-specific PDF generation.
 * Implementations should be provided for each platform (Android, iOS).
 */
expect class TripCostsPdfGenerator() {
    /**
     * Generate and share a PDF document for trip costs.
     * @param pdfData The data to include in the PDF
     * @param onSuccess Callback when PDF is successfully generated and shared
     * @param onError Callback when an error occurs
     */
    fun generateAndSharePdf(
        pdfData: TripDetailContract.TripCostsPdfData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
}

/**
 * Generates HTML content for the trip costs PDF.
 * This is shared between platforms.
 */
object TripCostsPdfHtmlGenerator {

    fun generateHtml(pdfData: TripDetailContract.TripCostsPdfData): String {
        return buildString {
            append("""
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trip Costs Report</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
            font-size: 12px;
            line-height: 1.5;
            color: #333;
            padding: 20px;
            background: #fff;
        }
        .header {
            text-align: center;
            padding-bottom: 20px;
            border-bottom: 2px solid #1976D2;
            margin-bottom: 20px;
        }
        .header h1 {
            color: #1976D2;
            font-size: 24px;
            margin-bottom: 5px;
        }
        .header .subtitle {
            color: #666;
            font-size: 14px;
        }
        .header .vehicle-number {
            font-size: 18px;
            font-weight: 700;
            color: #333;
            margin-top: 8px;
        }
        .header .status-badge {
            display: inline-block;
            padding: 6px 16px;
            border-radius: 16px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            margin-top: 10px;
        }
        .status-badge.planned, .status-text.planned {
            background: #E3F2FD;
            color: #1565C0;
        }
        .status-badge.in_progress, .status-text.in_progress {
            background: #FFF3E0;
            color: #E65100;
        }
        .status-badge.completed, .status-text.completed {
            background: #E8F5E9;
            color: #2E7D32;
        }
        .status-badge.cancelled, .status-text.cancelled {
            background: #FFEBEE;
            color: #C62828;
        }
        .status-text {
            font-weight: 600;
        }
        .section-title {
            font-size: 14px;
            font-weight: 600;
            color: #1976D2;
            margin: 20px 0 10px 0;
            padding-bottom: 5px;
            border-bottom: 1px solid #e0e0e0;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .trip-info {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 15px;
        }
        .trip-info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 12px;
        }
        .info-item {
            display: flex;
            flex-direction: column;
        }
        .info-item.full-width {
            grid-column: 1 / -1;
        }
        .info-label {
            color: #666;
            font-size: 10px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 2px;
        }
        .info-value {
            font-weight: 600;
            font-size: 13px;
            color: #333;
        }
        .route-info {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 15px;
        }
        .route-row {
            display: flex;
            align-items: flex-start;
            margin: 10px 0;
        }
        .route-icon {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 11px;
            font-weight: 700;
            margin-right: 12px;
            flex-shrink: 0;
        }
        .route-icon.start { background: #4CAF50; color: white; }
        .route-icon.end { background: #F44336; color: white; }
        .route-text {
            flex: 1;
        }
        .route-label {
            font-size: 10px;
            color: #666;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .route-value {
            font-size: 13px;
            font-weight: 500;
            color: #333;
        }
        .route-arrow {
            text-align: center;
            color: #1976D2;
            font-size: 16px;
            margin: 5px 0;
            padding-left: 36px;
        }
        .schedule-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 15px;
        }
        .schedule-box {
            background: #fff;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            padding: 12px;
        }
        .schedule-box.departure {
            border-left: 4px solid #4CAF50;
        }
        .schedule-box.arrival {
            border-left: 4px solid #FF9800;
        }
        .schedule-title {
            font-size: 11px;
            font-weight: 600;
            color: #666;
            text-transform: uppercase;
            margin-bottom: 8px;
        }
        .schedule-date {
            font-size: 14px;
            font-weight: 600;
            color: #333;
        }
        .schedule-time {
            font-size: 12px;
            color: #666;
            margin-top: 2px;
        }
        .summary-box {
            background: linear-gradient(135deg, #1976D2 0%, #1565C0 100%);
            color: white;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            text-align: center;
        }
        .summary-box .total-label {
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: 1px;
            opacity: 0.9;
        }
        .summary-box .total-amount {
            font-size: 32px;
            font-weight: 700;
            margin-top: 5px;
        }
        .summary-box .entry-count {
            font-size: 11px;
            opacity: 0.8;
            margin-top: 5px;
        }
        .cost-type-summary {
            background: #f9f9f9;
            border-radius: 8px;
            overflow: hidden;
            margin-bottom: 20px;
        }
        .cost-type-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 15px;
            border-bottom: 1px solid #e0e0e0;
        }
        .cost-type-row:last-child {
            border-bottom: none;
        }
        .cost-type-name {
            font-weight: 500;
        }
        .cost-type-amount {
            font-weight: 600;
            color: #1976D2;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }
        th {
            background: #1976D2;
            color: white;
            padding: 10px;
            text-align: left;
            font-weight: 600;
            font-size: 11px;
            text-transform: uppercase;
        }
        td {
            padding: 10px;
            border-bottom: 1px solid #e0e0e0;
            font-size: 11px;
        }
        tr:nth-child(even) {
            background: #f9f9f9;
        }
        .amount-cell {
            font-weight: 600;
            color: #1976D2;
            text-align: right;
        }
        .footer {
            margin-top: 30px;
            padding-top: 15px;
            border-top: 2px solid #e0e0e0;
            text-align: center;
            color: #666;
            font-size: 10px;
        }
        .footer .company {
            font-weight: 700;
            color: #1976D2;
            font-size: 14px;
        }
        .footer .export-info {
            margin-top: 8px;
            padding: 8px;
            background: #f5f5f5;
            border-radius: 4px;
        }
        .footer .trip-id {
            margin-top: 5px;
            font-size: 9px;
            color: #999;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Trip Costs Report</h1>
        <div class="subtitle">${pdfData.tripNumber ?: "Trip Report"}</div>
        <div class="vehicle-number">🚚 ${pdfData.vehicleNumber ?: "N/A"}</div>
        ${pdfData.tripStatusLabel?.let { 
            "<div class=\"status-badge ${getStatusClass(pdfData.tripStatus)}\">$it</div>"
        } ?: ""}
    </div>

    <div class="section-title">📋 Trip Information</div>
    <div class="trip-info">
        <div class="trip-info-grid">
            <div class="info-item">
                <span class="info-label">🚚 Vehicle Registration</span>
                <span class="info-value">${pdfData.vehicleNumber ?: "N/A"}</span>
            </div>
            <div class="info-item">
                <span class="info-label">👤 Driver</span>
                <span class="info-value">${pdfData.driverName ?: "N/A"}</span>
            </div>
            <div class="info-item">
                <span class="info-label">📊 Trip Status</span>
                <span class="info-value status-text ${getStatusClass(pdfData.tripStatus)}">${pdfData.tripStatusLabel ?: "N/A"}</span>
            </div>
            ${pdfData.customerName?.let { """
            <div class="info-item">
                <span class="info-label">🏢 Customer</span>
                <span class="info-value">$it</span>
            </div>
            """ } ?: ""}
            ${pdfData.estimatedDistance?.let { """
            <div class="info-item">
                <span class="info-label">📏 Estimated Distance</span>
                <span class="info-value">$it km</span>
            </div>
            """ } ?: ""}
            ${pdfData.estimatedDuration?.let { """
            <div class="info-item">
                <span class="info-label">⏱️ Estimated Duration</span>
                <span class="info-value">$it</span>
            </div>
            """ } ?: ""}
        </div>
    </div>

    <div class="section-title">📍 Route</div>
    <div class="route-info">
        <div class="route-row">
            <div class="route-icon start">A</div>
            <div class="route-text">
                <div class="route-label">From</div>
                <div class="route-value">${pdfData.startLocation ?: "N/A"}</div>
            </div>
        </div>
        <div class="route-arrow">↓</div>
        <div class="route-row">
            <div class="route-icon end">B</div>
            <div class="route-text">
                <div class="route-label">To</div>
                <div class="route-value">${pdfData.endLocation ?: "N/A"}</div>
            </div>
        </div>
    </div>

    <div class="section-title">📅 Schedule</div>
    <div class="schedule-grid">
        <div class="schedule-box departure">
            <div class="schedule-title">🚀 Departure</div>
            <div class="schedule-date">${pdfData.departureDate ?: "N/A"}</div>
            <div class="schedule-time">${pdfData.departureTime?.let { "at $it" } ?: ""}</div>
        </div>
        <div class="schedule-box arrival">
            <div class="schedule-title">🏁 Arrival</div>
            <div class="schedule-date">${pdfData.arrivalDate ?: "N/A"}</div>
            <div class="schedule-time">${pdfData.arrivalTime?.let { "at $it" } ?: ""}</div>
        </div>
    </div>

    <div class="section-title">💰 Cost Summary</div>
    <div class="summary-box">
        <div class="total-label">Total Expenses</div>
        <div class="total-amount">₹${formatAmount(pdfData.totalCost)}</div>
        <div class="entry-count">${pdfData.costs.size} ${if (pdfData.costs.size == 1) "entry" else "entries"} • ${pdfData.costsByType.size} ${if (pdfData.costsByType.size == 1) "category" else "categories"}</div>
    </div>

    <div class="section-title">📊 Cost Breakdown by Type</div>
    <div class="cost-type-summary">
""")

            // Cost breakdown by type
            pdfData.costsByType.forEach { (type, amount) ->
                append("""
        <div class="cost-type-row">
            <span class="cost-type-name">${formatCostType(type)}</span>
            <span class="cost-type-amount">₹${formatAmount(amount)}</span>
        </div>
""")
            }

            append("""
    </div>

    <div class="section-title">📝 Detailed Cost Entries</div>
    <table>
        <thead>
            <tr>
                <th>Date & Time</th>
                <th>Type</th>
                <th>Description</th>
                <th style="text-align: right;">Amount</th>
            </tr>
        </thead>
        <tbody>
""")

            // Individual cost entries
            pdfData.costs.forEach { cost ->
                val dateTimeStr = buildString {
                    append(formatCostDate(cost.date))
                    cost.time?.takeIf { it.isNotBlank() }?.let {
                        append(" at ")
                        append(it)
                    }
                }
                append("""
            <tr>
                <td>$dateTimeStr</td>
                <td>${formatCostType(cost.costType)}</td>
                <td>${cost.notes ?: "-"}</td>
                <td class="amount-cell">₹${formatAmount(cost.amount)}</td>
            </tr>
""")
            }

            append("""
        </tbody>
    </table>

    <div class="footer">
        <div class="company">IndusJS Fleet</div>
        <div class="export-info">
            📄 Exported on <strong>${pdfData.exportDate}</strong> at <strong>${pdfData.exportTime}</strong>
        </div>
        <div class="trip-id">Trip ID: ${pdfData.tripId} | ${pdfData.vehicleNumber ?: "N/A"}</div>
    </div>
</body>
</html>
            """)
        }
    }

    private fun getStatusClass(status: String?): String {
        return when (status?.lowercase()) {
            "planned" -> "planned"
            "in_progress" -> "in_progress"
            "completed" -> "completed"
            "cancelled" -> "cancelled"
            else -> ""
        }
    }

    private fun formatAmount(amount: Double): String {
        val intPart = amount.toLong()
        val decPart = ((amount - intPart) * 100).toInt()
        val decStr = if (decPart < 10) "0$decPart" else "$decPart"
        return if (intPart >= 1000) {
            val formattedInt = intPart.toString().reversed().chunked(3).joinToString(",").reversed()
            "$formattedInt.$decStr"
        } else {
            "$intPart.$decStr"
        }
    }

    private fun formatCostType(costType: String): String {
        return costType.replace("_", " ").split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        }
    }

    private fun formatCostDate(dateString: String): String {
        return try {
            when {
                dateString.contains("T") -> {
                    val datePart = dateString.substringBefore("T")
                    val parts = datePart.split("-")
                    if (parts.size == 3) {
                        "${parts[2]}-${parts[1]}-${parts[0]}"
                    } else dateString
                }
                else -> dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
