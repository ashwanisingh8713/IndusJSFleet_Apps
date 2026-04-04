package com.indusjs.fleet.data.repository.auditlogs

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.data.datasource.auditlogs.AuditLogsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.model.auditlogs.AuditLogsDataDto
import com.indusjs.fleet.domain.repository.auditlogs.AuditLogsRepository
import com.indusjs.fleet.network.TAG_AUDIT_LOGS_REPO
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Inject
class AuditLogsRepositoryImpl(
    private val remoteDataSource: AuditLogsRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val logger: FleetLogger
) : AuditLogsRepository {

    override fun getAuditLogs(
        page: Int,
        perPage: Int,
        entityType: String?,
        action: String?
    ): Flow<Result<AuditLogsDataDto>> = flow {
        emit(Result.Loading)

        val token = userLocalDataSource.getAuthToken()
        if (token.isNullOrBlank()) {
            emit(Result.Error(Exception("Not authenticated"), "Please log in to continue"))
            return@flow
        }

        val response = remoteDataSource.getAuditLogs(token, page, perPage, entityType, action)
        if (response.success && response.data != null) {
            logger.d(TAG_AUDIT_LOGS_REPO, "Fetched ${response.data.items.size} audit log entries (page $page)")
            emit(Result.Success(response.data))
        } else {
            emit(Result.Error(
                Exception(response.message),
                response.message ?: "Failed to fetch audit logs"
            ))
        }
    }
}
