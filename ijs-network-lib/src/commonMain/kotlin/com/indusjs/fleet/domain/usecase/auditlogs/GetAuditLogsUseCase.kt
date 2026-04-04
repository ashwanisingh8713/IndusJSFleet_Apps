package com.indusjs.fleet.domain.usecase.auditlogs

import com.indusjs.error.result.Result
import com.indusjs.fleet.data.model.auditlogs.AuditLogsDataDto
import com.indusjs.fleet.domain.repository.auditlogs.AuditLogsRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case to fetch paginated audit logs with optional filters.
 * Only accessible to Owner and General Manager roles.
 */
@Inject
class GetAuditLogsUseCase(
    private val auditLogsRepository: AuditLogsRepository
) {
    operator fun invoke(
        page: Int = 1,
        perPage: Int = 20,
        entityType: String? = null,
        action: String? = null
    ): Flow<Result<AuditLogsDataDto>> =
        auditLogsRepository.getAuditLogs(page, perPage, entityType, action)
}
