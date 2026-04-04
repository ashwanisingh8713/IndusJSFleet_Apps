package com.indusjs.fleet.domain.repository.auditlogs

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.auditlogs.AuditLogsDataDto
import kotlinx.coroutines.flow.Flow

/**
 * Repository for audit log operations.
 * Available to Owner and General Manager roles only.
 */
interface AuditLogsRepository {
    fun getAuditLogs(
        page: Int = 1,
        perPage: Int = 20,
        entityType: String? = null,
        action: String? = null
    ): Flow<Result<AuditLogsDataDto>>
}
