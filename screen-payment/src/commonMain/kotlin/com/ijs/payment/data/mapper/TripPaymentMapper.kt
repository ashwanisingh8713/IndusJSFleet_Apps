package com.ijs.payment.data.mapper

import com.ijs.payment.data.model.*
import com.ijs.payment.domain.entity.*
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
        // Extract dates - use flat fields first, then trip object fields
        tripStartDate = extractDateFromIso(
            tripScheduledDate ?: tripStartTime
                ?: trip?.scheduledDate ?: trip?.startTime
                ?: trip?.plannedStart
        ),
        tripEndDate = extractDateFromIso(
            tripDeliveryDate ?: tripDeliveryTime
                ?: trip?.deliveryDate ?: trip?.deliveryTime
                ?: trip?.plannedEnd
        ),
        // Extract times - use flat fields first
        tripStartTime = extractTimeFromIso(tripStartTime ?: trip?.startTime ?: trip?.plannedStart),
        tripEndTime = extractTimeFromIso(tripDeliveryTime ?: trip?.deliveryTime ?: trip?.plannedEnd)
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
        // Extract date from plannedStart or scheduledDate (ISO 8601 format)
        tripStartDate = extractDateFromIso(plannedStart ?: scheduledDate),
        tripEndDate = extractDateFromIso(plannedEnd ?: deliveryDate),
        // Extract time from plannedStart/plannedEnd
        tripStartTime = extractTimeFromIso(plannedStart),
        tripEndTime = extractTimeFromIso(plannedEnd)
    )

    /**
     * Extract date in DD-MM-YYYY format from ISO 8601 datetime string.
     * Filters out invalid/default dates like 0001-01-01.
     */
    private fun extractDateFromIso(isoOrDate: String?): String? {
        if (isoOrDate.isNullOrBlank()) return null
        // Filter out invalid/default dates (0001-01-01, etc.)
        if (isoOrDate.startsWith("0001-") || isoOrDate.startsWith("0000-")) return null
        return try {
            // Try parsing as ISO 8601 first (YYYY-MM-DDTHH:mm:ssZ)
            if (isoOrDate.contains("T") && isoOrDate.length >= 10) {
                val datePart = isoOrDate.substring(0, 10)
                val parts = datePart.split("-")
                if (parts.size == 3 && parts[0].toIntOrNull()?.let { it > 1900 } == true) {
                    "${parts[2]}-${parts[1]}-${parts[0]}" // Convert to DD-MM-YYYY
                } else null
            } else if (isoOrDate.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
                // Already in DD-MM-YYYY format - validate year
                val year = isoOrDate.takeLast(4).toIntOrNull()
                if (year != null && year > 1900) isoOrDate else null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Extract time in HH:mm format from ISO 8601 datetime string.
     * Filters out invalid/default times.
     */
    private fun extractTimeFromIso(isoDate: String?): String? {
        if (isoDate.isNullOrBlank()) return null
        // Filter out invalid/default dates
        if (isoDate.startsWith("0001-") || isoDate.startsWith("0000-")) return null
        return try {
            if (isoDate.contains("T") && isoDate.length >= 16) {
                isoDate.substring(11, 16) // Extract HH:mm
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun List<TripPaymentDto>.toDomainList(): List<TripPayment> = map { it.toDomain() }

    fun TripPaymentSummaryDto.toDomain(): TripPaymentSummary = TripPaymentSummary(
        totalReceived = totalReceived,
        totalPending = totalPending,
        totalCancelled = totalCancelled,
        totalTds = totalTds,
        totalDiscount = totalDiscount,
        totalNetAmount = totalNetAmount,
        paymentCount = paymentCount,
        receivedCount = receivedCount,
        pendingCount = pendingCount,
        thisMonthTotal = thisMonthTotal,
        byMode = byMode?.mapKeys { PaymentMode.fromApiValue(it.key) } ?: emptyMap(),
        byType = byType?.mapKeys { PaymentType.fromApiValue(it.key) } ?: emptyMap()
    )

    fun TripPaymentListResponse.toDomain(): TripPaymentListResult = TripPaymentListResult(
        payments = data?.items?.toDomainList() ?: emptyList(),
        summary = data?.summary?.toDomain(),
        page = data?.page ?: 1,
        perPage = data?.perPage ?: 20,
        total = data?.total ?: data?.count ?: 0,
        totalPages = data?.totalPages ?: 1,
        hasMore = data?.hasMore ?: false
    )

    fun TripPaymentsHistoryDataDto.toDomain(): TripPaymentListResult = TripPaymentListResult(
        payments = payments.toDomainList(),
        summary = TripPaymentSummary(
            totalReceived = paidTripPrice,
            totalPending = pendingAmount,
            paymentCount = paymentCount
        ),
        page = page,
        perPage = perPage,
        total = total,
        totalPages = totalPages,
        hasMore = page < totalPages
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
        paymentDate: String,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): CreateTripPaymentRequest = CreateTripPaymentRequest(
        tripId = tripId,
        vehicleId = vehicleId,
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
        paymentDate: String,
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
        amount: Double?,
        tdsAmount: Double?,
        discountAmount: Double?,
        paymentType: PaymentType?,
        paymentMode: PaymentMode?,
        paymentSource: String?,
        paymentDate: String?,
        paymentStatus: PaymentStatus?,
        transactionId: String?,
        bankName: String?,
        notes: String?,
        receivedBy: String?,
        receivedAtLocation: String?
    ): UpdateTripPaymentRequest = UpdateTripPaymentRequest(
        amount = amount,
        tdsAmount = tdsAmount,
        discountAmount = discountAmount,
        paymentType = paymentType?.apiValue,
        paymentMode = paymentMode?.apiValue,
        paymentSource = paymentSource,
        paymentDate = paymentDate,
        paymentStatus = paymentStatus?.apiValue,
        transactionId = transactionId,
        bankName = bankName,
        notes = notes,
        receivedBy = receivedBy,
        receivedAtLocation = receivedAtLocation
    )
}
