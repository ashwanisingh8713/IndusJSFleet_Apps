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
     * Map CustomerStatisticsDto to CustomerStatistics domain entity.
     */
    fun CustomerStatisticsDto.toDomain(): CustomerStatistics = CustomerStatistics(
        customerId = customerId.toString(),
        totalTrips = totalTrips,
        completedTrips = completedTrips,
        activeTrips = activeTrips,
        totalRevenue = totalRevenue,
        totalPendingPayment = totalPendingPayment,
        totalReceivedPayment = totalReceivedPayment,
        averageTripValue = averageTripValue,
        lastTripDate = lastTripDate
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
        vehicleRegistration = vehicleRegistration,
        startLocation = startLocation,
        endLocation = endLocation,
        tripDate = tripDate,
        tripPrice = expectedTripPrice,
        paidAmount = paidTripPrice,
        pendingAmount = pendingAmount,
        daysOverdue = daysOverdue,
        state = state,
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
        mode = PaymentMode.fromApiValue(paymentMode),
        status = paymentStatus,
        date = paymentDate,
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
        mode = PaymentMode.fromApiValue(mode),
        amount = amount,
        count = count,
        percentage = percentage
    )

    /**
     * Map MonthlyPaymentDto to MonthlyPayment domain entity.
     */
    fun MonthlyPaymentDto.toDomain(): MonthlyPayment = MonthlyPayment(
        month = month,
        year = year,
        amount = amount,
        count = count
    )

    /**
     * Map CustomerPaymentSummaryDto to CustomerPaymentSummary domain entity.
     */
    fun CustomerPaymentSummaryDto.toDomain(): CustomerPaymentSummary = CustomerPaymentSummary(
        byMode = byMode?.map { it.toDomain() } ?: emptyList(),
        byMonth = byMonth?.map { it.toDomain() } ?: emptyList(),
        totalTds = tdsSummary?.totalTds ?: 0.0,
        totalAmount = totalAmount,
        totalPayments = totalPayments
    )

    // ============= Financial Report Mapping =============

    /**
     * Map PeriodBreakdownDto to PeriodBreakdown domain entity.
     */
    fun PeriodBreakdownDto.toDomain(): PeriodBreakdown = PeriodBreakdown(
        period = period,
        revenue = revenue,
        costs = costs,
        profit = profit,
        trips = trips
    )

    /**
     * Map TopVehicleDto to TopVehicle domain entity.
     */
    fun TopVehicleDto.toDomain(): TopVehicle = TopVehicle(
        vehicleId = vehicleId.toString(),
        vehicleRegistration = vehicleRegistration,
        trips = trips,
        revenue = revenue,
        costs = costs,
        profit = profit
    )

    /**
     * Map FinancialTripSummaryDto to FinancialTripSummary domain entity.
     */
    fun FinancialTripSummaryDto.toDomain(): FinancialTripSummary = FinancialTripSummary(
        totalTrips = totalTrips,
        completedTrips = completedTrips,
        averageTripValue = averageTripValue,
        totalDistance = totalDistance
    )

    /**
     * Map CustomerFinancialReportDto to CustomerFinancialReport domain entity.
     */
    fun CustomerFinancialReportDto.toDomain(): CustomerFinancialReport = CustomerFinancialReport(
        customerId = customerId.toString(),
        customerName = customerName,
        period = FinancialPeriod.fromApiValue(period),
        startDate = startDate,
        endDate = endDate,
        totalRevenue = totalRevenue,
        totalCosts = totalCosts,
        netProfit = netProfit,
        profitMargin = profitMargin,
        tripSummary = tripSummary?.toDomain(),
        periodBreakdown = periodBreakdown?.map { it.toDomain() } ?: emptyList(),
        topVehicles = topVehicles?.map { it.toDomain() } ?: emptyList(),
        paymentReceived = paymentReceived,
        paymentPending = paymentPending
    )
}
