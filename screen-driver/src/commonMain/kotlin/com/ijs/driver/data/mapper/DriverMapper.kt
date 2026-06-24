package com.ijs.driver.data.mapper

import com.ijs.driver.data.model.CreateDriverRequest
import com.ijs.driver.data.model.DriverDto
import com.ijs.driver.data.model.DriverOwnerDto
import com.ijs.driver.data.model.TripAssignmentDto
import com.ijs.driver.data.model.UpdateDriverRequest
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverOwner
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.entity.DriverTripAssignment
import com.ijs.driver.domain.entity.LicenseType
import dev.zacsweers.metro.Inject

/**
 * Mapper for converting between Driver DTOs and domain entities.
 */
@Inject
class DriverMapper {

    /**
     * Maps DriverDto to Driver domain entity.
     */
    fun mapToDomain(dto: DriverDto): Driver = Driver(
        id = dto.id.toString(),
        firstName = dto.firstName,
        lastName = dto.lastName,
        email = dto.email ?: "",
        mobile = dto.mobile,
        licenseNumber = dto.licenseNumber,
        licenseExpiry = dto.licenseExpiry ?: 0L,
        licenseType = dto.licenseType?.let { LicenseType.fromApiString(it) } ?: LicenseType.LMV,
        dateOfBirth = dto.dateOfBirth,
        address = dto.address,
        emergencyContact = dto.emergencyContact,
        bloodGroup = dto.bloodGroup,
        profilePhoto = dto.profilePhoto,
        status = DriverStatus.fromApiString(dto.status),
        isActive = dto.isActive,
        isOccupied = dto.isOccupied,
        tripAssignment = dto.tripAssignment?.let { mapTripAssignmentToDomain(it) },
        ownerId = dto.ownerId?.toString(),
        owner = dto.owner?.let { mapOwnerToDomain(it) },
        createdById = dto.createdById?.toString(),
        createdBy = dto.createdBy?.let { mapOwnerToDomain(it) },
        joiningDate = dto.joiningDate,
        createdAt = dto.createdAt,
        updatedAt = dto.updatedAt
    )

    /**
     * Maps a list of DriverDto to a list of Driver domain entities.
     */
    fun mapToDomainList(dtos: List<DriverDto>): List<Driver> = dtos.map { mapToDomain(it) }

    /**
     * Maps DriverOwnerDto to DriverOwner domain entity.
     */
    private fun mapOwnerToDomain(dto: DriverOwnerDto): DriverOwner = DriverOwner(
        id = dto.id.toString(),
        email = dto.email,
        firstName = dto.firstName,
        lastName = dto.lastName,
        role = dto.role
    )

    /**
     * Maps TripAssignmentDto to DriverTripAssignment domain entity.
     */
    private fun mapTripAssignmentToDomain(dto: TripAssignmentDto): DriverTripAssignment = DriverTripAssignment(
        tripId = dto.tripId.toString(),
        tripState = dto.tripState,
        scheduledDate = dto.scheduledDate,
        startTime = dto.startTime,
        plannedStart = dto.plannedStart,
        plannedEnd = dto.plannedEnd,
        startLocation = dto.startLocation,
        endLocation = dto.endLocation,
        customerName = dto.customerName
    )

    /**
     * Maps Driver domain entity to CreateDriverRequest.
     */
    fun mapToCreateRequest(
        driver: Driver,
        password: String,
        caretakerId: Int? = null
    ): CreateDriverRequest = CreateDriverRequest(
        password = password,
        firstName = driver.firstName,
        lastName = driver.lastName,
        email = driver.email.takeIf { it.isNotBlank() },
        mobile = driver.mobile,
        licenseNumber = driver.licenseNumber,
        licenseExpiry = driver.licenseExpiry.takeIf { it > 0L },
        licenseType = LicenseType.toApiString(driver.licenseType),
        dateOfBirth = driver.dateOfBirth,
        address = driver.address,
        emergencyContact = driver.emergencyContact,
        bloodGroup = driver.bloodGroup,
        joiningDate = driver.joiningDate,
        caretakerId = caretakerId
    )

    /**
     * Maps Driver domain entity to UpdateDriverRequest.
     */
    fun mapToUpdateRequest(driver: Driver, caretakerId: Int? = null): UpdateDriverRequest = UpdateDriverRequest(
        firstName = driver.firstName,
        lastName = driver.lastName,
        email = driver.email.takeIf { it.isNotBlank() },
        mobile = driver.mobile,
        licenseNumber = driver.licenseNumber,
        licenseExpiry = driver.licenseExpiry.takeIf { it > 0L },
        licenseType = LicenseType.toApiString(driver.licenseType),
        dateOfBirth = driver.dateOfBirth,
        address = driver.address,
        emergencyContact = driver.emergencyContact,
        bloodGroup = driver.bloodGroup,
        joiningDate = driver.joiningDate,
        caretakerId = caretakerId
    )
}
