package com.ijs.subscription.domain.usecase

import com.ijs.subscription.domain.entity.OnboardingStatus
import com.ijs.subscription.domain.repository.SubscriptionRepository

class GetOnboardingStatusUseCase(private val repository: SubscriptionRepository) {
    suspend operator fun invoke(): Result<OnboardingStatus> =
        repository.getOnboardingStatus()
}
