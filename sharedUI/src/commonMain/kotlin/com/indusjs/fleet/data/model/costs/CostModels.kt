package com.indusjs.fleet.data.model.costs

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip Cost Types.
 */
object TripCostTypes {
    val types = listOf(
        "fuel" to "Fuel",
        "toll" to "Toll",
        "driver_allowance" to "Driver Allowance",
        "loading" to "Loading",
        "unloading" to "Unloading",
        "parking" to "Parking",
        "rto" to "RTO",
        "police" to "Police",
        "repair" to "Repair",
        "food" to "Food",
        "halt" to "Halt",
        "commission" to "Commission",
        "weighing" to "Weighing",
        "detention" to "Detention",
        "miscellaneous" to "Miscellaneous",
        "other" to "Other"
    )
}

/**
 * Maintenance Cost Types.
 */
object MaintenanceCostTypes {
    val types = listOf(
        "tyre" to "Tyre",
        "oil_change" to "Oil Change",
        "servicing" to "Servicing",
        "battery" to "Battery",
        "brake" to "Brake",
        "engine" to "Engine",
        "body_work" to "Body Work",
        "electrical" to "Electrical",
        "cleaning" to "Cleaning",
        "other" to "Other"
    )
}

/**
 * Fuel Types.
 */
object FuelTypes {
    val types = listOf(
        "diesel" to "Diesel",
        "petrol" to "Petrol",
        "cng" to "CNG"
    )
}

/**
 * Trip Cost API response.
 */
@Serializable
data class TripCostApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripCostDto? = null
)

/**
 * Trip Cost Types API response.
 */
@Serializable
data class CostTypesApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CostTypesDataDto? = null
)

@Serializable
data class CostTypesDataDto(
    val types: List<CostTypeDto> = emptyList()
) : Dto

@Serializable
data class CostTypeDto(
    val id: String,
    val name: String,
    val description: String? = null
) : Dto

/**
 * Trip Cost DTO.
 */
@Serializable
data class TripCostDto(
    val id: Int = 0,
    @SerialName("trip_id")
    val tripId: String,
    @SerialName("vehicle_id")
    val vehicleId: String,
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val notes: String? = null,
    // Fuel specific fields
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto

/**
 * Trip Costs List API response.
 */
@Serializable
data class TripCostsListApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripCostsListDto? = null
)

@Serializable
data class TripCostsListDto(
    val costs: List<TripCostDto> = emptyList(),
    val total: Double = 0.0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
) : Dto

/**
 * Create Trip Cost request.
 * Note: API expects trip_id and vehicle_id as integers.
 */
@Serializable
data class CreateTripCostRequest(
    @SerialName("trip_id")
    val tripId: Int,
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val notes: String? = null,
    // Fuel specific
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null
)

/**
 * Bulk Create Trip Costs request - for /trips/{trip_id}/costs/bulk API.
 */
@Serializable
data class BulkCreateTripCostsRequest(
    val costs: List<BulkCostItem>
)

/**
 * Individual cost item for bulk creation.
 * Note: trip_id is taken from URL path and vehicle_id from the trip record.
 * Do NOT include trip_id or vehicle_id in the request body.
 */
@Serializable
data class BulkCostItem(
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val notes: String? = null,
    // Fuel specific
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("fuel_quantity")
    val fuelQuantity: Double? = null,
    @SerialName("fuel_rate")
    val fuelRate: Double? = null,
    @SerialName("km_per_liter")
    val kmPerLiter: Double? = null
)

/**
 * Bulk Create Trip Costs API response.
 */
@Serializable
data class BulkTripCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BulkTripCostsResultDto? = null
)

@Serializable
data class BulkTripCostsResultDto(
    val created: Int = 0,
    val costs: List<TripCostDto> = emptyList()
) : Dto

/**
 * Maintenance Cost DTO.
 */
@Serializable
data class MaintenanceCostDto(
    val id: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: String,
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
) : Dto

/**
 * Maintenance Cost API response.
 */
@Serializable
data class MaintenanceCostApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MaintenanceCostDto? = null
)

/**
 * Maintenance Costs List API response.
 */
@Serializable
data class MaintenanceCostsListApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MaintenanceCostsListDto? = null
)

@Serializable
data class MaintenanceCostsListDto(
    val costs: List<MaintenanceCostDto> = emptyList(),
    val total: Double = 0.0,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1
) : Dto

/**
 * Create Maintenance Cost request.
 * Note: API expects vehicle_id as integer.
 */
@Serializable
data class CreateMaintenanceCostRequest(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null
)

/**
 * Bulk Create Maintenance Costs request - for /vehicles/{vehicle_id}/maintenance-costs/bulk API.
 */
@Serializable
data class BulkCreateMaintenanceCostsRequest(
    val costs: List<BulkMaintenanceCostItem>
)

/**
 * Individual maintenance cost item for bulk creation.
 * Note: vehicle_id is passed in the URL path, not in the body.
 */
@Serializable
data class BulkMaintenanceCostItem(
    @SerialName("cost_type")
    val costType: String,
    val amount: Double,
    val date: String,
    val time: String? = null,
    val description: String? = null,
    val notes: String? = null,
    @SerialName("vendor_name")
    val vendorName: String? = null,
    @SerialName("invoice_no")
    val invoiceNo: String? = null
)

/**
 * Bulk Create Maintenance Costs API response.
 */
@Serializable
data class BulkMaintenanceCostsApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: BulkMaintenanceCostsResultDto? = null
)

@Serializable
data class BulkMaintenanceCostsResultDto(
    val created: Int = 0,
    val costs: List<MaintenanceCostDto> = emptyList()
) : Dto

/**
 * Trip Cost Summary API response - for /trips/{trip_id}/costs/summary API.
 */
@Serializable
data class TripCostSummaryApiResponse(
    val success: Boolean,
    val message: String? = null,
    val data: TripCostSummaryDto? = null
)

/**
 * Trip Cost Summary DTO.
 * Contains total cost and breakdown by cost type.
 */
@Serializable
data class TripCostSummaryDto(
    val total: Double = 0.0,
    @SerialName("by_type")
    val byType: Map<String, Double> = emptyMap(),
    @SerialName("cost_count")
    val costCount: Int = 0
) : Dto

