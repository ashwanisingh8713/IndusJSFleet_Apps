package com.indusjs.fleet.feature.sample.domain.usecase

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.usecase.SuspendUseCaseWithParams
import com.indusjs.fleet.feature.sample.domain.entity.User
import com.indusjs.fleet.feature.sample.domain.repository.UserRepository
import dev.zacsweers.metro.Inject

/**
 * Use case for getting a user by ID.
 */
@Inject
class GetUserByIdUseCase(
    private val userRepository: UserRepository
) : SuspendUseCaseWithParams<String, User> {

    override suspend operator fun invoke(params: String): Result<User> {
        return userRepository.getUserById(params)
    }
}

