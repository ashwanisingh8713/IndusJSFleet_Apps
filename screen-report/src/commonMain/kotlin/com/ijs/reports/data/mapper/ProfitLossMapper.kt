package com.ijs.reports.data.mapper

import com.ijs.reports.data.model.*
import com.ijs.reports.domain.entity.*

/**
 * Mapper for converting P&L DTOs to domain entities.
 * Extracted from ReportsRepositoryImpl to follow Clean Architecture's
 * dedicated mapper pattern and keep files focused.
 */
object ProfitLossMapper {

    fun toTripProfitLoss(dto: TripProfitLossDto) = TripProfitLoss(
        tripId = dto.tripId,
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber,
        driverId = dto.driverId,
        driverName = dto.driverName,
        startLocation = dto.startLocation,
        endLocation = dto.endLocation,
        scheduledDate = dto.scheduledDate,
        state = dto.state,
        purchasePrice = dto.purchasePrice,
        sellingValue = dto.sellingValue,
        totalTripCosts = dto.totalTripCosts,
        totalExpenses = dto.totalExpenses,
        grossProfit = dto.grossProfit,
        netProfit = dto.netProfit,
        profitMargin = dto.profitMargin,
        isProfitable = dto.isProfitable,
        costBreakdown = dto.costBreakdown?.map { toCostBreakdownItem(it) } ?: emptyList()
    )

    fun toVehicleProfitLoss(dto: VehicleProfitLossDto) = VehicleProfitLoss(
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber ?: dto.vehicleRegistration,
        make = dto.make,
        model = dto.model,
        period = dto.period?.let { "${it.startDate ?: ""} to ${it.endDate ?: ""}" },
        startDate = dto.startDate ?: dto.period?.startDate,
        endDate = dto.endDate ?: dto.period?.endDate,
        totalTrips = dto.totalTrips,
        completedTrips = dto.completedTrips,
        totalRevenue = dto.totalRevenue,
        totalTripCosts = dto.totalTripCosts,
        totalMaintenanceCosts = dto.totalMaintenanceCosts,
        totalExpenses = dto.totalExpenses,
        grossProfit = dto.grossProfit,
        netProfit = dto.netProfit,
        profitMargin = dto.profitMargin,
        isProfitable = dto.isProfitable,
        costBreakdown = dto.costBreakdown?.map { toCostBreakdownItem(it) } ?: emptyList(),
        tripSummary = dto.tripSummary?.map { toTripSummaryItem(it) } ?: emptyList()
    )

    fun toFleetProfitLoss(dto: FleetProfitLossDto) = FleetProfitLoss(
        period = dto.period,
        startDate = dto.startDate,
        endDate = dto.endDate,
        totalVehicles = dto.totalVehicles,
        totalTrips = dto.totalTrips,
        completedTrips = dto.completedTrips,
        totalRevenue = dto.totalRevenue,
        totalExpenses = dto.totalExpenses,
        totalTripCosts = dto.totalTripCosts,
        totalMaintenanceCosts = dto.totalMaintenanceCosts,
        grossProfit = dto.grossProfit,
        netProfit = dto.netProfit,
        profitMargin = dto.profitMargin,
        isProfitable = dto.isProfitable,
        vehicleBreakdown = dto.vehicleBreakdown?.map { toVehicleProfitLoss(it) } ?: emptyList(),
        costBreakdown = dto.costBreakdown?.map { toCostBreakdownItem(it) } ?: emptyList()
    )

    fun toCostBreakdownItem(dto: CostBreakdownItemDto) = CostBreakdownItem(
        costType = dto.costType,
        amount = dto.amount,
        count = dto.count,
        percentage = dto.percentage
    )

    fun toTripSummaryItem(dto: TripSummaryItemDto) = TripSummaryItem(
        tripId = dto.tripId,
        scheduledDate = dto.scheduledDate,
        startLocation = dto.startLocation,
        endLocation = dto.endLocation,
        revenue = dto.revenue,
        expenses = dto.expenses,
        profit = dto.profit,
        isProfitable = dto.isProfitable
    )

    fun toCostTypeAnalysis(dto: CostTypeAnalysisDto) = CostTypeAnalysis(
        costType = dto.costType,
        startDate = dto.startDate,
        endDate = dto.endDate,
        totalAmount = dto.totalAmount,
        totalCount = dto.totalCount,
        averagePerEntry = dto.averagePerEntry,
        vehicleBreakdown = dto.vehicleBreakdown?.map { toVehicleCostBreakdown(it) } ?: emptyList(),
        monthlyTrend = dto.monthlyTrend?.map { toMonthlyTrend(it) } ?: emptyList()
    )

    fun toVehicleCostBreakdown(dto: VehicleCostBreakdownDto) = VehicleCostBreakdown(
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber,
        amount = dto.amount,
        count = dto.count,
        percentage = dto.percentage
    )

    fun toMonthlyTrend(dto: MonthlyTrendDto) = MonthlyTrend(
        month = dto.month,
        year = dto.year,
        amount = dto.amount,
        count = dto.count
    )

    fun toConsolidatedPL(dto: ConsolidatedPLDto) = ConsolidatedPL(
        startDate = dto.startDate,
        endDate = dto.endDate,
        groupBy = dto.groupBy,
        totalRevenue = dto.totalRevenue,
        totalExpenses = dto.totalExpenses,
        netProfit = dto.netProfit,
        profitMargin = dto.profitMargin,
        isProfitable = dto.isProfitable,
        totalVehicles = dto.totalVehicles,
        totalTrips = dto.totalTrips,
        completedTrips = dto.completedTrips,
        vehicleSummary = dto.vehicleSummary?.map { toVehiclePLSummary(it) } ?: emptyList(),
        tripSummary = dto.tripSummary?.map { toTripPLSummary(it) } ?: emptyList(),
        costBreakdown = dto.costBreakdown?.map { toCostBreakdownItem(it) } ?: emptyList(),
        periodBreakdown = dto.periodBreakdown?.map { toPeriodBreakdown(it) } ?: emptyList()
    )

    fun toVehiclePLSummary(dto: VehiclePLSummaryDto) = VehiclePLSummary(
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber,
        revenue = dto.revenue,
        expenses = dto.expenses,
        profit = dto.profit,
        profitMargin = dto.profitMargin,
        isProfitable = dto.isProfitable,
        tripCount = dto.tripCount
    )

    fun toTripPLSummary(dto: TripPLSummaryDto) = TripPLSummary(
        tripId = dto.tripId,
        vehicleNumber = dto.vehicleNumber,
        route = dto.route,
        scheduledDate = dto.scheduledDate,
        revenue = dto.revenue,
        expenses = dto.expenses,
        profit = dto.profit,
        isProfitable = dto.isProfitable
    )

    fun toPeriodBreakdown(dto: PeriodBreakdownDto) = PeriodBreakdown(
        period = dto.period,
        label = dto.label,
        revenue = dto.revenue,
        expenses = dto.expenses,
        profit = dto.profit,
        tripCount = dto.tripCount,
        isProfitable = dto.isProfitable
    )

    fun toPLSummary(dto: PLSummaryDto) = PLSummary(
        startDate = dto.period?.startDate,
        endDate = dto.period?.endDate,
        totalRevenue = dto.overview?.totalRevenue ?: 0.0,
        totalExpenses = dto.overview?.totalExpenses ?: 0.0,
        grossProfit = dto.overview?.grossProfit ?: 0.0,
        profitMarginPercentage = dto.overview?.profitMarginPercentage ?: 0.0,
        status = dto.overview?.status ?: "neutral",
        isProfitable = dto.overview?.status == "profit",
        totalVehicles = dto.fleetSummary?.totalVehicles ?: 0,
        activeVehicles = dto.fleetSummary?.activeVehicles ?: 0,
        profitableVehicles = dto.fleetSummary?.profitableVehicles ?: 0,
        lossMakingVehicles = dto.fleetSummary?.lossMakingVehicles ?: 0,
        totalTrips = dto.tripSummary?.totalTrips ?: 0,
        completedTrips = dto.tripSummary?.completedTrips ?: 0,
        profitableTrips = dto.tripSummary?.profitableTrips ?: 0,
        lossMakingTrips = dto.tripSummary?.lossMakingTrips ?: 0,
        expenseBreakdown = dto.expenseBreakdown?.map { (type, item) ->
            ExpenseBreakdownItem(
                type = type,
                amount = item.amount,
                percentage = item.percentage
            )
        } ?: emptyList(),
        topPerformingVehicle = dto.topPerformers?.mostProfitableVehicle?.let { toVehiclePerformer(it) },
        lossMakingVehiclesList = dto.alerts?.lossMakingVehicles?.map { toVehiclePerformer(it) } ?: emptyList()
    )

    fun toVehiclePerformer(dto: PLVehiclePerformerDto) = VehiclePerformer(
        id = dto.id,
        registrationNumber = dto.registrationNumber,
        profit = dto.profit,
        loss = dto.loss
    )

    fun toPLAlert(dto: PLAlertDto) = PLAlert(
        type = dto.type,
        severity = dto.severity,
        message = dto.message,
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber
    )
}

