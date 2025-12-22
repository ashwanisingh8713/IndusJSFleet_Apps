package com.indusjs.fleet.feature.sample.data.datasource

import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.feature.sample.data.model.UserDto
import dev.zacsweers.metro.Inject
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Interface for remote user data operations.
 */
interface UserRemoteDataSource : RemoteDataSource {
    suspend fun getUsers(): List<UserDto>
    suspend fun getUserById(id: String): UserDto
    suspend fun createUser(userDto: UserDto): UserDto
    suspend fun updateUser(userDto: UserDto): UserDto
    suspend fun deleteUser(id: String)
}

/**
 * Implementation of UserRemoteDataSource using Ktor.
 */
@Inject
class UserRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : UserRemoteDataSource {

    private val baseUrl = "https://api.example.com/users" // Replace with actual API URL

    override suspend fun getUsers(): List<UserDto> {
        return httpClient.get(baseUrl).body()
    }

    override suspend fun getUserById(id: String): UserDto {
        return httpClient.get("$baseUrl/$id").body()
    }

    override suspend fun createUser(userDto: UserDto): UserDto {
        return httpClient.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(userDto)
        }.body()
    }

    override suspend fun updateUser(userDto: UserDto): UserDto {
        return httpClient.put("$baseUrl/${userDto.id}") {
            contentType(ContentType.Application.Json)
            setBody(userDto)
        }.body()
    }

    override suspend fun deleteUser(id: String) {
        httpClient.delete("$baseUrl/$id")
    }
}

