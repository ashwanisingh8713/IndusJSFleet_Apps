package com.ijs.trip.presentation.detail

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles Google Places location search and distance calculation
 * for the Trip Detail edit mode.
 */
internal class TripDetailLocationHandler(
    private val stateManager: TripDetailStateManager,
    private val dispatcherProvider: DispatcherProvider,
    private val googlePlacesService: GooglePlacesService?
) {
    private var startLocationSearchJob: Job? = null
    private var endLocationSearchJob: Job? = null

    fun searchStartLocation(query: String) {
        stateManager.updateTripState { copy(startLocationAddress = query) }

        if (googlePlacesService == null) return

        startLocationSearchJob?.cancel()

        if (query.length < 3) {
            stateManager.updateTripState {
                copy(
                    startLocationPredictions = emptyList(),
                    showStartLocationDropdown = false,
                    isSearchingStartLocation = false
                )
            }
            return
        }

        stateManager.updateTripState {
            copy(isSearchingStartLocation = true, showStartLocationDropdown = true)
        }

        startLocationSearchJob = CoroutineScope(dispatcherProvider.main).launch {
            delay(300) // Debounce
            withContext(dispatcherProvider.io) {
                googlePlacesService.searchPlaces(query).fold(
                    onSuccess = { predictions ->
                        stateManager.updateTripState {
                            copy(
                                startLocationPredictions = predictions,
                                isSearchingStartLocation = false,
                                showStartLocationDropdown = predictions.isNotEmpty()
                            )
                        }
                    },
                    onFailure = {
                        stateManager.updateTripState {
                            copy(
                                startLocationPredictions = emptyList(),
                                isSearchingStartLocation = false,
                                showStartLocationDropdown = false
                            )
                        }
                    }
                )
            }
        }
    }

    fun searchEndLocation(query: String) {
        stateManager.updateTripState { copy(endLocationAddress = query) }

        if (googlePlacesService == null) return

        endLocationSearchJob?.cancel()

        if (query.length < 3) {
            stateManager.updateTripState {
                copy(
                    endLocationPredictions = emptyList(),
                    showEndLocationDropdown = false,
                    isSearchingEndLocation = false
                )
            }
            return
        }

        stateManager.updateTripState {
            copy(isSearchingEndLocation = true, showEndLocationDropdown = true)
        }

        endLocationSearchJob = CoroutineScope(dispatcherProvider.main).launch {
            delay(300) // Debounce
            withContext(dispatcherProvider.io) {
                googlePlacesService.searchPlaces(query).fold(
                    onSuccess = { predictions ->
                        stateManager.updateTripState {
                            copy(
                                endLocationPredictions = predictions,
                                isSearchingEndLocation = false,
                                showEndLocationDropdown = predictions.isNotEmpty()
                            )
                        }
                    },
                    onFailure = {
                        stateManager.updateTripState {
                            copy(
                                endLocationPredictions = emptyList(),
                                isSearchingEndLocation = false,
                                showEndLocationDropdown = false
                            )
                        }
                    }
                )
            }
        }
    }

    suspend fun selectStartLocationPrediction(prediction: PlacePrediction) {
        stateManager.updateTripState {
            copy(
                startLocationAddress = prediction.description,
                showStartLocationDropdown = false,
                startLocationPredictions = emptyList(),
                startLocationError = null
            )
        }

        googlePlacesService?.let { service ->
            withContext(dispatcherProvider.io) {
                service.getPlaceDetails(prediction.placeId).fold(
                    onSuccess = { details ->
                        details.geometry?.location?.let { latLng ->
                            stateManager.updateTripState {
                                copy(
                                    startLat = latLng.lat.toString(),
                                    startLng = latLng.lng.toString()
                                )
                            }
                            calculateAndSetDistance()
                        }
                    },
                    onFailure = { /* Coordinates fetch failed */ }
                )
            }
        }
    }

    suspend fun selectEndLocationPrediction(prediction: PlacePrediction) {
        stateManager.updateTripState {
            copy(
                endLocationAddress = prediction.description,
                showEndLocationDropdown = false,
                endLocationPredictions = emptyList(),
                endLocationError = null
            )
        }

        googlePlacesService?.let { service ->
            withContext(dispatcherProvider.io) {
                service.getPlaceDetails(prediction.placeId).fold(
                    onSuccess = { details ->
                        details.geometry?.location?.let { latLng ->
                            stateManager.updateTripState {
                                copy(
                                    endLat = latLng.lat.toString(),
                                    endLng = latLng.lng.toString()
                                )
                            }
                            calculateAndSetDistance()
                        }
                    },
                    onFailure = { /* Coordinates fetch failed */ }
                )
            }
        }
    }

    private fun calculateAndSetDistance() {
        val state = stateManager.currentTripState

        val startLat = state.startLat.toDoubleOrNull()
        val startLng = state.startLng.toDoubleOrNull()
        val endLat = state.endLat.toDoubleOrNull()
        val endLng = state.endLng.toDoubleOrNull()

        if (startLat != null && startLng != null && endLat != null && endLng != null) {
            if (googlePlacesService != null) {
                CoroutineScope(dispatcherProvider.main).launch {
                    stateManager.updateTripState { copy(isCalculatingDistance = true) }
                    withContext(dispatcherProvider.io) {
                        googlePlacesService.getRoadDistance(startLat, startLng, endLat, endLng).fold(
                            onSuccess = { result ->
                                val distanceKm = kotlin.math.round(result.distanceKm * 10) / 10
                                stateManager.updateTripState {
                                    copy(
                                        estimatedDistance = distanceKm.toString(),
                                        estimatedDuration = result.durationText,
                                        isCalculatingDistance = false
                                    )
                                }
                            },
                            onFailure = {
                                val distance = calculateHaversineDistance(startLat, startLng, endLat, endLng)
                                val distanceKm = kotlin.math.round(distance * 10) / 10
                                stateManager.updateTripState {
                                    copy(
                                        estimatedDistance = distanceKm.toString(),
                                        estimatedDuration = "",
                                        isCalculatingDistance = false
                                    )
                                }
                            }
                        )
                    }
                }
            } else {
                val distance = calculateHaversineDistance(startLat, startLng, endLat, endLng)
                val distanceKm = kotlin.math.round(distance * 10) / 10
                stateManager.updateTripState {
                    copy(estimatedDistance = distanceKm.toString(), estimatedDuration = "")
                }
            }
        }
    }

    private fun calculateHaversineDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val lat1Rad = toRadians(lat1)
        val lat2Rad = toRadians(lat2)

        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2) *
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadiusKm * c
    }

    private fun toRadians(degrees: Double): Double {
        return degrees * kotlin.math.PI / 180.0
    }
}

