package com.indusjs.fleet.data.mapper.payment

import com.indusjs.fleet.data.model.payment.*
import com.indusjs.fleet.domain.entity.payment.*

/**
 * Mapper for trip payment DTOs to domain entities.
 */
object TripPaymentMapper {

    fun TripPaymentDto.toDomain(): TripPayment = TripPayment(
        id = id.toString(),
        tripId = tripId.toString(),
        vehicleId = vehicleId?.toString(),
        driverId = driverId?.toString(),
        customerId = customerId?.toString(),
        customerName = customerName,
        customerContact = customerContact,
        customerCompany = customerCompany,
        customerGst = customerGst,
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
        tripInfo = trip?.toDomain()
    )

    fun TripPaymentTripInfoDto.toDomain(): TripPaymentTripInfo = TripPaymentTripInfo(
        tripId = id?.toString(),
        vehicleRegistration = vehicle?.registrationNumber,
        vehicleMake = vehicle?.make,
        vehicleModel = vehicle?.model,
        driverName = driver?.fullName,
        startLocation = startLocation,
        endLocation = endLocation,
        tripPrice = expectedTripPrice,
        tripState = state
    )

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
