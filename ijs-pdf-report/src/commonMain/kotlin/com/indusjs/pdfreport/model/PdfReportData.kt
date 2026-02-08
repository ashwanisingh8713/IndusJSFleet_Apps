package com.indusjs.pdfreport.model

/**
 * Enum representing different types of PDF reports.
 */
enum class PdfReportType {
    TRIP_COSTS,
    DRIVER_COSTS,
    CUSTOMER_TRIPS,
    CUSTOMER_PAYMENTS,
    CUSTOMER_FINANCIALS,
    PAYMENTS_LIST,
    PAYMENT_RECEIPT,
    VEHICLE_FINANCE,
    FLEET_PROFIT_LOSS,
    VEHICLE_PROFIT_LOSS,
    COST_ANALYSIS,
    VEHICLE_MAINTENANCE_COSTS
}

/**
 * Base sealed interface for all PDF report data.
 * All specific report data classes must implement this.
 */
sealed interface PdfReportData {
    val reportType: PdfReportType
    val title: String
    val generatedAt: String  // DD-MMM-YYYY hh:mm AM/PM format
}
