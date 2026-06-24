package com.indusjs.fleet.data.mapper.dashboard

import com.indusjs.fleet.data.model.dashboard.AlertDto
import com.indusjs.fleet.data.model.dashboard.AlertsStatusDto
import com.indusjs.fleet.data.model.dashboard.CostBreakdownItemDto
import com.indusjs.fleet.data.model.dashboard.CostOverviewDto
import com.indusjs.fleet.data.model.dashboard.DashboardDataDto
import com.indusjs.fleet.data.model.dashboard.DocumentStatsDto
import com.indusjs.fleet.data.model.dashboard.LiveStatusDto
import com.indusjs.fleet.data.model.dashboard.LiveVehicleDto
import com.indusjs.fleet.data.model.dashboard.OngoingTripDto
import com.indusjs.fleet.data.model.dashboard.QuickActionsDto
import com.indusjs.fleet.data.model.dashboard.TeamStatsDto
import com.indusjs.fleet.data.model.dashboard.UserInfoDto
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertPriority
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.CostBreakdownItem
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DashboardUserInfo
import com.indusjs.fleet.domain.entity.dashboard.DocumentStats
import com.indusjs.fleet.domain.entity.dashboard.LiveStatus
import com.indusjs.fleet.domain.entity.dashboard.LiveVehicle
import com.indusjs.fleet.domain.entity.dashboard.OngoingTrip
import com.indusjs.fleet.domain.entity.dashboard.QuickActions
import com.indusjs.fleet.domain.entity.dashboard.TeamStats
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.fleet.core.util.formatDateTimeForDisplay
import com.indusjs.fleet.core.util.formatLastUpdated

/**
 * Mapper for dashboard DTOs to domain entities.
 *
 * Wire timestamps are UTC epoch-millis (Long). The dashboard domain entities
 * keep their timestamp fields as display Strings (their consumers render them
 * directly), so we format here at the DTO -> domain boundary.
 */
object DashboardMapper {

    fun DashboardDataDto.toDomain(): DashboardStats {
        return DashboardStats(
            // Fleet Overview
            totalVehicles = fleetOverview.totalVehicles,
            activeVehicles = fleetOverview.activeVehicles,
            maintenanceVehicles = fleetOverview.maintenanceVehicles,
            inactiveVehicles = fleetOverview.inactiveVehicles,
            totalDrivers = fleetOverview.totalDrivers,
            activeDrivers = fleetOverview.activeDrivers,
            driversOnTrip = fleetOverview.driversOnTrip,
            driversOnLeave = fleetOverview.driversOnLeave,
            totalTrips = fleetOverview.totalTrips,
            ongoingTrips = fleetOverview.ongoingTrips,
            plannedTrips = fleetOverview.plannedTrips,
            completedTrips = fleetOverview.completedTrips,
            totalDistance = fleetOverview.totalDistance,

            // Today's Summary
            completedTripsToday = todaySummary.completedTripsToday,
            totalDistanceToday = todaySummary.totalDistanceToday,
            fuelConsumption = todaySummary.fuelConsumption,
            totalFuelFilled = todaySummary.totalFuelFilled,
            totalFuelUsed = todaySummary.totalFuelUsed,
            totalFuelCost = todaySummary.totalFuelCost,
            activeVehiclesNow = todaySummary.activeVehiclesNow,
            newTripsToday = todaySummary.newTripsToday,
            alertsCount = todaySummary.alertsCount,

            // Alerts
            alerts = alerts.map { it.toDomain() },
            totalAlerts = totalAlerts,

            // Quick Actions
            quickActions = quickActions.toDomain(),

            // Live Status
            liveStatus = liveStatus.toDomain(),

            // Team Stats
            teamStats = teamStats?.toDomain(),

            // Document Stats
            documentStats = documentStats?.toDomain(),

            // Metadata
            lastUpdated = formatLastUpdated(lastUpdated)
        )
    }

    fun UserInfoDto.toDomain(): DashboardUserInfo {
        return DashboardUserInfo(
            id = id,
            firstName = firstName,
            lastName = lastName,
            role = role,
            email = email
        )
    }

    fun AlertDto.toDomain(): Alert {
        return Alert(
            id = id,
            type = AlertType.fromString(type),
            priority = AlertPriority.fromString(priority),
            title = title,
            message = message,
            entityType = entityType,
            entityId = entityId,
            entityName = entityName,
            createdAt = formatDateTimeForDisplay(createdAt),
            timestamp = createdAt ?: 0L,
            // NEW: Enhanced alert fields
            vehicleId = vehicleId,
            vehicleRegistrationNumber = vehicleRegistrationNumber,
            driverId = driverId,
            driverName = driverName,
            daysUntilExpiry = daysUntilExpiry,
            expiryDate = formatDateToHumanReadable(expiryDate)
        )
    }

    fun QuickActionsDto.toDomain(): QuickActions {
        return QuickActions(
            vehiclesCount = vehiclesCount,
            driversCount = driversCount,
            tripsCount = tripsCount,
            alertsCount = alertsCount
        )
    }

    fun LiveStatusDto.toDomain(): LiveStatus {
        return LiveStatus(
            liveTrackingVehicles = liveTrackingVehicles,
            ongoingTripsCount = ongoingTripsCount,
            driversOnTrip = driversOnTrip,
            ongoingTrips = ongoingTrips.map { it.toDomain() },
            liveVehicles = liveVehicles.map { it.toDomain() }
        )
    }

    fun OngoingTripDto.toDomain(): OngoingTrip {
        return OngoingTrip(
            tripId = tripId,
            vehicleRegistration = vehicleRegistration,
            driverName = driverName,
            startLocation = startLocation,
            endLocation = endLocation,
            status = status,
            startedAt = formatDateTimeForDisplay(startedAt)
        )
    }

    fun LiveVehicleDto.toDomain(): LiveVehicle {
        return LiveVehicle(
            vehicleId = vehicleId,
            registrationNumber = registrationNumber,
            latitude = latitude,
            longitude = longitude,
            speed = speed,
            lastUpdated = formatLastUpdated(lastUpdated),
            driverName = driverName,
            tripId = tripId
        )
    }

    fun TeamStatsDto.toDomain(): TeamStats {
        return TeamStats(
            totalMembers = totalMembers
        )
    }

    fun DocumentStatsDto.toDomain(): DocumentStats {
        return DocumentStats(
            totalDocuments = totalDocuments,
            expiringDocuments = expiringDocuments,
            expiredDocuments = expiredDocuments
        )
    }

    // Cost Overview mapper with detailed breakdowns.
    // Backend reports total_profit/total_loss/net_profit_loss; the UI renders a
    // single signed profit/loss figure, so derive profitLoss/isProfit here.
    // `filter` is the requested filter; the wire payload carries `period`.
    fun CostOverviewDto.toDomain(): CostOverview {
        return CostOverview(
            filter = period,
            periodLabel = "",
            totalExpenses = totalExpenses,
            totalRevenue = totalRevenue,
            totalProfit = totalProfit,
            totalLoss = totalLoss,
            netProfitLoss = netProfitLoss,
            profitLoss = netProfitLoss,
            isProfit = netProfitLoss >= 0.0,
            completedTrips = completedTrips,
            fuelExpenses = fuelExpenses,
            tollExpenses = tollExpenses,
            maintenanceExpenses = maintenanceExpenses,
            otherExpenses = otherExpenses,
            pendingPayments = pendingPayments,
            receivedPayments = receivedPayments,
            // Detailed trip-cost expense breakdowns
            driverAllowanceExpenses = driverAllowanceExpenses,
            parkingExpenses = parkingExpenses,
            loadingCharges = loadingCharges,
            unloadingCharges = unloadingCharges,
            chalanExpenses = chalanExpenses,
            permitExpenses = permitExpenses,
            insuranceExpenses = insuranceExpenses,
            tripCostBreakdown = tripCostBreakdown.map { it.toDomain() },
            maintenanceCostBreakdown = maintenanceCostBreakdown.map { it.toDomain() }
        )
    }

    fun CostBreakdownItemDto.toDomain(): CostBreakdownItem {
        return CostBreakdownItem(
            costId = costId,
            costLabel = costLabel,
            groupId = groupId,
            amount = amount,
            count = count
        )
    }

    // NEW: Alerts Status mapper with detailed counts
    fun AlertsStatusDto.toDomain(): AlertsSummary {
        return AlertsSummary(
            totalAlerts = totalAlerts,
            criticalAlerts = criticalAlerts,
            warningAlerts = warningAlerts,
            infoAlerts = infoAlerts,
            documentExpiring = documentExpiring,
            licenseExpiring = licenseExpiring,
            pendingPayments = pendingPayments,
            maintenanceVehicles = maintenanceVehicles,
            // NEW: Detailed expiry counts
            documentExpired = documentExpired,
            documentExpiring7Days = documentExpiring7Days,
            documentExpiring30Days = documentExpiring30Days,
            licenseExpired = licenseExpired,
            licenseExpiring7Days = licenseExpiring7Days,
            licenseExpiring30Days = licenseExpiring30Days,
            maintenanceVehiclesCount = maintenanceVehiclesCount,
            alerts = alerts.map { it.toDomain() }
        )
    }
}
