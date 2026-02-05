package com.indusjs.pdfreport.util

import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.model.*

/**
 * Utility object for generating PDF filenames.
 */
object PdfFileNameGenerator {

    /**
     * Generate PDF filename based on report type.
     * Format: {ReportType}_{Identifier}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf
     */
    fun generateFileName(data: PdfReportData): String {
        val timestamp = formatTimestampForFileName()

        return when (data) {
            is TripCostsPdfData -> {
                val vehicleNumber = sanitize(data.vehicleNumber)
                "TripCosts_${vehicleNumber}_Trip${data.tripId}_$timestamp.pdf"
            }
            is CustomerTripsPdfData -> {
                val customerName = sanitize(data.customerName)
                "CustomerTrips_${customerName}_$timestamp.pdf"
            }
            is CustomerPaymentsPdfData -> {
                val customerName = sanitize(data.customerName)
                "CustomerPayments_${customerName}_$timestamp.pdf"
            }
            is PaymentsListPdfData -> {
                "PaymentsList_$timestamp.pdf"
            }
            is PaymentReceiptPdfData -> {
                val receiptNumber = sanitize(data.receiptNumber)
                "PaymentReceipt_${receiptNumber}_$timestamp.pdf"
            }
            is VehicleFinancePdfData -> {
                val vehicleNumber = sanitize(data.registrationNumber)
                "VehicleFinance_${vehicleNumber}_$timestamp.pdf"
            }
            is FleetProfitLossPdfData -> {
                val period = sanitize(data.periodType)
                "FleetPnL_${period}_$timestamp.pdf"
            }
            is VehicleProfitLossPdfData -> {
                val vehicleNumber = sanitize(data.registrationNumber)
                "VehiclePnL_${vehicleNumber}_$timestamp.pdf"
            }
            is CostAnalysisPdfData -> {
                val analysisType = sanitize(data.analysisType)
                "CostAnalysis_${analysisType}_$timestamp.pdf"
            }
            is CustomerFinancialsPdfData -> {
                val customerName = sanitize(data.customerName)
                "CustomerFinancials_${customerName}_$timestamp.pdf"
            }
        }
    }

    /**
     * Sanitize string for use in filename.
     * - Replace spaces with hyphen
     * - Remove special characters
     * - Limit length to 50 characters
     */
    fun sanitize(input: String): String {
        return input
            .replace(" ", "-")
            .replace("/", "-")
            .replace("\\", "-")
            .replace(":", "-")
            .replace("*", "")
            .replace("?", "")
            .replace("\"", "")
            .replace("<", "")
            .replace(">", "")
            .replace("|", "")
            .replace(".", "-")
            .take(50)
    }

    /**
     * Format current timestamp for filename: DD-MMM-YYYY_hh-mm-AM-PM
     * Example: 05-Feb-2026_02-30-PM
     */
    private fun formatTimestampForFileName(): String {
        val now = FleetDateTime.now()
        val date = FleetDateTime.formatDisplayDate(now) // 05-Feb-2026
        val time = FleetDateTime.formatTime12Hour(now)  // 02:30 PM

        // Convert time format: "02:30 PM" -> "02-30-PM"
        val timeForFile = time.replace(":", "-").replace(" ", "-")

        return "${date}_$timeForFile"
    }
}
