package com.indusjs.fleet.feature.sample.domain.repository

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.repository.Repository
import com.indusjs.fleet.feature.sample.domain.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User operations.
 * Defined in the domain layer to be implemented by the data layer.
 */
interface UserRepository : Repository {

    /**
     * Get all users as a Flow.
     */
    fun getUsers(): Flow<Result<List<User>>>

    /**
     * Get a specific user by ID.
     */
    suspend fun getUserById(id: String): Result<User>

    /**
     * Create a new user.
     */
    suspend fun createUser(user: User): Result<User>

    /**
     * Update an existing user.
     */
    suspend fun updateUser(user: User): Result<User>

    /**
     * Delete a user by ID.
     */
    suspend fun deleteUser(id: String): Result<Unit>
}

