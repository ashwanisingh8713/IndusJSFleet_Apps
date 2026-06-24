package com.ijs.map.domain

import com.indusjs.error.result.Result
import com.indusjs.fleet.domain.entity.maps.MapVehicle

/**
 * Cross-feature adapter that supplies the tenant's vehicles to the map module
 * as seed markers, without screen-map importing another feature's internals.
 *
 * The implementation lives in the sharedUI DI layer (see
 * `com.indusjs.fleet.di.adapter.MapVehicleProviderAdapter`) and bridges the
 * vehicle feature's repository to the map domain. This mirrors the existing
 * `TripProviderAdapter` pattern used to break cross-feature coupling.
 */
interface MapVehicleProvider {

    /**
     * Loads the tenant's vehicles mapped into [MapVehicle] seeds.
     *
     * Seeds carry the vehicle's last-known location when the backend provides
     * one; vehicles without a known position are still returned (so the fleet
     * list is complete) with a zeroed location and [com.indusjs.fleet.domain.entity.maps.MapVehicleStatus.OFFLINE].
     * Live positions then arrive over the WebSocket and update the markers.
     */
    suspend fun getMapVehicles(): Result<List<MapVehicle>>
}
