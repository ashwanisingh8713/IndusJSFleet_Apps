package com.ijs.subscription.domain.usecase

import com.ijs.subscription.domain.entity.TenantCreateResult
import com.ijs.subscription.domain.repository.SubscriptionRepository

class CreateTenantUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(organizationName: String, slug: String): Result<TenantCreateResult> =
        repository.createTenant(organizationName, slug)
}
