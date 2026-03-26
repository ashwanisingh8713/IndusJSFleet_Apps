package com.ijs.customer.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.customer.TAG_CUSTOMER_PDF
import com.indusjs.datetimeutils.FleetDateTime
import com.ijs.customer.presentation.detail.CustomerDetailContract.Effect
import com.ijs.customer.presentation.detail.CustomerDetailContract.ReportType
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.indusjs.pdfreport.model.*

/**
 * Handles PDF export logic for all customer report types.
 * Extracted from CustomerDetailViewModel to keep it under 500 lines.
 */
class CustomerDetailPdfExporter(
    private val getState: () -> State,
    private val setState: (State.() -> State) -> Unit,
    private val sendEffect: (Effect) -> Unit,
    private val logger: FleetLogger
) {

    suspend fun exportPdf(reportType: ReportType) {
        setState { copy(isExportingPdf = true, exportType = reportType) }

        try {
            when (reportType) {
                ReportType.TRIPS -> exportTripsPdf()
                ReportType.PENDING_PAYMENTS -> exportTripsPdf()
                ReportType.PAYMENTS -> exportPaymentsPdf()
                ReportType.FINANCIALS -> exportFinancialsPdf()
            }
        } catch (e: Exception) {
            logger.e(TAG_CUSTOMER_PDF, "PDF export failed: ${e.message}")
            sendEffect(Effect.ShowSnackbar("Failed to export PDF: ${e.message}"))
        } finally {
            setState { copy(isExportingPdf = false, exportType = null) }
        }
    }

    private fun exportTripsPdf() {
        val state = getState()
        val customer = state.customer ?: return
        val trips = state.trips
        val summary = state.tripsSummary

        if (trips.isEmpty()) {
            sendEffect(Effect.ShowSnackbar("No trips to export"))
            return
        }

        val totalTrips = summary?.totalTrips ?: trips.size
        val totalRevenue = summary?.totalRevenue ?: trips.sumOf { it.tripPrice ?: 0.0 }
        val totalPaid = trips.sumOf { it.paidAmount ?: 0.0 }
        val totalPending = trips.sumOf {
            val price = it.tripPrice ?: 0.0
            val paid = it.paidAmount ?: 0.0
            (price - paid).coerceAtLeast(0.0)
        }

        val pdfData = CustomerTripsPdfData(
            customerId = customer.id.toIntOrNull() ?: 0,
            customerName = customer.personName,
            companyName = customer.companyName,
            contactNumber = customer.primaryContact,
            totalTrips = totalTrips,
            totalRevenue = totalRevenue,
            totalPaid = totalPaid,
            totalPending = totalPending,
            dateRange = null,
            trips = trips.map { trip ->
                val dueAmount = trip.pendingAmount ?: run {
                    val price = trip.tripPrice ?: 0.0
                    val paid = trip.paidAmount ?: 0.0
                    (price - paid).coerceAtLeast(0.0)
                }
                CustomerTripItem(
                    tripId = trip.id.toIntOrNull() ?: 0,
                    vehicleNumber = trip.vehicleRegistration ?: "-",
                    startLocation = trip.startLocation ?: "-",
                    endLocation = trip.endLocation ?: "-",
                    startDate = FleetDateTime.formatIsoToDisplayDate(trip.plannedStart ?: trip.scheduledDate),
                    endDate = FleetDateTime.formatIsoToDisplayDate(trip.plannedEnd),
                    tripStatus = trip.stateDisplay,
                    tripPrice = trip.tripPrice ?: 0.0,
                    paidAmount = trip.paidAmount ?: 0.0,
                    pendingAmount = dueAmount
                )
            },
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        sendEffect(Effect.ExportTripsPdf(pdfData))
    }

    private fun exportPaymentsPdf() {
        val state = getState()
        val customer = state.customer ?: return
        val payments = state.receivedPayments

        if (payments.isEmpty()) {
            sendEffect(Effect.ShowSnackbar("No payments to export"))
            return
        }

        val pdfData = CustomerPaymentsPdfData(
            customerId = customer.id.toIntOrNull() ?: 0,
            customerName = customer.personName,
            companyName = customer.companyName,
            contactNumber = customer.primaryContact,
            totalPayments = payments.size,
            totalAmount = state.totalReceivedAmount,
            dateRange = null,
            payments = payments.map { payment ->
                CustomerPaymentItem(
                    paymentId = payment.id.toIntOrNull() ?: 0,
                    tripId = payment.tripId?.toIntOrNull() ?: 0,
                    vehicleNumber = "-",
                    amount = payment.amount,
                    paymentType = payment.paymentType ?: "payment",
                    paymentMode = payment.modeDisplay,
                    paymentDate = payment.date?.let { FleetDateTime.formatIsoToDisplayDateTime12Hour(it) } ?: "-",
                    receiptNumber = payment.receiptNumber,
                    startLocation = null,
                    endLocation = null
                )
            },
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        sendEffect(Effect.ExportPaymentsPdf(pdfData))
    }

    private fun exportFinancialsPdf() {
        val state = getState()
        val customer = state.customer ?: return
        val report = state.financialReport

        if (report == null) {
            sendEffect(Effect.ShowSnackbar("No financial data to export"))
            return
        }

        val payments = state.receivedPayments
        val paymentStats = CustomerPaymentStatsPdf(
            totalPayments = payments.size,
            advancePayments = payments.count { it.paymentType?.lowercase() == "advance" },
            partialPayments = payments.count { it.paymentType?.lowercase() == "partial" },
            finalPayments = payments.count { it.paymentType?.lowercase() == "final" },
            avgPaymentAmount = if (payments.isNotEmpty()) payments.sumOf { it.amount } / payments.size else 0.0
        )

        val recentPayments = payments.take(10).map { payment ->
            CustomerPaymentPdfItem(
                paymentId = payment.id.toIntOrNull() ?: 0,
                tripId = payment.tripId?.toIntOrNull() ?: 0,
                paymentDate = payment.date?.let { FleetDateTime.formatIsoToDisplayDate(it) } ?: "-",
                amount = payment.amount,
                paymentType = payment.paymentType ?: "payment",
                paymentMode = payment.modeDisplay,
                receiptNumber = payment.receiptNumber ?: "-"
            )
        }

        val trips = state.trips
        val tripSummary = trips.take(10).map { trip ->
            val pendingAmount = trip.pendingAmount ?: run {
                val price = trip.tripPrice ?: 0.0
                val paid = trip.paidAmount ?: 0.0
                (price - paid).coerceAtLeast(0.0)
            }
            CustomerTripSummaryPdfItem(
                tripId = trip.id.toIntOrNull() ?: 0,
                tripDate = FleetDateTime.formatIsoToDisplayDate(trip.plannedStart ?: trip.scheduledDate),
                route = trip.routeDisplay,
                tripPrice = trip.tripPrice ?: 0.0,
                paidAmount = trip.paidAmount ?: 0.0,
                pendingAmount = pendingAmount,
                paymentStatus = when {
                    pendingAmount <= 0 -> "Paid"
                    (trip.paidAmount ?: 0.0) > 0 -> "Partial"
                    else -> "Pending"
                }
            )
        }

        val dateRange = "${state.financialsStartDate} to ${state.financialsEndDate}"

        val pdfData = CustomerFinancialsPdfData(
            customerId = customer.id.toIntOrNull() ?: 0,
            customerName = customer.personName,
            companyName = customer.companyName,
            contactNumber = customer.primaryContact,
            dateRange = dateRange,
            totalTrips = report.tripSummary?.totalTrips ?: trips.size,
            completedTrips = report.tripSummary?.completedTrips ?: trips.count { it.state?.lowercase() == "completed" },
            activeTrips = trips.count { it.state?.lowercase() == "on_route" },
            totalRevenue = report.totalRevenue ?: 0.0,
            totalReceived = report.paymentReceived ?: 0.0,
            totalPending = report.paymentPending ?: 0.0,
            paymentStats = paymentStats,
            recentPayments = recentPayments,
            tripSummary = tripSummary,
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )

        sendEffect(Effect.ExportFinancialsPdf(pdfData))
    }
}

