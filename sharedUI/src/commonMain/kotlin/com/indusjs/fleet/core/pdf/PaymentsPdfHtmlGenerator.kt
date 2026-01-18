package com.indusjs.fleet.core.pdf

import com.indusjs.fleet.presentation.payments.PaymentsListPdfData
import com.indusjs.fleet.presentation.payments.PaymentReceiptPdfData

/**
 * Generates HTML content for Payments PDFs.
 */
object PaymentsPdfHtmlGenerator {

    /**
     * Generate HTML for Payments List Report.
     */
    fun generatePaymentsListHtml(pdfData: PaymentsListPdfData): String {
        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { font-family: Arial, sans-serif; font-size: 12px; color: #333; padding: 20px; }
                        .header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #4CAF50; padding-bottom: 15px; }
                        .header h1 { color: #2E7D32; font-size: 24px; margin-bottom: 5px; }
                        .header .subtitle { color: #666; font-size: 12px; }
                        .meta-row { display: flex; justify-content: space-between; margin-bottom: 15px; }
                        .meta-box { background: #f5f5f5; padding: 10px; border-radius: 5px; flex: 1; margin: 0 5px; }
                        .meta-box:first-child { margin-left: 0; }
                        .meta-box:last-child { margin-right: 0; }
                        .meta-label { color: #666; font-size: 10px; text-transform: uppercase; }
                        .meta-value { font-size: 14px; font-weight: bold; color: #333; margin-top: 3px; }
                        .summary-section { display: flex; justify-content: space-around; margin: 20px 0; padding: 15px; background: linear-gradient(135deg, #e8f5e9, #fff); border-radius: 8px; border: 1px solid #c8e6c9; }
                        .summary-item { text-align: center; }
                        .summary-label { color: #666; font-size: 10px; text-transform: uppercase; }
                        .summary-value { font-size: 18px; font-weight: bold; margin-top: 5px; }
                        .summary-value.received { color: #2E7D32; }
                        .summary-value.pending { color: #E65100; }
                        .summary-value.total { color: #1976D2; }
                        .filters-section { background: #fff3e0; padding: 10px; border-radius: 5px; margin-bottom: 15px; }
                        .filters-title { font-weight: bold; color: #E65100; margin-bottom: 5px; }
                        .filters-content { font-size: 11px; color: #666; }
                        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
                        th { background: #4CAF50; color: white; padding: 10px 8px; text-align: left; font-size: 11px; }
                        td { padding: 8px; border-bottom: 1px solid #e0e0e0; font-size: 11px; }
                        tr:nth-child(even) { background: #f9f9f9; }
                        tr:hover { background: #f0f0f0; }
                        .amount { font-weight: bold; text-align: right; }
                        .status-received { color: #2E7D32; font-weight: bold; }
                        .status-pending { color: #E65100; font-weight: bold; }
                        .status-cancelled { color: #C62828; font-weight: bold; }
                        .footer { margin-top: 20px; padding-top: 15px; border-top: 1px solid #e0e0e0; text-align: center; color: #999; font-size: 10px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>💳 ${pdfData.reportTitle}</h1>
                        <div class="subtitle">IndusJS Fleet Management</div>
                    </div>
                    
                    <div class="meta-row">
                        <div class="meta-box">
                            <div class="meta-label">Generated On</div>
                            <div class="meta-value">${pdfData.generatedDate} at ${pdfData.generatedTime}</div>
                        </div>
                        <div class="meta-box">
                            <div class="meta-label">Date Range</div>
                            <div class="meta-value">${pdfData.fromDate ?: "All"} to ${pdfData.toDate ?: "Today"}</div>
                        </div>
                        <div class="meta-box">
                            <div class="meta-label">Total Payments</div>
                            <div class="meta-value">${pdfData.totalPaymentsCount}</div>
                        </div>
                    </div>
            """.trimIndent())

            // Filters section (only if filters applied)
            if (pdfData.paymentType != null || pdfData.paymentMode != null || pdfData.paymentStatus != null) {
                append("""
                    <div class="filters-section">
                        <div class="filters-title">🔍 Applied Filters</div>
                        <div class="filters-content">
                """.trimIndent())

                val filters = mutableListOf<String>()
                pdfData.paymentType?.let { filters.add("Type: $it") }
                pdfData.paymentMode?.let { filters.add("Mode: $it") }
                pdfData.paymentStatus?.let { filters.add("Status: $it") }
                append(filters.joinToString(" | "))

                append("</div></div>")
            }

            // Summary section
            append("""
                    <div class="summary-section">
                        <div class="summary-item">
                            <div class="summary-label">💰 Total Received</div>
                            <div class="summary-value received">₹${formatAmount(pdfData.totalReceived)}</div>
                        </div>
                        <div class="summary-item">
                            <div class="summary-label">⏳ Total Pending</div>
                            <div class="summary-value pending">₹${formatAmount(pdfData.totalPending)}</div>
                        </div>
                        <div class="summary-item">
                            <div class="summary-label">📅 This Month</div>
                            <div class="summary-value total">₹${formatAmount(pdfData.thisMonthTotal)}</div>
                        </div>
                    </div>
            """.trimIndent())

            // Payments table
            append("""
                    <table>
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Date</th>
                                <th>Vehicle</th>
                                <th>Customer</th>
                                <th>Type</th>
                                <th>Mode</th>
                                <th>Status</th>
                                <th style="text-align: right;">Amount</th>
                            </tr>
                        </thead>
                        <tbody>
            """.trimIndent())

            pdfData.payments.forEachIndexed { index, payment ->
                val statusClass = when (payment.paymentStatus.lowercase()) {
                    "received" -> "status-received"
                    "pending" -> "status-pending"
                    "cancelled" -> "status-cancelled"
                    else -> ""
                }
                append("""
                            <tr>
                                <td>${index + 1}</td>
                                <td>${payment.paymentDate}</td>
                                <td>${payment.vehicleNumber ?: "-"}</td>
                                <td>${payment.customerName ?: "-"}</td>
                                <td>${payment.paymentType}</td>
                                <td>${payment.paymentMode}</td>
                                <td class="$statusClass">${payment.paymentStatus}</td>
                                <td class="amount">₹${formatAmount(payment.amount)}</td>
                            </tr>
                """.trimIndent())
            }

            append("""
                        </tbody>
                    </table>
                    
                    <div class="footer">
                        <p>This is a computer-generated document. No signature required.</p>
                        <p>Generated by IndusJS Fleet Management System</p>
                    </div>
                </body>
                </html>
            """.trimIndent())
        }
    }

    /**
     * Generate HTML for Payment Receipt.
     */
    fun generatePaymentReceiptHtml(pdfData: PaymentReceiptPdfData): String {
        return buildString {
            append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { font-family: Arial, sans-serif; font-size: 12px; color: #333; padding: 30px; max-width: 600px; margin: 0 auto; }
                        .receipt-container { border: 2px solid #4CAF50; border-radius: 10px; padding: 25px; background: #fff; }
                        .header { text-align: center; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px dashed #e0e0e0; }
                        .header h1 { color: #2E7D32; font-size: 22px; margin-bottom: 5px; }
                        .header .company { color: #666; font-size: 14px; font-weight: bold; }
                        .receipt-no { background: #e8f5e9; padding: 10px; text-align: center; border-radius: 5px; margin-bottom: 20px; }
                        .receipt-no .label { font-size: 10px; color: #666; text-transform: uppercase; }
                        .receipt-no .number { font-size: 18px; font-weight: bold; color: #2E7D32; }
                        .amount-section { background: linear-gradient(135deg, #4CAF50, #2E7D32); color: white; padding: 20px; border-radius: 8px; text-align: center; margin-bottom: 20px; }
                        .amount-section .label { font-size: 12px; opacity: 0.9; }
                        .amount-section .amount { font-size: 32px; font-weight: bold; margin: 10px 0; }
                        .amount-section .status { display: inline-block; background: rgba(255,255,255,0.2); padding: 5px 15px; border-radius: 15px; font-size: 11px; }
                        .section { margin-bottom: 15px; }
                        .section-title { font-size: 11px; color: #4CAF50; font-weight: bold; text-transform: uppercase; margin-bottom: 8px; padding-bottom: 5px; border-bottom: 1px solid #e0e0e0; }
                        .row { display: flex; justify-content: space-between; padding: 5px 0; }
                        .row .label { color: #666; }
                        .row .value { font-weight: 500; text-align: right; }
                        .divider { border-top: 1px dashed #e0e0e0; margin: 15px 0; }
                        .amount-breakdown { background: #f5f5f5; padding: 12px; border-radius: 5px; }
                        .amount-breakdown .row { padding: 3px 0; }
                        .amount-breakdown .total { border-top: 1px solid #ccc; margin-top: 5px; padding-top: 8px; font-weight: bold; }
                        .footer { margin-top: 20px; padding-top: 15px; border-top: 2px dashed #e0e0e0; text-align: center; }
                        .footer .thank-you { font-size: 14px; color: #4CAF50; font-weight: bold; margin-bottom: 10px; }
                        .footer .note { font-size: 9px; color: #999; }
                    </style>
                </head>
                <body>
                    <div class="receipt-container">
                        <div class="header">
                            <h1>🧾 ${pdfData.receiptTitle}</h1>
                            <div class="company">IndusJS Fleet Management</div>
                        </div>
                        
                        <div class="receipt-no">
                            <div class="label">Receipt Number</div>
                            <div class="number">${pdfData.receiptNumber ?: "PMT-${pdfData.paymentId}"}</div>
                        </div>
                        
                        <div class="amount-section">
                            <div class="label">Amount Received</div>
                            <div class="amount">₹${formatAmount(pdfData.netAmount)}</div>
                            <div class="status">${pdfData.paymentStatus}</div>
                        </div>
                        
                        <div class="section">
                            <div class="section-title">📅 Payment Details</div>
                            <div class="row">
                                <span class="label">Date</span>
                                <span class="value">${pdfData.paymentDate}</span>
                            </div>
                            ${if (pdfData.paymentTime != null) """
                            <div class="row">
                                <span class="label">Time</span>
                                <span class="value">${pdfData.paymentTime}</span>
                            </div>
                            """ else ""}
                            <div class="row">
                                <span class="label">Payment Type</span>
                                <span class="value">${pdfData.paymentType}</span>
                            </div>
                            <div class="row">
                                <span class="label">Payment Mode</span>
                                <span class="value">${pdfData.paymentMode}</span>
                            </div>
                            ${if (pdfData.transactionId != null) """
                            <div class="row">
                                <span class="label">Transaction ID</span>
                                <span class="value">${pdfData.transactionId}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.bankName != null) """
                            <div class="row">
                                <span class="label">Bank</span>
                                <span class="value">${pdfData.bankName}</span>
                            </div>
                            """ else ""}
                        </div>
            """.trimIndent())

            // Amount breakdown if there are deductions
            if (pdfData.tdsAmount > 0 || pdfData.discountAmount > 0) {
                append("""
                        <div class="amount-breakdown">
                            <div class="row">
                                <span class="label">Gross Amount</span>
                                <span class="value">₹${formatAmount(pdfData.amount)}</span>
                            </div>
                            ${if (pdfData.tdsAmount > 0) """
                            <div class="row">
                                <span class="label">TDS Deduction</span>
                                <span class="value">- ₹${formatAmount(pdfData.tdsAmount)}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.discountAmount > 0) """
                            <div class="row">
                                <span class="label">Discount</span>
                                <span class="value">- ₹${formatAmount(pdfData.discountAmount)}</span>
                            </div>
                            """ else ""}
                            <div class="row total">
                                <span class="label">Net Amount</span>
                                <span class="value">₹${formatAmount(pdfData.netAmount)}</span>
                            </div>
                        </div>
                """.trimIndent())
            }

            // Trip info
            if (pdfData.tripId != null || pdfData.vehicleNumber != null) {
                append("""
                        <div class="divider"></div>
                        <div class="section">
                            <div class="section-title">🚛 Trip Information</div>
                            ${if (pdfData.tripId != null) """
                            <div class="row">
                                <span class="label">Trip ID</span>
                                <span class="value">#${pdfData.tripId}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.vehicleNumber != null) """
                            <div class="row">
                                <span class="label">Vehicle</span>
                                <span class="value">${pdfData.vehicleNumber}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.driverName != null) """
                            <div class="row">
                                <span class="label">Driver</span>
                                <span class="value">${pdfData.driverName}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.startLocation != null && pdfData.endLocation != null) """
                            <div class="row">
                                <span class="label">Route</span>
                                <span class="value">${pdfData.startLocation} → ${pdfData.endLocation}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.tripPrice != null) """
                            <div class="row">
                                <span class="label">Trip Price</span>
                                <span class="value">₹${formatAmount(pdfData.tripPrice)}</span>
                            </div>
                            """ else ""}
                        </div>
                """.trimIndent())
            }

            // Customer info
            if (pdfData.customerName != null) {
                append("""
                        <div class="divider"></div>
                        <div class="section">
                            <div class="section-title">👤 Customer Information</div>
                            <div class="row">
                                <span class="label">Name</span>
                                <span class="value">${pdfData.customerName}</span>
                            </div>
                            ${if (pdfData.customerContact != null) """
                            <div class="row">
                                <span class="label">Contact</span>
                                <span class="value">${pdfData.customerContact}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.customerCompany != null) """
                            <div class="row">
                                <span class="label">Company</span>
                                <span class="value">${pdfData.customerCompany}</span>
                            </div>
                            """ else ""}
                            ${if (pdfData.customerGst != null) """
                            <div class="row">
                                <span class="label">GST No.</span>
                                <span class="value">${pdfData.customerGst}</span>
                            </div>
                            """ else ""}
                        </div>
                """.trimIndent())
            }

            // Notes
            if (pdfData.notes != null) {
                append("""
                        <div class="divider"></div>
                        <div class="section">
                            <div class="section-title">📝 Notes</div>
                            <p style="color: #666; font-size: 11px;">${pdfData.notes}</p>
                        </div>
                """.trimIndent())
            }

            // Footer
            append("""
                        <div class="footer">
                            <div class="thank-you">Thank You for Your Business!</div>
                            <div class="note">
                                <p>Generated on ${pdfData.generatedDate} at ${pdfData.generatedTime}</p>
                                ${if (pdfData.receivedBy != null) "<p>Received by: ${pdfData.receivedBy}</p>" else ""}
                                <p>This is a computer-generated receipt.</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent())
        }
    }

    private fun formatAmount(value: Double): String {
        val intPart = value.toLong()
        val decPart = ((value - intPart) * 100).toLong()
        val formattedInt = formatWithIndianCommas(intPart)
        return if (decPart == 0L) "$formattedInt.00" else "$formattedInt.${decPart.toString().padStart(2, '0')}"
    }

    private fun formatWithIndianCommas(value: Long): String {
        if (value < 1000) return value.toString()
        val str = value.toString()
        val len = str.length
        val sb = StringBuilder()
        var count = 0
        for (i in len - 1 downTo 0) {
            sb.insert(0, str[i])
            count++
            if (i > 0) {
                if (count == 3 || (count > 3 && (count - 3) % 2 == 0)) {
                    sb.insert(0, ',')
                }
            }
        }
        return sb.toString()
    }
}
