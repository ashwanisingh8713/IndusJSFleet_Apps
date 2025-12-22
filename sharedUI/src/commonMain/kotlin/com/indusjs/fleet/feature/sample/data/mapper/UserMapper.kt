package com.indusjs.fleet.feature.sample.data.mapper

import com.indusjs.fleet.data.mapper.Mapper
import com.indusjs.fleet.feature.sample.data.model.UserDto
import com.indusjs.fleet.feature.sample.domain.entity.User
import dev.zacsweers.metro.Inject

/**
 * Mapper between UserDto (data layer) and User (domain layer).
 */
@Inject
class UserMapper : Mapper<UserDto, User> {

    override fun mapToDomain(data: UserDto): User = User(
        id = data.id,
        name = data.name,
        email = data.email
    )

    override fun mapToData(entity: User): UserDto = UserDto(
        id = entity.id,
        name = entity.name,
        email = entity.email
    )
}

