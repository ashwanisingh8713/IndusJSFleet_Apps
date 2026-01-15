package com.indusjs.fleet.domain.usecase.costs

import com.indusjs.fleet.data.model.costs.CostTypeCategoryDto
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import dev.zacsweers.metro.Inject

/**
 * Use case to get trip cost types from local cache.
 * Returns cached cost types or fallback hardcoded types if not cached.
 */
@Inject
class GetTripCostTypesUseCase(
    private val costTypesRepository: CostTypesRepository
) {
    /**
     * Get trip cost types as a flat list of id to name pairs.
     * Falls back to hardcoded types if cache is empty.
     */
    suspend operator fun invoke(): List<Pair<String, String>> {
        return costTypesRepository.getTripCostTypesFlat()
    }

    /**
     * Get full trip cost types category with groups.
     * Returns null if not cached.
     */
    suspend fun getGrouped(): CostTypeCategoryDto? {
        return costTypesRepository.getTripCostTypes()
    }
}

/**
 * Use case to get maintenance cost types from local cache.
 * Returns cached cost types or fallback hardcoded types if not cached.
 */
@Inject
class GetMaintenanceCostTypesUseCase(
    private val costTypesRepository: CostTypesRepository
) {
    /**
     * Get maintenance cost types as a flat list of id to name pairs.
     * Falls back to hardcoded types if cache is empty.
     */
    suspend operator fun invoke(): List<Pair<String, String>> {
        return costTypesRepository.getMaintenanceCostTypesFlat()
    }

    /**
     * Get full maintenance cost types category with groups.
     * Returns null if not cached.
     */
    suspend fun getGrouped(): CostTypeCategoryDto? {
        return costTypesRepository.getMaintenanceCostTypes()
    }
}

/**
 * Use case to get driver cost types from local cache.
 * Returns cached cost types or fallback hardcoded types if not cached.
 */
@Inject
class GetDriverCostTypesUseCase(
    private val costTypesRepository: CostTypesRepository
) {
    /**
     * Get driver cost types as a flat list of id to name pairs.
     * Falls back to hardcoded types if cache is empty.
     */
    suspend operator fun invoke(): List<Pair<String, String>> {
        return costTypesRepository.getDriverCostTypesFlat()
    }

    /**
     * Get full driver cost types category with groups.
     * Returns null if not cached.
     */
    suspend fun getGrouped(): CostTypeCategoryDto? {
        return costTypesRepository.getDriverCostTypes()
    }
}

