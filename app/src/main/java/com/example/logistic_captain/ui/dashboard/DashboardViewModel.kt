package com.example.logistic_captain.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logistic_captain.data.RouteRepository
import com.example.logistic_captain.model.RouteResponse
import com.example.logistic_captain.model.StopResponse
import com.example.logistic_captain.model.DriverProfileResponse
import com.example.logistic_captain.model.DriverProfileUpdateRequest
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.*

class DashboardViewModel(private val repository: RouteRepository) : ViewModel() {

    var route by mutableStateOf<RouteResponse?>(null)
    var isShiftStarted by mutableStateOf(false)
    var isOnBreak by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var driverProfile by mutableStateOf<DriverProfileResponse?>(null)

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getMyProfile()
                if (response.isSuccessful) {
                    driverProfile = response.body()?.data
                } else {
                    errorMessage = "Failed to load profile"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(firstName: String, lastName: String, phoneNumber: String, vehicleType: String? = null, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val req = DriverProfileUpdateRequest(firstName = firstName, lastName = lastName, phoneNumber = phoneNumber, vehicleType = vehicleType)
                val response = repository.updateMyProfile(req)
                if (response.isSuccessful) {
                    driverProfile = response.body()?.data
                    onComplete(true)
                } else {
                    errorMessage = "Failed to update profile"
                    onComplete(false)
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onComplete(false)
            } finally {
                isLoading = false
            }
        }
    }

    fun loadRoute() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getMyRoute()
                if (response.isSuccessful) {
                    val loadedRoute = response.body()?.data
                    route = loadedRoute?.let { recalculateETAs(it) }
                } else {
                    errorMessage = "Failed to load route"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun startShift(lat: Double, lng: Double) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.checkIn(lat, lng)
                if (response.isSuccessful) {
                    isShiftStarted = true
                    loadRoute()
                } else {
                    isShiftStarted = true
                    loadRoute()
                }
            } catch (e: Exception) {
                isShiftStarted = true
                errorMessage = "Offline mode: ${e.message}"
                loadRoute()
            } finally {
                isLoading = false
            }
        }
    }

    fun endShift() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.checkOut()
                if (response.isSuccessful) {
                    isShiftStarted = false
                    route = null
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleBreak() {
        val nextBreakState = !isOnBreak
        viewModelScope.launch {
            try {
                val response = repository.toggleBreak(nextBreakState)
                if (response.isSuccessful) {
                    isOnBreak = nextBreakState
                } else {
                    errorMessage = "Failed to update break status"
                }
            } catch (e: Exception) {
                // If offline, still toggle locally but maybe show warning
                isOnBreak = nextBreakState
                errorMessage = "Offline: Break status saved locally"
            }
        }
    }

    fun updateStopStatus(stopId: String, status: String) {
        viewModelScope.launch {
            try {
                val response = repository.updateStopStatus(stopId, status, null)
                if (response.isSuccessful) {
                    loadRoute() // Refresh and recalculate ETAs
                }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    /**
     * Recalculates ETAs for all remaining (non-completed) stops.
     * Uses haversine distance between consecutive stops and average driving speed.
     */
    private fun recalculateETAs(routeResponse: RouteResponse): RouteResponse {
        val stops = routeResponse.stops
        if (stops.isEmpty()) return routeResponse

        val avgSpeedKmh = 25.0 // Average city driving speed
        val now = LocalDateTime.now()
        var currentTime = now
        val formatter = DateTimeFormatter.ofPattern("h:mm a")

        val updatedStops = mutableListOf<StopResponse>()
        var prevLat: Double? = null
        var prevLng: Double? = null

        for (stop in stops) {
            if (stop.status == "COMPLETED" || stop.status == "DELIVERED" ||
                stop.status == "ATTEMPTED_NO_ACCESS" || stop.status == "ATTEMPTED_ABSENT" ||
                stop.status == "FAILED") {
                updatedStops.add(stop)
                // Track last completed stop position for next ETA calc
                if (stop.latitude != null && stop.longitude != null) {
                    prevLat = stop.latitude
                    prevLng = stop.longitude
                }
                continue
            }

            // Calculate travel time from previous stop
            if (prevLat != null && prevLng != null && stop.latitude != null && stop.longitude != null) {
                val distanceKm = haversineKm(prevLat, prevLng, stop.latitude, stop.longitude)
                val travelMins = (distanceKm / avgSpeedKmh * 60).toLong()
                currentTime = currentTime.plusMinutes(travelMins)
            }

            val etaString = currentTime.format(formatter)
            val serviceTime = (stop.serviceTimeMins ?: 3).toLong()
            updatedStops.add(stop.copy(eta = etaString))

            // Add service time for next stop calculation
            currentTime = currentTime.plusMinutes(serviceTime)
            if (stop.latitude != null && stop.longitude != null) {
                prevLat = stop.latitude
                prevLng = stop.longitude
            }
        }

        return routeResponse.copy(stops = updatedStops)
    }

    /** Haversine formula — calculates great-circle distance between two GPS points in km. */
    private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}

