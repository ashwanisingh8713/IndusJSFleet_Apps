package com.ijs.driver.domain.usecase

import com.indusjs.error.result.Result
import com.ijs.driver.domain.entity.Driver
import com.ijs.driver.domain.entity.DriverStatus
import com.ijs.driver.domain.repository.DriverRepository
import com.indusjs.fleet.domain.usecase.UseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting the list of drivers.
 */
@Inject
class GetDriversUseCase(
    private val driverRepository: DriverRepository
) : UseCase<List<Driver>> {

    override operator fun invoke(): Flow<Result<List<Driver>>> {
        return driverRepository.getDrivers()
    }

    /**
     * Get drivers with optional filters.
     */
    operator fun invoke(status: DriverStatus? = null, search: String? = null): Flow<Result<List<Driver>>> {
        return driverRepository.getDrivers(status, search)
    }
}

/**
 * Use case for getting available drivers for trip assignment.
 */
@Inject
class GetAvailableDriversUseCase(
    private val driverRepository: DriverRepository
) {
    operator fun invoke(): Flow<Result<List<Driver>>> {
        return driverRepository.getAvailableDrivers()
    }
}

/**
 * Use case for getting a driver by ID.
 */
@Inject
class GetDriverByIdUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(id: String): Result<Driver> {
        return driverRepository.getDriverById(id)
    }
}

/**
 * Use case for creating a new driver.
 */
@Inject
class CreateDriverUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(
        driver: Driver,
        password: String,
        caretakerId: Int? = null
    ): Result<Driver> {
        return driverRepository.createDriver(driver, password, caretakerId)
    }
}

/**
 * Use case for updating an existing driver.
 */
@Inject
class UpdateDriverUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(driver: Driver, caretakerId: Int? = null): Result<Driver> {
        return driverRepository.updateDriver(driver, caretakerId)
    }
}

/**
 * Use case for updating driver status.
 */
@Inject
class UpdateDriverStatusUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(id: String, status: DriverStatus): Result<Driver> {
        return driverRepository.updateDriverStatus(id, status)
    }
}

/**
 * Use case for toggling driver active state.
 */
@Inject
class ToggleDriverActiveUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(id: String): Result<Driver> {
        return driverRepository.toggleDriverActive(id)
    }
}

/**
 * Use case for deleting a driver.
 */
@Inject
class DeleteDriverUseCase(
    private val driverRepository: DriverRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> {
        return driverRepository.deleteDriver(id)
    }
}

