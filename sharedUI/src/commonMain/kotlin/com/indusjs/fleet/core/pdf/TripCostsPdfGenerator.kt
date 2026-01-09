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
        .header .status-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            margin-top: 8px;
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
        .trip-info {
            background: #f5f5f5;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .trip-info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 10px;
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
        }
        .info-value {
            font-weight: 600;
            font-size: 13px;
        }
        .route-info {
            background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
        .route-row {
            display: flex;
            align-items: center;
            margin: 8px 0;
        }
        .route-icon {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 10px;
            margin-right: 10px;
        }
        .route-icon.start { background: #4CAF50; color: white; }
        .route-icon.end { background: #F44336; color: white; }
        .section-title {
            font-size: 16px;
            font-weight: 600;
            color: #1976D2;
            margin: 20px 0 10px 0;
            padding-bottom: 5px;
            border-bottom: 1px solid #e0e0e0;
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
            font-size: 28px;
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
            border-top: 1px solid #e0e0e0;
            text-align: center;
            color: #999;
            font-size: 10px;
        }
        .footer .company {
            font-weight: 600;
            color: #1976D2;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>Trip Costs Report</h1>
        <div class="subtitle">${pdfData.tripNumber ?: "Trip Report"}</div>
        ${pdfData.tripStatusLabel?.let { 
            "<div class=\"status-badge ${getStatusClass(pdfData.tripStatus)}\">$it</div>"
        } ?: ""}
    </div>

    <div class="trip-info">
        <div class="trip-info-grid">
            <div class="info-item">
                <span class="info-label">Vehicle</span>
                <span class="info-value">${pdfData.vehicleNumber ?: "N/A"}</span>
            </div>
            <div class="info-item">
                <span class="info-label">Driver</span>
                <span class="info-value">${pdfData.driverName ?: "N/A"}</span>
            </div>
            <div class="info-item">
                <span class="info-label">Scheduled Date</span>
                <span class="info-value">${pdfData.scheduledDate ?: "N/A"}</span>
            </div>
            <div class="info-item">
                <span class="info-label">Trip Status</span>
                <span class="info-value status-text ${getStatusClass(pdfData.tripStatus)}">${pdfData.tripStatusLabel ?: "N/A"}</span>
            </div>
            <div class="info-item full-width">
                <span class="info-label">Exported On</span>
                <span class="info-value">${pdfData.exportDate} at ${pdfData.exportTime}</span>
            </div>
        </div>
    </div>

    <div class="route-info">
        <div class="route-row">
            <div class="route-icon start">A</div>
            <span>${pdfData.startLocation ?: "N/A"}</span>
        </div>
        <div class="route-row">
            <div class="route-icon end">B</div>
            <span>${pdfData.endLocation ?: "N/A"}</span>
        </div>
    </div>

    <div class="summary-box">
        <div class="total-label">Total Cost</div>
        <div class="total-amount">₹${formatAmount(pdfData.totalCost)}</div>
        <div class="entry-count">${pdfData.costs.size} ${if (pdfData.costs.size == 1) "entry" else "entries"}</div>
    </div>

    <div class="section-title">Cost Breakdown by Type</div>
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

    <div class="section-title">Detailed Cost Entries</div>
    <table>
        <thead>
            <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Notes</th>
                <th style="text-align: right;">Amount</th>
            </tr>
        </thead>
        <tbody>
""")

            // Individual cost entries
            pdfData.costs.forEach { cost ->
                append("""
            <tr>
                <td>${formatCostDate(cost.date)}${cost.time?.let { " $it" } ?: ""}</td>
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
        <div>Generated on ${pdfData.exportDate} at ${pdfData.exportTime}</div>
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

