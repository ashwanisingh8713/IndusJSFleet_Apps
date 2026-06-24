package com.ijs.customer.data.mapper

import com.ijs.customer.data.model.*
import com.ijs.customer.domain.entity.*

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
     * Map list of CustomerDto to list of Customer domain entities.
     */
    fun List<CustomerDto>.toDomainList(): List<Customer> = map { it.toDomain() }

    /**
     * Map CustomerStatisticsDataDto to CustomerStatistics domain entity.
     * Matches backend statistics response (summary / trips_by_state / ...).
     */
    fun CustomerStatisticsDataDto.toDomain(): CustomerStatistics = CustomerStatistics(
        customerId = "",
        totalTrips = summary?.totalTrips ?: 0,
        completedTrips = summary?.completedTrips ?: 0,
        cancelledTrips = summary?.cancelledTrips ?: 0,
        activeTrips = summary?.activeTrips ?: 0,
        plannedTrips = summary?.plannedTrips ?: 0,
        totalRevenue = summary?.totalRevenue ?: 0.0,
        totalPendingPayment = summary?.totalPending ?: 0.0,
        totalReceivedPayment = summary?.totalPaid ?: 0.0,
        collectionRate = summary?.collectionRate ?: 0.0,
        totalTripCosts = summary?.totalTripCosts ?: 0.0,
        netProfit = summary?.netProfit ?: 0.0
    )

    /**
     * Map list of CustomerDto to list of CustomerSummary.
     */
    fun List<CustomerDto>.toSummaryList(): List<CustomerSummary> = map { it.toSummary() }

    /**
     * Alias for backward compatibility after local data source returns DTOs.
     * Previously operated on List<CustomerEntity>.
     */
    fun List<CustomerDto>.entityListToDomainList(): List<Customer> = toDomainList()

    /**
     * Alias for backward compatibility after local data source returns DTOs.
     * Previously operated on List<CustomerEntity>.
     */
    fun List<CustomerDto>.entityListToSummaryList(): List<CustomerSummary> = toSummaryList()

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
        val monthNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val monthName = com.indusjs.datetimeutils.FleetDateTime.getMonthNameShort(monthNum)
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
        // "Total Received" header must match the sum of the per-mode rows (net); TDS shown separately above.
        totalAmount = totals?.netAmount ?: 0.0,
        totalPayments = byMode.sumOf { it.count }
    )

    // ============= Financial Report Mapping =============

    /**
     * Map FinancialPeriodBreakdownDto to PeriodBreakdown domain entity.
     * Backend gives per-period trip costs (driver costs excluded, per P&L) and
     * profit (= revenue − costs) alongside revenue/collected; map them through so
     * the column shows real profit.
     */
    fun FinancialPeriodBreakdownDto.toPeriodBreakdown(): PeriodBreakdown = PeriodBreakdown(
        period = period ?: "",
        revenue = revenue,
        costs = costs,
        profit = profit,
        trips = trips
    )

    /**
     * Map FinancialTopVehicleDto to TopVehicle domain entity.
     */
    fun FinancialTopVehicleDto.toDomain(): TopVehicle = TopVehicle(
        vehicleId = vehicleId.toString(),
        vehicleRegistration = registrationNumber ?: "-",
        trips = tripCount,
        revenue = totalRevenue,
        costs = 0.0,
        profit = 0.0
    )

    /**
     * Map CustomerFinancialReportDataDto to CustomerFinancialReport domain entity.
     * Matches backend financial-report response shape.
     */
    fun CustomerFinancialReportDataDto.toDomain(): CustomerFinancialReport = CustomerFinancialReport(
        customerId = customer?.id?.toString() ?: "",
        customerName = customer?.companyName ?: customer?.personName,
        period = FinancialPeriod.MONTHLY,
        startDate = startDate,
        endDate = endDate,
        // Display the BILLED revenue (total_revenue = SUM selling_value) so it lines up
        // with net_profit/net_margin; fall back to total_expected (the quote) on older responses.
        totalRevenue = revenueSummary?.totalRevenue?.takeIf { it != 0.0 } ?: revenueSummary?.totalExpected ?: 0.0,
        totalCosts = costSummary?.totalTripCosts ?: 0.0,
        totalDriverCosts = costSummary?.totalDriverCosts ?: 0.0,
        // Driver costs are EXCLUDED from the P&L, so NET == GROSS profit (both are
        // BILLED total_revenue − total_trip_costs); total_driver_costs is 0.
        grossProfit = profitLoss?.grossProfit ?: 0.0,
        netProfit = profitLoss?.netProfit ?: 0.0,
        profitMargin = profitLoss?.profitMargin ?: 0.0,
        netMargin = profitLoss?.netMargin ?: 0.0,
        tripSummary = tripSummary?.let {
            FinancialTripSummary(
                totalTrips = it.totalTrips,
                completedTrips = it.completedTrips,
                averageTripValue = if (it.totalTrips > 0) {
                    (revenueSummary?.totalExpected ?: 0.0) / it.totalTrips
                } else 0.0,
                totalDistance = 0.0
            )
        },
        periodBreakdown = periodBreakdown.map { it.toPeriodBreakdown() },
        topVehicles = topVehicles.map { it.toDomain() },
        paymentReceived = revenueSummary?.totalReceived ?: 0.0,
        paymentPending = revenueSummary?.totalPending ?: 0.0
    )
}
