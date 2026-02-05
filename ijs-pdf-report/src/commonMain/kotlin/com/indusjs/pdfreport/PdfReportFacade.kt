package com.indusjs.pdfreport

import com.indusjs.pdfreport.generator.HtmlTemplateGenerator
import com.indusjs.pdfreport.generator.PdfGenerator
import com.indusjs.pdfreport.model.*
import com.indusjs.pdfreport.util.PdfFileNameGenerator

/**
 * Facade for PDF Report generation.
 * Single entry point for all PDF operations.
 *
 * Usage:
 * ```
 * val result = PdfReportFacade.generateTripCostsReport(tripCostsPdfData)
 * if (result.success) {
 *     PdfReportFacade.shareReport(result)
 * }
 * ```
 */
object PdfReportFacade {

    private val generator: PdfGenerator = PdfGenerator()
    private val htmlGenerator: HtmlTemplateGenerator = HtmlTemplateGenerator

    /**
     * Generate Trip Costs PDF report.
     */
    suspend fun generateTripCostsReport(
        data: TripCostsPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Customer Trips PDF report.
     */
    suspend fun generateCustomerTripsReport(
        data: CustomerTripsPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Customer Payments PDF report.
     */
    suspend fun generateCustomerPaymentsReport(
        data: CustomerPaymentsPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Payments List PDF report.
     */
    suspend fun generatePaymentsListReport(
        data: PaymentsListPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Payment Receipt PDF.
     */
    suspend fun generatePaymentReceiptReport(
        data: PaymentReceiptPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Vehicle Finance PDF report.
     */
    suspend fun generateVehicleFinanceReport(
        data: VehicleFinancePdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Fleet Profit & Loss PDF report.
     */
    suspend fun generateFleetProfitLossReport(
        data: FleetProfitLossPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Vehicle Profit & Loss PDF report.
     */
    suspend fun generateVehicleProfitLossReport(
        data: VehicleProfitLossPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Cost Analysis PDF report.
     */
    suspend fun generateCostAnalysisReport(
        data: CostAnalysisPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generate Customer Financials PDF report.
     */
    suspend fun generateCustomerFinancialsReport(
        data: CustomerFinancialsPdfData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return generateReport(data, onProgress)
    }

    /**
     * Generic method to generate any report type.
     */
    suspend fun generateReport(
        data: PdfReportData,
        onProgress: ((Float) -> Unit)? = null
    ): PdfExportResult {
        return try {
            onProgress?.invoke(0.1f)

            // Generate HTML content
            val html = htmlGenerator.generateHtml(data)
            onProgress?.invoke(0.3f)

            // Generate filename
            val fileName = PdfFileNameGenerator.generateFileName(data)
            onProgress?.invoke(0.4f)

            // Generate PDF
            val result = generator.generatePdf(html, fileName) { progress ->
                // Scale progress from 0.4 to 1.0
                onProgress?.invoke(0.4f + (progress * 0.6f))
            }

            result
        } catch (e: Exception) {
            PdfExportResult.error(e.message ?: "Failed to generate PDF")
        }
    }

    /**
     * Share a generated PDF file.
     */
    suspend fun shareReport(result: PdfExportResult) {
        if (result.success) {
            generator.sharePdf(result)
        }
    }

    /**
     * Open a generated PDF file.
     */
    suspend fun openReport(result: PdfExportResult) {
        if (result.success) {
            generator.openPdf(result)
        }
    }
}
