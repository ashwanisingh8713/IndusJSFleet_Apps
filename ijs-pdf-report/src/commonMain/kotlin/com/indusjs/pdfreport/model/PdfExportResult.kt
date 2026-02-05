package com.indusjs.pdfreport.model

/**
 * Result of PDF generation operation.
 */
data class PdfExportResult(
    val success: Boolean,
    val fileName: String,
    val filePath: String,
    val fileSize: Long = 0,
    val errorMessage: String? = null
) {
    companion object {
        fun success(fileName: String, filePath: String, fileSize: Long = 0): PdfExportResult {
            return PdfExportResult(
                success = true,
                fileName = fileName,
                filePath = filePath,
                fileSize = fileSize
            )
        }

        fun error(message: String): PdfExportResult {
            return PdfExportResult(
                success = false,
                fileName = "",
                filePath = "",
                errorMessage = message
            )
        }
    }
}
