package com.ijs.reports.data.mapper

import com.ijs.reports.data.model.*
import com.ijs.reports.domain.entity.*
import com.indusjs.datetimeutils.FleetEpoch

/**
 * Mapper for converting P&L DTOs to domain entities.
 * Extracted from ReportsRepositoryImpl to follow Clean Architecture's
 * dedicated mapper pattern and keep files focused.
 */
object ProfitLossMapper {

    /**
     * Builds a human-readable "start to end" period label from two epoch-millis
     * instants. Returns null when neither bound is present (treating 0 as unset).
     */
    private fun periodLabel(startMs: Long?, endMs: Long?): String? {
        val start = startMs?.takeIf { it != 0L }
        val end = endMs?.takeIf { it != 0L }
        if (start == null && end == null) return null
        val startStr = FleetEpoch.toDisplayDate(start) ?: ""
        val endStr = FleetEpoch.toDisplayDate(end) ?: ""
        return "$startStr to $endStr"
    }

    fun toTripProfitLoss(dto: TripProfitLossDto) = TripProfitLoss(
        tripId = dto.tripId,
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber ?: dto.vehicleRegistration,
        driverId = dto.driverId,
        driverName = dto.driverName,
        customerId = dto.customerId,
        customerName = dto.customerName,
        startLocation = dto.startLocation,
        endLocation = dto.endLocation,
        scheduledDate = dto.scheduledDate ?: dto.tripDate,
        // `state` is the trip lifecycle state; the P&L `status` (profit/loss/
        // break_even) flows to plStatus, not state. Keep state from the trip
        // lifecycle field only (don't overwrite it with the P&L status).
        state = dto.state,
        // Multi-trip P&L status: "profit" | "loss" | "break_even" (null single-trip).
        plStatus = dto.status,
        purchasePrice = dto.purchasePrice,
        sellingValue = dto.sellingValue.takeIf { it != 0.0 } ?: dto.tripPrice,
        totalTripCosts = dto.totalTripCosts,
        // API may return total_cost OR total_expenses — use whichever is non-zero
        totalExpenses = dto.totalExpenses.takeIf { it != 0.0 } ?: dto.totalCost,
        // API may return gross_profit OR net_profit — use whichever is non-zero
        grossProfit = dto.grossProfit.takeIf { it != 0.0 } ?: dto.netProfit,
        netProfit = dto.netProfit.takeIf { it != 0.0 } ?: dto.grossProfit,
        profitMargin = dto.profitMargin,
        // Drive profitability off the backend status when present: only "profit"
        // counts as profitable (break_even and loss do not). Fall back to the
        // explicit flag / net sign when the backend sends no status.
        isProfitable = when (dto.status) {
            "profit" -> true
            "loss", "break_even" -> false
            else -> dto.isProfitable || dto.netProfit > 0 || dto.grossProfit > 0
        },
        costBreakdown = dto.costBreakdown?.map { toCostBreakdownItem(it) } ?: emptyList()
    )

    fun toVehicleProfitLoss(dto: VehicleProfitLossDto) = VehicleProfitLoss(
        vehicleId = dto.vehicleId,
        vehicleNumber = dto.vehicleNumber ?: dto.vehicleRegistration,
        make = dto.make,
        model = dto.model,
        period = dto.period?.let { periodLabel(it.startDate, it.endDate) },
        startDate = (dto.startDate ?: dto.period?.startDate)?.takeIf { it != 0L },
        endDate = (dto.endDate ?: dto.period?.endDate)?.takeIf { it != 0L },
        totalTrips = dto.totalTrips,
        completedTrips = dto.completedTrips,
        totalRevenue = dto.totalRevenue,
        totalTripCosts = dto.totalTripCosts,
        totalMaintenanceCosts = dto.totalMaintenanceCosts,
        // API may return total_cost OR total_expenses — use whichever is non-zero
        totalExpenses = dto.totalExpenses.takeIf { it != 0.0 } ?: dto.totalCost,
        // API may return gross_profit OR net_profit — use whichever is non-zero
        grossProfit = dto.grossProfit.takeIf { it != 0.0 } ?: dto.netProfit,
        netProfit = dto.netProfit.takeIf { it != 0.0 } ?: dto.grossProfit,
        profitMargin = dto.profitMargin,
        // API may return is_profitable or we derive it from profit_status or net_profit
        isProfitable = dto.isProfitable
                || dto.profitStatus == "profit"
                || dto.profitStatus == "highly_profitable"
                || (dto.netProfit > 0 && !dto.isProfitable),
        costBreakdown = dto.costBreakdown?.map { toCostBreakdownItem(it) } ?: emptyList(),
        tripSummary = dto.tripSummary?.map { toTripSummaryItem(it) } ?: emptyList()
    )

    fun toFleetProfitLoss(dto: FleetProfitLossDto): FleetProfitLoss {
        val s = dto.fleetSummary
        val revenue = s?.totalRevenue ?: 0.0
        val expenses = s?.totalCost ?: 0.0
        val netProfit = s?.netProfit ?: 0.0
        return FleetProfitLoss(
            // Backend sends start_date/end_date as epoch-millis alongside the period name.
            period = periodLabel(dto.startDate, dto.endDate),
            startDate = dto.startDate?.takeIf { it != 0L },
            endDate = dto.endDate?.takeIf { it != 0L },
            // Backend emits active_vehicles + vehicle_count, not total_vehicles.
            totalVehicles = dto.vehicleCount.takeIf { it != 0 } ?: (s?.activeVehicles ?: 0),
            totalTrips = s?.totalTrips ?: 0,
            // Backend FleetTotal now sends completed_trips.
            completedTrips = s?.completedTrips ?: 0,
            totalRevenue = revenue,
            totalExpenses = expenses,
            // Backend no longer splits trip vs maintenance at fleet level.
            totalTripCosts = 0.0,
            totalMaintenanceCosts = 0.0,
            // Fleet P&L is an opex model with NO gross profit (the consolidated
            // endpoint is the gross/COGS source). Don't fake grossProfit = netProfit.
            grossProfit = 0.0,
            netProfit = netProfit,
            // Backend FleetTotal now sends profit_margin directly; use it instead of deriving.
            profitMargin = s?.profitMargin ?: 0.0,
            isProfitable = netProfit > 0.0,
            vehicleBreakdown = dto.vehicles?.map { toVehicleProfitLoss(it) } ?: emptyList(),
            // Fleet P&L response has no fleet-level cost_breakdown array.
            costBreakdown = emptyList()
        )
    }

    fun toCostBreakdownItem(dto: CostBreakdownItemDto) = CostBreakdownItem(
        // Backend cost_breakdown carries cost_label / cost_id, not cost_type.
        costType = dto.displayLabel,
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

    /**
     * Maps a backend CostTypeProfitLoss to the domain entity.
     * @param startDate/@param endDate come from the wrapper's `period`, since the
     * backend cost-type analysis object itself carries no dates.
     */
    fun toCostTypeAnalysis(
        dto: CostTypeAnalysisDto,
        startDate: Long? = null,
        endDate: Long? = null
    ) = CostTypeAnalysis(
        // Stable id for icon matching; falls back to legacy cost_type then label.
        costType = dto.costId ?: dto.costType ?: dto.costLabel ?: "",
        startDate = startDate,
        endDate = endDate,
        totalAmount = dto.totalAmount,
        // Backend renamed total_count -> transaction_count.
        totalCount = dto.transactionCount,
        // Backend has no average_per_entry; use statistics.average_per_trip.
        averagePerEntry = dto.statistics?.averagePerTrip ?: 0.0,
        vehicleBreakdown = dto.byVehicle?.map { toVehicleCostBreakdown(it) } ?: emptyList(),
        monthlyTrend = dto.byMonth?.map { toMonthlyTrend(it) } ?: emptyList()
    )

    fun toVehicleCostBreakdown(dto: VehicleCostBreakdownDto) = VehicleCostBreakdown(
        vehicleId = dto.vehicleId,
        // Backend renamed vehicle_number -> registration_number here.
        vehicleNumber = dto.registrationNumber,
        amount = dto.amount,
        count = dto.count,
        // Backend by_vehicle has no percentage field.
        percentage = 0.0
    )

    fun toMonthlyTrend(dto: MonthlyTrendDto) = MonthlyTrend(
        month = dto.month ?: "",
        // Backend by_month carries "YYYY-MM" in month; no separate year field.
        year = 0,
        amount = dto.amount,
        count = dto.count
    )

    fun toConsolidatedPL(dto: ConsolidatedPLDto): ConsolidatedPL {
        val pl = dto.profitLoss
        val isProfit = (pl?.status == "profit") || ((pl?.netProfit ?: 0.0) >= 0.0 && (pl?.netProfit ?: 0.0) != 0.0)
        return ConsolidatedPL(
            startDate = dto.period?.startDate?.takeIf { it != 0L },
            endDate = dto.period?.endDate?.takeIf { it != 0L },
            // Backend no longer echoes group_by in the response.
            groupBy = null,
            totalRevenue = dto.revenue?.totalRevenue ?: 0.0,
            totalExpenses = dto.expenses?.totalExpenses ?: 0.0,
            netProfit = pl?.netProfit ?: 0.0,
            profitMargin = pl?.profitMarginPercentage ?: 0.0,
            isProfitable = isProfit,
            // completed_trips is the only true count on the consolidated response; vehicles/trips
            // filters_applied are filter-INPUT counts (0 when unfiltered), so prefer real sources.
            totalVehicles = (pl?.byVehicle?.size ?: 0).takeIf { it > 0 } ?: (dto.filtersApplied?.vehicles ?: 0),
            totalTrips = (dto.completedTrips).takeIf { it > 0 } ?: (dto.filtersApplied?.trips ?: 0),
            completedTrips = dto.completedTrips,
            // Per-vehicle rows come from profit_loss.by_vehicle (richest source).
            vehicleSummary = pl?.byVehicle?.map { toVehiclePLSummaryFromVehicle(it) } ?: emptyList(),
            // Backend consolidated report carries no per-trip rows.
            tripSummary = emptyList(),
            // Expense breakdown is a {cost_id -> amount} map on the backend.
            costBreakdown = dto.expenses?.byCostType?.map { (costId, amount) ->
                CostBreakdownItem(costType = costId, amount = amount)
            } ?: emptyList(),
            // Period rows come from profit_loss.by_period.
            periodBreakdown = pl?.byPeriod?.map { toPeriodBreakdownFromPL(it) } ?: emptyList()
        )
    }

    private fun toVehiclePLSummaryFromVehicle(dto: VehicleProfitLossDto): VehiclePLSummary {
        val expenses = dto.totalExpenses.takeIf { it != 0.0 } ?: dto.totalCost
        val profit = dto.netProfit.takeIf { it != 0.0 } ?: dto.grossProfit
        return VehiclePLSummary(
            vehicleId = dto.vehicleId,
            vehicleNumber = dto.vehicleNumber ?: dto.vehicleRegistration,
            revenue = dto.totalRevenue,
            expenses = expenses,
            profit = profit,
            profitMargin = dto.profitMargin,
            isProfitable = profit >= 0.0 && profit != 0.0,
            tripCount = dto.totalTrips
        )
    }

    private fun toPeriodBreakdownFromPL(dto: PeriodProfitLossDto) = PeriodBreakdown(
        period = dto.period ?: "",
        label = null,
        revenue = dto.revenue,
        expenses = dto.expenses,
        profit = dto.profitLoss,
        tripCount = 0,
        isProfitable = (dto.status == "profit") || dto.profitLoss >= 0.0
    )

    fun toPLSummary(dto: PLSummaryDto) : PLSummary {
        // Use financialOverview which handles both "overview" and "summary" keys
        val overview = dto.financialOverview
        return PLSummary(
            startDate = dto.period?.startDate,
            endDate = dto.period?.endDate,
            totalRevenue = overview?.totalRevenue ?: 0.0,
            totalExpenses = overview?.totalExpenses ?: 0.0,
            grossProfit = overview?.effectiveProfit ?: 0.0,
            // Net profit = backend net_profit (revenue − all opex). Falls back to
            // revenue−expenses if a legacy response omits it. Must NOT use gross_profit
            // (which == revenue when COGS is 0) for the "Net Profit" headline.
            netProfit = overview?.netProfit?.takeIf { it != 0.0 }
                ?: ((overview?.totalRevenue ?: 0.0) - (overview?.totalExpenses ?: 0.0)),
            profitMarginPercentage = overview?.effectiveMargin ?: 0.0,
            status = overview?.effectiveStatus ?: "neutral",
            isProfitable = overview?.effectiveIsProfitable ?: false,
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
    }

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

    fun toCustomerPLReport(dto: CustomerPLReportDto) = CustomerPLReport(
        period = dto.period,
        startDate = dto.startDate,
        endDate = dto.endDate,
        summary = dto.summary?.let {
            CustomerPLSummary(
                totalTrips = it.totalTrips,
                totalRevenue = it.totalRevenue,
                totalCost = it.totalCost,
                netProfit = it.netProfit,
                totalPaid = it.totalPaid,
                totalPending = it.totalPending,
                activeCustomers = it.activeCustomers
            )
        } ?: CustomerPLSummary(),
        customers = dto.customers?.map { toCustomerPLItem(it) } ?: emptyList()
    )

    fun toCustomerPLItem(dto: CustomerPLItemDto) = CustomerPLItem(
        customerId = dto.customerId,
        customerName = dto.customerName ?: "",
        totalTrips = dto.totalTrips,
        totalRevenue = dto.totalRevenue,
        totalCost = dto.totalCost,
        fuelCost = dto.fuelCost,
        otherCost = dto.otherCost,
        netProfit = dto.netProfit,
        profitMargin = dto.profitMargin,
        totalPaid = dto.totalPaid,
        totalPending = dto.totalPending,
        collectionRate = dto.collectionRate,
        avgProfitPerTrip = dto.avgProfitPerTrip
    )
}

