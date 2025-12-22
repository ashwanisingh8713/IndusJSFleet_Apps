package com.indusjs.fleet.feature.vehicles.domain.entity

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
    val localFilePath: String? = null
)

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

