package com.indusjs.fleet.domain.entity.vehicle

/**
 * Vehicle status enumeration.
 */
enum class VehicleStatus {
    ACTIVE,
    INACTIVE,
    IN_MAINTENANCE,
    OUT_OF_SERVICE
}

/**
 * Vehicle type enumeration.
 */
enum class VehicleType {
    TRUCK,
    VAN,
    CAR,
    BUS,
    MOTORCYCLE,
    TRAILER
}

/**
 * Location data class for vehicles.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * Trip assignment info when vehicle is occupied.
 */
data class VehicleTripAssignment(
    val tripId: String,
    val tripState: String,
    val scheduledDate: String?,
    val startTime: String?,
    val plannedStart: String?,
    val plannedEnd: String?,
    val startLocation: String?,
    val endLocation: String?,
    val customerName: String?
)

/**
 * Assigned driver info for vehicle.
 */
data class AssignedDriver(
    val id: String,
    val firstName: String?,
    val lastName: String?,
    val mobile: String?,
    val licenseNumber: String?
) {
    /**
     * Returns the full name of the driver.
     */
    fun fullName(): String {
        val first = firstName ?: ""
        val last = lastName ?: ""
        return "$first $last".trim().ifEmpty { "N/A" }
    }
}

/**
 * Vehicle entity representing a fleet vehicle.
 */
data class Vehicle(
    val id: String,
    val registrationNumber: String,
    val make: String,
    val model: String,
    val year: Int,
    val type: VehicleType,
    val status: VehicleStatus,
    val fuelType: String = "petrol",
    val color: String = "white",
    val capacity: Int = 4,
    val fuelLevel: Int = 0,
    val mileage: Double = 0.0,
    val lastLocation: Location? = null,
    val assignedDriverId: String? = null,
    val assignedDriverName: String? = null,
    val assignedDriver: AssignedDriver? = null,
    val lastServiceDate: Long? = null,
    val nextServiceDate: Long? = null,
    val isOccupied: Boolean = false,
    val tripAssignment: VehicleTripAssignment? = null
)

/**
 * Types of vehicle documents that can be uploaded.
 */
enum class DocumentType {
    REGISTRATION_CERTIFICATE,
    INSURANCE,
    PUC_CERTIFICATE,       // Pollution Under Control
    FITNESS_CERTIFICATE,
    ROAD_TAX,
    PERMIT,
    DRIVER_LICENSE,
    OTHER
}

/**
 * Status of a document.
 */
enum class DocumentStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    EXPIRED
}

/**
 * Represents a vehicle document.
 */
data class VehicleDocument(
    val id: String,
    val vehicleId: String,
    val type: DocumentType,
    val name: String,
    val fileName: String,
    val fileSize: Long = 0L,
    val mimeType: String = "application/pdf",
    val uploadDate: Long = 0L,
    val expiryDate: Long? = null,
    val status: DocumentStatus = DocumentStatus.PENDING,
    val notes: String? = null,
    val fileUrl: String? = null,
    val localFilePath: String? = null,
    val fileBytes: ByteArray? = null // For holding file data during upload
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as VehicleDocument
        return id == other.id &&
                vehicleId == other.vehicleId &&
                type == other.type &&
                fileName == other.fileName
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + vehicleId.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + fileName.hashCode()
        return result
    }
}

/**
 * Represents a file to be uploaded (platform-agnostic).
 */
data class FileToUpload(
    val name: String,
    val bytes: ByteArray,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FileToUpload

        if (name != other.name) return false
        if (!bytes.contentEquals(other.bytes)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

