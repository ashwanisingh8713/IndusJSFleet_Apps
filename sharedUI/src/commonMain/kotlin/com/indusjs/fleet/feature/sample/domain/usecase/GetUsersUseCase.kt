package com.indusjs.fleet.feature.sample.domain.usecase

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.usecase.UseCase
import com.indusjs.fleet.feature.sample.domain.entity.User
import com.indusjs.fleet.feature.sample.domain.repository.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the list of users.
 * Demonstrates the use case pattern in clean architecture.
 */
@Inject
class GetUsersUseCase(
    private val userRepository: UserRepository
) : UseCase<List<User>> {

    override operator fun invoke(): Flow<Result<List<User>>> {
        return userRepository.getUsers()
    }
}

