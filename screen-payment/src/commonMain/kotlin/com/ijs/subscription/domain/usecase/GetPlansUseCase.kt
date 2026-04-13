package com.ijs.subscription.domain.usecase

import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.domain.repository.SubscriptionRepository

class GetPlansUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(): Result<List<Plan>> =
        repository.getPlans()
}
