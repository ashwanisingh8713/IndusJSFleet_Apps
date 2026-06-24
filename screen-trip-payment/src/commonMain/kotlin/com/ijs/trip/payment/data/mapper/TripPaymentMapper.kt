package com.ijs.trip.payment.data.mapper

import com.ijs.trip.payment.data.model.*
import com.ijs.trip.payment.domain.entity.*
import com.indusjs.datetimeutils.FleetEpoch
/**
 * Mapper for trip payment DTOs to domain entities.
 */
object TripPaymentMapper {

    fun TripPaymentDto.toDomain(): TripPayment {
        // Use effectiveTripId which handles both flat and nested response
        val resolvedTripId = effectiveTripId

        // Resolve customer name from multiple sources (payment level, customer.person_name, then fallbacks)
        // Priority: customerName (flat) → customer.personName → customer.companyName → trip.customerName
        val resolvedCustomerName = customerName?.ifBlank { null }
            ?: customer?.personName?.ifBlank { null }
            ?: customer?.companyName?.ifBlank { null }
            ?: customerCompany?.ifBlank { null }
            ?: trip?.customerName?.ifBlank { null }

        // Resolve customer company from customer object
        val resolvedCustomerCompany = customerCompany?.ifBlank { null }
            ?: customer?.companyName?.ifBlank { null }

        // Resolve vehicle info from nested objects
        val resolvedVehicleId = vehicleId?.toString() ?: vehicle?.id?.toString() ?: trip?.vehicleId?.toString()
        val resolvedDriverId = driverId?.toString() ?: driver?.id?.toString() ?: trip?.driverId?.toString()
        val resolvedCustomerId = customerId?.toString() ?: customer?.id?.toString()

        // Mapping payment id=$id: tripId=$resolvedTripId, customerName='$resolvedCustomerName'

        return TripPayment(
            id = id.toString(),
            tripId = resolvedTripId.toString(),
            vehicleId = resolvedVehicleId,
            driverId = resolvedDriverId,
            customerId = resolvedCustomerId,
            // Use resolved customer info - personName for name, companyName for company
            customerName = resolvedCustomerName,
            customerContact = customerContact?.ifBlank { null } ?: customer?.primaryContact?.ifBlank { null },
            customerCompany = resolvedCustomerCompany,
            customerGst = customerGst?.ifBlank { null } ?: customer?.gstNumber?.ifBlank { null },
            amount = amount,
            tdsAmount = tdsAmount,
            discountAmount = discountAmount,
            netAmount = netAmount,
            paymentType = PaymentType.fromApiValue(paymentType),
            paymentMode = PaymentMode.fromApiValue(paymentMode),
            paymentSource = paymentSource,
            paymentDate = paymentDate,
            paymentStatus = PaymentStatus.fromApiValue(paymentStatus),
            transactionId = transactionId,
            bankName = bankName,
            receiptNumber = receiptNumber,
            financialYear = financialYear,
            financialMonth = financialMonth,
            notes = notes,
            receivedBy = receivedBy,
            receivedAtLocation = receivedAtLocation,
            ownerId = ownerId?.toString(),
            createdById = createdBy?.toString(),
            createdByName = createdByUser?.fullName,
            createdAt = createdAt,
            updatedAt = updatedAt,
            // Build tripInfo from flat fields (list response) OR nested objects (detail response)
            tripInfo = buildTripInfo()
        )
    }

    /**
     * Build TripPaymentTripInfo from flat fields (list response) with fallback to nested objects (detail response).
     * Uses ifBlank to handle empty strings from API.
     */
    private fun TripPaymentDto.buildTripInfo(): TripPaymentTripInfo = TripPaymentTripInfo(
        tripId = effectiveTripId.toString(),
        // Use flat field first, fallback to nested vehicle object, then trip.vehicle
        vehicleRegistration = vehicleRegistrationNumber?.ifBlank { null }
            ?: vehicle?.registrationNumber?.ifBlank { null }
            ?: trip?.vehicle?.registrationNumber?.ifBlank { null }
            ?: trip?.vehicleRegistration?.ifBlank { null },
        vehicleMake = vehicle?.make?.ifBlank { null } ?: trip?.vehicle?.make?.ifBlank { null },
        vehicleModel = vehicle?.model?.ifBlank { null } ?: trip?.vehicle?.model?.ifBlank { null },
        // Driver name from nested driver object first
        driverName = driver?.fullName?.ifBlank { null }
            ?: trip?.driver?.fullName?.ifBlank { null }
            ?: trip?.driverName?.ifBlank { null },
        // Customer name - prefer personName, fallback to companyName
        customerName = customerName?.ifBlank { null }
            ?: customer?.personName?.ifBlank { null }
            ?: customer?.companyName?.ifBlank { null }
            ?: customerCompany?.ifBlank { null }
            ?: trip?.customerName?.ifBlank { null },
        // Use flat fields first, fallback to nested
        startLocation = tripStartLocation?.ifBlank { null }
            ?: trip?.startLocation?.ifBlank { null },
        endLocation = tripEndLocation?.ifBlank { null }
            ?: trip?.endLocation?.ifBlank { null },
        tripPrice = trip?.expectedTripPrice,
        tripState = tripState?.ifBlank { null }
            ?: trip?.state?.ifBlank { null },
        // Extract dates - use flat fields first, then trip object fields (epoch ms → DD-MM-YYYY)
        tripStartDate = extractDisplayDate(
            tripScheduledDate ?: tripStartTime
                ?: trip?.scheduledDate ?: trip?.startTime
                ?: trip?.plannedStart
        ),
        tripEndDate = extractDisplayDate(
            tripDeliveryDate ?: tripDeliveryTime
                ?: trip?.deliveryDate ?: trip?.deliveryTime
                ?: trip?.plannedEnd
        ),
        // Extract times - use flat fields first (epoch ms → HH:mm)
        tripStartTime = extractDisplayTime(tripStartTime ?: trip?.startTime ?: trip?.plannedStart),
        tripEndTime = extractDisplayTime(tripDeliveryTime ?: trip?.deliveryTime ?: trip?.plannedEnd)
    )

    /**
     * Legacy mapper for nested trip object (for detail response).
     */
    fun TripPaymentTripInfoDto.toDomain(paymentCustomerName: String? = null): TripPaymentTripInfo = TripPaymentTripInfo(
        tripId = id?.toString(),
        vehicleRegistration = vehicle?.registrationNumber?.ifBlank { null }
            ?: vehicleRegistration?.ifBlank { null },
        vehicleMake = vehicle?.make?.ifBlank { null },
        vehicleModel = vehicle?.model?.ifBlank { null },
        driverName = driver?.fullName?.ifBlank { null }
            ?: driverName?.ifBlank { null },
        customerName = customerName?.ifBlank { null }
            ?: paymentCustomerName?.ifBlank { null },
        startLocation = startLocation?.ifBlank { null },
        endLocation = endLocation?.ifBlank { null },
        tripPrice = expectedTripPrice,
        tripState = state?.ifBlank { null },
        // Extract date from plannedStart or scheduledDate (epoch ms → DD-MM-YYYY)
        tripStartDate = extractDisplayDate(plannedStart ?: scheduledDate),
        tripEndDate = extractDisplayDate(plannedEnd ?: deliveryDate),
        // Extract time from plannedStart/plannedEnd (epoch ms → HH:mm)
        tripStartTime = extractDisplayTime(plannedStart),
        tripEndTime = extractDisplayTime(plannedEnd)
    )

    /**
     * Convert a UTC epoch-millis timestamp to a DD-MM-YYYY display date (device zone).
     * Treats null/0 as "unset" and returns null.
     */
    private fun extractDisplayDate(ms: Long?): String? {
        if (ms == null || ms == 0L) return null
        return FleetEpoch.toDisplayDate(ms)
    }

    /**
     * Convert a UTC epoch-millis timestamp to an HH:mm display time (device zone).
     * Treats null/0 as "unset" and returns null.
     */
    private fun extractDisplayTime(ms: Long?): String? {
        if (ms == null || ms == 0L) return null
        val value = FleetEpoch.toValue(ms) ?: return null
        return com.indusjs.datetimeutils.FleetDateTime.formatTime(value)
    }

    fun List<TripPaymentDto>.toDomainList(): List<TripPayment> = map { it.toDomain() }

    fun TripPaymentListResponse.toDomain(): TripPaymentListResult = TripPaymentListResult(
        payments = data?.items?.toDomainList() ?: emptyList(),
        // Backend list response has no inline summary; stats come from /summary.
        summary = null,
        page = data?.page ?: 1,
        perPage = data?.perPage ?: 20,
        // Backend sends `count` (total matching rows), not `total`.
        total = data?.count ?: 0,
        totalPages = data?.totalPages ?: 1,
        hasMore = data?.hasMore ?: false
    )

    fun TripPaymentsHistoryDataDto.toDomain(): TripPaymentListResult = TripPaymentListResult(
        payments = payments.toDomainList(),
        // Backend now sends an authoritative per-trip `summary` block; surface it
        // so the trip-detail payments UI shows paid/pending/count/status without
        // deriving them from a paginated payment list.
        summary = summary?.toDomain(),
        page = page,
        perPage = perPage,
        // Backend sends `count`, not `total`.
        total = count,
        totalPages = totalPages,
        hasMore = hasMore
    )

    /**
     * Map the nested per-trip payment rollup into the domain TripPaymentSummary.
     * paid_amount → totalReceived, pending_amount → totalPending,
     * receipt_count → paymentCount/receivedCount, payment_status → paymentStatus.
     */
    fun TripPaymentsHistorySummaryDto.toDomain(): TripPaymentSummary = TripPaymentSummary(
        totalReceived = paidAmount,
        totalPending = pendingAmount,
        paymentCount = receiptCount.toInt(),
        receivedCount = receiptCount.toInt(),
        paymentStatus = paymentStatus
    )

    // Create request from domain data
    fun createRequest(
        tripId: Int?,
        vehicleId: Int?,
        driverId: Int?,
        customerId: Int?,
        customerName: String?,
        customerContact: String?,
        customerCompany: String?,
        customerGst: String?,
        amount: Double,
        tdsAmount: Double,
        discountAmount: Double,
        paymentType: PaymentType,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentDate: Long,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): CreateTripPaymentRequest = CreateTripPaymentRequest(
        // Backend requires trip_id and vehicle_id (binding:"required"); reject early
        // with a clear error rather than sending an omitted field that 400s.
        tripId = requireNotNull(tripId) { "trip_id is required to create a payment" },
        vehicleId = requireNotNull(vehicleId) { "vehicle_id is required to create a payment" },
        driverId = driverId,
        customerId = customerId,
        customerName = customerName,
        customerContact = customerContact,
        customerCompany = customerCompany,
        customerGst = customerGst,
        amount = amount,
        tdsAmount = tdsAmount,
        discountAmount = discountAmount,
        paymentType = paymentType.apiValue,
        paymentMode = paymentMode.apiValue,
        paymentSource = paymentSource,
        paymentDate = paymentDate,
        transactionId = transactionId,
        bankName = bankName,
        notes = notes,
        receivedBy = receivedBy,
        receivedAtLocation = receivedAtLocation
    )

    fun createAddToTripRequest(
        tripId: Int,
        vehicleId: Int,
        driverId: Int,
        customerId: Int?,
        amount: Double,
        tdsAmount: Double,
        discountAmount: Double,
        paymentType: PaymentType,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentDate: Long,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): AddPaymentToTripRequest = AddPaymentToTripRequest(
        tripId = tripId,
        vehicleId = vehicleId,
        driverId = driverId,
        customerId = customerId,
        amount = amount,
        tdsAmount = tdsAmount,
        discountAmount = discountAmount,
        paymentType = paymentType.apiValue,
        paymentMode = paymentMode.apiValue,
        paymentSource = paymentSource,
        paymentDate = paymentDate,
        transactionId = transactionId,
        bankName = bankName,
        notes = notes,
        receivedBy = receivedBy,
        receivedAtLocation = receivedAtLocation
    )

    fun createUpdateRequest(
        tdsAmount: Double?,
        discountAmount: Double?,
        paymentMode: PaymentMode,
        paymentSource: String?,
        paymentStatus: PaymentStatus?,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): UpdateTripPaymentRequest = UpdateTripPaymentRequest(
        tdsAmount = tdsAmount,
        discountAmount = discountAmount,
        // payment_mode is required by the backend on update.
        paymentMode = paymentMode.apiValue,
        paymentSource = paymentSource,
        paymentStatus = paymentStatus?.apiValue,
        transactionId = transactionId,
        bankName = bankName,
        notes = notes,
        receivedBy = receivedBy,
        receivedAtLocation = receivedAtLocation
    )
}
