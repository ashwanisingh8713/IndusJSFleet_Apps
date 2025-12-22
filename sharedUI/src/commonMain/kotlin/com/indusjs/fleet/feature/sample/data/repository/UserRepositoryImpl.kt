package com.indusjs.fleet.feature.sample.data.repository

import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.data.mapper.mapToDomainList
import com.indusjs.fleet.feature.sample.data.datasource.UserRemoteDataSource
import com.indusjs.fleet.feature.sample.data.mapper.UserMapper
import com.indusjs.fleet.feature.sample.domain.entity.User
import com.indusjs.fleet.feature.sample.domain.repository.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of UserRepository.
 * Follows the Repository pattern, coordinating between data sources.
 */
@Inject
class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val mapper: UserMapper
) : UserRepository {

    override fun getUsers(): Flow<Result<List<User>>> = flow {
        emit(Result.Loading)
        try {
            val users = remoteDataSource.getUsers()
            emit(Result.Success(mapper.mapToDomainList(users)))
        } catch (e: Exception) {
            emit(Result.Error(e, e.message))
        }
    }

    override suspend fun getUserById(id: String): Result<User> {
        return try {
            val userDto = remoteDataSource.getUserById(id)
            Result.Success(mapper.mapToDomain(userDto))
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun createUser(user: User): Result<User> {
        return try {
            val userDto = mapper.mapToData(user)
            val createdUserDto = remoteDataSource.createUser(userDto)
            Result.Success(mapper.mapToDomain(createdUserDto))
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return try {
            val userDto = mapper.mapToData(user)
            val updatedUserDto = remoteDataSource.updateUser(userDto)
            Result.Success(mapper.mapToDomain(updatedUserDto))
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }

    override suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            remoteDataSource.deleteUser(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }
}

