package com.indusjs.fleet.domain.usecase

import com.indusjs.error.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Base use case interface for executing business logic without parameters.
 * Returns a Flow to support reactive streams.
 */
interface UseCase<out R> {
    operator fun invoke(): Flow<Result<R>>
}

/**
 * Base use case interface for executing business logic with parameters.
 * Returns a Flow to support reactive streams.
 */
interface UseCaseWithParams<in P, out R> {
    operator fun invoke(params: P): Flow<Result<R>>
}

/**
 * Base use case interface for suspending operations without parameters.
 */
interface SuspendUseCase<out R> {
    suspend operator fun invoke(): Result<R>
}

/**
 * Base use case interface for suspending operations with parameters.
 */
interface SuspendUseCaseWithParams<in P, out R> {
    suspend operator fun invoke(params: P): Result<R>
}

