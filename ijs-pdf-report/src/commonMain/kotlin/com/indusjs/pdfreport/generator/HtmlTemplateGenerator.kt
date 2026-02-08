package com.indusjs.pdfreport.generator

import com.indusjs.pdfreport.model.*

/**
 * Generates HTML content for PDF reports.
 * Shared across all platforms.
 */
object HtmlTemplateGenerator {

    /**
     * Generate HTML for any report type.
     */
    fun generateHtml(data: PdfReportData): String {
        return when (data) {
            is TripCostsPdfData -> generateTripCostsHtml(data)
            is DriverCostsPdfData -> generateDriverCostsHtml(data)
            is CustomerTripsPdfData -> generateCustomerTripsHtml(data)
            is CustomerPaymentsPdfData -> generateCustomerPaymentsHtml(data)
            is PaymentsListPdfData -> generatePaymentsListHtml(data)
            is PaymentReceiptPdfData -> generatePaymentReceiptHtml(data)
            is VehicleFinancePdfData -> generateVehicleFinanceHtml(data)
            is FleetProfitLossPdfData -> generateFleetProfitLossHtml(data)
            is VehicleProfitLossPdfData -> generateVehicleProfitLossHtml(data)
            is CostAnalysisPdfData -> generateCostAnalysisHtml(data)
            is CustomerFinancialsPdfData -> generateCustomerFinancialsHtml(data)
            is VehicleMaintenanceCostsPdfData -> generateVehicleMaintenanceCostsHtml(data)
        }
    }

    /**
     * Generate HTML for Trip Costs report.
     */
    fun generateTripCostsHtml(data: TripCostsPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Header
            append(generateHeader(data.title, data.tripNumber))

            // Trip Info Section
            append("""
                <div class="section">
                    <div class="section-title">Trip Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Vehicle</span>
                            <span class="value">${data.vehicleNumber}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Driver</span>
                            <span class="value">${data.driverName ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Status</span>
                            <span class="value status-${data.tripStatus.lowercase()}">${formatStatus(data.tripStatus)}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Departure</span>
                            <span class="value">${data.departureDate}</span>
                        </div>
                    </div>
                    <div class="route-info">
                        <div class="route-point">
                            <span class="route-label">From</span>
                            <span class="route-value">${data.startLocation}</span>
                        </div>
                        <div class="route-arrow">→</div>
                        <div class="route-point">
                            <span class="route-label">To</span>
                            <span class="route-value">${data.endLocation}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Cost Summary Section
            append("""
                <div class="section">
                    <div class="section-title">Cost Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Cost</span>
                            <span class="summary-value primary">₹${formatAmount(data.totalCost)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Fuel Cost</span>
                            <span class="summary-value">₹${formatAmount(data.fuelCost)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Toll Cost</span>
                            <span class="summary-value">₹${formatAmount(data.tollCost)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Other Cost</span>
                            <span class="summary-value">₹${formatAmount(data.otherCost)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Cost Details Table
            if (data.costs.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Cost Details</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Cost Type</th>
                                    <th>Date</th>
                                    <th>Amount</th>
                                    <th>Notes</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.costs.forEach { cost ->
                    append("""
                        <tr>
                            <td>${cost.costLabel}</td>
                            <td>${cost.date}${cost.time?.let { " $it" } ?: ""}</td>
                            <td class="amount">₹${formatAmount(cost.amount)}</td>
                            <td>${cost.notes ?: "-"}</td>
                        </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="2"><strong>Total</strong></td>
                                    <td class="amount"><strong>₹${formatAmount(data.totalCost)}</strong></td>
                                    <td></td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Footer
            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Driver Costs report.
     */
    fun generateDriverCostsHtml(data: DriverCostsPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Header
            append(generateHeader(data.title, data.driverName))

            // Driver Info Section
            append("""
                <div class="section">
                    <div class="section-title">Driver Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Driver Name</span>
                            <span class="value">${data.driverName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Mobile</span>
                            <span class="value">${data.mobile ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">License</span>
                            <span class="value">${data.licenseNumber ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Period</span>
                            <span class="value">${data.period}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Summary Section
            append("""
                <div class="section">
                    <div class="section-title">Cost Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Earnings</span>
                            <span class="summary-value success">₹${formatAmount(data.totalEarnings)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Deductions</span>
                            <span class="summary-value danger">₹${formatAmount(data.totalDeductions)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Net Amount</span>
                            <span class="summary-value primary">₹${formatAmount(data.netAmount)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Entries</span>
                            <span class="summary-value">${data.entryCount}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Cost Details by Group
            if (data.costs.isNotEmpty()) {
                // Costs by Group
                data.costsByGroup.forEach { (groupName, groupCosts) ->
                    val groupTotal = groupCosts.sumOf { it.amount }
                    val isDeductionGroup = groupCosts.any { it.isDeduction }

                    append("""
                        <div class="section">
                            <div class="section-title">$groupName (${groupCosts.size} entries)</div>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Cost Type</th>
                                        <th>Date</th>
                                        <th>Trip</th>
                                        <th>Description</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                    """.trimIndent())

                    groupCosts.forEach { cost ->
                        val amountClass = if (cost.isDeduction) "amount danger" else "amount success"
                        val amountPrefix = if (cost.isDeduction) "- " else "+ "
                        append("""
                            <tr>
                                <td>${cost.costLabel}</td>
                                <td>${cost.date}</td>
                                <td>${cost.tripId?.let { "Trip #$it" } ?: "-"}</td>
                                <td>${cost.description ?: "-"}</td>
                                <td class="$amountClass">$amountPrefix₹${formatAmount(cost.amount)}</td>
                            </tr>
                        """.trimIndent())
                    }

                    val totalClass = if (isDeductionGroup) "amount danger" else "amount success"
                    val totalPrefix = if (isDeductionGroup) "- " else ""
                    append("""
                                </tbody>
                                <tfoot>
                                    <tr>
                                        <td colspan="4"><strong>Group Total</strong></td>
                                        <td class="$totalClass"><strong>$totalPrefix₹${formatAmount(groupTotal)}</strong></td>
                                    </tr>
                                </tfoot>
                            </table>
                        </div>
                    """.trimIndent())
                }

                // Grand Total Summary
                append("""
                    <div class="section">
                        <div class="section-title">Grand Total</div>
                        <table class="data-table">
                            <tbody>
                                <tr>
                                    <td>Total Earnings</td>
                                    <td class="amount success">+ ₹${formatAmount(data.totalEarnings)}</td>
                                </tr>
                                <tr>
                                    <td>Total Deductions</td>
                                    <td class="amount danger">- ₹${formatAmount(data.totalDeductions)}</td>
                                </tr>
                                <tr class="highlight">
                                    <td><strong>Net Amount</strong></td>
                                    <td class="amount primary"><strong>₹${formatAmount(data.netAmount)}</strong></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Footer
            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Customer Trips report.
     */
    fun generateCustomerTripsHtml(data: CustomerTripsPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Header
            append(generateHeader(data.title, data.customerName))

            // Customer Info Section
            append("""
                <div class="section">
                    <div class="section-title">Customer Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Name</span>
                            <span class="value">${data.customerName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Company</span>
                            <span class="value">${data.companyName ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Contact</span>
                            <span class="value">${data.contactNumber ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Period</span>
                            <span class="value">${data.dateRange ?: "All Time"}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Summary Section
            append("""
                <div class="section">
                    <div class="section-title">Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Trips</span>
                            <span class="summary-value">${data.totalTrips}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Revenue</span>
                            <span class="summary-value primary">₹${formatAmount(data.totalRevenue)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Received</span>
                            <span class="summary-value success">₹${formatAmount(data.totalPaid)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Pending</span>
                            <span class="summary-value warning">₹${formatAmount(data.totalPending)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Trips Table
            if (data.trips.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Trip Details</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Trip ID</th>
                                    <th>Vehicle</th>
                                    <th>Route</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Price</th>
                                    <th>Paid</th>
                                    <th>Pending</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.trips.forEach { trip ->
                    append("""
                        <tr>
                            <td>#${trip.tripId}</td>
                            <td>${trip.vehicleNumber}</td>
                            <td>${trip.startLocation} → ${trip.endLocation}</td>
                            <td>${trip.startDate}</td>
                            <td class="status-${trip.tripStatus.lowercase()}">${formatStatus(trip.tripStatus)}</td>
                            <td class="amount">₹${formatAmount(trip.tripPrice)}</td>
                            <td class="amount success">₹${formatAmount(trip.paidAmount)}</td>
                            <td class="amount warning">₹${formatAmount(trip.pendingAmount)}</td>
                        </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="5"><strong>Total</strong></td>
                                    <td class="amount"><strong>₹${formatAmount(data.totalRevenue)}</strong></td>
                                    <td class="amount success"><strong>₹${formatAmount(data.totalPaid)}</strong></td>
                                    <td class="amount warning"><strong>₹${formatAmount(data.totalPending)}</strong></td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Footer
            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Customer Payments report.
     */
    fun generateCustomerPaymentsHtml(data: CustomerPaymentsPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Header
            append(generateHeader(data.title, data.customerName))

            // Customer Info Section
            append("""
                <div class="section">
                    <div class="section-title">Customer Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Name</span>
                            <span class="value">${data.customerName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Company</span>
                            <span class="value">${data.companyName ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Contact</span>
                            <span class="value">${data.contactNumber ?: "N/A"}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Period</span>
                            <span class="value">${data.dateRange ?: "All Time"}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Summary Section
            append("""
                <div class="section">
                    <div class="section-title">Payment Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Payments</span>
                            <span class="summary-value">${data.totalPayments}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Amount</span>
                            <span class="summary-value primary">₹${formatAmount(data.totalAmount)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Payments Table
            if (data.payments.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Payment Details</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Receipt #</th>
                                    <th>Trip</th>
                                    <th>Vehicle</th>
                                    <th>Date</th>
                                    <th>Type</th>
                                    <th>Mode</th>
                                    <th>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.payments.forEach { payment ->
                    append("""
                        <tr>
                            <td>${payment.receiptNumber ?: "-"}</td>
                            <td>#${payment.tripId}</td>
                            <td>${payment.vehicleNumber}</td>
                            <td>${payment.paymentDate}</td>
                            <td>${formatPaymentType(payment.paymentType)}</td>
                            <td>${formatPaymentMode(payment.paymentMode)}</td>
                            <td class="amount">₹${formatAmount(payment.amount)}</td>
                        </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="6"><strong>Total</strong></td>
                                    <td class="amount"><strong>₹${formatAmount(data.totalAmount)}</strong></td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Footer
            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Payments List report.
     */
    fun generatePaymentsListHtml(data: PaymentsListPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Header
            append(generateHeader(data.title, data.dateRange ?: "All Payments"))

            // Summary Section
            append("""
                <div class="section">
                    <div class="section-title">Payment Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Payments</span>
                            <span class="summary-value">${data.totalPayments}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Received</span>
                            <span class="summary-value success">₹${formatAmount(data.totalReceived)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Pending</span>
                            <span class="summary-value warning">₹${formatAmount(data.totalPending)}</span>
                        </div>
                    </div>
                    ${data.filterInfo?.let { "<p class='filter-info'>Filters: $it</p>" } ?: ""}
                </div>
            """.trimIndent())

            // Payments Table
            if (data.payments.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Payment Details</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Receipt #</th>
                                    <th>Customer</th>
                                    <th>Vehicle</th>
                                    <th>Route</th>
                                    <th>Date</th>
                                    <th>Type</th>
                                    <th>Status</th>
                                    <th>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.payments.forEach { payment ->
                    append("""
                        <tr>
                            <td>${payment.receiptNumber ?: "-"}</td>
                            <td>${payment.customerName}</td>
                            <td>${payment.vehicleNumber}</td>
                            <td>${payment.startLocation ?: ""} → ${payment.endLocation ?: ""}</td>
                            <td>${payment.paymentDate}</td>
                            <td>${formatPaymentType(payment.paymentType)}</td>
                            <td class="status-${payment.paymentStatus.lowercase()}">${formatStatus(payment.paymentStatus)}</td>
                            <td class="amount">₹${formatAmount(payment.amount)}</td>
                        </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td colspan="7"><strong>Total</strong></td>
                                    <td class="amount"><strong>₹${formatAmount(data.totalReceived)}</strong></td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Footer
            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Payment Receipt.
     */
    fun generatePaymentReceiptHtml(data: PaymentReceiptPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Receipt Header
            append("""
                <div class="receipt-header">
                    <div class="receipt-title">PAYMENT RECEIPT</div>
                    <div class="receipt-number">${data.receiptNumber}</div>
                    <div class="receipt-date">${data.paymentDate}</div>
                </div>
            """.trimIndent())

            // Customer & Trip Info
            append("""
                <div class="section">
                    <div class="two-column">
                        <div class="column">
                            <div class="section-title">Customer Details</div>
                            <div class="detail-row">
                                <span class="label">Name:</span>
                                <span class="value">${data.customerName}</span>
                            </div>
                            ${data.customerCompany?.let { """
                            <div class="detail-row">
                                <span class="label">Company:</span>
                                <span class="value">$it</span>
                            </div>
                            """ } ?: ""}
                            ${data.customerContact?.let { """
                            <div class="detail-row">
                                <span class="label">Contact:</span>
                                <span class="value">$it</span>
                            </div>
                            """ } ?: ""}
                        </div>
                        <div class="column">
                            <div class="section-title">Trip Details</div>
                            <div class="detail-row">
                                <span class="label">Trip ID:</span>
                                <span class="value">#${data.tripId}</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Vehicle:</span>
                                <span class="value">${data.vehicleNumber}</span>
                            </div>
                            ${data.driverName?.let { """
                            <div class="detail-row">
                                <span class="label">Driver:</span>
                                <span class="value">$it</span>
                            </div>
                            """ } ?: ""}
                            <div class="detail-row">
                                <span class="label">Route:</span>
                                <span class="value">${data.startLocation} → ${data.endLocation}</span>
                            </div>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Payment Details
            append("""
                <div class="section">
                    <div class="section-title">Payment Details</div>
                    <table class="receipt-table">
                        <tr>
                            <td class="label">Payment Type</td>
                            <td class="value">${formatPaymentType(data.paymentType)}</td>
                        </tr>
                        <tr>
                            <td class="label">Payment Mode</td>
                            <td class="value">${formatPaymentMode(data.paymentMode)}</td>
                        </tr>
                        <tr>
                            <td class="label">Payment Status</td>
                            <td class="value status-${data.paymentStatus.lowercase()}">${formatStatus(data.paymentStatus)}</td>
                        </tr>
                        ${data.transactionId?.let { """
                        <tr>
                            <td class="label">Transaction ID</td>
                            <td class="value">$it</td>
                        </tr>
                        """ } ?: ""}
                        ${data.bankName?.let { """
                        <tr>
                            <td class="label">Bank</td>
                            <td class="value">$it</td>
                        </tr>
                        """ } ?: ""}
                    </table>
                </div>
            """.trimIndent())

            // Amount Section
            append("""
                <div class="section amount-section">
                    <div class="section-title">Amount Details</div>
                    <table class="amount-table">
                        <tr>
                            <td>Amount</td>
                            <td class="amount">₹${formatAmount(data.amount)}</td>
                        </tr>
                        ${if (data.tdsAmount > 0) """
                        <tr>
                            <td>TDS Deduction</td>
                            <td class="amount">- ₹${formatAmount(data.tdsAmount)}</td>
                        </tr>
                        """ else ""}
                        ${if (data.discountAmount > 0) """
                        <tr>
                            <td>Discount</td>
                            <td class="amount">- ₹${formatAmount(data.discountAmount)}</td>
                        </tr>
                        """ else ""}
                        <tr class="total-row">
                            <td><strong>Net Amount</strong></td>
                            <td class="amount"><strong>₹${formatAmount(data.netAmount)}</strong></td>
                        </tr>
                    </table>
                </div>
            """.trimIndent())

            // Notes
            data.notes?.let {
                append("""
                    <div class="section">
                        <div class="section-title">Notes</div>
                        <p class="notes-text">$it</p>
                    </div>
                """.trimIndent())
            }

            // Footer
            append("""
                <div class="receipt-footer">
                    <p>Generated by: ${data.createdBy ?: "System"}</p>
                    <p>Generated on: ${data.generatedAt}</p>
                    <p class="disclaimer">This is a computer-generated receipt and does not require a signature.</p>
                </div>
            """.trimIndent())
        })
    }

    // ══════════════════════════════════════════════════════════════════════════════
    // PRIVATE HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════════

    private fun wrapInDocument(title: String, content: String): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <style>
        ${getCommonStyles()}
    </style>
</head>
<body>
    <div class="container">
        $content
    </div>
</body>
</html>
        """.trimIndent()
    }

    private fun generateHeader(title: String, subtitle: String): String {
        return """
            <div class="header">
                <div class="logo">IndusJS Fleet</div>
                <h1>$title</h1>
                <p class="subtitle">$subtitle</p>
            </div>
        """.trimIndent()
    }

    private fun generateFooter(generatedAt: String): String {
        return """
            <div class="footer">
                <p>Generated on: $generatedAt</p>
                <p class="powered-by">Powered by IndusJS Fleet Management</p>
            </div>
        """.trimIndent()
    }

    private fun getCommonStyles(): String {
        return """
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
                background: #fff;
            }
            .container {
                max-width: 800px;
                margin: 0 auto;
                padding: 20px;
            }
            .header {
                text-align: center;
                padding-bottom: 20px;
                border-bottom: 2px solid #1976D2;
                margin-bottom: 20px;
            }
            .header .logo {
                font-size: 14px;
                color: #1976D2;
                font-weight: 600;
                margin-bottom: 10px;
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
            .section {
                margin-bottom: 20px;
                padding: 15px;
                background: #f9f9f9;
                border-radius: 8px;
            }
            .section-title {
                font-size: 14px;
                font-weight: 600;
                color: #1976D2;
                margin-bottom: 12px;
                padding-bottom: 8px;
                border-bottom: 1px solid #e0e0e0;
            }
            .info-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 15px;
            }
            .info-item {
                display: flex;
                flex-direction: column;
            }
            .info-item .label {
                font-size: 10px;
                color: #888;
                text-transform: uppercase;
            }
            .info-item .value {
                font-size: 12px;
                font-weight: 600;
                color: #333;
            }
            .summary-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 15px;
            }
            .summary-card {
                text-align: center;
                padding: 12px;
                background: #fff;
                border-radius: 6px;
                border: 1px solid #e0e0e0;
            }
            .summary-label {
                display: block;
                font-size: 10px;
                color: #888;
                text-transform: uppercase;
                margin-bottom: 4px;
            }
            .summary-value {
                display: block;
                font-size: 16px;
                font-weight: 700;
            }
            .summary-value.primary { color: #1976D2; }
            .summary-value.success { color: #2E7D32; }
            .summary-value.warning { color: #F57C00; }
            .data-table {
                width: 100%;
                border-collapse: collapse;
                font-size: 11px;
            }
            .data-table th, .data-table td {
                padding: 8px 10px;
                text-align: left;
                border-bottom: 1px solid #e0e0e0;
            }
            .data-table th {
                background: #1976D2;
                color: #fff;
                font-weight: 600;
                text-transform: uppercase;
                font-size: 10px;
            }
            .data-table tbody tr:nth-child(even) {
                background: #f5f5f5;
            }
            .data-table tfoot td {
                background: #e3f2fd;
                font-weight: 600;
            }
            .data-table .amount {
                text-align: right;
                font-family: monospace;
            }
            .route-info {
                display: flex;
                align-items: center;
                gap: 15px;
                margin-top: 12px;
                padding: 10px;
                background: #fff;
                border-radius: 6px;
            }
            .route-point {
                flex: 1;
            }
            .route-label {
                font-size: 10px;
                color: #888;
                display: block;
            }
            .route-value {
                font-size: 12px;
                font-weight: 500;
            }
            .route-arrow {
                font-size: 18px;
                color: #1976D2;
            }
            .status-planned { color: #1565C0; }
            .status-on_route, .status-in_progress { color: #E65100; }
            .status-completed { color: #2E7D32; }
            .status-cancelled, .status-failed { color: #C62828; }
            .status-received { color: #2E7D32; }
            .status-pending { color: #F57C00; }
            .status-partial { color: #1976D2; }
            .receipt-header {
                text-align: center;
                padding: 20px;
                background: #1976D2;
                color: #fff;
                border-radius: 8px;
                margin-bottom: 20px;
            }
            .receipt-title {
                font-size: 20px;
                font-weight: 700;
                margin-bottom: 8px;
            }
            .receipt-number {
                font-size: 16px;
                font-weight: 600;
            }
            .receipt-date {
                font-size: 12px;
                opacity: 0.9;
            }
            .two-column {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 20px;
            }
            .detail-row {
                display: flex;
                margin-bottom: 6px;
            }
            .detail-row .label {
                width: 100px;
                color: #888;
            }
            .detail-row .value {
                flex: 1;
                font-weight: 500;
            }
            .receipt-table, .amount-table {
                width: 100%;
            }
            .receipt-table td, .amount-table td {
                padding: 8px 0;
                border-bottom: 1px solid #e0e0e0;
            }
            .amount-table .total-row td {
                border-top: 2px solid #1976D2;
                padding-top: 12px;
                font-size: 14px;
            }
            .amount-section {
                background: #e3f2fd;
            }
            .notes-text {
                color: #666;
                font-style: italic;
            }
            .receipt-footer {
                text-align: center;
                padding: 20px;
                margin-top: 20px;
                border-top: 1px solid #e0e0e0;
                color: #888;
                font-size: 10px;
            }
            .disclaimer {
                margin-top: 10px;
                font-style: italic;
            }
            .footer {
                text-align: center;
                padding: 15px;
                margin-top: 20px;
                border-top: 1px solid #e0e0e0;
                color: #888;
                font-size: 10px;
            }
            .powered-by {
                margin-top: 5px;
                color: #1976D2;
            }
            .filter-info {
                margin-top: 10px;
                color: #666;
                font-size: 11px;
                font-style: italic;
            }
            @media print {
                body { -webkit-print-color-adjust: exact; }
                .section { break-inside: avoid; }
            }
        """.trimIndent()
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            // Round to 2 decimal places
            val rounded = kotlin.math.round(amount * 100) / 100
            val intPart = rounded.toLong()
            val decimalPart = kotlin.math.abs(((rounded - intPart) * 100).toInt())
            if (decimalPart == 0) {
                intPart.toString()
            } else {
                val decStr = if (decimalPart < 10) "0$decimalPart" else "$decimalPart"
                "$intPart.$decStr"
            }
        }
    }

    private fun formatStatus(status: String): String {
        return status.replace("_", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun formatPaymentType(type: String): String {
        return when (type.lowercase()) {
            "advance" -> "Advance"
            "partial" -> "Partial"
            "final" -> "Final"
            "refund" -> "Refund"
            else -> type.replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatPaymentMode(mode: String): String {
        return when (mode.lowercase()) {
            "cash" -> "Cash"
            "upi" -> "UPI"
            "bank_transfer" -> "Bank Transfer"
            "cheque" -> "Cheque"
            "card" -> "Card"
            else -> mode.replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Generate HTML for Vehicle Finance report.
     */
    fun generateVehicleFinanceHtml(data: VehicleFinancePdfData): String {
        return wrapInDocument(data.title, buildString {
            append(generateHeader(data.title, data.registrationNumber))

            // Vehicle Info Section
            append("""
                <div class="section">
                    <div class="section-title">Vehicle Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Registration</span>
                            <span class="value">${data.registrationNumber}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Vehicle</span>
                            <span class="value">${data.vehicleName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Purchase Date</span>
                            <span class="value">${data.purchaseDate}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Payment Type</span>
                            <span class="value">${data.paymentType.replaceFirstChar { it.uppercase() }}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Purchase & Loan Details
            append("""
                <div class="section">
                    <div class="section-title">Purchase Details</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Purchase Price</span>
                            <span class="summary-value">₹${formatAmount(data.purchasePrice)}</span>
                        </div>
            """.trimIndent())

            if (data.paymentType.lowercase() == "loan") {
                append("""
                        <div class="summary-card">
                            <span class="summary-label">Down Payment</span>
                            <span class="summary-value">₹${formatAmount(data.downPayment ?: 0.0)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Loan Amount</span>
                            <span class="summary-value">₹${formatAmount(data.loanAmount ?: 0.0)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Interest Rate</span>
                            <span class="summary-value">${data.interestRate ?: 0}%</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">EMI Amount</span>
                            <span class="summary-value primary">₹${formatAmount(data.emiAmount ?: 0.0)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Tenure</span>
                            <span class="summary-value">${data.tenureMonths ?: 0} months</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Financier</span>
                            <span class="summary-value">${data.financierName ?: "N/A"}</span>
                        </div>
                """.trimIndent())
            }

            append("""
                    </div>
                </div>
            """.trimIndent())

            // Payment Status
            append("""
                <div class="section">
                    <div class="section-title">Payment Status</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Paid</span>
                            <span class="summary-value success">₹${formatAmount(data.totalPaidAmount)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Remaining</span>
                            <span class="summary-value ${if (data.remainingAmount > 0) "warning" else "success"}">₹${formatAmount(data.remainingAmount)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">EMIs Paid</span>
                            <span class="summary-value">${data.paidEmisCount}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">EMIs Remaining</span>
                            <span class="summary-value">${data.remainingEmisCount}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // EMI Payment History
            if (data.emiPayments.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">EMI Payment History</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>EMI #</th>
                                    <th>Payment Date</th>
                                    <th>Amount</th>
                                    <th>Mode</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.emiPayments.forEach { emi ->
                    append("""
                                <tr>
                                    <td>${emi.emiNumber}</td>
                                    <td>${emi.paymentDate}</td>
                                    <td>₹${formatAmount(emi.amount)}</td>
                                    <td>${emi.paymentMode}</td>
                                    <td class="status-${emi.status.lowercase()}">${formatStatus(emi.status)}</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Fleet Profit & Loss report.
     */
    fun generateFleetProfitLossHtml(data: FleetProfitLossPdfData): String {
        return wrapInDocument(data.title, buildString {
            append(generateHeader(data.title, data.dateRange))

            // Fleet Overview
            append("""
                <div class="section">
                    <div class="section-title">Fleet Overview</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Vehicles</span>
                            <span class="summary-value">${data.totalVehicles}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Profitable</span>
                            <span class="summary-value success">${data.profitableVehicles}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Loss Making</span>
                            <span class="summary-value danger">${data.lossMakingVehicles}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Period</span>
                            <span class="summary-value">${data.periodType.replaceFirstChar { it.uppercase() }}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Financial Summary
            val profitLossClass = if (data.netProfitLoss >= 0) "success" else "danger"
            append("""
                <div class="section">
                    <div class="section-title">Financial Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Revenue</span>
                            <span class="summary-value">₹${formatAmount(data.totalRevenue)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Expenses</span>
                            <span class="summary-value">₹${formatAmount(data.totalExpenses)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Net Profit/Loss</span>
                            <span class="summary-value $profitLossClass">₹${formatAmount(data.netProfitLoss)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Profit Margin</span>
                            <span class="summary-value">${formatAmount(data.profitMargin)}%</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Vehicle-wise P&L Table
            if (data.vehicles.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Vehicle-wise Profit & Loss</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Vehicle</th>
                                    <th>Trips</th>
                                    <th>Revenue</th>
                                    <th>Cost</th>
                                    <th>Profit/Loss</th>
                                    <th>Margin</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.vehicles.forEach { vehicle ->
                    val plClass = if (vehicle.profitStatus == "profit") "success" else "danger"
                    append("""
                                <tr>
                                    <td><strong>${vehicle.registrationNumber}</strong><br><small>${vehicle.vehicleName}</small></td>
                                    <td>${vehicle.totalTrips}</td>
                                    <td>₹${formatAmount(vehicle.totalRevenue)}</td>
                                    <td>₹${formatAmount(vehicle.totalCost)}</td>
                                    <td class="$plClass">₹${formatAmount(vehicle.netProfit)}</td>
                                    <td>${formatAmount(vehicle.profitMargin)}%</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Cost Breakdown
            if (data.costBreakdown.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Cost Breakdown</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Cost Type</th>
                                    <th>Amount</th>
                                    <th>Count</th>
                                    <th>Percentage</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.costBreakdown.forEach { cost ->
                    append("""
                                <tr>
                                    <td>${cost.costLabel}</td>
                                    <td>₹${formatAmount(cost.amount)}</td>
                                    <td>${cost.count}</td>
                                    <td>${formatAmount(cost.percentage)}%</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Vehicle Profit & Loss report.
     */
    fun generateVehicleProfitLossHtml(data: VehicleProfitLossPdfData): String {
        return wrapInDocument(data.title, buildString {
            append(generateHeader(data.title, data.registrationNumber))

            // Vehicle Info
            append("""
                <div class="section">
                    <div class="section-title">Vehicle Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Registration</span>
                            <span class="value">${data.registrationNumber}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Vehicle</span>
                            <span class="value">${data.vehicleName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Period</span>
                            <span class="value">${data.dateRange}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Status</span>
                            <span class="value status-${data.profitStatus}">${data.profitStatus.replaceFirstChar { it.uppercase() }}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Financial Summary
            val profitLossClass = if (data.netProfitLoss >= 0) "success" else "danger"
            append("""
                <div class="section">
                    <div class="section-title">Financial Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Revenue</span>
                            <span class="summary-value">₹${formatAmount(data.totalRevenue)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Cost</span>
                            <span class="summary-value">₹${formatAmount(data.totalCost)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Net Profit/Loss</span>
                            <span class="summary-value $profitLossClass">₹${formatAmount(data.netProfitLoss)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Profit Margin</span>
                            <span class="summary-value">${formatAmount(data.profitMargin)}%</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Cost Breakdown
            append("""
                <div class="section">
                    <div class="section-title">Cost Breakdown</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Fuel Cost</span>
                            <span class="summary-value">₹${formatAmount(data.fuelCost)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Maintenance</span>
                            <span class="summary-value">₹${formatAmount(data.maintenanceCost)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Other Costs</span>
                            <span class="summary-value">₹${formatAmount(data.otherCost)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Performance Metrics
            append("""
                <div class="section">
                    <div class="section-title">Performance Metrics</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Trips</span>
                            <span class="summary-value">${data.totalTrips}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Distance</span>
                            <span class="summary-value">${formatAmount(data.totalDistance)} km</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Avg Profit/Trip</span>
                            <span class="summary-value">₹${formatAmount(data.avgProfitPerTrip)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Avg Profit/Km</span>
                            <span class="summary-value">₹${formatAmount(data.avgProfitPerKm)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Trip Details
            if (data.tripDetails.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Trip Details</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Trip ID</th>
                                    <th>Date</th>
                                    <th>Route</th>
                                    <th>Distance</th>
                                    <th>Revenue</th>
                                    <th>Cost</th>
                                    <th>Profit</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.tripDetails.forEach { trip ->
                    val tripProfitClass = if (trip.profit >= 0) "success" else "danger"
                    append("""
                                <tr>
                                    <td>#${trip.tripId}</td>
                                    <td>${trip.tripDate}</td>
                                    <td>${trip.startLocation} → ${trip.endLocation}</td>
                                    <td>${formatAmount(trip.distance)} km</td>
                                    <td>₹${formatAmount(trip.revenue)}</td>
                                    <td>₹${formatAmount(trip.cost)}</td>
                                    <td class="$tripProfitClass">₹${formatAmount(trip.profit)}</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Cost Analysis report.
     */
    fun generateCostAnalysisHtml(data: CostAnalysisPdfData): String {
        return wrapInDocument(data.title, buildString {
            append(generateHeader(data.title, data.dateRange))

            // Summary
            append("""
                <div class="section">
                    <div class="section-title">Cost Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Analysis Type</span>
                            <span class="summary-value">${data.analysisType.replaceFirstChar { it.uppercase() }}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Period</span>
                            <span class="summary-value">${data.dateRange}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Cost</span>
                            <span class="summary-value primary">₹${formatAmount(data.totalCost)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Top Cost Items
            if (data.topCostItems.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Top Cost Items</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Rank</th>
                                    <th>Cost Type</th>
                                    <th>Amount</th>
                                    <th>Percentage</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.topCostItems.forEach { item ->
                    append("""
                                <tr>
                                    <td>#${item.rank}</td>
                                    <td>${item.costLabel}</td>
                                    <td>₹${formatAmount(item.amount)}</td>
                                    <td>${formatAmount(item.percentage)}%</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Cost Groups
            if (data.costGroups.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Cost Groups</div>
                """.trimIndent())

                data.costGroups.forEach { group ->
                    append("""
                        <div class="cost-group">
                            <div class="cost-group-header">
                                <span class="group-name">${group.groupLabel}</span>
                                <span class="group-total">₹${formatAmount(group.totalAmount)} (${formatAmount(group.percentage)}%)</span>
                            </div>
                            <table class="data-table">
                                <tbody>
                    """.trimIndent())

                    group.items.forEach { item ->
                        append("""
                                    <tr>
                                        <td>${item.costLabel}</td>
                                        <td>₹${formatAmount(item.amount)}</td>
                                        <td>${item.count} entries</td>
                                    </tr>
                        """.trimIndent())
                    }

                    append("""
                                </tbody>
                            </table>
                        </div>
                    """.trimIndent())
                }

                append("</div>")
            }

            // Monthly Trend
            if (data.monthlyTrend.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Monthly Trend</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Month</th>
                                    <th>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.monthlyTrend.forEach { month ->
                    append("""
                                <tr>
                                    <td>${month.month}</td>
                                    <td>₹${formatAmount(month.amount)}</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Vehicle-wise Costs
            data.vehicleWiseCosts?.let { vehicles ->
                if (vehicles.isNotEmpty()) {
                    append("""
                        <div class="section">
                            <div class="section-title">Vehicle-wise Costs</div>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Vehicle</th>
                                        <th>Trip Cost</th>
                                        <th>Maintenance</th>
                                        <th>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                    """.trimIndent())

                    vehicles.forEach { vehicle ->
                        append("""
                                    <tr>
                                        <td>${vehicle.registrationNumber}</td>
                                        <td>₹${formatAmount(vehicle.tripCost)}</td>
                                        <td>₹${formatAmount(vehicle.maintenanceCost)}</td>
                                        <td>₹${formatAmount(vehicle.totalCost)}</td>
                                    </tr>
                        """.trimIndent())
                    }

                    append("""
                                </tbody>
                            </table>
                        </div>
                    """.trimIndent())
                }
            }

            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Customer Financials report.
     */
    fun generateCustomerFinancialsHtml(data: CustomerFinancialsPdfData): String {
        return wrapInDocument(data.title, buildString {
            append(generateHeader(data.title, data.customerName))

            // Customer Info
            append("""
                <div class="section">
                    <div class="section-title">Customer Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Customer Name</span>
                            <span class="value">${data.customerName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Company</span>
                            <span class="value">${data.companyName}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Contact</span>
                            <span class="value">${data.contactNumber}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Period</span>
                            <span class="value">${data.dateRange}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Financial Summary
            append("""
                <div class="section">
                    <div class="section-title">Financial Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Revenue</span>
                            <span class="summary-value">₹${formatAmount(data.totalRevenue)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Received</span>
                            <span class="summary-value success">₹${formatAmount(data.totalReceived)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Pending</span>
                            <span class="summary-value ${if (data.totalPending > 0) "warning" else "success"}">₹${formatAmount(data.totalPending)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Trip Statistics
            append("""
                <div class="section">
                    <div class="section-title">Trip Statistics</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Trips</span>
                            <span class="summary-value">${data.totalTrips}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Completed</span>
                            <span class="summary-value success">${data.completedTrips}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Active</span>
                            <span class="summary-value">${data.activeTrips}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Payment Statistics
            append("""
                <div class="section">
                    <div class="section-title">Payment Statistics</div>
                    <div class="summary-grid">
                        <div class="summary-card">
                            <span class="summary-label">Total Payments</span>
                            <span class="summary-value">${data.paymentStats.totalPayments}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Advance</span>
                            <span class="summary-value">${data.paymentStats.advancePayments}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Partial</span>
                            <span class="summary-value">${data.paymentStats.partialPayments}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Final</span>
                            <span class="summary-value">${data.paymentStats.finalPayments}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Avg Amount</span>
                            <span class="summary-value">₹${formatAmount(data.paymentStats.avgPaymentAmount)}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Trip Summary Table
            if (data.tripSummary.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Trip Summary</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Trip ID</th>
                                    <th>Date</th>
                                    <th>Route</th>
                                    <th>Price</th>
                                    <th>Paid</th>
                                    <th>Pending</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.tripSummary.forEach { trip ->
                    val statusClass = when (trip.paymentStatus.lowercase()) {
                        "paid" -> "success"
                        "partial" -> "warning"
                        else -> "danger"
                    }
                    append("""
                                <tr>
                                    <td>#${trip.tripId}</td>
                                    <td>${trip.tripDate}</td>
                                    <td>${trip.route}</td>
                                    <td>₹${formatAmount(trip.tripPrice)}</td>
                                    <td>₹${formatAmount(trip.paidAmount)}</td>
                                    <td>₹${formatAmount(trip.pendingAmount)}</td>
                                    <td class="status-$statusClass">${formatStatus(trip.paymentStatus)}</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            // Recent Payments
            if (data.recentPayments.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Recent Payments</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Receipt #</th>
                                    <th>Date</th>
                                    <th>Trip ID</th>
                                    <th>Amount</th>
                                    <th>Type</th>
                                    <th>Mode</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.recentPayments.forEach { payment ->
                    append("""
                                <tr>
                                    <td>${payment.receiptNumber}</td>
                                    <td>${payment.paymentDate}</td>
                                    <td>#${payment.tripId}</td>
                                    <td>₹${formatAmount(payment.amount)}</td>
                                    <td>${formatPaymentType(payment.paymentType)}</td>
                                    <td>${formatPaymentMode(payment.paymentMode)}</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            append(generateFooter(data.generatedAt))
        })
    }

    /**
     * Generate HTML for Vehicle Maintenance Costs report.
     */
    fun generateVehicleMaintenanceCostsHtml(data: VehicleMaintenanceCostsPdfData): String {
        return wrapInDocument(data.title, buildString {
            // Header
            append(generateHeader(data.title, data.registrationNumber))

            // Vehicle Info Section
            append("""
                <div class="section">
                    <div class="section-title">Vehicle Information</div>
                    <div class="info-grid">
                        <div class="info-item">
                            <span class="label">Registration</span>
                            <span class="value">${data.registrationNumber}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Make</span>
                            <span class="value">${data.vehicleMake}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Model</span>
                            <span class="value">${data.vehicleModel}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">Period</span>
                            <span class="value">${data.period}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Cost Summary Section
            append("""
                <div class="section">
                    <div class="section-title">Cost Summary</div>
                    <div class="summary-grid">
                        <div class="summary-card primary">
                            <span class="summary-label">Total Amount</span>
                            <span class="summary-value">₹${formatAmount(data.totalAmount)}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Total Entries</span>
                            <span class="summary-value">${data.entryCount}</span>
                        </div>
                        <div class="summary-card">
                            <span class="summary-label">Categories</span>
                            <span class="summary-value">${data.categoryCount}</span>
                        </div>
                    </div>
                </div>
            """.trimIndent())

            // Costs by Category
            if (data.costsByType.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">Cost Breakdown by Category</div>
                """.trimIndent())

                data.costsByType.forEach { (category, costs) ->
                    val categoryTotal = costs.sumOf { it.amount }
                    append("""
                        <div class="category-section">
                            <div class="category-header">
                                <span class="category-name">$category</span>
                                <span class="category-total">₹${formatAmount(categoryTotal)} (${costs.size} entries)</span>
                            </div>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Description</th>
                                        <th>Vendor</th>
                                        <th>Amount</th>
                                    </tr>
                                </thead>
                                <tbody>
                    """.trimIndent())

                    costs.forEach { cost ->
                        append("""
                                    <tr>
                                        <td>${cost.date}</td>
                                        <td>${cost.description ?: cost.notes ?: "-"}</td>
                                        <td>${cost.vendorName ?: "-"}</td>
                                        <td>₹${formatAmount(cost.amount)}</td>
                                    </tr>
                        """.trimIndent())
                    }

                    append("""
                                </tbody>
                            </table>
                        </div>
                    """.trimIndent())
                }

                append("</div>")
            }

            // All Costs Table
            if (data.costs.isNotEmpty()) {
                append("""
                    <div class="section">
                        <div class="section-title">All Maintenance Costs</div>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Type</th>
                                    <th>Description</th>
                                    <th>Vendor</th>
                                    <th>Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                """.trimIndent())

                data.costs.forEach { cost ->
                    append("""
                                <tr>
                                    <td>${cost.date}</td>
                                    <td>${cost.costLabel}</td>
                                    <td>${cost.description ?: cost.notes ?: "-"}</td>
                                    <td>${cost.vendorName ?: "-"}</td>
                                    <td>₹${formatAmount(cost.amount)}</td>
                                </tr>
                    """.trimIndent())
                }

                append("""
                            </tbody>
                        </table>
                    </div>
                """.trimIndent())
            }

            append(generateFooter(data.generatedAt))
        })
    }
}
