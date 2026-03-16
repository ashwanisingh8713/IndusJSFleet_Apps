package com.indusjs.fleet.data.mapper.finance

import com.indusjs.fleet.data.model.finance.*
import com.indusjs.fleet.domain.entity.finance.*

/**
 * Mapper functions for Vehicle Finance DTOs to Domain entities
 */

// ==================== Vehicle Purchase Mapping ====================

fun VehiclePurchaseDto.toDomain(): VehiclePurchase {
    return VehiclePurchase(
        id = id,
        vehicleId = vehicleId,
        vehicle = vehicle?.toDomain(),
        purchaseDate = purchaseDate,
        purchasePrice = purchasePrice,
        vendorName = vendorName,
        invoiceNumber = invoiceNumber,
        paymentType = PaymentType.fromValue(paymentType),
        downPayment = downPayment,
        loanAmount = loanAmount ?: 0.0,
        interestRate = interestRate ?: 0.0,
        tenureMonths = tenureMonths ?: 0,
        emiAmount = emiAmount ?: 0.0,
        loanStartDate = loanStartDate,
        loanEndDate = loanEndDate,
        financierName = financierName,
        loanAccountNumber = loanAccountNumber,
        bankName = bankName,
        bankAccountNumber = bankAccountNumber,
        bankIfsc = bankIfsc,
        autoDebitEnabled = autoDebitEnabled,
        totalPaid = totalPaid,
        outstandingBalance = outstandingBalance,
        emisPaid = emisPaid,
        emisRemaining = emisRemaining,
        nextEmiDueDate = nextEmiDueDate,
        loanStatus = LoanStatus.fromValue(loanStatus),
        notes = notes,
        ownerId = ownerId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun VehicleBasicInfoDto.toDomain(): VehicleBasicInfo {
    return VehicleBasicInfo(
        id = id,
        registrationNumber = registrationNumber,
        make = make,
        model = model
    )
}

// ==================== Loan Payment Mapping ====================

fun LoanPaymentDto.toDomain(): LoanPayment {
    return LoanPayment(
        id = id,
        vehiclePurchaseId = vehiclePurchaseId,
        vehicleId = vehicleId,
        emiNumber = emiNumber,
        dueDate = dueDate,
        amount = amount,
        paymentDate = paymentDate,
        principalAmount = principalAmount ?: 0.0,
        interestAmount = interestAmount ?: 0.0,
        lateFee = lateFee ?: 0.0,
        prepaymentAmount = prepaymentAmount ?: 0.0,
        entryType = EntryType.fromValue(entryType),
        paymentMode = PaymentMode.fromValue(paymentMode),
        paymentSource = paymentSource,
        paymentStatus = PaymentStatus.fromValue(paymentStatus),
        transactionRef = transactionRef,
        notes = notes,
        ownerId = ownerId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ==================== Loan Summary Mapping ====================

fun LoanSummaryDto.toDomain(): LoanSummary {
    return LoanSummary(
        vehicleId = vehicleId,
        vehiclePurchaseId = vehiclePurchaseId,
        vehicle = vehicle?.toDomain(),
        loanAmount = loanAmount,
        emiAmount = emiAmount,
        tenureMonths = tenureMonths,
        interestRate = interestRate,
        totalPaid = totalPaid,
        outstandingBalance = outstandingBalance,
        emisPaid = emisPaid,
        emisRemaining = emisRemaining,
        nextEmiDueDate = nextEmiDueDate,
        nextEmiAmount = nextEmiAmount,
        loanStatus = LoanStatus.fromValue(loanStatus),
        financierName = financierName
    )
}

// ==================== EMI Alert Mapping ====================

fun EmiAlertDto.toDomain(): EmiAlert {
    return EmiAlert(
        paymentId = paymentId,
        vehicleId = vehicleId,
        vehiclePurchaseId = vehiclePurchaseId,
        vehicle = vehicle?.toDomain(),
        emiNumber = emiNumber,
        dueDate = dueDate,
        amount = amount,
        daysUntilDue = daysUntilDue,
        daysOverdue = daysOverdue,
        isOverdue = isOverdue,
        financierName = financierName
    )
}

// ==================== List Mappings ====================

fun List<LoanPaymentDto>.toDomainPayments(): List<LoanPayment> = map { it.toDomain() }

fun List<EmiAlertDto>.toDomainAlerts(): List<EmiAlert> = map { it.toDomain() }
