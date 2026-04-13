package com.ijs.subscription.domain.usecase

import com.ijs.subscription.domain.repository.SubscriptionRepository

class SelectPlanUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(planId: String): Result<Unit> =
        repository.selectPlan(planId)
}
