package com.indusjs.fleet.data.mapper.customer

import com.indusjs.fleet.data.database.entity.CustomerEntity
import com.indusjs.fleet.data.model.customer.*
import com.indusjs.fleet.domain.entity.customer.*

/**
 * Mapper for converting between Customer DTOs, Entities, and domain models.
 */
object CustomerMapper {

    // ============= Basic Customer Mapping =============

    /**
     * Map CustomerDto to Customer domain entity.
     */
    fun CustomerDto.toDomain(): Customer = Customer(
        id = id.toString(),
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        secondaryContact = secondaryContact,
        companyAddress = companyAddress,
        email = email,
        gstNumber = gstNumber,
        notes = notes,
        isActive = isActive,
        ownerId = ownerId?.toString(),
        createdById = createdById?.toString(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    /**
     * Map CustomerDto to CustomerSummary.
     */
    fun CustomerDto.toSummary(): CustomerSummary = CustomerSummary(
        id = id.toString(),
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        isActive = isActive
    )

    /**
     * Map CustomerEntity to Customer domain entity.
     */
    fun CustomerEntity.toDomain(): Customer = Customer(
        id = id.toString(),
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        secondaryContact = secondaryContact,
        companyAddress = companyAddress,
        email = email,
        gstNumber = gstNumber,
        notes = notes,
        isActive = isActive,
        ownerId = ownerId?.toString(),
        createdById = createdById?.toString(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    /**
     * Map CustomerEntity to CustomerSummary.
     */
    fun CustomerEntity.toSummary(): CustomerSummary = CustomerSummary(
        id = id.toString(),
        companyName = companyName,
        personName = personName,
        primaryContact = primaryContact,
        isActive = isActive
    )

    /**
     * Map CustomerStatisticsDataDto to CustomerStatistics domain entity.
     * Updated to match new API response with nested trips/financials/performance.
     */
    fun CustomerStatisticsDataDto.toDomain(): CustomerStatistics = CustomerStatistics(
        customerId = customer?.id?.toString() ?: "",
        totalTrips = trips?.total ?: 0,
        completedTrips = trips?.completed ?: 0,
        activeTrips = trips?.onRoute ?: 0,
        totalRevenue = financials?.totalRevenue ?: 0.0,
        totalPendingPayment = financials?.totalPending ?: 0.0,
        totalReceivedPayment = financials?.totalReceived ?: 0.0,
        averageTripValue = financials?.averageTripValue ?: 0.0,
        lastTripDate = performance?.lastTripDate
    )

    /**
     * Map list of CustomerDto to list of Customer domain entities.
     */
    fun List<CustomerDto>.toDomainList(): List<Customer> = map { it.toDomain() }

    /**
     * Map list of CustomerDto to list of CustomerSummary.
     */
    fun List<CustomerDto>.toSummaryList(): List<CustomerSummary> = map { it.toSummary() }

    /**
     * Map list of CustomerEntity to list of Customer domain entities.
     */
    fun List<CustomerEntity>.entityListToDomainList(): List<Customer> = map { it.toDomain() }

    /**
     * Map list of CustomerEntity to list of CustomerSummary.
     */
    fun List<CustomerEntity>.entityListToSummaryList(): List<CustomerSummary> = map { it.toSummary() }

    // ============= Customer Trip Mapping =============

    /**
     * Map CustomerTripDto to CustomerTrip domain entity.
     */
    fun CustomerTripDto.toDomain(): CustomerTrip = CustomerTrip(
        id = id.toString(),
        vehicleId = vehicleId?.toString(),
        vehicleRegistration = vehicleRegistration,
        driverId = driverId?.toString(),
        driverName = driverName,
        startLocation = startLocation,
        endLocation = endLocation,
        estimatedDistance = estimatedDistance,
        scheduledDate = scheduledDate,
        plannedStart = plannedStart,
        plannedEnd = plannedEnd,
        actualStart = actualStart,
        actualEnd = actualEnd,
        tripPrice = expectedTripPrice,
        paidAmount = paidTripPrice,
        pendingAmount = pendingAmount,
        paymentStatus = paymentStatus,
        state = state,
        priority = priority,
        cargoType = cargoType,
        createdAt = createdAt
    )

    /**
     * Map CustomerTripsSummaryDto to CustomerTripsSummary domain entity.
     */
    fun CustomerTripsSummaryDto.toDomain(): CustomerTripsSummary = CustomerTripsSummary(
        totalTrips = totalTrips,
        completedTrips = completedTrips,
        activeTrips = activeTrips,
        plannedTrips = plannedTrips,
        cancelledTrips = cancelledTrips,
        totalRevenue = totalRevenue,
        totalPending = totalPending
    )

    /**
     * Map list of CustomerTripDto to list of CustomerTrip.
     */
    fun List<CustomerTripDto>.toTripsDomain(): List<CustomerTrip> = map { it.toDomain() }

    // ============= Pending Payment Mapping =============

    /**
     * Map CustomerPendingPaymentDto to CustomerPendingPayment domain entity.
     */
    fun CustomerPendingPaymentDto.toDomain(): CustomerPendingPayment = CustomerPendingPayment(
        tripId = tripId.toString(),
        vehicleRegistration = vehicleNumber,
        startLocation = startLocation,
        endLocation = endLocation,
        tripDate = scheduledDate,
        tripPrice = expectedPrice,
        paidAmount = paidAmount,
        pendingAmount = pendingAmount,
        daysOverdue = daysOverdue,
        state = null,
        paymentStatus = paymentStatus
    )

    /**
     * Map list of CustomerPendingPaymentDto to list of CustomerPendingPayment.
     */
    fun List<CustomerPendingPaymentDto>.toPendingPaymentsDomain(): List<CustomerPendingPayment> =
        map { it.toDomain() }

    // ============= Payment Mapping =============

    /**
     * Map CustomerPaymentDto to CustomerPayment domain entity.
     */
    fun CustomerPaymentDto.toDomain(): CustomerPayment = CustomerPayment(
        id = id.toString(),
        tripId = tripId?.toString(),
        amount = amount,
        tdsAmount = tdsAmount,
        discountAmount = discountAmount,
        netAmount = netAmount,
        paymentType = paymentType,
        mode = PaymentMode.fromApiValue(paymentMode),
        status = paymentStatus,
        date = paymentDate,
        receiptNumber = receiptNumber,
        referenceNumber = referenceNumber,
        notes = notes,
        createdAt = createdAt
    )

    /**
     * Map list of CustomerPaymentDto to list of CustomerPayment.
     */
    fun List<CustomerPaymentDto>.toPaymentsDomain(): List<CustomerPayment> = map { it.toDomain() }

    // ============= Payment Summary Mapping =============

    /**
     * Map PaymentByModeDto to PaymentByMode domain entity.
     */
    fun PaymentByModeDto.toDomain(): PaymentByMode = PaymentByMode(
        mode = PaymentMode.fromApiValue(paymentMode),
        amount = amount,
        count = count,
        percentage = 0.0 // Calculate if needed
    )

    /**
     * Map MonthlyPaymentDto to MonthlyPayment domain entity.
     */
    fun MonthlyPaymentDto.toDomain(): MonthlyPayment {
        // month format: "2026-02" - extract month and year
        val parts = month.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val monthName = when (parts.getOrNull(1)) {
            "01" -> "Jan"
            "02" -> "Feb"
            "03" -> "Mar"
            "04" -> "Apr"
            "05" -> "May"
            "06" -> "Jun"
            "07" -> "Jul"
            "08" -> "Aug"
            "09" -> "Sep"
            "10" -> "Oct"
            "11" -> "Nov"
            "12" -> "Dec"
            else -> "Unknown"
        }
        return MonthlyPayment(
            month = monthName,
            year = year,
            amount = amount,
            count = count
        )
    }

    /**
     * Map CustomerPaymentSummaryDataDto to CustomerPaymentSummary domain entity.
     */
    fun CustomerPaymentSummaryDataDto.toDomain(): CustomerPaymentSummary = CustomerPaymentSummary(
        byMode = byMode.map { it.toDomain() },
        byMonth = byMonth.map { it.toDomain() },
        totalTds = totals?.tdsAmount ?: 0.0,
        totalAmount = totals?.grossAmount ?: 0.0,
        totalPayments = byMode.sumOf { it.count }
    )

    // ============= Financial Report Mapping =============

    /**
     * Map MonthlyTrendDto to PeriodBreakdown domain entity.
     */
    fun MonthlyTrendDto.toPeriodBreakdown(): PeriodBreakdown = PeriodBreakdown(
        period = month,
        revenue = revenue,
        costs = costs,
        profit = profit,
        trips = trips
    )

    /**
     * Map CustomerFinancialReportDataDto to CustomerFinancialReport domain entity.
     */
    fun CustomerFinancialReportDataDto.toDomain(): CustomerFinancialReport = CustomerFinancialReport(
        customerId = customer?.id?.toString() ?: "",
        customerName = customer?.companyName ?: customer?.personName,
        period = FinancialPeriod.fromApiValue(period),
        startDate = dateRange?.startDate,
        endDate = dateRange?.endDate,
        totalRevenue = summary?.totalRevenue ?: 0.0,
        totalCosts = summary?.totalCosts ?: 0.0,
        netProfit = summary?.grossProfit ?: 0.0,
        profitMargin = summary?.profitMargin ?: 0.0,
        tripSummary = summary?.let {
            FinancialTripSummary(
                totalTrips = it.totalTrips,
                completedTrips = it.totalTrips, // API returns total as completed
                averageTripValue = if (it.totalTrips > 0) it.totalRevenue / it.totalTrips else 0.0,
                totalDistance = 0.0
            )
        },
        periodBreakdown = monthlyTrend.map { it.toPeriodBreakdown() },
        topVehicles = emptyList(), // Not in current API response
        paymentReceived = summary?.totalReceived ?: 0.0,
        paymentPending = summary?.totalPending ?: 0.0
    )
}
