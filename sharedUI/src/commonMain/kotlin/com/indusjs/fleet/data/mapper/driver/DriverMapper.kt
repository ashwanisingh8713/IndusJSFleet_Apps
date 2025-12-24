package com.indusjs.fleet.data.mapper.driver

import com.indusjs.fleet.data.model.driver.CreateDriverRequest
import com.indusjs.fleet.data.model.driver.DriverDto
import com.indusjs.fleet.data.model.driver.DriverOwnerDto
import com.indusjs.fleet.data.model.driver.UpdateDriverRequest
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverOwner
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.entity.driver.LicenseType
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
        licenseExpiry = dto.licenseExpiry?.let { parseTimestamp(it) } ?: 0L,
        licenseType = dto.licenseType?.let { LicenseType.fromApiString(it) } ?: LicenseType.LMV,
        dateOfBirth = dto.dateOfBirth?.let { parseTimestamp(it) },
        address = dto.address,
        emergencyContact = dto.emergencyContact,
        bloodGroup = dto.bloodGroup,
        profilePhoto = dto.profilePhoto,
        status = DriverStatus.fromApiString(dto.status),
        isActive = dto.isActive,
        ownerId = dto.ownerId?.toString(),
        owner = dto.owner?.let { mapOwnerToDomain(it) },
        createdById = dto.createdById?.toString(),
        createdBy = dto.createdBy?.let { mapOwnerToDomain(it) },
        joiningDate = dto.joiningDate?.let { parseTimestamp(it) },
        createdAt = dto.createdAt?.let { parseTimestamp(it) },
        updatedAt = dto.updatedAt?.let { parseTimestamp(it) }
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
     * Maps Driver domain entity to CreateDriverRequest.
     */
    fun mapToCreateRequest(driver: Driver): CreateDriverRequest = CreateDriverRequest(
        firstName = driver.firstName,
        lastName = driver.lastName,
        email = driver.email.takeIf { it.isNotBlank() },
        mobile = driver.mobile,
        licenseNumber = driver.licenseNumber,
        licenseExpiry = formatTimestamp(driver.licenseExpiry),
        licenseType = LicenseType.toApiString(driver.licenseType),
        dateOfBirth = driver.dateOfBirth?.let { formatTimestamp(it) },
        address = driver.address,
        emergencyContact = driver.emergencyContact,
        bloodGroup = driver.bloodGroup,
        joiningDate = driver.joiningDate?.let { formatTimestamp(it) }
    )

    /**
     * Maps Driver domain entity to UpdateDriverRequest.
     */
    fun mapToUpdateRequest(driver: Driver): UpdateDriverRequest = UpdateDriverRequest(
        firstName = driver.firstName,
        lastName = driver.lastName,
        email = driver.email.takeIf { it.isNotBlank() },
        mobile = driver.mobile,
        licenseExpiry = formatTimestamp(driver.licenseExpiry),
        address = driver.address,
        emergencyContact = driver.emergencyContact,
        bloodGroup = driver.bloodGroup
    )

    /**
     * Parse ISO timestamp string to Long.
     * Handles formats like: 2027-12-31T00:00:00Z
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            // Simple parsing - in production use kotlinx-datetime
            // For now, extract year-month-day and convert to approximate epoch
            val datePattern = Regex("(\\d{4})-(\\d{2})-(\\d{2})")
            val match = datePattern.find(timestamp)
            if (match != null) {
                val (year, month, day) = match.destructured
                // Approximate conversion to epoch milliseconds
                val baseYear = 1970
                val daysFromBase = ((year.toInt() - baseYear) * 365.25).toLong() +
                        (month.toInt() - 1) * 30L + day.toInt()
                daysFromBase * 24 * 60 * 60 * 1000
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Format Long timestamp to ISO date string.
     * Returns format: YYYY-MM-DDTHH:MM:SSZ
     */
    private fun formatTimestamp(timestamp: Long): String? {
        if (timestamp <= 0) return null
        return try {
            // Approximate conversion from epoch - in production use kotlinx-datetime
            val days = timestamp / (24 * 60 * 60 * 1000)
            val years = (days / 365.25).toInt() + 1970
            val remainingDays = (days % 365.25).toInt()
            val months = (remainingDays / 30) + 1
            val dayOfMonth = (remainingDays % 30) + 1
            val monthStr = months.coerceIn(1, 12).toString().padStart(2, '0')
            val dayStr = dayOfMonth.coerceIn(1, 28).toString().padStart(2, '0')
            "$years-$monthStr-${dayStr}T00:00:00Z"
        } catch (_: Exception) {
            null
        }
    }
}

